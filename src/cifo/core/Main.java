package cifo.core;

import cifo.utils.Statistics;
import cifo.searchMethods.GeneticAlgorithm.XOOperator;
import cifo.core.Solution.MutationOperator;
import gd.gui.GeneticDrawingApp;

public class Main {

	public enum SearchMethods {
		GA, HC, RS
	}

	public static SearchMethods SEARCH_METHOD = SearchMethods.GA;
	//public static SearchMethods SEARCH_METHOD = SearchMethods.HC;
	// public static SearchMethods SEARCH_METHOD = SearchMethods.RS;

	public static int NUMBER_OF_TRIANGLES = 100;

	public static int NUMBER_OF_RUNS = 3;
	public static int NUMBER_OF_GENERATIONS = 2000;
	public static int POPULATION_SIZE = 50;
	public static int NEIGHBORHOOD_SIZE = 10;
	public static double MUTATION_PROBABILIY = 0.02;
	public static int TOURNAMENT_SIZE = 3;
	public static boolean USE_ELITISM = true;
	public static double ELITE_PROPORTION = 0.05;
	public static int USE_DYNAMIC_POPULATION_SIZE = 150;
	
	public static int benchmarkPosition = 0;

	public static boolean KEEP_WINDOWS_OPEN = false;
	public static boolean printFlag = true;

	public static Solution[] bestSolutions = new Solution[NUMBER_OF_RUNS];
	public static double[] bestFitness = new double[NUMBER_OF_RUNS];
	public static int currentRun = 0;
	
	public static XOOperator[] CROSSOVER_OPERATORS = {XOOperator.alternating, XOOperator.frontMost};
	public static MutationOperator[] MUTATION_OPERATORS = {MutationOperator.oneValue};
	public static void main(String[] args) {
		run();
	}
	
	public static void runInBenchmarkMode(int currentPos, Benchmark.ParameterSet parameterSet) {
		Main.benchmarkPosition = currentPos;
		
		Main.SEARCH_METHOD = parameterSet.SEARCH_METHOD;
		Main.NUMBER_OF_TRIANGLES = parameterSet.NUMBER_OF_TRIANGLES;
		Main.NUMBER_OF_RUNS = parameterSet.NUMBER_OF_RUNS;
		Main.NUMBER_OF_GENERATIONS = parameterSet.NUMBER_OF_GENERATIONS;
		Main.POPULATION_SIZE = parameterSet.POPULATION_SIZE;
		Main.NEIGHBORHOOD_SIZE = parameterSet.NEIGHBORHOOD_SIZE;
		Main.MUTATION_PROBABILIY = parameterSet.MUTATION_PROBABILIY;
		Main.TOURNAMENT_SIZE = parameterSet.TOURNAMENT_SIZE;
		Main.CROSSOVER_OPERATORS = parameterSet.CROSSOVER_OPERATORS;
		Main.USE_DYNAMIC_POPULATION_SIZE = parameterSet.USE_DYNAMIC_POPULATION_SIZE;
		
		// set other parameters
		KEEP_WINDOWS_OPEN = false;
		printFlag = false;
		bestSolutions = new Solution[NUMBER_OF_RUNS];
		bestFitness = new double[NUMBER_OF_RUNS];
		currentRun = 0;
		main(null);
		
	}

	public static void addBestSolution(Solution bestSolution) {
		bestSolutions[currentRun] = bestSolution;
		bestFitness[currentRun] = bestSolution.getFitness();
		if(printFlag) {
			System.out.printf("Got %.2f as a result for run %d\n", bestFitness[currentRun], currentRun + 1);
			System.out.print("All runs:");
			for (int i = 0; i <= currentRun; i++) {
				System.out.printf("\t%.2f", bestFitness[i]);
			}
			System.out.println();
		}
		currentRun++;
		if (KEEP_WINDOWS_OPEN == false) {
			ProblemInstance.view.getFittestDrawingView().dispose();
			ProblemInstance.view.getFrame().dispose();
		}
		if (currentRun < NUMBER_OF_RUNS) {
			run();
		} else if(benchmarkPosition > 0) {
			Benchmark.continueAt(benchmarkPosition, resultsAsCSV());
		} else {
			presentResults();
		}
	}

	
	public static void presentResults() {
		double mean = Statistics.mean(bestFitness);
		double stdDev = Statistics.standardDeviation(bestFitness);
		double best = Statistics.min(bestFitness);
		double worst = Statistics.max(bestFitness);
		System.out.printf("\n\t\tMean +- std dev\t\tBest\t\tWorst\n\n");
		System.out.printf("Results\t\t%.2f +- %.2f\t%.2f\t%.2f\n", mean, stdDev, best, worst);
		System.out.println(resultsAsCSV());
	}
	
	public static String resultsAsCSV() {
		double mean = Statistics.mean(bestFitness);
		double stdDev = Statistics.standardDeviation(bestFitness);
		double best = Statistics.min(bestFitness);
		double worst = Statistics.max(bestFitness);
		String xoOps = "";
		for(XOOperator xoOp : Main.CROSSOVER_OPERATORS) {
			xoOps += xoOp.name() + "/";
		}
		xoOps = xoOps.substring(0, xoOps.length() - 1);
		return(
				SEARCH_METHOD + ","
				+ NUMBER_OF_TRIANGLES + ","
				+ NUMBER_OF_RUNS + ","
				+ NUMBER_OF_GENERATIONS + ","
				+ POPULATION_SIZE + ","
				+ NEIGHBORHOOD_SIZE + ","
				+ MUTATION_PROBABILIY + ","
				+ TOURNAMENT_SIZE + ","
				+ mean + ","
				+ stdDev + ","
				+ best + ","
				+ worst + ","
				+ xoOps + ","
				+ USE_DYNAMIC_POPULATION_SIZE
		);
		//System.out.printf("Results\t\t%.2f +- %.2f\t%.2f\t%.2f\n", mean, stdDev, best, worst);
	}

	public static void run() {
		GeneticDrawingApp.main(null);
	}
}
