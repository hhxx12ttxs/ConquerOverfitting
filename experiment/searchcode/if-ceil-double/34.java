package org.esa.beam.smos.ee2netcdf;

import com.vividsolutions.jts.geom.*;
import com.vividsolutions.jts.geom.Polygon;
import org.esa.beam.dataio.smos.DggFile;
import org.esa.beam.dataio.smos.DggUtils;
import org.esa.beam.dataio.smos.ProductFile;
import org.esa.beam.dataio.smos.SmosProductReader;
import org.esa.beam.framework.dataio.ProductIO;
import org.esa.beam.framework.dataio.ProductSubsetDef;
import org.esa.beam.framework.datamodel.GeoCoding;
import org.esa.beam.framework.datamodel.GeoPos;
import org.esa.beam.framework.datamodel.PixelPos;
import org.esa.beam.framework.datamodel.Product;
import org.esa.beam.framework.gpf.OperatorException;
import org.esa.beam.framework.gpf.OperatorSpi;
import org.esa.beam.framework.gpf.annotations.OperatorMetadata;
import org.esa.beam.util.io.FileUtils;

import java.awt.*;
import java.awt.geom.Area;
import java.awt.geom.PathIterator;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TreeSet;

@SuppressWarnings("MismatchedReadAndWriteOfArray")
@OperatorMetadata(
        alias = EEToNetCDFExporterOp.ALIAS,
        version = "1.0",
        authors = "Tom Block",
        copyright = "(c) 2013, 2014 by Brockmann Consult",
        description = "Converts SMOS EE Products to NetCDF format.",
        autoWriteDisabled = true)
public class EEToNetCDFExporterOp extends AbstractNetCDFExporterOp {

    public static final String ALIAS = "SmosEE2NetCDF";

    @Override
    public void initialize() throws OperatorException {
        setDummyTargetProduct();

        ExporterUtils.assertTargetDirectoryExists(targetDirectory);

        if (sourceProducts != null) {
            for (Product sourceProduct : sourceProducts) {
                exportProduct(sourceProduct);
            }
        }

        if (sourceProductPaths != null) {
            final TreeSet<File> sourceFileSet = ExporterUtils.createInputFileSet(sourceProductPaths);

            for (File inputFile : sourceFileSet) {
                exportFile(inputFile);
            }
        }
    }

    public static File getOutputFile(File dblFile, File targetDirectory) {
        File outFile = new File(targetDirectory, dblFile.getName());
        outFile = FileUtils.exchangeExtension(outFile, ".nc");
        return outFile;
    }

    // package access for testing only tb 2013-03-26
    static MultiPolygon convertToPolygon(Area dataArea) {
        final PathIterator pathIterator = dataArea.getPathIterator(null);
        final ArrayList<double[]> coordList = new ArrayList<>();
        final ArrayList<Polygon> polygonList = new ArrayList<>();
        final GeometryFactory geometryFactory = new GeometryFactory();

        while (!pathIterator.isDone()) {
            final double[] coords = new double[6];
            final int segType = pathIterator.currentSegment(coords);
            if (segType == PathIterator.SEG_CLOSE) {
                coordList.add(coordList.get(0));
                final Coordinate[] coordinates = convert(coordList);

                final Polygon polygon = geometryFactory.createPolygon(geometryFactory.createLinearRing(coordinates),
                        null);
                polygonList.add(polygon);

                coordList.clear();
            } else {
                coordList.add(coords);
            }
            pathIterator.next();
        }

        final Polygon[] polygons = polygonList.toArray(new Polygon[polygonList.size()]);
        return new MultiPolygon(polygons, geometryFactory);
    }

    // package access for testing tb 2013-03-26
    static Coordinate[] convert(ArrayList<double[]> coordList) {
        final Coordinate[] coordinates = new Coordinate[coordList.size()];
        for (int i = 0; i < coordinates.length; i++) {
            final double[] coord = coordList.get(i);
            coordinates[i] = new Coordinate(coord[0], coord[1]);
        }
        return coordinates;
    }

