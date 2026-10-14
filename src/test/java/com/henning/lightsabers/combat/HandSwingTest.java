package com.henning.lightsabers.combat;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

class HandSwingTest {
    @Test
    void eachHandStartsImmediatelyAndIndependently() {
        HandSwing right = new HandSwing();
        HandSwing left = new HandSwing();
        right.start();
        assertTrue(right.progress(0.5F) > 0.0F);
        assertEquals(0.0F, left.progress(0.5F));
        right.tick();
        left.start();
        assertTrue(right.progress(0.5F) > left.progress(0.5F));
        for (int tick = 0; tick < 6; tick++) left.tick();
        assertEquals(0.0F, left.progress(0.5F));
    }

    @Test
    void swingsNeverReplayAfterFinishing() {
        HandSwing swing = new HandSwing();
        for (int i = 0; i < 50; i++) swing.start();
        for (int tick = 0; tick < 20; tick++) swing.tick();
        assertEquals(0.0F, swing.progress(0.5F));
    }

    @Test
    void unequippingClearsAnimation() {
        HandSwing swing = new HandSwing();
        swing.start();
        swing.reset();
        assertEquals(0.0F, swing.progress(0.5F));
    }

    @Test
    void physicalButtonMappingRespectsTheMainArmSetting() {
        assertTrue(SaberControls.usesMainHand(true, true));
        assertFalse(SaberControls.usesMainHand(false, true));
        assertFalse(SaberControls.usesMainHand(true, false));
        assertTrue(SaberControls.usesMainHand(false, false));
    }
}
