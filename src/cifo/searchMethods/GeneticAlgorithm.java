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
		if (useElitism){eliteNum = (int) Math.ceil(populationSize*Main.ELITE_PROPORTION);}
		else {eliteNum = 0;};
		
		useDynamicPopulationSize = false;
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
		currentBest = getBest(population);
	}
	
	
	public void evolve() {
		while (currentGeneration <= numberOfGenerations) {
			Solution[] offspring = new Solution[populationSize];
			for (int k = 0; k < population.length; k++) {
				int[] parents = selectParents();
				offspring[k] = applyCrossover(parents);

				offspring[k] = offspring[k].applyMutationTest(mutationProbability);
 				offspring[k].evaluate();
				
			}
			if (useElitism){
				population = survivorSelection(offspring);
			}
			else{
				population=offspring;
			}
			
			
			Solution lastBest = this.currentBest;
			updateCurrentBest();
			

			if(useDynamicPopulationSize && this.currentGeneration > 2) {
				adaptPopulationSize(lastBest, currentBest);
				System.out.println(populationSize);
			}
			updateInfo();
			currentGeneration++;
		}
	}
	
	
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
	
	private void adaptPopulationSize(Solution lastBest, Solution currentBest) {
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
			int suppressCount =  (int) Math.ceil(Math.ceil(0.1 * populationSize) * relativeDelta);
			suppressPopulation( suppressCount );
		}
		else {
			double relativeDelta = 100 - (currentDelta / initialFitness) * 100;
			int increaseCount =  (int) Math.ceil(Math.ceil(0.05 * populationSize) * relativeDelta);
			increasePopulation( increaseCount );
		}
		lastDelta = currentDelta;
	}

	private void increasePopulation(int noOfChangedIndividuals) {
		if(noOfChangedIndividuals < 1) {
			noOfChangedIndividuals = 1;
		}
		if(populationSize + noOfChangedIndividuals <= maxPopulationSize) {
			populationSize += noOfChangedIndividuals;
		}
		else if(populationSize != maxPopulationSize){
			populationSize = maxPopulationSize;
		}
		else {
			return;
		}
		Solution[] newPopulation = new Solution[populationSize];
		for(int i = 0; i < newPopulation.length; i++) {
			if(i < newPopulation.length - noOfChangedIndividuals - 1) {
				newPopulation[i] = population[i];
			}
			else {
				Solution newIndividuals[] = getBest(population, noOfChangedIndividuals);
				int randomIndiv = r.nextInt(newIndividuals.length);
				newPopulation[i] = newIndividuals[randomIndiv].applyMutation();
			}
		}
		population = newPopulation;
	}

	private void suppressPopulation(int noOfChangedIndividuals) {
		if(noOfChangedIndividuals < 1) {
			noOfChangedIndividuals = 1;
		}
		if(populationSize - noOfChangedIndividuals >= minPopulationSize) {
			populationSize -= noOfChangedIndividuals;
		}
		else if(populationSize != minPopulationSize){
			populationSize = minPopulationSize;
		}
		else {
			return;
		}
		Solution[] worstIndividuals = getWorstNo(population, noOfChangedIndividuals); 
		Solution[] newPopulation = new Solution[populationSize];
		int lastFilledIndex = 0;
		for(int i = 0; i < population.length && lastFilledIndex < populationSize; i++) {
			boolean isWorst = false;
			for(int j = 0; j < worstIndividuals.length; j++) {	
				if(population[j] == population[i]) {
					isWorst = true;
				}
			}
			if(!isWorst) {
				newPopulation[lastFilledIndex] = population[i];
				lastFilledIndex++;
			}
		}
		population = newPopulation;
		
	}
	public Solution[] getWorstNo(Solution[] population, int noOfIndividuals) {
		Solution[] worst = new Solution[noOfIndividuals];
		for(int i = 0; i < worst.length; i++) {
			for(int j = 0; j < population.length; j ++) {
				boolean alreadyListed = false;
				for(int k = 0; k < i; k++) {
					if(worst[k] == population[j]) {
						alreadyListed = true;
						break;
					}
				}
				if(alreadyListed) {
					continue;
				}
				else if(worst[i] == null || worst[i].getFitness() > population[j].getFitness()) {
					worst[i] = population[j];
				}
			}
		}
		return worst;
	}
	public Solution[] getBest(Solution[] population, int noOfIndividuals) {
		Solution[] best = new Solution[noOfIndividuals];
		if(noOfIndividuals > population.length) {
			return population;
		}
		for(int i = 0; i < best.length; i++) {
			for(int j = 0; j < population.length; j ++) {
				boolean alreadyListed = false;
				for(int k = 0; k < i; k++) {
					if(best[k] == population[j]) {
						alreadyListed = true;
						break;
					}
				}
				if(alreadyListed) {
					continue;
				}
				else if(best[i] == null || best[i].getFitness() < population[j].getFitness()) {
					best[i] = population[j];
				}
			}
		}
		return best;
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
	

	//If best solutions of current pop are better than best offspring solution, they replace one of the worst offspring solutions
	//maximum solutions carried over is set by ELITE_PROPORTION field
	public Solution[] survivorSelection(Solution[] offspring) {

		Solution[] newPopulation = offspring;
		Solution[] elites = GetElites();
		int[] worst = GetWorst(offspring);
		double bestOffspringFitness  = getBest(offspring).getFitness();
		for (int i=0;i<elites.length;i++){
			if (elites[i].getFitness()<bestOffspringFitness){
				newPopulation[worst[i]] = elites[i];
			}
			
		}
		
//		double bestOffspring=getBest(newPopulation).getFitness();
//		if (bestOffspring>currentBest.getFitness()){
//			printPopulationFitness(offspring);
//			printPopulationFitness(population);
//			for (int i=0;i<elites.length;i++){
//				System.out.println("worst in pop: " + newPopulation[worst[i]].getFitness()+ " Best in pop:" + elites[i].getFitness());
//			}
//		}
				
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
