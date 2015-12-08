package cifo.searchMethods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import cifo.core.Main;
import cifo.core.ProblemInstance;
import cifo.core.Solution;
import cifo.core.Solution.MutationOperator;


public class GeneticAlgorithm extends SearchMethod {

	public enum XOOperator {VERTEX_BASED, COLOR_BASED, TRIANGLE_BASED, MULTI_TRIANGLE_BASED, MULTI_RANDOM_FEATURE, LAYER_BASED, ALTERNATING};
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
	protected boolean useElitism;
	protected int eliteNum;
	protected XOOperator[] crossoverOperators;

	// Fields for dynamic population sizes
	protected int useDynamicPopulationSize; // set to negative to disable dyn pop
	protected double lastDelta;
	protected double pivotSum;
	protected int period;
	protected double deltaSum;
	
	protected double sumDiversity;

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
		
		useDynamicPopulationSize = Main.USE_DYNAMIC_POPULATION_SIZE;
		period = 10;
		
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
		sumDiversity = this.calculateGenotypicEntropy(population);
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
			

			if(useDynamicPopulationSize >= 0 && this.currentGeneration > useDynamicPopulationSize) {
				String populationSizeChange = this.populationSize + " --> ";
				String debug = adaptPopulationSize(lastBest, currentBest);
				if (printFlag) {
					System.out.println(debug + "\t" + populationSizeChange + this.populationSize);
				}
			}			
			sumDiversity += calculateGenotypicEntropy(population);
			if(currentGeneration % period == 0) {
				sumDiversity = sumDiversity / period;
				//System.out.println("Gen " + currentGeneration + " diversity: " + Math.round(sumDiversity) +"%");
				sumDiversity = 0;
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
		if(currentGeneration % period != 0) {
			deltaSum += currentDelta;
			return "--";
		}
		else {
			double averageDelta = deltaSum / period;
			deltaSum = 0;
			if(averageDelta == 0) {
				increasePopulation(3);
			}
//			else if(averageDelta < 5) {
//				increasePopulation(3);
//			}
//			else if(averageDelta > 50) {
//				suppressPopulation(5);
//			}
			else if(averageDelta > 15) {
				suppressPopulation(3);
			}
			return Math.round(Math.round(averageDelta)) + "";
		}
//		double pivot = lastDelta - currentDelta; // SUP method
//		if(currentGeneration % period == 0) {
//			pivot += pivotSum;
//			pivotSum = 0;
//			pivot = pivot / period;
//		}
//		else {
//			pivotSum += pivot;
//		}
//				
//		if(currentDelta > pivot) {
//			double relativeDelta = (currentDelta / initialFitness) * 100;
//			int suppressCount =  (int) Math.ceil(Math.ceil(0.1 * populationSize) * currentDelta);
//			suppressPopulation( 3 );
//		}
//		else {
//			double relativeDelta = 100 - (currentDelta / initialFitness) * 100;
//			int increaseCount =  (int) Math.ceil(Math.ceil(0.05 * populationSize) * currentDelta);
//			increasePopulation( 3 );
//		}
//		lastDelta = currentDelta;
		
//		return (currentDelta>pivot? "-" : "+") + "\t" + (int)Math.floor(currentDelta) + "\t" + (int)Math.floor(pivot) + "\t" + (int)Math.floor(currentDelta - pivot);
	}

	

	private void suppressPopulation(int count) {
		if(populationSize - count < minPopulationSize) {
			count = populationSize - minPopulationSize;
		}
		populationSize -= count;
		population = getBest(population, populationSize);
	}

	private void increasePopulation(int count) {
		if(populationSize + count > maxPopulationSize) {
			suppressPopulation(count*2);
			count = maxPopulationSize - populationSize;
		}
		populationSize += count;
		Solution[] newPopulation = new Solution[populationSize];
		Solution[] bestIndividuals = getBest(population, count);
		MutationOperator[] muOps = {MutationOperator.orderFlip, MutationOperator.locationFlip, MutationOperator.manyValueChange, MutationOperator.manyValueAddSubtract};
		for(int i = 0; i < count; i++) {
			// generate a new individual from the best, based on mutation
			newPopulation[i] = bestIndividuals[i].applyMutationTest(0.3, muOps);
		}
		for(int i = count; i < newPopulation.length; i++) {
			newPopulation[i] = population[i-count];
		}
		population = newPopulation;
		Arrays.sort(population);
	}

