/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package thinkingonsoftware.net.gu.model.universe.dao;

import com.sun.java.swing.plaf.windows.WindowsInternalFrameTitlePane;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.sql.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.ImageIcon;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import thinkingonsoftware.net.gu.lib.FastNoise;
import thinkingonsoftware.net.gu.model.players.Fleet;
import thinkingonsoftware.net.gu.model.players.Player;
import thinkingonsoftware.net.gu.model.players.Population;

/**
 *
 * @author Lorenzo Boccaccia
 */
public class Body implements Serializable {

    static Body generateRandomPlanet(Body focus) {
        Body b = generateRandomPlanet();
        b.setFocus(focus);
        b.setFocusDistance(Math.random() * Math.random() * 50 + 0.4);
        return b;
    }
    //game information
    private Integer metalsDensity;
    private Integer earthsDensity;
    private Integer chemicalDensity;
    Collection<Colony> colonies = new ArrayList<Colony>();
    Collection<Army> armies;
    protected BodyType type = BodyType.GENERIC;
    Set<Construction> buildings = new HashSet<Construction>();
    Collection<Fleet> groundedFleets = new ArrayList<Fleet>();
    Collection<Fleet> orbitingFleets = new ArrayList<Fleet>();
    //graphical information
    private double magnitude;
    public StellarType classification = StellarType.random();
    public Color bodyPrimary = new Color((float) Math.random(), (float) Math.random(), (float) Math.random());
    public Color bodySecondary = new Color((float) Math.random(), (float) Math.random(), (float) Math.random()); //ignored in stars
    public Color bodyAtmos = new Color((float) Math.random(), (float) Math.random(), (float) Math.random()); //ignored in stars
    public Color bodyOvercast = new Color((float) Math.random(), (float) Math.random(), (float) Math.random()); //ignored in stars
    public int seed = Double.toString(Math.random() * 8192.0).hashCode() >> 16;
    //TODO: externalize and randomize
    public int turbolence = 4;
    Double overcastCoverage = Math.random();
    Double landCoverage = Math.random();

    public static Body generateRandomStar() {
        return new Body(BodyType.STAR);
    }
    private transient WeakReference<BufferedImage> image = null;

    public Body() {
    }

    public static Body generateRandomPlanet() {
        return new Body(BodyType.PLANET);
    }
    public String trueName;
    private Body focus;
    //AU
    private Double focusDistance;
    //YEARS
    private Double focusPeriod;
    //OFFSET RAD
    private double focusPhase;

    public Body(BodyType bodyType) {
        this.type = bodyType;
    }

    @Override
    public String toString() {
        return ReflectionToStringBuilder.toString(this);
    }

    public Point getDrawPosition() {
        if (getFocus() == null) {
            return new Point(0, 0);
        }

        double x = getFocus().getDrawPosition().getX() * 1000.0d;
        double y = getFocus().getDrawPosition().getX() * 1000.0d;

        x = x + getFocusDistance() * 300000.0d * Math.cos(getFocusPhase());
        y = y + getFocusDistance() * 300000.0d * Math.sin(getFocusPhase());

        return new Point((int) (x / 1000.0d), (int) (y / 1000.0d));
    }

    public String getTrueName() {
        return trueName;
    }

    public BodyType getType() {
        return type;
    }

    /**
     * @return the focus
     */
    public Body getFocus() {
        return focus;
    }

    /**
     * @param focus the focus to set
     */
    public void setFocus(Body focus) {
        this.focus = focus;
    }

    /**
     * @param type the type to set
     */
    public void setType(BodyType type) {
        this.type = type;
    }

    /**
     * @param trueName the trueName to set
     */
    public void setTrueName(String trueName) {
        this.trueName = trueName;
    }

    /**
     * @return the focusDistance
     */
    public double getFocusDistance() {
        return focusDistance;
    }

    /**
     * @param focusDistance the focusDistance to set
     */
    public void setFocusDistance(Double focusDistance) {
        this.focusDistance = focusDistance;
    }

    /**
     * @return the focusPeriod
     */
    public double getFocusPeriod() {
        return focusPeriod;
    }

    /**
     * @param focusPeriod the focusPeriod to set
     */
    public void setFocusPeriod(double focusPeriod) {
        this.focusPeriod = focusPeriod;
    }

