package com.criticalrange.config;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import com.criticalrange.config.FogType;

/**
 * Refactored configuration system for VulkanMod Extra
 * Clean, modular, and maintainable configuration structure
 */
public class VulkanModExtraConfig {

    // Core settings
    public final CoreSettings coreSettings = new CoreSettings();

    // Feature categories
    public final AnimationSettings animationSettings = new AnimationSettings();
    public final ParticleSettings particleSettings = new ParticleSettings();
    public final RenderSettings renderSettings = new RenderSettings();
    public final HudSettings hudSettings = new HudSettings();
    public final PerformanceSettings performanceSettings = new PerformanceSettings();
    public final EnvironmentSettings environmentSettings = new EnvironmentSettings();

    // Legacy compatibility - maps old config paths to new ones
    @Deprecated
    public final ExtraSettings extraSettings = new ExtraSettings();

    // Settings categories for compatibility
    @Deprecated
    public AnimationSettings getAnimationSettings() { return animationSettings; }
    @Deprecated
    public ParticleSettings getParticleSettings() { return particleSettings; }
    @Deprecated
    public DetailSettings getDetailSettings() { return new DetailSettings(); }
    @Deprecated
    public RenderSettings getRenderSettings() { return renderSettings; }

    /**
     * Write configuration changes
     */
    public void writeChanges() {
        writeChanges(false);
    }

    /**
     * Write configuration changes with force option
     */
    public void writeChanges(boolean force) {
        // Note: Configuration save is handled by ConfigurationManager
        ConfigurationManager.getInstance().saveConfig();
    }

    // ===== SETTINGS CLASSES =====

    public static class CoreSettings {
        public boolean enableMod = true;
        public boolean enableGuiIntegration = true;
        public boolean enableDebugLogging = false;
        public boolean autoSaveConfig = true;
        public int configSaveInterval = 300; // seconds
    }

    public static class AnimationSettings {
        // Individual animation controls
        
        // Fluid animations
        public boolean water = true;
        public boolean waterStill = true;
        public boolean waterFlow = true;
        public boolean lava = true;
        public boolean lavaStill = true;
        public boolean lavaFlow = true;
        
        // Fire & light animations
        public boolean fire = true;
        public boolean fire0 = true;
        public boolean fire1 = true;
        public boolean soulFire = true;
        public boolean soulFire0 = true;
        public boolean soulFire1 = true;
        public boolean campfireFire = true;
        public boolean soulCampfireFire = true;
        public boolean lantern = true;
        public boolean soulLantern = true;
        public boolean seaLantern = true;
        
        // Portal animations
        public boolean portal = true;
        public boolean netherPortal = true;
        public boolean endPortal = true;
        public boolean endGateway = true;
        
        // Block animations
        public boolean blockAnimations = true;
        public boolean magma = true;
        public boolean prismarine = true;
        public boolean prismarineBricks = true;
        public boolean darkPrismarine = true;
        public boolean conduit = true;
        public boolean respawnAnchor = true;
        public boolean stonecutterSaw = true;
        
        // Machine animations (when active)
        public boolean machineAnimations = true;
        public boolean blastFurnaceFrontOn = true;
        public boolean smokerFrontOn = true;
        public boolean furnaceFrontOn = true;
        
        // Plant animations
        public boolean plantAnimations = true;
        public boolean kelp = true;
        public boolean kelpPlant = true;
        public boolean seagrass = true;
        public boolean tallSeagrassBottom = true;
        public boolean tallSeagrassTop = true;
        
        // Nether stem animations
        public boolean stemAnimations = true;
        public boolean warpedStem = true;
        public boolean crimsonStem = true;
        public boolean warpedHyphae = true;
        public boolean crimsonHyphae = true;
        
        // Sculk animations
        public boolean sculkAnimations = true;
        public boolean sculk = true;
        public boolean sculkVein = true;
        public boolean sculkSensor = true;
        public boolean sculkSensorSide = true;
        public boolean sculkSensorTop = true;
        public boolean sculkShrieker = true;
        public boolean sculkShriekerSide = true;
        public boolean sculkShriekerTop = true;
        public boolean calibratedSculkSensor = true;
        public boolean calibratedSculkSensorSide = true;
        public boolean calibratedSculkSensorTop = true;
        
        // Command block animations
        public boolean commandBlockAnimations = true;
        public boolean commandBlockFront = true;
        public boolean chainCommandBlockFront = true;
        public boolean repeatingCommandBlockFront = true;
        
        // Additional animated elements
        public boolean additionalAnimations = true;
        public boolean beacon = true;
        public boolean dragonEgg = true;
        public boolean brewingStandBase = true;
        public boolean cauldronWater = true;
    }

