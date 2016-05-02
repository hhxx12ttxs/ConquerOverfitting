package modules.adsr;

import java.util.List;

import modules.algorythm.IAlgo;
import modules.generic.IAModule;
import ports.ISignal;
import ports.Signal;

public class AlgoADSR implements IAlgo {

	private IAADSR client;
	
	private short attack;
	private short decay;
	private short release;
	private short sustain;
	//	Divers
	private int time;
	private int state;
	int level;
	private double frameRate = 1/ IAModule.MAX_FREQ;

	public AlgoADSR() {
		attack = 1;
		decay = 0;
		release = 2;
		sustain = 0;
		time = 0;
		state = 0;
		level = 0;
	}

	/**
	 * @see modules.algorythm.IAlgo#execute()
	 */
	@Override
	public void execute() {
/*
 * BEN
		short ga = (short) Math.exp(-1 / (attack));
		short gr = (short) Math.exp(-1 / (release));
	*/	
		double ga = Math.exp(Math.log(0.01)/( attack * 44100 * 0.001)); 
		double gr =Math.exp(Math.log(0.01)/( release * 44100 * 0.001));;
		time+= frameRate;
		
		short signal =0;

		short envelope = 4;
		
		ISignal inputSignal = client.getIn().getSignal();
		ISignal outputSignal = new Signal();
		
		List<Short> values =inputSignal.getValues();
		for (int i = 0; i < ISignal.CAPACITY; i++) {
			
			short EnvIn = (short) values.get(i);

			if (envelope < EnvIn) {
				/*
				 * BEN
				envelope *= ga;
				envelope += (1 - ga) * EnvIn;
				*/
				envelope = (short) (gr * (envelope - EnvIn) + EnvIn);
			} else {
				envelope =(short)(ga * (envelope - EnvIn) + EnvIn);
				/*
				 * BEN
				envelope *= gr;
				envelope += (1 - gr) * EnvIn;
				*/
			}

			// envelope now contains.........the envelope ;)
			outputSignal.addValue(envelope);
			//System.out.println("out");
			//	System.out.println(envelope);
		}
		//System.out.println("signal "+outputSignal.getValues());
		client.getOut().addSignal(outputSignal);
		//System.exit(0);
	}

	/**
	 * @see modules.algorythm.IAlgo#configure(modules.generic.IAModule)
	 */
	@Override
	public void configure(IAModule module) {
		this.client = (IAADSR) module;
	}
}

