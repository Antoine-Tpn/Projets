package com.catacombes;

import com.badlogic.gdx.graphics.g2d.BitmapFont;

final class Fonts {
    private static final BitmapFont FONT = new BitmapFont();

    private Fonts() {
    }

    static BitmapFont get() {
        return FONT;
    }

    static void dispose() {
        FONT.dispose();
    }
}