    /**
     * @return the focusPhase
     */
    public double getFocusPhase() {
        return focusPhase;
    }

    /**
     * @param focusPhase the focusPhase to set
     */
    public void setFocusPhase(double focusPhase) {
        this.focusPhase = focusPhase;
    }

    /**
     * @return the magnitude
     */
    public double getMagnitude() {
        return magnitude;
    }

    /**
     * @param magnitude the magnitude to set
     */
    public void setMagnitude(double magnitude) {
        this.magnitude = magnitude;
    }

    /**
     * @return the classificatiom
     */
    public StellarType getClassificatiom() {
        return classification;
    }

    /**
     * @param classificatiom the classificatiom to set
     */
    public void setClassificatiom(StellarType classificatiom) {
        this.classification = classificatiom;
    }

    public Integer getEarthsDensity() {
        return earthsDensity;
    }

    public Integer getChemicalDensity() {
        return chemicalDensity;
    }

    public Integer getMetalsDensity() {
        return metalsDensity;
    }

    public void setChemicalDensity(Integer chemicalDensity) {
        this.chemicalDensity = chemicalDensity;
    }

    public void setEarthsDensity(Integer earthsDensity) {
        this.earthsDensity = earthsDensity;
    }

    public void setMetalsDensity(Integer metalsDensity) {
        this.metalsDensity = metalsDensity;
    }

    public Collection<? extends Fleet> getAllFleets() {
        ArrayList<Fleet> ret = new ArrayList<Fleet>();

        ret.addAll(groundedFleets);
        ret.addAll(orbitingFleets);

        return ret;
    }

    public int getTotalPopulation() {
        int ret = 0;

        for (Colony p : colonies) {
            ret += p.getPopulationSize();
        }

        return ret;
    }

    public int getProductionModifier(Player p) {
        if (armies.size() < 2) {
            int populationPlayer = 0;
            for (Colony c : getColoniesForPlayer(p)) {
                populationPlayer += c.getWorkForce();
            }
            return (100 * populationPlayer) / getTotalPopulation();
        }

        int warEffort = 0;
        int playerDefence = 0;
        int securityFactor = 0;
        for (Army a : armies) {
            warEffort = a.totalOffence();
            securityFactor += a.totalDefence() / 2;
            if (a.isControlledBy(p)) {
                playerDefence += a.totalDefence();
                warEffort -= a.totalOffence();
            }
        }

        double securityModifier = (securityFactor) / warEffort;
        double warModifier = (playerDefence) / warEffort;

        return (int) (100 * (securityModifier * warModifier));
    }

    public Collection<Colony> getColoniesForPlayer(Player p) {
        Collection<Colony> ret = new LinkedList<Colony>();
        for (Colony c : colonies) {
            if (c.isControlledBy(p)) {
                ret.add(c);
            }
        }

        return ret;
    }

    public BufferedImage getImage() {
        BufferedImage ret = null;
        if (image != null) {
            ret = image.get();
            if (ret != null) {
                return ret;
            }
        }
        if (this.type == BodyType.STAR) {
            ret = generateStarImage();
        }
        if (this.type == BodyType.PLANET) {
            ret = generatePlanetImage();
        }
        image = new WeakReference<BufferedImage>(ret);
        return ret;
    }

    private BufferedImage generateStarImage() {
        final int size = 512;

        //parameters
        System.out.println("Seed: " + seed);


        final BufferedImage background = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);
        Color starColor = classification.color;
        Color starHalo = starColor.darker().darker();


        final BufferedImage afilter = toBufferedImage("data/matm.png");
        final BufferedImage bfilter = toBufferedImage("data/mcut.png");



        Graphics2D g = (Graphics2D) background.getGraphics();
        final Raster data = background.getData();
        final DataBuffer dataBuffer = data.getDataBuffer();

        final DataBuffer aBuffer = afilter.getData().getDataBuffer();
        final DataBuffer bBuffer = bfilter.getData().getDataBuffer();


        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int sized = size / 2;
                double a = Math.atan2((x - sized), (y - sized)) * 16.0;

                double dist = Math.sqrt((double) ((y - sized) * (y - sized) + (x - sized) * (x - sized)));

