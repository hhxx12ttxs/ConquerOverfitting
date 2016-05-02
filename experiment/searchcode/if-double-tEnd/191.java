package ai.ann;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Vector;

import org.joone.engine.FullSynapse;
import org.joone.engine.Layer;
import org.joone.engine.LinearLayer;
import org.joone.engine.Monitor;
import org.joone.engine.NeuralNetEvent;
import org.joone.engine.NeuralNetListener;
import org.joone.engine.SigmoidLayer;
import org.joone.engine.learning.TeachingSynapse;
import org.joone.io.FileInputSynapse;
import org.joone.io.FileOutputSynapse;
import org.joone.net.NeuralNet;
import org.joone.net.NeuralNetLoader;

import util.essential.SourceGetter;

/**
 * @author Object
 */
public class ComplexNeuralNetwork implements NeuralNetListener,NeuralNetwork {
	//Attributes:
	private LinearLayer inputTrainner = null;
	private SigmoidLayer hiddenLayer20Trainner = null;
	private SigmoidLayer hiddenLayer10Trainner = null;
	private SigmoidLayer outputTrainner = null;
	private Monitor monitor = null;
	private boolean mode = ComplexNeuralNetwork.EXECUTION_MODE;
	private String inputFileName = null;
	private String inputRowsSelector = null;
	private String trainingRowsSelector = null;
	FileInputSynapse inputStream  = null;
	FileOutputSynapse outputStream = null;
	TeachingSynapse trainer = null;
	FileInputSynapse samples = null;
	
	//Execution
	private LinearLayerDirect inputLayer = null;
	private SigmoidLayerDirect hiddenLayer20 = null;
	private SigmoidLayerDirect hiddenLayer10 = null;
	private SigmoidLayerDirect outputLayer = null;
	private FullSynapseDirect inputHidden20 = null;
	private FullSynapseDirect hidden20Hidden10 = null;
	private FullSynapseDirect hidden10Output = null;
	
	/**
	 * This constructur is used when this structure is initialized from 
	 * a stored one, after its trainning.
	 * @param neuralNetworkFilePath is the neural network structure file path.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public ComplexNeuralNetwork(String neuralNetworkFilePath) throws IOException, ClassNotFoundException {
		this.mode = ComplexNeuralNetwork.EXECUTION_MODE;
		this.inputRowsSelector = null;
		this.trainingRowsSelector = "0";
		this.inputFileName = null;
		this.load(neuralNetworkFilePath);
	}
	
	/**
	 * This constructur is used to run the neural network over a file.
	 * @param mode is the work mode of the neural network.
	 * @param inputFileName is the pattern input file name.
	 * @param inputRowsSelector is the row selector of the pattern input file.
	 * @param outputFileName is the result output file name.
	 */
	public ComplexNeuralNetwork(boolean mode, String inputFileName, String inputRowsSelector, String outputFileName) {
		this.mode = mode;
		this.inputRowsSelector = inputRowsSelector;
		this.trainingRowsSelector = "0";
		this.inputFileName = inputFileName;
		this.initializateNeuralNetworkDirect();
		this.initializateNeuralNetworkTrainner();
	}

	/**
	 * This constructur is used to train a neural network.
	 * @param mode is the work mode of the neural network.
	 * @param inputFileName is the pattern input file name.
	 * @param inputRowsSelector is the row selector for the pattern input file.
	 * @param trainingRowsSelector is the trainning row selector.
	 * @param outputFileName is the result output file name. 
	 */
	public ComplexNeuralNetwork(boolean mode, String inputFileName, String inputRowsSelector, String trainingRowsSelector, String outputFileName) {
		this.mode = mode;
		this.inputFileName = inputFileName;
		this.inputRowsSelector = inputRowsSelector;
		this.trainingRowsSelector = trainingRowsSelector;
		this.initializateNeuralNetworkDirect();
		this.initializateNeuralNetworkTrainner();
	}

