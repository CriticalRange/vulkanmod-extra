package com.criticalrange.client;
import com.criticalrange.VulkanModExtra;
import net.minecraft.text.Text;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
/**
 * Runtime integration with VulkanMod's GUI system
 * This class uses reflection to add VulkanMod Extra options to VulkanMod's settings screen
 */
public class VulkanModExtraIntegration {
    private static boolean integrationAttempted = false;
    private static boolean integrationSuccessful = false;
    // Track which screen instances have already been injected to prevent multiple injections
    private static final Map<Object, Boolean> injectedInstances = new WeakHashMap<>();
    // Debounced resource reload to prevent multiple reloads when changing multiple settings
    private static java.util.concurrent.ScheduledFuture<?> pendingResourceReload;
    private static final java.util.concurrent.ScheduledExecutorService resourceReloadScheduler = 
        java.util.concurrent.Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "VulkanModExtra-ResourceReload");
            t.setDaemon(true);
            return t;
        });
    /**
     * Schedule a debounced resource reload to avoid multiple reloads when changing multiple settings
     */
    private static void scheduleResourceReload() {
        // Cancel any pending reload
        if (pendingResourceReload != null && !pendingResourceReload.isDone()) {
            pendingResourceReload.cancel(false);
        }
        // Schedule a new reload with a 500ms delay
        pendingResourceReload = resourceReloadScheduler.schedule(() -> {
            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
            if (minecraft != null) {
                minecraft.execute(() -> {
                    VulkanModExtra.LOGGER.info("Reloading resources due to animation setting changes");
                    minecraft.reloadResources();
                });
            }
        }, 500, java.util.concurrent.TimeUnit.MILLISECONDS);
    }
    /**
     * Attempt to integrate with VulkanMod's GUI system at runtime
     * This should be called when VulkanMod is loaded
     */
    public static void tryIntegrateWithVulkanMod() {
        if (integrationAttempted) {
            return; // Only attempt once
        }
        integrationAttempted = true;
        try {
            VulkanModExtra.LOGGER.info("Attempting to integrate VulkanMod Extra with VulkanMod GUI...");
            // Check if VulkanMod classes are available
            Class<?> vOptionScreenClass = findClass("net.vulkanmod.config.gui.VOptionScreen");
            if (vOptionScreenClass == null) {
                VulkanModExtra.LOGGER.warn("VulkanMod not found, skipping GUI integration");
                return;
            }
            // Try to set up the mixin integration
            // Note: The actual mixin will handle the GUI integration
            integrationSuccessful = true;
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to integrate with VulkanMod GUI", e);
            // Don't rethrow - let the mod continue without GUI integration
        }
    }
    /**
     * Safe method to inject pages into the currently active VulkanMod screen
     * Called by the mixin after screen initialization is complete
     */
    public static void injectPagesIntoCurrentScreen() {
        // Check if GUI integration is disabled
        if (!VulkanModExtra.CONFIG.coreSettings.enableGuiIntegration) {
            return;
        }
        if (integrationAttempted && !integrationSuccessful) {
            return; // Don't retry if we already failed
        }
        try {
            // Get the current screen through Minecraft client
            Class<?> minecraftClass = findClass("net.minecraft.client.MinecraftClient");
            if (minecraftClass == null) {
                VulkanModExtra.LOGGER.warn("Could not find MinecraftClient class");
                return;
            }
            VulkanModExtra.LOGGER.debug("Found MinecraftClient class: {}", minecraftClass.getName());
            // Get the singleton Minecraft instance
            Object minecraft = getStaticFieldValue(minecraftClass, "INSTANCE");
            if (minecraft == null) {
                VulkanModExtra.LOGGER.warn("Could not get Minecraft instance");
                return;
            }
            VulkanModExtra.LOGGER.debug("Got Minecraft instance");
            Object currentScreen = getFieldValue(minecraft, "currentScreen");
            if (currentScreen == null) {
                VulkanModExtra.LOGGER.debug("No current screen found");
                return;
            }
            VulkanModExtra.LOGGER.debug("Found current screen: {}", currentScreen.getClass().getName());
            // Check if it's a VulkanMod options screen
            if (!isVulkanModOptionScreen(currentScreen)) {
                VulkanModExtra.LOGGER.debug("Current screen is not a VulkanMod options screen");
                return;
            }
            VulkanModExtra.LOGGER.info("Found VulkanMod options screen, creating extra pages...");
            // Try to add our pages to this screen
            List<Object> extraPages = createVulkanModExtraPages();
            if (extraPages != null && !extraPages.isEmpty()) {
                addPagesToScreen(currentScreen, extraPages);
                integrationSuccessful = true;
                VulkanModExtra.LOGGER.info("Successfully injected VulkanMod Extra pages into active screen!");
            }
            
            // Optimization options are now directly integrated into VulkanMod's Optimizations page
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Could not inject pages into current screen: {}", e.getMessage());
        }
    }
    /**
     * Check if the given screen is a VulkanMod options screen
     */
    private static boolean isVulkanModOptionScreen(Object screen) {
        if (screen == null) return false;
        String className = screen.getClass().getName();
        VulkanModExtra.LOGGER.debug("Checking screen class: {}", className);
        // More flexible detection - just check for vulkanmod in the class name
        boolean isVulkanModScreen = className.toLowerCase().contains("vulkanmod");
        boolean isOptionScreen = className.toLowerCase().contains("option") || className.toLowerCase().contains("screen");
        VulkanModExtra.LOGGER.debug("Is VulkanMod screen: {}, Is option screen: {}", isVulkanModScreen, isOptionScreen);
        return isVulkanModScreen && isOptionScreen;
    }
    /**
     * Try to add extra pages to an active screen
     */
    private static void addPagesToScreen(Object screen, List<Object> extraPages) {
        try {
            // Look for common field names that might hold option pages
            String[] possibleFieldNames = {"optionPages", "pages", "tabs", "categories"};
            for (String fieldName : possibleFieldNames) {
                try {
                    java.lang.reflect.Field field = screen.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object fieldValue = field.get(screen);
                    if (fieldValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> pages = (List<Object>) fieldValue;
                        pages.addAll(extraPages);
                        VulkanModExtra.LOGGER.info("Added {} extra pages to VulkanMod screen via field {}", extraPages.size(), fieldName);
                        return;
                    }
                } catch (NoSuchFieldException e) {
                    // Field not found, continue trying other fields
                }
            }
            // If we couldn't find a pages field, try to call addPage methods
            tryAddPagesViaMethods(screen, extraPages);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Could not add pages to screen: {}", e.getMessage());
        }
    }
    
    /**
     * Add VulkanMod Extra optimization options to the existing VulkanMod Optimizations page
     */
    private static void addOptimizationOptionsToScreen(Object screen) {
        try {
            if (!loadVulkanModClasses()) {
                VulkanModExtra.LOGGER.warn("Cannot add optimization options - VulkanMod classes not available");
                return;
            }
            
            // Get the optionPages field from the screen
            java.lang.reflect.Field optionPagesField = screen.getClass().getDeclaredField("optionPages");
            optionPagesField.setAccessible(true);
            Object optionPagesObj = optionPagesField.get(screen);
            
            if (!(optionPagesObj instanceof List)) {
                VulkanModExtra.LOGGER.warn("optionPages field is not a List");
                return;
            }
            
            @SuppressWarnings("unchecked")
            List<Object> optionPages = (List<Object>) optionPagesObj;
            
            // Find the Optimizations page (check all pages to find the correct one)
            Object optimizationsPage = null;
            int optimizationsIndex = -1;
            
            // Debug: Log all available pages
            VulkanModExtra.LOGGER.debug("Available pages count: {}", optionPages.size());
            for (int i = 0; i < optionPages.size(); i++) {
                Object page = optionPages.get(i);
                try {
                    // Try to get the page name/title using different possible field names
                    String pageName = "Unknown";
                    try {
                        java.lang.reflect.Field nameField = page.getClass().getDeclaredField("name");
                        nameField.setAccessible(true);
                        pageName = (String) nameField.get(page);
                    } catch (NoSuchFieldException e1) {
                        try {
                            java.lang.reflect.Field titleField = page.getClass().getDeclaredField("title");
                            titleField.setAccessible(true);
                            pageName = titleField.get(page).toString();
                        } catch (NoSuchFieldException e2) {
                            try {
                                java.lang.reflect.Field displayNameField = page.getClass().getDeclaredField("displayName");
                                displayNameField.setAccessible(true);
                                pageName = displayNameField.get(page).toString();
                            } catch (NoSuchFieldException e3) {
                                pageName = page.getClass().getSimpleName();
                            }
                        }
                    }
                    
                    VulkanModExtra.LOGGER.debug("Page {}: {}", i, pageName);
                    
                    // Check if this is the Optimizations page
                    if (pageName.contains("Optimization") || pageName.contains("optimization") || 
                        (i == 2 && optionPages.size() > 2)) { // Fallback to index 2 if name doesn't match
                        optimizationsPage = page;
                        optimizationsIndex = i;
                    }
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.debug("Page {}: (could not get name)", i);
                }
            }
            
            if (optimizationsPage != null) {
                VulkanModExtra.LOGGER.debug("Found Optimizations page at index {}: {}", optimizationsIndex, optimizationsPage);
                
                // Get the optionBlocks field from the page (it's an array, not a List)
                java.lang.reflect.Field optionBlocksField = optimizationsPage.getClass().getDeclaredField("optionBlocks");
                optionBlocksField.setAccessible(true);
                Object optionBlocksObj = optionBlocksField.get(optimizationsPage);
                
                if (!optionBlocksObj.getClass().isArray()) {
                    VulkanModExtra.LOGGER.warn("optionBlocks field is not an array");
                    return;
                }
                
                // Get the current option blocks array
                Object[] currentOptionBlocks = (Object[]) optionBlocksObj;
                VulkanModExtra.LOGGER.debug("Current option blocks count: {}", currentOptionBlocks.length);
                
                // Create optimization options to add
                VulkanModExtra.LOGGER.info("Creating optimization options...");
                List<Object> extraOptimizationOptions = createExtraOptimizationOptions(cachedSwitchOptionClass);
                VulkanModExtra.LOGGER.info("Created {} optimization options", extraOptimizationOptions.size());
                
                if (!extraOptimizationOptions.isEmpty()) {
                    try {
                        // Create a new option block for our extra options
                        Class<?> optionArrayClass = java.lang.reflect.Array.newInstance(cachedOptionClass, 0).getClass();
                        Object[] extraOptionsArray = extraOptimizationOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionClass, extraOptimizationOptions.size()));
                        VulkanModExtra.LOGGER.info("Created option array with {} elements", extraOptionsArray.length);
                        
                        Object extraOptionBlock = cachedOptionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("VulkanMod Extra Optimizations", extraOptionsArray);
                        VulkanModExtra.LOGGER.info("Created option block successfully");
                        
                        // Create a new larger array to hold both existing and new option blocks
                        Object[] newOptionBlocks = (Object[]) java.lang.reflect.Array.newInstance(currentOptionBlocks.getClass().getComponentType(), currentOptionBlocks.length + 1);
                        
                        // Copy existing option blocks
                        System.arraycopy(currentOptionBlocks, 0, newOptionBlocks, 0, currentOptionBlocks.length);
                        
                        // Add our new option block at the end
                        newOptionBlocks[currentOptionBlocks.length] = extraOptionBlock;
                        
                        // Replace the optionBlocks field with our new array
                        optionBlocksField.set(optimizationsPage, newOptionBlocks);
                        
                        VulkanModExtra.LOGGER.info("Successfully added VulkanMod Extra optimization options to Optimizations page. New total: {} option blocks", newOptionBlocks.length);
                        
                        // Also try to update the option list if it exists
                        try {
                            java.lang.reflect.Field optionListField = optimizationsPage.getClass().getDeclaredField("optionList");
                            optionListField.setAccessible(true);
                            Object optionList = optionListField.get(optimizationsPage);
                            if (optionList != null) {
                                // Try to call addAll method on the option list
                                java.lang.reflect.Method addAllMethod = optionList.getClass().getMethod("addAll", Object[].class);
                                addAllMethod.invoke(optionList, (Object) new Object[]{extraOptionBlock});
                                VulkanModExtra.LOGGER.debug("Updated option list with new option block");
                            }
                        } catch (Exception e) {
                            VulkanModExtra.LOGGER.debug("Could not update option list: {}", e.getMessage());
                        }
                        
                    } catch (Exception e) {
                        VulkanModExtra.LOGGER.error("Failed to create and add optimization option block: {}", e.getMessage(), e);
                    }
                } else {
                    VulkanModExtra.LOGGER.warn("No optimization options were created");
                }
            } else {
                VulkanModExtra.LOGGER.warn("Optimizations page not found (page count: {})", optionPages.size());
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Could not add optimization options to screen: {}", e.getMessage());
        }
    }
    
    /**
     * Create extra optimization options for the VulkanMod Optimizations page
     */
    private static List<Object> createExtraOptimizationOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();
        VulkanModExtra.LOGGER.debug("Starting to create optimization options...");
        
        // Create option factory function
        java.util.function.BiFunction<String, java.util.function.Consumer<Boolean>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.render." + key),
                        setter,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.renderSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.renderSettings);
                            } catch (Exception e) { return false; }
                        });
            } catch (Exception e) { return null; }
        };
        
        // Create option factory for extra settings
        java.util.function.BiFunction<String, java.util.function.Consumer<Boolean>, Object> createExtraOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.render." + key),
                        setter,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.extraSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.extraSettings);
                            } catch (Exception e) { return false; }
                        });
            } catch (Exception e) { return null; }
        };
        
        // Add render optimization options
        VulkanModExtra.LOGGER.debug("Creating render optimization options...");
        
        // Light updates
        Object lightUpdatesOption = createOption.apply("lightUpdates", value -> {
            VulkanModExtra.CONFIG.renderSettings.lightUpdates = value;
            VulkanModExtra.CONFIG.writeChanges();
        });
        if (lightUpdatesOption != null) {
            try {
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(lightUpdatesOption, Text.translatable("vulkanmod-extra.option.lightUpdates.tooltip"));
            } catch (Exception e) {}
            options.add(lightUpdatesOption);
            VulkanModExtra.LOGGER.debug("Successfully added lightUpdates option");
        }
        
        // Item frame rendering
        Object itemFrameOption = createOption.apply("itemFrame", value -> {
            VulkanModExtra.CONFIG.renderSettings.itemFrame = value;
            VulkanModExtra.CONFIG.writeChanges();
        });
        if (itemFrameOption != null) {
            try {
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(itemFrameOption, Text.translatable("vulkanmod-extra.option.itemFrame.tooltip"));
            } catch (Exception e) {}
            options.add(itemFrameOption);
            VulkanModExtra.LOGGER.debug("Successfully added itemFrame option");
        }
        
        // Armor stand rendering
        Object armorStandOption = createOption.apply("armorStand", value -> {
            VulkanModExtra.CONFIG.renderSettings.armorStand = value;
            VulkanModExtra.CONFIG.writeChanges();
        });
        if (armorStandOption != null) {
            try {
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(armorStandOption, Text.translatable("vulkanmod-extra.option.armorStand.tooltip"));
            } catch (Exception e) {}
            options.add(armorStandOption);
            VulkanModExtra.LOGGER.debug("Successfully added armorStand option");
        }
        
        // Painting rendering
        Object paintingOption = createOption.apply("painting", value -> {
            VulkanModExtra.CONFIG.renderSettings.painting = value;
            VulkanModExtra.CONFIG.writeChanges();
        });
        if (paintingOption != null) {
            try {
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(paintingOption, Text.translatable("vulkanmod-extra.option.painting.tooltip"));
            } catch (Exception e) {}
            options.add(paintingOption);
            VulkanModExtra.LOGGER.debug("Successfully added painting option");
        }
        
        // Piston rendering
        Object pistonOption = createOption.apply("piston", value -> {
            VulkanModExtra.CONFIG.renderSettings.piston = value;
            VulkanModExtra.CONFIG.writeChanges();
        });
        if (pistonOption != null) {
            try {
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(pistonOption, Text.translatable("vulkanmod-extra.option.piston.tooltip"));
            } catch (Exception e) {}
            options.add(pistonOption);
            VulkanModExtra.LOGGER.debug("Successfully added piston option");
        }
        
        // Beacon beam rendering
        Object beaconBeamOption = createOption.apply("beaconBeam", value -> {
            VulkanModExtra.CONFIG.renderSettings.beaconBeam = value;
            VulkanModExtra.CONFIG.writeChanges();
        });
        if (beaconBeamOption != null) {
            try {
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(beaconBeamOption, Text.translatable("vulkanmod-extra.option.beaconBeam.tooltip"));
            } catch (Exception e) {}
            options.add(beaconBeamOption);
            VulkanModExtra.LOGGER.debug("Successfully added beaconBeam option");
        }
        
        return options;
    }

    /**
     * Try to add pages using method calls
     */
    private static void tryAddPagesViaMethods(Object screen, List<Object> extraPages) {
        try {
            // Look for methods like addPage, addTab, etc.
            String[] possibleMethods = {"addPage", "addTab", "addCategory", "addOptionPage"};
            for (String methodName : possibleMethods) {
                try {
                    java.lang.reflect.Method method = screen.getClass().getDeclaredMethod(methodName, Object.class);
                    method.setAccessible(true);
                    for (Object page : extraPages) {
                        method.invoke(screen, page);
                    }
                    VulkanModExtra.LOGGER.info("Added {} pages via method {} to VulkanMod screen", extraPages.size(), methodName);
                    return;
                } catch (NoSuchMethodException e) {
                    // Method not found, continue trying other methods
                }
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Could not add pages via methods: {}", e.getMessage());
        }
    }
    // Cache VulkanMod classes to avoid repeated reflection lookups
    private static Class<?> cachedOptionPageClass;
    private static Class<?> cachedOptionBlockClass;
    private static Class<?> cachedSwitchOptionClass;
    private static Class<?> cachedCyclingOptionClass;
    private static Class<?> cachedOptionClass;
    private static boolean classesLoaded = false;
    private static boolean loadVulkanModClasses() {
        if (classesLoaded) return true;
        try {
            cachedOptionPageClass = Class.forName("net.vulkanmod.config.option.OptionPage");
            cachedOptionBlockClass = Class.forName("net.vulkanmod.config.gui.OptionBlock");
            cachedSwitchOptionClass = Class.forName("net.vulkanmod.config.option.SwitchOption");
            cachedCyclingOptionClass = Class.forName("net.vulkanmod.config.option.CyclingOption");
            cachedOptionClass = Class.forName("net.vulkanmod.config.option.Option");
            classesLoaded = true;
            return true;
        } catch (ClassNotFoundException e) {
            VulkanModExtra.LOGGER.error("Failed to load VulkanMod classes for GUI integration", e);
            return false;
        }
    }
    /**
     * Create VulkanMod-compatible option pages using cached reflection classes
     */
    public static List<Object> createVulkanModExtraPages() {
        if (!loadVulkanModClasses()) {
            VulkanModExtra.LOGGER.warn("Cannot create VulkanMod Extra pages - VulkanMod classes not available");
            return new ArrayList<>();
        }
        VulkanModExtra.LOGGER.debug("Creating VulkanMod Extra pages...");
        List<Object> pages = new ArrayList<>();
        try {
            VulkanModExtra.LOGGER.debug("VulkanMod classes loaded successfully");
            // Pre-calculate array classes for performance
            Class<?> optionArrayClass = java.lang.reflect.Array.newInstance(cachedOptionClass, 0).getClass();
            Class<?> optionBlockArrayClass = java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 0).getClass();
            // Create Animation page with separate blocks for master toggle and individual options
            VulkanModExtra.LOGGER.debug("Creating animation page...");
            Object[] animationOptionBlocks = createAnimationOptionBlocks(cachedSwitchOptionClass, cachedOptionBlockClass, optionArrayClass);
            Object animationPage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.animations").getString(), animationOptionBlocks);
            pages.add(animationPage);
            // Create Particle page with separate blocks for master toggle and individual options
            VulkanModExtra.LOGGER.debug("Creating particle page...");
            Object[] particleOptionBlocks = createParticleOptionBlocks(cachedSwitchOptionClass, cachedOptionBlockClass, optionArrayClass);
            Object particlePage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.particles").getString(), particleOptionBlocks);
            pages.add(particlePage);
            // Create Details page with comprehensive options
            VulkanModExtra.LOGGER.debug("Creating details page...");
            List<Object> detailOptions = createDetailOptions(cachedSwitchOptionClass);
            Object[] detailArray = detailOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionClass, detailOptions.size()));
            Object detailBlock = cachedOptionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Detail Settings", detailArray);
            Object[] detailBlocks = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 1);
            detailBlocks[0] = detailBlock;
            Object detailPage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.details").getString(), detailBlocks);
            pages.add(detailPage);
            // Create Render page with comprehensive options including fog
            VulkanModExtra.LOGGER.debug("Creating render page...");
            Object[] renderBlocks = createRenderOptionBlocks(cachedSwitchOptionClass, cachedOptionBlockClass, optionArrayClass, cachedOptionClass);
            Object renderPage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.render").getString(), renderBlocks);
            pages.add(renderPage);
            // Create Extra page with multiple option blocks for better organization
            VulkanModExtra.LOGGER.debug("Creating Extra page...");
            Object[] extraOptionBlocks = createExtraOptionBlocks(cachedSwitchOptionClass, cachedCyclingOptionClass, cachedOptionBlockClass, optionArrayClass, cachedOptionClass);
            Object extraPage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.extra").getString(), extraOptionBlocks);
            pages.add(extraPage);
            VulkanModExtra.LOGGER.info("Successfully created {} VulkanMod Extra pages", pages.size());
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create VulkanMod Extra pages", e);
        }
        return pages;
    }
    /**
     * Create VulkanMod Extra pages with proper typing for direct mixin injection
     */
    public static List<net.vulkanmod.config.option.OptionPage> createVulkanModExtraOptionPages() {
        List<net.vulkanmod.config.option.OptionPage> pages = new ArrayList<>();
        try {
            // Load VulkanMod classes
            Class<?> optionBlockClass = Class.forName("net.vulkanmod.config.gui.OptionBlock");
            Class<?> switchOptionClass = Class.forName("net.vulkanmod.config.option.SwitchOption");
            Class<?> cyclingOptionClass = Class.forName("net.vulkanmod.config.option.CyclingOption");
            Class<?> optionClass = Class.forName("net.vulkanmod.config.option.Option");
            Class<?> optionPageClass = Class.forName("net.vulkanmod.config.option.OptionPage");
            Class<?> optionArrayClass = java.lang.reflect.Array.newInstance(optionClass, 0).getClass();
            Class<?> optionBlockArrayClass = java.lang.reflect.Array.newInstance(optionBlockClass, 0).getClass();
            // Create Animation page with separate blocks for master toggle and individual options
            Object[] animationOptionBlocks = createAnimationOptionBlocks(switchOptionClass, optionBlockClass, optionArrayClass);
            net.vulkanmod.config.option.OptionPage animationPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.animations").getString(), animationOptionBlocks);
            pages.add(animationPage);
            // Create Particle page with separate blocks for master toggle and individual options
            Object[] particleOptionBlocks = createParticleOptionBlocks(switchOptionClass, optionBlockClass, optionArrayClass);
            net.vulkanmod.config.option.OptionPage particlePage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.particles").getString(), particleOptionBlocks);
            pages.add(particlePage);
            // Create Details page with comprehensive options
            List<Object> detailOptions = createDetailOptions(switchOptionClass);
            Object[] detailArray = detailOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, detailOptions.size()));
            Object detailBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Detail Settings", detailArray);
            Object[] detailBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            detailBlocks[0] = detailBlock;
            net.vulkanmod.config.option.OptionPage detailPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.details").getString(), detailBlocks);
            pages.add(detailPage);
            // Create Render page with comprehensive options including fog
            VulkanModExtra.LOGGER.debug("Creating render page...");
            Object[] renderBlocks = createRenderOptionBlocks(switchOptionClass, optionBlockClass, optionArrayClass, optionClass);
            net.vulkanmod.config.option.OptionPage renderPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.render").getString(), renderBlocks);
            pages.add(renderPage);
            // Create Extra page with multiple option blocks for better organization
            Object[] extraOptionBlocks = createExtraOptionBlocks(switchOptionClass, cyclingOptionClass, optionBlockClass, optionArrayClass, optionClass);
            net.vulkanmod.config.option.OptionPage extraPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Text.translatable("vulkanmod-extra.pages.extra").getString(), extraOptionBlocks);
            pages.add(extraPage);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create VulkanMod Extra pages", e);
        }
        return pages;
    }
    /**
     * Custom List implementation that dynamically handles VulkanMod Extra pages
     * This prevents IndexOutOfBoundsException by providing safe access to all pages
     */
    private static class CustomPageList extends ArrayList<Object> {
        private static final long serialVersionUID = 1L;
        private transient final List<Object> originalPages;
        private transient final List<Object> extraPages;
        public CustomPageList(List<Object> originalPages, List<Object> extraPages) {
            super();
            this.originalPages = originalPages;
            this.extraPages = extraPages;
            // Add all pages to this list
            addAll(originalPages);
            addAll(extraPages);
        }
        @Override
        public Object get(int index) {
            try {
                // First try to get from original pages
                if (index < originalPages.size()) {
                    return originalPages.get(index);
                }
                // Then try to get from extra pages
                else if (index < originalPages.size() + extraPages.size()) {
                    return extraPages.get(index - originalPages.size());
                }
                // If index is out of bounds, return null instead of crashing
                else {
                    return null;
                }
            } catch (Exception e) {
                return null;
            }
        }
        @Override
        public int size() {
            return originalPages.size() + extraPages.size();
        }
        @Override
        public boolean contains(Object o) {
            return originalPages.contains(o) || extraPages.contains(o);
        }
        @Override
        public int indexOf(Object o) {
            int index = originalPages.indexOf(o);
            if (index >= 0) return index;
            index = extraPages.indexOf(o);
            if (index >= 0) return originalPages.size() + index;
            return -1;
        }
    }
    /**
     * Alternative integration method using mixin injection
     * This would be called by the MixinVOptionScreen
     */
    public static void injectPagesIntoVulkanMod(Object vOptionScreenInstance) {
        try {
            // Check if this screen instance has already been injected
            if (injectedInstances.containsKey(vOptionScreenInstance)) {
                return;
            }
            // Get the VulkanMod Extra pages
            List<Object> extraPages = createVulkanModExtraPages();
            // Get the optionPages field from VOptionScreen
            Class<?> vOptionScreenClass = vOptionScreenInstance.getClass();
            java.lang.reflect.Field optionPagesField = vOptionScreenClass.getDeclaredField("optionPages");
            optionPagesField.setAccessible(true);
            // Cast to the correct type
            @SuppressWarnings("unchecked")
            List<Object> originalOptionPages = (List<Object>) optionPagesField.get(vOptionScreenInstance);
            // Create a custom page list that wraps the original and adds our pages
            CustomPageList customPageList = new CustomPageList(originalOptionPages, extraPages);
            // Replace the original page list with our custom one
            optionPagesField.set(vOptionScreenInstance, customPageList);
                   // Initialize the VOptionList for each new page
                   try {
                       // Use VulkanMod's standard dimensions
                       int top = 40;
                       int bottom = 60;
                       int itemHeight = 20;
                       int leftMargin = 100;
                       int listWidth = 277;
                       int listHeight = 160;
                       // Initialize each new page
                       for (Object page : extraPages) {
                           try {
                               java.lang.reflect.Method createListMethod = page.getClass().getMethod("createList", int.class, int.class, int.class, int.class, int.class);
                               createListMethod.invoke(page, leftMargin, top, listWidth, listHeight, itemHeight);
                           } catch (Exception e) {
                               // Page initialization failed, continue with other pages
                           }
                       }
                   } catch (Exception e) {
                       // Page list initialization failed
                   }
                   // Refresh the UI to include our new pages
                   try {
                       java.lang.reflect.Method buildPageMethod = vOptionScreenInstance.getClass().getDeclaredMethod("buildPage");
                       buildPageMethod.setAccessible(true);
                       buildPageMethod.invoke(vOptionScreenInstance);
                   } catch (Exception e) {
                       // UI refresh failed
                   }
                   VulkanModExtra.LOGGER.info("Successfully injected {} VulkanMod Extra pages into GUI", extraPages.size());
                   // Mark this screen instance as injected to prevent multiple injections for this instance
                   injectedInstances.put(vOptionScreenInstance, true);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to inject pages into VulkanMod GUI", e);
        }
    }
    private static Class<?> findClass(String className) {
        try {
            return Class.forName(className, false, VulkanModExtraIntegration.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            // Try with different class loaders
            try {
                return Class.forName(className, false, Thread.currentThread().getContextClassLoader());
            } catch (ClassNotFoundException ex) {
                return null;
            }
        }
    }
    private static Object getFieldValue(Object instance, String fieldName) {
        try {
            java.lang.reflect.Field field = instance.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(instance);
        } catch (Exception e) {
            return null;
        }
    }
    private static Object getStaticFieldValue(Class<?> clazz, String fieldName) {
        try {
            java.lang.reflect.Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(null);
        } catch (Exception e) {
            return null;
        }
    }
    public static boolean isIntegrationSuccessful() {
        return integrationSuccessful;
    }
    // Comprehensive option creation methods
    private static List<Object> createAnimationOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();
        // Helper for consistent option creation with master toggle awareness
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.animation." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                // Simply check the individual animation setting - no master toggle
                                var field = VulkanModExtra.CONFIG.animationSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.animationSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };
        // Master toggle for all animations - first option (works as override)
        Object masterOption;
        try {
            masterOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(Text.translatable("vulkanmod-extra.option.animation.allAnimations"),
                    (java.util.function.Consumer<Boolean>) value -> {
                        try {
                            VulkanModExtra.CONFIG.animationSettings.allAnimations = value;
                            VulkanModExtra.CONFIG.writeChanges();
                            scheduleResourceReload();
                            VulkanModExtra.LOGGER.info("Set allAnimations to: " + value);
                        } catch (Exception e) {
                            VulkanModExtra.LOGGER.error("Failed to set all animations option", e);
                        }
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.animationSettings.allAnimations);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create all animations option", e);
            masterOption = null;
        }
        if (masterOption != null) {
            try {
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(masterOption, Text.translatable("vulkanmod-extra.option.animation.allAnimations.tooltip"));
            } catch (Exception e) {}
            options.add(masterOption);
            VulkanModExtra.LOGGER.info("Successfully added All Animations master toggle");
        }
        // Comprehensive animation options - organized by category for better UX
        // Fluid animations
        String[] fluidAnimations = {
            "water", "water_still", "water_flow", "lava", "lava_still", "lava_flow"
        };
        // Fire & light animations
        String[] fireAnimations = {
            "fire", "fire0", "fire1", "soul_fire", "soul_fire0", "soul_fire1", 
            "campfire_fire", "soul_campfire_fire", "lantern", "soul_lantern", "sea_lantern"
        };
        // Portal animations
        String[] portalAnimations = {
            "portal", "nether_portal", "end_portal", "end_gateway"
        };
        // Block animations
        String[] blockAnimations = {
            "block_animations", "magma", "prismarine", "prismarine_bricks", "dark_prismarine", 
            "conduit", "respawn_anchor", "stonecutter_saw"
        };
        // Machine animations
        String[] machineAnimations = {
            "machine_animations", "blast_furnace_front_on", "smoker_front_on", "furnace_front_on"
        };
        // Plant animations
        String[] plantAnimations = {
            "plant_animations", "kelp", "kelp_plant", "seagrass", "tall_seagrass_bottom", "tall_seagrass_top"
        };
        // Stem animations
        String[] stemAnimations = {
            "stem_animations", "warped_stem", "crimson_stem", "warped_hyphae", "crimson_hyphae"
        };
        // Sculk animations
        String[] sculkAnimations = {
            "sculk_animations", "sculk", "sculk_vein", "sculk_sensor", "sculk_sensor_side", "sculk_sensor_top",
            "sculk_shrieker", "sculk_shrieker_side", "sculk_shrieker_top", "calibrated_sculk_sensor", 
            "calibrated_sculk_sensor_side", "calibrated_sculk_sensor_top"
        };
        // Command block animations
        String[] commandBlockAnimations = {
            "command_block_animations", "command_block_front", "chain_command_block_front", "repeating_command_block_front"
        };
        // Additional animations
        String[] additionalAnimations = {
            "additional_animations", "beacon", "dragon_egg", "brewing_stand_base", "cauldron_water", "enchanting_table_book"
        };
        // Combine all animation types (each controls only its own behavior)
        String[][] allAnimationCategories = {
            fluidAnimations, fireAnimations, portalAnimations,
            blockAnimations, machineAnimations, plantAnimations, stemAnimations, 
            sculkAnimations, commandBlockAnimations, additionalAnimations
        };
        for (String[] categoryAnimations : allAnimationCategories) {
            for (String type : categoryAnimations) {
                Object option = createOption.apply(type, value -> {
                    try {
                        var field = VulkanModExtra.CONFIG.animationSettings.getClass().getDeclaredField(type);
                        field.setAccessible(true);
                        field.setBoolean(VulkanModExtra.CONFIG.animationSettings, value);
                        VulkanModExtra.CONFIG.writeChanges();
                        // Schedule a debounced resource reload to apply animation changes
                        scheduleResourceReload();
                    } catch (Exception e) {
                        VulkanModExtra.LOGGER.error("Failed to set animation option: " + type, e);
                    }
                    return null;
                });
                if (option != null) {
                    try {
                        // Set tooltip using reflection
                        java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                        setTooltipMethod.invoke(option, Text.translatable("vulkanmod-extra.option.animation." + type + ".tooltip"));
                    } catch (Exception e) {
                        // Tooltip setting failed, continue without tooltip
                    }
                    options.add(option);
                }
            }
        }
        return options;
    }
    private static List<Object> createParticleOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();
        // Helper for creating particle options with individual control
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.particle." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.particleSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.particleSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };
        // Master toggle for all particles - first option (works as override)
        Object masterOption;
        try {
            masterOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(Text.translatable("vulkanmod-extra.option.particle.allParticles"),
                    (java.util.function.Consumer<Boolean>) value -> {
                        try {
                            VulkanModExtra.CONFIG.particleSettings.allParticles = value;
                            VulkanModExtra.CONFIG.writeChanges();
                            scheduleResourceReload();
                            VulkanModExtra.LOGGER.info("Set allParticles to: " + value);
                        } catch (Exception e) {
                            VulkanModExtra.LOGGER.error("Failed to set all particles option", e);
                        }
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.particleSettings.allParticles);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create all particles option", e);
            masterOption = null;
        }
        if (masterOption != null) {
            try {
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(masterOption, Text.translatable("vulkanmod-extra.option.particle.allParticles.tooltip"));
            } catch (Exception e) {}
            options.add(masterOption);
            VulkanModExtra.LOGGER.info("Successfully added All Particles master toggle");
        }
        // Core particle types (comprehensive list)
        String[] coreParticles = {
            "rain_splash", "block_break", "block_breaking", "flame", "smoke",
            "bubble", "splash", "rain", "dripping_water", "explosion", "heart",
            "crit", "enchant", "note", "portal", "lava", "firework", "happy_villager",
            "angry_villager", "ash", "campfire_cosy_smoke", "effect", "dust", "poof",
            "large_smoke", "small_flame", "small_gust", "sneeze", "snowflake", "sonic_boom",
            "soul", "soul_fire_flame", "spit", "splash", "spore_blossom_air", "squid_ink",
            "sweep_attack", "totem_of_undying", "trial_omen", "trial_spawner_detection",
            "trial_spawner_detection_ominous", "underwater", "vault_connection", "vibration",
            "warped_spore", "wax_off", "wax_on", "white_ash", "white_smoke", "witch",
            "ambient_entity_effect", "barrier", "block", "block_crumble", "block_marker",
            "bubble_column_up", "bubble_pop", "campfire_signal_smoke", "cherry_leaves",
            "cloud", "composter", "crimson_spore", "current_down", "damage_indicator",
            "dolphin", "dragon_breath", "dripping_dripstone_lava", "dripping_dripstone_water",
            "dripping_honey", "dripping_lava", "dripping_obsidian_tear", "dust_color_transition",
            "dust_pillar", "dust_plume", "egg_crack", "elder_guardian", "electric_spark",
            "enchanted_hit", "end_rod", "entity_effect", "explosion_emitter", "falling_dripstone_lava",
            "falling_dripstone_water", "falling_dust", "falling_honey", "falling_lava",
            "falling_nectar", "falling_obsidian_tear", "falling_spore_blossom", "falling_water",
            "fishing", "flash", "glow", "glow_squid_ink", "gust", "gust_emitter_large",
            "gust_emitter_small", "happy_villager", "infested", "instant_effect", "item",
            "item_cobweb", "item_slime", "item_snowball", "landing_honey", "landing_lava",
            "landing_obsidian_tear", "mycelium", "nautilus", "raid_omen", "reverse_portal",
            "scrape", "sculk_charge", "sculk_charge_pop", "sculk_soul", "shriek", "trail"
        };
        for (String particle : coreParticles) {
            Object option = createOption.apply(particle, value -> {
                try {
                    var field = VulkanModExtra.CONFIG.particleSettings.getClass().getDeclaredField(particle);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.particleSettings, value);
                    VulkanModExtra.CONFIG.writeChanges();
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.debug("Particle field not found: " + particle);
                }
                return null;
            });
            if (option != null) {
                try {
                    // Set tooltip using reflection
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                    setTooltipMethod.invoke(option, Text.translatable("vulkanmod-extra.option.particle." + particle + ".tooltip"));
                } catch (Exception e) {
                    // Tooltip setting failed, continue without tooltip
                }
                options.add(option);
            }
        }
        return options;
    }
    private static List<Object> createDetailOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();
        // Helper for consistent option creation
        // Create a function that takes translation key and field name separately
        interface TriFunction<T, U, V, R> {
            R apply(T t, U u, V v);
        }
        
        TriFunction<String, String, java.util.function.Function<Boolean, Void>, Object> createOption = (translationKey, fieldName, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.details." + translationKey),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.detailSettings.getClass().getDeclaredField(fieldName);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.detailSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };
        // Detail options mapping: translation key -> actual field name
        // Note: sun, moon, stars controls are disabled until proper injection points are found
        String[][] detailMapping = {
            {"sky", "sky"},
            {"rain_snow", "rainSnow"},
            {"biome_colors", "biomeColors"},
            {"sky_colors", "skyColors"}
        };
        
        for (String[] mapping : detailMapping) {
            String translationKey = mapping[0];
            String fieldName = mapping[1];
            Object option = createOption.apply(translationKey, fieldName, value -> {
                try {
                    var field = VulkanModExtra.CONFIG.detailSettings.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.detailSettings, value);
                    VulkanModExtra.CONFIG.writeChanges();
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.error("Failed to set detail option: " + fieldName, e);
                }
                return null;
            });
            if (option != null) {
                try {
                    // Set tooltip using reflection
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                    setTooltipMethod.invoke(option, Text.translatable("vulkanmod-extra.option.details." + translationKey + ".tooltip"));
                } catch (Exception e) {
                    // Tooltip setting failed, continue without tooltip
                }
                options.add(option);
            }
        }
        return options;
    }
      private static List<Object> createRenderOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();
        // Helper for consistent option creation
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.render." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                // Convert snake_case to camelCase for field lookup
                                String fieldName = convertSnakeToCamelCase(key);
                                var configManager = com.criticalrange.config.ConfigurationManager.getInstance();
                                var config = configManager.getConfig();
                                var field = config.renderSettings.getClass().getDeclaredField(fieldName);
                                field.setAccessible(true);
                                return field.getBoolean(config.renderSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };
        
        // Basic render options - map snake_case to camelCase field names
        String[] renderTypes = {"light_updates", "item_frame", "armor_stand", "painting", "piston",
                               "beacon_beam", "limit_beacon_beam_height", "item_frame_name_tag", "player_name_tag"};
        for (String type : renderTypes) {
            Object option = createOption.apply(type, value -> {
                try {
                    // Convert snake_case to camelCase for field lookup
                    String fieldName = convertSnakeToCamelCase(type);
                    
                    // Get the current config from ConfigurationManager and modify it
                    var configManager = com.criticalrange.config.ConfigurationManager.getInstance();
                    var config = configManager.getConfig();
                    var field = config.renderSettings.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    field.setBoolean(config.renderSettings, value);
                    
                    // Save through ConfigurationManager
                    configManager.saveConfig();
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.error("Failed to set render option: " + type, e);
                }
                return null;
            });
            if (option != null) {
                try {
                    // Set tooltip using reflection
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                    setTooltipMethod.invoke(option, Text.translatable("vulkanmod-extra.option.render." + type + ".tooltip"));
                } catch (Exception e) {
                    // Tooltip setting failed, continue without tooltip
                }
                options.add(option);
            }
        }
        
        // Add prevent shaders from extra settings
        Object preventShadersOption = createOption.apply("prevent_shaders", value -> {
            VulkanModExtra.CONFIG.extraSettings.preventShaders = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (preventShadersOption != null) {
            try {
                // Set tooltip using reflection
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(preventShadersOption, Text.translatable("vulkanmod-extra.option.render.prevent_shaders.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(preventShadersOption);
        }
        
        return options;
    }
    
    private static List<Object> createFogRenderOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();
        // Helper for consistent option creation
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.render." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                // Convert snake_case to camelCase for field lookup
                                String fieldName = convertSnakeToCamelCase(key);
                                var configManager = com.criticalrange.config.ConfigurationManager.getInstance();
                                var config = configManager.getConfig();
                                var field = config.renderSettings.getClass().getDeclaredField(fieldName);
                                field.setAccessible(true);
                                return field.getBoolean(config.renderSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };
        
        // Add fog options
        Object fogOption = createOption.apply("global_fog", value -> {
            VulkanModExtra.CONFIG.renderSettings.globalFog = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (fogOption != null) {
            try {
                // Set tooltip using reflection
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(fogOption, Text.translatable("vulkanmod-extra.option.render.global_fog.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(fogOption);
        }
        
        Object multiDimFogOption = createOption.apply("multi_dimension_fog", value -> {
            VulkanModExtra.CONFIG.renderSettings.multiDimensionFog = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (multiDimFogOption != null) {
            try {
                // Set tooltip using reflection
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(multiDimFogOption, Text.translatable("vulkanmod-extra.option.render.multi_dimension_fog.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(multiDimFogOption);
        }
        
        return options;
    }
    
    private static List<Object> createFogTypeRenderOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();
        // Create range option function for sliders
        interface RangeOptionCreator2 {
            Object apply(String key, int min, int max, int step, Text name, Consumer<Integer> setter, Supplier<Integer> getter);
        }
        RangeOptionCreator2 createRangeOption2 = (key, min, max, step, name, setter, getter) -> {
            try {
                Class<?> rangeOptionClass = Class.forName("net.vulkanmod.config.option.RangeOption");
                Function<Integer, Text> translator = value -> Text.literal(value + "%");
                return rangeOptionClass.getConstructor(Text.class, int.class, int.class, int.class, 
                        Function.class, Consumer.class, Supplier.class)
                        .newInstance(name, min, max, step, translator, setter, getter);
            } catch (Exception e) { 
                VulkanModExtra.LOGGER.error("Failed to create range option: " + key, e);
                return null; 
            }
        };
        
        // Add fog configuration options with proper sliders (comprehensive)
        try {
            // Use our custom FogType enum values
            com.criticalrange.config.FogType[] fogTypes = com.criticalrange.config.FogType.values();
            for (com.criticalrange.config.FogType fogType : fogTypes) {
                String fogTypeName = fogType.toString().toLowerCase();
                if (!fogTypeName.equals("none")) {
                    // Create cloud end multiplier slider for each fog type, but exclude water, lava, and powder snow
                    if (fogType != com.criticalrange.config.FogType.WATER && 
                        fogType != com.criticalrange.config.FogType.LAVA && 
                        fogType != com.criticalrange.config.FogType.POWDER_SNOW) {
                    Object cloudEndOption = createRangeOption2.apply(
                        "fog_type.cloud_end", 0, 300, 1, 
                        Text.translatable("vulkanmod-extra.option.fog_type.cloud_end", fogTypeName),
                        value -> {
                            try {
                                var config = VulkanModExtra.CONFIG.renderSettings.fogTypeConfig.computeIfAbsent(
                                    fogType, 
                                    k -> new com.criticalrange.config.VulkanModExtraConfig.FogTypeConfig()
                                );
                                config.cloudEndMultiplier = value;
                                VulkanModExtra.CONFIG.writeChanges();
                            } catch (Exception e) {
                                VulkanModExtra.LOGGER.error("Failed to set fog cloud end multiplier", e);
                            }
                        },
                        () -> {
                            try {
                                var config = VulkanModExtra.CONFIG.renderSettings.fogTypeConfig.computeIfAbsent(
                                    fogType, 
                                    k -> new com.criticalrange.config.VulkanModExtraConfig.FogTypeConfig()
                                );
                                return config.cloudEndMultiplier;
                            } catch (Exception e) {
                                VulkanModExtra.LOGGER.error("Failed to get fog cloud end multiplier", e);
                                return 100; // default value
                            }
                        }
                    );
                    if (cloudEndOption != null) {
                        try {
                            // Set tooltip using reflection
                            java.lang.reflect.Method setTooltipMethod = cloudEndOption.getClass().getMethod("setTooltip", Text.class);
                            setTooltipMethod.invoke(cloudEndOption, Text.translatable("vulkanmod-extra.option.fog_type.cloud_end.tooltip", fogTypeName));
                        } catch (Exception e) {
                            // Tooltip setting failed, continue without tooltip
                        }
                        options.add(cloudEndOption);
                    }
                    }
                }
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Could not create fog type configurations", e);
        }
        
        return options;
    }
    
    private static Object[] createRenderOptionBlocks(Class<?> switchOptionClass, Class<?> optionBlockClass, Class<?> optionArrayClass, Class<?> optionClass) throws Exception {
        // Create separate option blocks for better organization with visual spacing
        List<Object> blocks = new ArrayList<>();
        
        // Basic Render Options Block
        List<Object> renderOptions = createRenderOptions(switchOptionClass);
        if (!renderOptions.isEmpty()) {
            Object[] renderArray = renderOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, renderOptions.size()));
            Object renderBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Rendering", renderArray);
            blocks.add(renderBlock);
        }
        
        // Fog Options Block (creates visual space)
        List<Object> fogOptions = createFogRenderOptions(switchOptionClass);
        if (!fogOptions.isEmpty()) {
            Object[] fogArray = fogOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, fogOptions.size()));
            Object fogBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Fog Settings", fogArray);
            blocks.add(fogBlock);
        }
        
        // Fog Type Configuration Block (creates additional visual space)
        List<Object> fogTypeOptions = createFogTypeRenderOptions(switchOptionClass);
        if (!fogTypeOptions.isEmpty()) {
            Object[] fogTypeArray = fogTypeOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, fogTypeOptions.size()));
            Object fogTypeBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Fog Distance Multipliers", fogTypeArray);
            blocks.add(fogTypeBlock);
        }
        
        return blocks.toArray((Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, blocks.size()));
    }
    private static List<Object> createHUDOptions(Class<?> switchOptionClass, Class<?> cyclingOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();
        // FPS display
        Text fpsText = Text.translatable("vulkanmod-extra.option.extra.show_fps");
        Object fpsOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(fpsText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.showFps = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.showFps);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(fpsOption, Text.translatable("vulkanmod-extra.option.extra.show_fps.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(fpsOption);
        // FPS Display Mode using CyclingOption pattern
        try {
            Text fpsModeText = Text.translatable("vulkanmod-extra.option.extra.fps_display_mode");
            // Create CyclingOption with FPSDisplayMode enum values
            var fpsDisplayModeValues = com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.values();
            Object fpsModeOption = cyclingOptionClass
                    .getConstructor(Text.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(fpsModeText,
                        fpsDisplayModeValues, // All enum values as options
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode>) value -> {
                            VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode>) () ->
                            VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode);
            // Set translator for display names
            java.lang.reflect.Method setTranslatorMethod = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod.invoke(fpsModeOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode, Text>) value ->
                    Text.translatable(com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.getComponentName(value)));
            // Set tooltip using reflection
            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(fpsModeOption, Text.translatable("vulkanmod-extra.option.extra.fps_display_mode.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(fpsModeOption);
        } catch (Exception e) {
            // Fallback to switch option if CyclingOption is not available
            Text fpsModeText = Text.translatable("vulkanmod-extra.option.extra.fps_display_mode");
            Object fpsModeOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(fpsModeText,
                        (java.util.function.Consumer<Boolean>) value -> {
                            // Cycle through FPS modes: BASIC -> EXTENDED -> DETAILED -> BASIC
                            var currentMode = VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode;
                            var nextMode = switch (currentMode) {
                                case BASIC -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.EXTENDED;
                                case EXTENDED -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.DETAILED;
                                case DETAILED -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.BASIC;
                            };
                            VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode = nextMode;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode != com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.BASIC);
            options.add(fpsModeOption);
        }
        // Overlay Corner using CyclingOption
        try {
            Text overlayCornerText = Text.translatable("vulkanmod-extra.option.extra.overlay_corner");
            var overlayCornerValues = com.criticalrange.config.VulkanModExtraConfig.OverlayCorner.values();
            Object overlayCornerOption = cyclingOptionClass
                    .getConstructor(Text.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(overlayCornerText,
                        overlayCornerValues,
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner>) value -> {
                            VulkanModExtra.CONFIG.extraSettings.overlayCorner = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner>) () ->
                            VulkanModExtra.CONFIG.extraSettings.overlayCorner);
                        // Set translator for display names
            java.lang.reflect.Method setTranslatorMethod = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod.invoke(overlayCornerOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner, Text>) value ->
                    Text.translatable("vulkanmod-extra.option.extra.overlay_corner." + value.toString().toLowerCase()));
            // Set tooltip using reflection
            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(overlayCornerOption, Text.translatable("vulkanmod-extra.option.extra.overlay_corner.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(overlayCornerOption);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to create Overlay Corner cycling option", e);
        }
        // Text Contrast using CyclingOption
        try {
            Text textContrastText = Text.translatable("vulkanmod-extra.option.extra.text_contrast");
            var textContrastValues = com.criticalrange.config.VulkanModExtraConfig.TextContrast.values();
            Object textContrastOption = cyclingOptionClass
                    .getConstructor(Text.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(textContrastText,
                        textContrastValues,
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.TextContrast>) value -> {
                            VulkanModExtra.CONFIG.extraSettings.textContrast = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.TextContrast>) () ->
                            VulkanModExtra.CONFIG.extraSettings.textContrast);
                        // Set translator for display names
            java.lang.reflect.Method setTranslatorMethod2 = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod2.invoke(textContrastOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.TextContrast, Text>) value ->
                    Text.translatable("vulkanmod-extra.option.extra.text_contrast." + value.toString().toLowerCase()));
            // Set tooltip using reflection
            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(textContrastOption, Text.translatable("vulkanmod-extra.option.extra.text_contrast.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(textContrastOption);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to create Text Contrast cycling option", e);
        }
        // Coordinates display
        Text coordsText = Text.translatable("vulkanmod-extra.option.extra.show_coords");
        Object coordsOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(coordsText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.showCoords = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.showCoords);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(coordsOption, Text.translatable("vulkanmod-extra.option.extra.show_coords.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(coordsOption);
        // Toasts
        Text toastsText = Text.translatable("vulkanmod-extra.option.extra.toasts");
        Object toastsOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(toastsText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.toasts = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.toasts);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(toastsOption, Text.translatable("vulkanmod-extra.option.extra.toasts.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(toastsOption);
        // Advancement toasts
        Text advancementToastText = Text.translatable("vulkanmod-extra.option.extra.advancement_toast");
        Object advancementToastOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(advancementToastText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.advancementToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.advancementToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(advancementToastOption, Text.translatable("vulkanmod-extra.option.extra.advancement_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(advancementToastOption);
        // Recipe toasts
        Text recipeToastText = Text.translatable("vulkanmod-extra.option.extra.recipe_toast");
        Object recipeToastOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(recipeToastText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.recipeToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.recipeToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(recipeToastOption, Text.translatable("vulkanmod-extra.option.extra.recipe_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(recipeToastOption);
        // System toasts
        Text systemToastText = Text.translatable("vulkanmod-extra.option.extra.system_toast");
        Object systemToastOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(systemToastText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.systemToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.systemToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(systemToastOption, Text.translatable("vulkanmod-extra.option.extra.system_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(systemToastOption);
        // Tutorial toasts
        Text tutorialToastText = Text.translatable("vulkanmod-extra.option.extra.tutorial_toast");
        Object tutorialToastOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(tutorialToastText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.tutorialToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.tutorialToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(tutorialToastOption, Text.translatable("vulkanmod-extra.option.extra.tutorial_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(tutorialToastOption);
        // Instant sneak
        Text instantSneakText = Text.translatable("vulkanmod-extra.option.extra.instant_sneak");
        Object instantSneakOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(instantSneakText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.instantSneak = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.instantSneak);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(instantSneakOption, Text.translatable("vulkanmod-extra.option.extra.instant_sneak.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(instantSneakOption);
        // Steady debug Extra
        Text steadyDebugHudText = Text.translatable("vulkanmod-extra.option.extra.steady_debug_hud");
        Object steadyDebugHudOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(steadyDebugHudText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.steadyDebugHud = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.steadyDebugHud);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(steadyDebugHudOption, Text.translatable("vulkanmod-extra.option.extra.steady_debug_hud.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(steadyDebugHudOption);
        
        return options;
    }

    /**
     * Create animation option blocks with proper spacing using VulkanMod's OptionBlock pattern
     */
    private static Object[] createAnimationOptionBlocks(Class<?> switchOptionClass, Class<?> optionBlockClass, Class<?> optionArrayClass) throws Exception {
        // Create master toggle option
        Object masterOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
            .newInstance(Text.translatable("vulkanmod-extra.option.animation.allAnimations"),
                (java.util.function.Consumer<Boolean>) value -> {
                    try {
                        VulkanModExtra.CONFIG.animationSettings.allAnimations = value;
                        VulkanModExtra.CONFIG.writeChanges();
                        scheduleResourceReload();
                        VulkanModExtra.LOGGER.info("Set allAnimations to: " + value);
                    } catch (Exception e) {
                        VulkanModExtra.LOGGER.error("Failed to set all animations option", e);
                    }
                },
                (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.animationSettings.allAnimations);

        // Set tooltip for master toggle
        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(masterOption, Text.translatable("vulkanmod-extra.option.animation.allAnimations.tooltip"));
        } catch (Exception e) {}

        // Create master toggle block
        Object[] masterArray = (Object[]) java.lang.reflect.Array.newInstance(switchOptionClass, 1);
        masterArray[0] = masterOption;
        Object masterBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("", masterArray);

        // Create individual animation options (without the master toggle and spacer)
        List<Object> individualOptions = new ArrayList<>();
        createIndividualAnimationOptions(switchOptionClass, individualOptions);

        // Create individual options block
        Object[] individualArray = individualOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(switchOptionClass, individualOptions.size()));
        Object individualBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("", individualArray);

        // Return both blocks
        Object[] blocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 2);
        blocks[0] = masterBlock;
        blocks[1] = individualBlock;
        return blocks;
    }

    /**
     * Create particle option blocks with proper spacing using VulkanMod's OptionBlock pattern
     */
    private static Object[] createParticleOptionBlocks(Class<?> switchOptionClass, Class<?> optionBlockClass, Class<?> optionArrayClass) throws Exception {
        // Create master toggle option
        Object masterOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
            .newInstance(Text.translatable("vulkanmod-extra.option.particle.allParticles"),
                (java.util.function.Consumer<Boolean>) value -> {
                    try {
                        VulkanModExtra.CONFIG.particleSettings.allParticles = value;
                        VulkanModExtra.CONFIG.writeChanges();
                        scheduleResourceReload();
                        VulkanModExtra.LOGGER.info("Set allParticles to: " + value);
                    } catch (Exception e) {
                        VulkanModExtra.LOGGER.error("Failed to set all particles option", e);
                    }
                },
                (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.particleSettings.allParticles);

        // Set tooltip for master toggle
        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(masterOption, Text.translatable("vulkanmod-extra.option.particle.allParticles.tooltip"));
        } catch (Exception e) {}

        // Create master toggle block
        Object[] masterArray = (Object[]) java.lang.reflect.Array.newInstance(switchOptionClass, 1);
        masterArray[0] = masterOption;
        Object masterBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("", masterArray);

        // Create individual particle options (without the master toggle and spacer)
        List<Object> individualOptions = new ArrayList<>();
        createIndividualParticleOptions(switchOptionClass, individualOptions);

        // Create individual options block
        Object[] individualArray = individualOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(switchOptionClass, individualOptions.size()));
        Object individualBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("", individualArray);

        // Return both blocks
        Object[] blocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 2);
        blocks[0] = masterBlock;
        blocks[1] = individualBlock;
        return blocks;
    }

    /**
     * Create individual animation options without master toggle or spacer
     */
    private static void createIndividualAnimationOptions(Class<?> switchOptionClass, List<Object> options) throws Exception {
        // Helper for consistent option creation
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.animation." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.animationSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.animationSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };

        // All animation categories (same as before but without master toggle)
        String[] fluidAnimations = {"water", "water_still", "water_flow", "lava", "lava_still", "lava_flow"};
        String[] fireAnimations = {"fire", "fire0", "fire1", "soul_fire", "soul_fire0", "soul_fire1", "campfire_fire", "soul_campfire_fire", "lantern", "soul_lantern", "sea_lantern"};
        String[] portalAnimations = {"portal", "nether_portal", "end_portal", "end_gateway"};
        String[] blockAnimations = {"block_animations", "magma", "prismarine", "prismarine_bricks", "dark_prismarine", "conduit", "respawn_anchor", "stonecutter_saw"};
        String[] machineAnimations = {"machine_animations", "blast_furnace_front_on", "smoker_front_on", "furnace_front_on"};
        String[] plantAnimations = {"plant_animations", "kelp", "kelp_plant", "seagrass", "tall_seagrass_bottom", "tall_seagrass_top"};
        String[] stemAnimations = {"stem_animations", "warped_stem", "crimson_stem", "warped_hyphae", "crimson_hyphae"};
        String[] sculkAnimations = {"sculk_animations", "sculk", "sculk_vein", "sculk_sensor", "sculk_sensor_side", "sculk_sensor_top", "sculk_shrieker", "sculk_shrieker_side", "sculk_shrieker_top", "calibrated_sculk_sensor", "calibrated_sculk_sensor_side", "calibrated_sculk_sensor_top"};
        String[] commandBlockAnimations = {"command_block_animations", "command_block_front", "chain_command_block_front", "repeating_command_block_front"};
        String[] additionalAnimations = {"additional_animations", "beacon", "dragon_egg", "brewing_stand_base", "cauldron_water", "enchanting_table_book"};

        String[][] allAnimationCategories = {fluidAnimations, fireAnimations, portalAnimations, blockAnimations, machineAnimations, plantAnimations, stemAnimations, sculkAnimations, commandBlockAnimations, additionalAnimations};

        for (String[] categoryAnimations : allAnimationCategories) {
            for (String type : categoryAnimations) {
                Object option = createOption.apply(type, value -> {
                    try {
                        var field = VulkanModExtra.CONFIG.animationSettings.getClass().getDeclaredField(type);
                        field.setAccessible(true);
                        field.setBoolean(VulkanModExtra.CONFIG.animationSettings, value);
                        VulkanModExtra.CONFIG.writeChanges();
                        scheduleResourceReload();
                    } catch (Exception e) {
                        VulkanModExtra.LOGGER.error("Failed to set animation option: " + type, e);
                    }
                    return null;
                });
                if (option != null) {
                    try {
                        java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                        setTooltipMethod.invoke(option, Text.translatable("vulkanmod-extra.option.animation." + type + ".tooltip"));
                    } catch (Exception e) {}
                    options.add(option);
                }
            }
        }
    }

    /**
     * Create individual particle options without master toggle or spacer
     */
    private static void createIndividualParticleOptions(Class<?> switchOptionClass, List<Object> options) throws Exception {
        // Helper for creating particle options
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.particle." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.particleSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.particleSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };

        // Core particle types (same as before but without master toggle)
        String[] coreParticles = {"rain_splash", "block_break", "block_breaking", "flame", "smoke", "bubble", "splash", "rain", "dripping_water", "explosion", "heart", "crit", "enchant", "note", "portal", "lava", "firework", "happy_villager", "angry_villager", "ash", "campfire_cosy_smoke", "effect", "dust", "poof", "large_smoke", "small_flame", "small_gust", "sneeze", "snowflake", "sonic_boom", "soul", "soul_fire_flame", "spit", "splash", "spore_blossom_air", "squid_ink", "underwater", "witch", "dripping_lava", "falling_dripstone_water", "falling_dust", "falling_honey", "falling_lava", "falling_nectar", "falling_obsidian_tear", "falling_spore_blossom", "falling_water", "fishing", "flash", "glow", "glow_squid_ink", "gust", "gust_emitter_large", "gust_emitter_small", "happy_villager", "infested", "instant_effect", "item", "item_cobweb", "item_slime", "item_snowball", "landing_honey", "landing_lava", "landing_obsidian_tear", "mycelium", "nautilus", "raid_omen", "reverse_portal", "scrape", "sculk_charge", "sculk_charge_pop", "sculk_soul", "shriek", "trail"};

        for (String particle : coreParticles) {
            Object option = createOption.apply(particle, value -> {
                try {
                    var field = VulkanModExtra.CONFIG.particleSettings.getClass().getDeclaredField(particle);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.particleSettings, value);
                    VulkanModExtra.CONFIG.writeChanges();
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.error("Failed to set particle option: " + particle, e);
                }
                return null;
            });
            if (option != null) {
                try {
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
                    setTooltipMethod.invoke(option, Text.translatable("vulkanmod-extra.option.particle." + particle + ".tooltip"));
                } catch (Exception e) {}
                options.add(option);
            }
        }
    }

    /**
     * Create Extra option blocks with proper spacing using VulkanMod's OptionBlock pattern
     * Organizes options into logical groups as requested
     */
    private static Object[] createExtraOptionBlocks(Class<?> switchOptionClass, Class<?> cyclingOptionClass, Class<?> optionBlockClass, Class<?> optionArrayClass, Class<?> baseOptionClass) throws Exception {
        List<Object> blockList = new ArrayList<>();

        // Block 1: Display Options (FPS, FPS Mode, Overlay Corner, Text Contrast)
        List<Object> displayOptions = new ArrayList<>();
        createDisplayOptions(switchOptionClass, cyclingOptionClass, displayOptions);
        if (!displayOptions.isEmpty()) {
            Object[] displayArray = displayOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(baseOptionClass, displayOptions.size()));
            Object displayBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("", displayArray);
            blockList.add(displayBlock);
        }

        // Block 2: Coordinate Options (Show Coords)
        List<Object> coordinateOptions = new ArrayList<>();
        createCoordinateOptions(switchOptionClass, coordinateOptions);
        if (!coordinateOptions.isEmpty()) {
            Object[] coordinateArray = coordinateOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(baseOptionClass, coordinateOptions.size()));
            Object coordinateBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("", coordinateArray);
            blockList.add(coordinateBlock);
        }

        // Block 3: Optimization Options (Fast Random, Linear Flat Color Blender)
        List<Object> optimizationOptions = new ArrayList<>();
        createOptimizationOptionsForExtra(switchOptionClass, optimizationOptions);
        if (!optimizationOptions.isEmpty()) {
            Object[] optimizationArray = optimizationOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(baseOptionClass, optimizationOptions.size()));
            Object optimizationBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("", optimizationArray);
            blockList.add(optimizationBlock);
        }

        // Block 4: Toast Options (Toasts, Advancement, Recipe, System, Tutorial)
        List<Object> toastOptions = new ArrayList<>();
        createToastOptions(switchOptionClass, toastOptions);
        if (!toastOptions.isEmpty()) {
            Object[] toastArray = toastOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(baseOptionClass, toastOptions.size()));
            Object toastBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("", toastArray);
            blockList.add(toastBlock);
        }

        // Block 4: Other Options (Instant Sneak, Steady Debug Extra)
        List<Object> otherOptions = new ArrayList<>();
        createOtherExtraOptions(switchOptionClass, otherOptions);
        if (!otherOptions.isEmpty()) {
            Object[] otherArray = otherOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(baseOptionClass, otherOptions.size()));
            Object otherBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("", otherArray);
            blockList.add(otherBlock);
        }

        return blockList.toArray((Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, blockList.size()));
    }

    /**
     * Create display-related options (FPS, FPS Mode, Overlay Corner, Text Contrast)
     */
    private static void createDisplayOptions(Class<?> switchOptionClass, Class<?> cyclingOptionClass, List<Object> options) throws Exception {
        // FPS display
        Text fpsText = Text.translatable("vulkanmod-extra.option.extra.show_fps");
        Object fpsOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(fpsText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.showFps = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.showFps);

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(fpsOption, Text.translatable("vulkanmod-extra.option.extra.show_fps.tooltip"));
        } catch (Exception e) {}
        options.add(fpsOption);

        // FPS Display Mode using CyclingOption pattern
        try {
            Text fpsModeText = Text.translatable("vulkanmod-extra.option.extra.fps_display_mode");
            var fpsDisplayModeValues = com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.values();
            Object fpsModeOption = cyclingOptionClass
                    .getConstructor(Text.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(fpsModeText,
                        fpsDisplayModeValues,
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode>) value -> {
                            VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode>) () ->
                            VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode);

            java.lang.reflect.Method setTranslatorMethod = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod.invoke(fpsModeOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode, Text>) value ->
                    Text.translatable(com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.getComponentName(value)));

            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(fpsModeOption, Text.translatable("vulkanmod-extra.option.extra.fps_display_mode.tooltip"));
            } catch (Exception e) {}
            options.add(fpsModeOption);
        } catch (Exception e) {
            // Fallback to switch option if CyclingOption is not available
            Text fpsModeText = Text.translatable("vulkanmod-extra.option.extra.fps_display_mode");
            Object fpsModeOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(fpsModeText,
                        (java.util.function.Consumer<Boolean>) value -> {
                            var currentMode = VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode;
                            var nextMode = switch (currentMode) {
                                case BASIC -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.EXTENDED;
                                case EXTENDED -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.DETAILED;
                                case DETAILED -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.BASIC;
                            };
                            VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode = nextMode;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.fpsDisplayMode != com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.BASIC);
            options.add(fpsModeOption);
        }

        // Overlay Corner using CyclingOption
        try {
            Text overlayCornerText = Text.translatable("vulkanmod-extra.option.extra.overlay_corner");
            var overlayCornerValues = com.criticalrange.config.VulkanModExtraConfig.OverlayCorner.values();
            Object overlayCornerOption = cyclingOptionClass
                    .getConstructor(Text.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(overlayCornerText,
                        overlayCornerValues,
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner>) value -> {
                            VulkanModExtra.CONFIG.extraSettings.overlayCorner = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner>) () ->
                            VulkanModExtra.CONFIG.extraSettings.overlayCorner);

            java.lang.reflect.Method setTranslatorMethod = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod.invoke(overlayCornerOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner, Text>) value ->
                    Text.translatable("vulkanmod-extra.option.extra.overlay_corner." + value.toString().toLowerCase()));

            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(overlayCornerOption, Text.translatable("vulkanmod-extra.option.extra.overlay_corner.tooltip"));
            } catch (Exception e) {}
            options.add(overlayCornerOption);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to create Overlay Corner cycling option", e);
        }

        // Text Contrast using CyclingOption
        try {
            Text textContrastText = Text.translatable("vulkanmod-extra.option.extra.text_contrast");
            var textContrastValues = com.criticalrange.config.VulkanModExtraConfig.TextContrast.values();
            Object textContrastOption = cyclingOptionClass
                    .getConstructor(Text.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(textContrastText,
                        textContrastValues,
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.TextContrast>) value -> {
                            VulkanModExtra.CONFIG.extraSettings.textContrast = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.TextContrast>) () ->
                            VulkanModExtra.CONFIG.extraSettings.textContrast);

            java.lang.reflect.Method setTranslatorMethod2 = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod2.invoke(textContrastOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.TextContrast, Text>) value ->
                    Text.translatable("vulkanmod-extra.option.extra.text_contrast." + value.toString().toLowerCase()));

            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Text.class);
                setTooltipMethod.invoke(textContrastOption, Text.translatable("vulkanmod-extra.option.extra.text_contrast.tooltip"));
            } catch (Exception e) {}
            options.add(textContrastOption);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to create Text Contrast cycling option", e);
        }
    }

    /**
     * Create coordinate-related options (Show Coords)
     */
    private static void createCoordinateOptions(Class<?> switchOptionClass, List<Object> options) throws Exception {
        // Coordinates display
        Text coordsText = Text.translatable("vulkanmod-extra.option.extra.show_coords");
        Object coordsOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(coordsText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.showCoords = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.showCoords);

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(coordsOption, Text.translatable("vulkanmod-extra.option.extra.show_coords.tooltip"));
        } catch (Exception e) {}
        options.add(coordsOption);
    }

    /**
     * Create toast-related options (Toasts, Advancement, Recipe, System, Tutorial)
     */
    private static void createToastOptions(Class<?> switchOptionClass, List<Object> options) throws Exception {
        // Toasts
        Text toastsText = Text.translatable("vulkanmod-extra.option.extra.toasts");
        Object toastsOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(toastsText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.toasts = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.toasts);

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(toastsOption, Text.translatable("vulkanmod-extra.option.extra.toasts.tooltip"));
        } catch (Exception e) {}
        options.add(toastsOption);

        // Advancement toasts
        Text advancementToastText = Text.translatable("vulkanmod-extra.option.extra.advancement_toast");
        Object advancementToastOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(advancementToastText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.advancementToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.advancementToast);

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(advancementToastOption, Text.translatable("vulkanmod-extra.option.extra.advancement_toast.tooltip"));
        } catch (Exception e) {}
        options.add(advancementToastOption);

        // Recipe toasts
        Text recipeToastText = Text.translatable("vulkanmod-extra.option.extra.recipe_toast");
        Object recipeToastOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(recipeToastText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.recipeToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.recipeToast);

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(recipeToastOption, Text.translatable("vulkanmod-extra.option.extra.recipe_toast.tooltip"));
        } catch (Exception e) {}
        options.add(recipeToastOption);

        // System toasts
        Text systemToastText = Text.translatable("vulkanmod-extra.option.extra.system_toast");
        Object systemToastOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(systemToastText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.systemToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.systemToast);

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(systemToastOption, Text.translatable("vulkanmod-extra.option.extra.system_toast.tooltip"));
        } catch (Exception e) {}
        options.add(systemToastOption);

        // Tutorial toasts
        Text tutorialToastText = Text.translatable("vulkanmod-extra.option.extra.tutorial_toast");
        Object tutorialToastOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(tutorialToastText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.tutorialToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.tutorialToast);

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(tutorialToastOption, Text.translatable("vulkanmod-extra.option.extra.tutorial_toast.tooltip"));
        } catch (Exception e) {}
        options.add(tutorialToastOption);
    }

    /**
     * Create optimization options for the Extra page
     * These options were originally intended for VulkanMod's Optimizations page
     * but are now placed in the Extra page for better visibility
     */
    private static void createOptimizationOptionsForExtra(Class<?> switchOptionClass, List<Object> options) throws Exception {
        // Create option factory function
        java.util.function.BiFunction<String, java.util.function.Consumer<Boolean>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Text.translatable("vulkanmod-extra.option.extra." + key),
                        setter,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.extraSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.extraSettings);
                            } catch (Exception e) { return false; }
                        });
            } catch (Exception e) { return null; }
        };
    }

    /**
     * Create other Extra-related options (Instant Sneak, Steady Debug Extra)
     */
    private static void createOtherExtraOptions(Class<?> switchOptionClass, List<Object> options) throws Exception {
        // Instant sneak
        Text instantSneakText = Text.translatable("vulkanmod-extra.option.extra.instant_sneak");
        Object instantSneakOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(instantSneakText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.instantSneak = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.instantSneak);

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(instantSneakOption, Text.translatable("vulkanmod-extra.option.extra.instant_sneak.tooltip"));
        } catch (Exception e) {}
        options.add(instantSneakOption);

        // Steady debug Extra
        Text steadyDebugHudText = Text.translatable("vulkanmod-extra.option.extra.steady_debug_hud");
        Object steadyDebugHudOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(steadyDebugHudText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.steadyDebugHud = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.steadyDebugHud);

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(steadyDebugHudOption, Text.translatable("vulkanmod-extra.option.extra.steady_debug_hud.tooltip"));
        } catch (Exception e) {}
        options.add(steadyDebugHudOption);

        // Advanced Item Tooltips - binds to Minecraft's vanilla F3+H functionality
        Text advancedItemTooltipsText = Text.translatable("vulkanmod-extra.option.extra.advanced_item_tooltips");
        Object advancedItemTooltipsOption = switchOptionClass.getConstructor(Text.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(advancedItemTooltipsText,
                    (java.util.function.Consumer<Boolean>) value -> {
                        try {
                            // Get Minecraft client instance and options
                            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
                            if (minecraft != null && minecraft.options != null) {
                                // Set the vanilla advancedItemTooltips option (same as F3+H)
                                minecraft.options.advancedItemTooltips = value;
                                // Also update our config for persistence
                                VulkanModExtra.CONFIG.extraSettings.advancedItemTooltips = value;
                                VulkanModExtra.CONFIG.writeChanges();
                                VulkanModExtra.LOGGER.info("Set advanced item tooltips to: " + value);
                            }
                        } catch (Exception e) {
                            VulkanModExtra.LOGGER.error("Failed to set advanced item tooltips", e);
                        }
                    },
                    (java.util.function.Supplier<Boolean>) () -> {
                        try {
                            // Get the value from Minecraft's vanilla options if available
                            net.minecraft.client.MinecraftClient minecraft = net.minecraft.client.MinecraftClient.getInstance();
                            if (minecraft != null && minecraft.options != null) {
                                return minecraft.options.advancedItemTooltips;
                            }
                        } catch (Exception e) {
                            VulkanModExtra.LOGGER.debug("Could not get vanilla advanced item tooltips option", e);
                        }
                        // Fall back to our config value
                        return VulkanModExtra.CONFIG.extraSettings.advancedItemTooltips;
                    });

        try {
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Text.class);
            setTooltipMethod.invoke(advancedItemTooltipsOption, Text.translatable("vulkanmod-extra.option.extra.advanced_item_tooltips.tooltip"));
        } catch (Exception e) {}
        options.add(advancedItemTooltipsOption);
    }

    
    /**
     * Convert snake_case to camelCase for field name lookup
     */
    private static String convertSnakeToCamelCase(String snakeCase) {
        if (!snakeCase.contains("_")) {
            return snakeCase;
        }
        
        String[] parts = snakeCase.split("_");
        StringBuilder camelCase = new StringBuilder(parts[0]);
        
        for (int i = 1; i < parts.length; i++) {
            if (parts[i].length() > 0) {
                camelCase.append(Character.toUpperCase(parts[i].charAt(0)));
                if (parts[i].length() > 1) {
                    camelCase.append(parts[i].substring(1));
                }
            }
        }
        
        return camelCase.toString();
    }
}
