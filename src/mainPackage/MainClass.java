package mainPackage;

import java.util.ArrayList;

public class MainClass {

	public static void main(String[] args) {
		int time = 0;
		DataStructure data = new DataStructure();
		
		if(args.length != 3 || args[1].compareTo("-t") != 0) {
			System.out.println("Usage: java -jar jarfile.jar instancefilename -t timelimit");
			System.exit(-1);
		}
		
		try {
			time = Integer.parseInt(args[2]);
		}catch(NumberFormatException e) {
			System.out.println("timelimit must be numeric!");
			System.out.println("Usage: java -jar jarfile.jar instancefilename -t timelimit");
			System.exit(-1);
		}
		
		Utils.readData(data, args[0]);
		
		String outputfile = args[0] + "_OMAMZ_group02.sol";
		
		int numPopulation = 50;
		int nElite = 5;
		int pressure = numPopulation / 20;

		int numThreads = 6;
		if(time > 30) numThreads += (time - 30) / 10;
		if(numThreads > 64) numThreads = 64;
				
		MultiThreadGenetic multi = new MultiThreadGenetic(data, numThreads, outputfile, time, numPopulation, nElite, pressure);
		ArrayList<GeneticThread> threads = new ArrayList<>();
		
		for(int i=0; i<numThreads; i++) {
			GeneticThread thread = new GeneticThread(i, multi);
			threads.add(thread);
			thread.start();
		}
		
		for(int i=0; i<numThreads; i++) {
			try {
				GeneticThread thread = threads.get(i);
				thread.join();
			}catch(Exception e) {
				System.out.println("FATAL ERROR WITH THREADS");
				System.exit(-1);
			}
		}
		
		System.exit(multi.getObjectiveFunction());
	}
}
