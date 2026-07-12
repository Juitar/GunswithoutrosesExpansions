package juitar.gwrexpansions.compat.kubejs;

import dev.latvian.mods.kubejs.item.ItemBuilder;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractKubeJSGunBuilder extends ItemBuilder {
    protected int bonusDamage = 0;
    protected double damageMultiplier = 1.0D;
    protected int fireDelay = 10;
    protected double inaccuracy = 1.0D;
    protected int enchantability = 10;
    protected double headshotMultiplier = 1.0D;
    protected double projectileSpeed = 3.0D;
    protected int projectiles = 1;
    protected double knockback = 0.0D;
    protected double chanceFreeShot = 0.0D;
    protected int skillCooldown = 1;
    protected ProjectileConversion projectileConversion = ProjectileConversion.ORIGINAL;
    protected int pierceCount = 1;
    protected final List<AmmoConversion> ammoConversions = new ArrayList<>();
    protected Consumer<GunSkillContext> skillUse;

    protected AbstractKubeJSGunBuilder(ResourceLocation id, ResourceLocation gunTag) {
        super(id);
        tag(gunTag);
    }

    public AbstractKubeJSGunBuilder bonusDamage(int value) { bonusDamage = value; return this; }
    public AbstractKubeJSGunBuilder damageMultiplier(double value) { damageMultiplier = nonNegative(value, "damageMultiplier"); return this; }
    public AbstractKubeJSGunBuilder fireDelay(int value) { fireDelay = atLeast(value, 1, "fireDelay"); return this; }
    public AbstractKubeJSGunBuilder inaccuracy(double value) { inaccuracy = nonNegative(value, "inaccuracy"); return this; }
    public AbstractKubeJSGunBuilder enchantability(int value) { enchantability = atLeast(value, 0, "enchantability"); return this; }
    public AbstractKubeJSGunBuilder headshotMultiplier(double value) { headshotMultiplier = atLeast(nonNegative(value, "headshotMultiplier"), 1.0D, "headshotMultiplier"); return this; }
    public AbstractKubeJSGunBuilder projectileSpeed(double value) { projectileSpeed = atLeast(value, 0.01D, "projectileSpeed"); return this; }
    public AbstractKubeJSGunBuilder projectiles(int value) { projectiles = Math.min(atLeast(value, 1, "projectiles"), 64); return this; }
    public AbstractKubeJSGunBuilder knockback(double value) { knockback = finite(value, "knockback"); return this; }
    public AbstractKubeJSGunBuilder chanceFreeShot(double value) { chanceFreeShot = Math.min(1.0D, nonNegative(value, "chanceFreeShot")); return this; }
    public AbstractKubeJSGunBuilder skillCooldown(int value) { skillCooldown = atLeast(value, 1, "skillCooldown"); return this; }
    public AbstractKubeJSGunBuilder projectileConversion(Object value) { projectileConversion = ProjectileConversion.of(value); return this; }
    public AbstractKubeJSGunBuilder projectileConversion(Object source, Object target) {
        ammoConversions.add(new AmmoConversion(id(source, "source ammo"), id(target, "target ammo")));
        return this;
    }
    public AbstractKubeJSGunBuilder pierce(int value) { pierceCount = Math.min(atLeast(value, 1, "pierce"), 64); return this; }
    public AbstractKubeJSGunBuilder onSkillUse(Consumer<GunSkillContext> callback) { skillUse = callback; return this; }

    protected static int atLeast(int value, int min, String name) { if (value < min) throw new IllegalArgumentException(name + " must be >= " + min); return value; }
    protected static double atLeast(double value, double min, String name) { return Math.max(min, finite(value, name)); }
    protected static double nonNegative(double value, String name) { return atLeast(value, 0.0D, name); }
    protected static double finite(double value, String name) { if (!Double.isFinite(value)) throw new IllegalArgumentException(name + " must be finite"); return value; }
    private static ResourceLocation id(Object value, String name) {
        ResourceLocation id = ResourceLocation.tryParse(String.valueOf(value));
        if (id == null) throw new IllegalArgumentException(name + " must be a valid item id");
        return id;
    }
}
