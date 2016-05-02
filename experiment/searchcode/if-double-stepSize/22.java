package at.fhjoanneum.eht;

import at.fhjoanneum.eht.calc.IDataCalculationStrategy;
import at.fhjoanneum.eht.calc.ParallelCalculationStrategyInstantMultiWindow;
import at.fhjoanneum.eht.calc.ParallelCalculationStrategyInstantSingleWindow;
import at.fhjoanneum.eht.gui.ROILayerDTO;
import at.fhjoanneum.eht.params.OP_SEMIAUTO;
import at.fhjoanneum.eht.utils.Utils;
import at.fhjoanneum.eht.weka.WekaClassifier;
import at.fhjoanneum.eht.weka.WekaFileUtil;
import at.mug.iqm.api.model.CustomDataModel;
import at.mug.iqm.api.model.IqmDataBox;
import at.mug.iqm.api.model.TableModel;
import at.mug.iqm.api.operator.*;
import at.mug.iqm.commons.util.image.ImageTools;
import org.apache.log4j.Logger;
import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.Instances;

import javax.media.jai.*;
import java.awt.*;
import java.awt.image.*;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * This class represents the algorithm of this IQM operator.
 *
 * @author J端rgen Kleinowitz
 */
public class SemiautoSegmentationOp extends AbstractOperator {

    private static final Logger logger = Logger.getLogger(SemiautoSegmentationOp.class);

    private int windowSize;
    private int stepSize;
    private List<String> classNames;
    private List<Color> classColors;
    private int scaledWidth;
    private int scaledHeight;
    private boolean extendBorder = true;

    private PlanarImage baseImage;
    private PlanarImage workImage;
    private TableModel allImageData;
    private WekaClassifier wekaClassifier;
    private Object loadedCls;

    private boolean paramsChanged = true; //Determines wheter Data needs to be recalculated
    private Result result;

    private State currentState = State.READY;
    private List<ROILayerDTO> roiLayers;
    private int packageSize;


    private IDataCalculationStrategy calculationStrategy = new ParallelCalculationStrategyInstantSingleWindow();
    private boolean createFeatureImages = true;

    public void reset() {
        currentState = State.READY;
        loadedCls = null;
        allImageData = null;
        classNames = null;
        classColors = null;
        workImage = null;
        baseImage = null;

        paramsChanged = true;
    }

    enum State {READY, START, CALCULATING, CLASSIFYING, TRAINING, EXTERNAL_CLS, DONE, VALIDATION, ERROR}

    /**
     * Gets the unique name of the operator.
     * <p/>
     * This method is merely a wrapper for the unique name of the operator
     * stored in the operator's associated {@link IOperatorDescriptor}.
     *
     * @return the name of the operator, declared in the operator's descriptor
     */
    public String getName() {
        if (this.name == null) {
            this.name = new SemiautoSegmentationOpDescriptor().getName();
        }
        return this.name;
    }

    /**
     * Gets the {@link OperatorType} of the operator.
     * <p/>
     * This method is merely a wrapper for the type of the operator stored in
     * the operator's associated {@link IOperatorDescriptor}.
     *
     * @return the type of the operator, declared in the operator's descriptor
     */
    public OperatorType getType() {
        if (this.type == null) {
            this.type = SemiautoSegmentationOpDescriptor.TYPE;
        }
        return this.type;
    }

