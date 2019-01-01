package mainPackage;

public class GeneticThread extends Thread {
	private MultiThreadGenetic genetic;
	
	public GeneticThread( MultiThreadGenetic genetic) {
		this.genetic = genetic;
	}
	@Override
	public void run() {
		super.run();
		genetic.start();
	}
}