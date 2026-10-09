package com.catacombes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;

public final class GameScreen extends ScreenAdapter {
    private static final int TILE = 48;
    private static final int MAP_WIDTH = 15;
    private static final int MAP_HEIGHT = 11;
    private static final int[][] DIRECTIONS = {{0, -1}, {0, 1}, {-1, 0}, {1, 0}};
    private static final float PLAYER_STEP_TIME = 0.20f;

    private final Main main;
    private final GameAssets assets;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final char[][] map = new char[MAP_HEIGHT][MAP_WIDTH];
    private final List<Enemy> enemies = new ArrayList<>();
    private final boolean[] unlockedWeapons = {true, false, false};
    private final boolean[] heldDirections = new boolean[4];

    private int level;
    private int playerX;
    private int playerY;
    private int facingX = 1;
    private int facingY;
    private int health = 8;
    private int weaponIndex;
    private float elapsed;
    private float moveTimer;
    private float enemyTimer;
    private float attackTimer;
    private float attackCooldown;
    private float messageTimer;
    private String message = "Trouve la sortie. Les chevaliers te traquent.";
    private boolean moving;
    private boolean gameOver;
    private boolean victory;

    GameScreen(Main main, GameAssets assets) {
        this.main = main;
        this.assets = assets;
        loadLevel(0);
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                int direction = directionFor(keycode);
                if (direction >= 0) {
                    heldDirections[direction] = true;
                    facingX = DIRECTIONS[direction][0];
                    facingY = -DIRECTIONS[direction][1];
                } else if (keycode == Input.Keys.SPACE) {
                    if (gameOver || victory) restart();
                    else attack();
                } else if (keycode == Input.Keys.NUM_1) {
                    selectWeapon(0);
                } else if (keycode == Input.Keys.NUM_2) {
                    selectWeapon(1);
                } else if (keycode == Input.Keys.NUM_3) {
                    selectWeapon(2);
                } else if (keycode == Input.Keys.R) {
                    restart();
                } else if (keycode == Input.Keys.ESCAPE) {
                    main.setScreen(new Menu(main, assets));
                } else {
                    return false;
                }
                return true;
            }

