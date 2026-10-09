package com.catacombes;

import com.badlogic.gdx.graphics.Color;

public final class Weapon {
    public static final Weapon[] ALL = {
        new Weapon("Epee rouillee", 1, 1, new Color(0.78f, 0.76f, 0.68f, 1f)),
        new Weapon("Lame d'acier", 2, 1, new Color(0.46f, 0.82f, 0.91f, 1f)),
        new Weapon("Hache de guerre", 2, 2, new Color(0.98f, 0.69f, 0.34f, 1f))
    };

    public final String name;
    public final int range;
    public final int damage;
    public final Color color;

    private Weapon(String name, int range, int damage, Color color) {
        this.name = name;
        this.range = range;
        this.damage = damage;
        this.color = color;
    }
}
