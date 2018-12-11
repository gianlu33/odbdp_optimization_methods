package mainPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import java.util.Random;

public class Utils {
	
	/*
	 * read the data from input file
	 */
	public static void readData(DataStructure data, String filename) {
		String sCurrentLine;
		BufferedReader br;
		String indexes[];
		String gains[];
		int i,j;
		
		int nIndexes, nConfigurations, nQueries;
		int[] indexesCost = data.getIndexesCost();
		int[] indexesMemory = data.getIndexesMemory();
		int[][] configurationQueryGain = data.getConfigurationQueryGain();
		HashMap<Integer, ArrayList<Integer>> configurationIndexes = data.getConfigurationIndexes();
		ArrayList<Integer> al;
		
		try {
			br = new BufferedReader(new FileReader(filename));
			
			//reading the number of queries (1st row of input)
			sCurrentLine = br.readLine();
//			System.out.println(sCurrentLine);
			
			//Loading the number of queries of the given instance
			nQueries = (Integer.parseInt(sCurrentLine.split(":")[1].trim()));
			data.setnQueries(nQueries);
			
			//reading the number of indexes (2nd row of input)
			sCurrentLine = br.readLine();
//			System.out.println(sCurrentLine);
			
			//Loading the number of indexes of the given instance
			nIndexes = Integer.parseInt(sCurrentLine.split(":")[1].trim());
			data.setnIndexes(nIndexes);
			
			//reading the number of configurations (3rd row of input)
			sCurrentLine = br.readLine();
//			System.out.println(sCurrentLine);
			
			//Loading the number of configuration of the given instance
			nConfigurations = Integer.parseInt(sCurrentLine.split(":")[1].trim());
			data.setnConfigurations(nConfigurations);
			
			//reading the memory of configurations (4th row of input)
			sCurrentLine = br.readLine();
//			System.out.println(sCurrentLine);
			
			//Loading the memory of the given instance
			data.setMaximumMemory(Integer.parseInt(sCurrentLine.split(":")[1].trim()));
			
			// Skip CONFIGURATIONS_INDEXES_MATRIX:
			sCurrentLine=br.readLine();
			
			//Reading matrix that links indexes used by each configuration
			for(i = 0; i < nConfigurations; i++) {
				sCurrentLine=br.readLine();
				indexes=sCurrentLine.split(" ");
				
				al = new ArrayList<>();
				
				for (j = 0; j < nIndexes; j++) {
					int value = Integer.parseInt(indexes[j]);
					
					//add index to list only if configuration i activates this index
					if(value == 1) {
						al.add(j);
					}	
				}
		
				configurationIndexes.put(i, al);
			}
				
			// Skip INDEXES_FIXED_COST:
			sCurrentLine=br.readLine();
			
			//Reading indexes costs
			indexesCost = new int[nIndexes];
			
			for(i = 0; i < nIndexes; i++) {
				sCurrentLine=br.readLine();
				indexesCost[i]=Integer.parseInt(sCurrentLine.trim());
			}
			
			data.setIndexesCost(indexesCost);
			
			// Skip INDEXES_MEMORY_OCCUPATION:
			sCurrentLine=br.readLine();
			
			//Reading indexes memory occupation
			indexesMemory = new int[nIndexes];
			
			for(i = 0; i < nIndexes; i++) {
				sCurrentLine=br.readLine();
				indexesMemory[i]=Integer.parseInt(sCurrentLine.trim());
			}
			
			data.setIndexesMemory(indexesMemory);
			
			// Skip CONFIGURATIONS_QUERIES_GAIN:
			sCurrentLine=br.readLine();
			
			//Reading matrix that links indexes used by each configuration
			configurationQueryGain = new int[nConfigurations][nQueries];
			
			for(i = 0; i < nConfigurations; i++) {
				sCurrentLine=br.readLine();
				gains=sCurrentLine.split(" ");
				for (j = 0; j < nQueries; j++) {
					configurationQueryGain[i][j]=Integer.parseInt(gains[j]);
				}
			}
			
			data.setConfigurationQueryGain(configurationQueryGain);
			
			//sCurrentLine=br.readLine();
			//if(!sCurrentLine.equals("EOF"))
				//throw new Exception("Erorr reading file: EOF not found");
			
			br.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * write solution to output file
	 */
	public static void writeOutput(int[][] solution, String filename) {
		BufferedWriter bw;
		
		try {
			bw = new BufferedWriter(new FileWriter(filename));
			
			for(int i=0; i<solution.length; i++) {
				for(int j=0; j<solution[i].length; j++)
					bw.write(solution[i][j] + " ");
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
	}
	
	/*
	 * transforms our solution from HashMap to matrix (in order to write it to file)
	 */
	public static int[][] mapToMatrix(HashMap<Integer, Integer> queryConfiguration, int nQueries, int nConfigurations){
		int[][] solution = new int[nConfigurations][nQueries];
		int configuration;
		
		for(int query=0; query<nQueries; query++) {
			if(!queryConfiguration.containsKey(query))
				continue; //no configuration for this query
			
			configuration = queryConfiguration.get(query);
			solution[configuration][query] = 1;
		}
		
		return solution;
	}
	
	/*
	 * transforms our solution from matrix to HashMap
	 */
	public static HashMap<Integer, Integer> matrixToMap(int[][] matrix, int nQueries, int nConfigurations){
		HashMap<Integer, Integer> solution = new HashMap<>();
		
		for(int i=0; i<nQueries; i++) {
			for(int j=0; j<nConfigurations; j++) {
				if(matrix[j][i] == 1) {
					solution.put(i, j);
					break;
				}
			}
		}
		
		return solution;
	}
	
	/*
	 * build array of indexes xi for calculating the objective function and total memory, given a temporary solution
	 * array[i] == 0 -> index not activated
	 * array[i] >  0 -> index activated
	 */
	public static int[] activateIndexes(int nIndexes, HashMap<Integer, ArrayList<Integer>> configurationIndexes, HashMap<Integer, Integer> queryConfiguration) {
		int[] indexes = new int[nIndexes];
		
		queryConfiguration.forEach((k, v) -> {
			ArrayList<Integer> listIndexes = configurationIndexes.get(v);
			listIndexes.forEach(i -> indexes[i]++);
		});
		
		return indexes;
	}
	
	/*
	 * calculates the objective function given a temporary solution
	 * NOTE -> for calculating the objective function is necessary to have indexes array, obtained by calling method activateIndexes
	 */
	public static int computeObjectiveFunction(int[][] configurationQueryGain, int[] indexesCost, HashMap<Integer, Integer> queryConfiguration, int[] indexes) {
		int objectiveFunction = 0;
		
		for(Entry<Integer, Integer> entry : queryConfiguration.entrySet()) {
			objectiveFunction += configurationQueryGain[entry.getValue()][entry.getKey()];
		}
		
		for(int i=0; i<indexes.length; i++) {
			objectiveFunction -= indexes[i] > 0 ? indexesCost[i] : 0;
		}
		
		return objectiveFunction;
	}
	
	/*
	 * calculates the memory used given a temporary solution
	 * NOTE -> for calculating the memory used is necessary to have indexes array, obtained by calling method activateIndexes
	 */
	public static int computeMemoryUsed(int[] indexesMemory, int[] indexes) {
		int memoryUsed = 0;
		
		for(int i=0; i<indexes.length; i++) {
			memoryUsed += indexes[i] > 0 ? indexesMemory[i] : 0;
		}
		
		return memoryUsed;
	}
	
	/*
	 * generate a random solution. This solution COULD BE NOT FEASIBLE! (due to memory cost)
	 */
	public static HashMap<Integer, Integer> generateRandomSolution(int nQueries, int nConfigurations){
		//TODO vedere se lasciare possibile infeasibility dovuta alla memoria oppure no
		Random rand = new Random();
		int num;
		HashMap<Integer, Integer> solution = new HashMap<>();
		
		for(int i=0; i<nQueries; i++) {
			num = rand.nextInt(nConfigurations * 1000); //TODO si può settare un valore più alto per abbassare il rischio di non feasibility
			if(num >= nConfigurations) {
				//no configuration for this query
				continue;
			}
			
			solution.put(i, num);	
		}
		
		return solution;
	}
	
	/*
	 * this method generate a new indexes array, given a change in actual solution (a query has changed his configuration)
	 * decrement the values of indexes that are served by old configuration, and increment the ones that are served by new configuration
	 * old array is not modified
	 */
	public static int[] updateIndexes(int[] originalIndexes, HashMap<Integer, ArrayList<Integer>> configurationIndexes, int oldConfiguration, int newConfiguration) {
		ArrayList<Integer> listIndexes;
		int[] indexes = originalIndexes.clone(); //no update of original array
		
		if(oldConfiguration != -1) {
			//decrement values in array indexes
			listIndexes = configurationIndexes.get(oldConfiguration);
			for(int index : listIndexes) {
				indexes[index]--;
			}
		}
		
		//increment values in array indexes
		listIndexes = configurationIndexes.get(newConfiguration);
		for(int index : listIndexes) {
			indexes[index]++;
		}
		
		
		return indexes;
	}
	
	/*
	 * this method updates the objective function, given a change in actual solution (a query has changed his configuration)
	 * subtract the gain of old configuration (gcq) and add the new gain of new configuration
	 * remove the total cost of old indexes array and add the new cost of new indexes array
	 * NOTE -> for calculating the objective function is necessary to have old and new indexes array, obtained by calling method activateIndexes
	 */
	public static int updateObjectiveFunction(int[] indexesCost, int objectiveFunction, int[] oldIndexes, int[] newIndexes, int oldGain, int newGain) {
		//TODO verifica quanto si risparmia a usare questo metodo invece di ricalcolare tutto
		//non sembra che si risparmi granchè
		int oldCost = 0, newCost = 0;
		
		for(int i=0; i<indexesCost.length; i++) {
			oldCost += oldIndexes[i] > 0 ? indexesCost[i] : 0;
			newCost += newIndexes[i] > 0 ? indexesCost[i] : 0;
		}
		System.out.println("costs: " + oldCost + " " + newCost);
		
		return objectiveFunction - oldGain + oldCost + newGain - newCost;
	}
	
	/*
	 * this method generates a "good" starting solution, obtained from a greedy algorithm
	 * there is some randomization so that the solution generated is not always the same
	 * 
	 */
	public static HashMap<Integer, Integer> generateGoodSolution1(DataStructure data){
		int maximumMemory = data.getMaximumMemory();
		int nQueries = data.getnQueries();
		int nIndexes = data.getnIndexes();
		int[][] configurationQueryGain = data.getConfigurationQueryGain();
		HashMap<Integer, ArrayList<Integer>> configurationIndexes = data.getConfigurationIndexes();
		ArrayList<Integer> confIndexes;
		int[] indexesCost = data.getIndexesCost();
		int[] indexesMemory = data.getIndexesMemory();
		boolean[] indexes = new boolean[nIndexes];
		int[] queries = new int[nQueries];
		int nConfigurations = data.getnConfigurations();
		Random rand = new Random();
		int j, temp, gain, cost, memory, bestMemory=0, bestConfig, tempValue, bestValue;
		int usedMemory = 0;
		
		HashMap<Integer, Integer> solution = new HashMap<>();
		
		//generate array of queries indexes
		for(int i=0; i<nQueries; i++)
			queries[i] = i;
		
		//shuffle query array in order to add some randomization in this algorithm (so i'll have different solutions)
		for(int i=0; i<nQueries; i++) {
			j = rand.nextInt(nQueries);
			temp = queries[i];
			queries[i] = queries[j];
			queries[j] = temp;
		}
		
		//good solution generation
		//for each query, i take the best configuration according to the formula (gain * confIndexes.size()) - cost
		for(int i=0; i<nQueries; i++) {
			bestConfig = -1;
			bestValue = Integer.MIN_VALUE;
			
			if(usedMemory >= maximumMemory)
				break;
			
			for(j=0; j<nConfigurations; j++) {
				cost = 0;
				memory = 0;
				gain = configurationQueryGain[j][queries[i]];
				confIndexes = configurationIndexes.get(j);
				
				for(int index : confIndexes) {
					cost += indexes[index] ? 0 : indexesCost[index]; 
					memory += indexes[index] ? 0 : indexesMemory[index];
				}
				
				tempValue = (gain * confIndexes.size()) - cost;
				if(usedMemory + memory <= maximumMemory && tempValue > bestValue) {
					bestValue = tempValue;
					bestConfig = j;
					bestMemory = memory;
				}
			}
			
			//assign bestConfig to query i, if found a feasible configuration that improves our solution
			if(bestConfig == -1) continue;
			
			usedMemory += bestMemory;
			confIndexes = configurationIndexes.get(bestConfig);
			
			for(int index : confIndexes)
				indexes[index] = true;
			
			solution.put(queries[i], bestConfig);
		}
		

		return solution;
	}
	
	/*
	 * another greedy algorithm
	 * activate the best indexes according to formula:
	 * 					numConfigurationsThatActivateIndex / indexCost * randomWeight
	 * 
	 * then, i mark as usable configurations that activate ONLY activated indexes
	 * then, for each query i assign the best configuration in terms of gain
	 */
	public static HashMap<Integer, Integer> generateGoodSolution2(DataStructure data){
		int nIndexes = data.getnIndexes();
		int nConfigurations = data.getnConfigurations();
		int nQueries = data.getnQueries();
		int maximumMemory = data.getMaximumMemory();
		int[] indexesCost = data.getIndexesCost();
		int[] indexesMemory = data.getIndexesMemory();
		int[][] configurationQueryGain = data.getConfigurationQueryGain();
		HashMap<Integer, ArrayList<Integer>> configurationIndexes = data.getConfigurationIndexes();
		Random rand = new Random();
		
		float[] indexWeights = new float[nIndexes];
		boolean[] indexes = new boolean[nIndexes];
		boolean[] configurations = new boolean[nConfigurations];
		int memoryUsed = 0, bestIndex, bestMemory = 0, bestConfiguration, bestGain;
		float bestWeight;
		
		HashMap<Integer, Integer> solution = new HashMap<>();
		
		
		//count how many configurations use index i, for each i
		configurationIndexes.forEach((k,v) -> {
			for(int index : v) {
				indexWeights[index]++;
			}
		});
		
		//divide each indexWeight for their activation cost (greedy approach) and multiply for a random weight (randomization)
		for(int i=0; i<nIndexes; i++)
			indexWeights[i] = indexWeights[i] / indexesCost[i] * rand.nextFloat();
		
		//activate indexes having best weight, until memory limit is reached
		while(true) {
			bestIndex = -1;
			bestWeight = 0;
			
			for(int i=0; i<nIndexes; i++) {
				if(!indexes[i] && memoryUsed + indexesMemory[i] <= maximumMemory && indexWeights[i] > bestWeight) {
					bestIndex = i;
					bestWeight = indexWeights[i];
					bestMemory = indexesMemory[i];
				}
			}
			
			//activate best index, stop if i don't have found an index
			if(bestIndex == -1) break;
			
			indexes[bestIndex] = true;
			memoryUsed += bestMemory;
		}
		
		//activate configurations that serve only activated indexes
		for(int i=0; i<nConfigurations; i++) {
			boolean activate = true;
			for(int index : configurationIndexes.get(i)) {
				if(!indexes[index]) {
					activate = false;
					break;
				}
			}
			
			if(activate)
				configurations[i] = true;
		}
		
		//assign to each query the active configuration that has best gain > 0 (if present)
		for(int i=0; i<nQueries; i++) {
			bestConfiguration = -1;
			bestGain = 0;
			
			for(int j=0; j<nConfigurations; j++) {
				if(configurations[j] && configurationQueryGain[j][i] > bestGain) {
					bestConfiguration = j;
					bestGain = configurationQueryGain[j][i];
				}
			}
			
			if(bestConfiguration == -1) continue;
			
			solution.put(i, bestConfiguration);
		}
		
		return solution;
	}
	
	/*
	 * another greedy algorithm
	 * i activate a random set of indexes until a certain percentage of memory used is reached (treshold)
	 * then, i mark as usable configurations that activate ONLY activated indexes
	 * then, for each query i assign the best configuration in terms of gain
	 */
	public static HashMap<Integer, Integer> generateGoodSolution3(DataStructure data, double treshold){
		int nIndexes = data.getnIndexes();
		int nConfigurations = data.getnConfigurations();
		int nQueries = data.getnQueries();
		int maximumMemory = data.getMaximumMemory();
		int[] indexesMemory = data.getIndexesMemory();
		int[][] configurationQueryGain = data.getConfigurationQueryGain();
		HashMap<Integer, ArrayList<Integer>> configurationIndexes = data.getConfigurationIndexes();
		Random rand = new Random();
		
		boolean[] indexes = new boolean[nIndexes];
		boolean[] configurations = new boolean[nConfigurations];
		int memoryUsed = 0, bestConfiguration, bestGain;
		
		double tresh;
		
		HashMap<Integer, Integer> solution = new HashMap<>();
		
		//activate random indexes, until memory limit is reached
		while(true) {
			tresh = (double) memoryUsed / maximumMemory;
			if(tresh > treshold) break;
			
			int index = rand.nextInt(nIndexes);
			if(indexes[index]) continue;
			
			if(memoryUsed + indexesMemory[index] > maximumMemory) continue;
			
			//activate index
			indexes[index] = true;
			memoryUsed += indexesMemory[index];
		}
		
		//activate configurations that serve only activated indexes
		for(int i=0; i<nConfigurations; i++) {
			boolean activate = true;
			for(int index : configurationIndexes.get(i)) {
				if(!indexes[index]) {
					activate = false;
					break;
				}
			}
			
			if(activate)
				configurations[i] = true;
		}
		
		//assign to each query the active configuration that has best gain > 0 (if present)
		for(int i=0; i<nQueries; i++) {
			bestConfiguration = -1;
			bestGain = 0;
			
			for(int j=0; j<nConfigurations; j++) {
				if(configurations[j] && configurationQueryGain[j][i] > bestGain) {
					bestConfiguration = j;
					bestGain = configurationQueryGain[j][i];
				}
			}
			
			if(bestConfiguration == -1) continue;
			
			solution.put(i, bestConfiguration);
		}
		
		return solution;
	}
	
	/*
	 * Generate a random set of activated indexes.
	 * This set provides a feasible solution in terms of memory occupation
	 * When a treshold of memory passed as parameter is reached, the algorithm stops
	 */
	public static boolean[] generateRandomIndexes(DataStructure data, double treshold){
		int nIndexes = data.getnIndexes();
		int maximumMemory = data.getMaximumMemory();
		int[] indexesMemory = data.getIndexesMemory();
		Random rand = new Random();
		
		boolean[] indexes = new boolean[nIndexes];
		int memoryUsed = 0;
		
		double tresh;
		
		//activate random indexes, until memory limit is reached
		while(true) {
			tresh = (double) memoryUsed / maximumMemory;
			if(tresh > treshold) break;
			
			int index = rand.nextInt(nIndexes);
			if(indexes[index]) continue;
			
			if(memoryUsed + indexesMemory[index] > maximumMemory) continue;
			
			//activate index
			indexes[index] = true;
			memoryUsed += indexesMemory[index];
		}
		
		return indexes;
	}
	
	/*
	 * Generate a random set of activated indexes.
	 * This set provides a solution (maybe non feasible)
	 * when maximumMemory is reached, algorithm stops
	 */
	public static boolean[] generateRandomIndexes2(DataStructure data){
		int nIndexes = data.getnIndexes();
		int maximumMemory = data.getMaximumMemory();
		int[] indexesMemory = data.getIndexesMemory();
		int index;
		
		boolean[] indexes = new boolean[nIndexes];
		int memoryUsed = 0;
		
		int[] sequence = generateRandomOrderedSequence(nIndexes);
		
		for(int i=0; i<nIndexes; i++) {
			if(memoryUsed >= maximumMemory) break;
			
			index = sequence[i];
			indexes[index] = true;
			memoryUsed += indexesMemory[index];
		}
		
		return indexes;
	}
	
	public static int[] generateRandomOrderedSequence(int values) {
		Random rand = new Random();
		int j, temp;
		
		int[] sequence = new int[values];
		for(int i=0; i<values; i++)
			sequence[i] = i;
		
		for(int i=0; i<values; i++) {
			j = rand.nextInt(values);
			temp = sequence[i];
			sequence[i] = sequence[j];
			sequence[j] = temp;
		}
		
		return sequence;
	}
	
	/*
	 * Generate a random set of activated indexes.
	 * This set provides a solution (maybe non feasible)
	 * for each index, i activate this index with probability 10/nIndexes
	 */
	public static boolean[] generateRandomIndexes3(DataStructure data){
		int nIndexes = data.getnIndexes();
		Random rand = new Random();
		boolean[] indexes = new boolean[nIndexes];
		int prob = nIndexes / 10;
		
		for(int i=0; i<nIndexes; i++) {
			indexes[i] = rand.nextInt(prob) == 0;
		}
		
		return indexes;
	}
	
	/*
	 * given a boolean array of indexes (indexes[i] == true if activated, false otherwise),
	 * this method calculates the best solution (ycq) that can be found for this array
	 * 
	 * 1 - from indexes array, build a list of configurations that could be used
	 * 		these configurations are the ones that activates ONLY the indexes that are activated in indexes array
	 * 
	 * 2 - for each query, assign the best configuration in terms of gain, from the list built before
	 * 
	 * 3 - recompute the indexes array (because i could have not used all the indexes)
	 * 		and calculate the objective function and the total memory used * 
	 * 
	 * It is very easy and fast finding this solution.
	 * 
	 * Returns the objective function of the solution ycq (-1 if infeasible), and it fills the matrix passed as parameter
	 * NOTE: the parameter solution MUST BE NOT NULL!!!
	 */
	public static Solution generateSolutionFromIndexes(DataStructure data, boolean[] indexes){
		int totalGain = 0, totalMemory = 0, totalCost = 0, bestConfig, bestGain, gain;
		HashMap<Integer, ArrayList<Integer>> configurationIndexes = data.getConfigurationIndexes();
		int[][] configurationQueryGain = data.getConfigurationQueryGain();
		int[] indexesCost = data.getIndexesCost();
		int[] indexesMemory = data.getIndexesMemory();
		int nQueries = data.getnQueries();
		int nConfigurations = data.getnConfigurations();
		int maximumMemory = data.getMaximumMemory();
		
		ArrayList<Integer> activeConfigurations = new ArrayList<>();
		boolean[] realIndexes = new boolean[indexes.length];
		int[][] matrix = new int[nConfigurations][nQueries];
		boolean isFeasible;
		int objectiveFunction;
		float fitness;
			
		//from indexes to active configurations
		configurationIndexes.forEach((k,v) -> {
			boolean active = true;
			
			for(int index : v) {
				if(!indexes[index]) {
					active = false;
					break;
				}
			}
			
			if(active)
				activeConfigurations.add(k);
		});
		
		//now i have a list of active configurations. All i have to do is to assign one of these configurations
		//to each query.
		for(int i=0; i<nQueries; i++) {
			bestConfig = -1;
			bestGain = 0;
			
			for(int config : activeConfigurations) {
				gain = configurationQueryGain[config][i];
				if(gain > bestGain) {
					bestConfig = config;
					bestGain = gain;
				}
			}
			
			if(bestConfig == -1) continue; //no configuration with gain > 0 for this query found
			
			totalGain += bestGain;
			matrix[bestConfig][i] = 1;
			
			//activate real indexes
			for(int index : configurationIndexes.get(bestConfig)) {
				realIndexes[index] = true;
			}
		}	
		
		//compute real cost and memory of the indexes, because the solution ycq might not have activate all the indexes
		for(int i=0; i<realIndexes.length; i++) {
			totalCost += realIndexes[i] ? indexesCost[i] : 0;
			totalMemory += realIndexes[i] ? indexesMemory[i] : 0;
		}
		
		isFeasible = totalMemory > maximumMemory ? false : true;	
		
		objectiveFunction = totalGain - totalCost;
		fitness = isFeasible ? (float) objectiveFunction / maximumMemory : (float) objectiveFunction / totalMemory;
		
		Solution s = new Solution(realIndexes, objectiveFunction, totalMemory, matrix, isFeasible, fitness);
		
		return s;
	}
	
	//TODO vedere se ci sono altri metodi di utilità da implementare

}