    /**
     * This is the starting point for the execution of the operator.
     * <p/>
     * The algorithm code is launched using the {@link #run(IWorkPackage)}
     * method and returns a {@link IResult}.
     *
     * @param wp the work package for this algorithm
     * @return a result of the processed work package
     */
    @Override
    public IResult run(IWorkPackage wp) throws Exception {
        currentState = State.VALIDATION;

        if (!new SemiautoSegmentationOpValidator().validateExecution(wp)) {
            //This if clause is never reached because the validator already throws an exception and only ever returns true if anything
            currentState = State.ERROR;
            throw new IllegalArgumentException("Validation error");
        }

        currentState = State.START;
        result = new Result();

        logger.debug("Starting SemiauotSegmentationOp");

        retrieveParams(wp.getParameters());

        if (needsRecalculation()) {
            currentState = State.CALCULATING;

            //Add Border and convert to greyscale
            workImage = doImagePreProcessing(baseImage);

            //Calculate the statistical values
            allImageData = createFullImageStatistics(workImage);


        }

        if (externalClassifierLoaded()) {
            currentState = State.EXTERNAL_CLS;
            getClassNamesAndColors();

            //Only create classification set and use already trained classifier
            wekaClassifier = new WekaClassifier(allImageData, loadedCls);
        } else {
            currentState = State.TRAINING;
            //Names and colors of the drawing layers
            getClassNamesAndColors(roiLayers);
            //Assign class values based on ROIs
            assignClassValue(roiLayers);
            //Create all data and train classifier
            wekaClassifier = new WekaClassifier(allImageData);

            CustomDataModel cdm = new CustomDataModel();
            cdm.setContent(new Object[] {wekaClassifier.getEvaluationResult()});
            IqmDataBox box = new IqmDataBox(cdm);
            result.addItem(box);
        }
        currentState = State.CLASSIFYING;
        wekaClassifier.useClassifier();

        PlanarImage resImage = createResultImage(wekaClassifier.getInstancesObject(WekaClassifier.CLASSIFIED_SET));
        PlanarImage overlayedImage = Utils.createOverlayImage(baseImage, resImage);
        result.addItem(Utils.createIqmDataBox(overlayedImage, overlayedImage.getProperty("model_name").toString() + "_overlay"));
        result.addItem(Utils.createIqmDataBox(resImage, resImage.getProperty("model_name").toString()));
        result.addItem(Utils.generateNewImageModel(workImage, "work_image"));

        if (createFeatureImages) {
            createAndAddFeatureImages(result, wekaClassifier.getInstancesObject(WekaClassifier.CLASSIFICATION_SET));
        }

        currentState = State.DONE;
        return result;
    }

    private void getClassNamesAndColors() {
        classColors = new ArrayList<>();
        classNames = new ArrayList<>();

        classColors.addAll(Arrays.asList(Color.RED, Color.GREEN, Color.BLUE, Color.ORANGE, Color.YELLOW));
    }

    private PlanarImage doImagePreProcessing(PlanarImage img) {
        //TODO: Resizing maybe?
        //img = resizeImage(0.5f, img);

        if (extendBorder) {
            img = Utils.addBorder(img, windowSize / 2);
        }
        //String baseImgName = baseImage.getProperty("image_name").toString();
        //String baseFileName = baseImage.getProperty("file_name").toString();

        double numBands = img.getNumBands();
        if (numBands > 1) {
            img = toGrayScale(img);
        }
        return img;
    }

    public boolean externalClassifierLoaded() {
        return loadedCls != null;
    }

    private boolean needsRecalculation() {
        return allImageData == null || paramsChanged;
    }

    /**
     * Retrieve param values. If they have changed, paramsChanged will be set to true
     * @param pb
     */
    private void retrieveParams(ParameterBlockIQM pb) {
        int windowSizeOld = windowSize;
        int stepSizeOld = stepSize;
        String imgHashOld;
        if (baseImage == null) {
            imgHashOld = "";
        } else {
            imgHashOld = Utils.calcImageHash(baseImage);
        }

        windowSize = pb.getIntParameter(OP_SEMIAUTO.WINDOW_SIZE.pbKey());
        stepSize = pb.getIntParameter(OP_SEMIAUTO.STEP.pbKey());
        baseImage = ((IqmDataBox) pb.getSource(0)).getImage();
        Object cls = pb.getObjectParameter(OP_SEMIAUTO.CLASSIFIER.pbKey());
        if (cls != null) {
            loadedCls = cls;
        } else {
            roiLayers = (List<ROILayerDTO>) pb.getObjectParameter(OP_SEMIAUTO.LAYERS.pbKey());
            //This ensures that in case of overlapping Layer shapes, pixels get the class of the layer with the highest z-order
            Collections.reverse(roiLayers);
        }
        packageSize = pb.getIntParameter(OP_SEMIAUTO.PACKAGE_SIZE.pbKey());

        String imgHashNew = Utils.calcImageHash(baseImage);

        //If any of these parameters change, data needs to be recalulated
        if (windowSizeOld != windowSize || stepSizeOld != stepSize || !imgHashOld.equals(imgHashNew)) {
            paramsChanged = true;
        } else {
            paramsChanged = false;
        }
    }

