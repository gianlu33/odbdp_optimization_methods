package mainPackage;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Utils {

	
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
	
	//TODO rivedere parametri passati e valori di ritorno per tutti i metodi qui sotto, e vedere se servono o meno tutti sti metodi
	
	//write solution to output file
	public static void writeOutput(HashMap<Integer, Integer> queryConfiguration, String filename) {
		//TODO implementare
	}
	
	//build vector of indexes xi for calculating the objective function and total memory, given a temporary solution
	public static int[] activateIndexes(DataStructure data, HashMap<Integer, Integer> queryConfiguration) {
		//TODO implementare
		return null;
	}
	
	//calculates the objective function given a temporary solution
	public static int computeObjectiveFunction(int[][] configurationQueryGain, int[] indexesCost, HashMap<Integer, Integer> queryConfiguration, int[] indexes) {
		//TODO implementare
		return 0;
	}
	
	//calculates the memory used given a temporary solution
	public static int computeMemoryUsed(int[] indexesMemory, int[] indexes) {
		//TODO implementare
		return 0;
	}
	
	//TODO vedere se ci sono altri metodi di utilità da implementare

}
