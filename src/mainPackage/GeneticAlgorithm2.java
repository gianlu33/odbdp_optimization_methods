package mainPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GeneticAlgorithm2 {
	private DataStructure data;
	private ArrayList<Solution> population;
	int numPopulation;
	private int bestObjectiveFunction;
	
	//methods for combination
	public static final int CROSSOVER = 0;
	public static final int MUTATION = 1;
	public static final int INVERSION = 2;
	public static final int MIXED = 3;
	
	public GeneticAlgorithm2(DataStructure data, int numPopulation) {
		this.data = data;
		this.population = new ArrayList<>();
		this.numPopulation = numPopulation;
		this.bestObjectiveFunction = -1;
	}
	
	//TODO rivedi bene sti metodi che magari hai scritto cagate
	
	/*
	 * start method
	 * combinationMethod -> one between CROSSOVER, MUTATION, INVERSION or MIXED
	 * nElite -> how many better solution will saved from generation replacement (elitist approach)
	 */
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
		//LocalSearch localSearch = new LocalSearch(data);
		
		Collections.sort(population, (a,b) -> b.objectiveFunction-a.objectiveFunction);
		
		//TODO ciclo - CONDIZIONE DI STOP -> TIME LIMIT...
		while(true) {
			//sort solutions in decreasing order of objective functions
			for(int i=0; i<nElite; i++) {
				eliteSolutions.add(population.get(i).clone());
			}
			
			//System.out.println("Iteration " + numIterations + ": starting combination..");
			combination(combinationMethod);
			//System.out.println("Iteration " + numIterations + ": combination ended.");
			
			//TODO in caso di best solution ->> stampo su file
			//per tutta la nuova popolazione, ricalcolo la objective function e la memoria, poi chiamo il checkandsavebestsolution
			for(Solution s : population) {
				bestImprovement(s); //local search.
				checkAndSaveBestSolution(s.matrix, s.objectiveFunction);
			}
			
			//System.out.println("Iteration " + numIterations + ": best objective function: " + bestObjectiveFunction);
			
			//TODO evolution
			//rimetto eventuali genitori rimossi
			population.addAll(eliteSolutions);
			eliteSolutions.clear();
			Collections.sort(population, (a,b) -> b.objectiveFunction-a.objectiveFunction);
			for(int i=0; i<nElite; i++)
				population.remove(numPopulation);
			
			//System.out.println("Iteration " + numIterations + ": ended.");
			numIterations++;
		}
		
	}

	/*
	 * method for the generation of initial population.
	 */
	private void generatePopulation() {
		//TODO verifica correttezza
		//TODO ottimizza (esempio i data.get(...) fare una volta sola)
		boolean[] tempIndexes;
		int tempObjectiveFunction;
		Solution tempSolution;
		int[][] tempMatrix = new int[data.getnConfigurations()][data.getnQueries()];
		
		for(int i=0; i<numPopulation; i++) {
			//generate a random solution
			
			//generate a good and feasible solution
			tempIndexes = Utils.generateRandomIndexes(data, 0.8);
			
			//compute objective function and solution matrix
			tempObjectiveFunction = Utils.generateSolutionFromIndexes(data, tempIndexes, tempMatrix);
			
			//add new solution to population
			tempSolution = new Solution(tempIndexes, tempObjectiveFunction, tempMatrix);
			population.add(tempSolution);
			
			//save best solution and best objective function
			checkAndSaveBestSolution(tempMatrix, tempObjectiveFunction);
			
		}
		
	}
	
	/*
	 * Method that checks if a solution is the best found so far.
	 * If it is, write this solution to output file
	 */
	private void checkAndSaveBestSolution(int[][] matrix, int objectiveFunction) {
		//TODO verifica correttezza e rivedi
		
		if(objectiveFunction > bestObjectiveFunction) {
			bestObjectiveFunction = objectiveFunction;
			
			System.out.println("New best solution! Objective Function: " + bestObjectiveFunction);
			
			//write to output file
			//TODO vedi nome file
			Utils.writeOutput(matrix, "outputs/GeneticAlgorithm2/prova.txt");
		}
	}
	
	/*
	 * Combination method.
	 * Four different types:
	 * Crossover -> mix two solutions
	 * Mutation -> change one index (activated -> not activated and viceversa)
	 * Inversion -> invert the values of an interval of indexes
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
			//TODO rivederee!!!!! ho scelto di randomizzare ma RIVEDI
			if(numPopulation % 2 != 0) {
				stemp = population.remove(rand.nextInt(numPopulation));
				newPopulation.add(stemp);
			}
			for(int i=population.size(); i>0;) {
				parent1 = population.remove(rand.nextInt(i--));
				parent2 = population.remove(rand.nextInt(i--));
				crossover(parent1.indexes, parent2.indexes);
				newPopulation.add(parent1);
				newPopulation.add(parent2);
			}
			
			population = newPopulation;				
			break;
		case MUTATION:
			for(Solution s : population)
				mutation(s.indexes, 1);
			break;
		case INVERSION:
			for(Solution s : population)
				inversion(s.indexes);
			break;						
		}
	}
	
	/*
	 * Crossover
	 */
	private void crossover(boolean[] parent1, boolean[] parent2) {
		Random rand = new Random();
		boolean temp;
		int nIndexes = parent1.length;
		//crossoverLine1 always bigger than crossoverLine2
		int crossoverLine1, crossoverLine2;
		do {
			crossoverLine1 = rand.nextInt(nIndexes);
			crossoverLine2 = rand.nextInt(1 + crossoverLine1);
		} while(crossoverLine1 == crossoverLine2);
		
		for(int i=crossoverLine2; i<=crossoverLine1; i++) {
			temp = parent1[i];
			parent1[i] = parent2[i];
			parent2[i] = temp;
		}
		
	}
	
	/*
	 * Mutation
	 */
	private void mutation(boolean[] parent, int iterations) {
		Random rand = new Random();
		int index, nIndexes = parent.length;
		
		for(int i=0; i<iterations; i++) {
			index = rand.nextInt(nIndexes);
			
			//System.out.println("Mutation: Index " + index);
			parent[index] = !parent[index];
		}
		
	}
	
	/*
	 * inversion
	 */
	private void inversion(boolean[] parent) {
		Random rand = new Random();
		int nIndexes = parent.length;		
		int crossoverLine1, crossoverLine2;
		boolean temp;
		
		do {
			crossoverLine1 = rand.nextInt(nIndexes);
			crossoverLine2 = rand.nextInt(1 + crossoverLine1);
		} while(crossoverLine1 == crossoverLine2);
		
		for(int i=crossoverLine2, j=crossoverLine1; i<j; i++, j--) {
			temp = parent[i];
			parent[i] = parent[j];
			parent[j] = temp;
		}
		
	}
	
	/*
	 * local search method. Best improvement
	 * change the value of one index (activated -> not activated and viceversa)
	 * The complexity is O(|I|)
	 */
	//TODO rivedi
	private void bestImprovement(Solution child) {
		int nIndexes = data.getnIndexes();
		int bestObjectiveFunction = child.objectiveFunction;
		int bestIndex = -1, tempObjectiveFunction;
		int[][] tempMatrix = new int[data.getnConfigurations()][data.getnQueries()];
		int[][] bestMatrix = null;
		boolean[] indexes = child.indexes;
		
		for(int i=0; i<nIndexes; i++) {
			indexes[i] = !indexes[i];
			tempObjectiveFunction = Utils.generateSolutionFromIndexes(data, indexes, tempMatrix);
			
			if(tempObjectiveFunction > bestObjectiveFunction) {
				bestObjectiveFunction = tempObjectiveFunction;
				bestMatrix = tempMatrix.clone();
				bestIndex = i;
			}
			
			indexes[i] = !indexes[i]; //restore state
		}
		
		if(bestIndex == -1) return; //actual solution is already the best
		
		child.objectiveFunction = bestObjectiveFunction;
		indexes[bestIndex] = !indexes[bestIndex]; //i go to te best neighbor
		child.matrix = bestMatrix;
	}
	
	private class Solution {
		private boolean[] indexes;
		private int objectiveFunction;
		private int[][] matrix;
		
		public Solution(boolean[] indexes, int objectiveFunction, int[][] matrix) {
			this.indexes = indexes;
			this.objectiveFunction = objectiveFunction;
			this.matrix = matrix;
		}
		
		public Solution() {}
		
		public Solution clone() {
			Solution s = new Solution();
			s.indexes = indexes.clone();
			s.objectiveFunction = objectiveFunction;
			s.matrix = matrix.clone();
			
			return s;
		}
		
	}
}
