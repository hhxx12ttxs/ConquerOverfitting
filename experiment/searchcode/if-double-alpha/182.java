package norc.pgrd;

import java.util.Arrays;

import norc.State;


/**
 * OLGARB Policy Gradient Algorithm
 * @author Jeshua Bratman
 * 
 * See:
 *  The optimal reward baseline for gradient-based reinforcement learning by Lex Weaver and Nigel Tao
 *  UAI'01 Proceedings of the Seventeenth Conference on Uncertainty in Artificial Intelligence
 *  
 */
public class OLGARB<T extends State> {
	private DifferentiablePolicy<T> policy;
    private double alpha;        //learning rate
    private double gamma;        //discount factor
    private double baseline;     //baseline
    private boolean use_baseline;

    private int timestep;    
    private int num_params;
    private double [] Z;         //eligibility trace vector
    private double [] theta;     //policy parameterization
    
    
	public OLGARB(DifferentiablePolicy<T> policy, double alpha, double gamma,boolean use_baseline){
		this.policy = policy;
		this.alpha = alpha;
		this.gamma = gamma;
		this.use_baseline = use_baseline;
		this.num_params = policy.numParams();
		this.theta = new double[num_params];
		this.Z = new double[num_params];
		this.initEpisode();
	}
	public OLGARB(DifferentiablePolicy<T> policy, double alpha, double gamma)
	{this(policy,alpha,gamma,false);};    
	
	public void initEpisode (){	  
	  Arrays.fill(Z, 0);
	  this.baseline = 0;
	  this.timestep = 0;	  
	}
	/**
	 * Update policy parameters after taking action a1 in state st1 
	 * and receiving reward r
	 * @param st
	 * @param a
	 * @param r
	 */
	public void learn(State st, int a, double r){	
		double[] mu = policy.getCurrentPolicy().y; 
		double[][] dmu = policy.getCurrentPolicy().dy;
		
		timestep++;
		baseline += (1d/timestep) * (r - baseline);//rolling average			
		
		for(int i = 0; i < num_params; ++i){
			Z[i] = gamma * Z[i] + dmu[a][i]/mu[a];
		}
		this.theta = this.policy.getParams().clone();
		double delta = 0;
		if (use_baseline) delta = alpha * (r-baseline);
		else delta = alpha * r;
		
		for(int i=0;i<num_params;i++)
			theta[i] += delta * Z[i];
		policy.setParams(theta);
	}
}

