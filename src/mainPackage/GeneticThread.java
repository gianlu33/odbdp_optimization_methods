package mainPackage;

public class GeneticThread extends Thread {
	private int numThread;
	private MultiThreadGenetic genetic;
	
	public GeneticThread(int numThread, MultiThreadGenetic genetic) {
		this.numThread = numThread;
		this.genetic = genetic;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
		super.run();
		genetic.start(numThread);
	}
}