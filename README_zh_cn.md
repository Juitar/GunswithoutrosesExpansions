# Guns Without Roses Expansions

**语言 / Languages：** [简体中文](README_zh_cn.md) · [English](README.md)

Guns Without Roses Expansions（GWRE）为 Guns Without Roses 增加枪械、特殊弹丸、跨模组武器、技能和挑战进度。

本模组还包含多种不同的战斗循环：弹射、传送门、元素弹、法术核心、召唤物、地雷爆发、抽奖射击，以及围绕这些机制设计的挑战进度。

## Guns Without Roses 核心内容

### 下界合金武器

- 下界合金霰弹枪
- 下界合金狙击枪
- 下界合金加特林
- 下界合金怪物霰弹枪

### 特殊弹药

- 史莱姆弹：在多个目标之间弹跳。
- 下界合金弹：有概率回收并重复使用。
- 钻石弹：会分裂为穿透碎片。
- 金弹：可能碎裂成金粒，并有极低概率掉落金苹果。

## Meet Your Fight 集成

### 武器系列

- 沼泽呼唤者霰弹枪：射击时投掷沼泽地雷；每三个地雷会准备 X 形地雷爆发和强化射击。
- 暮陨日蚀爆能枪：发射穿透弹并召唤 Dusk Rose Spirit 协助战斗。
- Destiny Seven：抽奖狙击枪，使用铁弹、金弹和钻石弹作为彩票，产生 Bust、Double、Triple 和 Jackpot 结果。

### 特殊机制

- 沼泽地雷、Dusk Rose Spirit、Destiny 抽奖和 MYF 武器获取/挑战进度。

## Ice and Fire 集成

### 龙钢武器

- 火龙钢霰弹枪/狙击枪/加特林：点燃目标，并对冰龙造成额外伤害。
- 冰龙钢霰弹枪/狙击枪/加特林：冻结目标，并对火龙造成额外伤害。
- 雷龙钢霰弹枪/狙击枪/加特林：召唤闪电攻击目标。

### 特殊弹药

- 龙钢火、冰、雷元素弹。
- 银弹：对亡灵造成额外伤害，并可转化为元素弹。

## Cataclysm 集成

### 武器系列

- Cursium 狙击枪：发射自动追踪弹，并在爆头时召唤幻影长柄武器。
- Ignitium 加特林：生命偷取弹，并施加 Blazing Brand。
- 潮汐手枪：施加 Abyssal Curse，并能够制造空间传送门。
- 熔岩能量霰弹枪：命中时制造十字形火焰喷射。

### 特殊弹药

- Cursium 弹、Ignitium 弹、熔岩能量弹和潮汐弹。

## Bosses of Mass Destruction 集成

### 武器系列

- Hellforge 左轮手枪：投掷可让子弹在目标之间弹射的硬币。
- 黑曜石发射器：发射能够施放火焰、冰霜或神圣法术的黑曜石核心。
- Skullcrusher Pulverizer：将头骨弹药粉碎成骨片，并随时间获得射速和伤害提升。
- Void Spike：发射能够生成治疗孢子的高速生物武器。

### 特殊机制

- 硬币系统、法术施放、骨片和治疗孢子。

## 通用功能

- 完整配置：伤害、射击间隔、散布、弹丸池、召唤数量、地雷威力和其他武器参数。
- 进度系统：武器获取目标和独特挑战目标。
- 自定义音效、物品模型、提示、翻译和视觉效果。
- 受 DOOM 启发的超级霰弹枪肉钩系统。
- 可选集成代码受到保护，缺少可选模组时不会导致基础模组崩溃。

## KubeJS 枪械支持

GWRE 可选支持 KubeJS Forge `2001.6.5-build.26`，配套 Rhino `2001.2.3-build.10`，并需要 Architectury API `9.2.14`。脚本放在：

```text
kubejs/startup_scripts/
```

修改 startup script 后需要重启游戏，普通 `/reload` 不会重新注册物品。

首版提供四种枪械 Builder：

| KubeJS 类型             | 底层 GWR 类型           | 自动添加的标签                 |
| ----------------------- | ----------------------- | ------------------------------ |
| `gwrexpansions:gun`     | `GunItem`               | `gunswithoutroses:gun/pistol`  |
| `gwrexpansions:shotgun` | `ShotgunItem`           | `gunswithoutroses:gun/shotgun` |
| `gwrexpansions:gatling` | `GatlingItem`           | `gunswithoutroses:gun/gatling` |
| `gwrexpansions:sniper`  | 带狙击默认值的`GunItem` | `gunswithoutroses:gun/sniper`  |

