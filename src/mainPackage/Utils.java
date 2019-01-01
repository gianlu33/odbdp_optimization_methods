package mainPackage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;

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
			
		} catch (Exception e) {
			System.out.println("Error with instancefilename");
			System.out.println("Usage: java -jar jarfile.jar instancefilename -t timelimit");
			System.exit(-1);
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
			
		} catch (Exception e) {
			System.out.println("IO error in writing output file");
			System.exit(-1);
		}
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
	 * Generate an array of random indexes
	 * Compute the average number of indexes to be activated in order to reach memory limit
	 * activate each index with probability avgIndexes / nIndexes
	 */
	public static boolean[] generateRandomIndexes(DataStructure data){
		int nIndexes = data.getnIndexes();
		int memory = data.getMaximumMemory();
		
		//compute sum of weights
		int[] weights = data.getIndexesMemory();
		int sumMemory = 0;
		for(int i=0; i<nIndexes; i++)
			sumMemory += weights[i];
		
		//compute average memory per index
		int avgMemory = sumMemory / nIndexes;
		//compute number of indexes to be activated in order to reach memory limit
		int avgIndexes = memory / avgMemory;
		
		//compute probability (for random)
		int probability = nIndexes / avgIndexes;
		
		Random rand = new Random();
		boolean[] indexes = new boolean[nIndexes];
		
		for(int i=0; i<nIndexes; i++) {
			indexes[i] = rand.nextInt(probability) == 0;
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
	 * 		and calculate the objective function and the total memory used 
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

}
