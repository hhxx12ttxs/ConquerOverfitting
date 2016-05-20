/**
 * 
 */
package game.model;

import game.render.Res;

import javax.microedition.lcdui.Graphics;

/**
 * @author Quynh Lam
 * 
 */
public class Effect {
    // 0: v?t sáng ?? d??i chân ch?p lęn 1 cái
    // 1: ??m l?a b?c cháy
    // 2: ??m sét
    // 3: v?t sét nhá lęn khi ch?m ng??i
    // 4: C?c ??t quay
    // 5: hi?u ?ng ??t ch?m ng??i
    // 6: n??c quay
    // 7: n??c ch?m ng??i
    // 8: m?c
    // 9: m?c nhá
    public static final int FRAME[][] = {//

    //
            { 0, 0 },//
            { 17, 16, 15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0 }, //
            { 0, 1, 2, 3, 4, 5, 6, 7 },//
            { 0 },//
            { 0, 1, 2, 3, 4, 0, 0, 1, 1, 2, 2, 3, 3, 4, 4 }, //
            { 0, 1 }, //
            { 0, 1, 2, 3, 4, 5, 6, 7, 8 },//
            { 0, 1 }, //
            { 8, 7, 6, 5, 4, 3, 2, 1, 0 },//
            { 0 },//
    };
    public static final int WIDTH[] = { 52, 11, 12, 41, 14, 32, 12, 32, 10, 32 };
    public static final int HEIGHT[] = { 18, 11, 12, 38, 14, 32, 12, 32, 10, 32 };
    int x, y, w, h;
    int type;
    int f;
    boolean wantDetroy;

    public Effect(int x, int y, int type) {
        this.x = x;
        this.y = y;
        this.type = type;
        System.out.println(type);
        w = WIDTH[type];
        h = HEIGHT[type];
        f = -1;
    }

    public void paint(Graphics g) {
        g.drawRegion(Res.imgEffect[type], 0, FRAME[type][f] * h, w, h, 0, x, y, 3);
    }

    /**
     * 
     */
    public void update() {
        f++;
        if (f >= FRAME[type].length) {
            f = 0;
            wantDetroy = true;
        }
    }
}

