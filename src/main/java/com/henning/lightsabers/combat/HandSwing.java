package com.henning.lightsabers.combat;

/** Each hand owns its animation; clicks start immediately and are never queued. */
public final class HandSwing {
    private static final int DURATION_TICKS = 6;
    private int age = -1;

    public void start() {
        age = 0;
    }

    public void tick() {
        if (age >= 0 && ++age >= DURATION_TICKS) age = -1;
    }

    public float progress(float partialTick) {
        return age < 0 ? 0.0F : Math.min(1.0F, (age + partialTick) / DURATION_TICKS);
    }

    public void reset() {
        age = -1;
    }
}
