package com.henning.lightsabers.combat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class JediProtectionTest {
    @Test
    void shortFallsAreSafeAndHighFallsStillCauseReducedDamage() {
        assertEquals(0.0F, JediProtection.reduceFallDamage(0.0F));
        assertEquals(0.0F, JediProtection.reduceFallDamage(4.0F));
        assertEquals(2.4F, JediProtection.reduceFallDamage(10.0F), 0.0001F);
        assertEquals(38.4F, JediProtection.reduceFallDamage(100.0F), 0.0001F);
    }
}
