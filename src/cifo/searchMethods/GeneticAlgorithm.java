package cifo.searchMethods;

import java.util.Arrays;
import java.util.Random;

import cifo.core.Main;
import cifo.core.ProblemInstance;
import cifo.core.Solution;


public class GeneticAlgorithm extends SearchMethod {

	public enum XOOperator {vertexBased, colorBased, complete, multiPointComplete, multiPointRandomFeature, frontMost, alternating};
	protected ProblemInstance instance;
	protected int populationSize, numberOfGenerations, minPopulationSize, maxPopulationSize;
	protected double mutationProbability;
	protected int tournamentSize;
	protected boolean printFlag;
	protected double initialFitness;
	protected Solution currentBest;
	protected int currentGeneration;
	protected Solution[] population;
	protected Random r;
	protected boolean useElitism, useDynamicPopulationSize;
	protected int eliteNum;
	protected XOOperator[] crossoverOperators;

	// Fields for dynamic population sizes
	protected double lastDelta;
	protected double pivotSum;
	protected int period;
	protected double averageFitness; // #forKristen

	public GeneticAlgorithm() {
		instance = new ProblemInstance(Main.NUMBER_OF_TRIANGLES);
		populationSize = Main.POPULATION_SIZE;
		minPopulationSize = 10;
		maxPopulationSize = 90;
		numberOfGenerations = Main.NUMBER_OF_GENERATIONS;
		mutationProbability = Main.MUTATION_PROBABILIY;
		tournamentSize = Main.TOURNAMENT_SIZE;
		crossoverOperators = Main.CROSSOVER_OPERATORS;
		printFlag = false;
		currentGeneration = 0;
		useElitism = Main.USE_ELITISM;
		eliteNum = (int) Math.ceil(populationSize*Main.ELITE_PROPORTION);
		
		useDynamicPopulationSize = true;
		period = 20;
		
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
		currentBest = getBest(population,1)[0];
	}
	
	
	public void evolve() {
		Arrays.sort(population);
		while (currentGeneration <= numberOfGenerations) {
			Solution[] offspring = new Solution[populationSize];
			for (int k = 0; k < population.length; k++) {
				int[] parents = selectParents();
				offspring[k] = applyCrossover(parents);

				offspring[k] = offspring[k].applyMutation();
 				offspring[k].evaluate();
			}
			Arrays.sort(offspring);
			if (useElitism){
				population = survivorSelection(offspring);
			}
			else{
				population=offspring;
			}
			
			
			Solution lastBest = this.currentBest;
			updateCurrentBest();
			

			if(useDynamicPopulationSize && this.currentGeneration > 2) {
				String populationSizeChange = this.populationSize + " --> ";
				String debug = adaptPopulationSize(lastBest, currentBest);
				//System.out.println(populationSizeChange + this.populationSize + " " + debug);
			}
			updateInfo();
			currentGeneration++;
		}
	}
	
	
	public double averagePopulationFitness(Solution[] pop){
		double averageFitness = 0;
		for(int i=0; i<pop.length;i++){
			averageFitness+=pop[i].getFitness();
		}
		averageFitness=averageFitness/pop.length;
		return averageFitness;
	}
		
			
	
	//can be deleted
	public void printPopulationFitness(Solution[] pop){
		double[] fitness = new double[population.length];
		for(int i=0; i<pop.length;i++){
			fitness[i]=pop[i].getFitness();
		}
		Arrays.sort(fitness);
		for(int i=0; i<pop.length;i++){
		System.out.println("Solution " +i +": "+ fitness[i]);
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
	
	private String adaptPopulationSize(Solution lastBest, Solution currentBest) {
		double currentDelta = lastBest.getFitness() - currentBest.getFitness();
		double pivot = lastDelta - currentDelta; // SUP method
		if(currentGeneration % period == 0) {
			pivot += pivotSum;
			pivotSum = 0;
			pivot = pivot / period;
		}
		else {
			pivotSum += pivot;
		}
				
		if(currentDelta > pivot) {
			double relativeDelta = (currentDelta / initialFitness) * 100;
			int suppressCount =  (int) Math.ceil(Math.ceil(0.1 * populationSize) * currentDelta);
			//suppressPopulation( 3 );
		}
		else {
			double relativeDelta = 100 - (currentDelta / initialFitness) * 100;
			int increaseCount =  (int) Math.ceil(Math.ceil(0.05 * populationSize) * currentDelta);
			//increasePopulation( 3 );
		}
		lastDelta = currentDelta;
		return (currentDelta>pivot? "-" : "+") + "\t" + (int)Math.floor(currentDelta) + "\t" + (int)Math.floor(pivot) + "\t" + (int)Math.floor(currentDelta - pivot);
	}

	

	private void suppressPopulation(int count) {
//		if(populationSize - count < minPopulationSize) {
//			count = populationSize - 
//		}
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
				break;
			case frontMost:
				frontMostCrossover(offspring, secondParent, crossoverPoint);
				break;
			case alternating:
				alternatingCrossover(offspring, secondParent);
				break;
		}
		return offspring;
	}
	

	private void completeCrossover(Solution offspring, Solution secondParent, int crossoverPoint) {
		crossoverPoint = crossoverPoint * Solution.VALUES_PER_TRIANGLE;
		for (int i = crossoverPoint; i < instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE; i++) {
			offspring.setValue(i, secondParent.getValue(i));
		}
	}
	
	private void frontMostCrossover(Solution offspring, Solution secondParent, int crossoverPoint) {
		crossoverPoint = crossoverPoint * Solution.VALUES_PER_TRIANGLE;
		int inverseCrossoverPoint = instance.getNumberOfTriangles() * Solution.VALUES_PER_TRIANGLE - crossoverPoint;
		for (int i = 0; i < crossoverPoint; i++) {
			offspring.setValue(i, secondParent.getValue(inverseCrossoverPoint + i));
		}
	}
	
	private void alternatingCrossover(Solution offspring, Solution secondParent) {
		for (int i = 0; i < instance.getNumberOfTriangles(); i+=2) {
			for(int j = 0; j < Solution.VALUES_PER_TRIANGLE; j++) {
				offspring.setValue(i+j, secondParent.getValue(i+j));
			}
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
	
	public Solution[] getBest(Solution[] zombiePool, int count){ // not actually zombies here
		return Arrays.copyOfRange(zombiePool, 0, count); //for(int i = 0; i < count; )
	}
	
	public Solution[] getWorst(Solution[] zombiePool, int count){ // not actually zombies here
		return Arrays.copyOfRange(zombiePool, zombiePool.length-count, zombiePool.length);
	}

	//If best solutions of current pop are better than best offspring solution, they replace one of the worst offspring solutions
	//maximum solutions carried over is set by ELITE_PROPORTION field
	public Solution[] survivorSelection(Solution[] offspring) {
		Solution[] newPopulation = offspring;
		Solution[] elites = getBest(population, eliteNum);
		double bestOffspringFitness = getBest(offspring, 1)[0].getFitness();
		for (int i=0;i<elites.length;i++){
			if (elites[i].getFitness()<bestOffspringFitness){
				newPopulation[newPopulation.length - i - 1] = elites[i];
			}
			else {
				break;
			}
		}
		Arrays.sort(newPopulation);
		return newPopulation;
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
