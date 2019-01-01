package mainPackage;

public class GeneticThread extends Thread {
	private GeneticAlgorithm ga;
	
	public GeneticThread( GeneticAlgorithm ga) {
		this.ga = ga;
	}
	@Override
	public void run() {
		super.run();
		ga.start();
	}
}