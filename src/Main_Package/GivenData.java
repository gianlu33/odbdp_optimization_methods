package Main_Package;


public class GivenData {

	public GivenData() {
		
	}

	private Integer n_Queries;
	private Integer n_Indexes;
	private Integer n_Configurations;
	private Integer maximum_Memory;
	private int configuration_Indexes[][];
	private int indexes_Cost[];
	private int indexes_Memory[];
	private int configuration_Query_Gain[][];

	
	public void initialize_Configuration_Index_Matrix(int rows,int columns) {
		this.configuration_Indexes=new int [rows][columns];
	}

	public void initialize_Indexes_Cost(int number_Of_Indexes) {
		this.setIndexes_cost(new int [number_Of_Indexes]);
	}
	
	public void initialize_Indexes_Memory(int number_Of_Indexes) {
		this.setIndexes_Memory(new int [number_Of_Indexes]);
	}
	
	public void initialize_Configuration_Query_Gain_Matrix(int rows,int columns) {
		this.configuration_Query_Gain=new int [rows][columns];
	}
	
	public Integer getN_Queries() {
		return n_Queries;
	}

	public void setN_Queries(Integer n_Queries) {
		this.n_Queries = n_Queries;
	}

	public Integer getN_Indexes() {
		return n_Indexes;
	}

	public void setN_Indexes(Integer n_Indexes) {
		this.n_Indexes = n_Indexes;
	}

	public Integer getN_Configurations() {
		return n_Configurations;
	}

	public void setN_Configurations(Integer n_Configurations) {
		this.n_Configurations = n_Configurations;
	}

	public Integer getMaximum_Memory() {
		return maximum_Memory;
	}

	public void setMaximum_Memory(Integer maximum_Memory) {
		this.maximum_Memory = maximum_Memory;
	}
	
	public int[][] getConfiguration_indexes() {
		return configuration_Indexes;
	}

	public int[] getIndexes_cost() {
		return indexes_Cost;
	}

	public void setIndexes_cost(int indexes_cost[]) {
		this.indexes_Cost = indexes_cost;
	}

	public int[] getIndexes_Memory() {
		return indexes_Memory;
	}

	public void setIndexes_Memory(int indexes_Memory[]) {
		this.indexes_Memory = indexes_Memory;
	}

	public int[][] getConfiguration_Query_Gain() {
		return configuration_Query_Gain;
	}

	public void setConfiguration_Query_Gain(int configuration_Query_Gain[][]) {
		this.configuration_Query_Gain = configuration_Query_Gain;
	}

}