	/**
	 * This method allows to initialize the neural network to run.
	 */
	private void initializateNeuralNetworkDirect() {
		//Layers
		this.inputLayer = new LinearLayerDirect(NeuralNetwork.INPUT_NEURON_NUMBER);
		this.hiddenLayer20 = new SigmoidLayerDirect(20);
		this.hiddenLayer10 = new SigmoidLayerDirect(10);
		this.outputLayer = new SigmoidLayerDirect(NeuralNetwork.NUMBER_OF_OUTPUTS);
		
		//Synapses
		this.inputHidden20 = new FullSynapseDirect();
		this.hidden20Hidden10 = new FullSynapseDirect();
		this.hidden10Output = new FullSynapseDirect();
		
		//Connecting
		this.inputLayer.setOutput(inputHidden20);
		this.hiddenLayer20.setInput(inputHidden20);
		this.hiddenLayer20.setOutput(hidden20Hidden10);
		this.hiddenLayer10.setInput(hidden20Hidden10);
		this.hiddenLayer10.setOutput(hidden10Output);
		this.outputLayer.setInput(hidden10Output);
	}
	
	/**
	 * This method allows to initialize the trainner part of the neural network.
	 */
	private void initializateNeuralNetworkTrainner(){
		//Creating layers
		this.inputTrainner = new LinearLayer();
		this.hiddenLayer20Trainner = new SigmoidLayer();
		this.hiddenLayer10Trainner = new SigmoidLayer();
		this.outputTrainner = new SigmoidLayer();

		//Set Dimension Layers
		this.inputTrainner.setRows(NeuralNetwork.INPUT_NEURON_NUMBER);
		this.hiddenLayer20Trainner.setRows(20);
		this.hiddenLayer10Trainner.setRows(10);
		this.outputTrainner.setRows(NeuralNetwork.NUMBER_OF_OUTPUTS);

		//Creating connections
		FullSynapse synapseInputHidden20 = new FullSynapse();
		FullSynapse synapseHidden20Hidden10 = new FullSynapse();
		FullSynapse synapseHidden10Output = new FullSynapse();

		//Set Connections
		this.inputTrainner.addOutputSynapse(synapseInputHidden20);
		this.hiddenLayer20Trainner.addInputSynapse(synapseInputHidden20);
		this.hiddenLayer20Trainner.addOutputSynapse(synapseHidden20Hidden10);
		this.hiddenLayer10Trainner.addInputSynapse(synapseHidden20Hidden10);
		this.hiddenLayer10Trainner.addOutputSynapse(synapseHidden10Output);
		this.outputTrainner.addInputSynapse(synapseHidden10Output);

		//Create Monitor
		this.monitor = new Monitor();
		this.monitor.setLearningRate(0.0014705882352941176470588235294118d);
		this.monitor.setMomentum(0.6);

		//Set Monitor
		this.inputTrainner.setMonitor(monitor);
		this.hiddenLayer20Trainner.setMonitor(monitor);
		this.hiddenLayer10Trainner.setMonitor(monitor);
		this.outputTrainner.setMonitor(monitor);

		this.monitor.addNeuralNetListener(this);

		//FileInput
		this.inputStream = new FileInputSynapse();
		/* The first three columns contain the input values */
		this.inputStream.setAdvancedColumnSelector(this.inputRowsSelector);
		/* This is the file that contains the input data */
		this.inputStream.setFileName(this.inputFileName);

		//Set FileInput connection
		this.inputTrainner.addInputSynapse(this.inputStream);

		this.trainer = new TeachingSynapse();
		/* Setting of the file containing the desired responses, provided by a FileInputSynapse */
		this.samples = new FileInputSynapse();
		this.samples.setFileName(this.inputFileName);
		this.trainer.setDesired(this.samples);
		/* The output values are on the third column of the file */
		this.samples.setAdvancedColumnSelector(this.trainingRowsSelector);
		/* We give it the monitor's reference */
		this.trainer.setMonitor(monitor);

		//Set Trainer connection
		this.outputTrainner.addOutputSynapse(this.trainer);

		//FileOutput
		//this.outputStream = new FileOutputSynapse();
		/* This is the file that contains the input data */
		//this.outputStream.setFileName(this.outputFileName);

		//Set FileOutput connection
		//this.outputTrainner.addOutputSynapse(this.outputStream);
	}
	/**
	 * This variable is a mini GUI that shows the trainning progress.
	 */
	private MiniGUI trainingGUI;
	/* (non-Javadoc)
	 * @see ImageFilter3_NN.neuralnetwok.NeuralNetwork#startTrainnigNeuralNetwork(int, int)
	 */
	public void startTrainnigNeuralNetwork( int amountPatternTrainn, int ciclesToTrainn){
		this.trainingGUI = new MiniGUI();
		inputTrainner.start();
	    hiddenLayer20Trainner.start();
	    hiddenLayer10Trainner.start();
	    outputTrainner.start();
	    /**/
	    double learningRate = Math.pow(amountPatternTrainn+(NeuralNetwork.INPUT_NEURON_NUMBER+31),-1);
	    monitor.setLearningRate(learningRate);
		/*monitor.setMomentum(momentum);*/
		System.out.println(
			"Starting Neural Network in "
				+ ((mode) ? "training mode" : "execution mode") + "..."
				+ "\n\tLearning Rate: "+learningRate);
		/**/
	    monitor.setTrainingPatterns(amountPatternTrainn); /* # of rows contained in the input file */
	    monitor.setTotCicles(ciclesToTrainn); /* How many times the net must be trained on the input patterns */
	    monitor.setLearning(TRAINING_MODE); /* The net must be trained */
	    monitor.Go(); /* The net starts the training job */
	    inputTrainner.join();
	    hiddenLayer20Trainner.join();
	    hiddenLayer10Trainner.join();
	    outputTrainner.join();
	    /**/
		System.out.println(((mode) ? "Training" : "Execution") + " done.\n Cicle: "+monitor.getCurrentCicle());
		/**/
	}
	