	public Solution applyCrossover(int[] parents) {
		Solution firstParent = population[parents[0]];
		Solution secondParent = population[parents[1]];
		Solution offspring = firstParent.copy();
		int crossoverPoint = r.nextInt(instance.getNumberOfTriangles());

		// randomly choose XO Operator to be used
		XOOperator xoOp = crossoverOperators[r.nextInt(crossoverOperators.length)];
		switch(xoOp) {
			case VERTEX_BASED:
				vertexBasedCrossover(offspring, secondParent, crossoverPoint);
				break;
			case COLOR_BASED:
				colorBasedCrossover(offspring, secondParent, crossoverPoint);
				break;
			case MULTI_TRIANGLE_BASED:
				multiPointCompleteCrossover(offspring, secondParent);
				break;
			case MULTI_RANDOM_FEATURE:
				multiPointRandomFeatureCrossover(offspring, secondParent);
				break;
			case TRIANGLE_BASED:
				completeCrossover(offspring, secondParent, crossoverPoint);
				break;
			case LAYER_BASED:
				frontMostCrossover(offspring, secondParent, crossoverPoint);
				break;
			case ALTERNATING:
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
	
	public double calculateGenotypicEntropy(Solution[] zombiePool) {
		Solution origin = zombiePool[r.nextInt(zombiePool.length)];
		double[] distances = new double[zombiePool.length];
		int minDistance = 0;
		int maxDistance = 1 * Solution.VALUES_PER_TRIANGLE * Main.NUMBER_OF_TRIANGLES; // = 1000
		ArrayList<ArrayList<Solution>> buckets = new ArrayList<>();
		// create 100 buckets
		for(int i = minDistance; i < 100; i ++) {
			buckets.add(new ArrayList<Solution>());
		}
		for(int i = 0; i < zombiePool.length; i++) {
			Solution individual = zombiePool[i];
			double distance = calculateHemmingDistance(individual, origin);
			for(int bucketNo = 0; bucketNo < buckets.size(); bucketNo++) {
				if(distance < (maxDistance / buckets.size())*bucketNo) {
					buckets.get(bucketNo).add(individual);
					break;
				}
			}
		}
		// calculate entropy
		double entropy = 0;
		double maxEntropy = populationSize * Math.log(populationSize);
		String bucketPrint = "|";
		for(int bucketNo = 0; bucketNo < buckets.size(); bucketNo++) {
			ArrayList<Solution> bucket = buckets.get(bucketNo);
			bucketPrint += bucket.size() + "|";
			if(bucket.size() > 0) {
				entropy += bucket.size() * Math.log(bucket.size());
			}
		}
		double diversity = (1 - entropy / maxEntropy)*100;
		//System.out.println(bucketPrint);
		//System.out.println("Diversity: " + Math.round(Math.round(diversity)) + "%\tEntropy: " + entropy + "\tMax Entropy: " + maxEntropy);
		
		return diversity;
	}

	private double calculateHemmingDistance(Solution solution, Solution origin) {
		double distance = 0;
		for(int triangleNo = 0; triangleNo < Main.NUMBER_OF_TRIANGLES; triangleNo++) {
			for(int valueNo = 0; valueNo < Solution.VALUES_PER_TRIANGLE; valueNo++) {
				double absoluteDistance = solution.getValue(triangleNo + valueNo) - origin.getValue(triangleNo + valueNo);
				// standardize distance
				if(valueNo < 4) {
					distance += Math.abs(absoluteDistance) / 255;
				}
				else {
					distance += Math.abs(absoluteDistance) / 200;
				}
			}
		}
		return distance;
	}
}
