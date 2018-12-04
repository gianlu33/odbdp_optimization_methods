package mainPackage;

import java.util.ArrayList;
import java.util.HashMap;

public class GeneticAlgorithm {
	//TODO VEDI se è meglio avere arraylist o vettori semplici
	private DataStructure data;
	private ArrayList<HashMap<Integer,Integer>> population;
	private ArrayList<Integer> objectiveFunctions;
	private ArrayList<Integer> memoryCosts;
	private ArrayList<Boolean> feasibilities; //TODO vedi se rimuovere perchè abbastanza inutile
	private ArrayList<int[]> indexes;
	int numPopulation;
	
	//Best solution
	private HashMap<Integer, Integer> bestSolution;
	private int bestObjectiveFunction;
	
	public GeneticAlgorithm(DataStructure data, int numPopulation) {
		this.data = data;
		this.population = new ArrayList<>();
		this.objectiveFunctions = new ArrayList<>();
		this.indexes = new ArrayList<>();
		this.memoryCosts = new ArrayList<>();
		this.feasibilities = new ArrayList<>();
		this.numPopulation = numPopulation;
		this.bestObjectiveFunction = -1;
	}
	
	public void start() {
		//TODO verifica correttezza e rivedi
		generatePopulation();
		//LocalSearch localSearch = new LocalSearch(data);
		
		//TODO ciclo - CONDIZIONE DI STOP -> TIME LIMIT...
		while(true) {
			//TODO combination
		
			//TODO local search sui figli
			//for each child, do a local search in order to improve it
			//localSearch.start(child);
			
			//TODO in caso di best solution ->> stampo su file
			//per tutta la nuova popolazione, chiamo il checkandsavebestsolution
			
			//TODO evolution
			//gen replacement o elistic approach?
		}
		
	}

	private void generatePopulation() {
		//TODO verifica correttezza
		//TODO ottimizza (esempio i data.get(...) fare una volta sola)
		HashMap<Integer, Integer> tempSolution;
		int[] tempIndexes;
		int tempObjectiveFunction, tempMemory, maximumMemory;
		boolean tempFeasibility;
		
		maximumMemory = data.getMaximumMemory();
		
		for(int i=0; i<numPopulation; i++) {
			//generate a random solution and add to list
			//WARNING!!! AT THE MOMENT THE SOLUTION GENERATED COULD BE INFEASIBLE FOR MEMORY CONSTRAINT!!!
			tempSolution = Utils.generateRandomSolution(data.getnQueries(), data.getnConfigurations());
			population.add(tempSolution);
			
			//activate indexes of this solution (in order to compute easily the objective function) and add to list
			tempIndexes = Utils.activateIndexes(data.getnIndexes(), data.getConfigurationIndexes(), tempSolution);
			indexes.add(tempIndexes);
			
			//compute objective function and add to list
			tempObjectiveFunction = Utils.computeObjectiveFunction(data.getConfigurationQueryGain(), data.getIndexesCost(), tempSolution, tempIndexes);
			objectiveFunctions.add(tempObjectiveFunction);
			
			//compute memory used and add to list
			tempMemory = Utils.computeMemoryUsed(data.getIndexesMemory(), tempIndexes);
			memoryCosts.add(tempMemory);
			
			//check feasibility in terms of memory of each solution and add to list
			tempFeasibility = tempMemory <= maximumMemory ? true : false;
			feasibilities.add(tempFeasibility);
			
			//save best solution and best objective function
			checkAndSaveBestSolution(tempSolution, tempObjectiveFunction, tempFeasibility);
			
		}
		
	}
	
	private void checkAndSaveBestSolution(HashMap<Integer, Integer> solution, int objectiveFunction, boolean feasibility) {
		//TODO verifica correttezza e rivedi
		if(feasibility && objectiveFunction > bestObjectiveFunction) {
			bestObjectiveFunction = objectiveFunction;
			bestSolution = solution;
			
			//write to output file
			//TODO vedi nome file
			int[][] solutionMatrix = Utils.mapToMatrix(solution, data.getnQueries(), data.getnConfigurations());
			Utils.writeOutput(solutionMatrix, "outputs/GeneticAlgorithm/prova.txt");
		}
	}
	
	private void combination() {
		//TODO implementa
	}
	
	private void crossover(HashMap<Integer, Integer> parent1, HashMap<Integer, Integer> parent2) {
		
	}
	
	private void mutation(HashMap<Integer, Integer> parent) {
		
	}
	
	private void inversion(HashMap<Integer, Integer> parent) {
		
	}
	
	private void evolution() {
		
	}
}