	/**
	 * This method allows to store the current neural network structure.
	 * This is the non official Joone way to do it.
	 * @param fileName is the output file name.
	 * @param object is the structure to store.
	 * @throws IOException
	 */
	private void store(String fileName, Object object) throws IOException {
		/**/
		System.out.println("Saving \"" + fileName + "\"....");
		/**/
		File file = new File(fileName);
		if (file.exists())
			file.delete();
		file.createNewFile();
		FileOutputStream out = new FileOutputStream(file);
		ObjectOutputStream output = new ObjectOutputStream(out);
		output.writeObject(object);
		output.close();
		/**/
		System.out.println("\"" + fileName + "\" ready.");
		/**/
	}
	
	/**
	 * This method just is to close with a saving structure.
	 */
	public void close() {
		if(this.mode == NeuralNetwork.TRAINING_MODE){
			/**/
			System.out.println("Saving weights....");
			/**/
			//Saving weights:
			if (this.inputTrainner != null)
				try {
					this.store(
						NeuralNetwork.LAYER_FOLDER + "/input.nn",
						this.inputTrainner.getBias());
				} catch (IOException e) {
					/**/
					System.err.println("Can't to save input layer weights.");
					/**/
				}
			if (this.hiddenLayer20Trainner != null)
				try {
					this.store(
						NeuralNetwork.LAYER_FOLDER + "/hiddenLayer20.nn",
						this.hiddenLayer20Trainner.getBias());
				} catch (IOException e) {
					/**/
					System.err.println(
						"Can't to save hiddenLayer20 layer weights.");
					/**/
				}
			if (this.hiddenLayer10Trainner != null)
				try {
					this.store(
						NeuralNetwork.LAYER_FOLDER + "/hiddenLayer10.nn",
						this.hiddenLayer10Trainner.getBias());
				} catch (IOException e) {
					/**/
					System.err.println(
						"Can't to save hiddenLayer10 layer weights.");
					/**/
				}
			if (this.outputTrainner != null)
				try {
					this.store(
						NeuralNetwork.LAYER_FOLDER + "/output.nn",
						this.outputTrainner.getBias());
				} catch (IOException e) {
					/**/
					System.err.println("Can't to save output layer weights.");
					/**/
				}
			/**/
			System.out.println("Weights Saved.");
			/**/
		}
	}
	
