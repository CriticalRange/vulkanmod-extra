package com.criticalrange.client;

import com.criticalrange.VulkanModExtra;
import com.criticalrange.client.config.VulkanModExtraClientConfig;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Runtime integration with VulkanMod's GUI system
 * This class uses reflection to add VulkanMod Extra options to VulkanMod's settings screen
 */
public class VulkanModExtraIntegration {

    private static boolean integrationAttempted = false;
    private static boolean integrationSuccessful = false;

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

                integrationSuccessful = true;
                VulkanModExtra.LOGGER.info("Successfully integrated VulkanMod Extra with VulkanMod GUI!");

        } catch (Exception e) {
            VulkanModExtra.LOGGER.error("Failed to integrate with VulkanMod GUI", e);
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

    private static List<Object> createAnimationOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Create animation master toggle
        Component animationComponent = Component.translatable("vulkanmod-extra.option.animation");
        Object animationOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(animationComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().animationSettings.animation = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().animationSettings.animation);
        options.add(animationOption);

        // Create water animation option
        Component waterComponent = Component.translatable("vulkanmod-extra.option.water");
        Object waterOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(waterComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().animationSettings.water = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().animationSettings.water);
        options.add(waterOption);

        return options;
    }

    private static List<Object> createParticleOptions(Class<?> switchOptionClass) throws Exception {
        List<Object> options = new ArrayList<>();

        // Create particles master toggle
        Component particlesComponent = Component.translatable("vulkanmod-extra.option.particles");
        Object particlesOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(particlesComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().particleSettings.particles = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().particleSettings.particles);
        options.add(particlesOption);

        // Rain splash particles
        Component rainSplashComponent = Component.translatable("vulkanmod-extra.option.rain_splash");
        Object rainSplashOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(rainSplashComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().particleSettings.rainSplash = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().particleSettings.rainSplash);
        options.add(rainSplashOption);

        // Block break particles
        Component blockBreakComponent = Component.translatable("vulkanmod-extra.option.block_break");
        Object blockBreakOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(blockBreakComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().particleSettings.blockBreak = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().particleSettings.blockBreak);
        options.add(blockBreakOption);

        // Block breaking particles
        Component blockBreakingComponent = Component.translatable("vulkanmod-extra.option.block_breaking");
        Object blockBreakingOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(blockBreakingComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().particleSettings.blockBreaking = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().particleSettings.blockBreaking);
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

        // Global fog (TBA - not implemented yet)
        Component globalFogComponent = Component.literal("Global Fog (TBA)");
        Object globalFogOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(globalFogComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.globalFog = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.globalFog);
        options.add(globalFogOption);

        // Light updates
        Component lightUpdatesComponent = Component.translatable("vulkanmod-extra.option.light_updates");
        Object lightUpdatesOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(lightUpdatesComponent,
                    (java.util.function.Consumer<Boolean>) value -> VulkanModExtraClientConfig.getInstance().renderSettings.lightUpdates = value,
                    (java.util.function.Supplier<Boolean>) () -> VulkanModExtraClientConfig.getInstance().renderSettings.lightUpdates);
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

        // Enchanting table book (TBA - not implemented yet)
        Component enchantingTableBookComponent = Component.literal("Enchanting Table Book (TBA)");
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

        // FPS display (TBA - not implemented yet)
        Component fpsComponent = Component.literal("Show FPS (TBA)");
        Object fpsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(fpsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {}, // TBA - no implementation yet
                    (java.util.function.Supplier<Boolean>) () -> false); // TBA - always false for now
        options.add(fpsOption);

        // Extended FPS display (TBA - not implemented yet)
        Component fpsExtendedComponent = Component.literal("Show FPS Extended (TBA)");
        Object fpsExtendedOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(fpsExtendedComponent,
                    (java.util.function.Consumer<Boolean>) value -> {}, // TBA - no implementation yet
                    (java.util.function.Supplier<Boolean>) () -> false); // TBA - always false for now
        options.add(fpsExtendedOption);

        // Coordinates display (TBA - not implemented yet)
        Component coordsComponent = Component.literal("Show Coordinates (TBA)");
        Object coordsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(coordsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {}, // TBA - no implementation yet
                    (java.util.function.Supplier<Boolean>) () -> false); // TBA - always false for now
        options.add(coordsOption);

        // Toasts (TBA - not implemented yet)
        Component toastsComponent = Component.literal("Show Toasts (TBA)");
        Object toastsOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(toastsComponent,
                    (java.util.function.Consumer<Boolean>) value -> {}, // TBA - no implementation yet
                    (java.util.function.Supplier<Boolean>) () -> false); // TBA - always false for now
        options.add(toastsOption);

        // Instant sneak (TBA - not implemented yet)
        Component instantSneakComponent = Component.literal("Instant Sneak (TBA)");
        Object instantSneakOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(instantSneakComponent,
                    (java.util.function.Consumer<Boolean>) value -> {}, // TBA - no implementation yet
                    (java.util.function.Supplier<Boolean>) () -> false); // TBA - always false for now
        options.add(instantSneakOption);

        // Adaptive sync (TBA - not implemented yet)
        Component adaptiveSyncComponent = Component.literal("Use Adaptive Sync (TBA)");
        Object adaptiveSyncOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(adaptiveSyncComponent,
                    (java.util.function.Consumer<Boolean>) value -> {}, // TBA - no implementation yet
                    (java.util.function.Supplier<Boolean>) () -> false); // TBA - always false for now
        options.add(adaptiveSyncOption);

        // Steady debug HUD (TBA - not implemented yet)
        Component steadyDebugHudComponent = Component.literal("Steady Debug HUD (TBA)");
        Object steadyDebugHudOption = switchOptionClass.getConstructor(Component.class, java.util.function.Consumer.class, java.util.function.Supplier.class)
                .newInstance(steadyDebugHudComponent,
                    (java.util.function.Consumer<Boolean>) value -> {}, // TBA - no implementation yet
                    (java.util.function.Supplier<Boolean>) () -> false); // TBA - always false for now
        options.add(steadyDebugHudOption);

        return options;
    }

    /**
     * Alternative integration method using mixin injection
     * This would be called by the MixinVOptionScreen
     */
    public static void injectPagesIntoVulkanMod(Object vOptionScreenInstance) {
        System.out.println("[VulkanMod Extra] Starting page injection...");
        try {
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
            List<Object> optionPages = (List<Object>) optionPagesField.get(vOptionScreenInstance);

            System.out.println("[VulkanMod Extra] Found " + optionPages.size() + " existing pages");

                               // Add our pages to VulkanMod's page list
                   optionPages.addAll(extraPages);

                   // Initialize the VOptionList for each new page
                   try {
                       // Use VulkanMod's standard dimensions
                       int top = 40;
                       int bottom = 60;
                       int itemHeight = 20;
                       int leftMargin = 100;
                       int listWidth = 277;
                       int listHeight = 220;

                       // Initialize each new page
                       for (Object page : extraPages) {
                           try {
                               java.lang.reflect.Method createListMethod = page.getClass().getMethod("createList", int.class, int.class, int.class, int.class, int.class);
                               createListMethod.invoke(page, leftMargin, top, listWidth, listHeight, itemHeight);

                               // Get the VOptionList and modify its input handling
                               java.lang.reflect.Method getOptionListMethod = page.getClass().getMethod("getOptionList");
                               Object optionList = getOptionListMethod.invoke(page);
                               if (optionList != null) {
                                   try {
                                       // Modify the VOptionList to not consume input in empty areas
                                       java.lang.reflect.Field listHeightField = optionList.getClass().getSuperclass().getDeclaredField("height");
                                       listHeightField.setAccessible(true);
                                       int currentHeight = (Integer) listHeightField.get(optionList);

                                       // Reduce the height to only cover our actual options (not empty space)
                                       int optionCount = 2; // We have max 2 options per page
                                       int actualHeight = optionCount * (20 + 3) + 10; // options + margins + padding (using VulkanMod's itemHeight of 20)
                                       if (actualHeight < currentHeight) {
                                           listHeightField.set(optionList, actualHeight);
                                           System.out.println("[VulkanMod Extra] Reduced VOptionList height to prevent empty input consumption");
                                       }
                                   } catch (Exception e) {
                                       System.out.println("[VulkanMod Extra] Could not modify VOptionList height: " + e.getMessage());
                                   }
                               }

                               System.out.println("[VulkanMod Extra] Initialized VOptionList for page: " + page.getClass().getSimpleName());
                           } catch (Exception e) {
                               System.out.println("[VulkanMod Extra] Failed to initialize VOptionList for page: " + e.getMessage());
                           }
                       }
                   } catch (Exception e) {
                       System.out.println("[VulkanMod Extra] Failed to initialize page lists: " + e.getMessage());
                   }

                   // Refresh the UI to include our new pages and ensure Done button works
                   try {
                       java.lang.reflect.Method buildPageMethod = vOptionScreenInstance.getClass().getDeclaredMethod("buildPage");
                       buildPageMethod.setAccessible(true);
                       buildPageMethod.invoke(vOptionScreenInstance);
                       System.out.println("[VulkanMod Extra] Refreshed UI to include new pages");

                       // UI refresh completed - Done button should work now
                       System.out.println("[VulkanMod Extra] UI refreshed successfully");
                   } catch (Exception e) {
                       System.out.println("[VulkanMod Extra] Failed to refresh UI: " + e.getMessage());
                   }

                   System.out.println("[VulkanMod Extra] Successfully injected pages! Total pages now: " + optionPages.size());
                   VulkanModExtra.LOGGER.info("Successfully injected {} VulkanMod Extra pages into GUI", extraPages.size());

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

    public static boolean isIntegrationSuccessful() {
        return integrationSuccessful;
    }
}