            @Override
            public boolean keyUp(int keycode) {
                int direction = directionFor(keycode);
                if (direction < 0) return false;
                heldDirections[direction] = false;
                return true;
            }
        });
    }

    private int directionFor(int keycode) {
        if (keycode == Input.Keys.UP || keycode == Input.Keys.W || keycode == Input.Keys.Z) return 0;
        if (keycode == Input.Keys.DOWN || keycode == Input.Keys.S) return 1;
        if (keycode == Input.Keys.LEFT || keycode == Input.Keys.A || keycode == Input.Keys.Q) return 2;
        if (keycode == Input.Keys.RIGHT || keycode == Input.Keys.D) return 3;
        return -1;
    }

    @Override
    public void render(float delta) {
        float step = Math.min(delta, 0.05f);
        elapsed += step;
        if (!gameOver && !victory) updateGame(step);
        renderScene();
    }

    private void updateGame(float delta) {
        if (attackTimer > 0) attackTimer = Math.max(0, attackTimer - delta);
        if (attackCooldown > 0) attackCooldown -= delta;
        if (messageTimer > 0) messageTimer -= delta;
        moveTimer += delta;

        int direction = heldDirection();
        moving = direction >= 0;
        if (direction >= 0 && moveTimer >= PLAYER_STEP_TIME) {
            moveTimer = 0;
            int dx = DIRECTIONS[direction][0];
            int dy = DIRECTIONS[direction][1];
            facingX = dx;
            facingY = -dy;
            int nextX = playerX + dx;
            int nextY = playerY + dy;
            if (walkable(nextX, nextY) && enemyAt(nextX, nextY) == null) {
                playerX = nextX;
                playerY = nextY;
                collectTile();
            }
        }

        enemyTimer += delta;
        float enemyStepTime = Math.max(0.40f, (0.72f - Math.min(level, 6) * 0.04f) * 1.5f);
        if (enemyTimer >= enemyStepTime) {
            enemyTimer = 0;
            moveEnemies(delta);
        } else {
            for (Enemy enemy : enemies) {
                enemy.attackCooldown = Math.max(0, enemy.attackCooldown - delta);
                enemy.attackFlash = Math.max(0, enemy.attackFlash - delta);
            }
        }
    }

    private int heldDirection() {
        for (int i = 0; i < heldDirections.length; i++) {
            if (heldDirections[i]) return i;
        }
        return -1;
    }

    private void attack() {
        if (attackCooldown > 0) return;
        attackTimer = 0.36f;
        attackCooldown = 0.30f;
        Weapon weapon = Weapon.ALL[weaponIndex];
        for (int distance = 1; distance <= weapon.range; distance++) {
            int x = playerX + facingX * distance;
            int y = playerY - facingY * distance;
            if (!inside(x, y) || map[y][x] == '#') break;
            Enemy enemy = enemyAt(x, y);
            if (enemy != null) {
                enemy.health -= weapon.damage;
                showMessage(enemy.health <= 0 ? enemy.type.label + " vaincu !" : "Coup porte !");
                if (enemy.health <= 0) enemies.remove(enemy);
                return;
            }
        }
    }

    private void moveEnemies(float delta) {
        for (Enemy enemy : new ArrayList<>(enemies)) {
            enemy.attackCooldown = Math.max(0, enemy.attackCooldown - delta);
            enemy.attackFlash = Math.max(0, enemy.attackFlash - delta);
            if (canAttackPlayer(enemy)) {
                if (enemy.attackCooldown <= 0) {
                    health -= enemy.type.damage;
                    enemy.attackCooldown = enemy.type.attackInterval;
                    enemy.attackFlash = 0.24f;
                    showMessage(enemy.type.label + " t'attaque !");
                    if (health <= 0) gameOver = true;
                }
                continue;
            }

            int[][] distances = distancesFromPlayer(enemy);
            int bestX = enemy.x;
            int bestY = enemy.y;
            int bestDistance = Integer.MAX_VALUE;
            for (int[] direction : DIRECTIONS) {
                int x = enemy.x + direction[0];
                int y = enemy.y + direction[1];
                if (!walkable(x, y) || enemyAt(x, y) != null) continue;
                int distance = distances[y][x];
                if (distance >= 0 && distance < bestDistance) {
                    bestDistance = distance;
                    bestX = x;
                    bestY = y;
                }
            }
            enemy.x = bestX;
            enemy.y = bestY;
        }
    }

    private boolean canAttackPlayer(Enemy enemy) {
        int distance = Math.abs(enemy.x - playerX) + Math.abs(enemy.y - playerY);
        return distance > 0 && distance <= enemy.type.range && hasLineOfSight(enemy.x, enemy.y, playerX, playerY);
    }

    private boolean hasLineOfSight(int startX, int startY, int endX, int endY) {
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        int stepX = startX < endX ? 1 : -1;
        int stepY = startY < endY ? 1 : -1;
        int error = dx - dy;
        int x = startX;
        int y = startY;
        while (x != endX || y != endY) {
            int twiceError = 2 * error;
            if (twiceError > -dy) {
                error -= dy;
                x += stepX;
            }
            if (twiceError < dx) {
                error += dx;
                y += stepY;
            }
            if (x == endX && y == endY) return true;
            if (!walkable(x, y)) return false;
        }
        return true;
    }

    private int[][] distancesFromPlayer(Enemy movingEnemy) {
        int[][] distances = new int[MAP_HEIGHT][MAP_WIDTH];
        for (int[] row : distances) Arrays.fill(row, -1);
        Deque<int[]> queue = new ArrayDeque<>();
        distances[playerY][playerX] = 0;
        queue.addLast(new int[] {playerX, playerY});
        while (!queue.isEmpty()) {
            int[] position = queue.removeFirst();
            for (int[] direction : DIRECTIONS) {
                int x = position[0] + direction[0];
                int y = position[1] + direction[1];
                if (!walkable(x, y) || distances[y][x] >= 0) continue;
                Enemy occupant = enemyAt(x, y);
                if (occupant != null && occupant != movingEnemy) continue;
                distances[y][x] = distances[position[1]][position[0]] + 1;
                queue.addLast(new int[] {x, y});
            }
        }
        return distances;
    }

    private void collectTile() {
        char tile = map[playerY][playerX];
        if (tile == 'h') {
            health = Math.min(8, health + 3);
            map[playerY][playerX] = '.';
            showMessage("Potion trouvee : +3 PV");
        } else if (tile >= '1' && tile <= '3') {
            weaponIndex = tile - '1';
            unlockedWeapons[weaponIndex] = true;
            map[playerY][playerX] = '.';
            showMessage(Weapon.ALL[weaponIndex].name + " trouvee !");
        } else if (tile == 'X') {
            if (level == LevelFactory.COUNT - 1) {
                victory = true;
            } else {
                level++;
                health = Math.min(8, health + 2);
                loadLevel(level);
                showMessage("Niveau " + (level + 1) + " : les catacombes s'enfoncent...");
            }
        }
    }

    private void selectWeapon(int index) {
        if (gameOver || victory) return;
        if (unlockedWeapons[index]) {
            weaponIndex = index;
            showMessage("Arme equipee : " + Weapon.ALL[index].name);
        } else {
            showMessage("Cette arme est encore verrouillee.");
        }
    }

    private void restart() {
        level = 0;
        health = 8;
        weaponIndex = 0;
        Arrays.fill(unlockedWeapons, false);
        unlockedWeapons[0] = true;
        gameOver = false;
        victory = false;
        loadLevel(0);
        showMessage("Nouvelle partie !");
    }

    private void loadLevel(int index) {
        enemies.clear();
        char[][] nextMap = LevelFactory.create(index);
        for (int y = 0; y < MAP_HEIGHT; y++) {
            for (int x = 0; x < MAP_WIDTH; x++) {
                char tile = nextMap[y][x];
                if (tile == 'P') {
                    playerX = x;
                    playerY = y;
                    tile = '.';
                } else if (tile == 'e' || tile == 'a' || tile == 'm' || tile == 'b') {
                    enemies.add(new Enemy(x, y, Enemy.Type.fromMapSymbol(tile), index));
                    tile = '.';
                }
                map[y][x] = tile;
            }
        }
        enemyTimer = 0;
        moveTimer = 0;
    }

    private void renderScene() {
        ScreenUtils.clear(0.035f, 0.04f, 0.05f, 1f);
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);

        float mapX = 22;
        float mapY = (Gdx.graphics.getHeight() - MAP_HEIGHT * TILE) / 2f;
        drawMap(mapX, mapY);
        drawCharacters(mapX, mapY);
        drawHud(mapX + MAP_WIDTH * TILE + 24, Gdx.graphics.getHeight() - 36);
        if (gameOver || victory) drawEndMessage();
    }

    private void drawMap(float mapX, float mapY) {
        batch.begin();
        for (int row = 0; row < MAP_HEIGHT; row++) {
            for (int column = 0; column < MAP_WIDTH; column++) {
                float x = mapX + column * TILE;
                float y = mapY + (MAP_HEIGHT - 1 - row) * TILE;
                char tile = map[row][column];
                batch.draw(assets.floor, x, y, TILE, TILE);
                if (tile == '#') {
                    batch.draw(assets.wall, x, y, TILE, TILE);
                } else if (tile == 'X') {
                    batch.draw(assets.exit, x, y, TILE, TILE);
                } else if (tile == 'h') {
                    batch.draw(assets.potion, x, y, TILE, TILE);
                } else if (tile >= '1' && tile <= '3') {
                    batch.draw(assets.weaponPickup, x, y, TILE, TILE);
                } else if (tile == 't') {
                    float flicker = 0.82f + 0.18f * (float) Math.sin(elapsed * 7 + row);
                    batch.setColor(1f, flicker, 0.76f, 1f);
                    batch.draw(assets.torch, x, y, TILE, TILE);
                    batch.setColor(Color.WHITE);
                }
            }
        }
        batch.end();
    }

    private void drawCharacters(float mapX, float mapY) {
        batch.begin();
        for (Enemy enemy : enemies) {
            float x = mapX + enemy.x * TILE;
            float y = mapY + (MAP_HEIGHT - 1 - enemy.y) * TILE;
            batch.draw(assets.enemyFrame(enemy.type), x, y, TILE, TILE);
        }

        float playerWorldX = mapX + playerX * TILE;
        float playerWorldY = mapY + (MAP_HEIGHT - 1 - playerY) * TILE;
        TextureRegion frame;
        if (attackTimer > 0) {
            frame = assets.orcAttack.getKeyFrame(Math.max(0, 0.24f - attackTimer), false);
        } else if (moving) {
            frame = assets.orcWalk.getKeyFrame(elapsed, true);
        } else {
            frame = assets.orcIdle;
        }
        float rotation = facingX == 1 ? 0 : facingX == -1 ? 180 : facingY == 1 ? 90 : 270;
        batch.draw(frame, playerWorldX, playerWorldY, TILE / 2f, TILE / 2f,
                TILE, TILE, 1, 1, rotation);
        batch.end();

        for (Enemy enemy : enemies) {
            if (enemy.health < enemy.maxHealth) {
                float x = mapX + enemy.x * TILE;
                float y = mapY + (MAP_HEIGHT - 1 - enemy.y) * TILE + TILE - 4;
                shapes.begin(ShapeRenderer.ShapeType.Filled);
                shapes.setColor(0.08f, 0.06f, 0.06f, 1f);
                shapes.rect(x + 5, y, TILE - 10, 3);
                shapes.setColor(enemy.type == Enemy.Type.BOSS ? 0.90f : 0.80f, 0.16f, 0.12f, 1f);
                shapes.rect(x + 5, y, (TILE - 10) * enemy.health / enemy.maxHealth, 3);
                shapes.end();
            }
        }

        for (Enemy enemy : enemies) {
            if (enemy.attackFlash > 0) {
                float enemyX = mapX + enemy.x * TILE + TILE / 2f;
                float enemyY = mapY + (MAP_HEIGHT - 1 - enemy.y) * TILE + TILE / 2f;
                shapes.begin(ShapeRenderer.ShapeType.Line);
                if (enemy.type == Enemy.Type.MAGE) shapes.setColor(0.72f, 0.42f, 1f, 1f);
                else if (enemy.type == Enemy.Type.ARCHER) shapes.setColor(0.96f, 0.75f, 0.34f, 1f);
                else shapes.setColor(0.95f, 0.28f, 0.20f, 1f);
                shapes.line(enemyX, enemyY, playerWorldX + TILE / 2f, playerWorldY + TILE / 2f);
                shapes.end();
            }
        }

        if (attackTimer > 0) {
            float centerX = playerWorldX + TILE / 2f + facingX * TILE * 0.72f;
            float centerY = playerWorldY + TILE / 2f + facingY * TILE * 0.72f;
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(1f, 0.78f, 0.35f, 1f);
            shapes.arc(centerX, centerY, 17, elapsed * 100 % 180, 120);
            shapes.end();
        }
    }

    private void drawHud(float x, float y) {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.08f, 0.075f, 0.07f, 1f);
        shapes.rect(x - 12, 70, Gdx.graphics.getWidth() - x - 12, Gdx.graphics.getHeight() - 140);
        shapes.setColor(0.24f, 0.18f, 0.13f, 1f);
        shapes.rect(x - 12, 70, 3, Gdx.graphics.getHeight() - 140);
        shapes.setColor(0.65f, 0.19f, 0.17f, 1f);
        for (int i = 0; i < 8; i++) {
            shapes.rect(x + (i % 4) * 25, y - 90 - (i / 4) * 20, 18,
                    i < health ? 12 : 4);
        }
        shapes.setColor(0.06f, 0.06f, 0.06f, 1f);
        for (int i = 0; i < Weapon.ALL.length; i++) {
            shapes.rect(x, y - 185 - i * 38, 220, 30);
            if (unlockedWeapons[i]) {
                Color c = Weapon.ALL[i].color;
                shapes.setColor(c.r, c.g, c.b, 1f);
                shapes.rect(x + 2, y - 183 - i * 38, 216, 26);
                shapes.setColor(0.06f, 0.06f, 0.06f, 1f);
            }
        }
        shapes.end();

        batch.begin();
        Fonts.get().setColor(0.92f, 0.88f, 0.78f, 1f);
        Fonts.get().getData().setScale(1.35f);
        Fonts.get().draw(batch, "CATACOMBES", x, y);
        Fonts.get().getData().setScale(1f);
        Fonts.get().draw(batch, "Niveau " + (level + 1) + " / " + LevelFactory.COUNT, x, y - 34);
        Fonts.get().draw(batch, "VIE : " + health + " / 8", x, y - 68);
        Fonts.get().draw(batch, "ARMES", x, y - 112);
        for (int i = 0; i < Weapon.ALL.length; i++) {
            String label = (i + 1) + "  " + (unlockedWeapons[i] ? Weapon.ALL[i].name : "Verrouillee");
            Fonts.get().setColor(unlockedWeapons[i] ? Color.BLACK : Color.LIGHT_GRAY);
            Fonts.get().draw(batch, label, x + 9, y - 176 - i * 38);
        }
        Fonts.get().setColor(Color.LIGHT_GRAY);
        Fonts.get().draw(batch, "Fleches / ZQSD : bouger", x, y - 330);
        Fonts.get().draw(batch, "Espace : attaquer", x, y - 353);
        Fonts.get().draw(batch, "R : recommencer", x, y - 376);
        Fonts.get().draw(batch, "Echap : menu", x, y - 399);
        Fonts.get().draw(batch, "Archer : tir a distance", x, y - 430);
        Fonts.get().draw(batch, "Mage : sort puissant", x, y - 451);
        Fonts.get().draw(batch, "Boss : lourd et resistant", x, y - 472);
        if (messageTimer > 0) {
            Fonts.get().setColor(0.93f, 0.72f, 0.37f, 1f);
            Fonts.get().draw(batch, message, x, y - 445);
        }
        Fonts.get().setColor(Color.WHITE);
        batch.end();
    }

    private void drawEndMessage() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.02f, 0.02f, 0.025f, 0.82f);
        shapes.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapes.end();
        batch.begin();
        Fonts.get().getData().setScale(2f);
        Fonts.get().setColor(victory ? Color.GOLD : Color.SCARLET);
        String title = victory ? "VICTOIRE !" : "L'ORC EST TOMBE";
        Fonts.get().draw(batch, title, Gdx.graphics.getWidth() / 2f - 110,
                Gdx.graphics.getHeight() / 2f + 15);
        Fonts.get().getData().setScale(1f);
        Fonts.get().setColor(Color.WHITE);
        Fonts.get().draw(batch, "Espace ou R pour recommencer", Gdx.graphics.getWidth() / 2f - 130,
                Gdx.graphics.getHeight() / 2f - 25);
        batch.end();
    }

    private void showMessage(String text) {
        message = text;
        messageTimer = 2.5f;
    }

    private Enemy enemyAt(int x, int y) {
        for (Enemy enemy : enemies) {
            if (enemy.x == x && enemy.y == y) return enemy;
        }
        return null;
    }

    private boolean inside(int x, int y) {
        return x >= 0 && x < MAP_WIDTH && y >= 0 && y < MAP_HEIGHT;
    }

    private boolean walkable(int x, int y) {
        return inside(x, y) && map[y][x] != '#';
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
