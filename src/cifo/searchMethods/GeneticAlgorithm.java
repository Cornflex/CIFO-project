package cifo.searchMethods;

import java.util.Arrays;
import java.util.Random;

import cifo.core.Main;
import cifo.core.ProblemInstance;
import cifo.core.Solution;


public class GeneticAlgorithm extends SearchMethod {

	public enum XOOperator {vertexBased, colorBased, complete, multiPointComplete, multiPointRandomFeature};
	protected ProblemInstance instance;
	protected int populationSize, numberOfGenerations;
	protected double mutationProbability;
	protected int tournamentSize;
	protected boolean printFlag;
	protected double initialFitness;
	protected Solution currentBest;
	protected int currentGeneration;
	protected Solution[] population;
	protected Random r;
	protected boolean useElitism;
	protected int eliteNum;
	protected XOOperator[] crossoverOperators;

	public GeneticAlgorithm() {
		instance = new ProblemInstance(Main.NUMBER_OF_TRIANGLES);
		populationSize = Main.POPULATION_SIZE;
		numberOfGenerations = Main.NUMBER_OF_GENERATIONS;
		mutationProbability = Main.MUTATION_PROBABILIY;
		tournamentSize = Main.TOURNAMENT_SIZE;
		crossoverOperators = Main.CROSSOVER_OPERATORS;
		printFlag = false;
		currentGeneration = 0;
		useElitism = Main.USE_ELITISM;
		if (useElitism){eliteNum = (int) Math.round(populationSize*Main.ELITE_PROPORTION);}
		else {eliteNum = 0;};
		
		r = new Random();
	}

	public void run() {
		initialize();
		evolve();
		Main.addBestSolution(currentBest);
	}

	public void initialize() {
		population = new Solution[populationSize];
		for (int i = 0; i < population.length; i++) {
			population[i] = new Solution(instance);
			population[i].evaluate();
		}
		updateCurrentBest();
		updateInfo();
		currentGeneration++;
	}

	public void updateCurrentBest() {
		currentBest = getBest(population);
	}
	
	
	public void evolve() {
		while (currentGeneration <= numberOfGenerations) {
			Solution[] offspring = new Solution[populationSize];
			for (int k = 0; k < population.length; k++) {
				int[] parents = selectParents();
				offspring[k] = applyCrossover(parents);
				if (r.nextDouble() <= mutationProbability) {
					offspring[k] = offspring[k].applyMutation();
				}
				offspring[k].evaluate();
			}
			if (useElitism){
				population = survivorSelection(offspring);
			}
			else{
				population=offspring;
			}
			updateCurrentBest();
			updateInfo();
			currentGeneration++;
		}
	}

	public int[] selectParents() {
		int[] parents = new int[2];
		parents[0] = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (population[temp].getFitness() < population[parents[0]].getFitness()) {
				parents[0] = temp;
			}
		}

