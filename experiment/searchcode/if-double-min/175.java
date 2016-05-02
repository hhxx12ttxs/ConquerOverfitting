/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
<<<<<<< HEAD
 *    (C) 2007-2008, Open Source Geospatial Foundation (OSGeo)
=======
 *    (C) 2002-2011, Open Source Geospatial Foundation (OSGeo)
>>>>>>> 76aa07461566a5976980e6696204781271955163
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
<<<<<<< HEAD
package org.geotools.coverage.io.util;

import it.geosolutions.imageio.stream.input.FileImageInputStreamExt;
import it.geosolutions.imageio.stream.input.FileImageInputStreamExtImpl;
import it.geosolutions.imageio.stream.input.URIImageInputStream;
import it.geosolutions.imageio.stream.input.URIImageInputStreamImpl;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.renderable.ParameterBlock;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;
import javax.measure.quantity.Quantity;
import javax.measure.unit.SI;
import javax.measure.unit.Unit;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GridCoverage2D;
import org.geotools.coverage.grid.GridCoverageFactory;
import org.geotools.coverage.grid.ViewType;
import org.geotools.coverage.grid.io.AbstractGridCoverage2DReader;
import org.geotools.coverage.grid.io.OverviewPolicy;
import org.geotools.data.DataSourceException;
import org.geotools.factory.Hints;
import org.geotools.feature.NameImpl;
import org.geotools.geometry.Envelope2D;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.metadata.iso.citation.Citations;
import org.geotools.referencing.CRS;
import org.geotools.referencing.NamedIdentifier;
import org.geotools.referencing.crs.DefaultGeographicCRS;
import org.geotools.referencing.cs.DefaultCartesianCS;
import org.geotools.referencing.datum.DefaultEllipsoid;
import org.geotools.referencing.datum.DefaultGeodeticDatum;
import org.geotools.referencing.datum.DefaultPrimeMeridian;
import org.geotools.referencing.factory.ReferencingFactoryContainer;
import org.geotools.referencing.operation.DefaultMathTransformFactory;
import org.geotools.referencing.operation.builder.GridToEnvelopeMapper;
import org.geotools.referencing.operation.transform.ConcatenatedTransform;
import org.geotools.referencing.operation.transform.IdentityTransform;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.resources.geometry.XRectangle2D;
import org.opengis.coverage.grid.GridCoverage;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.geometry.Envelope;
import org.opengis.parameter.ParameterValueGroup;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.ReferenceIdentifier;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.GeographicCRS;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.referencing.operation.MathTransformFactory;
import org.opengis.referencing.operation.TransformException;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * 
 * 
 * @author Daniele Romagnoli, GeoSolutions
 * 
 * 
 * 
 * 
 * @source $URL$
 */
public class Utilities {

    private final static Logger LOGGER = org.geotools.util.logging.Logging
            .getLogger(Utilities.class.toString());

    /**
     * TODO: Define a contains method which allows to know if the extent of a CoverageSlice contains a predefined extent. This would be useful to
     * know which CoverageSlice to be used to access to a specific temporal/vertical extent defined slice.
     */

    /** Caches a MathTransformFactory */
    private final static MathTransformFactory mtFactory = new DefaultMathTransformFactory();

    public static ReferenceIdentifier[] getIdentifiers(final String nameIdentifier) {
        if (nameIdentifier.equalsIgnoreCase("WGS84")) {
            final ReferenceIdentifier[] identifiers = {

            new NamedIdentifier(Citations.OGC, "WGS84"),
                    new NamedIdentifier(Citations.ORACLE, "WGS 84"),
                    new NamedIdentifier(null, "WGS_84"), new NamedIdentifier(null, "WGS 1984"),
                    new NamedIdentifier(Citations.EPSG, "WGS_1984"),
                    new NamedIdentifier(Citations.ESRI, "D_WGS_1984"),
                    new NamedIdentifier(Citations.EPSG, "World Geodetic System 1984") };
            return identifiers;
        }
        // TODO: Handle mores
        return null;
    }

    private Utilities() {

    }

    /**
     * Build a {@link DefaultGeodeticDatum} given a set of parameters.
     * 
     * @param name the datum name
     * @param equatorialRadius the equatorial radius parameter
     * @param inverseFlattening the inverse flattening parameter
     * @param unit the UoM
     * @return a properly built Datum.
     */
    public static DefaultGeodeticDatum getDefaultGeodeticDatum(final String name,
            final double equatorialRadius, final double inverseFlattening, Unit unit) {

        DefaultEllipsoid ellipsoid = DefaultEllipsoid.createFlattenedSphere(name, equatorialRadius,
                inverseFlattening, unit);
        final ReferenceIdentifier[] identifiers = Utilities.getIdentifiers(name);
        // TODO: Should I change this behavior?
        if (identifiers == null)
            throw new IllegalArgumentException("Reference Identifier not available");
        final Map<String, Object> properties = new HashMap<String, Object>(4);
        properties.put(DefaultGeodeticDatum.NAME_KEY, identifiers[0]);
        properties.put(DefaultGeodeticDatum.ALIAS_KEY, identifiers);
        DefaultGeodeticDatum datum = new DefaultGeodeticDatum(properties, ellipsoid,
                DefaultPrimeMeridian.GREENWICH);
        return datum;
    }

    /**
     * Temp utility method which allows to get the real file name from a custom input File, where "custom" means a file having a special name
     * structured as "originalFileName:imageIndex"
     */
    public static File getFileFromCustomInput(Object input) {
        final File fileCheck = (File) input;
        final String path = fileCheck.getAbsolutePath();
        final int imageSpecifierIndex = path.lastIndexOf(":");
        final File file;
        if (imageSpecifierIndex > 1 && imageSpecifierIndex > path.indexOf(":")) {
            file = new File(path.substring(0, imageSpecifierIndex));
        } else
            file = fileCheck;
        return file;
    }

    /**
     * Simple utility method which allows to build a Mercator2SP Projected CRS given the set of required parameters. It will be used by several
     * Terascan products.
     */
    @SuppressWarnings("deprecation")
    public static CoordinateReferenceSystem getMercator2SPProjectedCRS(
            final double standardParallel, final double centralMeridian, final double natOriginLat,
            GeographicCRS sourceCRS, Hints hints) throws DataSourceException {
        CoordinateReferenceSystem projectedCRS = null;

        // //
        //
        // Creating a proper projected CRS
        //
        // //
        final ReferencingFactoryContainer fg = ReferencingFactoryContainer.instance(hints);
        ParameterValueGroup params;
        try {
            params = mtFactory.getDefaultParameters("Mercator_2SP");
            params.parameter("standard_parallel_1").setValue(standardParallel);
            params.parameter("central_meridian").setValue(centralMeridian);
            params.parameter("false_northing").setValue(0);
            params.parameter("false_easting").setValue(0);
            params.parameter("latitude_of_origin").setValue(natOriginLat);

            // //
            //
            // Setting the CRS
            //
            // //

            final Map<String, String> props = new HashMap<String, String>();
            props.put("name", "Mercator CRS");
            projectedCRS = fg.createProjectedCRS(props, sourceCRS, null, params,
                    DefaultCartesianCS.PROJECTED);
        } catch (FactoryException e) {
            throw new DataSourceException(e);
        }
        return projectedCRS;
    }

    /**
     * Build a base {@link GeographicCRS} given the parameters to specify a Geodetic Datum
     */
    public static GeographicCRS getBaseCRS(final double equatorialRadius,
            final double inverseFlattening) {
        final DefaultGeodeticDatum datum = Utilities.getDefaultGeodeticDatum("WGS84",
                equatorialRadius, inverseFlattening, SI.METER);
        final GeographicCRS sourceCRS = new DefaultGeographicCRS("WGS-84", datum,
                DefaultGeographicCRS.WGS84.getCoordinateSystem());
        return sourceCRS;
    }

    /**
     * Simple method returning the value (as {@code String}) of the attribute with name {@code attributeName} from the input attributes map.
     * 
     * @param attributes the attributes map
     * @param attributeName the requested attribute
     * @return the value of the requested attribute as a {@code String}. Returns {@code null} in case of no attribute found.
     * 
     */
    public static String getAttributeValue(NamedNodeMap attributes, String attributeName) {
        String attributeValue = null;
        Node attribute = attributes.getNamedItem(attributeName);
        if (attribute != null) {
            attributeValue = attribute.getNodeValue();
        }
        return attributeValue;
    }

