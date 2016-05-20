package hu.krivan.jklickety;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Random;
import javax.swing.JPanel;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * A játék lelke; a játéktábla.
 *
 * @author Balint
 */
public class Table extends JPanel {

    private static final int CANVAS_WIDTH = 330;
    private static final int CANVAS_HEIGHT = 495;

    public static Color getRGB(int r, int g, int b) {
        float HSB[] = new float[3];

        Color.RGBtoHSB(r, g, b, HSB);

        return Color.getHSBColor(HSB[0], HSB[1], HSB[2]);
    }
    public static final int NUMBER_OF_COLORS = 4;
    public static final Color COLORS[] = {
        getRGB(100, 200, 200), getRGB(200, 100, 100),
        getRGB(100, 200, 100), getRGB(200, 200, 100),
        getRGB(200, 100, 200), getRGB(100, 100, 200)
    };
    public static final Color INNER_FRAME[] = {
        getRGB(50, 100, 100), getRGB(100, 50, 50),
        getRGB(50, 100, 50), getRGB(100, 100, 50),
        getRGB(100, 50, 100), getRGB(50, 50, 100)
    };
    public static final Color OUTER_FRAME[] = {
        getRGB(90, 181, 181), getRGB(181, 90, 90),
        getRGB(90, 181, 90), getRGB(181, 181, 90),
        getRGB(181, 90, 181), getRGB(90, 90, 181)
    };
    private final Image im = getToolkit().getImage(getClass().getResource("resources/background.jpg"));
    private int table[][];
    private int width, height;
    /**
     * Az a legkisebb indexű oszlop, ahol történt nullázás
     */
    private int legkisebbX;
    /**
     * Az a legnagyobb indexű oszlop, ahol történt nullázás
     */
    private int legnagyobbX;
    /**
     * Az a legnagyobb indexű sor, ahol történt nullázás
     */
    private int legnagyobbY;
    private double points;
    private final Controller controller;

    /**
     * Ezt a konstruktort csak a GUI Editor használja.
     *
     * @deprecated ne használd!
     */
    public Table() {
        this(10, 15, null);
    }

    /**
     * Megadott méretű táblázatot hoz létre.
     *
     * @param width táblázat szélessége
     * @param height táblázat magassága
     * @param controller {@link Controller} példány
     */
    public Table(final int width, final int height, final Controller controller) {
        super();
        this.width = width;
        this.height = height;
        this.controller = controller;

        reset();
        addMouseListener(new MouseListener() {

            @Override
            public void mouseClicked(MouseEvent e) {
                // valamiért buggos, ezért a releasedet használjuk
            }

            @Override
            public void mousePressed(MouseEvent e) {
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                int cx = (int) ((double) e.getX() / ((double) getWidth() / width));
                int cy = (int) ((double) e.getY() / ((double) getHeight() / height));

                click(cx, cy);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }
        });
    }

    /**
     * Oszlopok számának lekérdezése
     *
     * @return
     */
    public int getColumns() {
        return width;
    }

    /**
     * Sorok számának lekérdezése
     *
     * @return
     */
    public int getRows() {
        return height;
    }