		parents[1] = r.nextInt(populationSize);
		for (int i = 0; i < tournamentSize; i++) {
			int temp = r.nextInt(populationSize);
			if (population[temp].getFitness() < population[parents[1]].getFitness()) {
				parents[1] = temp;
			}
		}
		return parents;
	}

	public Solution applyCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int crossoverPoint = r.nextInt(instance.getNumberOfTriangles());

		// randomly choose XO Operator to be used
		XOOperator xoOp = crossoverOperators[r.nextInt(crossoverOperators.length)];
		switch(xoOp) {
			case vertexBased:
				vertexBasedCrossover(offspring, secondParent, crossoverPoint);
				break;
			case colorBased:
				colorBasedCrossover(offspring, secondParent, crossoverPoint);
				break;
			case multiPointComplete:
				multiPointCompleteCrossover(offspring, secondParent);
				break;
			case multiPointRandomFeature:
				multiPointRandomFeatureCrossover(offspring, secondParent);
				break;
			case complete:
				completeCrossover(offspring, secondParent, crossoverPoint);
		}
		return offspring;
	}
	

	private void completeCrossover(Solution offspring, Solution secondParent, int crossoverPoint) {
		crossoverPoint = crossoverPoint * Solution.VALUES_PER_TRIANGLE;
		for (int i = crossoverPoint; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			offspring.setValue(i, secondParent.getValue(i));
		}
	}
	
	private void multiPointCompleteCrossover(Solution offspring, Solution secondParent) {
		int noOfCrossovers = 1+(instance.getNumberOfTriangles()/10);
		for (int i = 0; i < noOfCrossovers; i++) {
			int crossoverPoint = r.nextInt(instance.getNumberOfTriangles());
			for (int valueIndex = crossoverPoint; valueIndex < crossoverPoint + Solution.VALUES_PER_TRIANGLE; valueIndex++) {
				offspring.setValue(valueIndex, secondParent.getValue(valueIndex));
			}
		}
	}
	
	private void multiPointRandomFeatureCrossover(Solution offspring, Solution secondParent) {
		int noOfCrossovers = 1+(instance.getNumberOfTriangles()/10);
		for (int i = 0; i < noOfCrossovers; i++) {
			int crossoverPoint = r.nextInt(instance.getNumberOfTriangles());
			for (int valueIndex = crossoverPoint; valueIndex < crossoverPoint + Solution.VALUES_PER_TRIANGLE; valueIndex++) {
				if(r.nextBoolean()) {
					offspring.setValue(valueIndex, secondParent.getValue(valueIndex));
				}
			}
		}
	}

	private void colorBasedCrossover(Solution offspring, Solution secondParent, int crossoverPoint) {
		for(int i = crossoverPoint; i < instance.getNumberOfTriangles(); i++) {
			offspring.setAlpha(i, secondParent.getAlpha(i));
			offspring.setHue(i, secondParent.getHue(i));
			offspring.setSaturation(i, secondParent.getSaturation(i));
			offspring.setBrightness(i, secondParent.getBrightness(i));
		}		
	}

	private void vertexBasedCrossover(Solution offspring, Solution secondParent, int crossoverPoint) {
		for(int i = crossoverPoint; i < instance.getNumberOfTriangles(); i++) {
			offspring.setXFromVertex1(i, secondParent.getXFromVertex1(i));
			offspring.setYFromVertex1(i, secondParent.getYFromVertex1(i));
			offspring.setXFromVertex2(i, secondParent.getXFromVertex2(i));
			offspring.setYFromVertex2(i, secondParent.getYFromVertex2(i));
			offspring.setXFromVertex3(i, secondParent.getXFromVertex3(i));
			offspring.setYFromVertex3(i, secondParent.getYFromVertex3(i));
		}
	}
	
	//for elitism, gets array of elites in the population
	public Solution[] GetElites(){
		
		Solution[] elites = new Solution[eliteNum];
		int idxExclude=0;
		int[] excluded = new int[eliteNum];
		
		for (int j=0; j < eliteNum; j++){
			Solution best = population[0];

			for (int i = 1; i < population.length; i++) {
				boolean temp = false;
				for(int k: excluded){if (i==k) temp = true;}
				if ((temp==false) && (population[i].getFitness() < best.getFitness())) {
					best = population[i];
					idxExclude = i;
				}		
			}	
			elites[j]=best;
			excluded[j]=idxExclude;
		}
		return elites;
	}
	
	//prints out the fitness of the best solutions in current population (the expected elites) just used for checking my code
	public void eliteCheck(){
		double[] fitness = new double[population.length];
		for(int i=0; i<population.length;i++){
			fitness[i]=population[i].getFitness();
		}
		Arrays.sort(fitness);
		for(int i=0; i<eliteNum;i++){
		System.out.println("most fit " + fitness[i]);
		}
	}
	
	//for elitism, gets the indexes of the worst solutions in the population
	public int[] GetWorst(Solution[] offspring){
		
		int idxExclude=0;
		int[] excluded = new int[eliteNum];
		
		for (int j=0; j < eliteNum; j++){
			Solution worst = offspring[0];
			for (int i = 1; i < offspring.length; i++) {
				boolean match = false;
				//if i is in the excluded indexes, set match to true
				for(int k: excluded){if (i==k) match = true;}
				//if match is false, check fitness, if fitness is worse, update worst
				if ((match==false) && (offspring[i].getFitness() > worst.getFitness())) {
					worst = offspring[i];
					idxExclude = i;
				}		
			}	
			excluded[j]=idxExclude;
			
		}

		return excluded;
	}
	
	//prints out the fitness of the best solutions in current population (the expected elites) just used for checking my cod
	public void worstCheck(Solution[] offspring){
		double[] fitness = new double[population.length];
		for(int i=0; i<offspring.length;i++){
			fitness[i]=offspring[i].getFitness();
		}
		Arrays.sort(fitness);
		for(int i=fitness.length-1; i>fitness.length-1-eliteNum;i--){
		System.out.println("least fit " + fitness[i] + " at index " + i);
		}
	}
	

	//implementation of elitism
	public Solution[] survivorSelection(Solution[] offspring) {

		Solution[] newPopulation = offspring;
		Solution[] best = GetElites();
		int[] worst = GetWorst(offspring);
		for (int i=0;i<best.length;i++){
			
			newPopulation[worst[i]] = best[i];
		}
		
		
		return newPopulation;
	}

	public Solution getBest(Solution[] solutions) {
		Solution best = solutions[0];
		for (int i = 1; i < solutions.length; i++) {
			if (solutions[i].getFitness() < best.getFitness()) {
				best = solutions[i];
			}
		}
		return best;
	}


	public void updateInfo() {
		currentBest.draw();
		series.add(currentGeneration, currentBest.getFitness());
		if (currentGeneration==0){
			initialFitness=currentBest.getFitness();
		}
		if (printFlag) {
			System.out.printf("Generation: %d\tFitness: %.1f\n", currentGeneration, currentBest.getFitness());
		}
	}
}
