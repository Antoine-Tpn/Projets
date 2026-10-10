package com.catacombes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.ScreenUtils;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

final class ZombieScreen extends ScreenAdapter {
    private static final float CITY_WIDTH = 2200;
    private static final float CITY_HEIGHT = 1360;
    private static final float AGENT_RADIUS = 7;
    private static final float HUD_HEIGHT = 164;
    private static final int CIVILIAN_COUNT = 42;
    private static final int MIN_ZOMBIE_COUNT = 1;
    private static final int MAX_ZOMBIE_COUNT = 40;
    private static final float CITY_GRID_SIZE = 32;
    private static final float ZOMBIE_LEAP_DURATION = 0.28f;
    private static final float ZOMBIE_TRANSFORM_DURATION = 1f;
    private static final String[] CITY_NAMES = {"Centre-ville", "Quartier residentiel", "Zone industrielle"};
    private static final float[][] MAP_PREVIEW_BUILDINGS = {
            {0.13f, 0.16f, 0.30f, 0.24f}, {0.55f, 0.14f, 0.30f, 0.27f},
            {0.15f, 0.56f, 0.28f, 0.20f}, {0.55f, 0.55f, 0.30f, 0.25f}
    };

    private static final class Agent {
        boolean zombie;
        final float phase;
        float x;
        float y;
        float vx;
        float vy;
        float leapTimer;
        float transformedTimer;
        float conversionTimer;
        float wanderAngle;
        boolean selected;
        boolean hasDestination;
        float destinationX;
        float destinationY;
        Agent attacker;
        Agent attackTarget;
        final ArrayDeque<Vector2> route = new ArrayDeque<>();

        Agent(boolean zombie, float x, float y, float vx, float vy, float phase) {
            this.zombie = zombie;
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
            this.phase = phase;
            this.wanderAngle = phase;
        }
    }

    private static final class Building {
        final Rectangle bounds;
        final Color roof;

        Building(float x, float y, float width, float height, Color roof) {
            bounds = new Rectangle(x, y, width, height);
            this.roof = roof;
        }
    }

    private final Main main;
    private final GameAudio audio;
    private final SpriteBatch batch = new SpriteBatch();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OrthographicCamera camera = new OrthographicCamera();
    private final OrthographicCamera uiCamera = new OrthographicCamera();
    private final List<Agent> agents = new ArrayList<>();
    private final List<Building> buildings = new ArrayList<>();
    private final Random random = new Random(27);
    private float fitZoom = 1;
    private float zoom = 1;
    private float elapsed;
    private boolean cameraInitialized;
    private boolean cameraDragging;
    private boolean selecting;
    private boolean started;
    private boolean zombiesTogether = true;
    private int selectedMap;
    private int initialZombieCount = 8;
    private int transformedCount;
    private int lastDragX;
    private int lastDragY;
    private int selectionStartX;
    private int selectionStartY;
    private int selectionEndX;
    private int selectionEndY;
    private final Vector3 projectedAgent = new Vector3();

