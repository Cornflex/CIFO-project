package cifo.core;

import java.util.ArrayList;

public class Benchmark {
	private static String results = "\nSEARCH_METHOD,NUMBER_OF_TRIANGLES,NUMBER_OF_RUNS,NUMBER_OF_GENERATIONS,POPULATION_SIZE,NEIGHBORHOOD_SIZE,MUTATION_PROBABILIY,TOURNAMENT_SIZE,mean,stdDev,best,worst";
	private static ParameterSet[] parameterSets;
	
	public static void main(String args[]) {
		int fraction = 8;
		int denominator = 9;
		ParameterSet[] fullSet = ParameterSet.generateParameterSets(Main.SearchMethods.GA, 2, 500);
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

		public ParameterSet(Main.SearchMethods SEARCH_METHOD, int NUMBER_OF_TRIANGLES, int NUMBER_OF_RUNS, int NUMBER_OF_GENERATIONS, int POPULATION_SIZE, int NEIGHBORHOOD_SIZE, double MUTATION_PROBABILIY, int TOURNAMENT_SIZE) {
			this.SEARCH_METHOD = SEARCH_METHOD;
			this.NUMBER_OF_TRIANGLES = NUMBER_OF_TRIANGLES;
			this.NUMBER_OF_RUNS = NUMBER_OF_RUNS;
			this.NUMBER_OF_GENERATIONS = NUMBER_OF_GENERATIONS;
			this.POPULATION_SIZE = POPULATION_SIZE;
			this.NEIGHBORHOOD_SIZE = NEIGHBORHOOD_SIZE;
			this.MUTATION_PROBABILIY = MUTATION_PROBABILIY;
			this.TOURNAMENT_SIZE = TOURNAMENT_SIZE;
		}

		public static ParameterSet[] generateParameterSets(Main.SearchMethods SEARCH_METHOD, int NUMBER_OF_RUNS, int NUMBER_OF_GENERATIONS) {
			int[] populationSizes = {10,25,75};
			int[] neighborhoodSizes = {4};
			double[] mutationProbablilities = {0.01, 0.05, 0.25};
			int[] tournamentSizes = {2,3,6};
			int[] triangleNumbers = {75,100,125};
			
			ArrayList<ParameterSet> parameterSet = new ArrayList<>();
			for(int i = 0; i < populationSizes.length; i++) {
				for(int j = 0; j < neighborhoodSizes.length; j++) {
					for(int k = 0; k < mutationProbablilities.length; k++) {
						for(int l = 0; l < tournamentSizes.length; l++) {
							for(int m = 0; m < triangleNumbers.length; m++) {
								parameterSet.add(new ParameterSet(SEARCH_METHOD, triangleNumbers[m], NUMBER_OF_RUNS, NUMBER_OF_GENERATIONS, populationSizes[i], neighborhoodSizes[j], mutationProbablilities[k], tournamentSizes[l]));
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
