import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;


public class CalcConstantsApp {
	/** Location of input files **/
	private static String TRAIN_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta";
	private static String TEST_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/probe.dta";

	/** Location of output file **/
	private static String OUTPUT_CONST_LOC =
			"/Users/debbie1/Documents/NetflixData/output/ASVD_constants_probe.dta";
	
	public static int NUM_USERS = 458293;

	public static void main(String[] args) {

		ArrayManager arrayManager = new ArrayManager();

		// Create buffered reader for getting reading in data
		String lineTraining, lineTesting;
		BufferedReader brTraining = null;
		BufferedReader brTesting = null;
		try {
			brTraining = new BufferedReader(new FileReader(TRAIN_FILE_LOC));
			brTesting = new BufferedReader(new FileReader(TEST_FILE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read in training data to memory
		int count = 0;
		try {
			while ((lineTraining = brTraining.readLine()) != null) {

				// Print progress
				if (count % 10000000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array, cast to Integers
				String[] input = lineTraining.split("\\s+");
				int userID = Integer.parseInt(input[0]) - 1;
				int movieID = Integer.parseInt(input[1]) - 1;
				int rating = Integer.parseInt(input[3]);

				// Add to hashmap
				arrayManager.add(userID, movieID, rating);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Read in testing data to memory
		count = 0;
		try {
			while ((lineTesting = brTesting.readLine()) != null) {

				// Print progress
				if (count % 500000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array, cast to Integers
				String[] input = lineTesting.split("\\s+");
				int userID = Integer.parseInt(input[0]) - 1;
				int movieID = Integer.parseInt(input[1]) - 1;
				int rating = 0; //Integer.parseInt(input[3]);

				// Add to hashmap
				arrayManager.add(userID, movieID, rating);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Initialize constant matrices
		arrayManager.initConstants();

		// Prepare to print out performance
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileOutputStream(OUTPUT_CONST_LOC, true));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Print out constants
		for (int f = 0; f < NUM_USERS; f++) {
			out.println(f + " " + arrayManager.getR(f) + " " + arrayManager.getN(f) + " " + arrayManager.getRSum(f));
		}
		out.close();
		
		System.out.println("Done printing constants!");

	}

}
