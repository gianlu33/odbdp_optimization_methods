package mainPackage;

public class Solution {
	private boolean[] indexes;
	private int objectiveFunction;
	private int[][] matrix;
	private boolean feasible;
	private int memory;
	private float fitness;
	
	public Solution(boolean[] indexes, int objectiveFunction, int memory, int[][] matrix, boolean feasible, float fitness) {
		this.indexes = indexes;
		this.objectiveFunction = objectiveFunction;
		this.memory = memory;
		this.matrix = matrix;
		this.feasible = feasible;
		this.fitness = fitness;

	}
	
	private Solution() {
		this.feasible = false;
	}
	
	public Solution clone() {
		Solution s = new Solution();
		s.indexes = indexes.clone();
		s.objectiveFunction = objectiveFunction;
		s.memory = memory;
		s.matrix = matrix.clone();
		s.feasible = feasible;
		s.fitness = fitness;
		
		return s;
	}
	
	public void copy(Solution s) {
		this.indexes = s.indexes;
		this.objectiveFunction = s.objectiveFunction;
		this.memory = s.memory;
		this.matrix = s.matrix;
		this.feasible = s.feasible;
		this.fitness = s.fitness;
	}
	
	public boolean[] getIndexes() {
		return indexes;
	}

	public void setIndexes(boolean[] indexes) {
		this.indexes = indexes;
	}

	public int getObjectiveFunction() {
		return objectiveFunction;
	}

	public void setObjectiveFunction(int objectiveFunction) {
		this.objectiveFunction = objectiveFunction;
	}
	
	public int getMemory() {
		return memory;
	}
	
	public void setMemory(int memory) {
		this.memory = memory;
	}

	public int[][] getMatrix() {
		return matrix;
	}

	public void setMatrix(int[][] matrix) {
		this.matrix = matrix;
	}

	public boolean isFeasible() {
		return feasible;
	}

	public void setFeasible(boolean feasible) {
		this.feasible = feasible;
	}
	
	public float getFitness() {
		return fitness;
	}
	
	public void setFitness(float fitness) {
		this.fitness = fitness;
	}
	
	public static int compare(Solution a, Solution b) {
		return Float.compare(b.fitness, a.fitness);
	}
	
	public static int compare2(Solution a, Solution b) {
		if(a.feasible == b.feasible)
			return Float.compare(b.fitness, a.fitness);
		
		if(a.feasible)
			return -1;
		
		return 1;
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		
		
		sb.append("Feasible: " + feasible + "\n");
		sb.append("Obj. Function: " + objectiveFunction + "\n");
		sb.append("Fitness: " + fitness + "\n");
		sb.append("Memory: " + memory + "\n");
		
		for(int i=0; i<indexes.length; i++) {
			if(indexes[i])
				sb.append("1");
			else
				sb.append("0");
		}
		
		return sb.toString();
	}
}
