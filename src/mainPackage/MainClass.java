package mainPackage;


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
		
		String outputfile = args[0] + "_OMAMZ_group02.sol";
		
		//parameters for genetic algorithm
		int numPopulation = 50;
		int nElite = 5;
		int pressure = 2;
		
		//start now
		long timeStart = System.currentTimeMillis();
		
		//reading data from file
		Utils.readData(data, args[0]);
		
		//number of threads, depends of time given (min. 6 - max. 64)
		//starting from 6 threads, if time is higher than 1 minute i add a thread every 20 seconds
		int numThreads = 6;
		if(time > 60) numThreads += (time - 60) / 20;
		if(numThreads > 64) numThreads = 64;

		MultiThreadGenetic multi = new MultiThreadGenetic(data, numThreads, outputfile, numPopulation, nElite, pressure);
		
		//time in millis
		time *= 1000;

		//start all the threads
		GeneticThread thread;
		for(int i=0; i<numThreads; i++) {
			thread = new GeneticThread(multi);
			thread.start();
		}
		
		while(true) {
			
			if(System.currentTimeMillis() - timeStart >= time) {
				System.exit(multi.getObjectiveFunction());
			}
			
			try {
				Thread.sleep(500);
			}catch(Exception e) {}
			
		}	
	}
	
}
