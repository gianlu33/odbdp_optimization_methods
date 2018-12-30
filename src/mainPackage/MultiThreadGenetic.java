package mainPackage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

public class MultiThreadGenetic {
	private DataStructure data;
	private ArrayList<ArrayList<Solution>> populationThreads;
	private ArrayList<ArrayList<Solution>> childrenThreads;
	private int bestObjectiveFunction;
	private float bestFitness;
	private Queue<Solution> bestSolutions;
	private int timeMillis;
	private long timeStart;
	private String outputfile;
	private int numPopulation;
	private int pressure;
	private int nElite;
	
	public MultiThreadGenetic(DataStructure data, int numThreads, String outputfile, int time,  int numPopulation, int nElite, int pressure) {
		this.data = data;
		this.populationThreads = new ArrayList<>();
		this.childrenThreads = new ArrayList<>();
		this.bestSolutions = new LinkedList<>();

		for(int i=0; i<numThreads; i++) {
			populationThreads.add(new ArrayList<>());
			childrenThreads.add(new ArrayList<>());
		}
		
		this.bestObjectiveFunction = Integer.MIN_VALUE;
		this.bestFitness = Float.MIN_VALUE;
		this.outputfile = outputfile;
		this.numPopulation = numPopulation;
		this.pressure = pressure;
		this.nElite = nElite;
		
		this.timeMillis = time * 1000;
		this.timeStart = System.currentTimeMillis();
	}
	
	public void start(int numThread) {
		int generation = 1;
		int pressureThread = pressure;
		
		ArrayList<Solution> population = populationThreads.get(numThread);
		ArrayList<Solution> children = childrenThreads.get(numThread);
		ArrayList<Solution> temp;
		Solution s1, s2, sChild;
		float similarity;
		int mutationRate;
		
		//initial population
		generatePopulation(population, numThread, numPopulation, nElite);

		while(System.currentTimeMillis() - timeStart < timeMillis) {
			Collections.sort(population, Solution::compare);
			
			//elitism -> SAVE THE BEST SOLUTIONS FOUND SO FAR
			children.addAll(bestSolutions);
			
			//crossover, mutation, ls
			for(int i=0; i<numPopulation-nElite; i++) {
				//selection -> TOURNAMENT SELECTION FOR COMBINATION
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
				checkAndSaveBestSolution(sChild, numThread, nElite);
				
				//local search
				firstImprovement(sChild, numThread, nElite);
				
				//add to children
				children.add(sChild);
			}
			
			//new generation
			temp = population;
			population = children;
			children = temp;
			children.clear();
			
			generation++;
			
			if(generation % 2 == 0)
				pressureThread = pressureThread >= numPopulation / 5 ? numPopulation / 5 : pressureThread + 1;
		}	
	}
	
	private void generatePopulation(ArrayList<Solution> population, int numThread, int numPopulation, int nElite) {
		boolean[] tempIndexes;
		Solution tempSolution;
		
		for(int i=0; i<numPopulation; i++) {

			//generate a random solution
			tempIndexes = Utils.generateRandomIndexes(data);
			
			//add new solution to population
			tempSolution = Utils.generateSolutionFromIndexes(data, tempIndexes);
			firstImprovement(tempSolution, numThread, nElite);
			population.add(tempSolution);
		}
		
	}
	
	/*
	 * Method that checks if a solution is the best found so far.
	 * If it is, write this solution to output file
	 */
	private synchronized void checkAndSaveBestSolution(Solution s, int numThread, int nElite) {
		int objectiveFunction = s.getObjectiveFunction();
		float fitness = s.getFitness();
		int[][] matrix = s.getMatrix();
		
		//add solution to bestSolutions
		if(fitness > bestFitness) {
			bestFitness = fitness;
			
			bestSolutions.add(s);
			if(bestSolutions.size() > nElite)
				bestSolutions.remove();
		}
	
		if(!s.isFeasible()) return;
		
		if(objectiveFunction > bestObjectiveFunction) {
			bestObjectiveFunction = objectiveFunction;
			
			//write to output file
			Utils.writeOutput(matrix, outputfile);
		}
	}
	
	/*
	 * Tournament selection.
	 * Extract random individuals from population, and select the best one
	 * The number of individual to extract is passed as parameter "Pressure"
	 * Population list is ordered, so the best individual is the one with lowest index
	 */
	private Solution tournamentSelection(ArrayList<Solution> population, int pressure) {
		int best = -1, temp;
		Random rand = new Random();
		
		for(int i=0; i<pressure; i++) {
			temp = rand.nextInt(population.size());
			
			if(best == -1 || temp < best)
				best = temp;
		}
		
		return population.get(best);
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
		
		return (float) equalGenes / totalGenes;
	}


	/*
	 * Uniform Crossover
	 * Try to have only a child.
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
	private void firstImprovement(Solution child, int numThread, int nElite) {
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
				
				checkAndSaveBestSolution(tempSolution, numThread, nElite);
				
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
