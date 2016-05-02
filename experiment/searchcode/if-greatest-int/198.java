package neuralnetbot;

import java.util.ArrayList;

import org.neuroph.core.NeuralNetwork;

import processing.core.PApplet;

public class Robot extends WorldObject{
	ArrayList <CollisionSensor> sensors;
	GameMap map;
	ArrayList<Double> drives;
	NeuralNetwork neuralNetwork = NeuralNetwork.load("AvoidAndFind.nnet");
	
	//NeuralNetwork neuralNetowrk = NeuralNetwork.load("AvoidAndFind.nnet");
	
	public Robot(PApplet p, GameMap m, int x, int y){
		super(p, x, y);
		map = m;
		drives = new ArrayList<Double>();
		drives.add(0.0);
		drives.add(0.0);
		sensors = new ArrayList<CollisionSensor>();
		sensors.add(new CollisionSensor(this, Directions.RIGHT, 15));
		sensors.add(new CollisionSensor(this, Directions.UP, 15));
		sensors.add(new CollisionSensor(this, Directions.LEFT, 15));
		sensors.add(new CollisionSensor(this, Directions.DOWN, 15));
		
	}
	
	public void move(int x, int y){
		X += x;
		Y += y;
		for (CollisionSensor s: sensors){
			s.move(x, y);
		}
		if (!map.runMode){
			run();
		}
	}
	
	public void draw(){
		super.draw();
		for (CollisionSensor i: sensors){
			i.draw();
		}
		getGardenDrive();
		if(drives.size() > 0){
			//System.out.println(drives.get(0));
			//System.out.println(drives.get(1));
		}
		
	}
	public void getGardenDrive(){
		ArrayList<Double> rv = new ArrayList<Double>();
		for (Garden g: map.gardens){
			rv.add((double)((g.X-X)/Math.sqrt((g.X-X)*(g.X-X) + (g.Y-Y)*(g.Y-Y))));
			rv.add((double)((g.Y-Y)/Math.sqrt((g.X-X)*(g.X-X) + (g.Y-Y)*(g.Y-Y))));
			//System.out.print("Garden Accessed");
		}
		if (rv.size() > 0){
			this.drives = rv;
		}
	}
	
	public double[] constructInput(){
		double[] rv = new double[8];
		for (int i = 0; i < 4; i++){
			rv[i] = sensors.get(i).getOutput();
		}
		if(drives.get(0) > 0){
			rv[4]=drives.get(0);
			rv[6]=0;
		}else{
			rv[4]=0;
			rv[6]=-drives.get(0);
		}
		if(drives.get(1)<0){
			rv[5] = -drives.get(1);
			rv[7] = 0;
		}else{
			rv[5] = 0;
			rv[7] = drives.get(1);
		}
		return rv;
	}
	
	public void run(){
		double [] input = constructInput();
		System.out.println();
		for (int i = 0; i <input.length ; i++){
			System.out.print(input[i] + ", ");
			
		}
		System.out.println();

		neuralNetwork.setInput(constructInput());
		neuralNetwork.calculate();
		neuralNetwork.calculate();
		neuralNetwork.calculate();
		neuralNetwork.calculate();
		neuralNetwork.calculate();
		double[] output = neuralNetwork.getOutput();
		System.out.println("Output: " + output[0] + ", " + output[1] + ", " + output[2] + ", " + output[3]);
		neuralMove(output);
	}
	
	public void neuralMove(double[] output){
		double greatest;
		greatest = 0;
		int greatestIndex;
		greatestIndex = 0;
		for (int i = 0; i < output.length; i++){
			if (output[i] > greatest){
				greatest = output[i];
				greatestIndex = i;
			}
		}
		System.out.println(greatestIndex);
		if (map.runMode){
			switch(greatestIndex){
			case 0:
				move(1,0);
				break;
			case 1:
				move(0,-1);
				break;
			case 2:
				move(-1, 0);
				break;
			case 3:
				move(0, 1);
				break;
			default:
				System.out.print("There is a problem");
				break;
			}
		}
	}
}

