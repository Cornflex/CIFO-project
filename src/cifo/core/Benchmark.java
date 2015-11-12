package cifo.core;

import java.util.ArrayList;

public class Benchmark {
	private static String results = "SEARCH_METHOD,NUMBER_OF_TRIANGLES,NUMBER_OF_RUNS,NUMBER_OF_GENERATIONS,POPULATION_SIZE,NEIGHBORHOOD_SIZE,MUTATION_PROBABILIY,TOURNAMENT_SIZE,mean,stdDev,best,worst";
	private static ParameterSet[] parameterSets;
	
	public static void main(String args[]) {
		int fraction = 1;
		int denominator = 10;
		ParameterSet[] fullSet = ParameterSet.generateParameterSets(Main.SearchMethods.GA, 100, 1, 2);
		parameterSets = ParameterSet.getPortion(fraction, denominator, fullSet);
		System.out.println("Benchmark started. Testing " + parameterSets.length +" out of " + fullSet.length + " parameter combinations.");
		Main.runInBenchmarkMode(1, parameterSets[0]);
	}
	
	public static void continueAt(int benchmarkPosition, String results) {
		System.out.println("Combination "+ benchmarkPosition + " tested.");
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

		public static ParameterSet[] generateParameterSets(Main.SearchMethods SEARCH_METHOD, int NUMBER_OF_TRIANGLES, int NUMBER_OF_RUNS, int NUMBER_OF_GENERATIONS) {
			int[] ps = {5,10,15};
			int[] ns = {4};
			double[] mp = {0.01, 0.05, 0.25};
			int[] ts = {2,4,8};
			
			ArrayList<ParameterSet> parameterSet = new ArrayList<>();
			for(int i = 0; i < ps.length; i++) {
				for(int j = 0; j < ns.length; j++) {
					for(int k = 0; k < mp.length; k++) {
						for(int l = 0; l < ts.length; l++) {
							parameterSet.add(new ParameterSet(SEARCH_METHOD, NUMBER_OF_TRIANGLES, NUMBER_OF_RUNS, NUMBER_OF_GENERATIONS, ps[i], ns[j], mp[k], ts[l]));
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