	/* (non-Javadoc)
	 * @see ImageFilter3_NN.neuralnetwok.NeuralNetwork#stimulate(double[])
	 */
	public double[] stimulate(double[] input){
		final int PERSENTAGE = 100;
		for (int i = 0; i < input.length; i++) {
			input[i] *= PERSENTAGE;
		}
		return inputLayer.stimulation(input);
	}
	
	/**
	 * this method allows to write trainning files, by using a color list as an input.
	 * @param trainnerColors is the color bean list.
	 */
	public void writeTrainnerFile(Vector trainnerColors){
		FileWriter fileWriter = null;
		File file = null;
		try {
			file = new File((NeuralNetwork.class.getResource("/logic/trainner/trainner.txt")).toURI());
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		}
        try  {
        	fileWriter = new FileWriter(file);
            for (Iterator iter = trainnerColors.iterator(); iter.hasNext();) {
				TrainnerBean element = (TrainnerBean) iter.next();
				fileWriter.write(element.getColor().getRed()+
						";"+element.getColor().getGreen()+
						";"+element.getColor().getBlue()+
						";"+((element.isCanopia())?"1\n":"0\n"));
			}
       }catch (IOException ex) {
            ex.printStackTrace();
       }finally{
            if(fileWriter!=null){
                try{
                	fileWriter.close();
                }catch(IOException ex){}
            }
       }

	}

	/* (non-Javadoc)
	 * @see org.joone.engine.NeuralNetListener#cicleTerminated(org.joone.engine.NeuralNetEvent)
	 */
	public void cicleTerminated(NeuralNetEvent arg0) {
		this.trainingGUI.msg("RMSE: "+this.getRMSE()+",\t in cicle "+this.getCurrentCicleTraining());
	}

	/* (non-Javadoc)
	 * @see org.joone.engine.NeuralNetListener#errorChanged(org.joone.engine.NeuralNetEvent)
	 */
	public void errorChanged(NeuralNetEvent arg0) {}

	/* (non-Javadoc)
	 * @see org.joone.engine.NeuralNetListener#netStarted(org.joone.engine.NeuralNetEvent)
	 */
	public void netStarted(NeuralNetEvent arg0) {
		System.out.println("Net Started.");
	}

	/* (non-Javadoc)
	 * @see org.joone.engine.NeuralNetListener#netStopped(org.joone.engine.NeuralNetEvent)
	 */
	public void netStopped(NeuralNetEvent arg0) {
		System.out.println("Net stoped.");
		this.trainingGUI.msg("<RMSE: "+this.getRMSE()+">");
		this.trainingGUI.close();
		/**/
		System.out.println("\tError de un "+(this.getRMSE()*100)+"%");
		/**/
	}

	/* (non-Javadoc)
	 * @see org.joone.engine.NeuralNetListener#netStoppedError(org.joone.engine.NeuralNetEvent, java.lang.String)
	 */
	public void netStoppedError(NeuralNetEvent arg0, String arg1) {
		System.out.println("Error. Net stoped.");
	}
	
	/**
	 * @return current trinning cicle.
	 */
	public int getCurrentCicleTraining() {
		return this.monitor.getCurrentCicle();
	}

	/**
	 * @return whether neural network is in trainning.
	 */
	public boolean isNeuralNetworkTrainnigDone() {
		return this.monitor.getCurrentCicle() == 0;
	}

	/**
	 * @param path
	 */
	public void setInputFile(String path) {
		this.inputStream.setFileName(path);
	}

	/**
	 * @param path
	 */
	public void setTrainnerFile(String path) {
		this.samples.setFileName(path);
	}

	/**
	 * @param path
	 */
	public void setOutputFile(String path) {
		this.outputStream.setFileName(path);
	}

	/**
	 * @return total of trainning cicles.
	 */
	public int getTotalCicles() {
		return this.monitor.getTotCicles();
	}

