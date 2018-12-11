package mainPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GeneticAlgorithm2 {
	private DataStructure data;
	private ArrayList<Solution> population;
	private ArrayList<Solution> children;
	int numPopulation;
	private int bestObjectiveFunction;
	private ArrayList<GAThread> listThreads;
	float mutationRate; //serve per potenziare la mutation se non trova soluzioni per molte generazioni
	long timeStart;
	
	public GeneticAlgorithm2(DataStructure data, int numPopulation) {
		this.data = data;
		this.population = new ArrayList<>();
		this.children = new ArrayList<>();
		this.numPopulation = numPopulation;
		this.bestObjectiveFunction = Integer.MIN_VALUE;
		listThreads = new ArrayList<>();
	}
	
	//TODO rivedi bene sti metodi che magari hai scritto cagate
	
	/*
	 * start method
	 * nElite -> how many best solution are saved for selection (elitist approach)
	 */
	public void start(int nElite) {
		GAThread thread;
		int generation = 1;
		mutationRate = 1;
		timeStart = System.currentTimeMillis();

		generatePopulation();
		Collections.sort(population, Solution::compare);		
		
		System.out.println("Initial population generated.");
		
		//TODO ciclo - CONDIZIONE DI STOP -> TIME LIMIT...
		while(true) {
			
			/*
			if(generation % 10 == 0) {
				System.out.println("Parents before combination:");
				for(Solution s : population)
					System.out.println(s);
			}
			*/
			
			combination();
			
			//local search and check
			for(Solution s : children) {
				thread = new GAThread(s);
				listThreads.add(thread);
				thread.start();
			}
			
			//wait for threads to finish
			for(GAThread t : listThreads){
				try {
					t.join();
				}catch(Exception e) {
					e.printStackTrace();
					return;
				}
			}
			listThreads.clear();

			/* la faccio nella local search

			for(Solution s : children)
				checkAndSaveBestSolution(s);
			*/
			
			/*
			if(generation % 100 == 0) {
				System.out.println("Children before selection:");
				for(Solution s : children)
					System.out.println(s);
			}
			*/
			
			//selection
			selection(nElite);
			
			System.out.println("Generation " + generation + ": best objective function: " + bestObjectiveFunction);
			generation++;
			mutationRate += 0.1;
		}
		
	}	
	

	/*
	 * method for the generation of initial population.
	 */
	private void generatePopulation() {
		//TODO verifica correttezza
		boolean[] tempIndexes;
		Solution tempSolution;
		int actualSize = population.size();
		
		for(int i=0; i<numPopulation - actualSize; i++) {

			//generate a good and feasible solution
			//tempIndexes = Utils.generateRandomIndexes(data, 0.8);
			tempIndexes = Utils.generateRandomIndexes2(data);
			//tempIndexes = Utils.generateRandomIndexes3(data);
			
			//add new solution to population
			tempSolution = Utils.generateSolutionFromIndexes(data, tempIndexes);
			population.add(tempSolution);
			
			//save best solution and best objective function
			checkAndSaveBestSolution(tempSolution);
			
		}
		//Collections.sort(population, Solution::compare);
	}
	
	/*
	 * Method that checks if a solution is the best found so far.
	 * If it is, write this solution to output file
	 */
	private synchronized void checkAndSaveBestSolution(Solution s) {
		//TODO verifica correttezza e rivedi
		int objectiveFunction = s.getObjectiveFunction();
		int[][] matrix = s.getMatrix();
		long time = System.currentTimeMillis();
		
		if(!s.isFeasible()) return;
		
		if(objectiveFunction > bestObjectiveFunction) {
			bestObjectiveFunction = objectiveFunction;
			
			System.out.println("New best solution! Time: " + (time - timeStart)/1000.0 + " Objective Function: " + bestObjectiveFunction);
			mutationRate = 1;
			
			//write to output file
			//TODO vedi nome file
			Utils.writeOutput(matrix, "outputs/GeneticAlgorithm2/prova.txt");
		}
	}
	
	private void combination() {
		Solution s, s1, s2;
		Random rand = new Random();
		ArrayList<boolean[]> list = new ArrayList<>();
		
		//TODO combination + local search + check best solution
		//TODO rivedere metodo di combinazione
		
		for(int i=0; i<numPopulation/2; i++) {
			//take the first parent in order, and the second randomly selected
			s1 = population.get(i);
			s2 = population.get(rand.nextInt(numPopulation));
			
			crossover(s1.getIndexes(), s2.getIndexes(), list);
		}
		
		for(boolean[] child : list) {
			mutation(child);
			s = Utils.generateSolutionFromIndexes(data, child);
			children.add(s);
		}
	}
	
	
	/*
	private boolean checkSimilarity(Solution parent1, Solution parent2) {
		boolean[] indexes1 = parent1.getIndexes();
		boolean[] indexes2 = parent2.getIndexes();
		int nIndexes = indexes1.length;
		int equalGenes = 0;
		
		for(int i=0; i<nIndexes; i++) {
			if(indexes1[i] == indexes2[i])
				equalGenes++;
		}
		
		if(equalGenes > equalGenes * 0.8)
			return true;
		
		return false;
	}
	*/
	
	/*
	 * Uniform Crossover
	 */
	private void crossover(boolean[] parent1, boolean[] parent2, ArrayList<boolean[]> list) {
		Random rand = new Random();
		int nIndexes = parent1.length;
		
		boolean[] child1 = new boolean[nIndexes];
		boolean[] child2 = new boolean[nIndexes];
		
		for(int i=0; i<nIndexes; i++) {
			
			if(rand.nextBoolean()) {
				//swap
				child1[i] = parent2[i];
				child2[i] = parent1[i];
			}
			else {
				//no swap
				child1[i] = parent1[i];
				child2[i] = parent2[i];
			}
		}

		list.add(child1);
		list.add(child2);
	}
	
	/*
	 * Mutation. Flip a index with probability 1 / nIndexes
	 */
	private void mutation(boolean[] child) {
		Random rand = new Random();
		int nIndexes = child.length;
		
		int range = (int) (Math.round((nIndexes-1) / mutationRate) + 1);
		
		for(int i=0; i<nIndexes; i++) {
			
			if(rand.nextInt(range) == 0) {
				//flip
				child[i] = !child[i];
			}
		}
	
	}
	
	/*
	 * local search method. Best improvement
	 * change the value of one index (activated -> not activated and viceversa)
	 * until convergence is reached
	 */
	//TODO rivedi
	private void bestImprovement(Solution child) {
		int nIndexes = data.getnIndexes();
		int bestObjectiveFunction = child.getObjectiveFunction();
		int tempObjectiveFunction;
		boolean[] indexes;
		Solution tempSolution, bestSolution = null;
		boolean found;
		//float tempFitness;
		//float bestFitness = child.getFitness();

		while(true) {
			indexes = child.getIndexes();
			found = false;
			
			for(int i=0; i<nIndexes; i++) {
				indexes[i] = !indexes[i];
				
				tempSolution = Utils.generateSolutionFromIndexes(data, indexes);
				tempObjectiveFunction = tempSolution.getObjectiveFunction();
				//tempFitness = tempSolution.getFitness();
				
				checkAndSaveBestSolution(tempSolution);
				
				//if(tempSolution.isFeasible() && tempObjectiveFunction > bestObjectiveFunction) {
				if(tempObjectiveFunction > bestObjectiveFunction) {
				//if(tempFitness > bestFitness) {
					bestObjectiveFunction = tempObjectiveFunction;
					//bestFitness = tempFitness;
					bestSolution = tempSolution;
					found = true;
				}
				
				indexes[i] = !indexes[i]; //restore state
			}
			
			if(!found) return; //actual solution is already the best
			
			child.setObjectiveFunction(bestSolution.getObjectiveFunction());
			child.setMemory(bestSolution.getMemory());
			child.setIndexes(bestSolution.getIndexes());
			child.setMatrix(bestSolution.getMatrix());
			child.setFeasible(bestSolution.isFeasible());
			child.setFitness(bestSolution.getFitness());
		}
	}
	
	/*
	 * Selection method for new generation
	 * remove equals
	 * if population size is less than numPopulation -> generate new random solutions
	 * else remove randomly solutions (saving the first nElite ones)
	 */
	private void selection(int nElite) {
		int ind;
		Random rand = new Random();
		
		population.addAll(children);
		children.clear();
		
		//System.out.println(population);
		Collections.sort(population, Solution::compare);
		removeEquals();
		
		if(population.size() < numPopulation) {
			generatePopulation();
		}
		else {
			while(population.size() > numPopulation) {
				ind = rand.nextInt(population.size()-nElite) + nElite;
				population.remove(ind);
			}
		}
	}
	
	/*
	 * method that removes from population solutions with equal objective function
	 * population list must be ordered!!
	 */
	private void removeEquals() {
		int size = population.size();
		Solution s, sOld;
		
		sOld = population.get(0);
		for(int i=1; i<size;) {
			s = population.get(i);
			
			if(s.getObjectiveFunction() == sOld.getObjectiveFunction()) {
				//remove s
				population.remove(i);
				
				//update indexes for loop
				size--;
			}
			else {
				//ok
				sOld = s;
				i++;
			}
			
		}
	}
	
	private class GAThread extends Thread{
		Solution solution;

		public GAThread(Solution s) {
			this.solution = s;
		}
		
		@Override
		public void run() {
			super.run();
			bestImprovement(solution);
		}
		
	}

}