    // package access for testing only tb 2013-03-26
    static Rectangle getDataBoundingRect(Product sourceProduct, Geometry dataArea) throws IOException {
        final GeoCoding geoCoding = sourceProduct.getGeoCoding();
        final GeoPos geoPos = new GeoPos(0.f, 0.f);
        final PixelPos pixelPos = new PixelPos(0.f, 0.f);
        double min_x = Integer.MAX_VALUE;
        double max_x = Integer.MIN_VALUE;
        double min_y = Integer.MAX_VALUE;
        double max_y = Integer.MIN_VALUE;

        final Coordinate[] coordinates = dataArea.getCoordinates();
        for (final Coordinate coordinate : coordinates) {
            geoPos.setLocation((float) coordinate.y, (float) coordinate.x);
            geoCoding.getPixelPos(geoPos, pixelPos);
            double ceil = Math.ceil(pixelPos.x);
            if (ceil > max_x) {
                max_x = ceil;
            }

            double floor = Math.floor(pixelPos.x);
            if (floor < min_x) {
                min_x = floor;
            }

            ceil = Math.ceil(pixelPos.y);
            if (ceil > max_y) {
                max_y = ceil;
            }

            floor = Math.floor(pixelPos.y);
            if (floor < min_y) {
                min_y = floor;
            }
        }

        return new Rectangle((int) min_x, (int) min_y, (int) (max_x - min_x), (int) (max_y - min_y));
    }

    // package access for testing only - tb 2013-03-27
    static ProductSubsetDef createSubsetDef(Rectangle rectangle) {
        final ProductSubsetDef subsetDef = new ProductSubsetDef();
        subsetDef.setRegion(rectangle);
        return subsetDef;
    }

    private void exportFile(File inputFile) {
        Product product = null;
        try {
            product = ProductIO.readProduct(inputFile);
            if (product != null) {
                final String productType = product.getProductType();
                if (productType.matches(ExportParameter.PRODUCT_TYPE_REGEX)) {
                    exportProduct(product);
                } else {
                    getLogger().info("Unable to convert file: " + inputFile.getAbsolutePath());
                    getLogger().info("Unsupported product of type: " + productType);
                }
            } else {
                getLogger().warning("Unable to open file: " + inputFile.getAbsolutePath());
            }
        } catch (Exception e) {
            getLogger().severe("Failed to convert file: " + inputFile.getAbsolutePath());
            getLogger().severe(e.getMessage());
        } finally {
            if (product != null) {
                product.dispose();
            }
        }
    }

    private void exportProduct(Product sourceProduct) {
        try {
            final SmosProductReader productReader = (SmosProductReader) sourceProduct.getProductReader();
            final ProductFile productFile = productReader.getProductFile();

            if (productFile instanceof DggFile) {
                final Area dataArea = DggUtils.computeArea(((DggFile) productFile).getGridPointList());
                Geometry polygon = convertToPolygon(dataArea);

                if (region != null) {
                    polygon = region.intersection(polygon);
                }

                if (polygon.isEmpty()) {
                    getLogger().info("No geometric intersection: " + sourceProduct.getFileLocation());
                    return;
                }

                final Rectangle x_y_subset = getDataBoundingRect(sourceProduct, polygon);
                final ProductSubsetDef subsetDef = createSubsetDef(x_y_subset);
                final Product subset = sourceProduct.createSubset(subsetDef, "", "");

                final File outFile = getOutputFile(productFile.getDataFile(), targetDirectory);
                if (outFile.isFile() && overwriteTarget) {
                    if (!outFile.delete()) {
                        throw new IOException(
                                "Unable to delete already existing product: " + outFile.getAbsolutePath());
                    }
                }
                if (!outFile.createNewFile()) {
                    throw new IOException("Unable to create target product: " + outFile.getAbsolutePath());
                }

                ProductIO.writeProduct(subset, outFile, "NetCDF4-CF", false);
                getLogger().info("Successfully converted: " + sourceProduct.getFileLocation());
            } else {
                getLogger().warning("Cannot convert file: " + sourceProduct.getFileLocation());
            }
        } catch (IOException e) {
            getLogger().severe("Failed to convert file: " + sourceProduct.getFileLocation());
            getLogger().severe(e.getMessage());
        }
    }

    public static class Spi extends OperatorSpi {

        public Spi() {
            super(EEToNetCDFExporterOp.class);
        }
    }
}