                int noise1 = FastNoise.noise(((x * turbolence + seed) / (32.0)), ((y * turbolence + seed) / (24.0)), 8) - 64;
                int noise = FastNoise.noise(a + seed, Math.log(dist) * 10.0 + seed, 8) - 128;

                final int pos = (x + y * size) * 3;

                int val = aBuffer.getElem(pos);
                int val1 = bBuffer.getElem(pos);

                int red = clipColor(starHalo.getRed() + noise);
                int green = clipColor(starHalo.getGreen() + noise);
                int blue = clipColor(starHalo.getBlue() + noise);

                int red1 = clipColor(starColor.getRed() + noise1);
                int green1 = clipColor(starColor.getGreen() + noise1);
                int blue1 = clipColor(starColor.getBlue() + noise1);



                red = red * val / 255;
                green = green * val / 255;
                blue = blue * val / 255;

                red1 = red1 * val1 / 255;
                green1 = green1 * val1 / 255;
                blue1 = blue1 * val1 / 255;

                double w = red + green + blue;
                double w1 = red1 + green1 + blue1;

                red = (int) ((red * w + red1 * w1) / (w + w1));
                green = (int) ((green * w + green1 * w1) / (w + w1));
                blue = (int) ((blue * w + blue1 * w1) / (w + w1));

                /*
                 red = red1;
                 blue=blue1;
                 green=green1;
                 */

                int posw = (x + y * size) * 4;
                if (red + green + blue < 128) {
                    dataBuffer.setElem(posw, clipColor(red + green + blue - 32));

                } else {
                    dataBuffer.setElem(posw, 255);
                }

