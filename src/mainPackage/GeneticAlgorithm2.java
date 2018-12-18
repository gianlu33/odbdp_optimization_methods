package mainPackage;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class GeneticAlgorithm2 {
	private DataStructure data;
	private ArrayList<Solution> population;
	private ArrayList<Solution> children;
	private int numPopulation;
	private int bestObjectiveFunction;
	private Solution bestSolution;
	private ArrayList<GAThread> listThreads;
	private long timeStart;

	public GeneticAlgorithm2(DataStructure data, int numPopulation) {
		this.data = data;
		this.population = new ArrayList<>();
		this.children = new ArrayList<>();
		this.numPopulation = numPopulation;
		this.bestObjectiveFunction = Integer.MIN_VALUE;
		listThreads = new ArrayList<>();
	}
	
	public void start(int nElite, int pressure) {
		timeStart = System.currentTimeMillis();
		int generation = 1;
		ArrayList<Solution> temp;
		GAThread thread;
		
		//generation of initial population
		generatePopulation();
		
		while(true) {
			//sort
			Collections.sort(population, Solution::compare);
			//removeEquals();
			
			//elitism -> SAVE THE BEST SOLUTIONS FOUND SO FAR
			for(int i=0; i<nElite; i++){
				Solution s = population.get(i);
				//localSearch(s);
				children.add(s);
			}
			
			//add best solution
			localSearch(bestSolution);
			children.add(bestSolution);
			
			//fill population with random elements
			//generatePopulation();
			
			/*
			if(generation % 10 == 0)
				printSimilarityMatrix();
				*/
			
			//create a thread for each new element i have to create
			for(int i=0; i<numPopulation-nElite-1; i++) {
				thread = new GAThread(pressure, generation);
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
			
			//new generation
			temp = population;
			population = children;
			children = temp;
			children.clear();
			
			System.out.println("Generation " + generation + ": best objective function: " + bestObjectiveFunction);
			generation++;
			//TODO verifica pressure
			if(generation % 10 == 0)
				pressure++;
		}
		
		//population.clear();
	}
	
	/*
	 * method for the generation of initial population.
	 */
	
	private void generatePopulation() {
		boolean[] tempIndexes;
		Solution tempSolution;
		int actualSize = population.size();
		
		for(int i=0; i<numPopulation - actualSize; i++) {

			//generate a random solution
			//tempIndexes = Utils.generateRandomIndexes(data, 0.8);
			tempIndexes = Utils.generateRandomIndexes2(data);
			//tempIndexes = Utils.generateRandomIndexes3(data);
			
			//add new solution to population
			tempSolution = Utils.generateSolutionFromIndexes(data, tempIndexes);
			firstImprovement(tempSolution);
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
		int objectiveFunction = s.getObjectiveFunction();
		int[][] matrix = s.getMatrix();
		long time = System.currentTimeMillis();
		
		if(!s.isFeasible()) return;
		
		if(objectiveFunction > bestObjectiveFunction) {
			bestObjectiveFunction = objectiveFunction;
			bestSolution = s;
			
			System.out.println("New best solution! Time: " + (time - timeStart)/1000.0 + " Objective Function: " + bestObjectiveFunction);
			
			//write to output file
			//TODO vedi nome file
			Utils.writeOutput(matrix, "outputs/GeneticAlgorithm2/prova.txt");
		}
	}
	
	/*
	 * Tournament selection.
	 * Extract random individuals from population, and select the best one
	 * The number of individual to extract is passed as parameter "Pressure"
	 * Population list is ordered, so the best individual is the one with lowest index
	 */
	private Solution tournamentSelection(int pressure) {
		int best = -1, temp;
		Random rand = new Random();
		
		for(int i=0; i<pressure; i++) {
			temp = rand.nextInt(numPopulation);
			
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

		return rand.nextInt(2) == 0 ? child1 : child2;
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
	 * Mutation. Swap two random indexes (one 0 and one 1) numSwaps time
	 */
	private void mutationSwap(boolean[] child, int numSwaps) {
		int nIndexes = child.length, ind0, ind1;
		int[] sequence;
		
		for(int i=0; i<numSwaps; i++) {
			
			sequence = Utils.generateRandomOrderedSequence(nIndexes);
			ind0 = ind1 = -1;
			
			for(int j=0; j<nIndexes; j++) {
				
				//find indexes
				if(child[sequence[j]] && ind1 == -1)
					ind1 = sequence[j];
				
				if(!child[sequence[j]] && ind0 == -1)
					ind0 = sequence[j];
				
				//if i have found the indexes, stop
				if(ind0 != -1 && ind1 != -1)
					break;
			}
			
			//if i have not found an index, stop
			if(ind0 == -1 || ind1 == -1)
				break;
			
			//swap
			child[ind0] = true;
			child[ind1] = false;
		}
	
	}
	
	/*
	 * local search method. First improvement
	 * change the value of one random index (activated -> not activated and viceversa)
	 * if the new fitness is better, then jump to this solution
	 * iterate until i can't improve anymore my fitness
	 */
	//TODO rivedi
	private void firstImprovement(Solution child) {
		int nIndexes = data.getnIndexes();
		//int bestObjectiveFunction = child.getObjectiveFunction();
		//int tempObjectiveFunction;
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
				//tempObjectiveFunction = tempSolution.getObjectiveFunction();
				tempFitness = tempSolution.getFitness();
				
				checkAndSaveBestSolution(tempSolution);
				
				//if(tempSolution.isFeasible() && tempObjectiveFunction > bestObjectiveFunction) {
				//if(tempObjectiveFunction > bestObjectiveFunction) {
				if(tempFitness > bestFitness) {
					//bestObjectiveFunction = tempObjectiveFunction;
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
	
	/*
	 * local search method. Best improvement
	 * change the value of one index (activated -> not activated and viceversa)
	 * until convergence is reached
	 */
	//TODO rivedi
	private void bestImprovement(Solution child) {
		int nIndexes = data.getnIndexes();
		//int bestObjectiveFunction = child.getObjectiveFunction();
		//int tempObjectiveFunction;
		boolean[] indexes;
		Solution tempSolution, bestSolution = null;
		boolean found;
		float tempFitness;
		float bestFitness = child.getFitness();

		while(true) {
			indexes = child.getIndexes();
			found = false;
			
			for(int i=0; i<nIndexes; i++) {
				indexes[i] = !indexes[i];
				
				tempSolution = Utils.generateSolutionFromIndexes(data, indexes);
				//tempObjectiveFunction = tempSolution.getObjectiveFunction();
				tempFitness = tempSolution.getFitness();
				
				checkAndSaveBestSolution(tempSolution);
				
				//if(tempSolution.isFeasible() && tempObjectiveFunction > bestObjectiveFunction) {
				//if(tempObjectiveFunction > bestObjectiveFunction) {
				if(tempFitness > bestFitness) {
					//bestObjectiveFunction = tempObjectiveFunction;
					bestFitness = tempFitness;
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
	
	private void localSearch(Solution child) {
		int nIndexes = data.getnIndexes();
		//int bestObjectiveFunction = child.getObjectiveFunction();
		//int tempObjectiveFunction;
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
				if(!indexes[sequence[i]]) continue;
				
				//set this index to 0
				indexes[sequence[i]] = false;
				
				for(int j=0; j<nIndexes && !found; j++) {
					if(indexes[sequence[j]]) continue;
					
					//set this index to 1
					indexes[sequence[j]] = true;
					
					tempSolution = Utils.generateSolutionFromIndexes(data, indexes);
					//tempObjectiveFunction = tempSolution.getObjectiveFunction();
					tempFitness = tempSolution.getFitness();
					
					checkAndSaveBestSolution(tempSolution);
					
					//if(tempSolution.isFeasible() && tempObjectiveFunction > bestObjectiveFunction) {
					//if(tempObjectiveFunction > bestObjectiveFunction) {
					if(tempFitness > bestFitness) {
						//bestObjectiveFunction = tempObjectiveFunction;
						bestFitness = tempFitness;
						bestSolution = tempSolution;
						found = true;
					}
					
					indexes[sequence[j]] = false;
				}
				
				indexes[sequence[i]] = true;
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
	
	
	private synchronized void addToChildren(Solution child) {
		children.add(child);
	}
	
	private void printSimilarityMatrix() {
		BufferedWriter bw;
		String s;
		float similarity, totSimilarity = 0, minSimilarity = -1, maxSimilarity = -1;
		try {
			bw = new BufferedWriter(new FileWriter("outputs/GeneticAlgorithm2/similarity.txt"));
			
			for(int i=0; i<numPopulation; i++) {
				for(int j=0; j<numPopulation; j++) {
					similarity = checkSimilarity(population.get(i), population.get(j));
					
					if(i != j) {
						totSimilarity += similarity;
						if(minSimilarity == -1 || similarity < minSimilarity)
							minSimilarity = similarity;
						if(maxSimilarity == -1 || similarity > maxSimilarity)
							maxSimilarity = similarity;
					}
					s = String.format("%.2f ", similarity);
					bw.write(s);
				}
				bw.newLine();
			}
			bw.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Average similarity: " + (totSimilarity / (numPopulation*numPopulation - numPopulation)));
		System.out.println("Min similarity: " + minSimilarity);
		System.out.println("Max similarity: " + maxSimilarity);
	}
	
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
		private int pressure, generation;
		
		private GAThread(int pressure, int generation) {
			this.pressure = pressure;
			this.generation = generation;
		}
		
		@Override
		public void run() {
			// TODO Auto-generated method stub
			super.run();
			
			//selection -> TOURNAMENT SELECTION FOR COMBINATION
			Solution s1 = tournamentSelection(pressure);
			Solution s2 = tournamentSelection(pressure);
			
			//check similarity
			float similarity = checkSimilarity(s1, s2);
			//adaptive mutation rate is function of similarity
			if(similarity < 0.1f) similarity = 0.1f;
			
			int mutationRate = 105 - Math.round(similarity * 100); //TODO verifica una funzione di similarity
			
			//combination -> CROSSOVER + MUTATION
			boolean[] child = crossover(s1, s2);
			mutation(child, mutationRate);
			Solution sChild = Utils.generateSolutionFromIndexes(data, child);
			checkAndSaveBestSolution(sChild);
			
			//local search
			firstImprovement(sChild);
			//bestImprovement(sChild);
			//localSearch(sChild);
			
			/*
			//mutation
			child = sChild.getIndexes();
			mutationSwap(child, 2);
			sChild = Utils.generateSolutionFromIndexes(data, child, s1, s2);
			*/
			
			//add to children
			addToChildren(sChild);				
		}
	}
}
