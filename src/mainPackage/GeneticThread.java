package mainPackage;

public class GeneticThread extends Thread {
	private GeneticAlgorithm genetic;
	
	public GeneticThread( GeneticAlgorithm genetic) {
		this.genetic = genetic;
	}
	@Override
	public void run() {
		super.run();
		genetic.start();
	}
}