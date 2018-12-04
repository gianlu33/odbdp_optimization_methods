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

	//TODO implementa metodi vuoti
	
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
	
	//write solution to output file
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
	
	//transforms our solution from HashMap to matrix (in order to write it to file)
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
	
	//build array of indexes xi for calculating the objective function and total memory, given a temporary solution
	//array[i] == 0 -> index not activated
	//array[i] >  0 -> index activated
	public static int[] activateIndexes(int nIndexes, HashMap<Integer, ArrayList<Integer>> configurationIndexes, HashMap<Integer, Integer> queryConfiguration) {
		int[] indexes = new int[nIndexes];
		
		queryConfiguration.forEach((k, v) -> {
			ArrayList<Integer> listIndexes = configurationIndexes.get(v);
			listIndexes.forEach(i -> indexes[i]++);
		});
		
		return indexes;
	}
	
	//calculates the objective function given a temporary solution
	//NOTE -> for calculating the objective function is necessary to have indexes array, obtained by calling method activateIndexes
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
	
	//calculates the memory used given a temporary solution
	//NOTE -> for calculating the memory used is necessary to have indexes array, obtained by calling method activateIndexes
	public static int computeMemoryUsed(int[] indexesMemory, int[] indexes) {
		int memoryUsed = 0;
		
		for(int i=0; i<indexes.length; i++) {
			memoryUsed += indexes[i] > 0 ? indexesMemory[i] : 0;
		}
		
		return memoryUsed;
	}
	
	//generate a random solution. This solution COULD BE NOT FEASIBLE! (due to memory cost)
	public static HashMap<Integer, Integer> generateRandomSolution(int nQueries, int nConfigurations){
		//TODO vedere se lasciare possibile infeasibility dovuta alla memoria oppure no
		Random rand = new Random();
		int num;
		HashMap<Integer, Integer> solution = new HashMap<>();
		
		for(int i=0; i<nQueries; i++) {
			num = rand.nextInt(nConfigurations + 500); //TODO si può settare un valore più alto per abbassare il rischio di non feasibility
			if(num >= nConfigurations) {
				//no configuration for this query
				continue;
			}
			
			solution.put(i, num);	
		}
		
		return solution;
	}
	
	//this method generate a new indexes array, given a change in actual solution (a query has changed his configuration)
	//decrement the values of indexes that are served by old configuration, and increment the ones that are served by new configuration
	//old array is not modified
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
	
	//this method updates the objective function, given a change in actual solution (a query has changed his configuration)
	//subtract the gain of old configuration (gcq) and add the new gain of new configuration
	//remove the total cost of old indexes array and add the new cost of new indexes array
	//NOTE -> for calculating the objective function is necessary to have old and new indexes array, obtained by calling method activateIndexes
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
	
	//this method generates a "good" starting solution, obtained from a greedy algorithm
	//there is some randomization so that the solution generated is not always the same
	public static HashMap<Integer, Integer> generateGoodSolution(DataStructure data){
		return null;
	}
	
	//TODO vedere se ci sono altri metodi di utilità da implementare


}