    ZombieScreen(Main main, GameAudio audio) {
        this.main = main;
        this.audio = audio;
        Gdx.input.setInputProcessor(new InputAdapter() {
            @Override
            public boolean keyDown(int keycode) {
                if (keycode == Input.Keys.ESCAPE) {
                    main.showMenu();
                    return true;
                }
                if (!started) {
                    if (keycode == Input.Keys.LEFT) selectedMap = (selectedMap + CITY_NAMES.length - 1) % CITY_NAMES.length;
                    else if (keycode == Input.Keys.RIGHT) selectedMap = (selectedMap + 1) % CITY_NAMES.length;
                    else if (keycode == Input.Keys.UP) initialZombieCount = Math.min(MAX_ZOMBIE_COUNT, initialZombieCount + 1);
                    else if (keycode == Input.Keys.DOWN) initialZombieCount = Math.max(MIN_ZOMBIE_COUNT, initialZombieCount - 1);
                    else if (keycode == Input.Keys.TAB) zombiesTogether = !zombiesTogether;
                    else if (keycode == Input.Keys.ENTER || keycode == Input.Keys.SPACE) startSimulation();
                    else return false;
                    return true;
                }
                if (keycode == Input.Keys.PLUS || keycode == Input.Keys.EQUALS
                        || keycode == Input.Keys.NUMPAD_ADD) {
                    zoomAtCenter(1.18f);
                    return true;
                }
                if (keycode == Input.Keys.MINUS || keycode == Input.Keys.NUMPAD_SUBTRACT) {
                    zoomAtCenter(0.85f);
                    return true;
                }
                return false;
            }

            @Override
            public boolean touchDown(int screenX, int screenY, int pointer, int button) {
                if (!started) {
                    if (button != Input.Buttons.LEFT) return false;
                    handleSetupClick(screenX, screenY);
                    return true;
                }
                if (button == Input.Buttons.RIGHT) {
                    if (screenY >= HUD_HEIGHT) issueMoveOrder(screenX, screenY);
                    return true;
                }
                if (screenY < HUD_HEIGHT) return false;
                if (button != Input.Buttons.LEFT && button != Input.Buttons.MIDDLE) return false;
                lastDragX = screenX;
                lastDragY = screenY;
                if (button == Input.Buttons.MIDDLE) {
                    cameraDragging = true;
                    return true;
                }
                selecting = true;
                selectionStartX = screenX;
                selectionStartY = screenY;
                selectionEndX = screenX;
                selectionEndY = screenY;
                return true;
            }

            @Override
            public boolean touchDragged(int screenX, int screenY, int pointer) {
                if (selecting) {
                    selectionEndX = MathUtils.clamp(screenX, 0, Gdx.graphics.getWidth());
                    selectionEndY = clampWorldScreenY(screenY);
                    return true;
                }
                if (!cameraDragging) return false;
                float worldPerPixelX = camera.viewportWidth * camera.zoom / Gdx.graphics.getWidth();
                float worldPerPixelY = camera.viewportHeight * camera.zoom / worldViewHeight();
                camera.position.x -= (screenX - lastDragX) * worldPerPixelX;
                camera.position.y += (screenY - lastDragY) * worldPerPixelY;
                lastDragX = screenX;
                lastDragY = screenY;
                clampCamera();
                return true;
            }

            @Override
            public boolean touchUp(int screenX, int screenY, int pointer, int button) {
                if (button == Input.Buttons.MIDDLE && cameraDragging) {
                    cameraDragging = false;
                    return true;
                }
                if (button == Input.Buttons.LEFT && selecting) {
                    selectionEndX = MathUtils.clamp(screenX, 0, Gdx.graphics.getWidth());
                    selectionEndY = clampWorldScreenY(screenY);
                    selectCivilians();
                    selecting = false;
                    return true;
                }
                return false;
            }

            @Override
            public boolean scrolled(float amountX, float amountY) {
                zoomAtCenter(amountY < 0 ? 1.14f : 0.88f);
                return true;
            }
        });
        updateCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    @Override
    public void show() {
        audio.startApocalypse();
    }

    private void createCity() {
        buildings.clear();
        Color[] roofColors = {
                new Color(0.45f, 0.32f, 0.26f, 1f),
                new Color(0.47f, 0.43f, 0.34f, 1f),
                new Color(0.36f, 0.39f, 0.39f, 1f),
                new Color(0.50f, 0.37f, 0.30f, 1f),
                new Color(0.39f, 0.36f, 0.32f, 1f)
        };
        addBuilding(190, 190, 190, 145, roofColors[0]);
        addBuilding(425, 190, 190, 145, roofColors[1]);
        addBuilding(190, 370, 155, 88, roofColors[2]);
        addBuilding(380, 370, 235, 88, roofColors[3]);

        addBuilding(825, 190, 205, 155, roofColors[1]);
        addBuilding(1080, 190, 270, 155, roofColors[4]);
        addBuilding(825, 355, 135, 105, roofColors[2]);
        addBuilding(1005, 355, 160, 105, roofColors[0]);
        addBuilding(1210, 355, 140, 105, roofColors[3]);
        addBuilding(825, 630, 250, 150, roofColors[3]);
        addBuilding(1120, 630, 230, 150, roofColors[0]);
        addBuilding(825, 825, 170, 135, roofColors[4]);
        addBuilding(1040, 825, 310, 135, roofColors[1]);

        addBuilding(1535, 190, 210, 145, roofColors[0]);
        addBuilding(1795, 190, 225, 145, roofColors[2]);
        addBuilding(1535, 350, 125, 113, roofColors[1]);
        addBuilding(1705, 350, 315, 113, roofColors[4]);
        addBuilding(1535, 630, 210, 155, roofColors[2]);
        addBuilding(1795, 630, 225, 155, roofColors[3]);
        addBuilding(1535, 835, 155, 125, roofColors[4]);
        addBuilding(1740, 835, 280, 125, roofColors[0]);

        addBuilding(200, 1130, 175, 68, roofColors[2]);
        addBuilding(410, 1130, 205, 68, roofColors[4]);
        addBuilding(860, 1130, 205, 68, roofColors[0]);
        addBuilding(1110, 1130, 240, 68, roofColors[3]);
        addBuilding(1550, 1130, 200, 68, roofColors[1]);
        addBuilding(1800, 1130, 215, 68, roofColors[2]);
    }

    private void addBuilding(float x, float y, float width, float height, Color roof) {
        if (selectedMap == 1) {
            width *= 0.68f;
            height *= 0.68f;
            x += (1f - 0.68f) * width / (2f * 0.68f);
            y += (1f - 0.68f) * height / (2f * 0.68f);
            roof = new Color(0.43f, 0.43f, 0.36f, 1f);
        } else if (selectedMap == 2) {
            width *= 1.05f;
            x -= (1.05f - 1f) * width / (2f * 1.05f);
            roof = new Color(0.34f, 0.38f, 0.38f, 1f);
        }
        buildings.add(new Building(x, y, width, height, roof));
    }

    private void startSimulation() {
        started = true;
        createCity();
        populateCity();
        updateCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void handleSetupClick(int screenX, int screenY) {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float y = height - screenY;
        float cardWidth = Math.min(280, (width - 72) / 3f);
        float gap = 18;
        float totalWidth = cardWidth * 3 + gap * 2;
        float firstX = (width - totalWidth) / 2f;
        float cardY = height * 0.49f;
        for (int i = 0; i < CITY_NAMES.length; i++) {
            if (inside(screenX, y, firstX + i * (cardWidth + gap), cardY, cardWidth, 160)) {
                selectedMap = i;
                return;
            }
        }
        float controlsY = height * 0.31f;
        if (inside(screenX, y, width / 2f - 120, controlsY, 240, 50)) {
            zombiesTogether = !zombiesTogether;
        } else if (inside(screenX, y, width / 2f - 145, height * 0.20f, 290, 58)) {
            startSimulation();
        } else if (inside(screenX, y, width / 2f + 58, height * 0.39f, 46, 40)) {
            initialZombieCount = Math.min(MAX_ZOMBIE_COUNT, initialZombieCount + 1);
        } else if (inside(screenX, y, width / 2f - 104, height * 0.39f, 46, 40)) {
            initialZombieCount = Math.max(MIN_ZOMBIE_COUNT, initialZombieCount - 1);
        }
    }

    private static boolean inside(float x, float y, float left, float bottom, float width, float height) {
        return x >= left && x <= left + width && y >= bottom && y <= bottom + height;
    }

    private void selectCivilians() {
        float left = Math.min(selectionStartX, selectionEndX);
        float right = Math.max(selectionStartX, selectionEndX);
        float top = Math.min(selectionStartY, selectionEndY);
        float bottom = Math.max(selectionStartY, selectionEndY);
        if (right - left < 8) {
            left -= 8;
            right += 8;
        }
        if (bottom - top < 8) {
            top -= 8;
            bottom += 8;
        }
        for (Agent agent : agents) {
            agent.selected = false;
            if (agent.zombie || agent.attacker != null) continue;
            projectedAgent.set(agent.x, agent.y, 0);
            camera.project(projectedAgent, 0, 0, Gdx.graphics.getWidth(), worldViewHeight());
            float projectedScreenY = Gdx.graphics.getHeight() - projectedAgent.y;
            float screenRadius = AGENT_RADIUS * Gdx.graphics.getWidth()
                    / (camera.viewportWidth * camera.zoom);
            agent.selected = projectedAgent.x + screenRadius >= left
                    && projectedAgent.x - screenRadius <= right
                    && projectedScreenY + screenRadius >= top
                    && projectedScreenY - screenRadius <= bottom;
        }
    }

    private void issueMoveOrder(int screenX, int screenY) {
        Vector3 destination = screenToWorld(screenX, screenY);
        float destinationX = MathUtils.clamp(destination.x, AGENT_RADIUS, CITY_WIDTH - AGENT_RADIUS);
        float destinationY = MathUtils.clamp(destination.y, AGENT_RADIUS, CITY_HEIGHT - AGENT_RADIUS);
        if (insideBuilding(destinationX, destinationY, AGENT_RADIUS)) {
            int[] cell = nearestWalkableCell(destinationX, destinationY);
            destinationX = cellCenterX(cell[0]);
            destinationY = cellCenterY(cell[1]);
        }
        for (Agent agent : agents) {
            if (!agent.selected || agent.zombie || agent.attacker != null) continue;
            agent.destinationX = destinationX;
            agent.destinationY = destinationY;
            agent.hasDestination = true;
            agent.route.clear();
            agent.route.addAll(findRoute(agent.x, agent.y, destinationX, destinationY));
        }
    }

    private Vector3 screenToWorld(int screenX, int screenY) {
        float height = worldViewHeight();
        return camera.unproject(new Vector3(screenX, screenY, 0),
                0, 0, Gdx.graphics.getWidth(), height);
    }

    private int clampWorldScreenY(int screenY) {
        return MathUtils.clamp(screenY, (int) HUD_HEIGHT, Gdx.graphics.getHeight());
    }

    private float worldViewHeight() {
        return Math.max(1, Gdx.graphics.getHeight() - HUD_HEIGHT);
    }

    private List<Vector2> findRoute(float startX, float startY, float destinationX, float destinationY) {
        int columns = (int) Math.ceil(CITY_WIDTH / CITY_GRID_SIZE);
        int rows = (int) Math.ceil(CITY_HEIGHT / CITY_GRID_SIZE);
        int[] start = nearestWalkableCell(startX, startY);
        int[] goal = nearestWalkableCell(destinationX, destinationY);
        int startIndex = start[1] * columns + start[0];
        int goalIndex = goal[1] * columns + goal[0];
        int[] parents = new int[columns * rows];
        java.util.Arrays.fill(parents, -2);
        int[] queue = new int[columns * rows];
        int head = 0;
        int tail = 0;
        queue[tail++] = startIndex;
        parents[startIndex] = -1;
        int[] dxs = {0, 0, -1, 1};
        int[] dys = {-1, 1, 0, 0};
        while (head < tail && parents[goalIndex] == -2) {
            int current = queue[head++];
            int x = current % columns;
            int y = current / columns;
            for (int direction = 0; direction < dxs.length; direction++) {
                int nextX = x + dxs[direction];
                int nextY = y + dys[direction];
                if (nextX < 0 || nextX >= columns || nextY < 0 || nextY >= rows) continue;
                int next = nextY * columns + nextX;
                if (parents[next] != -2 || !walkableCell(nextX, nextY)) continue;
                parents[next] = current;
                queue[tail++] = next;
            }
        }
        List<Vector2> route = new ArrayList<>();
        if (parents[goalIndex] == -2) return route;
        List<Integer> reversePath = new ArrayList<>();
        for (int current = goalIndex; current != startIndex; current = parents[current]) {
            reversePath.add(current);
        }
        for (int i = reversePath.size() - 1; i >= 0; i--) {
            int cell = reversePath.get(i);
            route.add(new Vector2(cellCenterX(cell % columns), cellCenterY(cell / columns)));
        }
        if (!insideBuilding(destinationX, destinationY, AGENT_RADIUS)) {
            route.add(new Vector2(destinationX, destinationY));
        }
        return route;
    }

    private int[] nearestWalkableCell(float x, float y) {
        int columns = (int) Math.ceil(CITY_WIDTH / CITY_GRID_SIZE);
        int rows = (int) Math.ceil(CITY_HEIGHT / CITY_GRID_SIZE);
        int nearestX = 0;
        int nearestY = 0;
        float bestDistance = Float.MAX_VALUE;
        for (int cellY = 0; cellY < rows; cellY++) {
            for (int cellX = 0; cellX < columns; cellX++) {
                if (!walkableCell(cellX, cellY)) continue;
                float dx = cellCenterX(cellX) - x;
                float dy = cellCenterY(cellY) - y;
                float distance = dx * dx + dy * dy;
                if (distance < bestDistance) {
                    bestDistance = distance;
                    nearestX = cellX;
                    nearestY = cellY;
                }
            }
        }
        return new int[] {nearestX, nearestY};
    }

    private boolean walkableCell(int x, int y) {
        float centerX = cellCenterX(x);
        float centerY = cellCenterY(y);
        return centerX < CITY_WIDTH && centerY < CITY_HEIGHT
                && !insideBuilding(centerX, centerY, AGENT_RADIUS + 2);
    }

    private float cellCenterX(int x) {
        return Math.min(CITY_WIDTH - AGENT_RADIUS, (x + 0.5f) * CITY_GRID_SIZE);
    }

    private float cellCenterY(int y) {
        return Math.min(CITY_HEIGHT - AGENT_RADIUS, (y + 0.5f) * CITY_GRID_SIZE);
    }

    private void drawConfiguration() {
        float width = Gdx.graphics.getWidth();
        float height = Gdx.graphics.getHeight();
        float cardWidth = Math.min(280, (width - 72) / 3f);
        float gap = 18;
        float firstX = (width - (cardWidth * 3 + gap * 2)) / 2f;
        float cardY = height * 0.49f;
        uiCamera.setToOrtho(false, width, height);
        shapes.setProjectionMatrix(uiCamera.combined);
        batch.setProjectionMatrix(uiCamera.combined);

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.055f, 0.065f, 0.065f, 1f);
        shapes.rect(0, 0, width, height);
        shapes.setColor(0.16f, 0.19f, 0.17f, 1f);
        shapes.rect(0, height * 0.46f, width, 3);
        for (int i = 0; i < CITY_NAMES.length; i++) {
            float x = firstX + i * (cardWidth + gap);
            shapes.setColor(i == selectedMap ? 0.33f : 0.14f,
                    i == selectedMap ? 0.25f : 0.17f,
                    i == selectedMap ? 0.15f : 0.16f, 1f);
            shapes.rect(x, cardY, cardWidth, 160);
            shapes.setColor(i == selectedMap ? 0.87f : 0.31f,
                    i == selectedMap ? 0.57f : 0.34f,
                    i == selectedMap ? 0.23f : 0.31f, 1f);
            shapes.rect(x, cardY + 156, cardWidth, 4);
            drawMapPreview(x + 14, cardY + 44, cardWidth - 28, 98, i);
        }
        shapes.setColor(0.10f, 0.13f, 0.13f, 1f);
        shapes.rect(width / 2f - 120, height * 0.31f, 240, 50);
        shapes.setColor(0.18f, 0.22f, 0.20f, 1f);
        shapes.rect(width / 2f - 145, height * 0.20f, 290, 58);
        shapes.setColor(0.72f, 0.44f, 0.20f, 1f);
        shapes.rect(width / 2f - 145, height * 0.20f, 290, 4);
        float counterY = height * 0.39f;
        shapes.setColor(0.16f, 0.18f, 0.18f, 1f);
        shapes.rect(width / 2f - 104, counterY, 46, 40);
        shapes.rect(width / 2f + 58, counterY, 46, 40);
        shapes.end();

        batch.begin();
        Fonts.get().setColor(0.92f, 0.95f, 0.91f, 1f);
        Fonts.get().getData().setScale(1.65f);
        Fonts.get().draw(batch, "CONFIGURATION DE LA PARTIE", width / 2f - 225, height - 52);
        Fonts.get().getData().setScale(1f);
        Fonts.get().setColor(0.68f, 0.73f, 0.68f, 1f);
        Fonts.get().draw(batch, "Choisis ta ville, ton nombre de zombies et leur point de depart.",
                width / 2f - 238, height - 83);
        for (int i = 0; i < CITY_NAMES.length; i++) {
            float x = firstX + i * (cardWidth + gap);
            Fonts.get().setColor(i == selectedMap ? Color.GOLD : Color.WHITE);
            Fonts.get().draw(batch, CITY_NAMES[i], x + 12, cardY + 28);
        }
        Fonts.get().setColor(0.82f, 0.86f, 0.82f, 1f);
        Fonts.get().draw(batch, "ZOMBIES AU DEPART", width / 2f - 68, height * 0.45f);
        Fonts.get().setColor(Color.WHITE);
        Fonts.get().draw(batch, "-", width / 2f - 86, counterY + 27);
        Fonts.get().draw(batch, "+" , width / 2f + 73, counterY + 27);
        Fonts.get().setColor(0.49f, 0.94f, 0.52f, 1f);
        Fonts.get().draw(batch, Integer.toString(initialZombieCount), width / 2f - 8, counterY + 27);
        Fonts.get().setColor(0.82f, 0.86f, 0.82f, 1f);
        Fonts.get().draw(batch, zombiesTogether ? "DEPART GROUPE : depuis le carrefour" : "DEPART DISPERSE : repartis en ville",
                width / 2f - 153, height * 0.31f + 30);
        Fonts.get().setColor(0.98f, 0.81f, 0.52f, 1f);
        Fonts.get().draw(batch, "LANCER LA SIMULATION", width / 2f - 92, height * 0.20f + 36);
        Fonts.get().setColor(0.68f, 0.73f, 0.68f, 1f);
        Fonts.get().draw(batch, "Fleches : choix   Haut/Bas : nombre   Tab : depart groupe/disperse   Entree : lancer   Echap : menu",
                24, 30);
        Fonts.get().setColor(Color.WHITE);
        batch.end();
    }

    private void drawMapPreview(float x, float y, float width, float height, int mapIndex) {
        shapes.setColor(0.22f, 0.25f, 0.23f, 1f);
        shapes.rect(x, y, width, height);
        shapes.setColor(0.55f, 0.52f, 0.43f, 1f);
        shapes.rect(x, y + height * 0.40f, width, height * 0.13f);
        shapes.rect(x + width * 0.43f, y, width * 0.12f, height);
        for (float[] building : MAP_PREVIEW_BUILDINGS) {
            float buildingWidth = width * building[2] * (mapIndex == 1 ? 0.74f : mapIndex == 2 ? 1.06f : 1f);
            float buildingHeight = height * building[3] * (mapIndex == 1 ? 0.74f : 1f);
            shapes.setColor(mapIndex == 2 ? 0.38f : mapIndex == 1 ? 0.49f : 0.45f,
                    mapIndex == 2 ? 0.42f : mapIndex == 1 ? 0.44f : 0.37f,
                    mapIndex == 2 ? 0.43f : mapIndex == 1 ? 0.36f : 0.31f, 1f);
            shapes.rect(x + width * building[0], y + height * building[1], buildingWidth, buildingHeight);
        }
        if (mapIndex == 1) {
            shapes.setColor(0.32f, 0.43f, 0.29f, 1f);
            shapes.rect(x + width * 0.04f, y + height * 0.60f, width * 0.27f, height * 0.25f);
        } else if (mapIndex == 2) {
            shapes.setColor(0.66f, 0.46f, 0.27f, 1f);
            shapes.rect(x + width * 0.63f, y + height * 0.60f, width * 0.25f, height * 0.12f);
        }
    }

    private void populateCity() {
        for (int i = 0; i < CIVILIAN_COUNT; i++) {
            float[] position = randomWalkablePosition();
            float angle = random.nextFloat() * MathUtils.PI2;
            agents.add(new Agent(false, position[0], position[1],
                    MathUtils.cos(angle) * 20, MathUtils.sin(angle) * 20,
                    random.nextFloat() * MathUtils.PI2));
        }
        float[] origin = {720, 530};
        for (int i = 0; i < initialZombieCount; i++) {
            float[] position;
            if (zombiesTogether) {
                float angle = random.nextFloat() * MathUtils.PI2;
                float distance = random.nextFloat() * 25;
                position = new float[] {origin[0] + MathUtils.cos(angle) * distance,
                        origin[1] + MathUtils.sin(angle) * distance};
            } else {
                position = randomWalkablePosition();
            }
            float angle = random.nextFloat() * MathUtils.PI2;
            agents.add(new Agent(true, position[0], position[1],
                    MathUtils.cos(angle) * 17, MathUtils.sin(angle) * 17,
                    random.nextFloat() * MathUtils.PI2));
        }
    }

    private float[] randomWalkablePosition() {
        for (int attempt = 0; attempt < 1000; attempt++) {
            float x = 150 + random.nextFloat() * (CITY_WIDTH - 300);
            float y = 155 + random.nextFloat() * (CITY_HEIGHT - 310);
            if (!insideBuilding(x, y, AGENT_RADIUS + 8)) return new float[] {x, y};
        }
        throw new IllegalStateException("Impossible de placer une unite dans la ville.");
    }

    @Override
    public void render(float delta) {
        float step = Math.min(delta, 0.05f);
        elapsed += step;
        if (!started) {
            Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            drawConfiguration();
            return;
        }
        updateAgents(step);
        ScreenUtils.clear(0.10f, 0.14f, 0.13f, 1f);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), (int) worldViewHeight());
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        shapes.setProjectionMatrix(camera.combined);
        drawCity();
        drawAgents();
        drawInterface();
    }

    private void updateAgents(float delta) {
        advanceInfection(delta);
        int count = agents.size();
        float[] oldX = new float[count];
        float[] oldY = new float[count];
        float[] oldVx = new float[count];
        float[] oldVy = new float[count];
        boolean[] oldZombie = new boolean[count];
        float[] nextX = new float[count];
        float[] nextY = new float[count];
        float[] nextVx = new float[count];
        float[] nextVy = new float[count];
        for (int i = 0; i < count; i++) {
            Agent agent = agents.get(i);
            oldX[i] = agent.x;
            oldY[i] = agent.y;
            oldVx[i] = agent.vx;
            oldVy[i] = agent.vy;
            oldZombie[i] = agent.zombie;
            agent.transformedTimer = Math.max(0, agent.transformedTimer - delta);
        }

        for (int i = 0; i < count; i++) {
            Agent agent = agents.get(i);
            if (agent.attacker != null || agent.attackTarget != null) {
                nextX[i] = oldX[i];
                nextY[i] = oldY[i];
                nextVx[i] = 0;
                nextVy[i] = 0;
                continue;
            }
            float separationX = 0;
            float separationY = 0;
            float flockX = 0;
            float flockY = 0;
            float velocityX = 0;
            float velocityY = 0;
            int neighbors = 0;
            for (int j = 0; j < count; j++) {
                if (i == j || oldZombie[j] != oldZombie[i]) continue;
                float dx = oldX[j] - oldX[i];
                float dy = oldY[j] - oldY[i];
                float distanceSquared = dx * dx + dy * dy;
                if (distanceSquared > 120 * 120) continue;
                float distance = (float) Math.sqrt(distanceSquared);
                flockX += oldX[j];
                flockY += oldY[j];
                velocityX += oldVx[j];
                velocityY += oldVy[j];
                neighbors++;
                if (distance > 0 && distance < 38) {
                    float strength = (38 - distance) / 38;
                    separationX -= dx / distance * strength;
                    separationY -= dy / distance * strength;
                }
            }

            float steeringX = 0;
            float steeringY = 0;
            if (neighbors > 0) {
                if (!oldZombie[i]) {
                    flockX = flockX / neighbors - oldX[i];
                    flockY = flockY / neighbors - oldY[i];
                    steeringX += normalizeX(flockX, flockY) * 17;
                    steeringY += normalizeY(flockX, flockY) * 17;
                    steeringX += (velocityX / neighbors - oldVx[i]) * 0.55f;
                    steeringY += (velocityY / neighbors - oldVy[i]) * 0.55f;
                }
                steeringX += separationX * 68;
                steeringY += separationY * 68;
            }

            int target = -1;
            float targetDistanceSquared = Float.MAX_VALUE;
            int visibleThreat = -1;
            for (int j = 0; j < count; j++) {
                Agent other = agents.get(j);
                if (oldZombie[j] == oldZombie[i]) continue;
                if (other.attacker != null) continue;
                float dx = oldX[j] - oldX[i];
                float dy = oldY[j] - oldY[i];
                float distanceSquared = dx * dx + dy * dy;
                if (distanceSquared > 430 * 430 || distanceSquared >= targetDistanceSquared
                        || !hasLineOfSight(oldX[i], oldY[i], oldX[j], oldY[j])) continue;
                targetDistanceSquared = distanceSquared;
                if (oldZombie[i]) target = j;
                else visibleThreat = j;
            }

            float maxSpeed;
            if (oldZombie[i] && target >= 0) {
                float dx = oldX[target] - oldX[i];
                float dy = oldY[target] - oldY[i];
                float distance = (float) Math.sqrt(dx * dx + dy * dy);
                float pursuit = distance < 100 ? 145 : 92;
                steeringX += normalizeX(dx, dy) * pursuit;
                steeringY += normalizeY(dx, dy) * pursuit;
                maxSpeed = distance < 100 ? 118 : 62;
            } else if (!oldZombie[i] && visibleThreat >= 0) {
                float dx = oldX[i] - oldX[visibleThreat];
                float dy = oldY[i] - oldY[visibleThreat];
                steeringX += normalizeX(dx, dy) * 105;
                steeringY += normalizeY(dx, dy) * 105;
                maxSpeed = 76;
            } else if (!oldZombie[i] && agent.hasDestination) {
                Vector2 waypoint = agent.route.peekFirst();
                float targetX = waypoint == null ? agent.destinationX : waypoint.x;
                float targetY = waypoint == null ? agent.destinationY : waypoint.y;
                float dx = targetX - oldX[i];
                float dy = targetY - oldY[i];
                while (waypoint != null && dx * dx + dy * dy < 12 * 12) {
                    agent.route.removeFirst();
                    waypoint = agent.route.peekFirst();
                    targetX = waypoint == null ? agent.destinationX : waypoint.x;
                    targetY = waypoint == null ? agent.destinationY : waypoint.y;
                    dx = targetX - oldX[i];
                    dy = targetY - oldY[i];
                }
                if (waypoint == null && dx * dx + dy * dy < 18 * 18) {
                    agent.hasDestination = false;
                    agent.route.clear();
                } else {
                    steeringX += normalizeX(dx, dy) * 70;
                    steeringY += normalizeY(dx, dy) * 70;
                }
                maxSpeed = 62;
            } else {
                if (oldZombie[i]) {
                    agent.wanderAngle += (random.nextFloat() - 0.5f) * delta * 2.8f;
                } else {
                    agent.wanderAngle = agent.phase + elapsed * 0.29f;
                }
                float wanderStrength = oldZombie[i] ? 25 : 9;
                steeringX += MathUtils.cos(agent.wanderAngle) * wanderStrength;
                steeringY += MathUtils.sin(agent.wanderAngle) * wanderStrength;
                maxSpeed = oldZombie[i] ? 27 : 38;
            }

            float[] obstacleSteering = obstacleAvoidance(oldX[i], oldY[i]);
            steeringX += obstacleSteering[0];
            steeringY += obstacleSteering[1];
            if (oldX[i] < 90) steeringX += 75;
            if (oldX[i] > CITY_WIDTH - 90) steeringX -= 75;
            if (oldY[i] < 90) steeringY += 75;
            if (oldY[i] > CITY_HEIGHT - 90) steeringY -= 75;

            float vx = oldVx[i] + steeringX * delta;
            float vy = oldVy[i] + steeringY * delta;
            float speed = (float) Math.sqrt(vx * vx + vy * vy);
            if (speed < 0.01f) {
                vx = MathUtils.cos(agent.phase);
                vy = MathUtils.sin(agent.phase);
                speed = 1;
            }
            if (speed > maxSpeed) {
                vx = vx / speed * maxSpeed;
                vy = vy / speed * maxSpeed;
            }
            float candidateX = MathUtils.clamp(oldX[i] + vx * delta, AGENT_RADIUS, CITY_WIDTH - AGENT_RADIUS);
            float candidateY = MathUtils.clamp(oldY[i] + vy * delta, AGENT_RADIUS, CITY_HEIGHT - AGENT_RADIUS);
            if (insideBuilding(candidateX, candidateY, AGENT_RADIUS)) {
                float xOnly = MathUtils.clamp(oldX[i] + vx * delta, AGENT_RADIUS, CITY_WIDTH - AGENT_RADIUS);
                float yOnly = MathUtils.clamp(oldY[i] + vy * delta, AGENT_RADIUS, CITY_HEIGHT - AGENT_RADIUS);
                boolean canMoveX = !insideBuilding(xOnly, oldY[i], AGENT_RADIUS);
                boolean canMoveY = !insideBuilding(oldX[i], yOnly, AGENT_RADIUS);
                if (canMoveX && canMoveY) {
                    if (Math.abs(vx) >= Math.abs(vy)) {
                        candidateX = xOnly;
                        candidateY = oldY[i];
                        vy *= -0.45f;
                    } else {
                        candidateX = oldX[i];
                        candidateY = yOnly;
                        vx *= -0.45f;
                    }
                } else if (canMoveX) {
                    candidateX = xOnly;
                    candidateY = oldY[i];
                    vy *= -0.45f;
                } else if (canMoveY) {
                    candidateX = oldX[i];
                    candidateY = yOnly;
                    vx *= -0.45f;
                } else {
                    candidateX = oldX[i];
                    candidateY = oldY[i];
                    vx *= -0.35f;
                    vy *= -0.35f;
                }
            }
            nextX[i] = candidateX;
            nextY[i] = candidateY;
            nextVx[i] = vx;
            nextVy[i] = vy;
        }

        for (int i = 0; i < count; i++) {
            Agent agent = agents.get(i);
            agent.x = nextX[i];
            agent.y = nextY[i];
            agent.vx = nextVx[i];
            agent.vy = nextVy[i];
        }
        tryStartInfection();
    }

    private void advanceInfection(float delta) {
        for (Agent zombie : agents) {
            Agent human = zombie.attackTarget;
            if (human == null) continue;
            zombie.x = human.x;
            zombie.y = human.y;
            zombie.vx = 0;
            zombie.vy = 0;
            if (zombie.leapTimer > 0) {
                zombie.leapTimer = Math.max(0, zombie.leapTimer - delta);
                if (zombie.leapTimer == 0) zombie.conversionTimer = ZOMBIE_TRANSFORM_DURATION;
                return;
            }
            zombie.conversionTimer = Math.max(0, zombie.conversionTimer - delta);
            if (zombie.conversionTimer == 0) {
                human.zombie = true;
                human.wanderAngle = human.phase;
                human.transformedTimer = 0.8f;
                human.attacker = null;
                human.selected = false;
                human.hasDestination = false;
                human.route.clear();
                zombie.attackTarget = null;
                transformedCount++;
            }
            return;
        }
    }

    private void tryStartInfection() {
        for (Agent agent : agents) {
            if (agent.attackTarget != null) return;
        }
        Agent attacker = null;
        Agent target = null;
        float nearestDistance = 24 * 24;
        for (Agent zombie : agents) {
            if (!zombie.zombie) continue;
            for (Agent human : agents) {
                if (human.zombie || human.attacker != null) continue;
                float dx = human.x - zombie.x;
                float dy = human.y - zombie.y;
                float distance = dx * dx + dy * dy;
                if (distance < nearestDistance && hasLineOfSight(zombie.x, zombie.y, human.x, human.y)) {
                    nearestDistance = distance;
                    attacker = zombie;
                    target = human;
                }
            }
        }
        if (attacker != null) {
            attacker.attackTarget = target;
            attacker.leapTimer = ZOMBIE_LEAP_DURATION;
            target.attacker = attacker;
            target.hasDestination = false;
            target.route.clear();
            target.selected = false;
        }
    }

    private float[] obstacleAvoidance(float x, float y) {
        float steeringX = 0;
        float steeringY = 0;
        float padding = AGENT_RADIUS + 48;
        for (Building building : buildings) {
            Rectangle bounds = building.bounds;
            float left = bounds.x - padding;
            float right = bounds.x + bounds.width + padding;
            float bottom = bounds.y - padding;
            float top = bounds.y + bounds.height + padding;
            float nearestX = MathUtils.clamp(x, left, right);
            float nearestY = MathUtils.clamp(y, bottom, top);
            float dx = x - nearestX;
            float dy = y - nearestY;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance >= 72) continue;
            if (distance < 0.001f) {
                float leftDistance = x - left;
                float rightDistance = right - x;
                float bottomDistance = y - bottom;
                float topDistance = top - y;
                distance = Math.min(Math.min(leftDistance, rightDistance), Math.min(bottomDistance, topDistance));
                if (distance == leftDistance) dx = -1;
                else if (distance == rightDistance) dx = 1;
                else if (distance == bottomDistance) dy = -1;
                else dy = 1;
            }
            float strength = (72 - distance) / 72 * 115;
            steeringX += dx / Math.max(1, (float) Math.sqrt(dx * dx + dy * dy)) * strength;
            steeringY += dy / Math.max(1, (float) Math.sqrt(dx * dx + dy * dy)) * strength;
        }
        return new float[] {steeringX, steeringY};
    }

    private boolean hasLineOfSight(float fromX, float fromY, float toX, float toY) {
        float dx = toX - fromX;
        float dy = toY - fromY;
        int samples = Math.max(1, (int) (Math.sqrt(dx * dx + dy * dy) / 18));
        for (int i = 1; i < samples; i++) {
            float progress = i / (float) samples;
            if (insideBuilding(fromX + dx * progress, fromY + dy * progress, 0)) return false;
        }
        return true;
    }

    private boolean insideBuilding(float x, float y, float padding) {
        for (Building building : buildings) {
            if (x >= building.bounds.x - padding && x <= building.bounds.x + building.bounds.width + padding
                    && y >= building.bounds.y - padding
                    && y <= building.bounds.y + building.bounds.height + padding) return true;
        }
        return false;
    }

    private static float normalizeX(float x, float y) {
        float length = (float) Math.sqrt(x * x + y * y);
        return length > 0.001f ? x / length : 0;
    }

    private static float normalizeY(float x, float y) {
        float length = (float) Math.sqrt(x * x + y * y);
        return length > 0.001f ? y / length : 0;
    }

    private void drawCity() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(0.25f, 0.30f, 0.24f, 1f);
        shapes.rect(0, 0, CITY_WIDTH, CITY_HEIGHT);

        shapes.setColor(0.66f, 0.62f, 0.52f, 1f);
        shapes.rect(140, 140, 510, 950);
        shapes.rect(790, 140, 600, 950);
        shapes.rect(1490, 140, 570, 950);

        shapes.setColor(0.20f, 0.23f, 0.23f, 1f);
        shapes.rect(0, 0, CITY_WIDTH, 140);
        shapes.rect(0, 470, CITY_WIDTH, 120);
        shapes.rect(0, 970, CITY_WIDTH, 120);
        shapes.rect(0, 1220, CITY_WIDTH, 140);
        shapes.rect(0, 0, 140, CITY_HEIGHT);
        shapes.rect(650, 0, 140, CITY_HEIGHT);
        shapes.rect(1390, 0, 100, CITY_HEIGHT);
        shapes.rect(2060, 0, 140, CITY_HEIGHT);
        drawRoadMarkings();

        for (Building building : buildings) {
            Rectangle bounds = building.bounds;
            shapes.setColor(0.12f, 0.14f, 0.13f, 0.45f);
            shapes.rect(bounds.x + 9, bounds.y - 9, bounds.width, bounds.height);
            shapes.setColor(0.35f, 0.33f, 0.29f, 1f);
            shapes.rect(bounds.x - 5, bounds.y - 5, bounds.width + 10, bounds.height + 10);
            shapes.setColor(building.roof);
            shapes.rect(bounds.x, bounds.y, bounds.width, bounds.height);
            shapes.setColor(0.67f, 0.60f, 0.47f, 0.65f);
            shapes.rect(bounds.x + 9, bounds.y + bounds.height - 9, bounds.width - 18, 3);
            shapes.rect(bounds.x + 9, bounds.y + 7, bounds.width - 18, 2);
            shapes.setColor(0.32f, 0.35f, 0.34f, 1f);
            shapes.rect(bounds.x + bounds.width * 0.43f, bounds.y + bounds.height * 0.43f,
                    bounds.width * 0.14f, bounds.height * 0.14f);
        }

        if (selectedMap == 2) {
            shapes.setColor(0.34f, 0.37f, 0.35f, 1f);
            shapes.rect(160, 660, 460, 235);
            shapes.setColor(0.57f, 0.45f, 0.30f, 1f);
            for (int i = 0; i < 5; i++) {
                shapes.rect(195 + i * 82, 686, 36, 10);
                shapes.rect(195 + i * 82, 854, 36, 10);
            }
        } else if (selectedMap == 1) {
            drawPark(160, 660, 460, 245);
        } else {
            drawPark(160, 660, 460, 235);
        }
        shapes.end();
    }

    private void drawRoadMarkings() {
        shapes.setColor(0.76f, 0.65f, 0.39f, 0.75f);
        for (int x = 20; x < CITY_WIDTH; x += 90) {
            shapes.rect(x, 528, 46, 3);
            shapes.rect(x, 1028, 46, 3);
        }
        for (int y = 20; y < CITY_HEIGHT; y += 90) {
            shapes.rect(718, y, 3, 45);
            shapes.rect(1438, y, 3, 45);
        }
        shapes.setColor(0.72f, 0.72f, 0.63f, 0.55f);
        shapes.rect(140, 470, 510, 3);
        shapes.rect(790, 470, 600, 3);
        shapes.rect(1490, 470, 570, 3);
        shapes.rect(140, 587, 510, 3);
        shapes.rect(790, 587, 600, 3);
        shapes.rect(1490, 587, 570, 3);
        shapes.rect(140, 970, 510, 3);
        shapes.rect(790, 970, 600, 3);
        shapes.rect(1490, 970, 570, 3);
        shapes.rect(140, 1087, 510, 3);
        shapes.rect(790, 1087, 600, 3);
        shapes.rect(1490, 1087, 570, 3);
    }

    private void drawPark(float x, float y, float width, float height) {
        shapes.setColor(0.32f, 0.40f, 0.29f, 1f);
        shapes.rect(x, y, width, height);
        shapes.setColor(0.57f, 0.53f, 0.42f, 1f);
        shapes.rect(x + width * 0.48f, y + 10, 20, height - 20);
        shapes.rect(x + 10, y + height * 0.48f, width - 20, 18);
        shapes.setColor(0.22f, 0.31f, 0.23f, 1f);
        for (int i = 0; i < 9; i++) {
            float treeX = x + 32 + (i * 73) % (int) (width - 64);
            float treeY = y + 28 + (i * 47) % (int) (height - 56);
            shapes.circle(treeX, treeY, 15);
            shapes.setColor(0.40f, 0.42f, 0.29f, 1f);
            shapes.circle(treeX - 4, treeY + 5, 5);
            shapes.setColor(0.22f, 0.31f, 0.23f, 1f);
        }
    }

    private void drawAgents() {
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (Agent agent : agents) {
            shapes.setColor(0.04f, 0.06f, 0.05f, 0.55f);
            shapes.circle(agent.x + 2, agent.y - 2, AGENT_RADIUS + 1);
            float jumpHeight = agent.leapTimer > 0
                    ? MathUtils.sin((1f - agent.leapTimer / ZOMBIE_LEAP_DURATION) * MathUtils.PI) * 17 : 0;
            if (agent.zombie) {
                shapes.setColor(0.24f, 0.92f, 0.31f, 1f);
                shapes.circle(agent.x, agent.y + jumpHeight, AGENT_RADIUS);
                shapes.setColor(agent.transformedTimer > 0 ? 0.96f : 0.10f,
                        agent.transformedTimer > 0 ? 0.84f : 0.42f,
                        agent.transformedTimer > 0 ? 0.34f : 0.17f, 1f);
                shapes.circle(agent.x, agent.y + jumpHeight, 2.3f);
                if (agent.leapTimer > 0) {
                    shapes.setColor(0.72f, 0.95f, 0.48f, 0.65f);
                    shapes.circle(agent.x, agent.y, AGENT_RADIUS + 3);
                }
            } else {
                shapes.setColor(agent.attacker != null ? 1f : 0.96f,
                        agent.attacker != null ? 0.73f : 0.97f,
                        agent.attacker != null ? 0.31f : 0.91f, 1f);
                shapes.circle(agent.x, agent.y, agent.attacker != null ? AGENT_RADIUS : AGENT_RADIUS - 1);
            }
        }
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        for (Agent agent : agents) {
            if (agent.selected) {
                shapes.setColor(0.28f, 0.80f, 1f, 1f);
                shapes.circle(agent.x, agent.y, AGENT_RADIUS + 5);
            } else if (agent.attacker != null) {
                float progress = agent.attacker.leapTimer > 0
                        ? 0.12f : 1f - agent.attacker.conversionTimer / ZOMBIE_TRANSFORM_DURATION;
                shapes.setColor(1f, 0.73f, 0.28f, 1f);
                shapes.arc(agent.x, agent.y, AGENT_RADIUS + 5, 90, Math.max(20, progress * 360));
            }
            if (agent.selected && agent.hasDestination) {
                shapes.setColor(0.28f, 0.80f, 1f, 0.75f);
                Vector2 previous = new Vector2(agent.x, agent.y);
                for (Vector2 waypoint : agent.route) {
                    shapes.line(previous, waypoint);
                    previous = waypoint;
                }
                shapes.line(previous.x, previous.y, agent.destinationX, agent.destinationY);
                shapes.circle(agent.destinationX, agent.destinationY, 9);
            }
        }
        shapes.end();
    }

    private void drawInterface() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        batch.setProjectionMatrix(uiCamera.combined);
        shapes.setProjectionMatrix(uiCamera.combined);
        int zombies = 0;
        int civilians = 0;
        int selected = 0;
        for (Agent agent : agents) {
            if (agent.zombie) zombies++;
            else civilians++;
            if (agent.selected) selected++;
        }

        shapes.begin(ShapeRenderer.ShapeType.Filled);
        float height = Gdx.graphics.getHeight();
        shapes.setColor(0.025f, 0.035f, 0.035f, 0.97f);
        shapes.rect(0, height - HUD_HEIGHT, Gdx.graphics.getWidth(), HUD_HEIGHT);
        shapes.setColor(0.29f, 0.82f, 0.34f, 1f);
        shapes.circle(28, height - 47, 5);
        shapes.setColor(0.95f, 0.96f, 0.92f, 1f);
        shapes.circle(28, height - 70, 5);
        shapes.setColor(0.32f, 0.37f, 0.35f, 1f);
        shapes.rect(18, height - HUD_HEIGHT + 1, Gdx.graphics.getWidth() - 36, 1);
        shapes.end();

        batch.begin();
        Fonts.get().getData().setScale(1.1f);
        Fonts.get().setColor(0.98f, 0.80f, 0.46f, 1f);
        Fonts.get().draw(batch, "SURVIE EN VILLE", 46, height - 34);
        Fonts.get().getData().setScale(1f);
        Fonts.get().setColor(0.47f, 0.96f, 0.51f, 1f);
        Fonts.get().draw(batch, "Zombies : " + zombies, 46, height - 52);
        Fonts.get().setColor(Color.WHITE);
        Fonts.get().draw(batch, "Civils : " + civilians, 46, height - 75);
        Fonts.get().setColor(0.96f, 0.57f, 0.37f, 1f);
        Fonts.get().draw(batch, "Transformes : " + transformedCount, 160, height - 75);
        Fonts.get().setColor(0.38f, 0.82f, 1f, 1f);
        Fonts.get().draw(batch, "Selection : " + selected, 325, height - 75);
        Fonts.get().setColor(0.82f, 0.84f, 0.80f, 1f);
        Fonts.get().draw(batch, "Carte : " + CITY_NAMES[selectedMap]
                + " | Depart : " + (zombiesTogether ? "groupe" : "disperse"),
                500, height - 52);
        Fonts.get().draw(batch, "Selection : glisser gauche    Ordre : clic droit    "
                + "Vue : bouton milieu    Zoom : molette    Echap : menu", 46, height - 108);
        Fonts.get().setColor(Color.WHITE);
        batch.end();

        float slotSize = 36;
        float slotGap = 8;
        float slotsWidth = 5 * slotSize + 4 * slotGap;
        float slotsX = Gdx.graphics.getWidth() - slotsWidth - 24;
        float hudBottom = height - HUD_HEIGHT;
        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(0.40f, 0.67f, 0.70f, 0.85f);
        for (int i = 0; i < 5; i++) {
            shapes.rect(slotsX + i * (slotSize + slotGap), hudBottom + 8, slotSize, slotSize);
        }
        shapes.end();
        batch.begin();
        Fonts.get().setColor(0.55f, 0.74f, 0.75f, 1f);
        Fonts.get().draw(batch, "ACTIONS", slotsX, hudBottom + 62);
        batch.end();

        if (selecting) {
            float left = Math.min(selectionStartX, selectionEndX);
            float top = Math.max(HUD_HEIGHT, Math.min(selectionStartY, selectionEndY));
            float bottom = Math.min(Gdx.graphics.getHeight(),
                    Math.max(selectionStartY, selectionEndY));
            float boxY = Gdx.graphics.getHeight() - bottom;
            float boxWidth = Math.abs(selectionEndX - selectionStartX);
            float boxHeight = bottom - top;
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            shapes.setColor(0.28f, 0.80f, 1f, 0.13f);
            shapes.rect(left, boxY, boxWidth, boxHeight);
            shapes.end();
            shapes.begin(ShapeRenderer.ShapeType.Line);
            shapes.setColor(0.38f, 0.86f, 1f, 0.95f);
            shapes.rect(left, boxY, boxWidth, boxHeight);
            shapes.end();
        }
    }

    private void zoomAtCenter(float factor) {
        zoom = MathUtils.clamp(zoom * factor, 1f, 12f);
        updateCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    private void updateCamera(int width, int height) {
        if (width == 0 || height == 0) return;
        float centerX = cameraInitialized ? camera.position.x : CITY_WIDTH / 2f;
        float centerY = cameraInitialized ? camera.position.y : CITY_HEIGHT / 2f;
        float viewHeight = Math.max(1, height - HUD_HEIGHT);
        fitZoom = Math.max(CITY_WIDTH / width, CITY_HEIGHT / viewHeight);
        camera.setToOrtho(false, width, viewHeight);
        camera.zoom = fitZoom * zoom;
        camera.position.set(centerX, centerY, 0);
        cameraInitialized = true;
        uiCamera.setToOrtho(false, width, height);
        clampCamera();
    }

    private void clampCamera() {
        float halfWidth = camera.viewportWidth * camera.zoom / 2f;
        float halfHeight = camera.viewportHeight * camera.zoom / 2f;
        float centerX = CITY_WIDTH / 2f;
        float centerY = CITY_HEIGHT / 2f;
        camera.position.x = halfWidth * 2 >= CITY_WIDTH
                ? centerX : MathUtils.clamp(camera.position.x, halfWidth, CITY_WIDTH - halfWidth);
        camera.position.y = halfHeight * 2 >= CITY_HEIGHT
                ? centerY : MathUtils.clamp(camera.position.y, halfHeight, CITY_HEIGHT - halfHeight);
    }

    @Override
    public void resize(int width, int height) {
        updateCamera(width, height);
    }

    @Override
    public void hide() {
        cameraDragging = false;
        selecting = false;
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        audio.stopApocalypse();
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapes.dispose();
    }
}
