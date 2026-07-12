package juitar.gwrexpansions.compat.kubejs;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum ProjectileConversion implements StringRepresentable {
    ORIGINAL,
    PIERCING;

    public static ProjectileConversion of(Object value) {
        if (value == null) return ORIGINAL;
        String id = value.toString().toLowerCase(Locale.ROOT);
        return switch (id) {
            case "gwrexpansions:piercing", "piercing" -> PIERCING;
            case "gwrexpansions:original", "original", "none" -> ORIGINAL;
            default -> throw new IllegalArgumentException("Unknown GWRE projectile conversion: " + value);
        };
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
