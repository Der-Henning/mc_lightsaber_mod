package com.henning.lightsabers.combat;

public final class SaberControls {
    private SaberControls() {
    }

    public static boolean usesMainHand(boolean rightButton, boolean mainArmIsRight) {
        return rightButton == mainArmIsRight;
    }
}
