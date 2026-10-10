package com.catacombes;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

public final class Lwjgl3Launcher {
    private Lwjgl3Launcher() {
    }

    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setTitle("Java Games");
        configuration.setWindowedMode(1100, 640);
        configuration.setForegroundFPS(60);
        configuration.useVsync(true);
        new Lwjgl3Application(new Main(), configuration);
    }
}
