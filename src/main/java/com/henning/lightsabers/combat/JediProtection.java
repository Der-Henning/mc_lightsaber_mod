package com.henning.lightsabers.combat;

public final class JediProtection {
    private JediProtection() {
    }

    public static float reduceFallDamage(float amount) {
        return Math.max(0.0F, amount - 4.0F) * 0.4F;
    }
}
