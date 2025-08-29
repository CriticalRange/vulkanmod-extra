package com.criticalrange.config;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.criticalrange.VulkanModExtra;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.world.level.material.FogType;

public class VulkanModExtraConfig {
    private static final Gson GSON = new GsonBuilder()
            .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
            .setPrettyPrinting()
            .excludeFieldsWithModifiers(Modifier.PRIVATE)
            .create();

    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("vulkanmod-extra-options.json");

    // Constructor to initialize settings
    public VulkanModExtraConfig() {
        this.animationSettings = new AnimationSettings();
        this.particleSettings = new ParticleSettings();
    }

    // Settings categories - comprehensive configuration matching sodium-extra
    public final ExtraSettings extraSettings = new ExtraSettings();
    public AnimationSettings animationSettings;
    public ParticleSettings particleSettings;
    public final DetailSettings detailSettings = new DetailSettings();
    public final RenderSettings renderSettings = new RenderSettings();

    public static VulkanModExtraConfig load() {
        VulkanModExtraConfig config;

        if (Files.exists(CONFIG_PATH)) {
            try (FileReader reader = new FileReader(CONFIG_PATH.toFile())) {
                config = GSON.fromJson(reader, VulkanModExtraConfig.class);
                
                if (config == null) {
                    VulkanModExtra.LOGGER.info("Config file exists but is empty, creating default config");
                    config = new VulkanModExtraConfig();
                }
            } catch (Exception e) {
                VulkanModExtra.LOGGER.warn("Config parse failed, creating new config with defaults: {}", e.getMessage());
                
                // Backup the old config file
                try {
                    Path backupPath = CONFIG_PATH.resolveSibling("vulkanmod-extra-options.json.backup");
                    Files.move(CONFIG_PATH, backupPath);
                    VulkanModExtra.LOGGER.info("Old config backed up to: {}", backupPath);
                } catch (Exception backupError) {
                    VulkanModExtra.LOGGER.warn("Could not backup old config: {}", backupError.getMessage());
                }
                
                config = new VulkanModExtraConfig();
            }
        } else {
            VulkanModExtra.LOGGER.info("No config file found, creating default config");
            config = new VulkanModExtraConfig();
        }

        // Initialize settings if they weren't loaded from file (defensive programming)
        if (config.animationSettings == null) {
            config.animationSettings = new AnimationSettings();
        }
        if (config.particleSettings == null) {
            config.particleSettings = new ParticleSettings();
        }
        // Note: detailSettings and renderSettings are final, so they're always initialized
        // Ensure fog type defaults are set
        if (config.renderSettings != null) {
            config.renderSettings.ensureFogTypeDefaults();
        }

        config.writeChanges();
        return config;
    }


    public void writeChanges() {
        writeChanges(false);
    }
    