	/**
	 * @return the neural network error.
	 */
	public double getRMSE() {
		return monitor.getGlobalError();
	}
	
	/* (non-Javadoc)
	 * @see ImageFilter3_NN.neuralnetwok.NeuralNetwork#save(java.lang.String)
	 */
	public void save(String fileName) throws IOException{
		File fd = new File(fileName);
		FileOutputStream foutput=new FileOutputStream(fd);
		ObjectOutputStream output = new ObjectOutputStream(foutput);
		output.writeObject(inputTrainner);
		output.writeObject(hiddenLayer20Trainner);
		output.writeObject(hiddenLayer10Trainner);
		output.writeObject(outputTrainner);
		foutput.close();
	}
	/**
	 * This method allows to load the neural network from a file.
	 * This is the non-official Joone way to do it.
	 * @param fileName is the structure input file name.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void load(String fileName) throws IOException, ClassNotFoundException{
		File fd = new File(fileName);
		FileInputStream finput=new FileInputStream(fd);
		ObjectInputStream input = new ObjectInputStream(finput);
		
		this.initializateNeuralNetworkDirect();
		
		 /*private SigmoidLayer*/
		Layer layer;
		
		layer = (Layer)input.readObject();
		this.inputLayer.setWeightMatrix(layer.getBias().getValue());
		
		
		layer = (Layer)input.readObject();
		this.hiddenLayer20.setWeightMatrix(layer.getBias().getValue());
		inputHidden20.setSynapseMatrix(((FullSynapse)layer.getAllInputs().elementAt(0)).getWeights().getValue());
		
		layer = (Layer)input.readObject();
		this.hiddenLayer10.setWeightMatrix(layer.getBias().getValue());
		hidden20Hidden10.setSynapseMatrix(((FullSynapse)layer.getAllInputs().elementAt(0)).getWeights().getValue());
		
		layer = (Layer)input.readObject();
		this.outputLayer.setWeightMatrix(layer.getBias().getValue());
		hidden10Output.setSynapseMatrix(((FullSynapse)layer.getAllInputs().elementAt(0)).getWeights().getValue());
		