    private void assignClassValue(List<ROILayerDTO> drawingLayers) {
        List<ROIShape> combinedLayershapes = getCombinedLayerShapes(drawingLayers);
        for (int index = 0; index < allImageData.getRowCount(); index++) {
            int i = (index / scaledHeight) * stepSize;
            int j = (index % scaledHeight) * stepSize;

            String classVal = "";
            for (ROIShape combinedShape : combinedLayershapes) {
                if (combinedShape.contains(i, j)) {
                    classVal = classNames.get(combinedLayershapes.indexOf(combinedShape));
                }
                if (classVal.isEmpty()) {
                    classVal = WekaClassifier.NONE_VALUE;
                }
            }
            allImageData.setValueAt(classVal, index, allImageData.getColumnCount() - 1);
        }
    }

    private void createAndAddFeatureImages(Result res, Instances instancesObject) {

        int numAttr = instancesObject.numAttributes();
        int numRows = instancesObject.numInstances();
        HashMap<String, ArrayList<Double>> columnValues = new HashMap<>();
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numAttr; j++) {
                String key = instancesObject.attribute(j).name();
                Double attrVal = instancesObject.instance(i).value(j);
                if (!columnValues.containsKey(key)) {
                    ArrayList<Double> doubeList = new ArrayList<>();
                    doubeList.add(attrVal);
                    columnValues.put(key, doubeList);
                } else {
                    columnValues.get(key).add(attrVal);
                }
            }
        }
        for (Map.Entry<String, ArrayList<Double>> entry : columnValues.entrySet()) {
            PlanarImage pi = createImage(entry.getValue());
            res.addItem(Utils.createIqmDataBox(pi, entry.getKey()));
        }
    }

    private PlanarImage createImage(ArrayList<Double> featureValues) {
        if (scaledHeight * scaledWidth != featureValues.size()) {
            throw new IllegalArgumentException("Error: Something went terrtibly wrong");
        }

        TiledImage tiledImage = createEmptyTiledImage(scaledWidth, scaledHeight, 1);
        WritableRaster raster = tiledImage.getWritableTile(0, 0);

        for (int i = 0; i < scaledWidth; i++) {
            for (int j = 0; j < scaledHeight; j++) {
                int index = i * scaledHeight + j;
                Double d = featureValues.get(index) * 255;
                raster.setSample(i, j, 0, d);
            }
        }
        return resizeToBaseImage(baseImage, tiledImage);
    }

    private PlanarImage createResultImage(Instances instancesObject) {
        if (scaledHeight * scaledWidth != instancesObject.size()) {
            throw new IllegalArgumentException("Error: Something went terrtibly wrong");
        }
        TiledImage tiledImage = createEmptyTiledImage(scaledWidth, scaledHeight, 3);
        WritableRaster raster = tiledImage.getWritableTile(0, 0);
        Attribute classAttr = instancesObject.classAttribute();

        for (int i = 0; i < scaledWidth; i++) {
            for (int j = 0; j < scaledHeight; j++) {
                int index = i * scaledHeight + j;
                Double d = instancesObject.instance(index).classValue();
                Color col = getColor(classAttr, d);
                raster.setSample(i, j, 0, col.getRed());
                raster.setSample(i, j, 1, col.getGreen());
                raster.setSample(i, j, 2, col.getBlue());
            }
        }
        return resizeToBaseImage(baseImage, tiledImage);
    }

    private Color getColor(Attribute classAttr, Double d) {
        Color col;
        if (classAttr.isInRange(d)) {
            String clasVal = classAttr.value(d.intValue());
            col = getClassColor(clasVal);
        } else {
            col = getClassColor(d.intValue() - 1);
        }
        return col;
    }

    private TiledImage createEmptyTiledImage(int width, int height, int bands) {
        SampleModel sampleModel = RasterFactory.createBandedSampleModel(DataBuffer.TYPE_BYTE,
                width, height, bands);
        ColorModel cm = TiledImage.createColorModel(sampleModel);
        return new TiledImage(0, 0, scaledWidth, scaledHeight, 0, 0, sampleModel, cm);
    }

    private Color getClassColor(String clasVal) {
        if (!classNames.contains(clasVal)) {
            classNames.add(clasVal);
        }
        return classColors.get(classNames.indexOf(clasVal));
    }

    private Color getClassColor(int index) {
        if (classColors.size() > index && index >= 0) {
            return classColors.get(index);
        } else {
            Random rand = new Random();
            float r = rand.nextFloat();
            float g = rand.nextFloat();
            float b = rand.nextFloat();
            return new Color(r, g, b);
        }
    }


    private PlanarImage resizeToBaseImage(PlanarImage baseImage, TiledImage tiledImage) {
        float scaleW = (float) baseImage.getWidth() / scaledWidth;
        PlanarImage resizedImage = Utils.resizeImage(scaleW, tiledImage);
        resizedImage.setProperty("model_name", "W:" + windowSize + "_S" + stepSize);
        resizedImage.setProperty("image_name", "W:" + windowSize + "_S" + stepSize);
        return resizedImage;
    }

    private void getClassNamesAndColors(List<ROILayerDTO> drawingLayers) {
        classColors = new ArrayList<>();
        classNames = new ArrayList<>();

        for (ROILayerDTO layer : drawingLayers) {
            classNames.add(layer.getName());
            classColors.add(layer.getColor());
        }
        classNames.add(WekaClassifier.NONE_VALUE);
        classColors.add(Color.WHITE);
    }

    private TableModel createFullImageStatistics(PlanarImage pi) throws Exception {
        int startValue;
        int endValueX;
        int endValueY;

        if (extendBorder) {
            startValue = 0;
            endValueX = pi.getTileWidth();
            endValueY = pi.getTileHeight();
        } else {
            startValue = windowSize / 2;
            endValueX = pi.getTileWidth() - windowSize / 2;
            endValueY = pi.getTileHeight() - windowSize / 2;
        }

        scaledWidth = (int) Math.ceil(((double) endValueX - (double) startValue) / (double) stepSize);
        scaledHeight = (int) Math.ceil(((double) endValueY - (double) startValue) / (double) stepSize);

        return calculationStrategy.calculate(pi, windowSize, stepSize, startValue, endValueX, endValueY);
    }

    /**
     * Combine all roi shapes of a drawing layer to a single ROI
     *
     * @param drawingLayers
     * @return
     */
    private List<ROIShape> getCombinedLayerShapes(List<ROILayerDTO> drawingLayers) {
        List<ROIShape> combinedLayershapes = new ArrayList<>();
        for (ROILayerDTO layer : drawingLayers) {
            ROIShape combinedShape = new ROIShape(new Rectangle(0, 0, 0, 0));
            for (ROIShape shape : layer.getRoiShapes()) {
                combinedShape = (ROIShape) combinedShape.add(shape);
            }
            combinedLayershapes.add(combinedShape);
        }
        return combinedLayershapes;
    }

    private PlanarImage toGrayScale(PlanarImage pi) {
        logger.debug("NumBands > 1, converting to grayscale image");

        List<Integer> bands = new ArrayList<Integer>();
        for (int i = 0; i < pi.getNumBands(); i++) {
            bands.add(i + 1);
        }
        return ImageTools.combineChannels(pi, bands);
    }

    public void saveTrainedModel(File file) throws Exception {
        if (currentState.equals(State.DONE)) {
            wekaClassifier.saveModel(file);
        }
    }

    public void saveArffFiles(File file) {
        if (currentState.equals(State.DONE)) {
            WekaFileUtil.saveAllInstances(file, wekaClassifier);
        }
    }

    public State getCurrentState() {
        return currentState;
    }


    public IDataCalculationStrategy getCalculationStrategy() {
        return calculationStrategy;
    }

    public void setCalculationStrategy(IDataCalculationStrategy calculationStrategy) {
        this.calculationStrategy = calculationStrategy;
    }

}

