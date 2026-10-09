package com.catacombes;

public final class Enemy {
    public enum Type {
        KNIGHT('e', "Chevalier", 2, 1, 6, 1, 1.0f),
        ARCHER('a', "Archer", 3, 4, 8, 1, 1.35f),
        MAGE('m', "Mage", 4, 5, 8, 2, 1.65f),
        BOSS('b', "Boss", 9, 1, 7, 2, 1.15f);

        public final char mapSymbol;
        public final String label;
        public final int baseHealth;
        public final int range;
        public final int visionRange;
        public final int damage;
        public final float attackInterval;

        Type(char mapSymbol, String label, int baseHealth, int range, int visionRange, int damage,
                float attackInterval) {
            this.mapSymbol = mapSymbol;
            this.label = label;
            this.baseHealth = baseHealth;
            this.range = range;
            this.visionRange = visionRange;
            this.damage = damage;
            this.attackInterval = attackInterval;
        }

        static Type fromMapSymbol(char symbol) {
            for (Type type : values()) {
                if (type.mapSymbol == symbol) return type;
            }
            throw new IllegalArgumentException("Symbole d'ennemi inconnu : " + symbol);
        }
    }

    public int x;
    public int y;
    public int previousX;
    public int previousY;
    public int health;
    public final int maxHealth;
    public final Type type;
    public float moveProgress = 1f;
    public float attackCooldown;
    public float attackFlash;

    public Enemy(int x, int y, Type type, int level) {
        this.x = x;
        this.y = y;
        this.previousX = x;
        this.previousY = y;
        this.type = type;
        this.maxHealth = type.baseHealth + (type == Type.BOSS ? level / 4 : level / 6);
        this.health = maxHealth;
    }

    public float renderX() {
        return previousX + (x - previousX) * moveProgress;
    }

    public float renderY() {
        return previousY + (y - previousY) * moveProgress;
    }
}
