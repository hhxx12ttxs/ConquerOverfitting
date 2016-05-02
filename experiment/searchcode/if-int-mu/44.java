package Stages;

import InstructionClasses.Instruction;
import Main.MipsSimulator;
import Main.ReadFile;



public class Decode extends Stage{
	public int raw[] = new int[100];
	public int waw[] = new int[100];
	public int struct[] = new int[100];
	public boolean isJump = false;
	public int whereToJump=0;
	public boolean firstHLT = false;
	public Decode(boolean flag) {
		super(flag);
		// TODO Auto-generated constructor stub
	}
	@Override
	public void processInstruction(int cycleNumber) {
		if(MipsSimulator.d.instruction!=null){
			//first identify F or R
			int rOrF = (MipsSimulator.d.instruction.getcDestinationRegister());
			//if destination  is locked WAW HAZARD
			//another condition added for branch instruction
			if(MipsSimulator.d.instruction.getDestinationRegister()==-1? true : MipsSimulator.isFreeRegister[rOrF]
					[MipsSimulator.d.instruction.getDestinationRegister()]){
				//RAW HAZARD
				if(MipsSimulator.isFreeRegister[rOrF][MipsSimulator.d.instruction.getSourceRegister1()] && 
						(MipsSimulator.d.instruction.getSourceRegister2()==-1 ? true:MipsSimulator.isFreeRegister[rOrF]
								[MipsSimulator.d.instruction.getSourceRegister2()])){

					//CHECK IF INST IS BRANCH OR JUMP
					if(instruction.getName().equalsIgnoreCase("BNE") ||instruction.getName().equalsIgnoreCase("J")  
							|| instruction.getName().equalsIgnoreCase("BEQ")|| instruction.getName().equalsIgnoreCase("HLT")){
						if(instruction.getName().equalsIgnoreCase("HLT")){
							firstHLT = true;
							instruction.clckCyclePosition[1] = cycleNumber;
							//if(MipsSimulator.f.halt){
								instruction = null;
							//}
							
						}
						else{
							MipsSimulator.d.instruction.extractValuesFromRegisterInDecode();
							MipsSimulator.d.instruction.executeInstruction();
							if(instruction.getJumpTo()==-1){
								//System.out.println("No jump");
								//continue execution
								instruction.clckCyclePosition[1] = cycleNumber;
								MipsSimulator.d.instruction = null;
								MipsSimulator.d.occupied = false;

							}
							else{
								//when u jump 
								//System.out.println("recognixed jump "+cycleNumber);
								instruction.clckCyclePosition[1] = cycleNumber;
								isJump = true;
								whereToJump = instruction.getJumpTo();
								instruction.unlockDestination();
								MipsSimulator.d.instruction = null;
								MipsSimulator.d.occupied = false;
							}
						}
					}
					else{
						//check if functional unit is busy or not
						/**
						 * 1. IU
						 * 2. AU
						 * 3. MU
						 * 4. DU*/

						int unit = getUnit(MipsSimulator.d.instruction);
						//Extract values from registers
						MipsSimulator.d.instruction.extractValuesFromRegisterInDecode();
						if(unit==1){
							if(!ReadFile.iu.isBusy){
								//PASSED TO IU, locked destination
								MipsSimulator.d.instruction.clckCyclePosition[1] = cycleNumber;
								MipsSimulator.d.instruction.lockDestination();
								ReadFile.iu.setInstruction(MipsSimulator.d.instruction);
								//trila
								MipsSimulator.d.instruction = null;
								////System.out.println("D-> IU");
								MipsSimulator.d.occupied = false;

							}
							else{
								//System.out.println("D->IU cannot be done");
								MipsSimulator.d.occupied = true;
								//NOW
								instruction.hazard[3] = 'Y';
								struct[ReadFile.instructionList.indexOf(instruction)]=1;
							}
						}
						else if(unit==2){
							if(!ReadFile.au.isBusy){
								//PASSED TO AU, locked destination
								MipsSimulator.d.instruction.clckCyclePosition[1] = cycleNumber;
								MipsSimulator.d.instruction.lockDestination();
								ReadFile.au.setInstruction(MipsSimulator.d.instruction);
								//System.out.println("D-> AU");
								MipsSimulator.d.occupied = false;
								//trila
								MipsSimulator.d.instruction = null;

							}
							else{
								//System.out.println("D->AU cannot be done");
								MipsSimulator.d.occupied = true;
								instruction.hazard[3] = 'Y';
								struct[ReadFile.instructionList.indexOf(instruction)]=1;
							}
						}
						else if(unit==3){
							if(!ReadFile.mu.isBusy){
								//PASSED TO MU, locked destination
								MipsSimulator.d.instruction.clckCyclePosition[1] = cycleNumber;
								MipsSimulator.d.instruction.lockDestination();
								ReadFile.mu.setInstruction(MipsSimulator.d.instruction);
								//System.out.println("D-> MU");
								MipsSimulator.d.occupied = false;
								//trila
								MipsSimulator.d.instruction = null;

							}
							else{
								//System.out.println("D->MU cannot be done");
								MipsSimulator.d.occupied = true;
								instruction.hazard[3] = 'Y';
								struct[ReadFile.instructionList.indexOf(instruction)]=1;
							}
						}
						else if(unit==4){
							if(!ReadFile.du.isBusy){
								//PASSED TO DU, locked destination
								MipsSimulator.d.instruction.clckCyclePosition[1] = cycleNumber;
								MipsSimulator.d.instruction.lockDestination();
								ReadFile.du.setInstruction(MipsSimulator.d.instruction);
								//System.out.println("D-> DU");
								MipsSimulator.d.occupied = false;
								//trila
								MipsSimulator.d.instruction = null;

							}
							else{
								//System.out.println("D->DU cannot be done");
								MipsSimulator.d.occupied = true;
								instruction.hazard[3] = 'Y';
								struct[ReadFile.instructionList.indexOf(instruction)]=1;
							}
						}
					}
				}
				else{
					////System.out.println("Source register/s is/are locked and hence canot procedd");
					////System.out.println("RAW detected");
					MipsSimulator.d.occupied = true;
					//TODO
					raw[ReadFile.instructionList.indexOf(instruction)] = 1;
					instruction.hazard[0] = 'Y';
				}
			}
			else{
				////System.out.println("Desitnation register is locked and hence canot procedd");
				//System.out.println("WAW detected "+cycleNumber);
				MipsSimulator.d.occupied = true;
				//TODO
				waw[ReadFile.instructionList.indexOf(instruction)] = 1;
				instruction.hazard[2] = 'Y';
			}
		}
	}


	public int getUnit(Instruction instruction) {
		int i = 0;
		switch (instruction.getName()) {
		case "DADD":
		case "DADDI":
		case "DSUB":
		case "DSUBI":
		case "AND":
		case "ANDI":
		case "OR":
		case "ORI":
		case "LW":
		case "SW":
		case "L.D":
		case "S.D":
			i= 1;
			break;

		case "ADD.D":
		case "SUB.D":
			i= 2;
			break;

		case "DIV.D":
			i= 4;
			break;

		case "MUL.D":
			i= 3;
			break;

		default://TODO
			break;
		}
		return i;
	}

	@Override
	public void passToNextStage(int cycleNumber) {
		//System.out.println("Not required here");
	}


}

