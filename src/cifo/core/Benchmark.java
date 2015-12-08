package cifo.core;

import java.util.ArrayList;

import cifo.core.Solution.MutationOperator;
import cifo.searchMethods.GeneticAlgorithm.XOOperator;

public class Benchmark {
	private static String results = "\nSEARCH_METHOD,NUMBER_OF_TRIANGLES,NUMBER_OF_RUNS,NUMBER_OF_GENERATIONS,POPULATION_SIZE,NEIGHBORHOOD_SIZE,MUTATION_PROBABILIY,TOURNAMENT_SIZE,mean,stdDev,best,worst,XO,DynPop,TimeInSecs,MuOps";
	private static ParameterSet[] parameterSets;
	
	public static void main(String args[]) {
		int fraction = 1;
		int denominator = 1;
		ParameterSet[] fullSet = ParameterSet.generateParameterSets(Main.SearchMethods.GA, 1, 2000);
		parameterSets = ParameterSet.getPortion(fraction, denominator, fullSet);
		System.out.println("Benchmark started. Testing " + parameterSets.length +" out of " + fullSet.length + " parameter combinations at fraction " + fraction + " of " + denominator);
		Main.runInBenchmarkMode(1, parameterSets[0]);
	}
	
	public static void continueAt(int benchmarkPosition, String results) {
		System.out.print(".");
		Benchmark.results += "\n" + results;
		if(benchmarkPosition<parameterSets.length) { // check if out of bounds
			Main.runInBenchmarkMode(benchmarkPosition + 1, parameterSets[benchmarkPosition]);
		}
		else {
			// save results
			System.out.println(Benchmark.results);
			return;
		}
	}
	
	static class ParameterSet {
		public Main.SearchMethods SEARCH_METHOD;
		public int NUMBER_OF_TRIANGLES;
		public int NUMBER_OF_RUNS;

		public int NUMBER_OF_GENERATIONS;
		public int POPULATION_SIZE;
		public int NEIGHBORHOOD_SIZE;
		public double MUTATION_PROBABILIY;
		public int TOURNAMENT_SIZE;
		public XOOperator[] CROSSOVER_OPERATORS;
		public int USE_DYNAMIC_POPULATION_SIZE;
		public MutationOperator[] MUTATION_OPERATORS;

		public ParameterSet(Main.SearchMethods SEARCH_METHOD, int NUMBER_OF_TRIANGLES, int NUMBER_OF_RUNS, int NUMBER_OF_GENERATIONS, int POPULATION_SIZE, double MUTATION_PROBABILIY, int TOURNAMENT_SIZE, XOOperator[] xoOperators, int dynPopSize, MutationOperator[] muOperators, int ns) {
			this.SEARCH_METHOD = SEARCH_METHOD;
			this.NUMBER_OF_TRIANGLES = NUMBER_OF_TRIANGLES;
			this.NUMBER_OF_RUNS = NUMBER_OF_RUNS;
			this.NUMBER_OF_GENERATIONS = NUMBER_OF_GENERATIONS;
			this.POPULATION_SIZE = POPULATION_SIZE;
			this.NEIGHBORHOOD_SIZE = ns;
			this.MUTATION_PROBABILIY = MUTATION_PROBABILIY;
			this.TOURNAMENT_SIZE = TOURNAMENT_SIZE;
			this.CROSSOVER_OPERATORS = xoOperators;
			this.MUTATION_OPERATORS = muOperators;
			this.USE_DYNAMIC_POPULATION_SIZE = dynPopSize;
		}

		public static ParameterSet[] generateParameterSets(Main.SearchMethods SEARCH_METHOD, int NUMBER_OF_RUNS, int NUMBER_OF_GENERATIONS) {
			int[] populationSizes = {70};
			double[] mutationProbablilities = {0.1};
			int[] neighborhoodSizes = {4};
			int[] tournamentSizes = {6};
			int[] triangleNumbers = {100};
			XOOperator[][] xoOperatorCombinations = {{XOOperator.TRIANGLE_BASED}};
			MutationOperator[][] muOperatorCombinations = {{MutationOperator.oneValue, MutationOperator.locationFlip}};
			int[] useDynamicPopulationSize = {150};
			
			ArrayList<ParameterSet> parameterSet = new ArrayList<>();
			for(int i = 0; i < populationSizes.length; i++) {
					for(int j = 0; j < mutationProbablilities.length; j++) {
						for(int k = 0; k < tournamentSizes.length; k++) {
							for(int l = 0; l < xoOperatorCombinations.length; l++) {
								for(int m = 0; m < triangleNumbers.length; m++) {
									for(int n = 0; n < useDynamicPopulationSize.length; n++) {
										for(int o = 0; o < muOperatorCombinations.length; o++) {
											for(int p = 0; p < neighborhoodSizes.length; p++) {
												parameterSet.add(new ParameterSet(SEARCH_METHOD, triangleNumbers[m], NUMBER_OF_RUNS, NUMBER_OF_GENERATIONS, populationSizes[i], mutationProbablilities[j], tournamentSizes[k], xoOperatorCombinations[l], useDynamicPopulationSize[n], muOperatorCombinations[o], neighborhoodSizes[p]));
											}
										}
									}
								}
						}
					}
				}
			}
			return parameterSet.toArray(new ParameterSet[parameterSet.size()]);

		}
		
		public static ParameterSet[] getPortion(int fraction, int denominator, ParameterSet[] sets) {
			int fractionSize = sets.length / denominator;
			int upperLimit = fractionSize * fraction;
			int lowerLimit = upperLimit - fractionSize;
			if(sets.length % 2 != 0 && fraction == denominator) {
				fractionSize += sets.length % denominator;
				
			}
			ParameterSet[] setsFraction = new ParameterSet[fractionSize];
			for(int i = 0; i < fractionSize; i++) {
				setsFraction[i] = sets[lowerLimit + i];
			}
			return setsFraction;
		}
	}
}