    public void writeChanges(boolean force) {
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            
            // Check if file actually needs updating to avoid unnecessary I/O
            if (!force && Files.exists(CONFIG_PATH)) {
                try {
                    String existingContent = Files.readString(CONFIG_PATH);
                    String newContent = GSON.toJson(this);
                    if (existingContent.equals(newContent)) {
                        return; // No changes needed
                    }
                } catch (Exception e) {
                    // If we can't read the existing file, proceed with writing
                    VulkanModExtra.LOGGER.debug("Could not compare existing config, proceeding with write: {}", e.getMessage());
                }
            }
            
            try (FileWriter writer = new FileWriter(CONFIG_PATH.toFile())) {
                GSON.toJson(this, writer);
            }
        } catch (IOException e) {
            VulkanModExtra.LOGGER.error("Could not save configuration file", e);
        }
    }

    // Extra Settings - HUD and general settings (matching sodium-extra)
    public static class ExtraSettings {
        public OverlayCorner overlayCorner = OverlayCorner.TOP_LEFT;
        public TextContrast textContrast = TextContrast.NONE;
        public boolean showFps = false;
        public FPSDisplayMode fpsDisplayMode = FPSDisplayMode.BASIC;
        public boolean showFPSExtended = true;    // Show additional FPS info (max, avg, min)
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
        
        // Performance settings
        public boolean enableVulkanModGuiIntegration = true; // Allow disabling GUI integration for better performance
        public boolean optimizeConfigWrites = true; // Only write config when changes are detected

        // Advanced features from sodium-extra
        public boolean advancedItemTooltips = false; // Show advanced item tooltips with identifiers and durability
        public boolean useFastRandom = false; // Use fast random function for block rendering
        public boolean linearFlatColorBlender = false; // Use linear flat color blender
    }
    
    // Detail Settings - sky and environmental rendering (from sodium-extra)
    public static class DetailSettings {
        public boolean sky = true;           // Render sky
        public boolean sun = true;          // Render sun
        public boolean moon = true;         // Render moon
        public boolean stars = true;        // Render stars
        public boolean rainSnow = true;     // Render rain & snow
        public boolean biomeColors = true;  // Biome-based colors
        public boolean skyColors = true;    // Biome-based sky colors
    }
    
    // Render Settings - entity and block rendering control (from sodium-extra)
    public static class RenderSettings {
        public boolean lightUpdates = true;           // Process lighting updates
        public boolean itemFrame = true;              // Render item frames
        public boolean armorStand = true;             // Render armor stands
        public boolean painting = true;               // Render paintings
        public boolean piston = true;                 // Render piston animations
        public boolean beaconBeam = true;             // Render beacon beams
        public boolean limitBeaconBeamHeight = false; // Limit beacon beam height to world max
        public boolean enchantingTableBook = true;    // Render enchanting table books
        public boolean itemFrameNameTag = true;       // Show item frame name tags
        public boolean playerNameTag = true;          // Show player name tags

        // Fog configuration - matching sodium-extra
        public boolean globalFog = true;              // Global fog rendering
        public EnumMap<FogType, FogTypeConfig> fogTypeConfig = new EnumMap<>(FogType.class);
        public boolean multiDimensionFog = false;     // Use per-dimension fog settings

        public RenderSettings() {
            ensureFogTypeDefaults();
        }

        public void ensureFogTypeDefaults() {
            for (FogType type : FogType.values()) {
                if (type == FogType.NONE) continue;
                fogTypeConfig.putIfAbsent(type, new FogTypeConfig());
            }
        }
    }

    // Animation Settings - control texture animations (matching sodium-extra)
    public static class AnimationSettings {
        public boolean animation = true;
        public boolean water = true;
        public boolean lava = true; 
        public boolean fire = true;
        public boolean portal = true;
        public boolean blockAnimations = true;
        public boolean sculkSensor = true;
    }

    // Particle Settings - comprehensive particle control (matching sodium-extra)
    public static class ParticleSettings {
        public boolean particles = true;
        public boolean rainSplash = true;
        public boolean blockBreak = true;
        public boolean blockBreaking = true;
        
        // Individual particle control - all from sodium-extra lang file
        public boolean ambientEntityEffect = true;    // Beacon effects
        public boolean angryVillager = true;         // Attacking villagers
        public boolean ash = true;                   // Soul sand valley biome
        public boolean barrier = true;               // Barrier block particles
        public boolean block = true;                 // Breaking blocks, sprinting, iron golems
        public boolean blockCrumble = true;
        public boolean blockMarker = true;           // Barrier/Light particles
        public boolean bubble = true;                // Entities in water, guardian beams
        public boolean bubbleColumnUp = true;        // Soul sand bubble columns
        public boolean bubblePop = true;
        public boolean campfireCosySmoke = true;     // Campfire smoke
        public boolean campfireSignalSmoke = true;   // Campfire smoke above hay bale
        public boolean cherryLeaves = true;          // Cherry petal particles
        public boolean cloud = true;                 // Death animation clouds
        public boolean composter = true;
        public boolean crimsonSpore = true;          // Crimson forest biome
        public boolean crit = true;                  // Critical hits, bow shots
        public boolean currentDown = true;           // Magma block bubble columns
        public boolean damageIndicator = true;       // Melee damage indicators
        public boolean dolphin = true;               // Swimming dolphin trails
        public boolean dragonBreath = true;
        public boolean drippingDripstoneLava = true;
        public boolean drippingDripstoneWater = true;
        public boolean drippingHoney = true;
        public boolean drippingLava = true;
        public boolean drippingObsidianTear = true;
        public boolean drippingWater = true;
        public boolean dust = true;                  // Redstone particles
        public boolean dustColorTransition = true;   // Sculk sensor triggers
        public boolean dustPillar = true;            // Mace smash attacks
        public boolean dustPlume = true;             // Decorated pots
        public boolean effect = true;                // Splash potions, bottles o' enchanting
        public boolean eggCrack = true;
        public boolean elderGuardian = true;
        public boolean electricSpark = true;         // Lightning on copper
        public boolean enchant = true;               // Enchanting table bookshelves
        public boolean enchantedHit = true;          // Enchanted weapon hits
        public boolean endRod = true;                // End rods, shulker bullets
        public boolean entityEffect = true;          // Status effects, lingering potions
        public boolean explosion = true;
        public boolean explosionEmitter = true;
        public boolean fallingDripstoneLava = true;
        public boolean fallingDripstoneWater = true;
        public boolean fallingDust = true;           // Floating sand/gravel
        public boolean fallingHoney = true;
        public boolean fallingLava = true;
        public boolean fallingNectar = true;         // Pollen-loaded bees
        public boolean fallingObsidianTear = true;
        public boolean fallingSporeBlosssom = true;
        public boolean fallingWater = true;
        public boolean firework = true;
        public boolean fishing = true;
        public boolean flame = true;                 // Torches, furnaces, magma cubes
        public boolean flash = true;                 // Firework explosions
        public boolean glow = true;                  // Glow squid
        public boolean glowSquidInk = true;
        public boolean gust = true;                  // Wind charge hits
        public boolean gustEmitterLarge = true;
        public boolean gustEmitterSmall = true;
        public boolean happyVillager = true;         // Bone meal, breeding, trading
        public boolean heart = true;                 // Breeding and taming
        public boolean infested = true;              // Infested effect
        public boolean instantEffect = true;         // Health/damage potions
        public boolean item = true;                  // Eating, thrown items
        public boolean itemCobweb = true;            // Weaving effect
        public boolean itemSlime = true;             // Slimes landing
        public boolean itemSnowball = true;          // Thrown snowballs
        public boolean landingHoney = true;
        public boolean landingLava = true;
        public boolean landingObsidianTear = true;
        public boolean largeSmoke = true;            // Fire, furnaces, blazes
        public boolean lava = true;                  // Lava bubbles
        public boolean lightBlock = true;            // Light block particles
        public boolean mycelium = true;              // Mycelium blocks
        public boolean nautilus = true;              // Activated conduits
        public boolean note = true;                  // Note blocks
        public boolean ominousSpawning = true;
        public boolean poof = true;                  // Explosions, mob death
        public boolean portal = true;                // Nether portals, endermen
        public boolean raidOmen = true;              // Raid Omen effect
        public boolean rain = true;                  // Rain splashes
        public boolean reversePortal = true;
        public boolean scrape = true;                // Copper oxidation scraping
        public boolean sculkCharge = true;
        public boolean sculkChargePop = true;
        public boolean sculkSoul = true;             // Mob death near Sculk Catalyst
        public boolean shriek = true;                // Sculk Shrieker
        public boolean smallFlame = true;
        public boolean smallGust = true;             // Wind Charged effect
        public boolean smoke = true;                 // Torches, TNT, droppers
        public boolean sneeze = true;                // Baby pandas
        public boolean snowflake = true;             // Powder snow
        public boolean sonicBoom = true;             // Warden sonic boom
        public boolean soul = true;                  // Soul Speed enchantment
        public boolean soulFireFlame = true;         // Soul torches
        public boolean spit = true;                  // Llama spitting
        public boolean splash = true;                // Entities in water
        public boolean sporeBlossomAir = true;       // Spore blossom ambience
        public boolean squidInk = true;
        public boolean sweepAttack = true;           // Sword sweep attacks
        public boolean totemOfUndying = true;
        public boolean trail = true;                 // Creaking mob trails
        public boolean trialOmen = true;
        public boolean trialSpawnerDetection = true;
        public boolean trialSpawnerDetectionOminous = true;
        public boolean underwater = true;            // Underwater particles
        public boolean vaultConnection = true;       // Near vault particles
        public boolean vibration = true;             // Sculk sensor vibrations
        public boolean warpedSpore = true;           // Warped forest biome
        public boolean waxOff = true;                // Removing wax from copper
        public boolean waxOn = true;                 // Waxing copper with honeycomb
        public boolean whiteAsh = true;              // Basalt deltas biome
        public boolean whiteSmoke = true;
        public boolean witch = true;


    }

    public enum OverlayCorner {
        TOP_LEFT,
        TOP_RIGHT,
        BOTTOM_LEFT,
        BOTTOM_RIGHT
    }

    public enum TextContrast {
        NONE,
        BACKGROUND,
        SHADOW
    }

    public enum FPSDisplayMode {
        BASIC,
        EXTENDED,
        DETAILED;

        public static String getComponentName(FPSDisplayMode mode) {
            return switch (mode) {
                case BASIC -> "vulkanmod-extra.option.fps_display_mode.basic";
                case EXTENDED -> "vulkanmod-extra.option.fps_display_mode.extended";
                case DETAILED -> "vulkanmod-extra.option.fps_display_mode.detailed";
            };
        }
    }

    // Fog type configuration - matching sodium-extra FogTypeConfig
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