    /**
     * Alaphelyzetbe állítja a táblázatot (új leosztás) és a pontszámot törli
     */
    public final void reset() {
        table = new int[width][height];
        setPoints(0);

        Random r = new Random();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                table[x][y] = (int) Math.round(r.nextDouble() * (NUMBER_OF_COLORS - 1) + 1);
            }
        }
    }

    @Override
    public Dimension getMinimumSize() {
        return new Dimension(CANVAS_WIDTH, CANVAS_HEIGHT);
    }

    @Override
    public Dimension getPreferredSize() {
        return getMinimumSize();
    }

    /**
     * A kirajzolás logikája.
     *
     * @param g
     */
    @Override
    public void paint(Graphics g) {
        g.drawImage(im, 0, 0, this);

        int y1, y2, x1, x2;

        g.drawRect(0, 0, CANVAS_WIDTH - 1, CANVAS_HEIGHT - 1);

        for (int i = (width - 1); i >= 0; i--) {
            for (int n = (height - 1); n >= 0; n--) {
                if (table[i][n] != 0 && table[i][n] != (NUMBER_OF_COLORS + 1)) {
                    g.setColor(COLORS[(table[i][n] - 1)]);
                    g.fillRect(i * CANVAS_WIDTH / (width) - 1, n * CANVAS_HEIGHT / (height) - 1, CANVAS_WIDTH / (width) + 1, CANVAS_HEIGHT / (height) + 1);
                }
            }
        }

        for (int i = 0; i < width; i++) {
            for (int n = 0; n < height; n++) {
                if (table[i][n] != 0 && table[i][n] != (NUMBER_OF_COLORS + 1)) {
                    //Ha belső négyzetekről van szó, akkor nincs probléma, tehát körbe rajzolhatunk.
                    if (i == 0) {
                        g.setColor(INNER_FRAME[(table[i][n] - 1)]);
                        y1 = 0;
                        y2 = 0;
                    } else {
                        if (table[i - 1][n] != table[i][n]) {
                            g.setColor(INNER_FRAME[(table[i][n] - 1)]);
                            y1 = 0;
                            y2 = 0;
                        } else {
                            g.setColor(OUTER_FRAME[(table[i][n] - 1)]);
                            y1 = 1;
                            y2 = -2;
                        }
                    }
                    g.drawLine(i * CANVAS_WIDTH / (width), n * CANVAS_HEIGHT / (height) + y1, i * CANVAS_WIDTH / (width), (n + 1) * CANVAS_HEIGHT / (height) + y2);

                    if (i == (width - 1)) {
                        g.setColor(INNER_FRAME[(table[i][n] - 1)]);
                        y1 = 0;
                        y2 = 0;
                    } else {
                        if (table[i + 1][n] != table[i][n]) {
                            g.setColor(INNER_FRAME[(table[i][n] - 1)]);
                            y1 = 0;
                            y2 = 0;
                        } else {
                            g.setColor(OUTER_FRAME[(table[i][n] - 1)]);
                            y1 = 1;
                            y2 = -2;
                        }
                    }
                    g.drawLine((i + 1) * CANVAS_WIDTH / (width) - 1, n * CANVAS_HEIGHT / (height) + y1, (i + 1) * CANVAS_WIDTH / (width) - 1, (n + 1) * CANVAS_HEIGHT / (height) + y2);

                    if (n == 0) {
                        g.setColor(INNER_FRAME[(table[i][n] - 1)]);
                        x1 = -1;
                        x2 = +1;
                    } else {
                        if (table[i][n - 1] != table[i][n]) {
                            g.setColor(INNER_FRAME[(table[i][n] - 1)]);
                            x1 = -1;
                            x2 = +1;
                        } else {
                            g.setColor(OUTER_FRAME[(table[i][n] - 1)]);
                            x1 = 0;
                            x2 = 0;
                        }
                    }
                    g.drawLine(i * CANVAS_WIDTH / (width) + 1 + x1, n * CANVAS_HEIGHT / (height), (i + 1) * CANVAS_WIDTH / (width) - 2 + x2, n * CANVAS_HEIGHT / (height));

                    if (n == (height - 1)) {
                        g.setColor(INNER_FRAME[(table[i][n] - 1)]);
                        x1 = -1;
                        x2 = +1;
                    } else {
                        if (table[i][n + 1] != table[i][n]) {
                            g.setColor(INNER_FRAME[(table[i][n] - 1)]);
                            x1 = -1;
                            x2 = +1;
                        } else {
                            g.setColor(OUTER_FRAME[(table[i][n] - 1)]);
                            x1 = 0;
                            x2 = 0;
                        }
                    }
                    g.drawLine(i * CANVAS_WIDTH / (width) + 1 + x1, (n + 1) * CANVAS_HEIGHT / (height) - 1, (i + 1) * CANVAS_WIDTH / (width) - 2 + x2, (n + 1) * CANVAS_HEIGHT / (height) - 1);
                }
            }
        }
    }

    /**
     * Táblára kattintunk a cx, cy pozícióba
     *
     * @param cx
     * @param cy
     */
    private void click(int cx, int cy) {
        if (canBeRemoved(cx, cy)) { // megnézzük, hogy el lehet-e távolítani
            remove(cx, cy); // eltávolítjuk őket
            for (int i = 0; i < legnagyobbY; i++) {
                moveDown(); // ekkor biztosan mindenki helyére kerül
            }
            shift(); // oszlopokat is mozgatjuk

            repaint(); // újrarajzolunk

            if (noMoreMove()) {
                // jelezzük a vezérlő felé, hogy vége a játéknak.
                controller.onNoMoreMove((int) points);
            }
        }
    }

    /**
     * Pontszám beállítása. Ezt hívjuk, mert ez értesíti a controllert a
     * pontszám változásáról!
     *
     * @param points
     */
    private void setPoints(double points) {
        this.points = points;
        if (controller != null) {
            // GUI editor nézetben elhasalna, mert ott nincs controller
            controller.onPointsChanged(this.points);
        }
    }

    /**
     * Meghatározza, hogy egy adott mezőre kattintva azt el lehet-e távolítani,
     * azaz van-e szomszédja akinek ugyanaz a színe
     *
     * @param x
     * @param y
     * @return eltüntethető-e az adott mező (és azonos színű szomszédjai)
     */
    private boolean canBeRemoved(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) {
            return false; // nem jó értékek esetén nyílván nem.
        }

        int color = table[x][y];

        if (color == 0 || color > NUMBER_OF_COLORS) {
            return false; // ha nincs legáis érték akkor sem.
        }

        if (x < (width - 1)) {
            // ha nem a jobb szélen van, akkor vizsgálunk jobb szomszédot
            if (table[x + 1][y] == color) {
                return true;
            }
        }
        if (x > 0) {
            if (table[x - 1][y] == color) {
                return true;
            }
        }
        if (y < (height - 1)) {
            if (table[x][y + 1] == color) {
                return true;
            }
        }
        if (y > 0) {
            if (table[x][y - 1] == color) {
                return true;
            }
        }

        // semelyik szomszédja se azonos színű, akkor vége
        return false;
    }

    /**
     * Az adott mezőt kinullázza és a 3. paraméterben megadott színű
     * szomszédjaira
     *
     * @param x
     * @param y
     */
    private int makeItZero(int x, int y) {
        int color = table[x][y];
        table[x][y] = 0;
        if (legnagyobbX < x) {
            legnagyobbX = x;
        }
        if (legkisebbX > x) {
            legkisebbX = x;
        }
        if (legnagyobbY < y) {
            legnagyobbY = y;
        }
        int count = 1;

        if (x < (width - 1)) {
            // ha nem a jobb szélen van, akkor vizsgálunk jobb szomszédot
            if (table[x + 1][y] == color) {
                count += makeItZero(x + 1, y);
            }
        }
        if (x > 0) {
            if (table[x - 1][y] == color) {
                count += makeItZero(x - 1, y);
            }
        }
        if (y < (height - 1)) {
            if (table[x][y + 1] == color) {
                count += makeItZero(x, y + 1);
            }
        }
        if (y > 0) {
            if (table[x][y - 1] == color) {
                count += makeItZero(x, y - 1);
            }
        }

        return count;
    }

    /**
     * Adott mezőt és azonos színű szomszédjait kinullázza. (még nem potyognak
     * le)
     *
     * @param x
     * @param y
     */
    private void remove(int x, int y) {
        legkisebbX = x;
        legnagyobbX = x;
        legnagyobbY = y;

        int db = makeItZero(x, y);

        setPoints(points + Math.pow(1.25, db - 2));
    }

    /**
     * Kilövés után egy rés marad a táblázatban, ezt el kell tüntetni úgy, hogy
     * a hézagok helyére fentről bemozgatjuk az elemeket, ez egy lépésben
     * mindenkit eggyel lejjebb visz.
     */
    private void moveDown() {
        int legalso;

        for (int x = legkisebbX; x <= legnagyobbX; x++) {
            legalso = -1;

            for (int y = 0; y <= legnagyobbY; y++) {
                if (table[x][y] == 0) {
                    legalso = y;
                }
            }

            if (legalso != -1) {
                for (int i = legalso; i > 0 && table[x][i] != (NUMBER_OF_COLORS + 1); i--) { // megtaláltuk az x. oszlopban a rés alját, mindenkit eggyel le
                    table[x][i] = table[x][i - 1];
                }
                table[x][0] = NUMBER_OF_COLORS + 1; // teteje az mindig tutira 0, mert legalább 1 törlődött
            }
        }
    }

    /**
     * Ha lesznek üres oszlopok, akkor a tőlük jobbra lévőeket eggyel balra kell
     * mozgatni!
     */
    private void shift() {
        boolean noMore;

        for (int x = legkisebbX; x < width - 1;) {
            noMore = true;
            if (table[x][height - 1] == (NUMBER_OF_COLORS + 1)) { // x. oszlopban nincs elem
                // x. oszlopban üresedés van. legegyszerűbb, ha mindent eltolunk balra.
                for (int y = 0; y < height; y++) {
                    for (int x2 = x; x2 < width - 1; x2++) {
                        table[x2][y] = table[x2 + 1][y];
                        if (table[x2][y] != NUMBER_OF_COLORS + 1) {
                            noMore = false; // ha legalább 1 színt be tudtunk mozgatni, akkor nem a legvégén vagyunk!
                        }
                    }
                    table[width - 1][y] = NUMBER_OF_COLORS + 1;
                }
                if (noMore) {
                    break;
                }
            } else {
                x++;
            }
        }
    }

    /**
     * Sakk-tábla szerűen megnézzük, hogy van-e még olyan mező amire lehet
     * kattintani
     *
     * @return van-e még lépés
     */
    private boolean noMoreMove() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (((y % 2 == 1) && (x % 2 == 1)) || ((y % 2 == 0) && (x % 2 == 0))) {
                    if (canBeRemoved(x, y)) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public TableData getTableData() {
        return new TableData(table, points);
    }
    
    public void loadTableData(TableData td) {
        table = td.getTable();
        setPoints(td.getPoints());
        repaint();
    }
}

