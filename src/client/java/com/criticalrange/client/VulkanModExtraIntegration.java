package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.network.chat.Component;

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
            net.minecraft.client.Minecraft minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft != null) {
                minecraft.execute(() -> {
                    VulkanModExtra.LOGGER.info("Reloading resources due to animation setting changes");
                    minecraft.reloadResourcePacks();
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
            VulkanModExtra.LOGGER.info("VulkanMod found, GUI mixin will handle integration");

            integrationSuccessful = true;
            VulkanModExtra.LOGGER.info("Successfully integrated VulkanMod Extra with VulkanMod GUI!");

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

        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Could not inject pages into current screen: {}", e.getMessage());
            e.printStackTrace();
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

            // Create Animation page with comprehensive options
            VulkanModExtra.LOGGER.debug("Creating animation page...");
            List<Object> animationOptions = createComprehensiveAnimationOptions(cachedSwitchOptionClass);
            Object[] animationArray = animationOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionClass, animationOptions.size()));
            Object animationBlock = cachedOptionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Animation Settings", animationArray);

            // Create OptionBlock array for OptionPage constructor
            Object[] animationBlocks = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 1);
            animationBlocks[0] = animationBlock;

            Object animationPage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.animations").getString(), animationBlocks);
            pages.add(animationPage);

            // Create Particle page with comprehensive options
            VulkanModExtra.LOGGER.debug("Creating particle page...");
            List<Object> particleOptions = createComprehensiveParticleOptions(cachedSwitchOptionClass);
            Object[] particleArray = particleOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionClass, particleOptions.size()));
            Object particleBlock = cachedOptionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Particle Settings", particleArray);
            Object[] particleBlocks = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 1);
            particleBlocks[0] = particleBlock;
            Object particlePage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.particles").getString(), particleBlocks);
            pages.add(particlePage);

            // Create Details page with comprehensive options
            VulkanModExtra.LOGGER.debug("Creating details page...");
            List<Object> detailOptions = createComprehensiveDetailOptions(cachedSwitchOptionClass);
            Object[] detailArray = detailOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionClass, detailOptions.size()));
            Object detailBlock = cachedOptionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Detail Settings", detailArray);
            Object[] detailBlocks = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 1);
            detailBlocks[0] = detailBlock;
            Object detailPage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.details").getString(), detailBlocks);
            pages.add(detailPage);

            // Create Render page with comprehensive options including fog
            VulkanModExtra.LOGGER.debug("Creating render page...");
            List<Object> renderOptions = createComprehensiveRenderOptions(cachedSwitchOptionClass);
            Object[] renderArray = renderOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionClass, renderOptions.size()));
            Object renderBlock = cachedOptionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Render Settings", renderArray);
            Object[] renderBlocks = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 1);
            renderBlocks[0] = renderBlock;
            Object renderPage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.render").getString(), renderBlocks);
            pages.add(renderPage);

            // Create HUD page with comprehensive options
            VulkanModExtra.LOGGER.debug("Creating HUD page...");
            List<Object> hudOptions = createComprehensiveHUDOptions(cachedSwitchOptionClass, cachedCyclingOptionClass);
            Object[] hudArray = hudOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(cachedOptionClass, hudOptions.size()));
            Object hudBlock = cachedOptionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("HUD Settings", hudArray);
            Object[] hudBlocks = (Object[]) java.lang.reflect.Array.newInstance(cachedOptionBlockClass, 1);
            hudBlocks[0] = hudBlock;
            Object hudPage = cachedOptionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.extra").getString(), hudBlocks);
            pages.add(hudPage);

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

            // Create Animation page with comprehensive options
            List<Object> animationOptions = createComprehensiveAnimationOptions(switchOptionClass);
            Object[] animationArray = animationOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, animationOptions.size()));
            Object animationBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Animation Settings", animationArray);
            Object[] animationBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            animationBlocks[0] = animationBlock;
            net.vulkanmod.config.option.OptionPage animationPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.animations").getString(), animationBlocks);
            pages.add(animationPage);

            // Create Particle page with comprehensive options
            List<Object> particleOptions = createComprehensiveParticleOptions(switchOptionClass);
            Object[] particleArray = particleOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, particleOptions.size()));
            Object particleBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Particle Settings", particleArray);
            Object[] particleBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            particleBlocks[0] = particleBlock;
            net.vulkanmod.config.option.OptionPage particlePage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.particles").getString(), particleBlocks);
            pages.add(particlePage);

            // Create Details page with comprehensive options
            List<Object> detailOptions = createComprehensiveDetailOptions(switchOptionClass);
            Object[] detailArray = detailOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, detailOptions.size()));
            Object detailBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Detail Settings", detailArray);
            Object[] detailBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            detailBlocks[0] = detailBlock;
            net.vulkanmod.config.option.OptionPage detailPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.details").getString(), detailBlocks);
            pages.add(detailPage);

            // Create Render page with comprehensive options including fog
            List<Object> renderOptions = createComprehensiveRenderOptions(switchOptionClass);
            Object[] renderArray = renderOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, renderOptions.size()));
            Object renderBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Render Settings", renderArray);
            Object[] renderBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            renderBlocks[0] = renderBlock;
            net.vulkanmod.config.option.OptionPage renderPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.render").getString(), renderBlocks);
            pages.add(renderPage);

            // Create HUD page with comprehensive options
            List<Object> hudOptions = createComprehensiveHUDOptions(switchOptionClass, cyclingOptionClass);
            Object[] hudArray = hudOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, hudOptions.size()));
            Object hudBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("HUD Settings", hudArray);
            Object[] hudBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            hudBlocks[0] = hudBlock;
            net.vulkanmod.config.option.OptionPage hudPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance(Component.translatable("vulkanmod-extra.pages.extra").getString(), hudBlocks);
            pages.add(hudPage);

        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to create VulkanMod Extra pages", e);
        }

        return pages;
    }

    private static List<Object> createAnimationOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Helper for consistent option creation with master toggle awareness
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Component.translatable("vulkanmod-extra.option.animation." + key),
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

        // Individual animation options - each can be controlled independently
        String[] animationTypes = {"water", "lava", "fire", "portal", "block_animations", "sculk_sensor"};
        for (String type : animationTypes) {
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
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                    setTooltipMethod.invoke(option, Component.translatable("vulkanmod-extra.option.animation." + type + ".tooltip"));
                } catch (Exception e) {
                    // Tooltip setting failed, continue without tooltip
                }
                options.add(option);
            }
        }

        return options;
    }

    private static List<Object> createParticleOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Helper for creating particle options with individual control
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Component.translatable("vulkanmod-extra.option.particle." + key),
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

        // Core particle types (most commonly used)
        String[] coreParticles = {
            "rain_splash", "block_break", "block_breaking", "flame", "smoke",
            "bubble", "splash", "rain", "dripping_water", "explosion", "heart",
            "crit", "enchant", "note", "portal", "lava", "firework", "happy_villager",
            "angry_villager", "ash", "campfire_cosy_smoke", "effect", "dust", "poof"
        };

        for (String particle : coreParticles) {
            Object option = createOption.apply(particle, value -> {
                try {
                    var field = VulkanModExtra.CONFIG.particleSettings.getClass().getDeclaredField(particle);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.particleSettings, value);
                    VulkanModExtra.CONFIG.writeChanges();
                    
                    // Schedule a debounced resource reload to apply particle changes
                    scheduleResourceReload();
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.error("Failed to set particle option: " + particle, e);
                }
                return null;
            });
            if (option != null) {
                try {
                    // Set tooltip using reflection
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                    setTooltipMethod.invoke(option, Component.translatable("vulkanmod-extra.option.particle." + particle + ".tooltip"));
                } catch (Exception e) {
                    // Tooltip setting failed, continue without tooltip
                }
                options.add(option);
            }
        }

        // Environmental particles
        String[] environmentalParticles = {
            "cherry_leaves", "crimson_spore", "warped_spore", "white_ash", "spore_blossom_air",
            "mycelium", "cloud", "composter", "dripping_honey", "falling_honey", "landing_honey"
        };

        for (String particle : environmentalParticles) {
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
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                    setTooltipMethod.invoke(option, Component.translatable("vulkanmod-extra.option.particle." + particle + ".tooltip"));
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
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Component.translatable("vulkanmod-extra.option.details." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.environmentSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.environmentSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };

        // Detail options from sodium-extra
        String[] detailTypes = {"sky", "sun", "moon", "stars", "rain_snow", "biome_colors", "sky_colors"};
        for (String type : detailTypes) {
            Object option = createOption.apply(type, value -> {
                try {
                    var field = VulkanModExtra.CONFIG.environmentSettings.getClass().getDeclaredField(type);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.environmentSettings, value);
                    VulkanModExtra.CONFIG.writeChanges();
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.error("Failed to set detail option: " + type, e);
                }
                return null;
            });
            if (option != null) {
                try {
                    // Set tooltip using reflection
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                    setTooltipMethod.invoke(option, Component.translatable("vulkanmod-extra.option.details." + type + ".tooltip"));
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
                return switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Component.translatable("vulkanmod-extra.option.render." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.renderSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.renderSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };

        // Create range option function for sliders
        interface RangeOptionCreator {
            Object apply(String key, int min, int max, int step, Component name, Consumer<Integer> setter, Supplier<Integer> getter);
        }
        
        RangeOptionCreator createRangeOption = (key, min, max, step, name, setter, getter) -> {
            try {
                Class<?> rangeOptionClass = Class.forName("net.vulkanmod.config.option.RangeOption");
                Function<Integer, Component> translator = value -> Component.literal(value + "%");
                return rangeOptionClass.getConstructor(Component.class, int.class, int.class, int.class, 
                        Function.class, Consumer.class, Supplier.class)
                        .newInstance(name, min, max, step, translator, setter, getter);
            } catch (Exception e) { 
                VulkanModExtra.LOGGER.error("Failed to create range option: " + key, e);
                return null; 
            }
        };

        // Render options from sodium-extra
        String[] renderTypes = {"light_updates", "item_frame", "armor_stand", "painting", "piston", 
                               "beacon_beam", "limit_beacon_beam_height", "enchanting_table_book", "item_frame_name_tag", "player_name_tag"};
        for (String type : renderTypes) {
            Object option = createOption.apply(type, value -> {
                try {
                    var field = VulkanModExtra.CONFIG.renderSettings.getClass().getDeclaredField(type);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.renderSettings, value);
                    VulkanModExtra.CONFIG.writeChanges();
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.error("Failed to set render option: " + type, e);
                }
                return null;
            });
            if (option != null) options.add(option);
        }
        
        // Add prevent shaders from extra settings
        Object preventShadersOption = createOption.apply("prevent_shaders", value -> {
            VulkanModExtra.CONFIG.performanceSettings.preventShaders = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (preventShadersOption != null) options.add(preventShadersOption);
        
        // Add performance optimization options
        Object useFastRandomOption = createOption.apply("use_fast_random", value -> {
            VulkanModExtra.CONFIG.performanceSettings.useFastRandom = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (useFastRandomOption != null) options.add(useFastRandomOption);
        
        Object linearFlatColorBlenderOption = createOption.apply("linear_flat_color_blender", value -> {
            VulkanModExtra.CONFIG.performanceSettings.linearFlatColorBlender = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (linearFlatColorBlenderOption != null) options.add(linearFlatColorBlenderOption);
        
        // Add fog configuration options with proper sliders (simplified)
        try {
            // Use our custom FogType enum values
            com.criticalrange.config.FogType[] fogTypes = com.criticalrange.config.FogType.values();
            
            for (com.criticalrange.config.FogType fogType : fogTypes) {
                String fogTypeName = fogType.toString().toLowerCase();
                if (!fogTypeName.equals("none")) {
                    // Create cloud end multiplier slider for each fog type
                    Object cloudEndOption = createRangeOption.apply(
                        "fog_type.cloud_end", 0, 300, 1, 
                        Component.translatable("vulkanmod-extra.option.fog_type.cloud_end", fogTypeName),
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
                            java.lang.reflect.Method setTooltipMethod = cloudEndOption.getClass().getMethod("setTooltip", Component.class);
                            setTooltipMethod.invoke(cloudEndOption, Component.translatable("vulkanmod-extra.option.fog_type.cloud_end.tooltip", fogTypeName));
                        } catch (Exception e) {
                            // Tooltip setting failed, continue without tooltip
                        }
                        options.add(cloudEndOption);
                    }
                }
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Could not create fog type configurations", e);
        }

        return options;
    }

    private static List<Object> createHUDOptions(Class<?> switchOptionClass, Class<?> cyclingOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // FPS display
        Component fpsComponent = Component.translatable("vulkanmod-extra.option.show_fps");
        Object fpsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(fpsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.showFps = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.showFps);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(fpsOption, Component.translatable("vulkanmod-extra.option.extra.show_fps.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(fpsOption);

        // FPS Display Mode using CyclingOption pattern
        try {
            Component fpsModeComponent = Component.translatable("vulkanmod-extra.option.extra.fps_display_mode");
            
            // Create CyclingOption with FPSDisplayMode enum values
            var fpsDisplayModeValues = com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.values();
            Object fpsModeOption = cyclingOptionClass
                    .getConstructor(Component.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(fpsModeComponent,
                        fpsDisplayModeValues, // All enum values as options
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode>) value -> {
                            VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode>) () -> 
                            VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode);
            
            // Set translator for display names
            java.lang.reflect.Method setTranslatorMethod = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod.invoke(fpsModeOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode, Component>) value ->
                    Component.translatable(com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.getComponentName(value)));

            // Set tooltip using reflection
            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(fpsModeOption, Component.translatable("vulkanmod-extra.option.extra.fps_display_mode.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }

            options.add(fpsModeOption);
        } catch (Exception e) {
            // Fallback to switch option if CyclingOption is not available
            Component fpsModeComponent = Component.translatable("vulkanmod-extra.option.extra.fps_display_mode");
            Object fpsModeOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(fpsModeComponent,
                        (java.util.function.Consumer<Boolean>) value -> {
                            // Cycle through FPS modes: BASIC -> EXTENDED -> DETAILED -> BASIC
                            var currentMode = VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode;
                            var nextMode = switch (currentMode) {
                                case BASIC -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.EXTENDED;
                                case EXTENDED -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.DETAILED;
                                case DETAILED -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.BASIC;
                            };
                            VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode = nextMode;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode != com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.BASIC);
            options.add(fpsModeOption);
        }

        // Overlay Corner using CyclingOption
        try {
            Component overlayCornerComponent = Component.translatable("vulkanmod-extra.option.extra.overlay_corner");
            var overlayCornerValues = com.criticalrange.config.VulkanModExtraConfig.OverlayCorner.values();
            Object overlayCornerOption = cyclingOptionClass
                    .getConstructor(Component.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(overlayCornerComponent,
                        overlayCornerValues,
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner>) value -> {
                            VulkanModExtra.CONFIG.hudSettings.overlayCorner = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner>) () -> 
                            VulkanModExtra.CONFIG.hudSettings.overlayCorner);
                            
            // Set translator for display names
            java.lang.reflect.Method setTranslatorMethod = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod.invoke(overlayCornerOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner, Component>) value ->
                    Component.translatable("vulkanmod-extra.option.extra.overlay_corner." + value.toString().toLowerCase()));

            // Set tooltip using reflection
            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(overlayCornerOption, Component.translatable("vulkanmod-extra.option.extra.overlay_corner.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }

            options.add(overlayCornerOption);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to create Overlay Corner cycling option", e);
        }

        // Text Contrast using CyclingOption
        try {
            Component textContrastComponent = Component.translatable("vulkanmod-extra.option.extra.text_contrast");
            var textContrastValues = com.criticalrange.config.VulkanModExtraConfig.TextContrast.values();
            Object textContrastOption = cyclingOptionClass
                    .getConstructor(Component.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(textContrastComponent,
                        textContrastValues,
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.TextContrast>) value -> {
                            VulkanModExtra.CONFIG.hudSettings.textContrast = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.TextContrast>) () -> 
                            VulkanModExtra.CONFIG.hudSettings.textContrast);
                            
            // Set translator for display names
            java.lang.reflect.Method setTranslatorMethod2 = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod2.invoke(textContrastOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.TextContrast, Component>) value ->
                    Component.translatable("vulkanmod-extra.option.extra.text_contrast." + value.toString().toLowerCase()));

            // Set tooltip using reflection
            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(textContrastOption, Component.translatable("vulkanmod-extra.option.extra.text_contrast.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }

            options.add(textContrastOption);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to create Text Contrast cycling option", e);
        }

        // Coordinates display
        Component coordsComponent = Component.translatable("vulkanmod-extra.option.extra.show_coords");
        Object coordsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(coordsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.showCoords = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.showCoords);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(coordsOption, Component.translatable("vulkanmod-extra.option.extra.show_coords.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(coordsOption);

        // Toasts
        Component toastsComponent = Component.translatable("vulkanmod-extra.option.extra.toasts");
        Object toastsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(toastsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.toasts = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.toasts);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(toastsOption, Component.translatable("vulkanmod-extra.option.extra.toasts.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(toastsOption);

        // Advancement toasts
        Component advancementToastComponent = Component.translatable("vulkanmod-extra.option.extra.advancement_toast");
        Object advancementToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(advancementToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.advancementToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.advancementToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(advancementToastOption, Component.translatable("vulkanmod-extra.option.extra.advancement_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(advancementToastOption);

        // Recipe toasts
        Component recipeToastComponent = Component.translatable("vulkanmod-extra.option.extra.recipe_toast");
        Object recipeToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(recipeToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.recipeToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.recipeToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(recipeToastOption, Component.translatable("vulkanmod-extra.option.extra.recipe_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(recipeToastOption);

        // System toasts
        Component systemToastComponent = Component.translatable("vulkanmod-extra.option.extra.system_toast");
        Object systemToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(systemToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.systemToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.systemToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(systemToastOption, Component.translatable("vulkanmod-extra.option.extra.system_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(systemToastOption);

        // Tutorial toasts
        Component tutorialToastComponent = Component.translatable("vulkanmod-extra.option.extra.tutorial_toast");
        Object tutorialToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(tutorialToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.tutorialToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.tutorialToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(tutorialToastOption, Component.translatable("vulkanmod-extra.option.extra.tutorial_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(tutorialToastOption);

        // Instant sneak
        Component instantSneakComponent = Component.translatable("vulkanmod-extra.option.extra.instant_sneak");
        Object instantSneakOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(instantSneakComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.performanceSettings.instantSneak = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.performanceSettings.instantSneak);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(instantSneakOption, Component.translatable("vulkanmod-extra.option.extra.instant_sneak.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(instantSneakOption);

        // Adaptive sync
        Component adaptiveSyncComponent = Component.translatable("vulkanmod-extra.option.extra.use_adaptive_sync");
        Object adaptiveSyncOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(adaptiveSyncComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.performanceSettings.useAdaptiveSync = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.performanceSettings.useAdaptiveSync);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(adaptiveSyncOption, Component.translatable("vulkanmod-extra.option.extra.use_adaptive_sync.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(adaptiveSyncOption);

        // Steady debug HUD
        Component steadyDebugHudComponent = Component.translatable("vulkanmod-extra.option.extra.steady_debug_hud");
        Object steadyDebugHudOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(steadyDebugHudComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.performanceSettings.steadyDebugHud = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.performanceSettings.steadyDebugHud);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(steadyDebugHudOption, Component.translatable("vulkanmod-extra.option.extra.steady_debug_hud.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(steadyDebugHudOption);

        return options;
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

    private static List<Object> createComprehensiveAnimationOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Helper for consistent option creation with master toggle awareness
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Component.translatable("vulkanmod-extra.option.animation." + key),
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

        // Comprehensive animation options - organized by category for better UX
        // Each animation controls only its own behavior (no master control)
        
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
            "additional_animations", "beacon", "dragon_egg", "brewing_stand_base", "cauldron_water"
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
                        java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                        setTooltipMethod.invoke(option, Component.translatable("vulkanmod-extra.option.animation." + type + ".tooltip"));
                    } catch (Exception e) {
                        // Tooltip setting failed, continue without tooltip
                    }
                    options.add(option);
                }
            }
        }

        return options;
    }

    private static List<Object> createComprehensiveParticleOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Helper for creating particle options with individual control
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Component.translatable("vulkanmod-extra.option.particle." + key),
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
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                    setTooltipMethod.invoke(option, Component.translatable("vulkanmod-extra.option.particle." + particle + ".tooltip"));
                } catch (Exception e) {
                    // Tooltip setting failed, continue without tooltip
                }
                options.add(option);
            }
        }

        return options;
    }

    private static List<Object> createComprehensiveDetailOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Helper for consistent option creation
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Component.translatable("vulkanmod-extra.option.details." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.environmentSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.environmentSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };

        // Detail options from sodium-extra
        String[] detailTypes = {"sky", "sun", "moon", "stars", "rain_snow", "biome_colors", "sky_colors"};
        for (String type : detailTypes) {
            Object option = createOption.apply(type, value -> {
                try {
                    var field = VulkanModExtra.CONFIG.environmentSettings.getClass().getDeclaredField(type);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.environmentSettings, value);
                    VulkanModExtra.CONFIG.writeChanges();
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.error("Failed to set detail option: " + type, e);
                }
                return null;
            });
            if (option != null) {
                try {
                    // Set tooltip using reflection
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                    setTooltipMethod.invoke(option, Component.translatable("vulkanmod-extra.option.details." + type + ".tooltip"));
                } catch (Exception e) {
                    // Tooltip setting failed, continue without tooltip
                }
                options.add(option);
            }
        }

        return options;
    }

    private static List<Object> createComprehensiveRenderOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Helper for consistent option creation
        java.util.function.BiFunction<String, java.util.function.Function<Boolean, Void>, Object> createOption = (key, setter) -> {
            try {
                return switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(Component.translatable("vulkanmod-extra.option.render." + key),
                        (java.util.function.Consumer<Boolean>) setter::apply,
                        (java.util.function.Supplier<Boolean>) () -> {
                            try {
                                var field = VulkanModExtra.CONFIG.renderSettings.getClass().getDeclaredField(key);
                                field.setAccessible(true);
                                return field.getBoolean(VulkanModExtra.CONFIG.renderSettings);
                            } catch (Exception e) { return true; }
                        });
            } catch (Exception e) { return null; }
        };

        // Create range option function for sliders
        interface RangeOptionCreator2 {
            Object apply(String key, int min, int max, int step, Component name, Consumer<Integer> setter, Supplier<Integer> getter);
        }
        
        RangeOptionCreator2 createRangeOption2 = (key, min, max, step, name, setter, getter) -> {
            try {
                Class<?> rangeOptionClass = Class.forName("net.vulkanmod.config.option.RangeOption");
                Function<Integer, Component> translator = value -> Component.literal(value + "%");
                return rangeOptionClass.getConstructor(Component.class, int.class, int.class, int.class, 
                        Function.class, Consumer.class, Supplier.class)
                        .newInstance(name, min, max, step, translator, setter, getter);
            } catch (Exception e) { 
                VulkanModExtra.LOGGER.error("Failed to create range option: " + key, e);
                return null; 
            }
        };

        // Basic render options
        String[] renderTypes = {"light_updates", "item_frame", "armor_stand", "painting", "piston",
                               "beacon_beam", "limit_beacon_beam_height", "enchanting_table_book", "item_frame_name_tag", "player_name_tag"};
        for (String type : renderTypes) {
            Object option = createOption.apply(type, value -> {
                try {
                    var field = VulkanModExtra.CONFIG.renderSettings.getClass().getDeclaredField(type);
                    field.setAccessible(true);
                    field.setBoolean(VulkanModExtra.CONFIG.renderSettings, value);
                    VulkanModExtra.CONFIG.writeChanges();
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.error("Failed to set render option: " + type, e);
                }
                return null;
            });
            if (option != null) {
                try {
                    // Set tooltip using reflection
                    java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                    setTooltipMethod.invoke(option, Component.translatable("vulkanmod-extra.option.render." + type + ".tooltip"));
                } catch (Exception e) {
                    // Tooltip setting failed, continue without tooltip
                }
                options.add(option);
            }
        }

        // Add fog options
        Object fogOption = createOption.apply("global_fog", value -> {
            VulkanModExtra.CONFIG.renderSettings.globalFog = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (fogOption != null) {
            try {
                // Set tooltip using reflection
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(fogOption, Component.translatable("vulkanmod-extra.option.render.global_fog.tooltip"));
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
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(multiDimFogOption, Component.translatable("vulkanmod-extra.option.render.multi_dimension_fog.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(multiDimFogOption);
        }

        // Add prevent shaders from extra settings
        Object preventShadersOption = createOption.apply("prevent_shaders", value -> {
            VulkanModExtra.CONFIG.performanceSettings.preventShaders = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (preventShadersOption != null) {
            try {
                // Set tooltip using reflection
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(preventShadersOption, Component.translatable("vulkanmod-extra.option.render.prevent_shaders.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(preventShadersOption);
        }
        
        // Add performance optimization options
        Object useFastRandomOption = createOption.apply("use_fast_random", value -> {
            VulkanModExtra.CONFIG.performanceSettings.useFastRandom = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (useFastRandomOption != null) {
            try {
                // Set tooltip using reflection
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(useFastRandomOption, Component.translatable("vulkanmod-extra.option.use_fast_random.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(useFastRandomOption);
        }
        
        Object linearFlatColorBlenderOption = createOption.apply("linear_flat_color_blender", value -> {
            VulkanModExtra.CONFIG.performanceSettings.linearFlatColorBlender = value;
            VulkanModExtra.CONFIG.writeChanges();
            return null;
        });
        if (linearFlatColorBlenderOption != null) {
            try {
                // Set tooltip using reflection
                java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(linearFlatColorBlenderOption, Component.translatable("vulkanmod-extra.option.linear_flat_color_blender.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }
            options.add(linearFlatColorBlenderOption);
        }
        
        // Add fog configuration options with proper sliders (comprehensive)
        try {
            // Use our custom FogType enum values
            com.criticalrange.config.FogType[] fogTypes = com.criticalrange.config.FogType.values();
            
            for (com.criticalrange.config.FogType fogType : fogTypes) {
                String fogTypeName = fogType.toString().toLowerCase();
                if (!fogTypeName.equals("none")) {
                    // Create cloud end multiplier slider for each fog type
                    Object cloudEndOption = createRangeOption2.apply(
                        "fog_type.cloud_end", 0, 300, 1, 
                        Component.translatable("vulkanmod-extra.option.fog_type.cloud_end", fogTypeName),
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
                            java.lang.reflect.Method setTooltipMethod = cloudEndOption.getClass().getMethod("setTooltip", Component.class);
                            setTooltipMethod.invoke(cloudEndOption, Component.translatable("vulkanmod-extra.option.fog_type.cloud_end.tooltip", fogTypeName));
                        } catch (Exception e) {
                            // Tooltip setting failed, continue without tooltip
                        }
                        options.add(cloudEndOption);
                    }
                }
            }
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Could not create fog type configurations", e);
        }

        return options;
    }

    private static List<Object> createComprehensiveHUDOptions(Class<?> switchOptionClass, Class<?> cyclingOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // FPS display
        Component fpsComponent = Component.translatable("vulkanmod-extra.option.extra.show_fps");
        Object fpsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(fpsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.showFps = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.showFps);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(fpsOption, Component.translatable("vulkanmod-extra.option.extra.show_fps.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(fpsOption);

        // FPS Display Mode using CyclingOption pattern
        try {
            Component fpsModeComponent = Component.translatable("vulkanmod-extra.option.extra.fps_display_mode");

            // Create CyclingOption with FPSDisplayMode enum values
            var fpsDisplayModeValues = com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.values();
            Object fpsModeOption = cyclingOptionClass
                    .getConstructor(Component.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(fpsModeComponent,
                        fpsDisplayModeValues, // All enum values as options
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode>) value -> {
                            VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode>) () ->
                            VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode);

            // Set translator for display names
            java.lang.reflect.Method setTranslatorMethod = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod.invoke(fpsModeOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode, Component>) value ->
                    Component.translatable(com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.getComponentName(value)));

            // Set tooltip using reflection
            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(fpsModeOption, Component.translatable("vulkanmod-extra.option.extra.fps_display_mode.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }

            options.add(fpsModeOption);
        } catch (Exception e) {
            // Fallback to switch option if CyclingOption is not available
            Component fpsModeComponent = Component.translatable("vulkanmod-extra.option.extra.fps_display_mode");
            Object fpsModeOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(fpsModeComponent,
                        (java.util.function.Consumer<Boolean>) value -> {
                            // Cycle through FPS modes: BASIC -> EXTENDED -> DETAILED -> BASIC
                            var currentMode = VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode;
                            var nextMode = switch (currentMode) {
                                case BASIC -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.EXTENDED;
                                case EXTENDED -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.DETAILED;
                                case DETAILED -> com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.BASIC;
                            };
                            VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode = nextMode;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.fpsDisplayMode != com.criticalrange.config.VulkanModExtraConfig.FPSDisplayMode.BASIC);
            options.add(fpsModeOption);
        }

        // Overlay Corner using CyclingOption
        try {
            Component overlayCornerComponent = Component.translatable("vulkanmod-extra.option.extra.overlay_corner");
            var overlayCornerValues = com.criticalrange.config.VulkanModExtraConfig.OverlayCorner.values();
            Object overlayCornerOption = cyclingOptionClass
                    .getConstructor(Component.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(overlayCornerComponent,
                        overlayCornerValues,
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner>) value -> {
                            VulkanModExtra.CONFIG.hudSettings.overlayCorner = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner>) () ->
                            VulkanModExtra.CONFIG.hudSettings.overlayCorner);

                        // Set translator for display names
            java.lang.reflect.Method setTranslatorMethod = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod.invoke(overlayCornerOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.OverlayCorner, Component>) value ->
                    Component.translatable("vulkanmod-extra.option.extra.overlay_corner." + value.toString().toLowerCase()));

            // Set tooltip using reflection
            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(overlayCornerOption, Component.translatable("vulkanmod-extra.option.extra.overlay_corner.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }

            options.add(overlayCornerOption);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to create Overlay Corner cycling option", e);
        }

        // Text Contrast using CyclingOption
        try {
            Component textContrastComponent = Component.translatable("vulkanmod-extra.option.extra.text_contrast");
            var textContrastValues = com.criticalrange.config.VulkanModExtraConfig.TextContrast.values();
            Object textContrastOption = cyclingOptionClass
                    .getConstructor(Component.class, Object[].class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                    .newInstance(textContrastComponent,
                        textContrastValues,
                        (java.util.function.Consumer<com.criticalrange.config.VulkanModExtraConfig.TextContrast>) value -> {
                            VulkanModExtra.CONFIG.hudSettings.textContrast = value;
                            VulkanModExtra.CONFIG.writeChanges();
                        },
                        (java.util.function.Supplier<com.criticalrange.config.VulkanModExtraConfig.TextContrast>) () ->
                            VulkanModExtra.CONFIG.hudSettings.textContrast);

                        // Set translator for display names
            java.lang.reflect.Method setTranslatorMethod2 = cyclingOptionClass.getMethod("setTranslator", java.util.function.Function.class);
            setTranslatorMethod2.invoke(textContrastOption,
                (java.util.function.Function<com.criticalrange.config.VulkanModExtraConfig.TextContrast, Component>) value ->
                    Component.translatable("vulkanmod-extra.option.extra.text_contrast." + value.toString().toLowerCase()));

            // Set tooltip using reflection
            try {
                java.lang.reflect.Method setTooltipMethod = cyclingOptionClass.getMethod("setTooltip", Component.class);
                setTooltipMethod.invoke(textContrastOption, Component.translatable("vulkanmod-extra.option.extra.text_contrast.tooltip"));
            } catch (Exception e) {
                // Tooltip setting failed, continue without tooltip
            }

            options.add(textContrastOption);
        } catch (Exception e) {
            VulkanModExtra.LOGGER.warn("Failed to create Text Contrast cycling option", e);
        }

        // Coordinates display
        Component coordsComponent = Component.translatable("vulkanmod-extra.option.extra.show_coords");
        Object coordsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(coordsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.showCoords = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.showCoords);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(coordsOption, Component.translatable("vulkanmod-extra.option.extra.show_coords.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(coordsOption);

        // Toasts
        Component toastsComponent = Component.translatable("vulkanmod-extra.option.extra.toasts");
        Object toastsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(toastsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.toasts = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.toasts);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(toastsOption, Component.translatable("vulkanmod-extra.option.extra.toasts.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(toastsOption);

        // Advancement toasts
        Component advancementToastComponent = Component.translatable("vulkanmod-extra.option.extra.advancement_toast");
        Object advancementToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(advancementToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.advancementToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.advancementToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(advancementToastOption, Component.translatable("vulkanmod-extra.option.extra.advancement_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(advancementToastOption);

        // Recipe toasts
        Component recipeToastComponent = Component.translatable("vulkanmod-extra.option.extra.recipe_toast");
        Object recipeToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(recipeToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.recipeToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.recipeToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(recipeToastOption, Component.translatable("vulkanmod-extra.option.extra.recipe_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(recipeToastOption);

        // System toasts
        Component systemToastComponent = Component.translatable("vulkanmod-extra.option.extra.system_toast");
        Object systemToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(systemToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.systemToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.systemToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(systemToastOption, Component.translatable("vulkanmod-extra.option.extra.system_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(systemToastOption);

        // Tutorial toasts
        Component tutorialToastComponent = Component.translatable("vulkanmod-extra.option.extra.tutorial_toast");
        Object tutorialToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(tutorialToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.hudSettings.tutorialToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.hudSettings.tutorialToast);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(tutorialToastOption, Component.translatable("vulkanmod-extra.option.extra.tutorial_toast.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(tutorialToastOption);

        // Instant sneak
        Component instantSneakComponent = Component.translatable("vulkanmod-extra.option.extra.instant_sneak");
        Object instantSneakOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(instantSneakComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.performanceSettings.instantSneak = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.performanceSettings.instantSneak);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(instantSneakOption, Component.translatable("vulkanmod-extra.option.extra.instant_sneak.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(instantSneakOption);

        // Adaptive sync
        Component adaptiveSyncComponent = Component.translatable("vulkanmod-extra.option.extra.use_adaptive_sync");
        Object adaptiveSyncOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(adaptiveSyncComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.performanceSettings.useAdaptiveSync = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.performanceSettings.useAdaptiveSync);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(adaptiveSyncOption, Component.translatable("vulkanmod-extra.option.extra.use_adaptive_sync.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(adaptiveSyncOption);

        // Steady debug HUD
        Component steadyDebugHudComponent = Component.translatable("vulkanmod-extra.option.extra.steady_debug_hud");
        Object steadyDebugHudOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(steadyDebugHudComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.performanceSettings.steadyDebugHud = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.performanceSettings.steadyDebugHud);
        try {
            // Set tooltip using reflection
            java.lang.reflect.Method setTooltipMethod = switchOptionClass.getMethod("setTooltip", Component.class);
            setTooltipMethod.invoke(steadyDebugHudOption, Component.translatable("vulkanmod-extra.option.extra.steady_debug_hud.tooltip"));
        } catch (Exception e) {
            // Tooltip setting failed, continue without tooltip
        }
        options.add(steadyDebugHudOption);

        return options;
    }
}











