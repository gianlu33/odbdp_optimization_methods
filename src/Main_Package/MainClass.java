package Main_Package;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class MainClass {

	@SuppressWarnings("resource")
	public static void main(String[] args) {
		
		GivenData givenData = new GivenData();
		String sCurrentLine;
		BufferedReader br;
		String indexes[];
		String gains[];
		int i,j;
		
		try {
			br = new BufferedReader(new FileReader("given_instances/instance01.odbdp"));

			// prova 
			// prova 2
			
			//prova 3
			
			//reading the number of queries (1st row of input)
			sCurrentLine = br.readLine();
//			System.out.println(sCurrentLine);
			
			//Loading the number of queries of the given instance
			givenData.setN_Queries(Integer.parseInt(sCurrentLine.split(":")[1].trim()));
			
			//reading the number of indexes (2nd row of input)
			sCurrentLine = br.readLine();
//			System.out.println(sCurrentLine);
			
			//Loading the number of indexes of the given instance
			givenData.setN_Indexes(Integer.parseInt(sCurrentLine.split(":")[1].trim()));
			
			//reading the number of configurations (3rd row of input)
			sCurrentLine = br.readLine();
//			System.out.println(sCurrentLine);
			
			//Loading the number of configuration of the given instance
			givenData.setN_Configurations(Integer.parseInt(sCurrentLine.split(":")[1].trim()));
			
			//reading the memory of configurations (4th row of input)
			sCurrentLine = br.readLine();
//			System.out.println(sCurrentLine);
			
			//Loading the memory of the given instance
			givenData.setMaximum_Memory(Integer.parseInt(sCurrentLine.split(":")[1].trim()));
			
			// Skip CONFIGURATIONS_INDEXES_MATRIX:
			sCurrentLine=br.readLine();
			
			//initialization of input matrix configuration/indexes
			givenData.initialize_Configuration_Index_Matrix(givenData.getN_Configurations(), givenData.getN_Indexes());
			
			//Reading matrix that links indexes used by each configuration
			for(i = 0; i < givenData.getN_Configurations(); i++) {
				sCurrentLine=br.readLine();
				indexes=sCurrentLine.split(" ");
				for (j = 0; j < givenData.getN_Indexes(); j++) {
					givenData.getConfiguration_indexes()[i][j]=Integer.parseInt(indexes[j]);
				}
			}
			
			
			// Skip INDEXES_FIXED_COST:
			sCurrentLine=br.readLine();
			
			//initialization of input vector of indexes costs
			givenData.initialize_Indexes_Cost(givenData.getN_Indexes());
			
			//Reading indexes costs
			for(i = 0; i < givenData.getN_Indexes(); i++) {
				sCurrentLine=br.readLine();
				givenData.getIndexes_cost()[i]=Integer.parseInt(sCurrentLine.trim());
			}
			
			
			
			// Skip INDEXES_MEMORY_OCCUPATION:
			sCurrentLine=br.readLine();
			
			//initialization of input vector of indexes memory occupation
			givenData.initialize_Indexes_Memory(givenData.getN_Indexes());
			
			//Reading indexes memory occupation
			for(i = 0; i < givenData.getN_Indexes(); i++) {
				sCurrentLine=br.readLine();
				givenData.getIndexes_Memory()[i]=Integer.parseInt(sCurrentLine.trim());
			}
			
			// Skip CONFIGURATIONS_QUERIES_GAIN:
			sCurrentLine=br.readLine();
						
			//initialization of input matrix configuration/query gain
			givenData.initialize_Configuration_Query_Gain_Matrix(givenData.getN_Configurations(), givenData.getN_Queries());
			
			//Reading matrix that links indexes used by each configuration
			for(i = 0; i < givenData.getN_Configurations(); i++) {
				sCurrentLine=br.readLine();
				gains=sCurrentLine.split(" ");
				for (j = 0; j < givenData.getN_Queries(); j++) {
					givenData.getConfiguration_Query_Gain()[i][j]=Integer.parseInt(gains[j]);
				}
			}
			
			sCurrentLine=br.readLine();
			if(!sCurrentLine.equals("EOF"))
				throw new Exception("Erorr reading file: EOF not found");
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}
