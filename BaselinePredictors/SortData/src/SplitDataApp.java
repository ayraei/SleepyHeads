import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Random;


/**
 *  Randomly divides up a training file into 10 files of roughly equal size.
 **/
public class SplitDataApp {
	// Fields
	/** Location of training file **/
	private static String TRAIN_ALL_FILE_LOC = 
			// "/Users/debranangel/Documents/2014-15/CNS156b/um_sorted/testingCode.dta";
			"/Users/debranangel/Documents/2014-15/CNS156b/um_sorted/trainingAll.dta";
	
	/** Number of files to split into **/
	private static int NUM_FILES = 10;
	
	/** Program entry point **/
	public static void main(String[] args) {
		String line;
		BufferedReader br = null;

		// Create buffered reader
		try {
			br = new BufferedReader(new FileReader(TRAIN_ALL_FILE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read in each line
		try {
			// Randomly pick a file
			Random generator = new Random(); 
			
			while ((line = br.readLine()) != null) {
				// Randomly select a number from [0, numFiles) with uniform distribution
				int n = generator.nextInt(NUM_FILES);
				PrintWriter out = new PrintWriter(new FileOutputStream(n + "training.dta", true));
				
				// Print the line to the output file
				System.out.println(line);
				out.println(line);
				
				// Close files
				out.close();
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

}