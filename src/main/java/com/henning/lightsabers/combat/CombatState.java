package com.henning.lightsabers.combat;

import java.util.EnumMap;

/** Transient timers owned by one player, separately on each logical side. */
public final class CombatState {
    public enum Action {
        MAIN_HAND_BLASTER, OFF_HAND_BLASTER, MAIN_HAND_SABER, OFF_HAND_SABER, FORCE_JUMP, SABER_USE
    }

    private final EnumMap<Action, Long> readyAt = new EnumMap<>(Action.class);
    private final EnumMap<Action, Long> lastUsedAt = new EnumMap<>(Action.class);
    private long perfectDeflectUntil = Long.MIN_VALUE;

    public boolean isReady(Action action, long tick) {
        return tick >= readyAt.getOrDefault(action, Long.MIN_VALUE);
    }

    public boolean tryUse(Action action, long tick, int cooldownTicks) {
        if (!isReady(action, tick)) {
            return false;
        }
        readyAt.put(action, tick + cooldownTicks);
        lastUsedAt.put(action, tick);
        return true;
    }

    public float attackStrength(Action action, long tick, double recoveryTicks) {
        Long previous = lastUsedAt.get(action);
        return previous == null ? 1.0F : (float) Math.clamp((tick - previous) / recoveryTicks, 0.0D, 1.0D);
    }

    public void openPerfectDeflectWindow(long tick, int durationTicks) {
        perfectDeflectUntil = tick + durationTicks;
    }

    public boolean hasPerfectDeflectWindow(long tick) {
        return tick < perfectDeflectUntil;
    }
}
