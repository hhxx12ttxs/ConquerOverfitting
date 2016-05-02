import at.fhjoanneum.eht.gui.ROILayerDTO;
import at.fhjoanneum.eht.params.OP_SEMIAUTO;
import at.mug.iqm.api.gui.roi.EllipseROI;
import at.mug.iqm.api.model.ImageModel;
import at.mug.iqm.api.model.IqmDataBox;
import at.mug.iqm.api.model.TableModel;
import at.mug.iqm.api.operator.*;
import org.apache.commons.math3.stat.descriptive.DescriptiveStatistics;

import javax.media.jai.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.image.*;
import java.io.IOException;
import java.util.*;
import java.util.List;

import static org.junit.Assert.assertTrue;

/*
 * #%L
 * Project: IQM - Standard Image Operator Bundle
 * File: IQMTestUtils.java
 *
 * $Id: IqmOpStatistics.java 344 2014-05-15 09:40:17Z jkleinowitz $
 * $HeadURL: http://svn.code.sf.net/p/iqm/code-0/trunk/iqm/application/iqm-img-op-bundle/src/main/java/at/mug/iqm/img/bundle/op/IqmOpStatistics.java $
 *
 * This file is part of IQM, hereinafter referred to as "this program".
 * %%
 * Copyright (C) 2009 - 2014 Helmut Ahammer, Philipp Kainz
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #L%
 */

/**
 * Utility methods for unit tests
 */