                dataBuffer.setElem(posw + 1, blue);
                dataBuffer.setElem(posw + 2, green);
                dataBuffer.setElem(posw + 3, red);

            }
        }

        background.setData(data);
        return background;
    }

    private static BufferedImage toBufferedImage(String datatemplatesystempng) {
        final ImageIcon i = new ImageIcon(datatemplatesystempng);


        final BufferedImage b = new BufferedImage(i.getIconWidth(), i.getIconHeight(), BufferedImage.TYPE_3BYTE_BGR);

        b.getGraphics().drawImage(i.getImage(), 0, 0, null);

        return b;

    }

    private static int clipColor(int i) {
        if (i < 0) {
            return 0;
        }
        if (i > 255) {
            return 255;
        }
        return i;
    }

    private BufferedImage generatePlanetImage() {
        final int size = 512;



        final BufferedImage background = new BufferedImage(size, size, BufferedImage.TYPE_4BYTE_ABGR);


        final BufferedImage afilter = toBufferedImage("data/matms.png");
        final BufferedImage bfilter = toBufferedImage("data/mcuts.png");



        Graphics2D g = (Graphics2D) background.getGraphics();
        final Raster data = background.getData();
        final DataBuffer dataBuffer = data.getDataBuffer();

        final DataBuffer aBuffer = afilter.getData().getDataBuffer();
        final DataBuffer bBuffer = bfilter.getData().getDataBuffer();

        int overcastLevel = (int) (overcastCoverage * 255.0);
        int landLevel = (int) (landCoverage * 255.0);

        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                int sized = size / 2;
                double a = Math.atan2((x - sized), (y - sized)) * 16.0;

                double dist = Math.sqrt((double) ((y - sized) * (y - sized) + (x - sized) * (x - sized)));

                int waterNoise = FastNoise.noise(((x * turbolence + seed) / (32.0)), ((y * turbolence + seed) / (24.0)), 8);
                int landNoise = FastNoise.noise(((x * turbolence + 2 * seed) / (128.0)), ((y * turbolence + 2 * seed) / (128.0)), 8);

                int atmNoise = FastNoise.noise(((x * turbolence / 2.0 - seed) / (64.0)), ((y * turbolence / 2.0 - seed) / (32.0)), 8);

                final int pos = (x + y * size) * 3;

                int atmMask = aBuffer.getElem(pos);
                int planetMask = bBuffer.getElem(pos);

                int atmRed = clipColor(bodyAtmos.getRed());
                int atmGreen = clipColor(bodyAtmos.getGreen());
                int atmBlue = clipColor(bodyAtmos.getBlue());

                if (atmNoise < overcastLevel) {

                    atmRed = clipColor(bodyOvercast.getRed());
                    atmGreen = clipColor(bodyOvercast.getGreen());
                    atmBlue = clipColor(bodyOvercast.getBlue());
                }

                int waterRed = clipColor(bodyPrimary.getRed() + waterNoise / 4);
                int waterGreen = clipColor(bodyPrimary.getGreen() + waterNoise / 4);
                int waterBlue = clipColor(bodyPrimary.getBlue() + waterNoise / 4);

                int landRed = clipColor(bodySecondary.getRed() + landNoise);
                int landGreen = clipColor(bodySecondary.getGreen() + landNoise);
                int landBlue = clipColor(bodySecondary.getBlue() + landNoise);

                /*
                 atmRed = (atmRed * atmMask) / 255;
                 atmGreen = (atmGreen * atmMask) / 255;
                 atmBlue = (atmBlue * atmMask) / 255;
                 */
                waterRed = waterRed * planetMask / 255;
                waterGreen = waterGreen * planetMask / 255;
                waterBlue = waterBlue * planetMask / 255;

                landRed = landRed * planetMask / 255;
                landGreen = landGreen * planetMask / 255;
                landBlue = landBlue * planetMask / 255;


                int planetRed = waterRed;
                int planetGreen = waterGreen;
                int planetBlue = waterBlue;
                if (landNoise < landLevel) {
                    planetRed = landRed;
                    planetGreen = landGreen;
                    planetBlue = landBlue;

                }


                double w = atmMask;
                double w1 = planetMask;

                if (atmNoise < overcastLevel && planetMask > 0) {
                    w = planetMask * 4;
                    w1 = planetMask;
                }
                if (planetMask == 0) {
                    if (atmNoise < overcastLevel) {
                        w1 = 64;
                    } else {
                        w1 = 128;
                    }
                }


                planetRed = (int) ((atmRed * w + planetRed * w1) / (w + w1));
                planetGreen = (int) ((atmGreen * w + planetGreen * w1) / (w + w1));
                planetBlue = (int) ((atmBlue * w + planetBlue * w1) / (w + w1));

                /*
                 planetRed = atmRed;
                 planetGreen = atmGreen;
                 planetBlue = atmBlue;
                 */
                /*
                 red = red1;
                 blue = blue1;
                 green = green1;
                 */

                int posw = (x + y * size) * 4;
                dataBuffer.setElem(posw, 255);

                if (planetMask == 0) {
                    dataBuffer.setElem(posw, atmMask);
                }

                dataBuffer.setElem(posw + 1, planetBlue);
                dataBuffer.setElem(posw + 2, planetGreen);
                dataBuffer.setElem(posw + 3, planetRed);

            }
        }

        background.setData(data);
        return background;

    }

    private int clipColor(double d) {
        return clipColor((int) d);
    }

    public String toHumanString() {
        StringBuffer text = new StringBuffer();

        text.append("Classification: " + type + "\n");
        text.append("Colonies: \n");

        for (Colony c : colonies) {
            text.append("  " + c.race.trueName + "(" + c.controller.trueName + "): " + c.populationSize + "b\n");

        }

        text.append("Constructions: \n");
        text.append("- Power Production: " + getTotalPower() + "\n");

        for (Construction c : buildings) {
            if (c.type == ConstructionType.POWER) {
                text.append("  Power plants: " + c.scale + "\n");
            }
            if (c.type == ConstructionType.SOLAR) {
                text.append("  Solarsat dyson spheres: " + c.scale + "\n");
            }
        }

        for (Construction c : buildings) {
            if (c.type == ConstructionType.MINE) {
                text.append("- Mines: " + c.scale + "\n");
            }
        }

        text.append("- Factories: \n");

        for (Construction c : buildings) {
            if (c.type == ConstructionType.ASSEMBLY) {
                text.append("  " + c.produced.trueName + " factory, size " + c.scale + "\n");
            }
        }


        return text.toString();
    }

    private int getTotalPower() {
        int ret = 0;
        for (Construction c : buildings) {
            if (c.type == ConstructionType.POWER) {
                ret += c.scale;
            }
            if (c.type == ConstructionType.SOLAR) {
                ret += c.scale;
            }
        }
        return ret;
    }
}

