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
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OrthographicCamera camera = new OrthographicCamera();
    private float time;

    Menu(Main main, GameAssets assets) {
        this.main = main;
        this.assets = assets;
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) {
                    Menu.this.main.startGame();
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                float x = screenX;
                float y = Gdx.graphics.getHeight() - screenY;
                float centerX = Gdx.graphics.getWidth() / 2f;
                return y > 130 && y < 210 && x > centerX - 125 && x < centerX + 125
                        && start();
            }
        });
    }

    private boolean start() {
        main.startGame();
        return true;
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
        shapes.setColor(0.91f, 0.53f, 0.19f, 1f);
        shapes.circle(width * 0.22f, height * 0.68f, 8);
        shapes.circle(width * 0.78f, height * 0.68f, 8);
        shapes.setColor(0.24f, 0.56f, 0.24f, 1f);
        shapes.rect(centerX - 125, 130, 250, 74);
        shapes.end();

        batch.begin();
        float characterY = height * 0.37f;
        batch.draw(assets.orcIdle, width * 0.22f - 38, characterY, 76, 76);
        batch.draw(assets.knight, width * 0.78f - 38, characterY, 76, 76);
        batch.setColor(Color.WHITE);
        assetsFont().draw(batch, "LES CATACOMBES", centerX - 220, height - 105);
        assetsFont().draw(batch, "20 niveaux : chevaliers, archers, mages et boss.", centerX - 225, height - 145);
        assetsFont().draw(batch, "JOUER", centerX - 32, 174);
        assetsFont().draw(batch, "Entree / Espace pour commencer", centerX - 160, 95);
        batch.end();
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
