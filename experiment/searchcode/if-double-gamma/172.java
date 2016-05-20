package norc.pgrd;

import java.util.Random;

import norc.Agent;
import norc.Learner;
import norc.State;
import norc.Utils;


public class Agent_OLGARB<T extends State> implements Agent<T> {
	
	private DifferentiableQFunction<T> qf;
	private Learner<T> 	               learner;
	private SoftmaxPolicy<T>           policy;
	private OLGARB<T>                  policy_gradient;
		
	private Random random;	
	
	/**
	 * Create OLGARB policy gradient agent
	 * @param qf           -- q function
	 * @param gamma        -- reward discount factor
	 * @param alpha        -- policy gradient learning rate
	 * @param temperature  -- softmax policy temperature
	 * @param depth        -- uct planning depth
	 * @param trajectories -- uct planning trajectory count
	 * @param use_baseline -- use the baseline estimate (otherwise this is GPOMDP)
	 */
	public Agent_OLGARB(DifferentiableQFunction<T> qf,
					Learner<T> learner,
			        double alpha, double temperature,			        
			        double gamma, boolean use_baseline, 
			        Random random){
		this.random = random;
		this.learner = learner;
		policy          = new SoftmaxPolicy<T>(qf, temperature);
		policy_gradient = new OLGARB<T>(policy,alpha,gamma,use_baseline);
		this.qf = qf;
	}
	
	public Agent_OLGARB(DifferentiableQFunction<T> qf,
	        double alpha, double temperature,			        
	        double gamma, boolean use_baseline, 
	        Random random){
		this(qf,null,alpha,temperature,gamma,use_baseline,random);
	}
	/**
	 * Plans from given state, updates reward parameters, returns chosen action
	 * @param st     -- current state
	 * @param reward -- objective reward sample
	 * @return chosen action
	 */
	public int step(T st1){
		policy.evaluate(st1);
		return Utils.sampleMultinomial(policy.getCurrentPolicy().y,this.random);
	}
	public int step(T st1, int a1, double reward, T st2){
		this.policy_gradient.learn(st1, a1, reward);
		if(learner!=null)
			learner.learn(st1, a1,reward, st2);
		int new_action;
		if(st2.isAbsorbing()){
		  this.policy_gradient.initEpisode();
		  new_action = -1;
		} else
		  new_action = step(st2);		
		return new_action;
	}		
	public DifferentiableQFunction<T> getQF(){return qf;}	
}

