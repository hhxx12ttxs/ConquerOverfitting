/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vx;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.UUID;
import org.dom4j.*;
import org.dom4j.io.*;
import org.encog.NullStatusReportable;
import org.encog.engine.network.activation.*;
import org.encog.mathutil.randomize.RangeRandomizer;
import org.encog.neural.data.NeuralData;
import org.encog.neural.data.NeuralDataPair;
import org.encog.neural.data.NeuralDataSet;
import org.encog.neural.networks.BasicNetwork;

import org.encog.neural.networks.layers.BasicLayer;
import org.encog.neural.networks.logic.*;
import org.encog.neural.networks.synapse.WeightedSynapse;
import org.encog.neural.networks.training.BasicTraining;
import org.encog.neural.networks.training.CalculateScore;
import org.encog.neural.networks.training.TrainingSetScore;
import org.encog.neural.networks.training.anneal.NeuralSimulatedAnnealing;
import org.encog.neural.networks.training.genetic.NeuralGeneticAlgorithm;
import org.encog.neural.networks.training.propagation.back.Backpropagation;
import org.encog.neural.networks.training.propagation.manhattan.ManhattanPropagation;
import org.encog.neural.networks.training.propagation.resilient.ResilientPropagation;
import org.encog.normalize.DataNormalization;
import org.encog.normalize.input.InputField;
import org.encog.normalize.input.InputFieldArray2D;
import org.encog.normalize.output.OutputFieldDirect;
import org.encog.normalize.output.multiplicative.MultiplicativeGroup;
import org.encog.normalize.output.multiplicative.OutputFieldMultiplicative;
import org.encog.persist.EncogPersistedCollection;
import org.encog.normalize.target.NormalizationStorageNeuralDataSet;
import org.encog.util.logging.Logging;

/**
 *
 * @author Administrator
 */
public class Main {

    private static LoadSavedPartner lspr;
    private static ArrayList<ActivationFunction> aaf;
    private static ArrayList<NeuralLogic> anl;

    public static void main(String[] args) throws FileNotFoundException, IOException, DocumentException, NullPointerException {

        aaf = new ArrayList<ActivationFunction>();
        aaf.add(new ActivationSoftMax());
        aaf.add(new ActivationLinear());
        aaf.add(new ActivationLOG());
        aaf.add(new ActivationSigmoid());
        aaf.add(new ActivationTANH());
        aaf.add(new ActivationSIN());
        aaf.add(new ActivationStep());


        anl = new ArrayList<NeuralLogic>();
        // anl.add(new HopfieldLogic());
        //anl.add(new BoltzmannLogic());
        anl.add(null);
        anl.add(new FeedforwardLogic());
        anl.add(new SimpleRecurrentLogic());
        anl.add(new SOMLogic());

        String udir = args[0];
        String iscontinue = args[1];
        if (iscontinue.compareToIgnoreCase("continue") != 0) {
            Init(udir);
        }
        lspr = new LoadSavedPartner(udir);
        File searchdir = new File(udir + "data/");
        String[] list = searchdir.list();

        if (list != null) {
            for (String lis : list) {

                    ArrayList<String> astring = new ArrayList<String>();
                    FileReader fr = new FileReader(searchdir.getAbsolutePath() + File.separator + lis);
                    BufferedReader br = new BufferedReader(fr);

                    int fieldcount = Integer.parseInt(br.readLine());
                    while (br.ready()) {
                        String line = br.readLine();
                        astring.add(line);
                    }
                    br.close();
                    fr.close();
                    double[][] pdataIN = new double[astring.size() - 25][fieldcount + 1];
                    double[][] edataIN = new double[25][fieldcount + 1];
                    int prei = 0;
                    int evli = 0;
                    for (String ds : astring) {
                        int arri = 0;
                        int input = 0;
                        String[] split = ds.split(",");
                        if (prei < astring.size() - 25) {
                            for (int li = 0; li < fieldcount + 1; li++) {
                                pdataIN[prei][input++] = Double.parseDouble(split[arri++]);
                            }
                            prei++;
                        } else if (evli < 25) {
                            for (int li = 0; li < fieldcount + 1; li++) {
                                double t = edataIN[evli][input++] = Double.parseDouble(split[arri++]);
                            }
                            evli++;
                        }
                    }

                    HPP(udir, lis, pdataIN, edataIN, fieldcount);

                }
            }

    }

