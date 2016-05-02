package com.algorithm.pso;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.algorithm.AlgorithmManager;

public class PSO implements AlgorithmManager {

	private static int numberOfCities;
	private static int numberOfParticles;
	private int numberOfIterations = 0;
	private double[][] cities;
	private ArrayList<Particle> particles = new ArrayList<Particle>();

	private double globalBest;
	private boolean globalBestChanged = false;
	private ArrayList<Integer> globalBestPosition = null;
	private int bestSolutionIteration = 0;

	private static double omega;
	private static double c1;
	private static double c2;

	private PSO(ArrayList<String> parameters, double[][] cities)
			throws NumberFormatException {
		initializeParameters(parameters, cities);
		initializeAlgorithm();
	}

	/**
	 * <p>
	 * Initializes PSO algorithm with coefficients given as list of parameters
	 * for instance specified by matrix of distances between cities.
	 * </p>
	 * 
	 * <p>
	 * The parameters parameter should be in a format as follows:
	 * </p>
	 * <ul>
	 * <li>"omega:W"</li>
	 * <li>"c1:X"</li>
	 * <li>"c2:Y"</li>
	 * <li>"swarm:Z"</li>
	 * </ul>
	 * <p>
	 * where W, X and Y are coefficients given in a format corresponding to
	 * double format and swarm is a number of particles to create in a format
	 * corresponding to int format. For example it could be ("omega:2.5",
	 * "c1:1.0", "c2:3.2", "swarm:49").
	 * </p>
	 * 
	 * @param parameters
	 *            the list of 3 coefficients for algorithm equations and int
	 *            value denoting swarm size
	 * @param cities
	 *            the square matrix containing distances between cities
	 * @return the PSO object executing Particle Swarm Optimization algorithm
	 */
	public static AlgorithmManager init(ArrayList<String> parameters,
			double[][] cities) {
		boolean error = false;
		for (String param : parameters) {
			try {
				if (param.startsWith("confidenceCoefficent1:")) {
					omega = Double.parseDouble(param.substring(param
							.indexOf(':') + 1));
				} else if (param.startsWith("confidenceCoefficent2:")) {
					c1 = Float
							.parseFloat(param.substring(param.indexOf(':') + 1));
				} else if (param.startsWith("confidenceCoefficent3:")) {
					c2 = Float
							.parseFloat(param.substring(param.indexOf(':') + 1));
				} else if (param.startsWith("numberOfSwarms:")) {
					numberOfParticles = Integer.parseInt(param.substring(param
							.indexOf(':') + 1));
				}
			} catch (NumberFormatException e) {
				error = true;
				break;
			}
		}
		if (error == true) {
			parameters.clear();
			parameters.add("Given coefficients are not numerical.");
			return null;
		}
		if (omega < 0.0 || omega > 1.0) {
			parameters.clear();
			parameters.add("Omega is not within range [0;1].");
			return null;
		}
		if (c1 <= 0) {
			parameters.clear();
			parameters.add("Wrong c1 value.");
			return null;
		}
		if (c2 <= 0) {
			parameters.clear();
			parameters.add("Wrong c2 value.");
			return null;
		}
		if (error == true)
			return null;

		return new PSO(parameters, cities);
	}

	private void initializeAlgorithm() {
		List<Integer> citiesPermutation = new ArrayList<Integer>();
		ArrayList<Integer> position = null;

		for (int i = 0; i < numberOfCities; i++) {
			citiesPermutation.add(i);
		}

		for (int i = 0; i < numberOfParticles; i++) {
			position = new ArrayList<Integer>();
			// Randomly shuffles list of all cities indexes
			Collections.shuffle(citiesPermutation, new Random());

			for (int j = 0; j < numberOfCities; j++) {
				position.add(citiesPermutation.get(j));
			}

			particles.add(new Particle(i, position));

			if ((globalBestPosition == null)
					|| (calculateDistance(position) < globalBest)) {
				globalBestPosition = position;
				globalBest = calculateDistance(globalBestPosition);
			}
		}
	}

	private void initializeParameters(ArrayList<String> parameters,
			double[][] cities) throws NumberFormatException {
		omega = Double.parseDouble(parameters.get(0).split(":")[1]);
		c1 = Double.parseDouble(parameters.get(1).split(":")[1]);
		c2 = Double.parseDouble(parameters.get(2).split(":")[1]);
		numberOfParticles = Integer.parseInt(parameters.get(3).split(":")[1]);
		this.cities = cities; // TODO: Here should go some array's values
								// verification.
		numberOfCities = cities[0].length;
	}

	/**
	 * <p>
	 * Updates particle's current velocity vector for every particle in a swarm.
	 * </p>
	 */
	private void updateVelocities() {
		for (Particle particle : particles) {
			Velocity currentVelocity = particle.getParticleVelocity();
			// velocityFromParticleBest = pbest_i - x_i(k)
			Velocity velocityFromParticleBest = new Velocity(
					new ArrayList<Integer>(particle.getParticleBestPosition()),
					new ArrayList<Integer>(particle.getPosition()));
			// velocityFromParticleBest = gbest_i - x_i(k)
			Velocity velocityFromGlobalBest = new Velocity(
					new ArrayList<Integer>(globalBestPosition),
					new ArrayList<Integer>(particle.getPosition()));

			Velocity newVelocity = currentVelocity.multiplyBy(omega);
			newVelocity.add(velocityFromParticleBest.multiplyRandomlyBy(c1));
			newVelocity.add(velocityFromGlobalBest.multiplyRandomlyBy(c2));

			particle.setVelocity(newVelocity);
		}
	}

	/**
	 * <p>
	 * Updates particle's current position for every particle in a swarm.
	 * </p>
	 */
	private void updateParticlesPositions() {
		for (Particle particle : particles) {
			ArrayList<Integer> newPosition = particle.getParticleVelocity()
					.applyOn(particle.getPosition());
			particle.setPosition(newPosition);

			double newPositionDistance = calculateDistance(newPosition);

			if (newPositionDistance < calculateDistance(particle
					.getParticleBestPosition())) {
				particle.setParticleBestPosition(newPosition);
			}

			if (newPositionDistance < globalBest) {
				globalBestPosition = particle.getParticleBestPosition();
				globalBest = newPositionDistance;
				globalBestChanged = true;
			}
		}
	}

	/**
	 * <p>
	 * Calculates distance carried by salesman when travelling from city to city
	 * in order given by position.
	 * </p>
	 * 
	 * @param position
	 *            given particle's position
	 * @return distance carried by salesman when travelling in order given by
	 *         position
	 */
	private double calculateDistance(ArrayList<Integer> position) {
		double distanceCalculated = 0.0;
		Integer previousCityIndex = position.get(0);

		for (int i = 1; i < position.size(); i++) {
			Integer currentCityIndex = position.get(i);
			distanceCalculated += cities[previousCityIndex][currentCityIndex];
			previousCityIndex = currentCityIndex;
		}

		return distanceCalculated;
	}

	@Override
	public void step(int n) {
		while (n-- > 0) {
			numberOfIterations++;
			updateVelocities();
			updateParticlesPositions();
			if (globalBestChanged) {
				bestSolutionIteration = numberOfIterations;
				globalBestChanged = false;
			}
		}
	}

	@Override
	public double getStepDistance() {
		return globalBest;
	}

	@Override
	public double getBestDistance() {
		return globalBest;
	}

	@Override
	public ArrayList<Integer> getBestCitiesOrder() {
		return globalBestPosition;
	}

	@Override
	public int getBestSolutionIteration() {
		return bestSolutionIteration;
	}

}

