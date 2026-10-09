package com.catacombes;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap.Format;

final class PixelArt {
    static final int SPRITE_SIZE = 48;
    private static final int TILE_SIZE = 48;

    private PixelArt() {
    }

    static Texture floor() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Format.RGBA8888);
        pixmap.setColor(new Color(0.20f, 0.19f, 0.18f, 1f));
        pixmap.fill();
        pixmap.setColor(new Color(0.25f, 0.23f, 0.21f, 1f));
        pixmap.drawLine(3, 13, 20, 11);
        pixmap.drawLine(27, 7, 44, 9);
        pixmap.drawLine(7, 36, 19, 34);
        pixmap.drawLine(28, 27, 43, 29);
        pixmap.setColor(new Color(0.13f, 0.14f, 0.14f, 1f));
        pixmap.drawLine(20, 11, 23, 16);
        pixmap.drawLine(33, 28, 29, 34);
        pixmap.setColor(new Color(0.32f, 0.29f, 0.25f, 1f));
        pixmap.drawPixel(7, 24);
        pixmap.drawPixel(38, 18);
        pixmap.drawPixel(25, 42);
        return texture(pixmap);
    }

    static Texture wall() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Format.RGBA8888);
        pixmap.setColor(new Color(0.27f, 0.26f, 0.25f, 1f));
        pixmap.fill();
        pixmap.setColor(new Color(0.35f, 0.32f, 0.29f, 1f));
        pixmap.fillRectangle(2, 3, 20, 12);
        pixmap.fillRectangle(25, 3, 20, 12);
        pixmap.fillRectangle(13, 18, 21, 12);
        pixmap.fillRectangle(2, 33, 20, 12);
        pixmap.fillRectangle(25, 33, 20, 12);
        pixmap.setColor(new Color(0.15f, 0.15f, 0.15f, 1f));
        pixmap.drawLine(0, 17, 48, 17);
        pixmap.drawLine(0, 32, 48, 32);
        pixmap.drawLine(23, 0, 23, 16);
        pixmap.drawLine(11, 18, 11, 31);
        pixmap.drawLine(36, 18, 36, 31);
        pixmap.drawLine(23, 33, 23, 47);
        pixmap.setColor(new Color(0.45f, 0.36f, 0.27f, 1f));
        pixmap.drawLine(5, 7, 11, 6);
        pixmap.drawLine(30, 38, 38, 37);
        return texture(pixmap);
    }

    static Texture exit() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0f));
        pixmap.fill();
        pixmap.setColor(new Color(0.10f, 0.08f, 0.07f, 1f));
        pixmap.fillRectangle(7, 2, 34, 44);
        pixmap.setColor(new Color(0.40f, 0.29f, 0.19f, 1f));
        pixmap.fillRectangle(11, 5, 26, 38);
        pixmap.setColor(new Color(0.15f, 0.12f, 0.11f, 1f));
        pixmap.fillRectangle(16, 5, 17, 34);
        pixmap.setColor(new Color(0.92f, 0.62f, 0.24f, 1f));
        pixmap.fillCircle(28, 25, 2);
        return texture(pixmap);
    }

    static Texture potion() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0f));
        pixmap.fill();
        pixmap.setColor(new Color(0.83f, 0.78f, 0.59f, 1f));
        pixmap.fillRectangle(20, 8, 9, 7);
        pixmap.setColor(new Color(0.68f, 0.19f, 0.23f, 1f));
        pixmap.fillRectangle(14, 16, 21, 22);
        pixmap.setColor(new Color(0.92f, 0.36f, 0.34f, 1f));
        pixmap.fillRectangle(17, 19, 6, 13);
        pixmap.setColor(new Color(0.41f, 0.10f, 0.14f, 1f));
        pixmap.drawRectangle(14, 16, 21, 22);
        return texture(pixmap);
    }

    static Texture weaponPickup() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0f));
        pixmap.fill();
        pixmap.setColor(new Color(0.72f, 0.77f, 0.76f, 1f));
        pixmap.drawLine(13, 35, 34, 11);
        pixmap.drawLine(15, 36, 36, 12);
        pixmap.setColor(new Color(0.47f, 0.28f, 0.16f, 1f));
        pixmap.fillRectangle(10, 30, 8, 5);
        pixmap.setColor(new Color(0.84f, 0.65f, 0.31f, 1f));
        pixmap.drawLine(8, 29, 18, 38);
        return texture(pixmap);
    }

    static Texture torch() {
        Pixmap pixmap = new Pixmap(TILE_SIZE, TILE_SIZE, Format.RGBA8888);
        pixmap.setColor(new Color(0f, 0f, 0f, 0f));
        pixmap.fill();
        pixmap.setColor(new Color(0.37f, 0.22f, 0.14f, 1f));
        pixmap.fillRectangle(21, 21, 7, 21);
        pixmap.setColor(new Color(0.85f, 0.35f, 0.10f, 1f));
        pixmap.fillCircle(24, 17, 8);
        pixmap.setColor(new Color(1f, 0.76f, 0.28f, 1f));
        pixmap.fillCircle(24, 17, 4);
        pixmap.setColor(new Color(1f, 0.93f, 0.57f, 1f));
        pixmap.drawPixel(24, 13);
        return texture(pixmap);
    }

    static Texture orcSheet() {
        Pixmap pixmap = new Pixmap(SPRITE_SIZE * 4, SPRITE_SIZE, Format.RGBA8888);
        for (int frame = 0; frame < 4; frame++) {
            drawOrc(pixmap, frame * SPRITE_SIZE, frame);
        }
        return texture(pixmap);
    }

    static Texture knightSheet() {
        Pixmap pixmap = new Pixmap(SPRITE_SIZE, SPRITE_SIZE, Format.RGBA8888);
        drawKnight(pixmap, 0);
        return texture(pixmap);
    }

    static Texture archer() {
        Pixmap pixmap = new Pixmap(SPRITE_SIZE, SPRITE_SIZE, Format.RGBA8888);
        drawKnight(pixmap, 0);
        color(pixmap, 0.18f, 0.31f, 0.20f);
        pixmap.fillRectangle(13, 21, 26, 14);
        color(pixmap, 0.37f, 0.51f, 0.31f);
        pixmap.fillRectangle(16, 22, 19, 7);
        color(pixmap, 0.46f, 0.28f, 0.15f);
        pixmap.drawLine(37, 12, 42, 24);
        pixmap.drawLine(42, 24, 37, 36);
        pixmap.drawLine(39, 17, 39, 32);
        color(pixmap, 0.75f, 0.68f, 0.47f);
        pixmap.drawLine(38, 23, 46, 15);
        return texture(pixmap);
    }

    static Texture mage() {
        Pixmap pixmap = new Pixmap(SPRITE_SIZE, SPRITE_SIZE, Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        color(pixmap, 0.20f, 0.13f, 0.31f);
        pixmap.fillRectangle(10, 12, 29, 27);
        pixmap.fillTriangle(8, 15, 24, 4, 40, 15);
        color(pixmap, 0.39f, 0.25f, 0.54f);
        pixmap.fillRectangle(14, 14, 20, 23);
        color(pixmap, 0.48f, 0.32f, 0.65f);
        pixmap.fillRectangle(16, 17, 16, 8);
        color(pixmap, 0.38f, 0.55f, 0.32f);
        pixmap.fillRectangle(16, 11, 17, 13);
        pixmap.fillRectangle(12, 15, 5, 9);
        pixmap.fillRectangle(32, 15, 5, 9);
        color(pixmap, 0.95f, 0.74f, 0.39f);
        pixmap.fillRectangle(18, 15, 3, 3);
        pixmap.fillRectangle(28, 15, 3, 3);
        color(pixmap, 0.76f, 0.55f, 0.88f);
        pixmap.fillCircle(40, 12, 6);
        color(pixmap, 0.91f, 0.82f, 0.98f);
        pixmap.fillCircle(40, 12, 3);
        color(pixmap, 0.47f, 0.29f, 0.17f);
        pixmap.drawLine(39, 18, 34, 42);
        return texture(pixmap);
    }

    static Texture boss() {
        Pixmap pixmap = new Pixmap(SPRITE_SIZE, SPRITE_SIZE, Format.RGBA8888);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fill();
        color(pixmap, 0.12f, 0.12f, 0.14f);
        pixmap.fillRectangle(5, 38, 39, 6);
        pixmap.fillRectangle(7, 19, 36, 21);
        color(pixmap, 0.29f, 0.28f, 0.29f);
        pixmap.fillRectangle(10, 17, 30, 21);
        color(pixmap, 0.48f, 0.43f, 0.39f);
        pixmap.fillRectangle(12, 19, 26, 14);
        color(pixmap, 0.35f, 0.12f, 0.13f);
        pixmap.fillTriangle(12, 13, 18, 2, 23, 16);
        pixmap.fillTriangle(26, 16, 34, 2, 39, 13);
        color(pixmap, 0.55f, 0.50f, 0.46f);
        pixmap.fillRectangle(14, 10, 22, 18);
        color(pixmap, 0.27f, 0.24f, 0.23f);
        pixmap.fillRectangle(13, 18, 24, 6);
        color(pixmap, 0.96f, 0.18f, 0.10f);
        pixmap.fillRectangle(17, 16, 5, 4);
        pixmap.fillRectangle(29, 16, 5, 4);
        color(pixmap, 0.85f, 0.77f, 0.63f);
        pixmap.fillTriangle(19, 23, 22, 23, 20, 29);
        pixmap.fillTriangle(29, 23, 32, 23, 30, 29);
        color(pixmap, 0.56f, 0.13f, 0.12f);
        pixmap.fillRectangle(11, 31, 27, 4);
        return texture(pixmap);
    }

    private static void drawOrc(Pixmap p, int x, int frame) {
        clearFrame(p, x);
        color(p, 0.12f, 0.11f, 0.10f); p.fillRectangle(x + 8, 40, 31, 4);
        color(p, 0.27f, 0.19f, 0.13f);
        p.fillRectangle(x + 14, 34, 8, frame == 1 ? 9 : 7);
        p.fillRectangle(x + 28, 34, 8, frame == 2 ? 9 : 7);
        color(p, 0.27f, 0.39f, 0.22f);
        p.fillRectangle(x + 12, 22, 26, 15);
        color(p, 0.44f, 0.52f, 0.29f);
        p.fillRectangle(x + 15, 24, 19, 9);
        color(p, 0.31f, 0.41f, 0.23f);
        p.fillRectangle(x + 7, 23, 8, 13);
        p.fillRectangle(x + 35, 23, 7, 12);

        color(p, 0.34f, 0.54f, 0.28f);
        p.fillRectangle(x + 12, 7, 28, 19);
        p.fillRectangle(x + 9, 11, 33, 11);
        color(p, 0.44f, 0.64f, 0.34f);
        p.fillRectangle(x + 15, 12, 22, 10);
        color(p, 0.23f, 0.40f, 0.22f);
        p.fillRectangle(x + 13, 11, 25, 4);
        color(p, 0.80f, 0.74f, 0.47f);
        p.fillRectangle(x + 19, 16, 4, 3);
        p.fillRectangle(x + 31, 16, 4, 3);
        color(p, 0.12f, 0.13f, 0.10f);
        p.drawPixel(x + 21, 17);
        p.drawPixel(x + 33, 17);
        color(p, 0.22f, 0.34f, 0.20f);
        p.fillRectangle(x + 20, 21, 12, 5);
        color(p, 0.89f, 0.83f, 0.66f);
        p.fillRectangle(x + 21, 22, 3, 6);
        p.fillRectangle(x + 28, 22, 3, 6);

        if (frame == 3) {
            color(p, 0.33f, 0.44f, 0.25f);
            p.fillRectangle(x + 34, 22, 8, 6);
            color(p, 0.72f, 0.73f, 0.69f);
            p.drawLine(x + 38, 23, x + 45, 14);
            p.drawLine(x + 40, 24, x + 46, 16);
            color(p, 0.63f, 0.35f, 0.18f);
            p.fillRectangle(x + 34, 25, 6, 3);
        } else {
            color(p, 0.48f, 0.29f, 0.16f);
            p.fillRectangle(x + 38, 24, 5, 8);
        }
    }

    private static void drawKnight(Pixmap p, int x) {
        clearFrame(p, x);
        color(p, 0.12f, 0.11f, 0.10f); p.fillRectangle(x + 8, 40, 31, 4);
        color(p, 0.19f, 0.20f, 0.23f);
        p.fillRectangle(x + 13, 34, 9, 8);
        p.fillRectangle(x + 28, 34, 9, 8);
        color(p, 0.38f, 0.39f, 0.41f);
        p.fillRectangle(x + 11, 20, 29, 16);
        color(p, 0.64f, 0.64f, 0.60f);
        p.fillRectangle(x + 15, 22, 21, 10);
        color(p, 0.73f, 0.67f, 0.48f);
        p.drawLine(x + 25, 22, x + 25, 33);
        p.drawLine(x + 17, 27, x + 34, 27);
        color(p, 0.31f, 0.32f, 0.35f);
        p.fillRectangle(x + 7, 21, 7, 13);
        p.fillRectangle(x + 37, 21, 6, 12);
        color(p, 0.55f, 0.56f, 0.55f);
        p.fillRectangle(x + 13, 7, 25, 17);
        color(p, 0.75f, 0.75f, 0.70f);
        p.fillRectangle(x + 17, 10, 17, 9);
        color(p, 0.20f, 0.21f, 0.22f);
        p.fillRectangle(x + 15, 16, 21, 5);
        color(p, 0.86f, 0.82f, 0.69f);
        p.drawLine(x + 18, 17, x + 33, 17);
        color(p, 0.55f, 0.16f, 0.13f);
        p.fillRectangle(x + 20, 6, 12, 3);
        color(p, 0.26f, 0.36f, 0.52f);
        p.fillRectangle(x + 3, 22, 9, 15);
        color(p, 0.75f, 0.62f, 0.32f);
        p.drawRectangle(x + 3, 22, 9, 15);
        p.drawLine(x + 7, 25, x + 7, 34);
        p.drawLine(x + 5, 29, x + 10, 29);
        color(p, 0.77f, 0.80f, 0.80f);
        p.drawLine(x + 39, 28, x + 45, 11);
        color(p, 0.52f, 0.34f, 0.20f);
        p.fillRectangle(x + 37, 28, 5, 4);
    }

    private static void clearFrame(Pixmap pixmap, int x) {
        pixmap.setBlending(Pixmap.Blending.None);
        pixmap.setColor(0, 0, 0, 0);
        pixmap.fillRectangle(x, 0, SPRITE_SIZE, SPRITE_SIZE);
        pixmap.setBlending(Pixmap.Blending.SourceOver);
    }

    private static void color(Pixmap pixmap, float r, float g, float b) {
        pixmap.setColor(r, g, b, 1f);
    }

    private static Texture texture(Pixmap pixmap) {
        Texture texture = new Texture(pixmap);
        texture.setFilter(Texture.TextureFilter.Nearest, Texture.TextureFilter.Nearest);
        pixmap.dispose();
        return texture;
    }
}
