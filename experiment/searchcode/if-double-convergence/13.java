package newExamples;

import gaia.cu1.tools.exception.GaiaException;
import gaia.cu1.tools.numeric.algebra.GVector2d;
import gaiasimu.SimuException;
import gaiasimu.universe.source.stellar.StarSystemSimuException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import org.apache.commons.math.FunctionEvaluationException;
import org.apache.commons.math.optimization.OptimizationException;
import org.apache.commons.math.optimization.VectorialPointValuePair;
import org.apache.commons.math.optimization.general.LevenbergMarquardtOptimizer;


public class FittingRun implements Runnable, Logging {

	/** Output directory for fit results */
	private String resultDir;
	
	/** System to be simulated */
	private SimulatedSystem system;
	
	/** Number of fits to perform */
	private int NFits;
	
	/** Inclination angle to be simulated */
	private double inc;
	
	/** Fit results */
	private FitResults fitResults;
	
	/** Private template clone */
	private SystemTemplate templateClone;
	
	/** Extensive output */
	private boolean extensiveOutput;
	
	
	/**
	 * Constructor setting all parameters.
	 * 
	 * @param NFits		Number of fits
	 * @param inc		Inclination angle [rad]
	 * @param resultDir	Output directory for results
	 * @param system	System to be simulated
	 * @param extensiveOutput	Whether extensive output about individual fits is to be created
	 */
	public FittingRun(int NFits, double inc, String resultDir, SimulatedSystem system, boolean extensiveOutput) {
		
		// Set parameters
		this.NFits = NFits;
		this.inc = inc;
		this.resultDir = resultDir;
		this.system = system;
		this.extensiveOutput = extensiveOutput;
		this.templateClone = system.getModelTemplate().clone();
		
		// Default null value for results
		this.fitResults = null;
		
	}
	
	
	/**
	 * Runs the fit.
	 */
	@Override
	public void run() {
		
		String name = system.getName();
		logger.log("Fitting " + name + "; output to "+ resultDir);
		
		// Create random number generator
		Random random = new Random();
		
		// Some setup of directories & files
		(new File(resultDir)).mkdir();
		(new File(resultDir+"results")).delete();
		
		// Basic loop
		for(int kFit = 0; kFit < NFits; kFit++) {
			
			// Try block in case anything goes horribly wrong
			try {
				
				Model obsModel;
				ObservationalDataSet obsData;
				ArrayList<TransitData> transits;
				Model fitModel;
				double trueInc, trueNodeAngle, trueTimePeri, trueOmega2, truePlx, trueMuAlpha, trueMuDelta, period, mProj, sigPlx, ecc;
				
				// Synchronized block for model generation to avoid interference
				synchronized(templateClone) {
					// Change inclination angle of planet templates
					templateClone.setPrimaryInclination(inc);
					
					// Generate observational single transit data
					Simulation sim = system.getSimulation();
					obsModel = templateClone.createModel(system.getSimulation(), true, false, sim.isRandGeo());
					DataGenerator generator = new DataGenerator(obsModel, sim);
					obsData = generator.getObservationalData(true);
					transits = obsData.getTransits();
					
					// Plot them, maybe
					if(extensiveOutput) {
						obsData.plotTransitData(resultDir+"simuobs."+kFit);
					}
					
					// Generate the fit model and sync transits
					fitModel = templateClone.createModel(sim, false, true, false);
					fitModel.setTransits(obsModel.getTransits());
					
					// Save true values
					trueInc = templateClone.getPlanetTemplates().get(0).getInclination();
					trueNodeAngle = templateClone.getPlanetTemplates().get(0).getNodeAngle();
					trueTimePeri = templateClone.getPlanetTemplates().get(0).getTimePeriastron();
					trueOmega2 = templateClone.getPlanetTemplates().get(0).getOmega2();
					truePlx = templateClone.getAstrometryTemplate().getParallax();
					trueMuAlpha = templateClone.getAstrometryTemplate().getMuAlphaStar();
					trueMuDelta = templateClone.getAstrometryTemplate().getMuDelta();
					period = templateClone.getPlanetTemplates().get(0).getPeriod();
					mProj = templateClone.getPlanetTemplates().get(0).getProjectedMass();
					sigPlx = (new ErrorModel(templateClone.getStarTemplate().getMagMv(), templateClone.getStarTemplate().getVMinusI(), sim.isFaintCutOff())).parallaxError();
					ecc = templateClone.getPlanetTemplates().get(0).getEccentricity();
				}
				
				// Initialize the model function
				ModelFunction function = new ModelFunction(fitModel, fitModel.useAcrossScan(), obsModel.getFilteredTransits());
				function.setVariable(FitQuantity.INCLINATION, false, random.nextDouble()*Math.PI, 1e-7);
				function.setVariable(FitQuantity.ALPHA_OFFSET, false, 0, 1e-4);
				function.setVariable(FitQuantity.DELTA_OFFSET, false, 0, 1e-4);
				function.setVariable(FitQuantity.PARALLAX, false, fitModel.getAstrometry().getVarpi(), 1e-4);
				function.setVariable(FitQuantity.MU_ALPHA, false, fitModel.getAstrometry().getMuAlphaStar(), 1e-6);
				function.setVariable(FitQuantity.MU_DELTA, false, fitModel.getAstrometry().getMuDelta(), 1e-6);
				if(system.getSimulation().isRandGeo()) {
					function.setVariable(FitQuantity.NODEANGLE, false, 0.0, 1e-5);
					function.setVariable(FitQuantity.TIMEPERI, false, 0.0, 1e-5);
					function.setVariable(FitQuantity.OMEGA2, false, 0.0, 1e-5);
				} else {
					function.setVariable(FitQuantity.NODEANGLE, true, trueNodeAngle, 1e-5);
					function.setVariable(FitQuantity.TIMEPERI, true, trueTimePeri, 1e-5);
					function.setVariable(FitQuantity.OMEGA2, true, trueOmega2, 1e-5);
				}
				
				// Constants
				final int		MAX_ITERATIONS	= 1000;          // Maximum interations for Gauss-Newton
				final double	CONVERGENCE		= 1.0e-5;        // Convergenge threshold to stop optimizer
				final double[]	FIRST_GUESS		= function.getInitialValues();
				
				// Initialize weights and target values
				
				int tsize = transits.size();
				int valueAmount =  tsize * (fitModel.useAcrossScan()? 2: 1);
				double[] errors	= new double[valueAmount];
				double[] targets = new double[valueAmount];
				double[] times = new double[tsize];
				
				for(int i = 0; i < tsize; i++) {
					
					times[i] = transits.get(i).getTime().getJD();
					
					if ( fitModel.useAcrossScan() ) {
						
						targets[2*i] = transits.get(i).getLSC().getX();
						targets[2*i+1] = transits.get(i).getLSC().getY();
						errors[2*i] = transits.get(i).getLSCError().getX();
						errors[2*i+1] = transits.get(i).getLSCError().getY();
						
					} else {
						
						targets[i] = transits.get(i).getLSC().getX();
						errors[i] = transits.get(i).getLSCError().getX();
						
					}
					
				}
		
				// Weights: 1 / uncertainty ^ 2: maximum likelihood
				double[] weights = new double[valueAmount];
				double weightSum = 0;
				
				for ( int i=0; i < valueAmount; i++ ) {
					
					weights[i] = 1.0 / Math.pow(errors[i], 2);
					weightSum += weights[i];
					
				}
				
				// Weights normalisation: <weight> = 1.0
				for (int i = 0; i < weights.length; i++) {
					weights[i] *= weights.length / weightSum;
				}
				
				// Number of transits
				logger.debug("Fit: Gaia will perform "+transits.size()+" transits of the system.");
				
				// Optimizer set-up
				logger.debug("Fit: Start fit.");
				LevenbergMarquardtOptimizer optimizer = new LevenbergMarquardtOptimizer();
				optimizer.setMaxIterations(MAX_ITERATIONS);
				optimizer.setCostRelativeTolerance(CONVERGENCE);
				optimizer.setParRelativeTolerance(CONVERGENCE);
				optimizer.setOrthoTolerance(CONVERGENCE);
				
				// Function optimization
				VectorialPointValuePair optimum;
				
				optimum = optimizer.optimize(function, targets, weights, FIRST_GUESS);
				
				// Covariances and errors
				logger.debug("Fit: Calculate errors and covariances.");
				double[]   fitErrors     = optimizer.guessParametersErrors();
				double[][] fitCovariance = optimizer.getCovariances();
				
				// Calculate chi square
				double[] bestFit = optimum.getValue();
				double chi2 = 0.0;
				for ( int i = 0; i < targets.length; i++ ) {
					chi2 += Math.pow ( (targets[i] - bestFit[i]), 2) * weights[i] / (weights.length / weightSum * targets.length);
				}
				
				// Prepare return values
				fitResults = new FitResults(bestFit, optimum.getPoint(), fitErrors, fitCovariance, chi2);
				
				// File handling objects
				File file;
				FileWriter filewriter;
				BufferedWriter buffwriter;
				
				// Write fit results to file, TODO: adapt to geometry randomization
				if(extensiveOutput) {
					
					file = new File(resultDir+"results."+kFit);
					filewriter = new FileWriter(file, false);
					buffwriter = new BufferedWriter(filewriter);
					
					buffwriter.write(String.format("Inclination [rad]: %18.10e +/- %18.10e\n", fitResults.getVariables()[0], fitResults.getErrors()[0]));
					buffwriter.write(String.format("Node angle [rad]:  %18.10e +/- %18.10e\n", fitResults.getVariables()[1], fitResults.getErrors()[1]));
					buffwriter.write(String.format("Time peri. [rad]:  %18.10e +/- %18.10e\n", fitResults.getVariables()[2], fitResults.getErrors()[2]));
					buffwriter.write(String.format("Arg. peri. [rad]:  %18.10e +/- %18.10e\n", fitResults.getVariables()[3], fitResults.getErrors()[3]));
					buffwriter.write(String.format("RA offset [mas]:   %18.10e +/- %18.10e\n", fitResults.getVariables()[4], fitResults.getErrors()[4]));
					buffwriter.write(String.format("Dec offset [mas]:  %18.10e +/- %18.10e\n", fitResults.getVariables()[5], fitResults.getErrors()[5]));
					buffwriter.write(String.format("Parallax [mas]:    %18.10e +/- %18.10e\n", fitResults.getVariables()[6], fitResults.getErrors()[6]));
					buffwriter.write(String.format("muAlpha [mas/yr]:  %18.10e +/- %18.10e\n", fitResults.getVariables()[7], fitResults.getErrors()[7]));
					buffwriter.write(String.format("muDelta [mas/yr]:  %18.10e +/- %18.10e\n", fitResults.getVariables()[8], fitResults.getErrors()[8]));
					buffwriter.write(String.format("Reduced chi square: %.10e\n", chi2));
					for(double[] covrow: fitCovariance) {
						for(double cov: covrow)
							buffwriter.write(String.format("%20.12e", cov));
						buffwriter.write("\n");
					}
					buffwriter.close();
					
				}
				
				// Append line with results to overall results
				file = new File(resultDir+"results");
				filewriter = new FileWriter(file, true);
				buffwriter = new BufferedWriter(filewriter);
				
				if(system.getSimulation().isRandGeo()) {
					buffwriter.write(String.format("%e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %d %d %e %e %e %e %e\n",
							trueInc,
							fitResults.getVariables()[0], fitResults.getErrors()[0],
							trueNodeAngle,
							fitResults.getVariables()[1], fitResults.getErrors()[1],
							trueTimePeri,
							fitResults.getVariables()[2], fitResults.getErrors()[2],
							trueOmega2,
							fitResults.getVariables()[3], fitResults.getErrors()[3],
							0.0,
							fitResults.getVariables()[4], fitResults.getErrors()[4],
							0.0,
							fitResults.getVariables()[5], fitResults.getErrors()[5],
							truePlx,
							fitResults.getVariables()[6], fitResults.getErrors()[6],
							trueMuAlpha,
							fitResults.getVariables()[7], fitResults.getErrors()[7],
							trueMuDelta,
							fitResults.getVariables()[8], fitResults.getErrors()[8],
							chi2,
							transits.size(),
							obsModel.useAcrossScan()?1:0,
							obsModel.getAstroSig(),
							period,	mProj, sigPlx, ecc));
				} else {
					buffwriter.write(String.format("%e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %e %d %d %e %e %e %e %e\n",
							trueInc,
							fitResults.getVariables()[0], fitResults.getErrors()[0],
							0.0,
							fitResults.getVariables()[1], fitResults.getErrors()[1],
							0.0,
							fitResults.getVariables()[2], fitResults.getErrors()[2],
							truePlx,
							fitResults.getVariables()[3], fitResults.getErrors()[3],
							trueMuAlpha,
							fitResults.getVariables()[4], fitResults.getErrors()[4],
							trueMuDelta,
							fitResults.getVariables()[5], fitResults.getErrors()[5],
							chi2,
							transits.size(),
							obsModel.useAcrossScan()?1:0,
							obsModel.getAstroSig(),
							period,	mProj, sigPlx, ecc));
				}

				buffwriter.close();

				// Write best fit values to file
				if(extensiveOutput) {
					
					file = new File(resultDir+"bestfit."+kFit);
					filewriter = new FileWriter(file, false);
					buffwriter = new BufferedWriter(filewriter);
					
					for ( int i = 0; i < obsData.getTransits().size(); i++ ) {
						
						if ( fitModel.useAcrossScan() ) {
							
							GVector2d bestFitLSC = new GVector2d(bestFit[2*i], bestFit[2*i+1]);
							GVector2d targetsLSC = new GVector2d(targets[2*i], targets[2*i+1]);
							GVector2d errorsLSC = new GVector2d(errors[2*i], errors[2*i+1]);
							double scanAngle = obsData.getTransits().get(i).getScanAngle();
							GVector2d bestFitLPC = Conversion.fromLSCtoLPC(bestFitLSC, scanAngle);
							GVector2d targetsLPC = Conversion.fromLSCtoLPC(targetsLSC, scanAngle);
							GVector2d errorsLPC = Conversion.fromLSCtoLPCError(errorsLSC, scanAngle);
							
							buffwriter.write(String.format("%20.12e %20.12e %20.12e %20.12e %20.12e %20.12e %20.12e %20.12e %20.12e %20.12e %20.12e %20.12e %20.12e\n",
									times[i],
									targets[2*i], targets[2*i+1],
									errors[2*i], errors[2*i+1],
									bestFit[2*i], bestFit[2*i+1],
									targetsLPC.getX(), targetsLPC.getY(),
									errorsLPC.getX(), errorsLPC.getY(),
									bestFitLPC.getX(), bestFitLPC.getY()));
							
						} else {
							
							buffwriter.write(String.format("%20.12e %20.12e %20.12e %20.12e\n",
									times[i],
									targets[i],
									errors[i],
									bestFit[i]));
							
						}
						
					}
					buffwriter.close();
				}
				
				// Plot fitted orbit
				if(extensiveOutput) {
					
					DataGenerator fitResultsGenerator = new DataGenerator(fitModel, system.getSimulation());
					fitResultsGenerator.getOrbit().plotTrajectory(resultDir+"orbit."+kFit);
					
				}
				
			} catch (OptimizationException e) {
				logger.error("Exception occurred in optimization for " + name + "!");
				e.printStackTrace();
			} catch (FunctionEvaluationException e) {
				logger.error("Exception occurred evaluating the model function for " + name + "!");
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				logger.error("Exception due to illegal argument given to model function for " + name + "!");
				e.printStackTrace();
			} catch (IOException e) {
				logger.error("IOException occurred in " + name + "!");
				e.printStackTrace();
			} catch (SimuException e) {
				logger.error("SimuException occurred in " + name + "!");
				e.printStackTrace();
			} catch (GaiaException e) {
				logger.error("GaiaException occurred in " + name + "!");
				e.printStackTrace();
			} catch (StarSystemSimuException e) {
				logger.error("StarSystemSimuException occurred in " + name + "!");
				e.printStackTrace();
			}
			
		}
		
	}

	
	/**
	 * Returns the fit results of the last completed run.
	 * 
	 * @return Fit results
	 */
	public FitResults getFitResults() {
		return fitResults;
	}

}

