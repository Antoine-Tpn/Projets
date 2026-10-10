package com.catacombes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;

public final class Menu extends ScreenAdapter {
    private final Main main;
    private final GameAssets assets;
    private final GameAudio audio;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OrthographicCamera camera = new OrthographicCamera();
    private float time;
    private int selectedGame;

    Menu(Main main, GameAssets assets, GameAudio audio) {
        this.main = main;
        this.assets = assets;
        this.audio = audio;
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.LEFT || keycode == Input.Keys.UP) {
                    selectedGame = 0;
                    return true;
                }
                if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.DOWN) {
                    selectedGame = 1;
                    return true;
                }
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    return launchSelectedGame();
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float x = screenX;
                float y = Gdx.graphics.getHeight() - screenY;
                float cardY = Gdx.graphics.getHeight() * 0.34f;
                float cardHeight = 210;
                float cardWidth = Math.min(360, (Gdx.graphics.getWidth() - 70) / 2f);
                float gap = 28;
                float centerX = Gdx.graphics.getWidth() / 2f;
                float firstX = centerX - gap / 2f - cardWidth;
                float secondX = centerX + gap / 2f;
                if (inside(x, y, firstX, cardY, cardWidth, cardHeight)) {
                    selectedGame = 0;
                    return launchSelectedGame();
                }
                if (inside(x, y, secondX, cardY, cardWidth, cardHeight)) {
                    selectedGame = 1;
                    return launchSelectedGame();
                }
                return false;
            }
        });
    }

    private boolean launchSelectedGame() {
        if (selectedGame == 0) {
            main.startGame();
        } else {
            main.startZombieGame();
        }
        return true;
    }

    @Override
    public void show() {
        audio.stopAmbience();
    }

    private static boolean inside(float x, float y, float left, float bottom, float width, float height) {
        return x >= left && x <= left + width && y >= bottom && y <= bottom + height;
    }

    @Override
    public void render(float delta) {
        time += delta;
        ScreenUtils.clear(0.045f, 0.055f, 0.07f, 1f);
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float centerX = width / 2f;
        float cardY = height * 0.34f;
        float cardHeight = 210;
        float cardWidth = Math.min(360, (width - 70) / 2f);
        float gap = 28;
        float firstX = centerX - gap / 2f - cardWidth;
        float secondX = centerX + gap / 2f;

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.12f, 0.13f, 0.14f, 1f);
        shapes.rect(0, 0, width, height);
        shapes.setColor(0.17f, 0.16f, 0.15f, 1f);
        for (int x = -30; x < width + 60; x += 92) {
            shapes.rect(x, 0, 3, height);
        }
        shapes.setColor(0.31f, 0.24f, 0.18f, 1f);
        shapes.rect(0, 75, width, 7);
        shapes.rect(0, height - 82, width, 7);
        shapes.setColor(0.37f, 0.18f, 0.08f, 0.35f);
        shapes.circle(width * 0.22f, height * 0.66f, 66 + (float) Math.sin(time * 3) * 7);
        shapes.circle(width * 0.78f, height * 0.66f, 66 + (float) Math.sin(time * 3 + 1) * 7);
        drawCard(firstX, cardY, cardWidth, cardHeight, selectedGame == 0, 0.34f, 0.52f, 0.28f);
        drawCard(secondX, cardY, cardWidth, cardHeight, selectedGame == 1, 0.34f, 0.22f, 0.18f);
        shapes.end();

        batch.begin();
        float characterY = cardY + 104;
        batch.draw(assets.orcIdle, firstX + cardWidth / 2f - 38, characterY, 76, 76);
        batch.draw(assets.knight, secondX + cardWidth / 2f - 38, characterY, 76, 76);
        batch.setColor(Color.WHITE);
        assetsFont().draw(batch, "CHOISIS TON AVENTURE", centerX - 155, height - 105);
        assetsFont().draw(batch, "LES CATACOMBES", firstX + cardWidth / 2f - 63, cardY + 75);
        assetsFont().draw(batch, "20 niveaux d'exploration et de combat", firstX + cardWidth / 2f - 110, cardY + 48);
        assetsFont().draw(batch, "JOUER", firstX + cardWidth / 2f - 20, cardY + 18);
        assetsFont().draw(batch, "JEU ZOMBIE", secondX + cardWidth / 2f - 45, cardY + 75);
        assetsFont().draw(batch, "Civils, zombies et petite ville", secondX + cardWidth / 2f - 102, cardY + 48);
        assetsFont().draw(batch, "JOUER", secondX + cardWidth / 2f - 20, cardY + 18);
        assetsFont().draw(batch, selectedGame == 1
                ? "Les zombies reperent et poursuivent les civils."
                : "Fleches pour choisir, Entree ou Espace pour jouer.",
                centerX - (selectedGame == 1 ? 145 : 164), 100);
        batch.end();
    }

    private void drawCard(float x, float y, float width, float height, boolean selected,
                          float red, float green, float blue) {
        shapes.setColor(selected ? red * 0.64f : 0.10f, selected ? green * 0.64f : 0.11f,
                selected ? blue * 0.64f : 0.12f, 1f);
        shapes.rect(x, y, width, height);
        shapes.setColor(selected ? red : 0.35f, selected ? green : 0.35f,
                selected ? blue : 0.35f, 1f);
        shapes.rect(x, y, width, 4);
        shapes.rect(x, y + height - 4, width, 4);
        shapes.rect(x, y, 4, height);
        shapes.rect(x + width - 4, y, 4, height);
    }

    private com.badlogic.gdx.graphics.g2d.BitmapFont assetsFont() {
        return Fonts.get();
    }

    @Override
    public void resize(int width, int height) {
        camera.setToOrtho(false, width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
    }
}
