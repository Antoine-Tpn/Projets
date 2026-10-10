package com.catacombes;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;

public class Main extends Game {
    private GameAssets assets;
    private GameAudio audio;

    @Override
    public void create() {
        assets = new GameAssets();
        audio = new GameAudio();
        showMenu();
    }

    void startGame() {
        switchScreen(new GameScreen(this, assets, audio));
    }

    void startZombieGame() {
        switchScreen(new ZombieScreen(this, audio));
    }

    void showMenu() {
        switchScreen(new Menu(this, assets, audio));
    }

    private void switchScreen(Screen nextScreen) {
        Screen previousScreen = getScreen();
        setScreen(nextScreen);
        if (previousScreen != null) {
            previousScreen.dispose();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (assets != null) {
            assets.dispose();
        }
        if (audio != null) {
            audio.dispose();
        }
        Fonts.dispose();
    }
}