    private static void HPP(String udir, String yahoo, double[][] pd, double[][] ed, int fieldcount) throws DocumentException, IOException {
        Logging.stopConsoleLogging();
        NeuralDataSet nset = Normalization(pd, fieldcount);
        LoadSavedPartner jlsp = new LoadSavedPartner(udir);
        ArrayList<String> abt = new ArrayList<String>();
       abt.add("ResilientPropagation");
        abt.add("NeuralGeneticAlgorithm");

        abt.add("ManhattanPropagation");




        for (String basicTrain : abt) {
            for (NeuralLogic nc : anl) {
                for (ActivationFunction f1 : aaf) {
                    for (ActivationFunction f2 : aaf) {
                        for (ActivationFunction f3 : aaf) {

                            String ncstring = "NULL";
                            if (nc != null) {
                                ncstring = nc.getClass().toString();
                            }
                            if (jlsp.skipTruely(yahoo, basicTrain, ncstring, f1.getClass().toString(), f2.getClass().toString(), f3.getClass().toString())) {

                                continue;
                            }
                            BasicNetwork network = new BasicNetwork();
                            BasicLayer b1 = new BasicLayer(f1, true, fieldcount);
                            BasicLayer b2 = new BasicLayer(f2, true, (int) Math.rint(Math.sqrt(fieldcount + 1) + 7));

                            BasicLayer b4 = new BasicLayer(f3, true, 1);
                            network.addLayer(b1);
                            network.addLayer(b2);
                            network.addLayer(b4);
                            boolean add = b1.getNext().add(new WeightedSynapse(b1, b2));
                            boolean add1 = b2.getNext().add(new WeightedSynapse(b2, b4));
                            network.tagLayer(BasicNetwork.TAG_INPUT, b1);
                            network.tagLayer(BasicNetwork.TAG_OUTPUT, b4);
                            if (nc != null) {
                                network.setLogic(nc);
                            }
                            network.getStructure().finalizeStructure();
                            network.reset();
                            NeuralDataSet nds = nset;
                            CalculateScore score = new TrainingSetScore(nds);
                            BasicTraining genetic = null;
                            int geneticCount = 5000;
                            if (basicTrain.compareToIgnoreCase("ResilientPropagation") == 0) {
                                genetic = new ResilientPropagation(network, nds);
                            }
                            if (basicTrain.compareToIgnoreCase("NeuralGeneticAlgorithm") == 0) {
                                geneticCount = 1000;
                                genetic = new NeuralGeneticAlgorithm(network, new RangeRandomizer(-1, 1), score, 500, 0.1, 0.25);
                            }
                            if (basicTrain.compareToIgnoreCase("NeuralGeneticAlgorithm") == 0) {
                                genetic = new Backpropagation(network, nds, 0.7, 0.8);
                            }
                            if (basicTrain.compareToIgnoreCase("ManhattanPropagation") == 0) {
                                genetic = new ManhattanPropagation(network, nds, 0.7);
                            }
                            if (basicTrain.compareToIgnoreCase("NeuralSimulatedAnnealing") == 0) {
                                geneticCount = 1000;
                                genetic = new NeuralSimulatedAnnealing(network, score, 10, 2, 100);
                            }
                    
                            int countf = 0;
                            do {
                                genetic.iteration();
                                countf++;
                                if (Double.isNaN(genetic.getError())) {
                                    break;
                                }
                            } while (countf < geneticCount && genetic.getError() > 0.001);
                            if (Double.isNaN(genetic.getError())) {
                                continue;
                            }

                            UUID ud = UUID.randomUUID();
                            EncogPersistedCollection en = new EncogPersistedCollection(udir + "network/" + ud.toString() + ".eg");
                            en.add("notebook", genetic.getNetwork());

                            SAXReader e = new SAXReader();
                            Document docu = e.read(udir + "savedata.xml");
                            Element root = docu.getRootElement();
                            Element item =  root.addElement("network").addAttribute("yahoo", yahoo).addAttribute("egfile", ud.toString() + ".eg").addAttribute("BasicTrain", basicTrain).addAttribute("Logic", ncstring).addAttribute("f1", f1.getClass().toString()).addAttribute("f2", f2.getClass().toString()).addAttribute("f3", f3.getClass().toString());
                           item.addAttribute("error",Double.toString(genetic.getError()) );
                            NeuralDataSet edn = Normalization(ed, fieldcount);
                            EvalueData(item, genetic.getNetwork(), edn);

                            OutputFormat format = OutputFormat.createPrettyPrint();
                            XMLWriter write = new XMLWriter(new FileWriter(udir + "savedata.xml"), format);
                            write.write(docu);
                            write.close();
                      
                        }
                    }
                }
            }
        }

    }

