package com.henning.lightsabers.combat;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class CombatStateTest {
    @Test
    void identicalBlastersHaveIndependentHandCooldowns() {
        CombatState state = new CombatState();
        assertTrue(state.tryUse(CombatState.Action.MAIN_HAND_BLASTER, 100, 5));
        assertTrue(state.tryUse(CombatState.Action.OFF_HAND_BLASTER, 100, 5));
        assertFalse(state.tryUse(CombatState.Action.MAIN_HAND_BLASTER, 104, 5));
        assertTrue(state.tryUse(CombatState.Action.MAIN_HAND_BLASTER, 105, 5));
    }

    @Test
    void playerAndSessionTimersDoNotShareState() {
        CombatState oldSession = new CombatState();
        oldSession.tryUse(CombatState.Action.MAIN_HAND_BLASTER, 2000, 5);
        assertTrue(new CombatState().tryUse(CombatState.Action.MAIN_HAND_BLASTER, 0, 5));
        assertFalse(oldSession.isReady(CombatState.Action.MAIN_HAND_BLASTER, 2001));
    }

    @Test
    void failedUsesDoNotExtendCooldown() {
        CombatState state = new CombatState();
        assertTrue(state.tryUse(CombatState.Action.FORCE_JUMP, 0, 28));
        assertFalse(state.tryUse(CombatState.Action.FORCE_JUMP, 27, 28));
        assertTrue(state.isReady(CombatState.Action.FORCE_JUMP, 28));
        assertTrue(state.isReady(CombatState.Action.OFF_HAND_SABER, 27));
    }

    @Test
    void saberCooldownAndStrengthAreIndependentPerHand() {
        CombatState state = new CombatState();
        assertEquals(1.0F, state.attackStrength(CombatState.Action.MAIN_HAND_SABER, 0, 12.0D));
        state.tryUse(CombatState.Action.MAIN_HAND_SABER, 0, 6);
        assertFalse(state.tryUse(CombatState.Action.MAIN_HAND_SABER, 2, 6));
        assertTrue(state.tryUse(CombatState.Action.OFF_HAND_SABER, 2, 6));
        assertEquals(0.5F, state.attackStrength(CombatState.Action.MAIN_HAND_SABER, 6, 12.0D));
        assertEquals(1.0F, state.attackStrength(CombatState.Action.MAIN_HAND_SABER, 12, 12.0D));
    }

    @Test
    void perfectDeflectionLastsExactlyEightTicks() {
        CombatState state = new CombatState();
        assertFalse(state.hasPerfectDeflectWindow(0));
        state.openPerfectDeflectWindow(100, 8);
        assertTrue(state.hasPerfectDeflectWindow(100));
        assertTrue(state.hasPerfectDeflectWindow(107));
        assertFalse(state.hasPerfectDeflectWindow(108));
    }
}
