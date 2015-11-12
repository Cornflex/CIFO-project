package cifo.searchMethods;

import cifo.core.Main;
import cifo.core.ProblemInstance;
import cifo.core.Solution;

public class HillClimber extends SearchMethod {

	protected ProblemInstance instance;
	protected int neighborhoodSize, numberOfGenerations;
	protected boolean printFlag;
	protected Solution currentBest;
	protected int currentGeneration;

	public HillClimber() {
		instance = new ProblemInstance(Main.NUMBER_OF_TRIANGLES);
		neighborhoodSize = Main.NEIGHBORHOOD_SIZE;
		numberOfGenerations = Main.NUMBER_OF_GENERATIONS;
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
		while (currentGeneration <= numberOfGenerations) {
			// explore the neighborhood with the mutation operator
			Solution bestNeighbor = currentBest.applyMutation();
			bestNeighbor.evaluate();
			for (int k = 1; k < neighborhoodSize; k++) {
				Solution tempSolution = currentBest.applyMutation();
				tempSolution.evaluate();
				if (tempSolution.getFitness() <= bestNeighbor.getFitness()) {
					bestNeighbor = tempSolution;
				}
			}

			// update current best if the best neighbor is better or equal
			if (bestNeighbor.getFitness() <= currentBest.getFitness()) {
				currentBest = bestNeighbor;
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
