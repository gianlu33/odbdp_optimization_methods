package mainPackage;

public class MainClass {

	public static void main(String[] args) {
		
		DataStructure data = new DataStructure();
		
		Utils.readData(data, "given_instances/instance01.odbdp");
		
		data.printData();
		
		
	}

}