    private static String formatNum(double error) {
        String retValue = null;
        DecimalFormat df = new DecimalFormat();
        df.setMinimumFractionDigits(0);
        df.setMaximumFractionDigits(5);
        retValue = df.format(error);
        retValue = retValue.replaceAll(",", "");
        return retValue;
    }

    private static void Init(String udir) throws IOException, DocumentException {

        SAXReader e = new SAXReader();
        Document doc = null;
        try {
            doc = e.read(udir + "savedata.xml");
        } catch (Exception ex) {
            Document document = DocumentHelper.createDocument();
            document.addElement("data");
            XMLWriter writer = new XMLWriter(new FileWriter(new File(udir + "savedata.xml")));
            writer.write(document);
            writer.close();
            doc = e.read(udir + "savedata.xml");
        }
        Element root = doc.getRootElement();
        Iterator it = root.nodeIterator();
        ArrayList<Node> an = new ArrayList<Node>();


        while (it.hasNext()) {
            Node node = (Node) it.next();
            if (node.getName() != null && node.getName().compareToIgnoreCase("network") == 0) {
                an.add(node);
            }
        }
        for (Node node : an) {
            root.remove(node);
        }
        OutputFormat format = OutputFormat.createPrettyPrint();
        XMLWriter write = new XMLWriter(new FileWriter(udir + "savedata.xml"), format);
        write.write(doc);
        write.close();
    }

    private static NeuralDataSet Normalization(double[][] pd, int fieldcount) {
        DataNormalization dn = new DataNormalization();
        InputField ifd[] = new InputField[fieldcount + 1];

        dn.setReport(new NullStatusReportable());
        NormalizationStorageNeuralDataSet nndDataSet = new NormalizationStorageNeuralDataSet(fieldcount, 1);

        dn.setTarget(nndDataSet);
        int count = 0;
        for (int fieldloop = 0; fieldloop
                < fieldcount + 1; fieldloop++) {
            dn.addInputField(ifd[count] = new InputFieldArray2D(true, pd, count));
            count++;

        }
        int kI = 0;
        MultiplicativeGroup group = new MultiplicativeGroup();
        for (InputField inputField : ifd) {
            if ((kI + 1) % (fieldcount + 1) == 0) {
                OutputFieldDirect od = new OutputFieldDirect(inputField);
                od.setIdeal(true);
                dn.addOutputField(od);
            } else {
                dn.addOutputField(new OutputFieldMultiplicative(group, inputField));
            }
            kI++;
        }

        dn.process();
        return nndDataSet.getDataset();
    }

    private static void EvalueData(Element rootElement, BasicNetwork network, NeuralDataSet edn) {
        String outS = "";
        String idealS = "";
        Iterator<NeuralDataPair> iterator = edn.iterator();


        while (iterator.hasNext()) {
            NeuralDataPair next = iterator.next();
            final NeuralData output = network.compute(next.getInput());
            if (iterator.hasNext()) {
                outS += formatNum(output.getData(0)) + ":";
            } else {
                outS += formatNum(output.getData(0));
            }
            if (iterator.hasNext()) {
                idealS += formatNum(next.getIdeal().getData(0)) + ":";
            } else {
                idealS += formatNum(next.getIdeal().getData(0));
            }
        }

        Element addAttribute = rootElement.addAttribute("false", outS).addAttribute("true", idealS);

    }
}