    public static class ParticleSettings {
        public boolean rainSplash = true;
        public boolean blockBreak = true;
        public boolean blockBreaking = true;
        public boolean ambientEntityEffect = true;
        public boolean angryVillager = true;
        public boolean ash = true;
        public boolean barrier = true;
        public boolean block = true;
        public boolean blockdust = true;
        public boolean blockMarker = true;
        public boolean bubble = true;
        public boolean bubbleColumnUp = true;
        public boolean bubblePop = true;
        public boolean campfireCosySmoke = true;
        public boolean campfireSignalSmoke = true;
        public boolean cherryLeaves = true;
        public boolean cloud = true;
        public boolean composter = true;
        public boolean crimsonSpore = true;
        public boolean crit = true;
        public boolean currentDown = true;
        public boolean damageIndicator = true;
        public boolean dolphin = true;
        public boolean dragonBreath = true;
        public boolean drippingDripstoneLava = true;
        public boolean drippingDripstoneWater = true;
        public boolean drippingHoney = true;
        public boolean drippingLava = true;
        public boolean drippingObsidianTear = true;
        public boolean drippingWater = true;
        public boolean dust = true;
        public boolean dustColorTransition = true;
        public boolean dustPillar = true;
        public boolean dustPlume = true;
        public boolean effect = true;
        public boolean eggCrack = true;
        public boolean elderGuardian = true;
        public boolean electricSpark = true;
        public boolean enchant = true;
        public boolean enchantedHit = true;
        public boolean endRod = true;
        public boolean entityEffect = true;
        public boolean explosion = true;
        public boolean explosionEmitter = true;
        public boolean fallingDripstoneLava = true;
        public boolean fallingDripstoneWater = true;
        public boolean fallingDust = true;
        public boolean fallingHoney = true;
        public boolean fallingLava = true;
        public boolean fallingNectar = true;
        public boolean fallingObsidianTear = true;
        public boolean fallingSporeBlossom = true;
        public boolean fallingWater = true;
        public boolean firework = true;
        public boolean fishing = true;
        public boolean flame = true;
        public boolean flash = true;
        public boolean glow = true;
        public boolean glowSquidInk = true;
        public boolean gust = true;
        public boolean gustEmitterLarge = true;
        public boolean gustEmitterSmall = true;
        public boolean happyVillager = true;
        public boolean heart = true;
        public boolean infested = true;
        public boolean instantEffect = true;
        public boolean item = true;
        public boolean itemCobweb = true;
        public boolean itemSlime = true;
        public boolean itemSnowball = true;
        public boolean landingHoney = true;
        public boolean landingLava = true;
        public boolean landingObsidianTear = true;
        public boolean largeSmoke = true;
        public boolean lava = true;
        public boolean lightBlock = true;
        public boolean mycelium = true;
        public boolean nautilus = true;
        public boolean note = true;
        public boolean ominousSpawning = true;
        public boolean poof = true;
        public boolean portal = true;
        public boolean raidOmen = true;
        public boolean rain = true;
        public boolean reversePortal = true;
        public boolean scrape = true;
        public boolean sculkCharge = true;
        public boolean sculkChargePop = true;
        public boolean sculkSoul = true;
        public boolean sculkShrieker = true;
        public boolean shriek = true;
        public boolean smallFlame = true;
        public boolean smallGust = true;
        public boolean smoke = true;
        public boolean sneeze = true;
        public boolean snowflake = true;
        public boolean sonicBoom = true;
        public boolean soul = true;
        public boolean soulFireFlame = true;
        public boolean spit = true;
        public boolean splash = true;
        public boolean sporeBlossomAir = true;
        public boolean squidInk = true;
        public boolean sweepAttack = true;
        public boolean totemOfUndying = true;
        public boolean trail = true;
        public boolean trialOmen = true;
        public boolean trialSpawnerDetection = true;
        public boolean trialSpawnerDetectionOminous = true;
        public boolean underwater = true;
        public boolean vaultConnection = true;
        public boolean vibration = true;
        public boolean warpedSpore = true;
        public boolean waxOff = true;
        public boolean waxOn = true;
        public boolean whiteAsh = true;
        public boolean whiteSmoke = true;
        public boolean witch = true;
        public boolean wither = true;
        public boolean witherArmor = true;
        public final Map<String, Boolean> otherParticles = new HashMap<>();
    }

