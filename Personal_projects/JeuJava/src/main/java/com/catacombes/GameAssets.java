package com.catacombes;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

final class GameAssets {
    final Texture floor = PixelArt.floor();
    final Texture wall = PixelArt.wall();
    final Texture exit = PixelArt.exit();
    final Texture potion = PixelArt.potion();
    final Texture weaponPickup = PixelArt.weaponPickup();
    final Texture torch = PixelArt.torch();
    final Texture archer = PixelArt.archer();
    final Texture mage = PixelArt.mage();
    final Texture boss = PixelArt.boss();

    private final Texture orcSheet = PixelArt.orcSheet();
    private final Texture knightSheet = PixelArt.knightSheet();
    private final TextureRegion archerFrame = new TextureRegion(archer);
    private final TextureRegion mageFrame = new TextureRegion(mage);
    private final TextureRegion bossFrame = new TextureRegion(boss);

    final Animation<TextureRegion> orcWalk = new Animation<>(
            0.16f, frames(orcSheet, 3));
    final Animation<TextureRegion> orcAttack = new Animation<>(
            0.12f, frames(orcSheet, 4)[3]);
    final TextureRegion orcIdle = frames(orcSheet, 4)[0];
    final TextureRegion knight = frames(knightSheet, 1)[0];

    TextureRegion enemyFrame(Enemy.Type type) {
        switch (type) {
            case ARCHER:
                return archerFrame;
            case MAGE:
                return mageFrame;
            case BOSS:
                return bossFrame;
            default:
                return knight;
        }
    }

    private static TextureRegion[] frames(Texture texture, int count) {
        TextureRegion[] frames = new TextureRegion[count];
        for (int i = 0; i < count; i++) {
            frames[i] = new TextureRegion(texture, i * PixelArt.SPRITE_SIZE, 0,
                    PixelArt.SPRITE_SIZE, PixelArt.SPRITE_SIZE);
        }
        return frames;
    }

    GameAssets() {
    }

    void dispose() {
        floor.dispose();
        wall.dispose();
        exit.dispose();
        potion.dispose();
        weaponPickup.dispose();
        torch.dispose();
        archer.dispose();
        mage.dispose();
        boss.dispose();
        orcSheet.dispose();
        knightSheet.dispose();
    }
}