		finput.close();
	}
	/**
	 * This method allows to load a neural network from a file.
	 * This is the oficial Joone way to do this. But is not used here.
	 * @param path is the path where is the neural network structure input file.
	 * @param fileName is the neural network structure file name. 
	 * @throws IOException
	 */
	public static void loadNeuralNet(String path,String fileName) throws IOException{
		/* We need just to provide the serialized NN file name */
		NeuralNetLoader loader = new NeuralNetLoader(path+((!path.endsWith("/"))?"/":"")+fileName+".snet");
		NeuralNet nn = loader.getNeuralNet();
		System.out.println("Layer Size> "+nn.getLayers().size());
		File fd = new File(path+((!path.endsWith("/"))?"/":"")+fileName+".nn");
		FileOutputStream foutput=new FileOutputStream(fd);
		ObjectOutputStream output = new ObjectOutputStream(foutput);
		for (Iterator iter = nn.getLayers().iterator(); iter.hasNext();) {
			output.writeObject(iter.next());
		}
		output.close();
	}
	/**
	 * This method allows to trainning the neural network.
	 * @param inputFileName is the pattern input file name.
	 * @param inputRowSelector is the pattern row selector in the input file.
	 * @param trainingRowSelector is the row selector for trainning data.
	 * @param outputFileName is the output structure file name.
	 * @param amountOfTrainningPatterns is the amount trainning patterns.
	 * @param cicles is the number of cilce to trainning.
	 * @throws IOException
	 */
	public static void trainning(String inputFileName,String inputRowSelector,String trainingRowSelector,String outputFileName,int amountOfTrainningPatterns, int cicles) throws IOException{
		System.out.println("Trainning: \n"+
					"\tInput File Name: "+inputFileName+"\n"+
					"\tInput Row Selector: "+inputRowSelector+"\n"+
					"\tTraining Row Selector: "+trainingRowSelector+"\n"+
					"\tOutput File Name: "+outputFileName+"\n"+
					"\tAmount Of Trainning Patterns: "+amountOfTrainningPatterns+"\n"+ 
					"\tCicles: "+cicles);
		NeuralNetwork nn = new ComplexNeuralNetwork(NeuralNetwork.TRAINING_MODE,inputFileName,inputRowSelector,trainingRowSelector,outputFileName);
		nn.startTrainnigNeuralNetwork(amountOfTrainningPatterns,cicles);
		nn.save(ComplexNeuralNetwork.valueGetter.getStringValue("neural_network_path"));
	}
	/**
	 * This method allows to test the neural network.
	 * @param neuralFilenNamePath is the structure file name of the neural network to test. 
	 * @param inputTestFileName is the pattern input file name.
	 * @param amountOfTrainningPatterns is the amount training patterns.
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void test(String neuralFilenNamePath,String inputTestFileName,int amountOfTrainningPatterns) throws IOException, ClassNotFoundException{
		//NeuralNetwork.loadNeuralNet(path,fileName);
		NeuralNetwork nn = NeuralNetworkFactory.getInstance(neuralFilenNamePath);
		Vector rows = SourceGetter.config.getSource(inputTestFileName);
		double[]input = new double[amountOfTrainningPatterns],output;
		boolean withThreshold = true; float errorCounter=0,lineCounter=0;
		final boolean HSI = NeuralNetwork.valueGetter.getStringValue("color_space").equals("HSI");
		String end,line;
		for (Iterator iter = rows.iterator(); iter.hasNext();) {
			String[] str = iter.next().toString().split(";");
			lineCounter++;
			//System.out.println("String Size> "+str.length);
			line = "";
			for (int i = 0; i < str.length; i++) {
				input[i] = Float.parseFloat(str[i])/((HSI)?100:1);
				line += str[i]+(((i+1)==str.length)?"":";");
			}
			output = nn.stimulate(input);
			System.out.print("[");
			double threshold = Double.parseDouble(NeuralNetwork.valueGetter.getStringValue("threshold"));
			end = "";
			for (int i = 0; i < output.length; i++) {
				end += ((output[i]>=threshold)?"1":"0");
				System.out.print(((withThreshold)?((output[i]>=threshold)?"1":"0"):String.valueOf(output[i]))+"-["+str[str.length-1]+"]=("+output[i]+")"+(((i+1)<output.length)?";":""));
			}
			System.out.println("]");
			/*System.out.println("\tLine = "+line);
			System.out.println("\tEnd = "+end);
			System.out.println("\tCompare = "+(line.endsWith(end))+"; Errors: "+errorCounter+"; Lines: "+lineCounter);*/
			errorCounter += (line.endsWith(end))?0:1;
		}
		System.out.println("Error: "+((errorCounter/lineCounter)*100)+"%");
	}
	//Main:
	/**
	 * @param args
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static void main(String[]args) throws IOException, ClassNotFoundException{
		/**
		//Training:
		NeuralNetwork.trainning(	NeuralNetwork.valueGetter.getStringValue("pattern_files_base_path"),
							    "1-"+NeuralNetwork.INPUT_NEURON_NUMBER,
							    String.valueOf(NeuralNetwork.INPUT_NEURON_NUMBER+1),
							    "",35,2000);
		/**/
		// Excecution:
		String testFileName = "./TrainingExtractor/patterns/out_S_7x7_RGB.txt"; 
		//ComplexNeuralNetwork.test(ComplexNeuralNetwork.valueGetter.getStringValue("neural_network_path"),ComplexNeuralNetwork.valueGetter.getStringValue("pattern_files_base_path"),ComplexNeuralNetwork.INPUT_NEURON_NUMBER+1);
		ComplexNeuralNetwork.test(NeuralNetwork.valueGetter.getStringValue("neural_network_path"),testFileName,NeuralNetwork.INPUT_NEURON_NUMBER+NeuralNetwork.NUMBER_OF_OUTPUTS);
		/**/
	}
}