    public static class RenderSettings {
        public boolean lightUpdates = true;
        public boolean itemFrame = true;
        public boolean armorStand = true;
        public boolean painting = true;
        public boolean piston = true;
        public boolean beaconBeam = true;
        public boolean limitBeaconBeamHeight = false;
        public boolean enchantingTableBook = true;
        public boolean itemFrameNameTag = true;
        public boolean playerNameTag = true;
        public boolean globalFog = true;
        public final EnumMap<FogType, FogTypeConfig> fogTypeConfig = new EnumMap<>(FogType.class);
        public boolean multiDimensionFog = false;

        public RenderSettings() {
            // Initialize fog configs
            for (FogType type : FogType.values()) {
                if (type != FogType.NONE) {
                    fogTypeConfig.put(type, new FogTypeConfig());
                }
            }
        }
    }

    public static class HudSettings {
        public OverlayCorner overlayCorner = OverlayCorner.TOP_LEFT;
        public TextContrast textContrast = TextContrast.NONE;
        public boolean showFps = false;
        public FPSDisplayMode fpsDisplayMode = FPSDisplayMode.BASIC;
        public boolean showFPSExtended = true;
        public boolean showCoords = false;
        public boolean toasts = true;
        public boolean advancementToast = true;
        public boolean recipeToast = true;
        public boolean systemToast = true;
        public boolean tutorialToast = true;
    }

    public static class PerformanceSettings {
        public boolean instantSneak = false;
        public boolean useAdaptiveSync = false;
        public boolean steadyDebugHud = true;
        public int steadyDebugHudRefreshInterval = 1;
        public boolean reduceResolutionOnMac = false;
        public boolean preventShaders = false;
        public boolean optimizeConfigWrites = true;
        public boolean useFastRandom = false;
        public boolean linearFlatColorBlender = false;
        public boolean advancedItemTooltips = false;
    }

    public static class EnvironmentSettings {
        public boolean sky = true;
        public boolean sun = true;
        public boolean moon = true;
        public boolean stars = true;
        public boolean rainSnow = true;
        public boolean biomeColors = true;
        public boolean skyColors = true;
        public int cloudHeight = 192;
        public int cloudDistance = 100;
    }

    // ===== LEGACY COMPATIBILITY CLASSES =====

    @Deprecated
    public static class ExtraSettings {
        public OverlayCorner overlayCorner = OverlayCorner.TOP_LEFT;
        public TextContrast textContrast = TextContrast.NONE;
        public boolean showFps = false;
        public FPSDisplayMode fpsDisplayMode = FPSDisplayMode.BASIC;
        public boolean showFPSExtended = true;
        public boolean showCoords = false;
        public boolean reduceResolutionOnMac = false;
        public boolean useAdaptiveSync = false;
        public int cloudHeight = 192;
        public int cloudDistance = 100;
        public boolean toasts = true;
        public boolean advancementToast = true;
        public boolean recipeToast = true;
        public boolean systemToast = true;
        public boolean tutorialToast = true;
        public boolean instantSneak = false;
        public boolean preventShaders = false;
        public boolean steadyDebugHud = true;
        public int steadyDebugHudRefreshInterval = 1;
        public boolean enableVulkanModGuiIntegration = true;
        public boolean optimizeConfigWrites = true;
        public boolean advancedItemTooltips = false;
        public boolean useFastRandom = false;
        public boolean linearFlatColorBlender = false;
    }

    @Deprecated
    public static class DetailSettings {
        public boolean sky = true;
        public boolean sun = true;
        public boolean moon = true;
        public boolean stars = true;
        public boolean rainSnow = true;
        public boolean biomeColors = true;
        public boolean skyColors = true;
    }

    // ===== ENUMS =====

    public enum OverlayCorner {
        TOP_LEFT, TOP_RIGHT, BOTTOM_LEFT, BOTTOM_RIGHT
    }

    public enum TextContrast {
        NONE, BACKGROUND, SHADOW
    }

    public enum FPSDisplayMode {
        BASIC, EXTENDED, DETAILED;

        public static String getComponentName(FPSDisplayMode mode) {
            return switch (mode) {
                case BASIC -> "vulkanmod-extra.option.extra.fps_display_mode.basic";
                case EXTENDED -> "vulkanmod-extra.option.extra.fps_display_mode.extended";
                case DETAILED -> "vulkanmod-extra.option.extra.fps_display_mode.detailed";
            };
        }
    }

    public static class FogTypeConfig {
        public boolean enable = true;
        public int environmentStartMultiplier = 100;
        public int environmentEndMultiplier = 100;
        public int renderDistanceStartMultiplier = 100;
        public int renderDistanceEndMultiplier = 100;
        public int skyEndMultiplier = 100;
        public int cloudEndMultiplier = 100;
    }
}