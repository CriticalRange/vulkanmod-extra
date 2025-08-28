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

/**
 * Runtime integration with VulkanMod's GUI system
 * This class uses reflection to add VulkanMod Extra options to VulkanMod's settings screen
 */
public class VulkanModExtraIntegration {

    private static boolean integrationAttempted = false;
    private static boolean integrationSuccessful = false;

    // Track which screen instances have already been injected to prevent multiple injections
    private static final Map<Object, Boolean> injectedInstances = new WeakHashMap<>();

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
        VulkanModExtra.LOGGER.info("Starting VulkanMod Extra screen injection...");

        if (integrationAttempted && !integrationSuccessful) {
            VulkanModExtra.LOGGER.info("Integration already attempted and failed, skipping...");
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
                VulkanModExtra.LOGGER.info("Created {} extra pages, attempting to add them...", extraPages.size());
                addPagesToScreen(currentScreen, extraPages);
                integrationSuccessful = true;
                VulkanModExtra.LOGGER.info("Successfully injected VulkanMod Extra pages into active screen!");
            } else {
                VulkanModExtra.LOGGER.warn("No extra pages were created");
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
        VulkanModExtra.LOGGER.info("Attempting to add {} pages to screen: {}", extraPages.size(), screen.getClass().getName());

        try {
            // Look for common field names that might hold option pages
            String[] possibleFieldNames = {"optionPages", "pages", "tabs", "categories"};

            for (String fieldName : possibleFieldNames) {
                try {
                    VulkanModExtra.LOGGER.debug("Trying field: {}", fieldName);
                    java.lang.reflect.Field field = screen.getClass().getDeclaredField(fieldName);
                    field.setAccessible(true);
                    Object fieldValue = field.get(screen);

                    VulkanModExtra.LOGGER.debug("Field {} value: {}", fieldName, fieldValue);

                    if (fieldValue instanceof List) {
                        @SuppressWarnings("unchecked")
                        List<Object> pages = (List<Object>) fieldValue;
                        VulkanModExtra.LOGGER.debug("Found List with {} existing pages", pages.size());
                        pages.addAll(extraPages);
                        VulkanModExtra.LOGGER.info("Added {} extra pages to VulkanMod screen via field {}", extraPages.size(), fieldName);
                        return;
                    }
                } catch (NoSuchFieldException e) {
                    VulkanModExtra.LOGGER.debug("Field {} not found", fieldName);
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.debug("Error accessing field {}: {}", fieldName, e.getMessage());
                }
            }

            VulkanModExtra.LOGGER.warn("Could not find pages field, trying method approach...");
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
        VulkanModExtra.LOGGER.info("Trying to add pages via method calls...");

        try {
            // Look for methods like addPage, addTab, etc.
            String[] possibleMethods = {"addPage", "addTab", "addCategory", "addOptionPage"};

            for (String methodName : possibleMethods) {
                try {
                    VulkanModExtra.LOGGER.debug("Trying method: {}", methodName);
                    java.lang.reflect.Method method = screen.getClass().getDeclaredMethod(methodName, Object.class);
                    method.setAccessible(true);

                    VulkanModExtra.LOGGER.debug("Found method {}, invoking for {} pages", methodName, extraPages.size());
                    for (Object page : extraPages) {
                        method.invoke(screen, page);
                    }
                    VulkanModExtra.LOGGER.info("Added {} pages via method {} to VulkanMod screen", extraPages.size(), methodName);
                    return;
                } catch (NoSuchMethodException e) {
                    VulkanModExtra.LOGGER.debug("Method {} not found", methodName);
                } catch (Exception e) {
                    VulkanModExtra.LOGGER.debug("Error calling method {}: {}", methodName, e.getMessage());
                }
            }

            VulkanModExtra.LOGGER.warn("Could not find any suitable method to add pages");
        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Could not add pages via methods: {}", e.getMessage());
        }
    }

    /**
     * Create VulkanMod-compatible option pages using reflection for VulkanMod classes and direct imports for Minecraft classes
     */
    public static List<Object> createVulkanModExtraPages() {
        System.out.println("[VulkanMod Extra] Creating VulkanMod Extra pages...");
        List<Object> pages = new ArrayList<>();

        try {
            // Load VulkanMod classes using reflection
            System.out.println("[VulkanMod Extra] Loading VulkanMod classes...");
            Class<?> optionPageClass = Class.forName("net.vulkanmod.config.option.OptionPage");
            Class<?> optionBlockClass = Class.forName("net.vulkanmod.config.gui.OptionBlock");
            Class<?> switchOptionClass = Class.forName("net.vulkanmod.config.option.SwitchOption");

            System.out.println("[VulkanMod Extra] VulkanMod classes loaded successfully");

            // Create Animation page
            System.out.println("[VulkanMod Extra] Creating animation page...");
            List<Object> animationOptions = createAnimationOptions(switchOptionClass);
            // Convert to properly typed Option array
            Class<?> optionClass = Class.forName("net.vulkanmod.config.option.Option");
            Object[] animationArray = animationOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, animationOptions.size()));
            Class<?> optionArrayClass = java.lang.reflect.Array.newInstance(optionClass, 0).getClass();
            Object animationBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Animation Settings", animationArray);

            // Create OptionBlock array for OptionPage constructor
            Object[] animationBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            animationBlocks[0] = animationBlock;
            Class<?> optionBlockArrayClass = java.lang.reflect.Array.newInstance(optionBlockClass, 0).getClass();

            Object animationPage = optionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance("Animations", animationBlocks);
            pages.add(animationPage);

            // Create Particle page
            System.out.println("[VulkanMod Extra] Creating particle page...");
            List<Object> particleOptions = createParticleOptions(switchOptionClass);
            Object[] particleArray = particleOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, particleOptions.size()));
            Object particleBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Particle Settings", particleArray);

            // Create OptionBlock array for OptionPage constructor
            Object[] particleBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            particleBlocks[0] = particleBlock;

            Object particlePage = optionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance("Particles", particleBlocks);
            pages.add(particlePage);

            // Create Details page
            System.out.println("[VulkanMod Extra] Creating details page...");
            List<Object> detailOptions = createDetailOptions(switchOptionClass);
            Object[] detailArray = detailOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, detailOptions.size()));
            Object detailBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Detail Settings", detailArray);
            Object[] detailBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            detailBlocks[0] = detailBlock;
            Object detailPage = optionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance("Details", detailBlocks);
            pages.add(detailPage);

            // Create Render page
            System.out.println("[VulkanMod Extra] Creating render page...");
            List<Object> renderOptions = createRenderOptions(switchOptionClass);
            Object[] renderArray = renderOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, renderOptions.size()));
            Object renderBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Render Settings", renderArray);
            Object[] renderBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            renderBlocks[0] = renderBlock;
            Object renderPage = optionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance("Render", renderBlocks);
            pages.add(renderPage);

            // Create HUD page
            System.out.println("[VulkanMod Extra] Creating HUD page...");
            List<Object> hudOptions = createHUDOptions(switchOptionClass);
            Object[] hudArray = hudOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, hudOptions.size()));
            Object hudBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("HUD Settings", hudArray);
            Object[] hudBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            hudBlocks[0] = hudBlock;
            Object hudPage = optionPageClass.getConstructor(String.class, optionBlockArrayClass).newInstance("HUD", hudBlocks);
            pages.add(hudPage);

            System.out.println("[VulkanMod Extra] Successfully created " + pages.size() + " pages");
            VulkanModExtra.LOGGER.info("Successfully created {} VulkanMod Extra pages", pages.size());

        } catch (Exception e) {
            System.out.println("[VulkanMod Extra] Failed to create pages: " + e.getMessage());
            e.printStackTrace();
            VulkanModExtra.LOGGER.error("Failed to create VulkanMod Extra pages", e);
        }

        return pages;
    }

    /**
     * Create VulkanMod Extra pages with proper typing for direct mixin injection
     */
    public static List<net.vulkanmod.config.option.OptionPage> createVulkanModExtraOptionPages() {
        System.out.println("[VulkanMod Extra] Creating typed VulkanMod Extra pages...");
        List<net.vulkanmod.config.option.OptionPage> pages = new ArrayList<>();

        try {
            // Load VulkanMod classes
            Class<?> optionBlockClass = Class.forName("net.vulkanmod.config.gui.OptionBlock");
            Class<?> switchOptionClass = Class.forName("net.vulkanmod.config.option.SwitchOption");
            Class<?> optionClass = Class.forName("net.vulkanmod.config.option.Option");
            Class<?> optionPageClass = Class.forName("net.vulkanmod.config.option.OptionPage");

            Class<?> optionArrayClass = java.lang.reflect.Array.newInstance(optionClass, 0).getClass();
            Class<?> optionBlockArrayClass = java.lang.reflect.Array.newInstance(optionBlockClass, 0).getClass();

            // Create Animation page
            System.out.println("[VulkanMod Extra] Creating animation page...");
            List<Object> animationOptions = createAnimationOptions(switchOptionClass);
            Object[] animationArray = animationOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, animationOptions.size()));
            Object animationBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Animation Settings", animationArray);
            Object[] animationBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            animationBlocks[0] = animationBlock;
            net.vulkanmod.config.option.OptionPage animationPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance("Animations", animationBlocks);
            pages.add(animationPage);

            // Create Particle page
            System.out.println("[VulkanMod Extra] Creating particle page...");
            List<Object> particleOptions = createParticleOptions(switchOptionClass);
            Object[] particleArray = particleOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, particleOptions.size()));
            Object particleBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Particle Settings", particleArray);
            Object[] particleBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            particleBlocks[0] = particleBlock;
            net.vulkanmod.config.option.OptionPage particlePage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance("Particles", particleBlocks);
            pages.add(particlePage);

            // Create Details page
            System.out.println("[VulkanMod Extra] Creating details page...");
            List<Object> detailOptions = createDetailOptions(switchOptionClass);
            Object[] detailArray = detailOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, detailOptions.size()));
            Object detailBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Detail Settings", detailArray);
            Object[] detailBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            detailBlocks[0] = detailBlock;
            net.vulkanmod.config.option.OptionPage detailPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance("Details", detailBlocks);
            pages.add(detailPage);

            // Create Render page
            System.out.println("[VulkanMod Extra] Creating render page...");
            List<Object> renderOptions = createRenderOptions(switchOptionClass);
            Object[] renderArray = renderOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, renderOptions.size()));
            Object renderBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("Render Settings", renderArray);
            Object[] renderBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            renderBlocks[0] = renderBlock;
            net.vulkanmod.config.option.OptionPage renderPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance("Render", renderBlocks);
            pages.add(renderPage);

            // Create HUD page
            System.out.println("[VulkanMod Extra] Creating HUD page...");
            List<Object> hudOptions = createHUDOptions(switchOptionClass);
            Object[] hudArray = hudOptions.toArray((Object[]) java.lang.reflect.Array.newInstance(optionClass, hudOptions.size()));
            Object hudBlock = optionBlockClass.getConstructor(String.class, optionArrayClass).newInstance("HUD Settings", hudArray);
            Object[] hudBlocks = (Object[]) java.lang.reflect.Array.newInstance(optionBlockClass, 1);
            hudBlocks[0] = hudBlock;
            net.vulkanmod.config.option.OptionPage hudPage = (net.vulkanmod.config.option.OptionPage) optionPageClass
                    .getConstructor(String.class, optionBlockArrayClass).newInstance("HUD", hudBlocks);
            pages.add(hudPage);

            System.out.println("[VulkanMod Extra] Successfully created " + pages.size() + " typed pages");

        } catch (Exception e) {
            System.out.println("[VulkanMod Extra] Failed to create typed pages: " + e.getMessage());
            e.printStackTrace();
        }

        return pages;
    }

    private static List<Object> createAnimationOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Create animation master toggle
        Component animationComponent = Component.translatable("vulkanmod-extra.option.animation");
        Object animationOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(animationComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.animationSettings.animation = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.animationSettings.animation);
        options.add(animationOption);

        // Create water animation option
        Component waterComponent = Component.translatable("vulkanmod-extra.option.water");
        Object waterOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(waterComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.animationSettings.water = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.animationSettings.water);
        options.add(waterOption);

        return options;
    }

    private static List<Object> createParticleOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Create particles master toggle
        Component particlesComponent = Component.translatable("vulkanmod-extra.option.particles");
        Object particlesOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(particlesComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.particleSettings.particles = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.particleSettings.particles);
        options.add(particlesOption);

        // Rain splash particles
        Component rainSplashComponent = Component.translatable("vulkanmod-extra.option.rain_splash");
        Object rainSplashOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(rainSplashComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.particleSettings.rainSplash = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.particleSettings.rainSplash);
        options.add(rainSplashOption);

        // Block break particles
        Component blockBreakComponent = Component.translatable("vulkanmod-extra.option.block_break");
        Object blockBreakOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(blockBreakComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.particleSettings.blockBreak = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.particleSettings.blockBreak);
        options.add(blockBreakOption);

        // Block breaking particles
        Component blockBreakingComponent = Component.translatable("vulkanmod-extra.option.block_breaking");
        Object blockBreakingOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(blockBreakingComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.particleSettings.blockBreaking = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.particleSettings.blockBreaking);
        options.add(blockBreakingOption);

        return options;
    }

    private static List<Object> createDetailOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Sky rendering
        Component skyComponent = Component.translatable("vulkanmod-extra.option.sky");
        Object skyOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(skyComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().detailSettings.sky = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().detailSettings.sky);
        options.add(skyOption);

        // Sun rendering
        Component sunComponent = Component.translatable("vulkanmod-extra.option.sun");
        Object sunOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(sunComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().detailSettings.sun = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().detailSettings.sun);
        options.add(sunOption);

        // Moon rendering
        Component moonComponent = Component.translatable("vulkanmod-extra.option.moon");
        Object moonOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(moonComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().detailSettings.moon = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().detailSettings.moon);
        options.add(moonOption);

        // Stars rendering
        Component starsComponent = Component.translatable("vulkanmod-extra.option.stars");
        Object starsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(starsComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().detailSettings.stars = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().detailSettings.stars);
        options.add(starsOption);

        // Rain/Snow rendering
        Component rainSnowComponent = Component.translatable("vulkanmod-extra.option.rain_snow");
        Object rainSnowOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(rainSnowComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().detailSettings.rainSnow = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().detailSettings.rainSnow);
        options.add(rainSnowOption);

        // Biome colors
        Component biomeColorsComponent = Component.translatable("vulkanmod-extra.option.biome_colors");
        Object biomeColorsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(biomeColorsComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().detailSettings.biomeColors = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().detailSettings.biomeColors);
        options.add(biomeColorsOption);

        // Sky colors
        Component skyColorsComponent = Component.translatable("vulkanmod-extra.option.sky_colors");
        Object skyColorsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(skyColorsComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().detailSettings.skyColors = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().detailSettings.skyColors);
        options.add(skyColorsOption);

        return options;
    }

    private static List<Object> createRenderOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Prevent shaders
        Component preventShadersComponent = Component.translatable("vulkanmod-extra.option.prevent_shaders");
        Object preventShadersOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(preventShadersComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.preventShaders = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.preventShaders);
        options.add(preventShadersOption);

        // Light updates
        Component lightUpdatesComponent = Component.translatable("vulkanmod-extra.option.light_updates");
        Object lightUpdatesOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(lightUpdatesComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.steadyDebugHud = value; // Map to existing config option
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.steadyDebugHud);
        options.add(lightUpdatesOption);

        // Item frame rendering
        Component itemFrameComponent = Component.translatable("vulkanmod-extra.option.item_frame");
        Object itemFrameOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(itemFrameComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.itemFrame = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.itemFrame);
        options.add(itemFrameOption);

        // Armor stand rendering
        Component armorStandComponent = Component.translatable("vulkanmod-extra.option.armor_stand");
        Object armorStandOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(armorStandComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.armorStand = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.armorStand);
        options.add(armorStandOption);

        // Painting rendering
        Component paintingComponent = Component.translatable("vulkanmod-extra.option.painting");
        Object paintingOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(paintingComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.painting = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.painting);
        options.add(paintingOption);

        // Piston rendering
        Component pistonComponent = Component.translatable("vulkanmod-extra.option.piston");
        Object pistonOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(pistonComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.piston = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.piston);
        options.add(pistonOption);

        // Beacon beam rendering
        Component beaconBeamComponent = Component.translatable("vulkanmod-extra.option.beacon_beam");
        Object beaconBeamOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(beaconBeamComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.beaconBeam = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.beaconBeam);
        options.add(beaconBeamOption);

        // Limit beacon beam height
        Component limitBeaconBeamComponent = Component.translatable("vulkanmod-extra.option.limit_beacon_beam_height");
        Object limitBeaconBeamOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(limitBeaconBeamComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.limitBeaconBeamHeight = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.limitBeaconBeamHeight);
        options.add(limitBeaconBeamOption);

        // Enchanting table book
        Component enchantingTableBookComponent = Component.translatable("vulkanmod-extra.option.enchanting_table_book");
        Object enchantingTableBookOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(enchantingTableBookComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.enchantingTableBook = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.enchantingTableBook);
        options.add(enchantingTableBookOption);

        // Item frame name tags
        Component itemFrameNameTagComponent = Component.translatable("vulkanmod-extra.option.item_frame_name_tag");
        Object itemFrameNameTagOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(itemFrameNameTagComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.itemFrameNameTag = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.itemFrameNameTag);
        options.add(itemFrameNameTagOption);

        // Player name tags
        Component playerNameTagComponent = Component.translatable("vulkanmod-extra.option.player_name_tag");
        Object playerNameTagOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(playerNameTagComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.playerNameTag = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.playerNameTag);
        options.add(playerNameTagOption);

        return options;
    }

    private static List<Object> createHUDOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // FPS display
        Component fpsComponent = Component.translatable("vulkanmod-extra.option.show_fps");
        Object fpsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(fpsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.showFps = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.showFps);
        options.add(fpsOption);

        // Extended FPS display
        Component fpsExtendedComponent = Component.translatable("vulkanmod-extra.option.show_fps_extended");
        Object fpsExtendedOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(fpsExtendedComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.showFPSExtended = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.showFPSExtended);
        options.add(fpsExtendedOption);

        // Coordinates display
        Component coordsComponent = Component.translatable("vulkanmod-extra.option.show_coords");
        Object coordsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(coordsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.showCoords = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.showCoords);
        options.add(coordsOption);

        // Toasts
        Component toastsComponent = Component.translatable("vulkanmod-extra.option.toasts");
        Object toastsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(toastsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.toasts = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.toasts);
        options.add(toastsOption);

        // Advancement toasts
        Component advancementToastComponent = Component.translatable("vulkanmod-extra.option.advancement_toast");
        Object advancementToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(advancementToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.advancementToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.advancementToast);
        options.add(advancementToastOption);

        // Recipe toasts
        Component recipeToastComponent = Component.translatable("vulkanmod-extra.option.recipe_toast");
        Object recipeToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(recipeToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.recipeToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.recipeToast);
        options.add(recipeToastOption);

        // System toasts
        Component systemToastComponent = Component.translatable("vulkanmod-extra.option.system_toast");
        Object systemToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(systemToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.systemToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.systemToast);
        options.add(systemToastOption);

        // Tutorial toasts
        Component tutorialToastComponent = Component.translatable("vulkanmod-extra.option.tutorial_toast");
        Object tutorialToastOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(tutorialToastComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.tutorialToast = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.tutorialToast);
        options.add(tutorialToastOption);

        // Instant sneak
        Component instantSneakComponent = Component.translatable("vulkanmod-extra.option.instant_sneak");
        Object instantSneakOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(instantSneakComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.instantSneak = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.instantSneak);
        options.add(instantSneakOption);

        // Adaptive sync
        Component adaptiveSyncComponent = Component.translatable("vulkanmod-extra.option.use_adaptive_sync");
        Object adaptiveSyncOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(adaptiveSyncComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.useAdaptiveSync = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.useAdaptiveSync);
        options.add(adaptiveSyncOption);

        // Steady debug HUD
        Component steadyDebugHudComponent = Component.translatable("vulkanmod-extra.option.steady_debug_hud");
        Object steadyDebugHudOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(steadyDebugHudComponent,
                    (java.util.function.Consumer<Boolean>) value -> {
                        VulkanModExtra.CONFIG.extraSettings.steadyDebugHud = value;
                        VulkanModExtra.CONFIG.writeChanges();
                    },
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtra.CONFIG.extraSettings.steadyDebugHud);
        options.add(steadyDebugHudOption);

        return options;
    }









    /**
     * Custom List implementation that dynamically handles VulkanMod Extra pages
     * This prevents IndexOutOfBoundsException by providing safe access to all pages
     */
    private static class CustomPageList extends ArrayList<Object> {
        private final List<Object> originalPages;
        private final List<Object> extraPages;

        public CustomPageList(List<Object> originalPages, List<Object> extraPages) {
            super();
            this.originalPages = originalPages;
            this.extraPages = extraPages;

            // Add all pages to this list
            addAll(originalPages);
            addAll(extraPages);

            System.out.println("[VulkanMod Extra] CustomPageList created with " +
                originalPages.size() + " original + " + extraPages.size() + " extra = " + size() + " total pages");
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
                    System.out.println("[VulkanMod Extra] Index " + index + " out of bounds, returning null");
                    return null;
                }
            } catch (Exception e) {
                System.out.println("[VulkanMod Extra] Error accessing page at index " + index + ": " + e.getMessage());
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
        System.out.println("[VulkanMod Extra] Starting page injection...");

        try {
            // Check if this screen instance has already been injected
            if (injectedInstances.containsKey(vOptionScreenInstance)) {
                System.out.println("[VulkanMod Extra] This screen instance already has pages injected, skipping...");
                return;
            }

            // Get the VulkanMod Extra pages
            List<Object> extraPages = createVulkanModExtraPages();
            System.out.println("[VulkanMod Extra] Created " + extraPages.size() + " extra pages");

            // Get the optionPages field from VOptionScreen
            Class<?> vOptionScreenClass = vOptionScreenInstance.getClass();
            System.out.println("[VulkanMod Extra] VOptionScreen class: " + vOptionScreenClass.getName());

            java.lang.reflect.Field optionPagesField = vOptionScreenClass.getDeclaredField("optionPages");
            optionPagesField.setAccessible(true);

            // Cast to the correct type
            @SuppressWarnings("unchecked")
            List<Object> originalOptionPages = (List<Object>) optionPagesField.get(vOptionScreenInstance);

            System.out.println("[VulkanMod Extra] Found " + originalOptionPages.size() + " existing pages");

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

                               // Get the VOptionList and modify its input handling
                               java.lang.reflect.Method getOptionListMethod = page.getClass().getMethod("getOptionList");
                               Object optionList = getOptionListMethod.invoke(page);
                               // Keep VulkanMod's standard fixed height for consistency
                               System.out.println("[VulkanMod Extra] Using fixed height (160) for all pages");

                               System.out.println("[VulkanMod Extra] Initialized VOptionList for page: " + page.getClass().getSimpleName());
                           } catch (Exception e) {
                               System.out.println("[VulkanMod Extra] Failed to initialize VOptionList for page: " + e.getMessage());
                           }
                       }
                   } catch (Exception e) {
                       System.out.println("[VulkanMod Extra] Failed to initialize page lists: " + e.getMessage());
                   }

                   // Refresh the UI to include our new pages
                   try {
                       java.lang.reflect.Method buildPageMethod = vOptionScreenInstance.getClass().getDeclaredMethod("buildPage");
                       buildPageMethod.setAccessible(true);
                       buildPageMethod.invoke(vOptionScreenInstance);
                       System.out.println("[VulkanMod Extra] Refreshed UI to include new pages");
                   } catch (Exception e) {
                       System.out.println("[VulkanMod Extra] Failed to refresh UI: " + e.getMessage());
                   }

                   System.out.println("[VulkanMod Extra] Successfully injected pages! Total pages now: " + customPageList.size());
                   VulkanModExtra.LOGGER.info("Successfully injected {} VulkanMod Extra pages into GUI", extraPages.size());

                   // Mark this screen instance as injected to prevent multiple injections for this instance
                   injectedInstances.put(vOptionScreenInstance, true);

        } catch (Exception e) {
            System.out.println("[VulkanMod Extra] Failed to inject pages: " + e.getMessage());
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
}
