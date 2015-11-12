package cifo.searchMethods;

import cifo.core.Main;
import cifo.core.ProblemInstance;
import cifo.core.Solution;

public class RandomSearch extends SearchMethod {

	protected ProblemInstance instance;
	protected int numberOfTries;
	protected boolean printFlag;
	protected Solution currentBest;
	protected int currentGeneration;

	public RandomSearch() {
		instance = new ProblemInstance(Main.NUMBER_OF_TRIANGLES);
		numberOfTries = Main.NUMBER_OF_GENERATIONS * Main.POPULATION_SIZE;
		printFlag = false;
		currentGeneration = 0;
	}

	public void run() {
		initialize();
		evolve();
		Main.addBestSolution(currentBest);
	}

	public void initialize() {
		currentBest = new Solution(instance);
		currentBest.evaluate();
		updateInfo();
		currentGeneration++;
	}

	public void evolve() {

		while (currentGeneration <= numberOfTries) {
			Solution tempSolution = new Solution(instance);
			tempSolution.evaluate();
			if (tempSolution.getFitness() <= currentBest.getFitness()) {
				currentBest = tempSolution;
			}
			updateInfo();
			currentGeneration++;
		}
	}

	public void updateInfo() {
		currentBest.draw();
		series.add(currentGeneration, currentBest.getFitness());
		if (printFlag) {
			System.out.printf("Generation: %d\tFitness: %.1f\n", currentGeneration, currentBest.getFitness());
		}
	}
}
