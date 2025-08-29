package com.criticalrange.client.config;

public class VulkanModExtraClientConfig {
    // Client-side configuration that extends the main config with Minecraft-specific settings

    // Animation Settings
    public final AnimationSettings animationSettings = new AnimationSettings();
    public final ParticleSettings particleSettings = new ParticleSettings();
    public final DetailSettings detailSettings = new DetailSettings();
    public final RenderSettings renderSettings = new RenderSettings();

    // Animation Settings
    public static class AnimationSettings {
        public boolean animation = true;
        public boolean water = true;
        public boolean lava = true;
        public boolean fire = true;
        public boolean portal = true;
        public boolean blockAnimations = true;
        public boolean sculkSensor = true;
    }

    // Particle Settings - comprehensive particle control
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
        public boolean landingHoney = true;
        public boolean fallingLava = true;
        public boolean fallingNectar = true;         // Pollen-loaded bees
        public boolean fallingObsidianTear = true;
        public boolean fallingSporeBlossom = true;
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
        public boolean itemCobweb = true;             // Weaving effect
        public boolean itemSlime = true;             // Slimes landing
        public boolean itemSnowball = true;           // Thrown snowballs
        public boolean landingLava = true;            // Landing lava
        public boolean landingObsidianTear = true;    // Landing obsidian tears
        public boolean largeSmoke = true;             // Large smoke particles
        public boolean lava = true;                  // Lava bubbles
        public boolean lightBlock = true;             // Light block particles
        public boolean mycelium = true;              // Mycelium blocks
        public boolean nautilus = true;              // Activated conduits
        public boolean note = true;                  // Note blocks
        public boolean ominousSpawning = true;       // Ominous spawning
        public boolean poof = true;                  // Explosions, mob death
        public boolean portal = true;                // Nether portals
        public boolean raidOmen = true;              // Raid Omen effect
        public boolean rain = true;                  // Rain splashes
        public boolean reversePortal = true;         // Reverse portal
        public boolean scrape = true;                // Copper oxidation scraping
        public boolean sculkCharge = true;           // Sculk spreading
        public boolean sculkChargePop = true;        // Sculk charge popping
        public boolean sculkSoul = true;             // Mob death near Sculk Catalyst
        public boolean shriek = true;                // Sculk Shrieker
        public boolean smallFlame = true;            // Small flame particles
        public boolean smallGust = true;             // Wind Charged effect
        public boolean smoke = true;                 // Torches, TNT, droppers
        public boolean sneeze = true;                // Baby pandas sneezing
        public boolean snowflake = true;             // Powder snow
        public boolean sonicBoom = true;             // Warden sonic boom
        public boolean soul = true;                  // Soul Speed enchantment
        public boolean soulFireFlame = true;         // Soul torches
        public boolean spit = true;                  // Llama spitting
        public boolean splash = true;                // Entities in water
        public boolean sporeBlossomAir = true;       // Spore blossom ambience
        public boolean squidInk = true;              // Squid ink
        public boolean sweepAttack = true;           // Sword sweep attacks
        public boolean totemOfUndying = true;        // Totem of undying
        public boolean trail = true;                 // Creaking mob trails
        public boolean trialOmen = true;             // Trial Omen effect
        public boolean trialSpawnerDetection = true; // Trial Spawner activation
        public boolean trialSpawnerDetectionOminous = true; // Ominous Trial Spawner
        public boolean underwater = true;            // Underwater particles
        public boolean vaultConnection = true;       // Near vault particles
        public boolean vibration = true;             // Sculk sensor vibrations
        public boolean warpedSpore = true;           // Warped forest biome
        public boolean waxOff = true;                // Removing wax from copper
        public boolean waxOn = true;                 // Waxing copper
        public boolean whiteAsh = true;              // Basalt deltas biome
        public boolean whiteSmoke = true;
        public boolean witch = true;
    }

    // Detail Settings
    public static class DetailSettings {
        public boolean sky = true;
        public boolean sun = true;
        public boolean moon = true;
        public boolean stars = true;
        public boolean rainSnow = true;
        public boolean biomeColors = true;
        public boolean skyColors = true;
    }

    // Render Settings - entity and block rendering control
    public static class RenderSettings {
        public boolean globalFog = true;
        public boolean multiDimensionFog = false;
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
    }



    // Static instance for easy access
    private static VulkanModExtraClientConfig instance;

    public static VulkanModExtraClientConfig getInstance() {
        if (instance == null) {
            instance = new VulkanModExtraClientConfig();
        }
        return instance;
    }
}
