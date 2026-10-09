package com.catacombes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

final class LevelFactory {
    static final int COUNT = 20;
    private static final int WIDTH = 31;
    private static final int HEIGHT = 23;

    private LevelFactory() {
    }

    static char[][] create(int level) {
        if (level < 0 || level >= COUNT) {
            throw new IllegalArgumentException("Niveau invalide : " + (level + 1));
        }

        char[][] map = new char[HEIGHT][WIDTH];
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) map[y][x] = '#';
        }

        Random random = new Random(67091L + level * 7919L);
        carveMaze(map, 1, 1, random);
        map[1][1] = 'P';
        map[HEIGHT - 2][WIDTH - 2] = 'X';

        List<int[]> openTiles = new ArrayList<>();
        for (int y = 1; y < HEIGHT - 1; y++) {
            for (int x = 1; x < WIDTH - 1; x++) {
                if (map[y][x] == '.') openTiles.add(new int[] {x, y});
            }
        }
        Collections.shuffle(openTiles, random);

        int cursor = 0;
        int enemyCount = Math.min(5 + level / 2, 24);
        for (int i = 0; i < enemyCount; i++) {
            Enemy.Type type = chooseEnemyType(level, i);
            int[] position = nextEnemyPosition(openTiles, cursor++, map, WIDTH - 2, HEIGHT - 2);
            if (position == null) break;
            map[position[1]][position[0]] = type.mapSymbol;
        }

        if (level == 1 || level == 5 || level == 10 || level == 15) {
            int[] position = nextAvailable(openTiles, cursor++, map);
            if (position != null) map[position[1]][position[0]] = level == 1 ? '2' : '3';
        }
        if (level % 2 == 1 || level >= 5) {
            int[] position = nextAvailable(openTiles, cursor++, map);
            if (position != null) map[position[1]][position[0]] = 'h';
        }
        if (level % 3 == 2 || level >= 8) {
            int[] position = nextAvailable(openTiles, cursor++, map);
            if (position != null) map[position[1]][position[0]] = 'h';
        }

        int torchCount = 2 + level % 3;
        for (int i = 0; i < torchCount; i++) {
            int[] position = nextAvailable(openTiles, cursor++, map);
            if (position != null) map[position[1]][position[0]] = 't';
        }
        return map;
    }

    private static Enemy.Type chooseEnemyType(int level, int index) {
        if (level >= 4 && level % 5 == 4 && index == 0) return Enemy.Type.BOSS;
        if (level >= 3 && (index + level) % 4 == 0) return Enemy.Type.MAGE;
        if (level >= 2 && (index + level) % 3 == 0) return Enemy.Type.ARCHER;
        return Enemy.Type.KNIGHT;
    }

    private static int[] nextAvailable(List<int[]> openTiles, int start, char[][] map) {
        for (int i = start; i < openTiles.size(); i++) {
            int[] position = openTiles.get(i);
            if (map[position[1]][position[0]] == '.') return position;
        }
        for (int[] position : openTiles) {
            if (map[position[1]][position[0]] == '.') return position;
        }
        return null;
    }

    private static int[] nextEnemyPosition(List<int[]> openTiles, int start, char[][] map,
            int exitX, int exitY) {
        for (int i = start; i < openTiles.size(); i++) {
            int[] position = openTiles.get(i);
            int distanceFromStart = Math.abs(position[0] - 1) + Math.abs(position[1] - 1);
            int distanceFromExit = Math.abs(position[0] - exitX) + Math.abs(position[1] - exitY);
            if (map[position[1]][position[0]] == '.' && distanceFromStart > 7 && distanceFromExit > 2) {
                return position;
            }
        }
        return nextAvailable(openTiles, start, map);
    }

    private static void carveMaze(char[][] map, int x, int y, Random random) {
        map[y][x] = '.';
        int[][] directions = {{0, -2}, {0, 2}, {-2, 0}, {2, 0}};
        for (int i = directions.length - 1; i > 0; i--) {
            int swap = random.nextInt(i + 1);
            int[] current = directions[i];
            directions[i] = directions[swap];
            directions[swap] = current;
        }
        for (int[] direction : directions) {
            int nextX = x + direction[0];
            int nextY = y + direction[1];
            if (nextX <= 0 || nextX >= WIDTH - 1 || nextY <= 0 || nextY >= HEIGHT - 1
                    || map[nextY][nextX] != '#') {
                continue;
            }
            map[y + direction[1] / 2][x + direction[0] / 2] = '.';
            carveMaze(map, nextX, nextY, random);
        }
    }
}