标签会根据 Builder 类型自动添加，不需要再写额外的标签脚本。

### 基础示例

```js
StartupEvents.registry("item", (event) => {
  event
    .create("diamond_repeater", "gwrexpansions:gun")
    .displayName("钻石连发枪")
    .bonusDamage(4)
    .damageMultiplier(1.25)
    .fireDelay(8)
    .inaccuracy(0.5)
    .enchantability(12)
    .projectileSpeed(3.0)
    .headshotMultiplier(1.5);
});
```

### 参数表

| 方法                           | 作用                   | 限制/说明                       |
| ------------------------------ | ---------------------- | ------------------------------- |
| `.bonusDamage(number)`         | 额外固定伤害           | 必须是有限数值，可为负数        |
| `.damageMultiplier(number)`    | 伤害倍率               | 最小`0`                         |
| `.fireDelay(number)`           | 射击间隔，单位 tick    | 最小`1`                         |
| `.inaccuracy(number)`          | 散布/不精确度          | 最小`0`                         |
| `.enchantability(number)`      | 附魔能力               | 最小`0`                         |
| `.projectileSpeed(number)`     | 弹丸速度               | 必须大于`0`                     |
| `.headshotMultiplier(number)`  | 爆头倍率               | 最小`1`                         |
| `.projectiles(number)`         | 每次发射的弹丸数       | 最大`64`，霰弹枪常用            |
| `.knockback(number)`           | 弹丸击退加成           | 必须是有限数值                  |
| `.chanceFreeShot(number)`      | 不消耗弹药的概率       | 限制在`0..1`                    |
| `.fireDelayFractional(number)` | 亚 tick 射击间隔       | 仅加特林可用                    |
| `.pierce(number)`              | 穿透弹可穿过的实体数量 | 最小`1`，最大 `64`              |
| `.skillCooldown(number)`       | 技能冷却，单位 tick    | 保存在物品 NBT 中并在背包内递减 |

霰弹枪默认使用多弹丸；加特林支持亚 tick 射速；狙击枪仍然继承 GWR 的 `GunItem`，只是使用狙击标签和狙击默认参数。

### 弹丸转化

保持原始 GWR 弹丸：

```js
.projectileConversion('original')
```

转化为 GWR 穿透弹：

```js
.projectileConversion('gwrexpansions:piercing')
.pierce(2)
```

也可以把一种弹药转换为另一种 GWR 弹丸实现。源弹药仍然会被消耗，但实际由目标弹药的 `IBullet` 创建弹丸：

```js
.projectileConversion(
  'gunswithoutroses:iron_bullet',
  'gwrexpansions:diamond_bullet'
)
```

目标必须是已注册并实现 GWR `IBullet` 的弹药。目标 ID 无效时会写入日志并跳过转换。

### 服务端枪械技能

技能沿用 GWRE 现有的 R 键网络链路，只在逻辑服务端执行。冷却保存在每个 `ItemStack` 的 NBT 中，即使枪械放在玩家背包里也会继续递减，行为类似超级霰弹枪肉钩和 Hellforge 硬币系统。

```js
StartupEvents.registry("item", (event) => {
  event
    .create("overload_gun", "gwrexpansions:gun")
    .skillCooldown(40)
    .onSkillUse((ctx) => {
      ctx.message("过载模式启动！");
    });
});
```

`ctx.message(text)` 是安全的服务端消息方法。回调不会同步到客户端；脚本异常会被捕获并记录，不会导致服务器崩溃。

## 依赖

基础 GWRE：

- Minecraft `1.20.1`
- Forge `47+`
- Guns Without Roses `1.20.1-2.5.1+`
- Cloth Config `11+`（客户端）

启用 KubeJS 支持时额外安装：

- KubeJS Forge `2001.6.5-build.26`
- Rhino `2001.2.3-build.10`
- Architectury API `9.2.14`

KubeJS 是可选依赖；不安装时 GWRE 的基础功能仍可运行。

## 可选集成

- Meet Your Fight `1.20.1-1.6.0+`
- Ice and Fire `2.1.13+`
- Cataclysm `2.31+`
- Bosses of Mass Destruction `1.1.1+`
- Alex's Caves `2.0.2+`

## 链接

- [Discord](https://discord.gg/vjpr74mEJV)
- [GitHub](https://github.com/Juitar/GunswithoutrosesExpansions)
- [Sweet Charm O&#39; Mine](https://www.curseforge.com/minecraft/mc-mods/sweet-charm-o-mine)：Guns Without Roses 的 Curios 附属模组
