package com.msc.game.Graphics;

import com.msc.game.level.tile.Tile;
import com.msc.game.entitiy.mob.Player;
import java.util.Random;

public class Screen {

    public int width, height;
    public int[] pixels;
    public final int MAP_SIZE = 64;
    public final int MAP_SIZE_MASK = MAP_SIZE - 1;
    public int[] tiles = new int[MAP_SIZE * MAP_SIZE];

    public int xoffset, yoffset;

    private Random random = new Random();

    public Screen(int width, int height) {
        this.width = width;
        this.height = height;
        pixels = new int[width * height];

        for (int i = 0; i < MAP_SIZE * MAP_SIZE; i++) {
            tiles[i] = random.nextInt(0xffffff);
        }
    }

    public void clear() {
        for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0;
        }
    }

    public void render(int xoffset, int yoffset) {
        for (int y = 0; y < height; y++) {
            int yy = y + yoffset;
            if (yy < 0 || yy >= height) {
                continue;
            }
            for (int x = 0; x < width; x++) {
                int xx = x + xoffset;
                if (xx < 0 || xx >= width) {
                    continue;
                }
                // int tileIndex = (xx >> 4) + (yy >> 4) * 64;
                //  int tileIndex = ((xx >> 4) + xoffset & MAP_SIZE_MASK) + ((yy >> 4) & MAP_SIZE_MASK) * MAP_SIZE;
                //  pixels[x + y * width] = tiles[tileIndex];
                pixels[(xx) + (yy) * width] = Sprite.grass.pixels[(x & Sprite.grass.SIZE - 1) + (y & Sprite.grass.SIZE - 1) * Sprite.grass.SIZE];
            }
        }
    }

    public void renderTile(int xp, int yp, Tile tile) {
        xp -= this.xoffset;
        yp -= this.yoffset;

        for (int y = 0; y < tile.sprite.SIZE; y++) {
            int ya = y + yp;
            for (int x = 0; x < tile.sprite.SIZE; x++) {
                int xa = x + xp;
                if (xa < -tile.sprite.SIZE || xa >= width || ya < 0 || ya >= height) {
                    break;
                }
                if (xa < 0) {
                    xa = 0;
                }
                pixels[xa + ya * width] = tile.sprite.pixels[x + y * tile.sprite.SIZE];
            }
        }
    }

    public void renderPlayer(int xp, int yp, Sprite sprite) {
        xp -= this.xoffset;
        yp -= this.yoffset;

        for (int y = 0; y < 32; y++) {
            int ya = y + yp;
            for (int x = 0; x < 32; x++) {
                int xa = x + xp;
                if (xa < -32 || xa >= width || ya < 0 || ya >= height) {
                    break;
                }
                if (xa < 0) {
                    xa = 0;
                }
                int col = sprite.pixels[x + y * 32];
                if (col != 0xffFF00FF) {
                    pixels[xa + ya * width] = col;
                }
            }
        }
    }

    public void setOffSet(int xOffSet, int yOffSet) {
        this.xoffset = xOffSet;
        this.yoffset = yOffSet;
    }
}

