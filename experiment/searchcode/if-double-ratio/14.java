package trussoptimizater.Truss.Optimize;

import trussoptimizater.Truss.TrussModel;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Observable;
import java.util.TimeZone;
import javax.swing.JOptionPane;
import javax.swing.SwingWorker;
import org.jgap.*;
import org.jgap.Genotype;
import org.jgap.IChromosome;
import org.jgap.impl.CrossoverOperator;
import org.jgap.impl.DefaultConfiguration;
import org.jgap.impl.IntegerGene;
import org.jgap.impl.MutationOperator;
import trussoptimizater.Truss.Elements.Bar;
import trussoptimizater.Truss.Elements.Node;
import trussoptimizater.Truss.Sections.TubularSection;
import trussoptimizater.Gui.SwingUtils;
import trussoptimizater.Truss.SectionLibrary;

/**
 * 
 * NOTE:
 * The following situation has not been solved:
 * You want to keep symmetry
 * Your symmetry Line is angled
 * You have nodes on the symmetry line
 * The nodes that are on the symmetry line are allowed to move in the x,y or both direction.
 * @author Chris
 */
public class GAOptimizer extends FitnessFunction {

    /* Object so can access methods in rest of project*/
    private TrussModel truss;
    private Genotype population;
    private int currentEvolution = 0;
    //private MySwingWorker evolve = new MySwingWorker(currentEvolution);
    private boolean verbose = false;
    private GAModel gaModel;
    /*The array indexes of nodes that do not have loads or are supportes*/
    private ArrayList<Integer> freeNodeIndexes;
    /*The array indexes of nodes that have loads on them*/
    private ArrayList<Integer> loadedNodeIndexes;
    /*The array indexes of nodes that are supported*/
    private ArrayList<Integer> supportedNodeIndexes;
    /*These indexs are used when you want to optimize sections sizes*/
    private ArrayList<Integer> braceIndexes;
    private ArrayList<Integer> chordIndexes;
    public static final int STOP_STATE = 1;
    public static final int PAUSE_STATE = 2;
    public static final int PLAY_STATE = 3;
    private int state = STOP_STATE;
    private ObserableSwingWorker obserableSwingWorker;


    //Dynamic grid variables
    double gridReduction = 0.8;
    double toleranceReduction = gridReduction;
    double fitnessReduction = 0.8;

    public GAOptimizer(TrussModel truss) {
        this.gaModel = new GAModel();
        this.truss = truss;
        obserableSwingWorker = new ObserableSwingWorker();
    }

    //Returns node indexes of nodes that are not loaded or supported*/
    private ArrayList<Integer> calculateFreeNodeIndexes(boolean keepSymmetry) {

        ArrayList<Integer> freeNodeIndexes = new ArrayList<Integer>();
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            if (!keepSymmetry && !truss.getNodeModel().get(i).isSupported() && !truss.getNodeModel().get(i).isLoaded()) {
                freeNodeIndexes.add(i);
            }

            if (keepSymmetry
                    && gaModel.getSymmetryAxis().isLeftorBelowLine(truss.getNodeModel().get(i).getPoint2D())
                    && !truss.getNodeModel().get(i).isSupported()
                    && !truss.getNodeModel().get(i).isLoaded()) {
                freeNodeIndexes.add(i);
            }
        }

