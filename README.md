# Guns Without Roses Expansions

**Languages:** [English](README.md) · [简体中文](README_zh_cn.md)

[![CurseForge](http://cf.way2muchnoise.eu/guns-without-roses.svg)](https://www.curseforge.com/minecraft/mc-mods/gunswithoutroses-expansions)

![](https://media.forgecdn.net/attachments/description/1219417/description_4e919a27-0c6e-47bb-bad9-7200ea6b97cf.png)

Guns Without Roses Expansions adds powerful new firearms, specialized ammunition, configurable weapon mechanics, and cross-mod boss-themed upgrades to Guns Without Roses.

The mod focuses on high-impact weapons with distinct combat loops: ricochets, portals, elemental bullets, spell cores, summons, mine bursts, lottery shots, and challenge advancements built around those mechanics.

## Core Guns Without Roses Content

### Netherite Tier Weapons

- Netherite Shotgun
- Netherite Sniper
- Netherite Gatling
- Netherite Monster Shotgun

### Special Ammunition

- Slime Bullet - bounces between multiple targets.
- Netherite Bullet - reusable bullet with a recovery chance.
- Diamond Bullet - splits into piercing fragments.
- Golden Bullet - may shatter into gold nuggets, with a rare golden apple drop.

## Meet Your Fight Integration

### Weapon Series

- Mirecaller Shotgun - Swampjaw-themed shotgun that throws swamp mines while firing; every third mine readies an X-pattern mine burst and empowered shot.
- Duskfall Eclipse Blaster - Rosalyne-themed burst gun with piercing rounds and player-owned Dusk Rose Spirits that assist in combat.
- Destiny Seven - Dame Fortuna-themed lottery sniper that uses iron, golden, and diamond bullets as tickets for Bust, Double, Triple, and Jackpot outcomes.

### Special Mechanics

- Mirecaller Mines - configurable mine explosion power, mine charge tracking, burst-ready tooltip, and Swampjaw-inspired sound cues.
- Dusk Rose Spirits - follow the player, can be damaged, attack enemies, attribute their damage to the owner, and grant configurable Duskfall-only damage bonus plus damage reduction.
- Destiny Lottery - configurable odds, shot counts, pity scaling, weighted bullet pools, and rare Obsidian Core reward shots when available.
- MYF Advancements - one obtain advancement and one challenge advancement for each MYF weapon.

## Ice and Fire Integration

### Dragonsteel Weapons

- Fire Dragonsteel Shotgun/Sniper/Gatling - ignites targets and deals bonus damage against Ice Dragons.
- Ice Dragonsteel Shotgun/Sniper/Gatling - freezes targets and deals bonus damage against Fire Dragons.
- Lightning Dragonsteel Shotgun/Sniper/Gatling - strikes targets with lightning.

### Special Ammunition

- Dragonsteel Elemental Bullets - fire, ice, and lightning bullet variants.
- Silver Bullets - extra damage against undead, and can be converted into elemental bullets.

## Cataclysm Integration

### Weapon Series

- Cursium Sniper - auto-tracking bullets that summon phantasmal halberds on headshots.
- Ignitium Gatling - life-stealing bullets that apply Blazing Brand.
- Tidal Pistol - applies Abyssal Curse and can create dimensional portals.
- Lavapower Shotgun - creates cross-shaped flame jets.

### Special Ammunition

- Cursium Bullets - track enemies automatically.
- Ignitium Bullets - apply Blazing Brand.
- Lavapower Bullets - create flame jets on impact.
- Tidal Bullets - apply stacking Abyssal Curse.

## Bosses of Mass Destruction Integration

### Weapon Series

- Hellforge Revolver - marks targets and throws coins that bullets can ricochet between.
- Obsidian Launcher - fires Obsidian Cores that cast random Fire, Frost, or Holy spells.
- Skullcrusher Pulverizer - crushes skulls into bone fragments, gaining speed and damage over time.
- Void Spike - rapid-fire biological weapon that launches healing spores.

### Special Mechanics

- Coin System - earn coins by shooting, then throw them to create ricochet points for tracking bullets.
- Spell Casting - Obsidian Cores cast different magical effects.
- Bone Fragments - skull ammunition shatters into multiple piercing projectiles.
- Healing Spores - bud bullets explode into spores that heal the shooter when hitting enemies.

## Universal Features

- Full configuration options for damage, fire delay, inaccuracy, burst behavior, projectile pools, summon limits, mine power, and other weapon parameters.
- Advancement system with weapon obtain goals and unique challenge goals.
- Custom sounds, item models, tooltips, translations, and visual effects.
- Meat Hook system for the Super Shotgun, inspired by DOOM.
- Optional integration code is guarded so missing optional mods should not crash the base mod.

## KubeJS Gun Support

GWRE optionally integrates with KubeJS Forge `2001.6.5-build.26` (with Rhino `2001.2.3-build.10`). Architectury API `9.2.14` is also required by KubeJS. Put registration code in `kubejs/startup_scripts/` and restart the game after changing startup registrations.

Four gun builders are available:

| KubeJS type | GWR implementation | Automatic GWR tag |
|---|---|---|
| `gwrexpansions:gun` | `GunItem` | `gunswithoutroses:gun/pistol` |
| `gwrexpansions:shotgun` | `ShotgunItem` | `gunswithoutroses:gun/shotgun` |
| `gwrexpansions:gatling` | `GatlingItem` | `gunswithoutroses:gun/gatling` |
| `gwrexpansions:sniper` | `GunItem` with sniper defaults | `gunswithoutroses:gun/sniper` |

Tags are assigned automatically; no extra tag script is required.

### Basic example

```js
StartupEvents.registry('item', event => {
  event.create('diamond_repeater', 'gwrexpansions:gun')
    .displayName('Diamond Repeater')
    .bonusDamage(4)
    .damageMultiplier(1.25)
    .fireDelay(8)
    .inaccuracy(0.5)
    .enchantability(12)
    .projectileSpeed(3.0)
    .headshotMultiplier(1.5)
})
```

### Parameters

| Method | Meaning | Notes |
|---|---|---|
| `.bonusDamage(number)` | Flat bonus damage | May be negative; must be finite |
| `.damageMultiplier(number)` | Damage multiplier | Minimum `0` |
| `.fireDelay(number)` | Fire delay in ticks | Minimum `1` |
| `.inaccuracy(number)` | Spread/inaccuracy | Minimum `0` |
| `.enchantability(number)` | Enchanting value | Minimum `0` |
| `.projectileSpeed(number)` | Projectile speed | Must be positive |
| `.headshotMultiplier(number)` | Headshot multiplier | Minimum `1` |
| `.projectiles(number)` | Projectiles per shot | Maximum `64`; mainly useful for shotguns |
| `.knockback(number)` | Projectile knockback bonus | Must be finite |
| `.chanceFreeShot(number)` | Chance to preserve ammunition | Clamped to `0..1` |
| `.fireDelayFractional(number)` | Gatling fractional fire delay | Gatling only |
| `.pierce(number)` | Number of entities a converted piercing projectile can pierce | Minimum `1`, maximum `64` |
| `.skillCooldown(number)` | Skill cooldown in ticks | Stored on the ItemStack and ticks down in inventory |

Shotgun builders default to multiple projectiles. Gatling builders support fractional fire delay. Sniper builders use the sniper tag and sniper-oriented defaults but remain based on GWR's `GunItem`.

### Projectile conversion

Keep the original GWR projectile:

```js
.projectileConversion('original')
```

Convert it to a GWR piercing projectile:

```js
.projectileConversion('gwrexpansions:piercing')
.pierce(2)
```

Replace one ammunition item with another projectile implementation. The source ammunition is consumed, while the target `IBullet` creates the projectile:

```js
.projectileConversion(
  'gunswithoutroses:iron_bullet',
  'gwrexpansions:diamond_bullet'
)
```

The target must be a registered GWR-compatible `IBullet`. Invalid target IDs are reported in the log and do not silently create an unrelated projectile.

### Server-side gun skills

Skills use GWRE's existing R-key network path and execute on the logical server. The cooldown belongs to the ItemStack, so it continues to tick while the gun is in the player's inventory, matching the Super Shotgun hook and Hellforge coin systems.

```js
StartupEvents.registry('item', event => {
  event.create('overload_gun', 'gwrexpansions:gun')
    .skillCooldown(40)
    .onSkillUse(ctx => {
      ctx.message('Overload activated!')
    })
})
```

`ctx.message(text)` is the safe convenience method for sending a server-side player message. The callback is never serialized to the client. Script exceptions are caught and logged without crashing the server.

## Requirements

- Minecraft `1.20.1`
- Forge `47+`
- Guns Without Roses `1.20.1-2.5.1+`
- Cloth Config `11+` on client

KubeJS support is optional. If enabled, install KubeJS Forge `2001.6.5-build.26`, Rhino `2001.2.3-build.10`, and Architectury API `9.2.14`.

## Optional Integrations

- Meet Your Fight `1.20.1-1.6.0+`
- Ice and Fire `2.1.13+`
- Cataclysm `2.31+`
- Bosses of Mass Destruction `1.1.1+`
- Alex's Caves `2.0.2+`

## Links

- [Discord](https://discord.gg/vjpr74mEJV)
- [GitHub](https://github.com/Juitar/GunswithoutrosesExpansions)
- [Sweet Charm O' Mine](https://www.curseforge.com/minecraft/mc-mods/sweet-charm-o-mine) - Curios addon for Guns Without Roses
