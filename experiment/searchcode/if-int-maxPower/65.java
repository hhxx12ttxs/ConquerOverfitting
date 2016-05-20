package TrainController;

import TrainModel.*;

public class IndividualController {
	
	static char trackLine;
	int trainID;
	TrainModel train;
	private TrainController controller;
	private boolean suggestionReceived = false, initPowerReceived = false, powerReceived = false, emergencyStopping = false;
	private double pendingPower, speedGoal;
	private double traveled=0.0;
	private int index;
	
	public IndividualController(TrainController p_controller, char p_trackLine, int p_trainID, TrainModel p_train, int p_index){
		trackLine = p_trackLine;
		trainID = p_trainID;
		train = p_train;
		controller = p_controller;
		index = p_index;
	}
	
	public void SendSpeed(double p_speed){
		suggestionReceived = true;
		if (p_speed <= train.speedLimit){
			speedGoal = p_speed;
		}
		else{
			speedGoal = train.speedLimit;
		}
		if (emergencyStopping){
			emergencyStopping = false;
		}
	}
	
	public void SendPower(double p_power){
		initPowerReceived = true;
		pendingPower = p_power;
		if (emergencyStopping){
			emergencyStopping = false;
		}
	}
	
	public void StopPower(){
		pendingPower = 0;
		powerReceived = false;
		train.currPower = 0.0;
	}
	
	public void EmergencyStop(){
		emergencyStopping = true;
	}
	
	public double GetDistance(){
		return traveled;
	}
	
	public void MoveTrain() throws InterruptedException{
		while (true){
			if (emergencyStopping){
				train.HandleEBrake();
			}
			else if (suggestionReceived){
				double initialTime = train.currTime;
				train.GivePower(train.maxPower);
				traveled += train.move();
				controller.timeArray[index] += train.currTime - initialTime;
				if (train.currSpeed == speedGoal){
					suggestionReceived = false;
				}
			}
			else if (initPowerReceived){
				double initialTime = train.currTime;
				train.GivePower(pendingPower);
				traveled += train.move();
				controller.timeArray[index] += train.currTime - initialTime;
				initPowerReceived = false;
				powerReceived = true;
			}
			else if (powerReceived){
				double initialTime = train.currTime;
				traveled += train.move();
				controller.timeArray[index] += train.currTime - initialTime;
				if (!(train.currSpeed < train.speedLimit)){
					powerReceived = false;
				}
			}
			else{
				traveled += train.keepMoving();
				controller.timeArray[index] += 0.5;
			}
			Thread.sleep(250);
			if (controller.currentTrain == trainID){
				controller.CallUpdate();
			}
		}
	}

}