        return freeNodeIndexes;
    }

    //Return indexes of nodes that are loaded
    private ArrayList<Integer> calculateLoadedNodeIndexes(boolean keepSymmetry) {

        ArrayList<Integer> loadedNodeIndexes = new ArrayList<Integer>();
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            if (!keepSymmetry && truss.getNodeModel().get(i).isLoaded() && !truss.getNodeModel().get(i).isSupported()) {
                loadedNodeIndexes.add(i);
            }

            if (keepSymmetry
                    && gaModel.getSymmetryAxis().isLeftorBelowLine(truss.getNodeModel().get(i).getPoint2D())
                    && truss.getNodeModel().get(i).isLoaded()
                    && !truss.getNodeModel().get(i).isSupported()) {
                loadedNodeIndexes.add(i);
                //optimizeModel.getSymmetryAxis().re
            }
        }


        return loadedNodeIndexes;
    }

    //Return indexes of nodes that are supported
    private ArrayList<Integer> calculateSupportedNodeIndexes(boolean keepSymmetry) {


        ArrayList<Integer> supportedNodeIndexes = new ArrayList<Integer>();
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            if (!keepSymmetry && truss.getNodeModel().get(i).isSupported() && !truss.getNodeModel().get(i).isLoaded()) {
                supportedNodeIndexes.add(i);
            }

            if (keepSymmetry
                    && gaModel.getSymmetryAxis().isLeftorBelowLine(truss.getNodeModel().get(i).getPoint2D())
                    && truss.getNodeModel().get(i).isSupported()
                    && !truss.getNodeModel().get(i).isLoaded()) {
                supportedNodeIndexes.add(i);
            }
        }
        return supportedNodeIndexes;


    }

    private ArrayList<Integer> calculateBraceIndexes() {
        ArrayList<Integer> braceIndexes = new ArrayList<Integer>();
        for (int i = 0; i < truss.getBarModel().size(); i++) {
            if (truss.getBarModel().get(i).getRestraint().equals(Bar.PINNED_PINNED_RESTRAINT)) {
                braceIndexes.add(i);
            }
        }
        return braceIndexes;
    }

    private ArrayList<Integer> calculateChordIndexes() {
        ArrayList<Integer> chordIndexes = new ArrayList<Integer>();
        for (int i = 0; i < truss.getBarModel().size(); i++) {
            if (truss.getBarModel().get(i).getRestraint().equals(Bar.FIXED_FIXED_RESTRAINT)) {
                chordIndexes.add(i);
            }
        }
        return chordIndexes;
    }

    public boolean checkTrussIsSymmetrical() {
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            if (truss.getNodeModel().get(i).getSymmetryNode() == null) {
                //System.out.println("Node "+truss.getNodeModel().get(i).getNumber()+ " is not symmteric "+truss.getNodeModel().get(i).getPoint());
                Object[] options = {"OK", "Cancel"};
                int n = JOptionPane.showOptionDialog(null, "Truss must be symmetrical around the axis\nNode " + truss.getNodeModel().get(i).getNumber()
                        + " is NOT symmetrical\n\n"
                        + "Optimize without keeping symmetry?", "Alert", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
                if (n == 0) {
                } else if (n == 1) {
                    return false;
                }

                gaModel.setKeepSymmetry(false);
                setAllSymmetryNodesToNull();

            }
        }
        return true;
    }

    public void calculateSymmetricNodes() {

        Point2D symmetryPoint2D;
        Point2D symmetryPoint;
        //System.out.println("Printing symmetrical nodes");
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            symmetryPoint2D = gaModel.getSymmetryAxis().getReflectedPoint(truss.getNodeModel().get(i).getPoint2D());
            symmetryPoint = SwingUtils.point2DtoPoint(symmetryPoint2D);
            for (int j = 0; j < truss.getNodeModel().size(); j++) {
                if (symmetryPoint.getX() - truss.getNodeModel().get(j).getPoint().getX() <= 0.0001
                        && symmetryPoint.getX() - truss.getNodeModel().get(j).getPoint().getX() >= -0.0001
                        && symmetryPoint.getY() - truss.getNodeModel().get(j).getPoint().getY() <= 0.0001
                        && symmetryPoint.getY() - truss.getNodeModel().get(j).getPoint().getY() >= -0.0001) {
                    try {
                        truss.getNodeModel().get(i).setSymmetryNode((Node) truss.getNodeModel().get(j).clone());
                    } catch (CloneNotSupportedException ex) {
                        System.out.println("Error trying to clone symmetery node " + ex);
                    }
                    break;
                }
            }
        }
    }

    public GAModel getGAModel() {
        return gaModel;
    }

    public void setAllSymmetryNodesToNull() {
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            truss.getNodeModel().get(i).setSymmetryNode(null);
        }
    }

    public boolean initElementArrays() {

        setAllSymmetryNodesToNull();
        if (gaModel.isKeepSymmetry()) {
            calculateSymmetricNodes();
            if (!checkTrussIsSymmetrical()) {
                return false;
            }
        }


        freeNodeIndexes = calculateFreeNodeIndexes(gaModel.isKeepSymmetry());
        loadedNodeIndexes = calculateLoadedNodeIndexes(gaModel.isKeepSymmetry());
        supportedNodeIndexes = calculateSupportedNodeIndexes(gaModel.isKeepSymmetry());
        braceIndexes = calculateBraceIndexes();
        chordIndexes = calculateChordIndexes();
        return true;

    }

    public int getGeneCount() {
        int geneCount = 0;
        //Add one as we want all chords to be same section
        if (gaModel.isOptimizeSections()) {
            geneCount += braceIndexes.size() + 1;
        }

        if (gaModel.isOptimizeSupportedNodesX()) {
            geneCount += supportedNodeIndexes.size();
        }
        if (gaModel.isOptimizeSupportedNodesY()) {
            geneCount += supportedNodeIndexes.size();
        }

        if (gaModel.isOptimizeLoadedNodesX()) {
            geneCount += loadedNodeIndexes.size();
        }
        if (gaModel.isOptimizeLoadedNodesY()) {
            geneCount += loadedNodeIndexes.size();
        }

        if (gaModel.isOptimizeFreeNodesX()) {
            geneCount += freeNodeIndexes.size();
        }
        if (gaModel.isOptimizeFreeNodesY()) {
            geneCount += freeNodeIndexes.size();
        }
        return geneCount;
    }

    private Gene[] addGenes(Configuration conf) throws Exception {

        int geneIndex = 0;

        Gene[] sampleGenes = new Gene[getGeneCount()];

        System.out.println("Adding genes");

        if (gaModel.isOptimizeSections()) {
            //System.out.println("Optimizing Sections");
            for (int i = 0; i < braceIndexes.size() + 1; i++) {
                sampleGenes[geneIndex++] = new IntegerGene(conf, gaModel.getMinSectionIndex(), gaModel.getMaxSectionIndex());
            }

        }

        if (gaModel.isOptimizeSupportedNodesX()) {
            //System.out.println("Optimizing Supported Nodes X");
            for (int i = 0; i < supportedNodeIndexes.size(); i++) {
                int nodeIndex = supportedNodeIndexes.get(i);
                Point node = truss.getNodeModel().get(nodeIndex).getPoint();
                if (gaModel.isKeepSymmetry()
                        && gaModel.getSymmetryAxis().contains(truss.getNodeModel().get(nodeIndex).getPoint2D())
                        && gaModel.getSymmetryAxis().isVertical()) {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.x, node.x, gaModel.getNodalGrid());
                } else {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.x - gaModel.getMaxNodalXDisplacement(), node.x + gaModel.getMaxNodalXDisplacement(), gaModel.getNodalGrid());
                }

            }

        }




        if (gaModel.isOptimizeSupportedNodesY()) {
            //System.out.println("Optimizing Supported Nodes Y");
            for (int i = 0; i < supportedNodeIndexes.size(); i++) {
                int nodeIndex = supportedNodeIndexes.get(i);
                Point node = truss.getNodeModel().get(nodeIndex).getPoint();
                //sampleGenes[geneIndex++] = new MutipleIntegerGene(conf, node.y - optimizeModel.getMaxNodalYDisplacement(), node.y + optimizeModel.getMaxNodalYDisplacement(), optimizeModel.getNodalGrid());
                if (gaModel.isKeepSymmetry()
                        && gaModel.getSymmetryAxis().contains(truss.getNodeModel().get(nodeIndex).getPoint2D())
                        && gaModel.getSymmetryAxis().isHorizotal()) {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.y, node.y, gaModel.getNodalGrid());
                } else {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.y - gaModel.getMaxNodalYDisplacement(), node.y + gaModel.getMaxNodalYDisplacement(), gaModel.getNodalGrid());
                }

            }
        }


        if (gaModel.isOptimizeLoadedNodesX()) {
            //System.out.println("Optimizing Loaded Nodes x");
            for (int i = 0; i < loadedNodeIndexes.size(); i++) {
                int nodeIndex = loadedNodeIndexes.get(i);
                Point node = truss.getNodeModel().get(nodeIndex).getPoint();
                if (gaModel.isKeepSymmetry()
                        && gaModel.getSymmetryAxis().contains(truss.getNodeModel().get(nodeIndex).getPoint2D())
                        && gaModel.getSymmetryAxis().isVertical()) {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.x, node.x, gaModel.getNodalGrid());
                } else {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.x - gaModel.getMaxNodalXDisplacement(), node.x + gaModel.getMaxNodalXDisplacement(), gaModel.getNodalGrid());
                }

            }
        }

        if (gaModel.isOptimizeLoadedNodesY()) {
            //System.out.println("Optimizing Loaded Nodes Y");
            for (int i = 0; i < loadedNodeIndexes.size(); i++) {
                int nodeIndex = loadedNodeIndexes.get(i);
                Point node = truss.getNodeModel().get(nodeIndex).getPoint();
                //sampleGenes[geneIndex++] = new MutipleIntegerGene(conf, node.y - optimizeModel.getMaxNodalYDisplacement(), node.y + optimizeModel.getMaxNodalYDisplacement(), optimizeModel.getNodalGrid());
                if (gaModel.isKeepSymmetry()
                        && gaModel.getSymmetryAxis().contains(truss.getNodeModel().get(nodeIndex).getPoint2D())
                        && gaModel.getSymmetryAxis().isHorizotal()) {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.y, node.y, gaModel.getNodalGrid());
                } else {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.y - gaModel.getMaxNodalYDisplacement(), node.y + gaModel.getMaxNodalYDisplacement(), gaModel.getNodalGrid());
                }

            }
        }


        if (gaModel.isOptimizeFreeNodesX()) {
            //System.out.println("Optimizing Free Nodes x");
            for (int i = 0; i < freeNodeIndexes.size(); i++) {
                int nodeIndex = freeNodeIndexes.get(i);
                Point node = truss.getNodeModel().get(nodeIndex).getPoint();
                //sampleGenes[geneIndex++] = new MutipleIntegerGene(conf, node.x - optimizeModel.getMaxNodalXDisplacement(), node.x + optimizeModel.getMaxNodalXDisplacement(), optimizeModel.getNodalGrid());sampleGenes[geneIndex++] = new MutipleIntegerGene(conf, node.x - optimizeModel.getMaxNodalXDisplacement(), node.x + optimizeModel.getMaxNodalXDisplacement(), optimizeModel.getNodalGrid());
                //System.out.println("\nDoes line contain node "+truss.getNodeModel().get(nodeIndex).getNumber()+"   "+optimizeModel.getSymmetryAxis().contains(node));
                //System.out.println("Is axis vert "+optimizeModel.getSymmetryAxis().isVertical());

                if (gaModel.isKeepSymmetry()
                        && gaModel.getSymmetryAxis().contains(truss.getNodeModel().get(nodeIndex).getPoint2D())
                        && gaModel.getSymmetryAxis().isVertical()) {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.x, node.x, gaModel.getNodalGrid());
                    //System.out.println("restricting is working");
                } else {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.x - gaModel.getMaxNodalXDisplacement(), node.x + gaModel.getMaxNodalXDisplacement(), gaModel.getNodalGrid());
                }
            }
        }

        if (gaModel.isOptimizeFreeNodesY()) {
            //System.out.println("Optimizing Free Nodes Y");
            for (int i = 0; i < freeNodeIndexes.size(); i++) {
                int nodeIndex = freeNodeIndexes.get(i);
                Point node = truss.getNodeModel().get(nodeIndex).getPoint();
                //sampleGenes[geneIndex++] = new MutipleIntegerGene(conf, node.y - optimizeModel.getMaxNodalYDisplacement(), node.y + optimizeModel.getMaxNodalYDisplacement(), optimizeModel.getNodalGrid());
                if (gaModel.isKeepSymmetry()
                        && gaModel.getSymmetryAxis().contains(truss.getNodeModel().get(nodeIndex).getPoint2D())
                        && gaModel.getSymmetryAxis().isHorizotal()) {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.y, node.y, gaModel.getNodalGrid());
                } else {
                    sampleGenes[geneIndex++] = new MultipleIntegerGene(conf, node.y - gaModel.getMaxNodalYDisplacement(), node.y + gaModel.getMaxNodalYDisplacement(), gaModel.getNodalGrid());
                }
            }

        }
        return sampleGenes;

    }

    public void initConfiguration() {
        Configuration conf = null;
        try {

            Configuration.reset();
            conf = new DefaultConfiguration();

            /*conf.getGeneticOperators().set(0, new CrossoverOperator(conf,0.8));
            CrossoverOperator co = (CrossoverOperator)conf.getGeneticOperators().get(0);
            MutationOperator mo = (MutationOperator)conf.getGeneticOperators().get(1);
            
            mo.setMutationRate(200);
            System.out.println("Genetic operator count is "+conf.getGeneticOperators().size());
            System.out.println("Crossover rate is "+co.getCrossOverRatePercent());
            System.out.println("mutation rate is "+mo.getMutationRate());*/

            Configuration.reset();
            conf.setFitnessFunction(this);

        } catch (Exception ex) {
            System.out.println("Error configuring JGAP " + ex);
        }
        try {
            //Configuration.reset();
            //Configuration.resetProperty(Configuration.PROPERTY_FITEVAL_INST);
            conf.setFitnessEvaluator(new DeltaFitnessEvaluator());
            conf.setPreservFittestIndividual(true);



            /*conf.getGeneticOperators().clear();
            CrossoverOperator cross = new CrossoverOperator(conf, 80);
            MutationOperator mut = new MutationOperator(conf, 3);
            conf.getGeneticOperators().add(cross);
            conf.getGeneticOperators().add(mut);*/

            Gene[] sampleGenes;

            sampleGenes = addGenes(conf);

            Chromosome sampleChromosome = new Chromosome(conf, sampleGenes);
            conf.setSampleChromosome(sampleChromosome);
            conf.setPopulationSize(gaModel.getPopulationSize());
            population = Genotype.randomInitialGenotype(conf);
        } catch (Exception ex) {
            System.out.println("Error2 configuring JGAP " + ex);
        }

    }

    @Override
    public double evaluate(IChromosome a_subject) {
        updateStructure(a_subject);
        double fitness = 0;

        if (truss.getAnalysisMethods().getAnalyzer().quickAnalysis()) {
            fitness = getFitness();
            return fitness;
        } else {
            return Double.MAX_VALUE;
        }
    }

    public double getFitness() {
        double fitness = 0;

        fitness += getWeightFitness();
        fitness += getCompressionFitness();
        fitness += getStressFitness();
        fitness += getBarLengthFitness();
        fitness += getNodeSpacingFitness();
        fitness += getDeflectionFitness();

        return fitness;
    }

    public double getWeightFitness() {
        double weightFitness = 0;
        double currentMass = truss.getMass();
        //double weightDiffernece = origTrussWeight - currentWeight;
        weightFitness = currentMass * gaModel.getWeightPenalty();

        if (verbose) {
            System.out.println("\nTotal Weight Fitness " + weightFitness);
        }
        return weightFitness;
    }
    //Compression - check buckling 

    public double getCompressionFitness() {
        double compressionFitness = 0;
        double ratio = 0;
        double axialForce = 0;
        double maxCompressionForce = 0;
        if (verbose) {
            System.out.println("\nCompression Fitness");
        }
        for (int i = 0; i < truss.getBarModel().size(); i++) {
            Bar bar = truss.getBarModel().get(i);
            if (bar.isInTension()) {
                continue;
            }
            axialForce = bar.getAxialForce();
            //maxCompressionForce = bar.getMaxCompressionAxialForce();
            maxCompressionForce = bar.getEulerBucklingForce();
            ratio = axialForce / maxCompressionForce;


            //if (axialForce > tolerance || axialForce < -tolerance) {
            if (ratio > 1) {
                compressionFitness += this.gaModel.getCompressionPenatly() * Math.pow((1 - ratio), 2);
                if (verbose) {
                    System.out.println("bar " + truss.getBarModel().get(i).getNumber() + " is " + gaModel.getCompressionPenatly() * Math.pow((1 - ratio), 2));
                }

            }
        }

        if (verbose) {
            System.out.println("Total Compression Fitness " + compressionFitness);
        }
        return compressionFitness;
    }

    public double getStressFitness() {
        double stressFitness = 0;
        double ratio = 0;
        if (verbose) {
            System.out.println("\nStress Fitness");
        }
        for (int i = 0; i < truss.getBarModel().size(); i++) {

            //double maxStress = Math.min(truss.getBarModel().get(i).getMaxStress()[0], truss.getBarModel().get(i).getMaxStress()[1]);
            double maxStress = truss.getBarModel().get(i).getMaterial().getYieldStrength();

            ratio = Math.abs(truss.getBarModel().get(i).getStress() / maxStress);

            //if (truss.getBarModel().get(i).getAxialForce() > tolerance || truss.getBarModel().get(i).getAxialForce() < -tolerance) {
            if (ratio > 1) {
                stressFitness += (this.gaModel.getStressPenalty() * Math.pow((Math.abs(1 - ratio)), 2));
                if (verbose) {
                    System.out.println("bar " + truss.getBarModel().get(i).getNumber() + " is " + gaModel.getStressPenalty() * Math.pow((Math.abs(1 - ratio)), 2));
                }
            } else {
                //stressFitness += (this.gaModel.getStressPenalty() * Math.pow((Math.abs(1 - ratio)), 2))/10;
                if (verbose) {
                    //System.out.println("bar " + truss.getBarModel().get(i).getNumber() + " is " + gaModel.getStressPenalty() * Math.pow((Math.abs(1 - ratio)), 2));
                }
            }

        }

        if (verbose) {
            System.out.println("Total Stress Fitness " + stressFitness);
        }
        return stressFitness;
    }

    /**
     * Note - method not great if one node is too close to another it will be peanalised twice.
     * @return
     */
    public double getNodeSpacingFitness() {
        if (verbose) {
            System.out.println("\nNode Spacing Fitness");
        }
        double nodeSpacingFitness = 0;
        double ratio = 0;
        Node node;
        double spacing;
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            node = truss.getNodeModel().get(i);

            for (int j = 0; j < truss.getNodeModel().size(); j++) {
                spacing = node.getPoint().distance(truss.getNodeModel().get(j).getPoint());
                ratio = (gaModel.getMinNodeSpacing() * 100) / spacing;

                if (ratio > 1 && j != i) {
                    nodeSpacingFitness += this.gaModel.getLengthPenatly() * Math.pow(ratio - 1, 2);
                    if (verbose) {
                        System.out.println("Node " + truss.getNodeModel().get(i).getNumber() + " is " + gaModel.getLengthPenatly() * Math.pow(ratio - 1, 2));
                    }
                }

            }


        }
        if (verbose) {
            System.out.println("Total Node spacing Fitness " + nodeSpacingFitness);
        }
        return nodeSpacingFitness;
    }

    public double getBarLengthFitness() {
        if (verbose) {
            System.out.println("\nLength Fitness");
        }
        double lengthFitness = 0;
        double ratio = 0;
        for (int i = 0; i < truss.getBarModel().size(); i++) {
            Bar bar = truss.getBarModel().get(i);
            //System.out.println(bar.getLength());
            /*if (bar.getLength() < this.gaModel.getMinNodeSpacing() * 100) {
            ratio = (gaModel.getMinNodeSpacing() * 100) / bar.getLength();
            //lengthFitness += (optimizeModel.getMinBarLength() - bar.getLength() )* lengthPenatly;
            lengthFitness += this.gaModel.getLengthPenatly() * Math.pow(ratio - 1, 2);
            if (verbose) {
            System.out.println("bar " + truss.getBarModel().get(i).getNumber() + " is " + gaModel.getLengthPenatly() * Math.pow(ratio - 1, 2));
            }
            }*/
            ratio = bar.getLength() / (gaModel.getMaxBarLength() * 100);

            if (ratio > 1) {

                //lengthFitness += (  bar.getLength()- optimizeModel.getMaxBarLength()) * lengthPenatly;
                lengthFitness += this.gaModel.getLengthPenatly() * Math.pow(ratio - 1, 2);

                if (verbose) {
                    System.out.println("bar " + truss.getBarModel().get(i).getNumber() + " is " + gaModel.getLengthPenatly() * Math.pow(ratio - 1, 2));
                }
            }

        }
        if (verbose) {
            System.out.println("Total Length Fitness " + lengthFitness);
        }
        return lengthFitness;
    }

    public double getDeflectionFitness() {
        double ratio = 0;
        double displacement = 0;
        double deflectioFitness = 0;
        if (verbose) {
            System.out.println("\nDeflection Fitness");
        }
        for (int i = 0; i < truss.getNodeModel().size(); i++) {
            Node node = truss.getNodeModel().get(i);
            displacement = node.getResultantDisplacement();
            ratio = Math.abs(displacement) / (gaModel.getMaxDeflection());

            if (ratio > 1) {
                deflectioFitness += this.gaModel.getDeflectionPenatly() * Math.pow((ratio - 1), 2);
                if (verbose) {
                    System.out.println("bar " + truss.getBarModel().get(i).getNumber() + " is " + gaModel.getDeflectionPenatly() * Math.pow((ratio - 1), 2));
                }
            }
        }
        if (verbose) {
            System.out.println("Total Deflection Fitness " + deflectioFitness);
        }
        return deflectioFitness;
    }

    /*This method loops through all the genes and changes the structure to match the genes*/
    private void updateStructure(IChromosome a_potentialSolution) {
        int geneIndex = 0;
        if (gaModel.isOptimizeSections()) {
            int sectionIndex = -1;
            TubularSection section = null;
            int barIndex = -1;

            for (int i = 0; i < braceIndexes.size(); i++) {
                sectionIndex = (Integer) a_potentialSolution.getGene(geneIndex++).getAllele();
                section = SectionLibrary.SECTIONS.get(sectionIndex);
                barIndex = braceIndexes.get(i);
                truss.getBarModel().get(barIndex).setSection(section);
            }
            sectionIndex = (Integer) a_potentialSolution.getGene(geneIndex++).getAllele();
            section = SectionLibrary.SECTIONS.get(sectionIndex);
            for (int i = 0; i < chordIndexes.size(); i++) {
                barIndex = chordIndexes.get(i);
                truss.getBarModel().get(barIndex).setSection(section);
            }
        }


        if (gaModel.isOptimizeSupportedNodesX()) {
            for (int i = 0; i < supportedNodeIndexes.size(); i++) {

                int nodeIndex = supportedNodeIndexes.get(i);
                int x = (Integer) a_potentialSolution.getGene(geneIndex++).getAllele();
                truss.getNodeModel().get(nodeIndex).setX(x);

                if (gaModel.isKeepSymmetry()) {
                    int symmetryNodeIndex = truss.getNodeModel().get(nodeIndex).getSymmetryNode().getIndex();
                    double symmetryX = gaModel.getSymmetryAxis().getReflectedPoint(truss.getNodeModel().get(nodeIndex).getPoint2D()).getX();
                    truss.getNodeModel().get(symmetryNodeIndex).setX(symmetryX);
                }
            }
        }

        if (gaModel.isOptimizeSupportedNodesY()) {
            for (int i = 0; i < supportedNodeIndexes.size(); i++) {
                int nodeIndex = supportedNodeIndexes.get(i);
                int z = (Integer) a_potentialSolution.getGene(geneIndex++).getAllele();
                truss.getNodeModel().get(nodeIndex).setZ(z);
                if (gaModel.isKeepSymmetry()) {
                    int symmetryNodeIndex = truss.getNodeModel().get(nodeIndex).getSymmetryNode().getIndex();
                    double symmetryZ = gaModel.getSymmetryAxis().getReflectedPoint(truss.getNodeModel().get(nodeIndex).getPoint2D()).getY();
                    truss.getNodeModel().get(symmetryNodeIndex).setZ(symmetryZ);
                }
            }
        }


        if (gaModel.isOptimizeLoadedNodesX()) {
            for (int i = 0; i < loadedNodeIndexes.size(); i++) {
                int nodeIndex = loadedNodeIndexes.get(i);
                int x = (Integer) a_potentialSolution.getGene(geneIndex++).getAllele();
                truss.getNodeModel().get(nodeIndex).setX(x);
                if (gaModel.isKeepSymmetry()) {
                    int symmetryNodeIndex = truss.getNodeModel().get(nodeIndex).getSymmetryNode().getIndex();
                    double symmetryX = gaModel.getSymmetryAxis().getReflectedPoint(truss.getNodeModel().get(nodeIndex).getPoint2D()).getX();
                    truss.getNodeModel().get(symmetryNodeIndex).setX(symmetryX);
                }
            }
        }

        if (gaModel.isOptimizeLoadedNodesY()) {
            for (int i = 0; i < loadedNodeIndexes.size(); i++) {
                int nodeIndex = loadedNodeIndexes.get(i);
                int z = (Integer) a_potentialSolution.getGene(geneIndex++).getAllele();
                truss.getNodeModel().get(nodeIndex).setZ(z);
                if (gaModel.isKeepSymmetry()) {
                    int symmetryNodeIndex = truss.getNodeModel().get(nodeIndex).getSymmetryNode().getIndex();
                    double symmetryZ = gaModel.getSymmetryAxis().getReflectedPoint(truss.getNodeModel().get(nodeIndex).getPoint2D()).getY();
                    truss.getNodeModel().get(symmetryNodeIndex).setZ(symmetryZ);
                }
            }
        }


        if (gaModel.isOptimizeFreeNodesX()) {
            for (int i = 0; i < freeNodeIndexes.size(); i++) {
                int nodeIndex = freeNodeIndexes.get(i);
                int x = (Integer) a_potentialSolution.getGene(geneIndex++).getAllele();
                truss.getNodeModel().get(nodeIndex).setX(x);
                if (gaModel.isKeepSymmetry()) {
                    int symmetryNodeIndex = truss.getNodeModel().get(nodeIndex).getSymmetryNode().getIndex();
                    double symmetryX = gaModel.getSymmetryAxis().getReflectedPoint(truss.getNodeModel().get(nodeIndex).getPoint2D()).getX();
                    truss.getNodeModel().get(symmetryNodeIndex).setX(symmetryX);
                }
            }
        }

        if (gaModel.isOptimizeFreeNodesY()) {
            for (int i = 0; i < freeNodeIndexes.size(); i++) {
                int nodeIndex = freeNodeIndexes.get(i);
                int z = (Integer) a_potentialSolution.getGene(geneIndex++).getAllele();
                truss.getNodeModel().get(nodeIndex).setZ(z);
                if (gaModel.isKeepSymmetry()) {
                    int symmetryNodeIndex = truss.getNodeModel().get(nodeIndex).getSymmetryNode().getIndex();
                    double symmetryZ = gaModel.getSymmetryAxis().getReflectedPoint(truss.getNodeModel().get(nodeIndex).getPoint2D()).getY();
                    truss.getNodeModel().get(symmetryNodeIndex).setZ(symmetryZ);
                }
            }
        }


    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void printOptimizationSummary(double origTrussMass, long startTime) {
        //Testing
        //verbose = false;
        System.out.println("\n\nPrinting Full Fitness");
        //getFitness();

        long currentTime = System.currentTimeMillis();
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        long elapsed = currentTime - startTime;
        double optimizedMass = truss.getMass();

        DecimalFormat DF = new DecimalFormat("#.##");
        System.out.println("\n\nOriginal Mass is " + DF.format(origTrussMass));
        System.out.println("Optimized Mass is " + DF.format(optimizedMass));
        System.out.println("Mass Saving " + DF.format(origTrussMass - optimizedMass) + " Kg which is " + DF.format(optimizedMass * 100 / origTrussMass) + "%");
        System.out.println("Optimization process took " + dateFormat.format(new Date(elapsed)) + "   hh:mm:ss");
        System.out.println("Final Fitness " + getFitness());
    }

    public ObserableSwingWorker getObserableSwingWorker() {
        return obserableSwingWorker;
    }

    public int getState() {
        return state;
    }

    public class ObserableSwingWorker extends Observable {

        private MySwingWorker evolve = new MySwingWorker(0);

        public void startEvolution() {
            if (!initElementArrays()) {
                return;
            }

            if (state == GAOptimizer.STOP_STATE) {
                initConfiguration();
                evolve = new MySwingWorker(currentEvolution);
            } else {
                evolve = new MySwingWorker(currentEvolution);
            }

            state = GAOptimizer.PLAY_STATE;
            evolve.execute();
        }

        public void pauseEvolution() {
            state = GAOptimizer.PAUSE_STATE;
            evolve.cancel(true);
            currentEvolution = evolve.getEvolution();
        }

        public void stopEvloution() {

            //int i = STOP_STATE;
            state = GAOptimizer.STOP_STATE;
            evolve.cancel(true);
            currentEvolution = 0;
            updateStructure(population.getFittestChromosome());
            //truss.setOperation(TrussModel.OPERATION_OPTIMIZING_STOPPED);
            truss.getAnalysisMethods().getAnalyzer().fullAnalysis();
            
        }

        /*Use SwingWorker so that GUI does not freeze when optimizing*/
        public class MySwingWorker extends SwingWorker<Void, String> {

            private int evolution = 0;

            public MySwingWorker(int evolution) {
                this.evolution = evolution;
            }

            @Override
            protected Void doInBackground() {

                double origTrussMass = truss.getMass();
                long startTime = System.currentTimeMillis();
                Point2D[] points = new Point2D.Double[6];
                double fitness = 0;
                double maxFitness = 0;
                

                for (int i = evolution; i < gaModel.getMaxAllowedEvolutions() && !isCancelled(); i++) {
                    evolution++;
                    population.evolve();
                    fitness = population.getPopulation().determineFittestChromosome().getFitnessValue();
                    //System.out.println("\nEvolution " + evolution + " / " + gaModel.getMaxAllowedEvolutions());
                    //System.out.println("Fitness " + fitness);
                    System.out.println(fitness);
                    //System.out.println("Mass " + truss.getMass());

                    updateStructure(population.getPopulation().determineFittestChromosome());

                    if (maxFitness == 0) {
                        maxFitness = fitness;
                    }

                    if (fitness <= fitnessReduction * maxFitness) {
                        maxFitness = fitness;
                        //System.out.println("\n\nALTERING GENES!!!");
                        //alterGenes();

                    }

                    if (truss.getAnalysisMethods().getAnalyzer().quickAnalysis()) {
                        fitness = getFitness();
                    } else {
                        fitness = Double.MAX_VALUE;
                    }


                    points[0] = new Point2D.Double(evolution, fitness / 1000);
                    points[1] = new Point2D.Double(evolution, getStressFitness() / 1000);
                    points[2] = new Point2D.Double(evolution, getWeightFitness() / 1000);
                    points[3] = new Point2D.Double(evolution, getDeflectionFitness() / 1000);
                    points[4] = new Point2D.Double(evolution, (getBarLengthFitness() + getNodeSpacingFitness()) / 1000);
                    points[5] = new Point2D.Double(evolution, getCompressionFitness() / 1000);

                    if (i == gaModel.getMaxAllowedEvolutions() - 1) {
                        state = GAOptimizer.STOP_STATE;
                    }
                    setChanged();
                    notifyObservers(points);
                }

                updateStructure(population.getFittestChromosome());
                truss.getAnalysisMethods().getAnalyzer().fullAnalysis();


                if (state == GAOptimizer.STOP_STATE) {
                    printOptimizationSummary(origTrussMass, startTime);

                }
                //truss.setOperation(TrussModel.OPERATION_OPTIMIZING_STOPPED);
                return null;
            }

            public void alterGenes() {



                //System.out.println("Number of chromoses is " + population.getPopulation().getChromosomes().size());
                //System.out.println("Chromosome 1 gene count is " + population.getPopulation().getChromosome(0).getGenes().length);

                //MultipleIntegerGene mtemp = (MultipleIntegerGene) population.getPopulation().getChromosome(0).getGenes()[braceIndexes.size() + 3];
                //System.out.println("Nodal Grid is " + gaModel.getNodalGrid());
                //System.out.println("Chromosome 1 significance is " + mtemp.getSignificance());

                int newNoalGrid = (int) (gridReduction * gaModel.getNodalGrid());
                int maxXDis = (int) (toleranceReduction * gaModel.getMaxNodalXDisplacement());
                int maxYDis = (int) (toleranceReduction * gaModel.getMaxNodalYDisplacement());

                for (int k = 0; k < population.getPopulation().size(); k++) {
                    Gene[] genes = population.getPopulation().getChromosome(k).getGenes();
                    //System.out.println("Chromosome " + k);

                    int geneIndex = 0;
                    if (gaModel.isOptimizeSections()) {
                        geneIndex += braceIndexes.size() + 1;
                    }


                    if (gaModel.isOptimizeSupportedNodesX() && maxXDis > 1) {
                        for (int i = 0; i < supportedNodeIndexes.size(); i++) {
                            adjustXGene((MultipleIntegerGene) genes[geneIndex++]);

                        }
                    }

                    if (gaModel.isOptimizeSupportedNodesY() && maxYDis > 1) {
                        for (int i = 0; i < supportedNodeIndexes.size(); i++) {
                            adjustYGene((MultipleIntegerGene) genes[geneIndex++]);
                        }
                    }


                    if (gaModel.isOptimizeLoadedNodesX() && maxXDis > 1) {
                        for (int i = 0; i < loadedNodeIndexes.size(); i++) {
                            adjustXGene((MultipleIntegerGene) genes[geneIndex++]);
                        }
                    }

                    if (gaModel.isOptimizeLoadedNodesY() && maxYDis > 1) {
                        for (int i = 0; i < loadedNodeIndexes.size(); i++) {
                            adjustYGene((MultipleIntegerGene) genes[geneIndex++]);
                        }
                    }


                    if (gaModel.isOptimizeFreeNodesX() && maxXDis > 1) {
                        for (int i = 0; i < freeNodeIndexes.size(); i++) {
                            adjustXGene((MultipleIntegerGene) genes[geneIndex++]);
                        }
                    }

                    if (gaModel.isOptimizeFreeNodesY() && maxYDis > 1) {
                        for (int i = 0; i < freeNodeIndexes.size(); i++) {
                            adjustYGene((MultipleIntegerGene) genes[geneIndex++]);
                        }
                    }
                }



                if (newNoalGrid >= 1) {
                    gaModel.setNodalGrid(newNoalGrid);
                }

                if (maxXDis > 1) {
                    gaModel.setMaxNodalXDisplacement(maxXDis);
                }

                if (maxYDis > 1) {
                    gaModel.setMaxNodalYDisplacement(maxYDis);
                }


                //System.out.println("Nodal Grid is " + gaModel.getNodalGrid());
            }

            private void adjustXGene(MultipleIntegerGene m) {
                int allele = (Integer) m.getAllele();
                int remainder = 0;
                int origXAllele = (m.getUpperBounds() + m.getLowerBounds()) / 2;
                if (gaModel.isKeepSymmetry()
                        && gaModel.getSymmetryAxis().isVertical()
                        && gaModel.getSymmetryAxis().contains(new Point(origXAllele, 0))) {
                    return;
                }

                if (allele + toleranceReduction * gaModel.getMaxNodalXDisplacement() <= m.getUpperBounds()
                        && allele - toleranceReduction * gaModel.getMaxNodalXDisplacement() >= m.getLowerBounds()) {
                    m.setLowerBounds(allele - (int) (toleranceReduction * gaModel.getMaxNodalXDisplacement()));
                    m.setUpperBounds(allele + (int) (toleranceReduction * gaModel.getMaxNodalXDisplacement()));
                } else if (allele + toleranceReduction * gaModel.getMaxNodalXDisplacement() > m.getUpperBounds()) {
                    //upper bound stay
                    remainder = (int) (toleranceReduction * gaModel.getMaxNodalXDisplacement()) - (m.getUpperBounds() - allele);
                    m.setLowerBounds(allele - (int) (toleranceReduction * gaModel.getMaxNodalXDisplacement() + remainder));
                } else if (allele - toleranceReduction * gaModel.getMaxNodalXDisplacement() >= m.getLowerBounds()) {
                    //lower bound stay
                    remainder = (int) (toleranceReduction * gaModel.getMaxNodalXDisplacement()) - (m.getLowerBounds() + allele);
                    m.setUpperBounds(allele + (int) (toleranceReduction * gaModel.getMaxNodalXDisplacement() + remainder));
                }

                if ((int) (gridReduction * gaModel.getNodalGrid()) >= 1) {
                    m.setSignificance((int) (gridReduction * gaModel.getNodalGrid()));
                }


            }

            private void adjustYGene(MultipleIntegerGene m) {
                int allele = (Integer) m.getAllele();
                int remainder = 0;
                int origYAllele = (m.getUpperBounds() + m.getLowerBounds()) / 2;
                if (gaModel.isKeepSymmetry()
                        && gaModel.getSymmetryAxis().isHorizotal()
                        && gaModel.getSymmetryAxis().contains(new Point(0, origYAllele))) {
                    return;
                }

                if (allele + toleranceReduction * gaModel.getMaxNodalYDisplacement() <= m.getUpperBounds()
                        && allele - toleranceReduction * gaModel.getMaxNodalYDisplacement() >= m.getLowerBounds()) {
                    m.setLowerBounds(allele - (int) (toleranceReduction * gaModel.getMaxNodalYDisplacement()));
                    m.setUpperBounds(allele + (int) (toleranceReduction * gaModel.getMaxNodalYDisplacement()));
                } else if (allele + toleranceReduction * gaModel.getMaxNodalYDisplacement() > m.getUpperBounds()) {
                    //upper bound stay
                    remainder = (int) (toleranceReduction * gaModel.getMaxNodalYDisplacement()) - (m.getUpperBounds() - allele);
                    m.setLowerBounds(allele - (int) (toleranceReduction * gaModel.getMaxNodalYDisplacement() + remainder));
                } else if (allele - toleranceReduction * gaModel.getMaxNodalYDisplacement() >= m.getLowerBounds()) {
                    //lower bound stay
                    remainder = (int) (toleranceReduction * gaModel.getMaxNodalYDisplacement()) - (m.getLowerBounds() + allele);
                    m.setUpperBounds(allele + (int) (toleranceReduction * gaModel.getMaxNodalYDisplacement() + remainder));
                }
                if ((int) (gridReduction * gaModel.getNodalGrid()) >= 1) {
                    m.setSignificance((int) (gridReduction * gaModel.getNodalGrid()));
                }

            }

            @Override
            protected void process(List<String> list) {
            }

            public int getEvolution() {
                return evolution;
            }
        }//end of Evolve Class
    }
}



