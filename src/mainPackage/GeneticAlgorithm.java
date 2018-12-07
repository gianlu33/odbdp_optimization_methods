package mainPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class GeneticAlgorithm {
	private DataStructure data;
	private ArrayList<Solution> population;
	int numPopulation;
	private int bestObjectiveFunction;
	
	//types of combination
	public static final int CROSSOVER = 0;
	public static final int MUTATION = 1;
	public static final int INVERSION = 2;
	public static final int MIXED = 3;
	
	public GeneticAlgorithm(DataStructure data, int numPopulation) {
		this.data = data;
		this.population = new ArrayList<>();
		this.numPopulation = numPopulation;
		this.bestObjectiveFunction = -1;
	}
	
	/*
	 * start method
	 * combinationMethod -> one between CROSSOVER, MUTATION, INVERSION or MIXED
	 * nElite -> how many better solution will saved from generation replacement (elitist approach)
	 */
	//TODO verifica correttezza e rivedi
	public void start(int combinationMethod, int nElite) {
		if(combinationMethod > 3) {
			System.out.println("Wrong combination method");
			return;
		}
		
		ArrayList<Solution> eliteSolutions = new ArrayList<>();
		int numIterations = 1;
		
		System.out.println("Generating initial population...");
		generatePopulation();
		System.out.println("Initial population generated.");
		
		//TODO ciclo - CONDIZIONE DI STOP -> TIME LIMIT FORNITO POI DA LINEA DI COMANDO
		while(true) {
			//TODO elitist approach, da implementare
			for(int i=0; i<nElite; i++) {
				//TODO trovo i parent migliori, li salvo nelle liste elite
				//TODO poi alla fine, in evolution li rimetto, al posto dei peggiori (o a random, o che cazz ne so)
			}
			
			//TODO dei metodi di combination, solo l'inversion genera una soluzione feasible da una feasible!
			//TODO gli altri metodi possono generare soluzioni infeasible, vedi se si può migliorare
			
			//System.out.println("Iteration " + numIterations + ": starting combination..");
			combination(combinationMethod);
			//System.out.println("Iteration " + numIterations + ": combination ended.");
		
			//TODO local search sui figli
			//for each child, do a local search in order to improve it
			//System.out.println("Iteration " + numIterations + ": starting local search..");
			//localSearch.start(child);
			//System.out.println("Iteration " + numIterations + ": local search ended.");
			
			//for each child, i compute the objective function and memory used, and then i check if it is
			//a new best solution calling method checkAndSaveBestSolution
			for(Solution s : population) {
				s.indexes = Utils.activateIndexes(data.getnIndexes(), data.getConfigurationIndexes(), s.map);
				s.objectiveFunction = Utils.computeObjectiveFunction(data.getConfigurationQueryGain(), data.getIndexesCost(), s.map, s.indexes);
				s.memoryCost = Utils.computeMemoryUsed(data.getIndexesMemory(), s.indexes);
				checkAndSaveBestSolution(s);
			}
			
			//System.out.println("Iteration " + numIterations + ": best objective function: " + bestObjectiveFunction);
			
			//evolution, new popolation of solutions obtained from old parents and new children
			//TODO da implementare, da reinserire nella popolazione gli eventuali genitori nella lista elite
			//TODO e rimuovere qualche soluzione per mantenere lo stesso numero di soluzioni (numPopulation)
			for(int i=0; i<nElite; i++) {
			}
			
			//System.out.println("Iteration " + numIterations + ": ended.");
			numIterations++;
		}
		
	}
	
	/*
	 * method for the generation of initial population.
	 */
	private void generatePopulation() {
		//TODO se vuoi ottimizza (esempio i data.get(...) fare una volta sola)
		HashMap<Integer, Integer> tempMap;
		int[] tempIndexes;
		int tempObjectiveFunction, tempMemory;
		Solution tempSolution;
		
		for(int i=0; i<numPopulation; i++) {
			
			//generate a good and feasible solution
			//TODO usa un metodo di Utils per generare una soluzione
			//TODO generateRandomSolution ne genera una a caso, fa schifo e può anche essere infeasible
			//TODO una delle generateGoodSolutionX usa un diverso algoritmo greedy (la 1 sembra migliore)
			tempMap = Utils.generateGoodSolution1(data);
			
			//activate indexes of this solution (in order to compute easily the objective function)
			tempIndexes = Utils.activateIndexes(data.getnIndexes(), data.getConfigurationIndexes(), tempMap);
			
			//compute objective function
			tempObjectiveFunction = Utils.computeObjectiveFunction(data.getConfigurationQueryGain(), data.getIndexesCost(), tempMap, tempIndexes);
			
			//compute memory used
			tempMemory = Utils.computeMemoryUsed(data.getIndexesMemory(), tempIndexes);
			
			//add new solution to population
			tempSolution = new Solution(tempMap, tempObjectiveFunction, tempMemory, tempIndexes);
			population.add(tempSolution);
			
			//save best solution and best objective function
			checkAndSaveBestSolution(tempSolution);
			
		}
		
	}
	
	/*
	 * Method that checks the feasibility of a given solution, and if so it checks also if this solution
	 * is the best found so far. If it is, write this solution to output file
	 */
	private void checkAndSaveBestSolution(Solution solution) {
		//TODO verifica correttezza e rivedi
		if(solution.memoryCost <= data.getMaximumMemory() && solution.objectiveFunction > bestObjectiveFunction) {
			bestObjectiveFunction = solution.objectiveFunction;
			
			System.out.println("New best solution! Objective Function: " + bestObjectiveFunction);
			
			//write to output file
			//TODO vedi nome file
			int[][] solutionMatrix = Utils.mapToMatrix(solution.map, data.getnQueries(), data.getnConfigurations());
			Utils.writeOutput(solutionMatrix, "outputs/GeneticAlgorithm/prova.txt");
		}
	}
	
	/*
	 * Combination method.
	 * Four different types:
	 * Crossover -> mix two solutions
	 * Mutation -> change the configuration of one or more queries
	 * Inversion -> invert the configurations assigned to an interval of queries
	 * Mixed -> randomly select one of the three types above
	 */
	//TODO rivedi bene, vedi se esistono altri metodi di combinazione
	private void combination(int combinationMethod) {
		Random rand = new Random();
		int cMethod = combinationMethod;
		Solution stemp, parent1, parent2;

		if(combinationMethod == MIXED) {
			//select a combination method (random or weighted probability)
			cMethod = rand.nextInt(3);
		}
		
		switch(cMethod) {
		case CROSSOVER:
			ArrayList<Solution> newPopulation = new ArrayList<>();
			//TODO rivedere il criterio di mixing, al momento si scelgono due soluzioni random
			if(numPopulation % 2 != 0) {
				stemp = population.remove(rand.nextInt(numPopulation));
				newPopulation.add(stemp);
			}
			for(int i=population.size(); i>0;) {
				parent1 = population.remove(rand.nextInt(i--));
				parent2 = population.remove(rand.nextInt(i--));
				crossover(parent1.map, parent2.map);
				newPopulation.add(parent1);
				newPopulation.add(parent2);
			}
			
			population = newPopulation;				
			break;
		case MUTATION:
			for(Solution s : population)
				mutation(s.map, 1);
			break;
		case INVERSION:
			for(Solution s : population)
				inversion(s.map);
			break;						
		}
	}
	
	/*
	 * Crossover
	 */
	private void crossover(HashMap<Integer, Integer> parent1, HashMap<Integer, Integer> parent2) {
		Random rand = new Random();
		int nQueries = data.getnQueries();
		
		//crossoverLine1 always bigger than crossoverLine2
		int crossoverLine1, crossoverLine2, conf1, conf2;
		do {
			crossoverLine1 = rand.nextInt(nQueries);
			crossoverLine2 = rand.nextInt(1 + crossoverLine1);
		} while(crossoverLine1 == crossoverLine2);
		
		for(int i=crossoverLine2; i<=crossoverLine1; i++) {
			conf1 = parent1.containsKey(i) ? parent1.get(i) : -1;
			conf2 = parent2.containsKey(i) ? parent2.get(i) : -1;
			
			//change in parent1
			if(conf2 == -1) 
				parent1.remove(i);
			else
				parent1.put(i, conf2);
			
			//change in parent2
			if(conf1 == -1)
				parent2.remove(i);
			else
				parent2.put(i, conf1);
		}
		
	}
	
	/*
	 * Mutation -> change a configuration for a certain query (randomly selected) for iterations times
	 */
	private void mutation(HashMap<Integer, Integer> parent, int iterations) {
		Random rand = new Random();
		int nQueries = data.getnQueries();
		int nConfigurations = data.getnConfigurations();
		int query, configuration;
		
		for(int i=0; i<iterations; i++) {
			query = rand.nextInt(nQueries);
			configuration = rand.nextInt(nConfigurations);
			
			//System.out.println("Mutation: Query " + query + " Configuration " + configuration);
			parent.put(query, configuration);
		}
	}
	
	/*
	 * inversion -> given a feasible solution, it certainly generates a feasible solution
	 * invert the configurations of an interval of queries
	 * e.g [1,2,3] -> [3,2,1]
	 */
	private void inversion(HashMap<Integer, Integer> parent) {
		Random rand = new Random();
		int nQueries = data.getnQueries();
		int crossoverLine1, crossoverLine2, conf1, conf2;
		do {
			crossoverLine1 = rand.nextInt(nQueries);
			crossoverLine2 = rand.nextInt(1 + crossoverLine1);
		} while(crossoverLine1 == crossoverLine2);
		
		for(int i=crossoverLine2, j=crossoverLine1; i<j; i++, j--) {
			conf1 = parent.containsKey(i) ? parent.get(i) : -1;
			conf2 = parent.containsKey(j) ? parent.get(j) : -1; 
			
			if(conf2 == -1)
				parent.remove(i);
			else
				parent.put(i, conf2);
			
			if(conf1 == -1)
				parent.remove(j);
			else
				parent.put(j, conf1);
			
		}
	}
	
	private class Solution {
		private HashMap<Integer,Integer> map;
		private int objectiveFunction;
		private int memoryCost;
		private int[] indexes;
		
		public Solution(HashMap<Integer, Integer> map, int objectiveFunction, int memoryCost, int[] indexes) {
			super();
			this.map = map;
			this.objectiveFunction = objectiveFunction;
			this.memoryCost = memoryCost;
			this.indexes = indexes;
		}
	}
}
