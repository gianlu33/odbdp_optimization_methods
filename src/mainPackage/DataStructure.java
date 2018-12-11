package mainPackage;

import java.util.ArrayList;
import java.util.HashMap;

public class DataStructure {
	private int nQueries;
	private int nIndexes;
	private int nConfigurations;
	private int maximumMemory;
	private int[] indexesCost;
	private int[] indexesMemory;
	private HashMap<Integer, ArrayList<Integer>>  configurationIndexes;
	private int[][] configurationQueryGain;
	
	public DataStructure() {
		configurationIndexes = new HashMap<>();
	}

	public int getnQueries() {
		return nQueries;
	}

	public void setnQueries(int nQueries) {
		this.nQueries = nQueries;
	}

	public int getnIndexes() {
		return nIndexes;
	}

	public void setnIndexes(int nIndexes) {
		this.nIndexes = nIndexes;
	}

	public int getnConfigurations() {
		return nConfigurations;
	}

	public void setnConfigurations(int nConfigurations) {
		this.nConfigurations = nConfigurations;
	}

	public int getMaximumMemory() {
		return maximumMemory;
	}

	public void setMaximumMemory(int maximumMemory) {
		this.maximumMemory = maximumMemory;
	}

	public int[] getIndexesCost() {
		return indexesCost;
	}
	
	public void setIndexesCost(int[] indexesCost) {
		this.indexesCost = indexesCost;
	}

	public int[] getIndexesMemory() {
		return indexesMemory;
	}
	
	public void setIndexesMemory(int[] indexesMemory) {
		this.indexesMemory = indexesMemory;
	}

	public HashMap<Integer, ArrayList<Integer>> getConfigurationIndexes() {
		return configurationIndexes;
	}

	public int[][] getConfigurationQueryGain() {
		return configurationQueryGain;
	}
	
	public void setConfigurationQueryGain(int[][] configurationQueryGain) {
		this.configurationQueryGain = configurationQueryGain;
	}

	public void printData() {
		System.out.println("Queries: " + nQueries);
		System.out.println("Indexes: " + nIndexes);
		System.out.println("Configurations: " + nConfigurations);
		System.out.println("Memory: " + maximumMemory);
		
		System.out.println("Configuration - Indexes:");
		configurationIndexes.forEach((k, v) -> System.out.println(v));
		
		System.out.println("Indexes Cost:");
		for(int i=0; i<nIndexes; i++)
			System.out.print(indexesCost[i] + " ");
		System.out.println();
		
		System.out.println("Indexes Memory:");
		for(int i=0; i<nIndexes; i++)
			System.out.print(indexesMemory[i] + " ");
		System.out.println();
		
		System.out.println("Gains:");
		for(int i=0; i<nConfigurations; i++) {
			for(int j=0; j<nQueries; j++)
				System.out.print(configurationQueryGain[i][j] + " ");
			System.out.println();
		}
			
	}
	
	public void printStatistics() {
		
		int sumWeight=0, sumCost=0, sumIndexes=0, sumGain=0, sumConfigurations=0;
		
		for(int i=0; i<nIndexes; i++) {
			sumWeight += indexesMemory[i];
			sumCost += indexesCost[i];
		}
		
		for(ArrayList<Integer> conf : configurationIndexes.values()) {
			sumIndexes += conf.size();
		}
		
		for(int i=0; i<nConfigurations; i++) {
			
			for(int j=0; j<nQueries; j++) {
				sumGain += configurationQueryGain[i][j];
				sumConfigurations = configurationQueryGain[i][j] > 0 ? sumConfigurations + 1 : sumConfigurations;
				
			}
		}
		
		System.out.println("Indexes: " + nIndexes + " Queries: " + nQueries + " Configurations: " + nConfigurations);
		System.out.println("Average weight per index: " + (float)sumWeight / nIndexes);
		System.out.println("Average cost per index: " + (float)sumCost / nIndexes);
		System.out.println("Average indexes per configuration: " + (float)sumIndexes / nConfigurations);
		System.out.println("Average gain per query: " + (float)sumGain / nQueries);
		System.out.println("Average configurations per query: " + (float)sumConfigurations / nQueries);
	
	}

}
