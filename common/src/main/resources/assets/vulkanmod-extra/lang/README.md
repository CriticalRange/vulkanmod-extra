# VulkanMod Extra Translations

Thank you for your interest in translating VulkanMod Extra!

## Available Languages

| Language | Code | Status | Completion |
|----------|------|--------|------------|
| English (United States) | `en_us.json` | ✅ Complete | 100% |
| Spanish (Spain) | `es_es.json` | 🟡 Partial | ~15% |
| German (Germany) | `de_de.json` | 🟡 Partial | ~15% |
| French (France) | `fr_fr.json` | 🟡 Partial | ~5% |
| Simplified Chinese | `zh_cn.json` | 🟡 Partial | ~5% |
| Traditional Chinese | `zh_tw.json` | 🟡 Partial | ~10% |
| Brazilian Portuguese | `pt_br.json` | 🟡 Partial | ~5% |
| Japanese | `ja_jp.json` | 🟡 Partial | ~15% |
| Korean | `ko_kr.json` | 🟡 Partial | ~15% |
| Russian | `ru_ru.json` | 🟡 Partial | ~15% |
| Italian | `it_it.json` | 🟡 Partial | ~5% |
| Dutch | `nl_nl.json` | 🟡 Partial | ~5% |
| Polish | `pl_pl.json` | 🟡 Partial | ~5% |
| Turkish | `tr_tr.json` | 🟡 Partial | ~5% |
| Czech | `cs_cz.json` | 🟡 Partial | ~5% |
| Swedish | `sv_se.json` | 🟡 Partial | ~5% |

**Total: 16 languages supported!**

## Contributing Translations

### Adding a New Language

1. Copy `en_us.json` to a new file with the appropriate [language code](https://minecraft.wiki/w/Language#Languages).
2. Translate all values (the text after the `:` in each line).
3. **Do NOT translate the keys** (the text before the `:` in each line).
4. Keep special formatting codes like `§7`, `§a`, `§c`, `§e` intact - these control text colors.
5. Keep placeholders like `%s` intact - these will be replaced with dynamic values.

### Completing Partial Translations

1. Open the partial translation file (e.g., `es_es.json`).
2. Find the missing translations by comparing with `en_us.json`.
3. Translate the missing keys while preserving the structure.
4. Test your translations in-game if possible.

## Translation Guidelines

### Key Naming Convention

- `vulkanmod-extra.pages.*` - Page/tab names
- `vulkanmod-extra.option.*` - Option labels
- `vulkanmod-extra.option.*.tooltip` - Option tooltips
- `vulkanmod-extra.block.*` - Section/block titles

### Special Formatting

#### Color Codes
Color codes start with `§` followed by a character:
- `§7` - Gray
- `§a` - Green (positive)
- `§e` - Yellow (neutral)
- `§6` - Gold (medium impact)
- `§c` - Red (high impact)

Example:
```json
"vulkanmod-extra.option.render.fog.tooltip": "Master toggle for all fog effects.\n\n§7Performance Impact: §eLow"
```

**Translation**: Translate the text but keep the `§7` and `§e` codes:
```json
"vulkanmod-extra.option.render.fog.tooltip": "Control maestro para todos los efectos de niebla.\n\n§7Impacto en Rendimiento: §eBajo"
```

#### Placeholders
- `%s` - String placeholder (will be replaced with text)
- `\n` - New line

Example:
```json
"vulkanmod-extra.block.additionalControls": "Additional Controls %s"
```

### Testing Your Translations

1. Place your language file in `.minecraft/resourcepacks/VulkanModExtra/assets/vulkanmod-extra/lang/`
2. Or build the mod with your translations included
3. Change your Minecraft language to match your translation
4. Open VulkanMod settings → VulkanMod Extra pages
5. Verify all text displays correctly

## Translation Priority

Focus on these high-priority sections first:

1. **Page names** (`vulkanmod-extra.pages.*`) - Most visible
2. **Master toggles** (`allAnimations`, `allParticles`, etc.) - Most frequently used
3. **Block titles** (`vulkanmod-extra.block.*`) - Navigation
4. **Common options** (FPS display, coordinates, fog controls)
5. **Tooltips** - Helpful but less critical

## Need Help?

- Check existing translations for examples
- Ask questions in [GitHub Discussions](https://github.com/CriticalRange/vulkanmod-extra/discussions)
- Open a [Pull Request](https://github.com/CriticalRange/vulkanmod-extra/pulls) when ready

## Credits

All translators will be credited in the project README and changelog!

Thank you for making VulkanMod Extra accessible to players worldwide! 🌍
