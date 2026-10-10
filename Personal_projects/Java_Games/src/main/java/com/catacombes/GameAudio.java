package com.catacombes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

final class GameAudio {
    private final Music ambience = Gdx.audio.newMusic(Gdx.files.internal("audio/catacombs_theme.wav"));
    private final Music apocalypse = Gdx.audio.newMusic(Gdx.files.internal("audio/apocalypse_theme.wav"));
    private final Sound footstep = Gdx.audio.newSound(Gdx.files.internal("audio/footstep.wav"));
    private final Sound attack = Gdx.audio.newSound(Gdx.files.internal("audio/attack.wav"));
    private final Sound hit = Gdx.audio.newSound(Gdx.files.internal("audio/hit.wav"));
    private final Sound hurt = Gdx.audio.newSound(Gdx.files.internal("audio/hurt.wav"));
    private final Sound pickup = Gdx.audio.newSound(Gdx.files.internal("audio/pickup.wav"));

    GameAudio() {
        ambience.setLooping(true);
        ambience.setVolume(0.24f);
        apocalypse.setLooping(true);
        apocalypse.setVolume(0.30f);
    }

    void startAmbience() {
        if (!ambience.isPlaying()) ambience.play();
    }

    void stopAmbience() {
        if (ambience.isPlaying()) ambience.stop();
    }

    void startApocalypse() {
        stopAmbience();
        if (!apocalypse.isPlaying()) apocalypse.play();
    }

    void stopApocalypse() {
        if (apocalypse.isPlaying()) apocalypse.stop();
    }

    void playFootstep() {
        footstep.play(0.28f);
    }

    void playAttack() {
        attack.play(0.55f);
    }

    void playHit() {
        hit.play(0.6f);
    }

    void playHurt() {
        hurt.play(0.55f);
    }

    void playPickup() {
        pickup.play(0.45f);
    }

    void dispose() {
        stopAmbience();
        stopApocalypse();
        ambience.dispose();
        apocalypse.dispose();
        footstep.dispose();
        attack.dispose();
        hit.dispose();
        hurt.dispose();
        pickup.dispose();
    }
}
