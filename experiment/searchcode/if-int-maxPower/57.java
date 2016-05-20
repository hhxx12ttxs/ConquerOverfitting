import java.util.Vector;

import org.neuroph.core.NeuralNetwork;
import org.neuroph.nnet.MultiLayerPerceptron;
import org.neuroph.core.learning.SupervisedTrainingElement;
import org.neuroph.core.learning.TrainingElement;
import org.neuroph.core.learning.TrainingSet;
import org.neuroph.nnet.learning.LMS;


public class NeuroBrain {
	
	/*
	 * structure mךme du rיseau de neuronnes
	 */
	NeuralNetwork neuralNetwork;
	
	/*
	 * nombre maximum d'itיration dans l'apprentissage
	 */
	public static final int maxIterations = 250000;
	
	/*
	 * jeu de donnיes
	 */
	TrainingSet<SupervisedTrainingElement> trainingSet;
	
	
	
	public static final double maxAngle = 360;
	
	public static final double maxX = 1200;
	
	public static final double maxY = 1200;
	
	public static final double maxPower = 3;
	
	
	
	

	public void NeuroBrain ( String neuroNetworkFile, String dataFile, boolean fromFile ) {
		
		// si on veut charger א partir d'un fichier
		if ( fromFile ) {
			
			neuralNetwork = NeuralNetwork.load ( neuroNetworkFile ); // crיation du rיseau de neuronne
		}
		else { // sinon on crיe un rיseau de base
			/*
			 * 7 entrיe :
			 * - pos ennemi ( x et y )
			 * - direction ennemi ( x, y )
			 * - notre pos ( x et y )
			 * - tir touche ( 0 / 1 )
			 * 
			 * 2 sorties:
			 * - angle de tir
			 * - puissance du tir
			 */
			neuralNetwork = new MultiLayerPerceptron ( 7, 11, 2 ); 
		}
		
		
		
		
		((LMS) neuralNetwork.getLearningRule()).setMaxError(0.00001);// taux d'erreur moyen acceptי
        ((LMS) neuralNetwork.getLearningRule()).setLearningRate(0.7);//
        ((LMS) neuralNetwork.getLearningRule()).setMaxIterations(maxIterations);
        
        
        /*
         * chargement d'un jeu de donnיes prיcrיי
         */
		trainingSet = TrainingSet.createFromFile( "dataFile.txt", 7, 2, ","); // separateur = ,
        
        /*
         * on apprend la premiטre fois pour initialiser le rיseau de neuronne
         */
        neuralNetwork.learnInNewThread ( trainingSet ); // il ne faut pas bloquer le robot dans ses autres taches
	}
	
	
	
	/*
	 * mettre en pause l'apprentissage, ce qui permet
	 * d'utiliser le rיseau tel quel si besoin sans garanti que
	 * l'apprentissage יtait fini
	 */
	public void pause () {
		neuralNetwork.pauseLearning();
	}
	
	
	
	
	/*
	 * permet de reprendre l'apprentissage
	 */
	public void resume() {
		neuralNetwork.resumeLearning();
	}
	
	
	
	
	/*
	 * ajoute une ligne dans le jeu de donnיe
	 */
	public void addEntryValues ( double[] entrees, double[] sorties ) {
		trainingSet.addElement ( new SupervisedTrainingElement ( entrees, sorties ) );
	}
	
	
	
	
	/*
	 * gיnטre une solution
	 */
	
	public double[] getSolution ( double[] input ) {
		/*
		 * donnיe d'entrיe de test pour la gיnיration de solution
		 */
		TrainingSet<SupervisedTrainingElement > testSet = new TrainingSet<SupervisedTrainingElement>();
		testSet.addElement ( new SupervisedTrainingElement  ( input, null ) ); // besoin que des entrיes
		
		double[] networkOutput = null;

		pause(); // on pause le training si jamais il tournait
		
		for (SupervisedTrainingElement  testElement : testSet.elements() ) {
			neuralNetwork.setInput ( testElement.getInput() ); // envoi ds donnיes d'entrיes au rיseau de neuronne
			neuralNetwork.calculate(); // calcul de la solution
			networkOutput = neuralNetwork.getOutput(); // rיcupיration des sorties gיnיrיes
		}
		
		resume(); // on relance le training
		
		
		/*
		 * on oublie pas de remettre les valeurs en non-normalisי
		 */
		networkOutput[0] = networkOutput[0] * maxAngle;
		networkOutput[1] = networkOutput[1] * maxPower;
		
		
		return networkOutput;
	}
	
	
}

