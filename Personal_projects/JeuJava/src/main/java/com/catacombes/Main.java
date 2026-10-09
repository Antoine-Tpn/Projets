package com.catacombes;

import com.badlogic.gdx.Game;

public class Main extends Game {
    private GameAssets assets;

    @Override
    public void create() {
        assets = new GameAssets();
        setScreen(new Menu(this, assets));
    }

    void startGame() {
        setScreen(new GameScreen(this, assets));
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assets != null) {
            assets.dispose();
        }
        Fonts.dispose();
    }
}
