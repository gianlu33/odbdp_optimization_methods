package mainPackage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class GeneticAlgorithm {
	private DataStructure data;
	private int bestObjectiveFunction;
	private float bestFitness;
	private Queue<Solution> bestSolutions;
	private String outputfile;
	private int numPopulation;
	private int pressure;
	private int nElite;
	
	public GeneticAlgorithm(DataStructure data, String outputfile, int numPopulation, int nElite, int pressure) {
		this.data = data;
		this.bestSolutions = new LinkedList<>();
		this.bestObjectiveFunction = Integer.MIN_VALUE;
		this.bestFitness = -Float.MAX_VALUE;
		this.outputfile = outputfile;
		this.numPopulation = numPopulation;
		this.pressure = pressure;
		this.nElite = nElite;
	}
	
	public void start() {
		int generation = 1;
		int pressureThread = pressure;
		ArrayList<Solution> population = new ArrayList<>();
		ArrayList<Solution> children = new ArrayList<>();
		ArrayList<Solution> temp;
		Solution s1, s2, sChild;
		float similarity;
		int mutationRate;
		int size;
		
		//initial population
		generatePopulation(population);

		while(true) {			
			//elitism -> SAVE THE BEST SOLUTIONS FOUND SO FAR
			children.addAll(bestSolutions);
			size = children.size();
			
			//crossover, mutation, local search to populate children
			for(int i=0; i<numPopulation - size; i++) {
				//Tournament selection
				s1 = tournamentSelection(population, pressureThread);
				s2 = tournamentSelection(population, pressureThread);
				
				//check similarity
				similarity = checkSimilarity(s1, s2);
				
				//adaptive mutation rate is function of similarity
				mutationRate =  Math.round(100f - similarity * 98);

				//combination -> CROSSOVER + MUTATION
				boolean[] child = crossover(s1, s2);
				mutation(child, mutationRate);
				sChild = Utils.generateSolutionFromIndexes(data, child);
				
				if(sChild.isFeasible() && sChild.getObjectiveFunction() > bestObjectiveFunction)
					printBestSolution(sChild);
				
				if(sChild.getFitness() > bestFitness)
					insertInBestSolutions(sChild);
				
				//local search
				firstImprovement(sChild);
				
				//add to children
				children.add(sChild);
			}
			
			//new generation
			temp = population;
			population = children;
			children = temp;
			children.clear();
			
			generation++;
			
			//increase pressure over generations
			if(generation % 3 == 0 && pressureThread < 10)
				pressureThread++;
		}	
	}
	
	private void generatePopulation(ArrayList<Solution> population) {
		boolean[] tempIndexes;
		Solution tempSolution;
		
		for(int i=0; i<numPopulation; i++) {

			//generate a random solution
			tempIndexes = Utils.generateRandomIndexes(data);
			
			//add new solution to population
			tempSolution = Utils.generateSolutionFromIndexes(data, tempIndexes);
			
			if(tempSolution.isFeasible() && tempSolution.getObjectiveFunction() > bestObjectiveFunction)
				printBestSolution(tempSolution);
			
			if(tempSolution.getFitness() > bestFitness)
				insertInBestSolutions(tempSolution);
			
			firstImprovement(tempSolution);
			population.add(tempSolution);
		}
		
	}
	
	/*
	 * print best solution to output file.
	 */
	private synchronized void printBestSolution(Solution s) {
			int objectiveFunction = s.getObjectiveFunction();
			
			//check of feasibility not necessary because it is checked before calling this method
			if(objectiveFunction <= bestObjectiveFunction) return;
			
			bestObjectiveFunction = objectiveFunction;
			
			//write to output file
			Utils.writeOutput(s.getMatrix(), outputfile);
	}
	
	/*
	 * insert solution in bestSolution queue
	 */
	private synchronized void insertInBestSolutions(Solution s) {
		float fitness = s.getFitness();
		
		if(fitness <= bestFitness) return;
		
		bestFitness = fitness;
		
		bestSolutions.add(s);
		if(bestSolutions.size() > nElite)
			bestSolutions.remove();
	}
	
	/*
	 * Tournament selection.
	 * Extract random individuals from population, and select the best one
	 * The number of individual to extract is passed as parameter "Pressure"
	 * Population list is ordered, so the best individual is the one with lowest index
	 */
	private Solution tournamentSelection(ArrayList<Solution> population, int pressure) {
		Random rand = new Random();
		Solution temp, best = null;
		float bestFitness = -Float.MAX_VALUE, tempFitness;
		
		for(int i=0; i<pressure; i++) {
			temp = population.get(rand.nextInt(numPopulation));
			tempFitness = temp.getFitness();
			
			if(tempFitness > bestFitness) {
				bestFitness = tempFitness;
				best = temp;
			}
		}
		
		return best;
	}

	private float checkSimilarity(Solution parent1, Solution parent2) {
		boolean[] indexes1 = parent1.getIndexes();
		boolean[] indexes2 = parent2.getIndexes();
		int nIndexes = indexes1.length;
		int equalGenes = 0, totalGenes = 0;
		
		for(int i=0; i<nIndexes; i++) {
			
			//Jaccard similarity
			if(indexes1[i] && indexes2[i])
				equalGenes++;
			
			if(indexes1[i] || indexes2[i])
				totalGenes++;
		}
		
		return totalGenes > 0 ? (float) equalGenes / totalGenes : 1f;
	}


	/*
	 * Uniform Crossover
	 */
	private boolean[] crossover(Solution s1, Solution s2) {
		Random rand = new Random();
		
		boolean[] parent1 = s1.getIndexes();
		boolean[] parent2 = s2.getIndexes();

		int nIndexes = parent1.length;
		boolean[] child1 = new boolean[nIndexes];
		
		for(int i=0; i<nIndexes; i++) {
			
			if(rand.nextBoolean()) {
				//swap
				child1[i] = parent2[i];
			}
			else {
				//no swap
				child1[i] = parent1[i];
			}
		}

		return child1;
	}
	
	/*
	 * Mutation. Flip an index with probability 1 / mutationRate
	 */
	private void mutation(boolean[] child, int mutationRate) {
		Random rand = new Random();
		int nIndexes = child.length;

		for(int i=0; i<nIndexes; i++) {
			
			if(rand.nextInt(mutationRate) == 0) {
				//flip
				child[i] = !child[i];
			}
		}
	
	}

	/*
	 * local search method. First improvement
	 * change the value of one random index (activated -> not activated and viceversa)
	 * if the new fitness is better, then jump to this solution
	 * iterate until i can't improve anymore my fitness
	 */
	private void firstImprovement(Solution child) {
		int nIndexes = data.getnIndexes();
		boolean[] indexes;
		Solution tempSolution, bestSolution = child;
		boolean found;
		float tempFitness;
		float bestFitness = child.getFitness();
		int[] sequence;

		while(true) {
			indexes = bestSolution.getIndexes();
			found = false;
			
			sequence = Utils.generateRandomOrderedSequence(nIndexes);
			
			for(int i=0; i<nIndexes && !found; i++) {
				indexes[sequence[i]] = !indexes[sequence[i]];
				
				tempSolution = Utils.generateSolutionFromIndexes(data, indexes);
				tempFitness = tempSolution.getFitness();
				
				//if this solution has the best fitness, insert in bestSolutions queue
				if(tempFitness > this.bestFitness)
					insertInBestSolutions(tempSolution);
				
				//if this solution is the best feasible one, write to output file
				if(tempSolution.isFeasible() && tempSolution.getObjectiveFunction() > bestObjectiveFunction)
					printBestSolution(tempSolution);
				
				if(tempFitness > bestFitness) {
					bestFitness = tempFitness;
					bestSolution = tempSolution;
					found = true;
				}
				
				indexes[sequence[i]] = !indexes[sequence[i]]; //restore state
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
	
	public int getObjectiveFunction() {
		return this.bestObjectiveFunction;
	}
}