    /**
     * Return a {@link Unit} instance for the specified uom String.
     * 
     * @param uom
     * @return
     */
    public static Unit<? extends Quantity> parseUnit(final String uom) {
        Unit<? extends Quantity> unit = Unit.ONE;
        if (uom != null && uom.trim().length() > 0) {
            // TODO: Add more well known cases
            if (uom.equalsIgnoreCase("temp_deg_c") || uom.equalsIgnoreCase("Celsius"))
                unit = javax.measure.unit.SI.CELSIUS;
            else {
                try {
                    unit = Unit.valueOf(uom);
                } catch (IllegalArgumentException iae) {
                    if (LOGGER.isLoggable(Level.FINE)) {
                        LOGGER.log(Level.FINE, "Unable to parse the provided unit " + uom);
                    }
                }
            }
        }
        return unit;
    }

/**
     * Get a WGS84 envelope for the specified envelope. The get2D parameter
     * allows to specify if we need the returned coverage as an
     * {@code Envelope2D} or a more general {@code GeneralEnvelope} instance.
     * 
     * @param envelope
     * @param get2D
     *                if {@code true}, the requested envelope will be an
     *                instance of {@link Envelope2D}. If {@code false} it will
     *                be an instance of {@link GeneralEnvelope
     * @return a WGS84 envelope as {@link Envelope2D} in case of request for a
     *         2D WGS84 Envelope, or a {@link GeneralEnvelope} otherwise.
     * @throws FactoryException
     * @throws TransformException
     */
    public static Envelope getEnvelopeAsWGS84(final Envelope envelope, boolean get2D)
            throws FactoryException, TransformException {
        if (envelope == null)
            throw new IllegalArgumentException("Specified envelope is null");
        Envelope requestedWGS84;
        final MathTransform transformToWGS84;
        final CoordinateReferenceSystem crs = envelope.getCoordinateReferenceSystem();

        // //
        //
        // get a math transform to go to WGS84
        //
        // //
        if (!CRS.equalsIgnoreMetadata(crs, DefaultGeographicCRS.WGS84)) {
            transformToWGS84 = CRS.findMathTransform(crs, DefaultGeographicCRS.WGS84, true);
        } else {
            transformToWGS84 = IdentityTransform.create(2);
        }

        // do we need to transform the requested envelope?
        if (!transformToWGS84.isIdentity()) {
            GeneralEnvelope env = CRS.transform(transformToWGS84, envelope);
            if (get2D) {
                requestedWGS84 = new Envelope2D(env);
                ((Envelope2D) requestedWGS84)
                        .setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
            } else {
                requestedWGS84 = env;
                ((GeneralEnvelope) requestedWGS84)
                        .setCoordinateReferenceSystem(DefaultGeographicCRS.WGS84);
            }
            return requestedWGS84;

        } else {
            if (get2D)
                return new Envelope2D(envelope);
            else
                return new GeneralEnvelope(envelope);
=======
package org.geotools.imageio.unidata;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.image.BandedSampleModel;
import java.awt.image.SampleModel;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;

import org.geotools.coverage.Category;
import org.geotools.coverage.GridSampleDimension;
import org.geotools.coverage.grid.GridEnvelope2D;
import org.geotools.coverage.grid.GridGeometry2D;
import org.geotools.coverage.grid.io.DefaultDimensionDescriptor;
import org.geotools.coverage.grid.io.DimensionDescriptor;
import org.geotools.coverage.io.CoverageSource.AdditionalDomain;
import org.geotools.coverage.io.CoverageSource.DomainType;
import org.geotools.coverage.io.CoverageSource.SpatialDomain;
import org.geotools.coverage.io.CoverageSource.TemporalDomain;
import org.geotools.coverage.io.CoverageSource.VerticalDomain;
import org.geotools.coverage.io.CoverageSourceDescriptor;
import org.geotools.coverage.io.RasterLayout;
import org.geotools.coverage.io.catalog.CoverageSlice;
import org.geotools.coverage.io.catalog.CoverageSlicesCatalog;
import org.geotools.coverage.io.range.FieldType;
import org.geotools.coverage.io.range.RangeType;
import org.geotools.coverage.io.range.impl.DefaultFieldType;
import org.geotools.coverage.io.range.impl.DefaultRangeType;
import org.geotools.coverage.io.util.DateRangeComparator;
import org.geotools.coverage.io.util.DateRangeTreeSet;
import org.geotools.coverage.io.util.DoubleRangeTreeSet;
import org.geotools.coverage.io.util.NumberRangeComparator;
import org.geotools.data.DataUtilities;
import org.geotools.data.collection.ListFeatureCollection;
import org.geotools.factory.GeoTools;
import org.geotools.feature.NameImpl;
import org.geotools.gce.imagemosaic.Utils;
import org.geotools.gce.imagemosaic.catalog.index.Indexer.Coverages.Coverage;
import org.geotools.gce.imagemosaic.catalog.index.SchemaType;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.imageio.unidata.cv.CoordinateVariable;
import org.geotools.imageio.unidata.utilities.UnidataCRSUtilities;
import org.geotools.imageio.unidata.utilities.UnidataUtilities;
import org.geotools.referencing.operation.transform.ProjectiveTransform;
import org.geotools.resources.coverage.CoverageUtilities;
import org.geotools.util.DateRange;
import org.geotools.util.NumberRange;
import org.geotools.util.SimpleInternationalString;
import org.geotools.util.logging.Logging;
import org.opengis.coverage.SampleDimension;
import org.opengis.coverage.grid.GridEnvelope;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.AttributeDescriptor;
import org.opengis.feature.type.Name;
import org.opengis.geometry.BoundingBox;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.metadata.spatial.PixelOrientation;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.crs.TemporalCRS;
import org.opengis.referencing.datum.PixelInCell;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.MathTransform2D;
import org.opengis.util.InternationalString;
import org.opengis.util.ProgressListener;

import ucar.nc2.Dimension;
import ucar.nc2.Variable;
import ucar.nc2.constants.AxisType;
import ucar.nc2.dataset.CoordinateAxis;
import ucar.nc2.dataset.CoordinateSystem;
import ucar.nc2.dataset.VariableDS;

/**
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 * @todo lazy initialization
 * @todo management of data read with proper mangling
 */
public class UnidataVariableAdapter extends CoverageSourceDescriptor {

    public class UnidataSpatialDomain extends SpatialDomain {

        /** The spatial coordinate reference system */
        private CoordinateReferenceSystem coordinateReferenceSystem;

        /** The spatial referenced envelope */
        private ReferencedEnvelope referencedEnvelope;

        /** The gridGeometry of the spatial domain */
        private GridGeometry2D gridGeometry;

        public ReferencedEnvelope getReferencedEnvelope() {
            return referencedEnvelope;
        }

        public void setReferencedEnvelope(ReferencedEnvelope referencedEnvelope) {
            this.referencedEnvelope = referencedEnvelope;
        }

        public GridGeometry2D getGridGeometry() {
            return gridGeometry;
        }

        public double[] getFullResolution() {
            AffineTransform gridToCRS = (AffineTransform) gridGeometry.getGridToCRS();
            return CoverageUtilities.getResolution(gridToCRS);
        }

        public void setGridGeometry(GridGeometry2D gridGeometry) {
            this.gridGeometry = gridGeometry;
        }

        public void setCoordinateReferenceSystem(CoordinateReferenceSystem coordinateReferenceSystem) {
            this.coordinateReferenceSystem = coordinateReferenceSystem;
        }

        @Override
        public Set<? extends BoundingBox> getSpatialElements(boolean overall,
                ProgressListener listener) throws IOException {
            return Collections.singleton(referencedEnvelope);
        }

        @Override
        public CoordinateReferenceSystem getCoordinateReferenceSystem2D() {
            return coordinateReferenceSystem;
        }

        @Override
        public MathTransform2D getGridToWorldTransform(ProgressListener listener)
                throws IOException {
            return gridGeometry.getGridToCRS2D(PixelOrientation.CENTER);
        }

        @Override
        public Set<? extends RasterLayout> getRasterElements(boolean overall,
                ProgressListener listener) throws IOException {
            Rectangle bounds = gridGeometry.getGridRange2D().getBounds();
            return Collections.singleton(new RasterLayout(bounds));
        }
    }

    public class UnidataTemporalDomain extends TemporalDomain {

        /**
         * @param adaptee
         */
        UnidataTemporalDomain(CoordinateVariable<?> adaptee) {
            if(!Date.class.isAssignableFrom(adaptee.getType())){
                throw new IllegalArgumentException("Unable to wrap non temporal CoordinateVariable:"+adaptee.toString());
            }
            this.adaptee = (CoordinateVariable<Date>)adaptee;
        }

        final CoordinateVariable<Date> adaptee;

        public SortedSet<DateRange> getTemporalExtent() {
            // Getting global Extent
            Date startTime;
            try {
                startTime = adaptee.getMinimum();
                Date endTime =  adaptee.getMaximum();
                final DateRange global = new DateRange(startTime, endTime);
                final SortedSet<DateRange> globalTemporalExtent = new DateRangeTreeSet();
                globalTemporalExtent.add(global);
                return globalTemporalExtent;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public SortedSet<? extends DateRange> getTemporalElements(boolean overall,
                ProgressListener listener) throws IOException {
            if (overall) {
                
                // Getting overall Extent
                final SortedSet<DateRange> extent = new TreeSet<DateRange>(new DateRangeComparator());
                for(Date dd:adaptee.read()){
                    extent.add(new DateRange(dd,dd));
                }
                return extent;
            } else {
                return getTemporalExtent();
            }
        }

        @Override
        public CoordinateReferenceSystem getCoordinateReferenceSystem() {
            return adaptee.getCoordinateReferenceSystem();
        }
    }

    public class UnidataVerticalDomain extends VerticalDomain {

        final CoordinateVariable<? extends Number> adaptee;

        /**
         * @param cv
         */
        UnidataVerticalDomain(CoordinateVariable<?> cv) {
            if(!Number.class.isAssignableFrom(cv.getType())){
                throw new IllegalArgumentException("Unable to wrap a non Number CoordinateVariable:"+cv.toString());
            }
            this.adaptee = (CoordinateVariable<? extends Number>)cv;
        }


        public SortedSet<NumberRange<Double>> getVerticalExtent() {
            // Getting global Extent
            final CoordinateVariable<? extends Number> verticalDimension=this.adaptee;
            NumberRange<Double> global;
            try {
                global = NumberRange.create(
                        verticalDimension.getMinimum().doubleValue(), 
                        verticalDimension.getMaximum().doubleValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            final SortedSet<NumberRange<Double>> globalVerticalExtent = new DoubleRangeTreeSet();
            globalVerticalExtent.add(global);
            return globalVerticalExtent;
        }

        @Override
        public SortedSet<? extends NumberRange<Double>> getVerticalElements(boolean overall,
                ProgressListener listener) throws IOException {

            if (overall) {
             // Getting overall Extent
                final SortedSet<NumberRange<Double>> extent = new TreeSet<NumberRange<Double>>(new NumberRangeComparator());
                for(Number vv:adaptee.read()){
                    final double doubleValue = vv.doubleValue();
                    extent.add(NumberRange.create(doubleValue,doubleValue));
                }
                return extent;
            } else {
                return getVerticalExtent();
            }
        }

        @Override
        public CoordinateReferenceSystem getCoordinateReferenceSystem() {
            return adaptee.getCoordinateReferenceSystem();
        }
    }

    /**
     * 
     * @author User
     * TODO improve support for this
     */
    public class UnidataAdditionalDomain extends AdditionalDomain {

        /** The detailed domain extent */
        private final Set<Object> domainExtent = new TreeSet<Object>();
        
        /** The merged domain extent */
        private final Set<Object> globalDomainExtent = new TreeSet<Object>(new Comparator<Object>() {
          private NumberRangeComparator  numberRangeComparator = new NumberRangeComparator();
          private DateRangeComparator    dateRangeComparator = new DateRangeComparator();

          public int compare(Object o1, Object o2) {
            // assume that o1 and o2 are both not null
            boolean o1IsDateRange = true;
            boolean o2IsDateRange = true;

            if (o1 instanceof NumberRange) {
              o1IsDateRange = false;
            }
            else if (!(o1 instanceof DateRange)) {
              throw new ClassCastException(o1.getClass() + " is not an known range type");
            }

            if (o2 instanceof NumberRange) {
              o2IsDateRange = false;
            }
            else if (!(o2 instanceof DateRange)) {
              throw new ClassCastException(o2.getClass() + " is not an known range type");
            }

            if (o1IsDateRange && o2IsDateRange) {
              return dateRangeComparator.compare((DateRange) o1, (DateRange) o2);
            }
            else if (!o1IsDateRange && !o2IsDateRange) {
              return numberRangeComparator.compare((NumberRange<?>) o1, (NumberRange<?>) o2);
            }

            throw new ClassCastException("Incompatible range types: " + o1.getClass() + " is not the same as " + o2.getClass());
          }

          public boolean equals(Object o) {
            return false;
          }
        });

        /** The domain name */
        private final String name;
        
        private final DomainType type;
        
        final CoordinateVariable<?> adaptee;

        /**
         * @param domainExtent
         * @param globalDomainExtent
         * @param name
         * @param type
         * @param adaptee
         * TODO missing support for Range
         * TODO missing support for String domains
         * @throws IOException 
         */
        UnidataAdditionalDomain(CoordinateVariable<?> adaptee) throws IOException {
            this.adaptee = adaptee;
            name=adaptee.getName();
            
            // type
            Class<?> type=adaptee.getType();
            if(Date.class.isAssignableFrom(type)){
                this.type=DomainType.DATE;
                
                // global domain
                globalDomainExtent.add(new DateRange(
                        (Date)adaptee.getMinimum(),
                        (Date)adaptee.getMaximum())); 
            } else if(Number.class.isAssignableFrom(type)){
                this.type=DomainType.NUMBER;

                // global domain
                globalDomainExtent.add(new NumberRange<Double>(
                        Double.class,
                        ((Number)adaptee.getMinimum()).doubleValue(),
                        ((Number)adaptee.getMaximum()).doubleValue()));                
            } else {
                throw new UnsupportedOperationException("Unsupported CoordinateVariable:"+adaptee.toString());
            }
            
            // domain
            domainExtent.addAll(adaptee.read());
        }

        @Override
        public Set<Object> getElements(boolean overall, ProgressListener listener)
                throws IOException {
            if (overall) {
                return globalDomainExtent;
            } else {
                return domainExtent;
            }
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public DomainType getType() {
            return type;
        }

        public Set<Object> getDomainExtent() {
            return domainExtent;
        }
        
    }
    
    final VariableDS variableDS;

    private ucar.nc2.dataset.CoordinateSystem coordinateSystem;

    private UnidataImageReader reader;

    private int numBands;

    private int rank;

    private SampleModel sampleModel;

    private int numberOfSlices;

    private int width;

    private int height;

    private CoordinateReferenceSystem coordinateReferenceSystem;

    private int[] shape;

    private Name coverageName;

    private SimpleFeatureType indexSchema;

    private final static java.util.logging.Logger LOGGER = Logging.getLogger(UnidataVariableAdapter.class);

    /** Usual schema are the_geom, imageIndex, so the first attribute (time or elevation) will have index = 2 */
    private static final int FIRST_ATTRIBUTE_INDEX = 2;

    /**
     * Extracts the compound {@link CoordinateReferenceSystem} from the unidata variable.
     * 
     * @return the compound {@link CoordinateReferenceSystem}.
     * @throws Exception 
     */
    private void init() throws Exception {
        
        // initialize the various domains
        initSpatialElements();
        
        // initialize rank and number of 2D slices
        initRange();
        
        // initialize info about slice
        initSlicesInfo();
    }

    /**
     * @throws Exception 
     * 
     */
    private void initSlicesInfo() throws Exception {
        // get the length of the coverageDescriptorsCache in each dimension
        shape = variableDS.getShape();
        switch (shape.length) {
        case 2:
            numberOfSlices = 1;
            break;
        case 3:
            numberOfSlices = shape[0];
            break;
        case 4:
            numberOfSlices = 0 + shape[0] * shape[1];
            break;
        default:
            if (LOGGER.isLoggable(Level.WARNING))
                LOGGER.warning("Ignoring variable: " + getName()
                        + " with shape length: " + shape.length);
            
        }

        
    }

    /**
     * @throws IOException 
     * 
     */
    private void initSpatialElements() throws Exception {
        
        final List<DimensionDescriptor> dimensions = new ArrayList<DimensionDescriptor>();
        List<CoordinateVariable<?>> otherAxes = initCRS(dimensions);

        // SPATIAL DIMENSIONS
        initSpatialDomain();

        // ADDITIONAL DOMAINS
        addAdditionalDomain(otherAxes, dimensions);
        
        setDimensionDescriptors(dimensions);
        if (reader.ancillaryFileManager.isImposedSchema()) {
            updateDimensions(getDimensionDescriptors());
        }
    }

    /**
     * Update the dimensions to attributes mapping for this variable if needed.
     * Default behaviour is to get attributes from the name of the dimensions of the variable.
     * In case the indexer.xml contains an explicit schema with different attributes for time and elevation
     * we need to remap them and updates the dimensions mapping as well as the DimensionsDescriptors
     * @param dimensionDescriptors
     * @throws IOException
     */
    private void updateDimensions(List<DimensionDescriptor> dimensionDescriptors) throws IOException {
        final Map<Name, String> mapping = reader.ancillaryFileManager.variablesMap;
        final Set<Name> keys = mapping.keySet();
        final String varName = getName();
        for (Name key: keys) {

            // Go to the current variable
            final String origName = mapping.get(key);
            if (origName.equalsIgnoreCase(varName)){

                // Get the mapped coverage name (as an instance, NO2 for a GOME2 with var = 'z')
                final String coverageName = key.getLocalPart();
                final Coverage coverage = reader.ancillaryFileManager.coveragesMapping.get(coverageName);
                final SchemaType schema = coverage.getSchema();
                if (schema != null) {
                    // look up the name
                    String schName= schema.getName();
                    final CoverageSlicesCatalog catalog = reader.getCatalog();
                    if (catalog != null) {
                        // Current assumption is that we have a typeName for each coverage but we should keep on working
                        // with shared schemas
                        // try with coveragename
                        SimpleFeatureType schemaType = null;
                        try{
                            if(schName!=null){
                                schemaType=catalog.getSchema(schName);
                            }
                        }catch (IOException e) {
                            // ok, we did not use the schema name, let's use the coverage name
                            schemaType = catalog.getSchema(coverageName);
                        }
                        if (schemaType != null) {
                            // Schema found: proceed with remapping attributes
                            updateMapping(schemaType, dimensionDescriptors);
                            indexSchema = schemaType;
                            break;
                        }
                        throw new IllegalStateException("Unable to find the table for this coverage: "+ coverageName);
                    }
                }
                break;
            }
        }
    }

    /**
     * Update the dimensionDescriptor attributes mapping by checking the actual attribute names from the schema
     * @param indexSchema
     * @param descriptors
     * @throws IOException
     */
    public void updateMapping(SimpleFeatureType indexSchema, List<DimensionDescriptor> descriptors)
            throws IOException {
        Map<String, String> dimensionsMapping = reader.dimensionsMapping;
        Set<String> keys = dimensionsMapping.keySet();
        int indexAttribute = FIRST_ATTRIBUTE_INDEX;

        // Remap time
        String currentDimName = UnidataUtilities.TIME_DIM;
        if (keys.contains(currentDimName)) {
            if (remapAttribute(indexSchema, currentDimName, indexAttribute, descriptors,
                    dimensionsMapping)) {
                indexAttribute++;
            }
        }

        // Remap elevation
        currentDimName = UnidataUtilities.ELEVATION_DIM;
        if (keys.contains(currentDimName)) {
            if (remapAttribute(indexSchema, currentDimName, indexAttribute, descriptors,
                    dimensionsMapping)) {
                indexAttribute++;
            }
>>>>>>> 76aa07461566a5976980e6696204781271955163
        }
    }

    /**
<<<<<<< HEAD
     * Return a 2D version of a requestedEnvelope
     * 
     * @param requestedEnvelope the {@code GeneralEnvelope} to be returned as 2D.
     * @return the 2D requested envelope
     * @throws FactoryException
     * @throws TransformException
     */
    public static GeneralEnvelope getRequestedEnvelope2D(GeneralEnvelope requestedEnvelope)
            throws FactoryException, TransformException {
        if (requestedEnvelope == null)
            throw new IllegalArgumentException("requested envelope is null");
        GeneralEnvelope requestedEnvelope2D = null;
        final MathTransform transformTo2D;
        CoordinateReferenceSystem requestedEnvelopeCRS2D = requestedEnvelope
                .getCoordinateReferenceSystem();

        // //
        //
        // Find the transformation to 2D
        //
        // //
        if (requestedEnvelopeCRS2D.getCoordinateSystem().getDimension() != 2) {
            transformTo2D = CRS.findMathTransform(requestedEnvelopeCRS2D,
                    CRS.getHorizontalCRS(requestedEnvelopeCRS2D));
            requestedEnvelopeCRS2D = CRS.getHorizontalCRS(requestedEnvelopeCRS2D);
        } else
            transformTo2D = IdentityTransform.create(2);

        if (!transformTo2D.isIdentity()) {
            requestedEnvelope2D = CRS.transform(transformTo2D, requestedEnvelope);
            requestedEnvelope2D.setCoordinateReferenceSystem(requestedEnvelopeCRS2D);
        } else
            requestedEnvelope2D = new GeneralEnvelope(requestedEnvelope);

        assert requestedEnvelopeCRS2D.getCoordinateSystem().getDimension() == 2;
        return requestedEnvelope2D;
    }

    /**
     * Return a crop region from a specified envelope, leveraging on a grid to world transformation.
     * 
     * @param envelope the crop envelope
     * @param gridToWorldTransform the grid2world transformation
     * @return a {@code Rectangle} representing the crop region.
     * @throws TransformException in case a problem occurs when going back to raster space.
     */
    public static Rectangle getCropRegion(GeneralEnvelope envelope,
            final MathTransform gridToWorldTransform) throws TransformException {
        if (envelope == null || gridToWorldTransform == null) {
            boolean isEnvelope = envelope == null;
            boolean isG2W = gridToWorldTransform == null;
            boolean twoErrors = isEnvelope && isG2W;
            final StringBuilder errorMessage = new StringBuilder();
            errorMessage.append("Specified ").append(isEnvelope ? "envelope" : "")
                    .append(twoErrors ? ", " : "")
                    .append(isG2W ? "grid to world transformation " : "").append("is null");
            throw new IllegalArgumentException(errorMessage.toString());
        }
        final MathTransform worldToGridTransform = gridToWorldTransform.inverse();
        final GeneralEnvelope rasterArea = CRS.transform(worldToGridTransform, envelope);
        final Rectangle2D ordinates = rasterArea.toRectangle2D();
        return ordinates.getBounds();
    }

    /**
     * Returns the intersection between the base envelope and the requested envelope.
     * 
     * @param baseEnvelope2D the base envelope.
     * 
     * @param requestedEnvelope2D the requested 2D envelope to be intersected with the base envelope.
     * @param requestedDim is the requested region where to load data of the specified envelope.
     * @param readGridToWorld the Grid to world transformation to be used in read
     * @param wgs84BaseEnvelope2D a WGS84 version of the baseEnvelope to be used to try finding an intersection in wgs84 in case it is impossible to
     *        compute an intersection of the base envelope with the specified requested envelope.
     * @return the resulting intersection of envelopes. In case of empty intersection, this method is allowed to return {@code null}
     * @throws TransformException
     * @throws FactoryException
     * @todo TODO XXX refactor this method leveraging on the coverageSourceCapabilities of reprojection. Moreover add a boolean parameter saying if
     *       trying to reproject to WGS84 always need to be done
     */
    public static GeneralEnvelope getIntersection(final Envelope2D baseEnvelope2D,
            final CoordinateReferenceSystem spatialReferenceSystem2D,
            GeneralEnvelope requestedEnvelope2D, Rectangle requestedDim,
            MathTransform2D readGridToWorld, final Envelope2D wgs84BaseEnvelope2D)
            throws TransformException, FactoryException {

        if (baseEnvelope2D == null || spatialReferenceSystem2D == null
                || requestedEnvelope2D == null || requestedDim == null || readGridToWorld == null) {
            StringBuilder sb = new StringBuilder("Some of the specified parameters are null:")
                    .append(baseEnvelope2D == null ? "base envelope \n" : "")
                    .append(spatialReferenceSystem2D == null ? "native spatial reference system\n"
                            : "")
                    .append(requestedEnvelope2D == null ? "requested envelope \n" : "")
                    .append(requestedDim == null ? "requested dim\n" : "")
                    .append(readGridToWorld == null ? "requested grid to world transformation \n"
                            : "");
            throw new IllegalArgumentException(sb.toString());
        }
        GeneralEnvelope adjustedRequestedEnvelope = new GeneralEnvelope(2);
        final CoordinateReferenceSystem requestedEnvelopeCRS2D = requestedEnvelope2D
                .getCoordinateReferenceSystem();
        boolean tryWithWGS84 = false;

        try {
            // convert the requested envelope 2D to this coverage native crs.
            MathTransform transform = null;
            if (!CRS.equalsIgnoreMetadata(requestedEnvelopeCRS2D, spatialReferenceSystem2D))
                transform = CRS.findMathTransform(requestedEnvelopeCRS2D, spatialReferenceSystem2D,
                        true);
            // now transform the requested envelope to source crs
            if (transform != null && !transform.isIdentity())
                adjustedRequestedEnvelope = CRS.transform(transform, requestedEnvelope2D);
            else
                adjustedRequestedEnvelope.setEnvelope(requestedEnvelope2D);

            // intersect the requested area with the bounds of this
            // layer in native crs
            if (!adjustedRequestedEnvelope.intersects(baseEnvelope2D, true))
                return null;
            adjustedRequestedEnvelope.intersect(baseEnvelope2D);
            adjustedRequestedEnvelope.setCoordinateReferenceSystem(spatialReferenceSystem2D);

            // //
            //
            // transform the intersection envelope from the destination world
            // space to the requested raster space
            //
            // //
            final Envelope requestedEnvelopeCropped = (transform != null && !transform.isIdentity()) ? CRS
                    .transform(transform.inverse(), adjustedRequestedEnvelope)
                    : adjustedRequestedEnvelope;
            final Rectangle2D ordinates = CRS.transform(readGridToWorld.inverse(),
                    requestedEnvelopeCropped).toRectangle2D();
            final GeneralGridEnvelope finalRange = new GeneralGridEnvelope(ordinates.getBounds());
            final Rectangle tempRect = finalRange.toRectangle();
            // check that we stay inside the source rectangle
            XRectangle2D.intersect(tempRect, requestedDim, tempRect);
            requestedDim.setRect(tempRect);
        } catch (TransformException te) {
            // something bad happened while trying to transform this
            // envelope. let's try with wgs84
            tryWithWGS84 = true;
        } catch (FactoryException fe) {
            // something bad happened while trying to transform this
            // envelope. let's try with wgs84
            tryWithWGS84 = true;
        }

        // //
        //
        // If this does not work, we go back to reproject in the wgs84
        // requested envelope
        //
        // //
        if (tryWithWGS84) {
            final GeneralEnvelope requestedEnvelopeWGS84 = (GeneralEnvelope) getEnvelopeAsWGS84(
                    requestedEnvelope2D, false);

            // checking the intersection in wgs84
            if (!requestedEnvelopeWGS84.intersects(wgs84BaseEnvelope2D, true))
                return null;

            // intersect
            adjustedRequestedEnvelope = new GeneralEnvelope(requestedEnvelopeWGS84);
            adjustedRequestedEnvelope.intersect(wgs84BaseEnvelope2D);
            adjustedRequestedEnvelope = CRS.transform(CRS.findMathTransform(
                    requestedEnvelopeWGS84.getCoordinateReferenceSystem(),
                    spatialReferenceSystem2D, true), adjustedRequestedEnvelope);
            adjustedRequestedEnvelope.setCoordinateReferenceSystem(spatialReferenceSystem2D);

        }
        return adjustedRequestedEnvelope;
    }

    /**
     * Retrieves the original grid to world transformation for this {@link AbstractGridCoverage2DReader}.
     * 
     * @param pixInCell specifies the datum of the transformation we want.
     * @return the original grid to world transformation
     */
    public static MathTransform getOriginalGridToWorld(MathTransform raster2Model,
            final PixelInCell pixInCell) {
        // we do not have to change the pixel datum
        if (pixInCell == PixelInCell.CELL_CENTER)
            return raster2Model;

        // we do have to change the pixel datum
        if (raster2Model instanceof AffineTransform) {
            final AffineTransform tr = new AffineTransform((AffineTransform) raster2Model);
            tr.concatenate(AffineTransform.getTranslateInstance(-0.5, -0.5));
            return ProjectiveTransform.create(tr);
        }
        if (raster2Model instanceof IdentityTransform) {
            final AffineTransform tr = new AffineTransform(1, 0, 0, 1, 0, 0);
            tr.concatenate(AffineTransform.getTranslateInstance(-0.5, -0.5));
            return ProjectiveTransform.create(tr);
        }
        throw new IllegalStateException("This grid to world transform is invalud!");
    }

    /**
     * Evaluates the requested envelope and builds a new adjusted version of it fitting this coverage envelope.
     * 
     * <p>
     * While adjusting the requested envelope this methods also compute the source region as a rectangle which is suitable for a successive read
     * operation with {@link ImageIO} to do crop-on-read.
     * 
     * @param originalGridToWorld
     * 
     * @param coordinateReferenceSystem
     * 
     * 
     * @param requestedEnvelope is the envelope we are requested to load.
     * @param sourceRegion represents the area to load in raster space. This parameter cannot be null since it gets filled with whatever the crop
     *        region is depending on the <code>requestedEnvelope</code>.
     * @param requestedDim is the requested region where to load data of the specified envelope.
     * @param readGridToWorld the Grid to world transformation to be used
     * @param wgs84BaseEnvelope2D
     * @return the adjusted requested envelope, empty if no requestedEnvelope has been specified, {@code null} in case the requested envelope does not
     *         intersect the coverage envelope or in case the adjusted requested envelope is covered by a too small raster region (an empty region).
     * 
     * @throws DataSourceException in case something bad occurs
     */
    public static GeneralEnvelope evaluateRequestedParams(GridEnvelope originalGridRange,
            Envelope2D baseEnvelope2D, CoordinateReferenceSystem spatialReferenceSystem2D,
            MathTransform originalGridToWorld, GeneralEnvelope requestedEnvelope,
            Rectangle sourceRegion, Rectangle requestedDim, MathTransform2D readGridToWorld,
            Envelope2D wgs84BaseEnvelope2D) throws DataSourceException {

        GeneralEnvelope adjustedRequestedEnvelope = new GeneralEnvelope(2);
        GeneralGridEnvelope baseGridRange = (GeneralGridEnvelope) originalGridRange;

        try {
            // ////////////////////////////////////////////////////////////////
            //
            // Check if we have something to load by intersecting the
            // requested envelope with the bounds of this data set.
            //
            // ////////////////////////////////////////////////////////////////
            if (requestedEnvelope != null) {
                final GeneralEnvelope requestedEnvelope2D = Utilities
                        .getRequestedEnvelope2D(requestedEnvelope);

                // ////////////////////////////////////////////////////////////
                //
                // INTERSECT ENVELOPES AND CROP Destination REGION
                //
                // ////////////////////////////////////////////////////////////
                adjustedRequestedEnvelope = Utilities.getIntersection(baseEnvelope2D,
                        spatialReferenceSystem2D, requestedEnvelope2D, requestedDim,
                        readGridToWorld, wgs84BaseEnvelope2D);
                if (adjustedRequestedEnvelope == null)
                    return null;

                // /////////////////////////////////////////////////////////////////////
                //
                // CROP SOURCE REGION
                //
                // /////////////////////////////////////////////////////////////////////
                sourceRegion.setRect(Utilities.getCropRegion(adjustedRequestedEnvelope, Utilities
                        .getOriginalGridToWorld(originalGridToWorld, PixelInCell.CELL_CORNER)));
                if (sourceRegion.isEmpty()) {
                    if (LOGGER.isLoggable(Level.INFO)) {
                        LOGGER.log(Level.INFO,
                                "Too small envelope resulting in empty cropped raster region");
                    }
                    return null;
                    // TODO: Future versions may define a 1x1 rectangle starting
                    // from the lower coordinate
                }
                if (!sourceRegion.intersects(baseGridRange.toRectangle()) || sourceRegion.isEmpty())
                    throw new DataSourceException("The crop region is invalid.");
                sourceRegion.setRect(sourceRegion.intersection(baseGridRange.toRectangle()));

                if (LOGGER.isLoggable(Level.FINE)) {
                    StringBuilder sb = new StringBuilder("Adjusted Requested Envelope = ")
                            .append(adjustedRequestedEnvelope.toString()).append("\n")
                            .append("Requested raster dimension = ")
                            .append(requestedDim.toString()).append("\n")
                            .append("Corresponding raster source region = ")
                            .append(sourceRegion.toString());
                    LOGGER.log(Level.FINE, sb.toString());
                }

            } else {
                // don't use the source region. Set an empty one
                sourceRegion.setBounds(new Rectangle(0, 0, Integer.MIN_VALUE, Integer.MIN_VALUE));
            }
        } catch (TransformException e) {
            throw new DataSourceException("Unable to create a coverage for this source", e);
        } catch (FactoryException e) {
            throw new DataSourceException("Unable to create a coverage for this source", e);
        }
        return adjustedRequestedEnvelope;
    }

    /**
     * Creates a {@link GridCoverage} for the provided {@link PlanarImage} using the {@link #raster2Model} that was provided for this coverage.
     * 
     * <p>
     * This method is vital when working with coverages that have a raster to model transformation that is not a simple scale and translate.
     * 
     * @param imageIndex
     * 
     * @param image contains the data for the coverage to create.
     * @param raster2Model is the {@link MathTransform} that maps from the raster space to the model space.
     * @return a {@link GridCoverage}
     * @throws IOException
     */
    public static GridCoverage createCoverageFromImage(final GridCoverageFactory coverageFactory,
            final String coverageName, int imageIndex, PlanarImage image,
            MathTransform raster2Model, final CoordinateReferenceSystem spatialReferenceSystem2D,
            GeneralEnvelope coverageEnvelope2D, final GridSampleDimension[] sampleDimensions,
            final boolean getGeophysics) throws IOException {
        final GridSampleDimension[] bands = sampleDimensions;

        GridCoverage2D gridCoverage;
        // creating coverage
        if (raster2Model != null) {
            gridCoverage = coverageFactory.create(coverageName, image, spatialReferenceSystem2D,
                    raster2Model, bands, null, null);
        } else
            gridCoverage = coverageFactory.create(coverageName, image, coverageEnvelope2D, bands,
                    null, null);

        if (getGeophysics)
            return gridCoverage.view(ViewType.GEOPHYSICS);
        else
            return gridCoverage;
    }

    /**
     * This method is responsible for evaluating possible subsampling factors once the best resolution level has been found in case we have support
     * for overviews, or starting from the original coverage in case there are no overviews available.
     * 
     * @param readP the imageRead parameter to be set
     * @param requestedRes the requested resolutions from which to determine the decimation parameters.
     */
    public static void setDecimationParameters(ImageReadParam readP, GridEnvelope baseGridRange,
            double[] requestedRes, double[] highestRes) {
        {
            if (readP == null || baseGridRange == null)
                throw new IllegalArgumentException("Specified parameters are null");
            final int w = baseGridRange.getSpan(0);
            final int h = baseGridRange.getSpan(1);

            // ///////////////////////////////////////////////////////////////
            // DECIMATION ON READING
            // Setting subsampling factors with some checkings
            // 1) the subsampling factors cannot be zero
            // 2) the subsampling factors cannot be such that the w or h are 0
            // ///////////////////////////////////////////////////////////////
            if (requestedRes == null) {
                readP.setSourceSubsampling(1, 1, 0, 0);
            } else {
                int subSamplingFactorX = (int) Math.floor(requestedRes[0] / highestRes[0]);
                subSamplingFactorX = (subSamplingFactorX == 0) ? 1 : subSamplingFactorX;

                while (((w / subSamplingFactorX) <= 0) && (subSamplingFactorX >= 0))
                    subSamplingFactorX--;

                subSamplingFactorX = (subSamplingFactorX == 0) ? 1 : subSamplingFactorX;

                int subSamplingFactorY = (int) Math.floor(requestedRes[1] / highestRes[1]);
                subSamplingFactorY = (subSamplingFactorY == 0) ? 1 : subSamplingFactorY;

                while (((h / subSamplingFactorY) <= 0) && (subSamplingFactorY >= 0))
                    subSamplingFactorY--;

                subSamplingFactorY = (subSamplingFactorY == 0) ? 1 : subSamplingFactorY;

                readP.setSourceSubsampling(subSamplingFactorX, subSamplingFactorY, 0, 0);
            }
        }
    }

    public static NameImpl buildCoverageName(URL input) {
        if (input == null) {
            throw new IllegalArgumentException("Null URL specified");
        }
        String fileName = input.getPath();
        final int slashIndex = fileName.lastIndexOf("/");

        // TODO: fix if slashIndex == -1
        fileName = fileName.substring(slashIndex + 1, fileName.length());
        final int dotIndex = fileName.lastIndexOf(".");
        final String coverageNameString = (dotIndex == -1) ? fileName : fileName.substring(0,
                dotIndex);
        return new NameImpl(coverageNameString);
    }

    /**
     * Prepares the read parameters for doing an {@link ImageReader#read(int, ImageReadParam)}.
     * 
     * It sets the passed {@link ImageReadParam} in terms of decimation on reading using the provided requestedEnvelope and requestedDim to evaluate
     * the needed resolution.
     * 
     * @param overviewPolicy it can be one of {@link Hints#VALUE_OVERVIEW_POLICY_IGNORE}, {@link Hints#VALUE_OVERVIEW_POLICY_NEAREST},
     *        {@link Hints#VALUE_OVERVIEW_POLICY_QUALITY} or {@link Hints#VALUE_OVERVIEW_POLICY_SPEED}. It specifies the policy to compute the
     *        overviews level upon request.
     * @param readParam an instance of {@link ImageReadParam} for setting the subsampling factors.
     * @param requestedEnvelope the {@link GeneralEnvelope} we are requesting.
     * @param requestedDim the requested dimensions.
     * @param gridRange
     * @throws IOException
     * @throws TransformException
     */
    public static void setReadParameters(OverviewPolicy overviewPolicy, ImageReadParam readParam,
            GeneralEnvelope requestedEnvelope, Rectangle requestedDim, double[] highestRes,
            GridEnvelope gridRange, PixelInCell pixelInCell) throws IOException, TransformException {
        double[] requestedRes = null;

        // //
        //
        // Initialize overview policy
        //
        // //
        if (overviewPolicy == null) {
            overviewPolicy = OverviewPolicy.NEAREST;
        }

        // //
        //
        // default values for subsampling
        //
        // //
        readParam.setSourceSubsampling(1, 1, 0, 0);

        // //
        //
        // requested to ignore overviews
        //
        // //
        if (overviewPolicy.equals(OverviewPolicy.IGNORE)) {
            return;
        }

        // //
        //
        // Resolution requested. I am here computing the resolution required
        // by the user.
        //
        // //
        if (requestedEnvelope != null) {
            final GridToEnvelopeMapper geMapper = new GridToEnvelopeMapper();
            geMapper.setEnvelope(requestedEnvelope);
            geMapper.setGridRange(new GeneralGridEnvelope(requestedDim, 2));
            geMapper.setPixelAnchor(pixelInCell);
            final AffineTransform transform = geMapper.createAffineTransform();
            requestedRes = CoverageUtilities.getResolution(transform);
        }

        if (requestedRes == null) {
            return;
        }

        // ////////////////////////////////////////////////////////////////////
        //
        // DECIMATION ON READING
        //
        // ////////////////////////////////////////////////////////////////////
        if (highestRes == null)
            throw new IllegalArgumentException("Unspecified highest Resolution");
        if ((requestedRes[0] > highestRes[0]) || (requestedRes[1] > highestRes[1])) {
            Utilities.setDecimationParameters(readParam, gridRange, requestedRes, highestRes);
        }
    }

    /**
     * This method creates the GridCoverage2D from the underlying file given a specified envelope, and a requested dimension.
     * 
     * @param imageIndex
     * @param coordinateReferenceSystem
     * @param generalEnvelope
     * @param mathTransform
     * 
     * @param iUseJAI specify if the underlying read process should leverage on a JAI ImageRead operation or a simple direct call to the {@code read}
     *        method of a proper {@code ImageReader}.
     * @param useMultithreading specify if the underlying read process should use multithreading when a JAI ImageRead operation is requested
     * @param overviewPolicy the overview policy which need to be adopted
     * @return a {@code GridCoverage}
     * 
     * @throws java.io.IOException
     */
    public static GridCoverage createCoverage(ImageReaderSpi spi, Object input,
            final int imageIndex, ImageReadParam imageReadParam, final boolean useJAI,
            final boolean useMultithreading, final boolean newTransform,
            final GridSampleDimension[] sampleDimensions, final String coverageName,
            GridCoverageFactory coverageFactory, MathTransform raster2Model,
            CoordinateReferenceSystem coordinateReferenceSystem, GeneralEnvelope coverageEnvelope2D)
            throws IOException {
        // ////////////////////////////////////////////////////////////////////
        //
        // Doing an image read for reading the coverage.
        //
        // ////////////////////////////////////////////////////////////////////
        final PlanarImage image = readImage(spi, input, imageIndex, useJAI, imageReadParam,
                useMultithreading);

        // /////////////////////////////////////////////////////////////////////
        //
        // Creating the coverage
        //
        // /////////////////////////////////////////////////////////////////////
        if (newTransform) {
            // I need to calculate a new transformation (raster2Model)
            // between the cropped image and the required envelope
            final int ssWidth = image.getWidth();
            final int ssHeight = image.getHeight();

            // //
            //
            // setting new coefficients to define a new affineTransformation
            // to be applied to the grid to world transformation
            // ------------------------------------------------------
            //
            // With respect to the original envelope, the obtained
            // planarImage needs to be rescaled and translated. The scaling
            // factors are computed as the ratio between the cropped source
            // region sizes and the read image sizes. The translate
            // settings are represented by the offsets of the source region.
            //
            // //
            final Rectangle sourceRegion = imageReadParam.getSourceRegion();
            final double scaleX = sourceRegion.width / (1.0 * ssWidth);
            final double scaleY = sourceRegion.height / (1.0 * ssHeight);
            final double translateX = sourceRegion.x;
            final double translateY = sourceRegion.y;
            return Utilities.createCoverageFromImage(coverageFactory, coverageName, imageIndex,
                    image, ConcatenatedTransform.create(ProjectiveTransform
                            .create(new AffineTransform(scaleX, 0, 0, scaleY, translateX,
                                    translateY)), raster2Model), coordinateReferenceSystem,
                    (GeneralEnvelope) null, sampleDimensions, true);
        } else {
            // In case of no transformation is required (As an instance,
            // when reading the whole image)
            return Utilities.createCoverageFromImage(coverageFactory, coverageName, imageIndex,
                    image, (MathTransform) null, (CoordinateReferenceSystem) null,
                    coverageEnvelope2D, sampleDimensions, true);
        }
    }

    /**
     * Returns a {@code PlanarImage} given a set of parameter specifying the type of read operation to be performed.
     * 
     * @param imageIndex
     * 
     * @param new FileImageInputStreamExtImplinput the input {@code ImageInputStream} to be used for reading the image.
     * @param useJAI {@code true} if we need to use a JAI ImageRead operation, {@code false} if we need a simple direct {@code ImageReader.read(...)}
     *        call.
     * @param imageReadParam an {@code ImageReadParam} specifying the read parameters
     * @param useMultithreading {@code true} if a JAI ImageRead operation is requested with support for multithreading. This parameter will be ignored
     *        if requesting a direct read operation.
     * @return the read {@code PlanarImage}
     * @throws IOException
     */
    public static PlanarImage readImage(final ImageReaderSpi spi, final Object input,
            final int imageIndex, final boolean useJAI, final ImageReadParam imageReadParam,
            final boolean useMultithreading) throws IOException {
        ImageInputStream paramInput = null;
        if (input instanceof File) {
            paramInput = new FileImageInputStreamExtImpl((File) input);
        } else if (input instanceof FileImageInputStreamExt) {
            paramInput = (FileImageInputStreamExt) input;
        } else if (input instanceof URIImageInputStream) {
            paramInput = (URIImageInputStream) input;
        } else if (input instanceof URL) {
            final URL tempURL = (URL) input;
            String protocol = tempURL.getProtocol();
            if (protocol.equalsIgnoreCase("file")) {
                try {
                    File file = it.geosolutions.imageio.utilities.Utilities.urlToFile(tempURL);
                    paramInput = new FileImageInputStreamExtImpl(file);
                } catch (IOException e) {
                    throw new RuntimeException("Failed to create a valid input stream ", e);
                }
            } else if (tempURL.getProtocol().toLowerCase().startsWith("http")
                    || tempURL.getProtocol().equalsIgnoreCase("dods")) {
                try {
                    paramInput = new URIImageInputStreamImpl(tempURL.toURI());
                } catch (URISyntaxException e) {
                    throw new RuntimeException("Failed to create a valid input stream ", e);
                }
            }
        } else
            throw new IllegalArgumentException("Unsupported Input type:" + input);

        PlanarImage planarImage;
        ImageReader reader;
        ImageReaderSpi readerSpi = spi;
        if (useJAI) {
            final ParameterBlock pbjImageRead = new ParameterBlock();
            pbjImageRead.add(paramInput);
            pbjImageRead.add(imageIndex);
            pbjImageRead.add(Boolean.FALSE);
            pbjImageRead.add(Boolean.FALSE);
            pbjImageRead.add(Boolean.FALSE);
            pbjImageRead.add(null);
            pbjImageRead.add(null);
            pbjImageRead.add(imageReadParam);
            reader = readerSpi.createReaderInstance();
            pbjImageRead.add(reader);

            // Check if to use a simple JAI ImageRead operation or a
            // multithreaded one
            final String jaiOperation = useMultithreading ? "ImageReadMT" : "ImageRead";
            // final String jaiOperation = "ImageRead";
            /** TODO: SET HINTS */
            planarImage = JAI.create(jaiOperation, pbjImageRead, null);
        } else {
            reader = readerSpi.createReaderInstance();
            reader.setInput(paramInput, true, true);
            planarImage = PlanarImage.wrapRenderedImage(reader.read(imageIndex, imageReadParam));
            reader.dispose();
        }
        return planarImage;
    }

    /**
     * Compute the coverage request and produce a grid coverage. The produced grid coverage may be {@code null} in case of empty request.
     * 
     * @param index
     * @param imageReadParam
     * @param isEmptyRequest
     * @param needTransformation
     * @param imageReaderSpi
     * @param coverageName
     * @param coverageFactory
     * @param raster2Model
     * @param coordinateReferenceSystem
     * @param envelope2D
     * 
     * @throws IOException
     * @TODO: handle more input types
     */
    public static GridCoverage compute(Object input, final int imageIndex,
            final boolean needTransformation, final boolean isEmptyRequest, final boolean useJAI,
            ImageReadParam imageReadParam, final boolean useMultithreading,
            final GridSampleDimension[] sampleDimensions, final ImageReaderSpi imageReaderSpi,
            final String coverageName, final GridCoverageFactory coverageFactory,
            final MathTransform raster2Model,
            final CoordinateReferenceSystem coordinateReferenceSystem,
            final GeneralEnvelope envelope2D) throws IOException {

        if (isEmptyRequest) {
            return null;
        } else {
            return Utilities.createCoverage(imageReaderSpi, input, imageIndex,
                    imageReadParam, useJAI, useMultithreading, needTransformation,
                    sampleDimensions, coverageName, coverageFactory, raster2Model,
                    coordinateReferenceSystem, envelope2D);
        }
    }

    public final static boolean ensureValidString(final String... strings) {
        for (String string : strings) {
            if (string == null || string.trim().length() <= 0)
                return false;
        }
        return true;

    }

    public static String buildIso8601Time(String date, String time) {
        String iso8601Date = null;
        if (ensureValidString(date, time)) {
            final String formattedDate = date.replace("/", "-");
            final StringBuilder sb = new StringBuilder(formattedDate).append("T").append(time)
                    .append("Z");
            iso8601Date = sb.toString();
        }
        return iso8601Date;
    }

//    /**
//     * Build a suitable {@link GridSampleDimension}
//     * 
//     * @param sampleDim
//     * @param elementName
//     * @param unit
//     * @return
//     */
//    public static GridSampleDimension buildBands(final Band sampleDim, final String elementName,
//            Unit unit) {
//        if (sampleDim == null)
//            throw new IllegalArgumentException("provided metadata element is null");
//        // final Unit unit = CRSUtilities.parseUnit(sampleDim.getUoM());
//        Category[] categories = null;
//        Category values = null;
//        Category nan = null;
//        int cat = 0;
//
//        final double scale = sampleDim.getScale();
//        final double offset = sampleDim.getOffset();
//
//        final NumberRange<? extends Number> validRange = sampleDim.getValidRange();
//        final double[] noDataValues = sampleDim.getNoDataValues();
//        // Actually, the underlying readers use fillValue as noData
//        // Is this correct?
//        Number minO = null;
//        Number maxO = null;
//        if (validRange != null) {
//            minO = validRange.getMinValue();
//            maxO = validRange.getMaxValue();
//        }
//
//        if (!(scale == 1.0 && offset == 0.0)) {
//            if (minO != null && maxO != null) {
//                double min = validRange.getMinimum();
//                double max = validRange.getMaximum();
//                if (!Double.isInfinite(min) && !Double.isNaN(min) && !Double.isInfinite(max)
//                        && !Double.isNaN(max)) {
//
//                    NumberRange<Double> minMax = NumberRange.create(min, false, max, true);
//                    // double geoMin = (minMax.getMinimum(false)*scale)+offset;
//                    // double geoMax = (minMax.getMaximum(true)*scale)+offset;
//
//                    values = new Category("values", null, minMax, scale, offset);
//                    // values = new Category("values", null, minMax
//                    // ,NumberRange.create(geoMin, false, geoMax,true));
//                    cat++;
//                }
//            }
//        }
//
//        if (noDataValues != null) {
//            final int size = noDataValues.length;
//            if (size == 1) {
//                double noData = noDataValues[0];
//                double newNoData = noData;
//                if (minO != null && maxO != null) {
//                    double min = validRange.getMinimum();
//                    double max = validRange.getMaximum();
//                    if (min == noData && minO instanceof Integer) {
//                        newNoData = min - 1;
//                    } else if (max == noData && maxO instanceof Integer) {
//                        newNoData = max + 1;
//                    }
//                }
//                if (!Double.isNaN(noData)) {
//                    nan = new Category(Vocabulary.formatInternational(VocabularyKeys.NODATA),
//                            new Color[] { new Color(0, 0, 0, 0) }, NumberRange.create(noData,
//                                    noData), NumberRange.create(newNoData, newNoData));
//                }
//            }
//            cat++;
//        }
//
//        if (cat > 0) {
//            categories = new Category[cat];
//            if (cat == 2) {
//                categories[0] = nan;
//                categories[1] = values;
//            } else
//                categories[0] = nan == null ? values : nan;
//        }
//
//        final GridSampleDimension band = new GridSampleDimension(elementName + ":sd", categories,
//                unit);
//        return band;
//    }

    
    
    // /**
    // * Check two {@link TemporalGeometricPrimitive} objects. Return {@code true} in case the first argument
    // * contains the second one. In case of instants, return {@code true} if they are equals.
    // * In case the first argument is a period and the second one is an instant, check if the instant is
    // * contained within the period. In case they are Periods, check for an intersection.
    // *
    // * @param containing
    // * @param contained
    // * @return
    // */
    // public static boolean contains(TemporalGeometricPrimitive containing, TemporalGeometricPrimitive contained) {
    // // //
    // //
    // // Instants should match to be taken
    // //
    // // //
    // if (containing instanceof Instant && contained instanceof Instant)
    // return containing.equals(contained);
    // // //
    // //
    // // If the first time is a period I will check if the second time (an instant)
    // // is between the beginning and the ending of the period.
    // //
    // // //
    // else if (containing instanceof Period && contained instanceof Instant) {
    // final Date position = ((DefaultInstant) contained).getPosition()
    // .getDate();
    // final DefaultPeriod period = (DefaultPeriod) containing;
    // final Date startPeriod = period.getBeginning().getPosition()
    // .getDate();
    // final Date endPeriod = period.getEnding().getPosition().getDate();
    // if (position.compareTo(startPeriod) >= 0
    // && position.compareTo(endPeriod) <= 0)
    // return true;
    // }
    // // //
    // //
    // // In case both times are periods, I check for an intersection.
    // //
    // // //
    // else if (containing instanceof Period && contained instanceof Period) {
    // final DefaultPeriod containingPeriod = (DefaultPeriod) containing;
    // final Date startContaining = containingPeriod.getBeginning().getPosition().getDate();
    // final Date endContaining = containingPeriod.getEnding().getPosition().getDate();
    // final DefaultPeriod containedPeriod = (DefaultPeriod) contained;
    // final Date startContained = containedPeriod.getBeginning().getPosition().getDate();
    // final Date endContained = containedPeriod.getEnding().getPosition().getDate();
    //
    // // Return false if the period which should be contained is totally
    // // outside the containing one.
    // // Silly example: a period between 3AM and 5AM isn't contained in a
    // // period between 6AM and 8AM.
    // // Instead, a period between 5AM and 7AM should be considered
    // // contained within the period between 6AM and 8AM since part of
    // // them are intersecting.
    // if (endContained.compareTo(startContaining) < 0
    // || startContained.compareTo(endContaining) > 0)
    // return false;
    // else
    // return true;
    // }
    // return false;
    // }
    //
    // /**
    // * Move these methods to an Utility Class and improve the logic.
    // *
    // * @param first
    // * @param second
    // * @return
    // */
    // public static boolean isTimeAccepted( TemporalGeometricPrimitive first, TemporalGeometricPrimitive second ) {
    // boolean takeThis = Utilities.contains(first, second);
    // if (!takeThis)
    // takeThis = Utilities.contains(second, first);
    // return takeThis;
    // }

    /**
     * Checks that a {@link File} is a real file, exists and is readable.
     * 
     * @param file the {@link File} instance to check. Must not be null.
     * 
     * @return <code>true</code> in case the file is a real file, exists and is readable; <code>false </code> otherwise.
     */
    public static boolean checkFileReadable(final File file) {
        if (LOGGER.isLoggable(Level.FINE)) {
            final String message = getFileInfo(file);
            LOGGER.fine(message);
        }
        if (!file.exists() || !file.canRead() || !file.isFile())
            return false;
        return true;
    }

    /**
     * Creates a human readable message that describe the provided {@link File} object in terms of its properties.
     * 
     * <p>
     * Useful for creating meaningful log messages.
     * 
     * @param file the {@link File} object to create a descriptive message for
     * @return a {@link String} containing a descriptive message about the provided {@link File}.
     * 
     */
    public static String getFileInfo(final File file) {
        final StringBuilder builder = new StringBuilder();
        builder.append("Checking file:").append(FilenameUtils.getFullPath(file.getAbsolutePath()))
                .append("\n");
        builder.append("isHidden:").append(file.isHidden()).append("\n");
        builder.append("exists:").append(file.exists()).append("\n");
        builder.append("isFile").append(file.isFile()).append("\n");
        builder.append("canRead:").append(file.canRead()).append("\n");
        builder.append("canWrite").append(file.canWrite()).append("\n");
        builder.append("canExecute:").append(file.canExecute()).append("\n");
        builder.append("isAbsolute:").append(file.isAbsolute()).append("\n");
        builder.append("lastModified:").append(file.lastModified()).append("\n");
        builder.append("length:").append(file.length());
        final String message = builder.toString();
        return message;
    }

    public static Properties loadPropertiesFromURL(URL propsURL) {
        final Properties properties = new Properties();
        InputStream stream = null;
        InputStream openStream = null;
        try {
            openStream = propsURL.openStream();
            stream = new BufferedInputStream(openStream);
            properties.load(stream);
        } catch (FileNotFoundException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        } catch (IOException e) {
            if (LOGGER.isLoggable(Level.SEVERE))
                LOGGER.log(Level.SEVERE, e.getLocalizedMessage(), e);
            return null;
        } finally {

            if (stream != null)
                IOUtils.closeQuietly(stream);

            if (openStream != null)
                IOUtils.closeQuietly(openStream);

        }
        return properties;
=======
     * Remap an attribute for a specified dimension. Get it from the schemaType and update
     * both the related dimension Descriptor as well as the dimensions mapping.
     * 
     * @param indexSchema
     * @param currentDimName
     * @param indexAttribute
     * @param descriptors
     * @param dimensionsMapping
     * @return
     */
    private boolean remapAttribute(final SimpleFeatureType indexSchema, final String currentDimName,
            final int indexAttribute, final List<DimensionDescriptor> descriptors,
            Map<String, String> dimensionsMapping) {
        final int numAttributes = indexSchema.getAttributeCount();
        if (numAttributes <= indexAttribute) {
            // Stop looking for attributes in case there aren't anymore
            return false;
        }

        // Get the attribute descriptor for that index
        final AttributeDescriptor attributeDescriptor = indexSchema.getDescriptor(indexAttribute);

        // Loop over dimensionDescriptors 
        for (DimensionDescriptor descriptor : descriptors) {
            // Find the descriptor related to the current dimension
            if (descriptor.getName().toUpperCase().equalsIgnoreCase(currentDimName)) {
                final String updatedAttribute = attributeDescriptor.getLocalName();
                if (!updatedAttribute.equals(((DefaultDimensionDescriptor) descriptor)
                        .getStartAttribute())) {
                    // Remap attributes in case the schema's attribute doesn't match the current attribute
                    ((DefaultDimensionDescriptor) descriptor).setStartAttribute(updatedAttribute);

                    // Update the dimensions mapping too
                    dimensionsMapping.put(currentDimName, updatedAttribute);
                }
                // the attribute has been found, prepare for the next one
                return true;
            }
        }
        return false;
    }

    /**
     * @param dimensions 
     * @return
     * @throws IllegalArgumentException
     * @throws RuntimeException
     * @throws IOException
     * @throws IllegalStateException
     */
    private List<CoordinateVariable<?>> initCRS(List<DimensionDescriptor> dimensions) throws IllegalArgumentException, RuntimeException,
            IOException, IllegalStateException {
        // from UnidataVariableAdapter        
        this.coordinateSystem = UnidataCRSUtilities.getCoordinateSystem(variableDS);
        if (coordinateSystem == null){
            throw new IllegalArgumentException("Provided CoordinateSystem is null");
        }
        // ////
        // Creating the CoordinateReferenceSystem
        // ////
        coordinateReferenceSystem=UnidataCRSUtilities.WGS84;

        /*
         * Adds the axis in reverse order, because the NetCDF image reader put the last dimensions in the rendered image. Typical NetCDF convention is
         * to put axis in the (time, depth, latitude, longitude) order, which typically maps to (longitude, latitude, depth, time) order in GeoTools
         * referencing framework.
         */
        final List<CoordinateVariable<?>> otherAxes = new ArrayList<CoordinateVariable<?>>();
        for(CoordinateAxis axis :coordinateSystem.getCoordinateAxes()){
            CoordinateVariable<?> cv=reader.coordinatesVariables.get(axis.getShortName());
            switch(cv.getAxisType()){
            case Time:case RunTime:
                initTemporalDomain(cv, dimensions);
                continue;
            case GeoZ:case Height:case Pressure:
                String axisName = cv.getName();
                if (UnidataCRSUtilities.VERTICAL_AXIS_NAMES.contains(axisName)) {
                    initVerticalDomain(cv, dimensions);
                }else{
                    otherAxes.add(cv);
                }
                continue;  
            case GeoX: case GeoY: case Lat: case Lon:
                // do nothing
                continue;
            default:
                otherAxes.add(cv);
            }
            
        }
        return otherAxes;
    }

    /**
     * @param cv
     * @param dimensions 
     * @throws IOException 
     */
    private void initVerticalDomain(CoordinateVariable<?> cv, List<DimensionDescriptor> dimensions) throws IOException {
        this.setHasVerticalDomain(true);
        final UnidataVerticalDomain verticalDomain = new UnidataVerticalDomain(cv);
        this.setVerticalDomain(verticalDomain);
        //TODO: Map ZAxis unit to UCUM UNIT (depending on type... elevation, level, pressure, ...)
        dimensions.add(new DefaultDimensionDescriptor(Utils.ELEVATION_DOMAIN, 
                cv.getUnit(), CoverageUtilities.UCUM.ELEVATION_UNITS.getSymbol(), cv.getName(), null));
    }

    /**
     * @param cv
     * @param dimensions 
     * @throws IOException 
     */
    private void initTemporalDomain(CoordinateVariable<?> cv, List<DimensionDescriptor> dimensions) throws IOException {
       if(!cv.getType().equals(Date.class)){
           throw new IllegalArgumentException("Unable to init temporal domani from CoordinateVariable that does not bind to Date");
       }
       if(!(cv.getCoordinateReferenceSystem() instanceof TemporalCRS)){
           throw new IllegalArgumentException("Unable to init temporal domani from CoordinateVariable that does not have a TemporalCRS");
       }
       this.setHasTemporalDomain(true);
       final UnidataTemporalDomain temporalDomain = new UnidataTemporalDomain(cv);
       this.setTemporalDomain(temporalDomain);
     //TODO: Fix that once schema attributes to dimension mapping is merged from Simone's code
       dimensions.add(new DefaultDimensionDescriptor(Utils.TIME_DOMAIN, 
               CoverageUtilities.UCUM.TIME_UNITS.getName(), CoverageUtilities.UCUM.TIME_UNITS.getSymbol(), cv.getName(), null));
    }

    /**
     * @param coordinateReferenceSystem
     * @throws MismatchedDimensionException
     * @throws IOException 
     */
    private void initSpatialDomain()
            throws Exception {
        // SPATIAL DOMAIN
        final UnidataSpatialDomain spatialDomain = new UnidataSpatialDomain();
        this.setSpatialDomain(spatialDomain);
        spatialDomain.setCoordinateReferenceSystem(coordinateReferenceSystem);

        spatialDomain.setReferencedEnvelope(reader.boundingBox);
        spatialDomain.setGridGeometry(getGridGeometry());
    }

    /**
     * 
     */
    private void initRange() {
        // set the rank
        rank = variableDS.getRank();
        
        width = variableDS.getDimension(rank - UnidataUtilities.X_DIMENSION).getLength();
        height = variableDS.getDimension(rank - UnidataUtilities.Y_DIMENSION).getLength();
        numBands = rank > 2 ? variableDS.getDimension(2).getLength() : 1;
        
        final int bufferType = UnidataUtilities.getRawDataType(variableDS);
        sampleModel = new BandedSampleModel(bufferType, width, height, 1);
        
        
        // range type
        String description = variableDS.getDescription();
        final StringBuilder sb = new StringBuilder();
        final Set<SampleDimension> sampleDims = new HashSet<SampleDimension>();
        sampleDims.add(new GridSampleDimension(description + ":sd", (Category[]) null, null));

        InternationalString desc = null;
        if (description != null && !description.isEmpty()) {
            desc = new SimpleInternationalString(description);
        }
        final FieldType fieldType = new DefaultFieldType(new NameImpl(getName()), desc, sampleDims);
        sb.append(description != null ? description.toString() + "," : "");
        final RangeType range = new DefaultRangeType(getName(), description, fieldType);
        this.setRangeType(range);
    }

    private void addAdditionalDomain(List<CoordinateVariable<?>> otherAxes, List<DimensionDescriptor> dimensions) {

        if (otherAxes == null||otherAxes.isEmpty()) {
            return;
        }
        final List<AdditionalDomain> additionalDomains = new ArrayList<AdditionalDomain>(otherAxes.size());
        this.setAdditionalDomains(additionalDomains);
        for(CoordinateVariable<?> cv :otherAxes){
            
            // create domain
            UnidataAdditionalDomain domain;
            try {
                domain = new UnidataAdditionalDomain(cv);
                additionalDomains.add(domain);
             // TODO: Parse Units from axis and map them to UCUM units
                dimensions.add(new DefaultDimensionDescriptor(cv.getName(), "FIXME_UNIT", "FIXME_UNITSYMBOL", cv.getName(), null));
                this.setHasAdditionalDomains(true);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, e.getMessage(), e);
            }
        }
    }

    /**
     * Extracts the {@link GridGeometry2D grid geometry} from the unidata variable.
     * 
     * @return the {@link GridGeometry2D}.
     * @throws IOException 
     */
    protected GridGeometry2D getGridGeometry() throws IOException {
        int[] low = new int[2];
        int[] high = new int[2];
        // String[] axesNames = new String[rank];
        double[] origin = new double[2];
        double scaleX=Double.POSITIVE_INFINITY, scaleY=Double.POSITIVE_INFINITY;

        for( CoordinateVariable<?> cv : reader.coordinatesVariables.values() ) {
            if(!cv.isNumeric()){
                continue;
            }
            final AxisType axisType = cv.getAxisType();
            switch (axisType) {
            case Lon: case GeoX:
                // raster space
                low[0] = 0;
                high[0] = (int) cv.getSize();
                
                // model space
                if(cv.isRegular()){
                    // regular model space
                    origin[0]=cv.getStart();
                    scaleX=cv.getIncrement();
                } else {
                    
                    // model space is not declared to be regular, but we kind of assume it is!!!
                    final int valuesLength=(int) cv.getSize();
                    double min = ((Number)cv.getMinimum()).doubleValue();
                    double max = ((Number)cv.getMaximum()).doubleValue();
                    // make sure we skip nodata coords, bah...
                    if (!Double.isNaN(min) && !Double.isNaN(max)) {
                        origin[0] = min;
                        scaleX = (max-min) / valuesLength;
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.log(Level.FINE, "Axis values contains NaN; finding first valid values");
                        }
                        for( int j = 0; j < valuesLength; j++ ) {
                            double v = ((Number)cv.read(j)).doubleValue();
                            if (!Double.isNaN(v)) {
                                for( int k = valuesLength; k > j; k-- ) {
                                    double vv = ((Number)cv.read(k)).doubleValue();
                                    if (!Double.isNaN(vv)) {
                                        origin[0] = v;
                                        scaleX = (vv - v) / valuesLength;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case Lat: case GeoY:
                // raster space
                low[1] = 0;
                high[1] = (int) cv.getSize();
                
                // model space
                if(cv.isRegular()){
                    scaleY=-cv.getIncrement();
                    origin[1]=cv.getStart()-scaleY*high[1];
                } else {
                    
                    // model space is not declared to be regular, but we kind of assume it is!!!
                    final int valuesLength=(int) cv.getSize();
                    double min = ((Number)cv.getMinimum()).doubleValue();
                    double max = ((Number)cv.getMaximum()).doubleValue();
                    // make sure we skip nodata coords, bah...
                    if (!Double.isNaN(min) && !Double.isNaN(max)) {
                        scaleY = -(max-min) / valuesLength;
                        origin[1] = max;
                    } else {
                        if (LOGGER.isLoggable(Level.FINE)) {
                            LOGGER.log(Level.FINE, "Axis values contains NaN; finding first valid values");
                        }
                        for( int j = 0; j < valuesLength; j++ ) {
                            double v = ((Number)cv.read(j)).doubleValue();
                            if (!Double.isNaN(v)) {
                                for( int k = valuesLength; k > j; k-- ) {
                                    double vv = ((Number)cv.read(k)).doubleValue();
                                    if (!Double.isNaN(vv)) {
                                        origin[1] = v;
                                        scaleY = -(vv - v) / valuesLength;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            default:
                break;
            }
        }

        final AffineTransform at = new AffineTransform(scaleX, 0, 0, scaleY, origin[0], origin[1]);
        final GridEnvelope gridRange = new GridEnvelope2D(
                low[0], 
                low[1], 
                high[0]-low[0], 
                high[1]-low[1]);
        final MathTransform raster2Model = ProjectiveTransform.create(at);
        return new GridGeometry2D(gridRange,PixelInCell.CELL_CORNER ,raster2Model, coordinateReferenceSystem,GeoTools.getDefaultHints());
    }

    public int getNumBands() {
        return numBands;
    }

    /**
     * @return the number of dimensions in the variable.
     */
    public int getRank() {
        return rank;
    }

    public SampleModel getSampleModel() {
        return sampleModel;
    }

    public UnidataVariableAdapter(UnidataImageReader reader, Name coverageName, VariableDS variable) throws Exception {
        this.variableDS = variable;
        this.reader = reader;
        this.coverageName = coverageName;
        setName(variable.getFullName());
        init();
    }

    @Override
    public UnidataSpatialDomain getSpatialDomain() {
        return (UnidataSpatialDomain) super.getSpatialDomain();
    }

    @Override
    public UnidataTemporalDomain getTemporalDomain() {
        return (UnidataTemporalDomain) super.getTemporalDomain();
    }

    @Override
    public UnidataVerticalDomain getVerticalDomain() {
        return (UnidataVerticalDomain) super.getVerticalDomain();
    }

    /**
     * @return
     */
    int getWidth() {
        return width;
    }

    /**
     * @return
     */
    int getHeight() {
        return height;
    }

    /**
     * Utility method to retrieve the z-index of a Variable coverageDescriptor stored on
     * {@link NetCDFImageReader} NetCDF Flat Reader {@link HashMap} indexMap.
     * 
     * @param imageIndex
     *                {@link int}
     * 
     * @return z-index {@link int} -1 if variable rank &lt; 3
     */
    public int getZIndex(int index) {
    
        if (rank > 2) {
            if (rank == 3) {
                return index;
            } else if (rank == 4){
                // return (int) Math.ceil((imageIndex - range.first()) /
                // var.getDimension(rank - (Z_DIMENSION + 1)).getLength());
                return index % UnidataUtilities.getZDimensionLength(variableDS);
            } else {
                throw new IllegalStateException("Unable to handle more than 4 dimensions");
            }
        }
    
        return -1;
    }

    /**
     * Utility method to retrieve the t-index of a Variable coverageDescriptor stored on
     * {@link NetCDFImageReader} NetCDF Flat Reader {@link HashMap} indexMap.
     * 
     * @param imageIndex
     *                {@link int}
     * 
     * @return t-index {@link int} -1 if variable rank > 4
     */
    public int getTIndex(int index) {
    
        if (rank > 2) {
            if (rank == 3) {
                return index;
            } else {
                // return (imageIndex - range.first()) % var.getDimension(rank -
                // (Z_DIMENSION + 1)).getLength();
                return (int) Math.ceil(index
                        / UnidataUtilities.getZDimensionLength(variableDS));
            }
        }
    
        return -1;
    }

    /**
     * @return the numberOfSlices
     */
    public int getNumberOfSlices() {
        return numberOfSlices;
    }

    /**
     * @return the shape
     */
    public int[] getShape() {
        return shape;
    }

    /**
     * Return features for that variable adapter, starting from slices with index = "startIndex", and up to "limit" elements.
     * This allows for paging. Put the created features inside the provided collection
     * 
     * @param startIndex the first slice to be returned
     * @param limit the max number of features to be created
     * @param collection the feature collection where features need to be stored
     */
    public void getFeatures(final int startIndex, final int limit, final ListFeatureCollection collection) {
        final boolean hasVerticalAxis = coordinateSystem.hasVerticalAxis();
        final SimpleFeatureType indexSchema = collection.getSchema();
        final int bandDimension = rank - UnidataUtilities.Z_DIMENSION;
        final int slicesNum = getNumberOfSlices();
        if (startIndex > slicesNum) {
            throw new IllegalArgumentException("The paging start index can't be higher than the number of available slices");
        }
        int lastIndex = startIndex + limit;
        if (lastIndex > slicesNum) {
            lastIndex = slicesNum;
        }
        final String varName = variableDS.getFullName();
        for (int imageIndex = startIndex; imageIndex < lastIndex; imageIndex++) {
            int zIndex = -1;
            int tIndex = -1;
            for (int i = 0; i < rank; i++) {
                switch (rank - i) {
                case UnidataUtilities.X_DIMENSION:
                case UnidataUtilities.Y_DIMENSION:
                    break;
                default: {
                    if (i == bandDimension && hasVerticalAxis) {
                        zIndex = getZIndex(imageIndex);
                    } else {
                        tIndex =  getTIndex(imageIndex);
                    }
                    break;
                }
                }
            }

            //Put a new sliceIndex in the list
            final UnidataSlice2DIndex variableIndex = new UnidataSlice2DIndex(tIndex, zIndex, varName);
            reader.ancillaryFileManager.addSlice(variableIndex);

            // Create a feature for that index to be put in the CoverageSlicesCatalog
            final SimpleFeature feature = createFeature(
                    variableDS, 
                    coverageName.toString(), 
                    tIndex, 
                    zIndex, 
                    coordinateSystem, 
                    imageIndex, 
                    indexSchema);
            collection.add(feature);
        }
    }

    /**
     * Create a SimpleFeature on top of the provided variable and indexes.
     * 
     * @param variable the input variable 
     * @param tIndex the time index 
     * @param zIndex the zeta index
     * @param cs the {@link CoordinateSystem} associated with that variable
     * @param imageIndex the index to be associated to the feature in the index
     * @param indexSchema the schema to be used to create the feature
     * @param geometry the geometry to be attached to the feature
     * @return the created {@link SimpleFeature}
     * TODO move to variable wrapper
     */
    private SimpleFeature createFeature(
            final Variable variable,
            final String coverageName,
            final int tIndex,
            final int zIndex,
            final CoordinateSystem cs,
            final int imageIndex, 
            final SimpleFeatureType indexSchema) {
        
        final Date date = getTimeValueByIndex(variable, tIndex, cs);
        final Number verticalValue = getVerticalValueByIndex(variable, zIndex, cs);

        final SimpleFeature feature = DataUtilities.template(indexSchema);
        feature.setAttribute(CoverageSlice.Attributes.GEOMETRY, UnidataCRSUtilities.GEOM_FACTORY.toGeometry(reader.boundingBox));
        feature.setAttribute(CoverageSlice.Attributes.INDEX, imageIndex);

        // TIME management
        // Check if we have time and elevation domain and set the attribute if needed
        if (date != null) {
            feature.setAttribute(reader.dimensionsMapping.get(UnidataUtilities.TIME_DIM), date);
        }

        // ELEVATION or other dimension
        final String elevationCVName = reader.dimensionsMapping.get(UnidataUtilities.ELEVATION_DIM);
        if (!Double.isNaN(verticalValue.doubleValue())) {
            List<AttributeDescriptor> descriptors = indexSchema.getAttributeDescriptors();
            String attribute = null;
            
            // Once we don't deal anymore with old coverage APIs, we can consider directly use the dimension name as attribute
            for (AttributeDescriptor descriptor: descriptors) {
                if (descriptor.getLocalName().equalsIgnoreCase(elevationCVName)) {
                    attribute = elevationCVName;
                    break;
                }
            }
            
            // custom dimension, mapped to an attribute using its name
            if (attribute == null) {
                // Assuming the custom dimension is always the last attribute
                attribute = variable.getDimension(0).getShortName();
            }
            feature.setAttribute(attribute, verticalValue);
        }
        return feature;
    }
    
    /** Return the zIndex-th value of the vertical dimension of the specified variable, as a double, or {@link Double#NaN} 
     * in case that variable doesn't have a vertical axis.
     * 
     * @param unidataReader the reader to be used for that search
     * @param variable the variable to be accessed
     * @param timeIndex the requested index
     * @param cs the coordinateSystem to be scan
     * @return
     * TODO move to variable wrapper
     */
    private Number getVerticalValueByIndex(Variable variable, final int zIndex,
            final CoordinateSystem cs ) {
        double ve = Double.NaN;
        if (cs != null && cs.hasVerticalAxis()) {
            final int rank = variable.getRank();
    
            final Dimension verticalDimension = variable.getDimension(rank - UnidataUtilities.Z_DIMENSION);
            return (Number) reader.coordinatesVariables.get(verticalDimension.getFullName()).read(zIndex);
        }
        return ve;
    }

    /** Return the timeIndex-th value of the time dimension of the specified variable, as a Date, or null in case that
     * variable doesn't have a time axis.
     * 
     * @param unidataReader the reader to be used for that search
     * @param variable the variable to be accessed
     * @param timeIndex the requested index
     * @param cs the coordinateSystem to be scan
     * @return
     * TODO move to variable wrapper
     */
    private Date getTimeValueByIndex( Variable variable, int timeIndex,
            final CoordinateSystem cs ) {
    
        if (cs != null && cs.hasTimeAxis()) {
            final int rank = variable.getRank();
            final Dimension temporalDimension = variable.getDimension(rank
                    - ((cs.hasVerticalAxis() ? UnidataUtilities.Z_DIMENSION : 2) + 1));
            return (Date) reader.coordinatesVariables.get(temporalDimension.getFullName()).read(timeIndex);
        }
    
        return null;
>>>>>>> 76aa07461566a5976980e6696204781271955163
    }
}