public class IQMTestUtils {
    public static void printMatrix(int[][] m) {
        try {
            int rows = m.length;
            int columns = m[0].length;
            String str = "|\t";

            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < columns; j++) {
                    str += m[i][j] + "\t";
                }

                System.out.println(str + "|");
                str = "|\t";
            }

        } catch (Exception e) {
            System.out.println("Matrix is empty!!");
        }
    }

    /**
     * Creates a random planar image with the desired size and number of bands.
     * Source: http://www.lac.inpe.br/JIPCookbook/index.jsp
     *
     * @param width
     * @param height
     * @param bands
     * @return
     */
    public static PlanarImage createRandomImage(int width, int height, int bands) {
        //To prevent warning in console
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");

        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE, width, height, bands);
        ColorModel cm = TiledImage.createColorModel(sampleModel);

        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, cm);
        WritableRaster wr = tiledImage.getWritableTile(0, 0);
        for (int b = 0; b < bands; b++) {
            for (int h = 0; h < height / 32; h++) {
                for (int w = 0; w < width / 32; w++) {

                    int[] fill = new int[32 * 32];
                    Arrays.fill(fill, (int) (Math.random() * 256));
                    wr.setSamples(w * 32, h * 32, 32, 32, b, fill);
                }
            }
        }

        //JAI.create("filestore", tiledImage, "testimage_color.png", "PNG");
        return tiledImage;
    }

    /**
     * Creates a sinus image with the desired size and number of bands.
     * Source: http://www.lac.inpe.br/JIPCookbook/index.jsp
     *
     * @param width
     * @param height
     * @param bands
     * @return
     */
    public static PlanarImage createSinusImage(int width, int height, int bands) {
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE, width, height, bands);
        ColorModel cm = TiledImage.createColorModel(sampleModel);
        TiledImage tiledImage = new TiledImage(0, 0, width, height, 0, 0, sampleModel, cm);
        WritableRaster wr = tiledImage.getWritableTile(0, 0);

        for (int h = 0; h < height / 32; h++) {
            for (int w = 0; w < width / 32; w++) {
                for (int b = 0; b < bands; b++) {
                    int[] fill = new int[32 * 32];
                    int value = 127 + (int) (128 * Math.sin(w) * Math.sin(h));
                    Arrays.fill(fill, value);
                    wr.setSamples(w * 32, h * 32, 32, 32, b, fill);
                }
            }
        }
        return tiledImage;
    }

    /**
     * Creates a new random image and adds it to the work package
     *
     * @param wp
     */
    public static void addNewRandomImage(WorkPackage wp) {
        int width = 480;
        int height = 320;
        int bands = 3;
        String imgName = height + "*" + width + "*" + bands;
        PlanarImage pi = createRandomImage(width, height, bands);
        updateWorkPackage(wp, pi, imgName);

        //JAI.create("filestore", pi, "testimage_color.png", "PNG");
    }

    /**
     * Loads an image from classpath and adds it to the work package
     *
     * @param path
     * @param wp
     * @throws IOException
     */
    public static void addLoadedImage(String path, WorkPackage wp) throws IOException {
        PlanarImage pi = loadImage(path);
        updateWorkPackage(wp, pi, path);
    }

    /**
     * Creates a new sinus image and adds it to the work package
     *
     * @param wp
     */
    public static void addNewSinusImage(WorkPackage wp) {
        int width = 500;
        int height = 500;
        int bands = 3;
        String imgName = height + "x" + width + "x" + bands;
        PlanarImage pi = createSinusImage(width, height, bands);
        updateWorkPackage(wp, pi, imgName);

        //JAI.create("filestore", pi, imgName + ".png", "PNG");
    }

    /**
     * Creates a new ImageModel and adds it to the work package
     *
     * @param wp
     * @param pi
     * @param imgName
     */
    private static void updateWorkPackage(WorkPackage wp, PlanarImage pi, String imgName) {
        ImageModel im = new ImageModel(pi);
        im.setModelName(imgName);
        im.setFileName(imgName + ".png");
        IqmDataBox iqmDataBox = new IqmDataBox(im);
        Vector<Object> vector = new Vector<Object>();
        vector.add(iqmDataBox);
        wp.updateSources(vector);
    }

    /**
     * Loads an image from the classpath
     *
     * @param path
     * @return
     * @throws IOException
     */
    public static PlanarImage loadImage(String path) throws IOException {
        System.setProperty("com.sun.media.jai.disableMediaLib", "true");
        RenderedImage renderedImage = javax.imageio.ImageIO.read(IQMTestUtils.class.getResourceAsStream(path));
        return PlanarImage.wrapRenderedImage(renderedImage);
    }

    /**
     * Executes an operator several times and prints a number of descriptive statistics.
     *
     * @param op
     * @param wp
     * @param times
     * @throws Exception
     */
    public static void benchMarkOperator(IOperator op, WorkPackage wp, int times) throws Exception {
        benchMarkOperator(op, Arrays.asList(wp), times);
    }

    /**
     * Run the operator n times with each work package
     *
     * @param op
     * @param wpList
     * @param times
     * @throws Exception
     */
    public static void benchMarkOperator(IOperator op, List<WorkPackage> wpList, int times) throws Exception {
        long testBegin = System.currentTimeMillis();
        List<DescriptiveStatistics> statList = new ArrayList<>();

        for (WorkPackage wp : wpList) {
            double[] durations = new double[times];
            for (int i = 0; i < times; i++) {
                durations[i] = benchMarkOperator(op, wp);
            }
            statList.add(calcStatistics(durations));
        }
        long testDuration = System.currentTimeMillis() - testBegin;
        System.out.println("Total test duration: " + testDuration);
        for (DescriptiveStatistics stats : statList) {
            int index = statList.indexOf(stats);
            System.out.println("-----------------Exectution summary for " + index + " executed " + times + " times.");
            System.out.println(getParamsAsString(wpList.get(index)));
            printStatResults(stats);

        }
        if (wpList.size() > 1) {
            printRanking(statList, wpList);
        }


    }
    public static void benchMarkUntilConvergence(IOperator op, WorkPackage wp) throws Exception {
        benchMarkUntilConvergence(op, Arrays.asList(wp));
    }

    public static void benchMarkUntilConvergence(IOperator op, List<WorkPackage> wpList) throws Exception {
        long testBegin = System.currentTimeMillis();
        List<DescriptiveStatistics> statList = new ArrayList<>();

        double limit = 0.1;

        for (WorkPackage wp : wpList) {

            DescriptiveStatistics statistics = new DescriptiveStatistics(20);

            int currentIt = 0;
            int maxIterations = 100;

            double varCoeff = 1;

            while (varCoeff > limit) {
                currentIt++;
                if (currentIt > maxIterations) {
                    System.out.println("-----Iteration limit of " + maxIterations + " reached, stopping text");
                    break;
                }
                statistics.addValue(benchMarkOperator(op, wp));

                double stdev = (statistics.getStandardDeviation() > 0 && currentIt > statistics.getWindowSize()/2)?statistics.getStandardDeviation() : statistics.getMean();

                varCoeff = stdev / statistics.getMean();
                System.out.println("-----Current variation coefficient: " + varCoeff);
            }
            System.out.println("Iterations needed: " + currentIt);
            statList.add(statistics);
        }



        long testDuration = System.currentTimeMillis() - testBegin;
        System.out.println("Total test duration: " + testDuration);
        for (DescriptiveStatistics stats : statList) {
            int index = statList.indexOf(stats);
            System.out.println("-----------------Exectution summary for " + index);
            System.out.println(getParamsAsString(wpList.get(index)));
            printStatResults(stats);

        }
    }

    private static String getParamsAsString(WorkPackage workPackage) {
        ParameterBlockIQM pb = workPackage.getParameters();
        String windowSize = "W: " + pb.getIntParameter(OP_SEMIAUTO.WINDOW_SIZE.pbKey());
        String stepSize = "S: " + pb.getIntParameter(OP_SEMIAUTO.STEP.pbKey());
        String packageSize = "P: " + pb.getIntParameter(OP_SEMIAUTO.PACKAGE_SIZE.pbKey());
        return windowSize + " " + stepSize + packageSize;
    }

    private static List<DescriptiveStatistics> rankList(List<DescriptiveStatistics> statList, Comparator<DescriptiveStatistics> comparator) {
        List<DescriptiveStatistics> sortedList = new ArrayList<>(statList);
        Collections.sort(sortedList, comparator);
        return sortedList;
    }


    private static void printRanking(List<DescriptiveStatistics> statList, List<WorkPackage> wpList) {
        List<DescriptiveStatistics> sortedList = rankList(statList, new Comparator<DescriptiveStatistics>() {
            @Override
            public int compare(DescriptiveStatistics o1, DescriptiveStatistics o2) {
                Double mean1 = o1.getMean();
                Double mean2 = o2.getMean();
                return mean1.compareTo(mean2);
            }
        });
        System.out.println("Ranking by Mean");
        for (int i = 0; i < sortedList.size(); i++) {
            DescriptiveStatistics stats = sortedList.get(i);
            int index = statList.indexOf(stats);
            double val = stats.getMean();
            System.out.println(index + " Mean: " + val + "Params: " + getParamsAsString(wpList.get(index)));
        }
        sortedList = rankList(statList, new Comparator<DescriptiveStatistics>() {
            @Override
            public int compare(DescriptiveStatistics o1, DescriptiveStatistics o2) {
                Double val1 = o1.getPercentile(50);
                Double val2 = o2.getPercentile(50);
                return val1.compareTo(val2);
            }
        });
        System.out.println("Ranking by Median");
        for (int i = 0; i < sortedList.size(); i++) {
            DescriptiveStatistics stats = sortedList.get(i);
            int index = statList.indexOf(stats);
            double val = stats.getPercentile(50);
            System.out.println(index + " Median: " + val + "Params: " + getParamsAsString(wpList.get(index)));
        }
    }

    public static long benchMarkOperator(IOperator op, WorkPackage wp) throws Exception {
        addNewRandomImage(wp);
        long startTime = System.currentTimeMillis();
        op.run(wp);
        long endTime = System.currentTimeMillis();
        return endTime - startTime;
    }

    private static void printStatResults(DescriptiveStatistics stats) {
        System.out.println("Mean: " + stats.getMean());
        System.out.println("Median: " + stats.getPercentile(50));
        System.out.println("Stdev: " + stats.getStandardDeviation());
        System.out.println("Max: " + stats.getMax());
        System.out.println("Min: " + stats.getMin());
    }

    private static DescriptiveStatistics calcStatistics(double[] durations) {
        return new DescriptiveStatistics(durations);
    }

    /**
     * Utility-method for printing the TableModel-results of an operator to the console
     *
     * @param res
     */
    public static void printTableResults(IResult res) {
        ArrayList<IqmDataBox> dataBoxList = res.listTableResults();
        for (IqmDataBox dataBox : dataBoxList) {
            TableModel tm = dataBox.getTableModel();
            printTable(tm);
        }
    }

    /**
     * Prints a TableModel to the console
     *
     * @param tm
     */
    public static void printTable(TableModel tm) {
        Vector dataRows = tm.getDataVector();

        assertTrue(dataRows.size() > 0); //at least one row

        for (int i = 0; i < tm.getColumnCount(); i++) {
            String colName = tm.getColumnName(i);
            StringBuilder sb = new StringBuilder(colName);
            sb.append(": ");
            for (Object o : dataRows) {
                Vector row = (Vector) o;
                sb.append(row.get(i));
                sb.append(", ");
            }
            System.out.println(sb);
        }
    }

    public static ROILayerDTO createNewROILayerDTO(String name, Color col, List<ROIShape> shapeList) {
        return new ROILayerDTO(name, col, name, shapeList);
    }

    public static List<ROILayerDTO> createRandomROILayerList(Dimension dim, int layers, int shapesPerLayer) {
        return null;
    }

    public static List<ROILayerDTO> createTestROILayerList() {
        List<ROILayerDTO> layerList = new ArrayList<>();
        ROIShape roiShape1 = new ROIShape(new Rectangle(25, 25, 100, 100));
        ROIShape roiShape2 = new EllipseROI(new Ellipse2D.Double(40, 50, 10, 20));
        layerList.add(createNewROILayerDTO("layer 1", Color.RED, Arrays.asList(roiShape1, roiShape2)));

        ROIShape roiShape3 = new EllipseROI(new Rectangle(200, 100, 60, 60));
        ROIShape roiShape4 = new EllipseROI(new Ellipse2D.Double(200, 200, 40, 50));
        layerList.add(createNewROILayerDTO("layer 2", Color.GREEN, Arrays.asList(roiShape3, roiShape4)));

        ROIShape roiShape5 = new EllipseROI(new Ellipse2D.Double(40, 300, 200, 100));
        ROIShape roiShape6 = new EllipseROI(new Ellipse2D.Double(300, 50, 100, 200));
        layerList.add(createNewROILayerDTO("layer 3", Color.BLUE, Arrays.asList(roiShape5, roiShape6)));
        return layerList;
    }
}

