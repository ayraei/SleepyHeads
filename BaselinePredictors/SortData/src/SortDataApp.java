import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

/** 
 * Sorts all the data into different files based on its label: training, 
 * probe, or test.
 **/
public class SortDataApp {
	/** Location of index file **/
	private static String INDEX_FILE_LOC = 
			"/Users/debranangel/Documents/2014-15/CNS156b/um/all.idx";

	/** Location of data file **/
	private static String DATA_FILE_LOC = 
			"/Users/debranangel/Documents/2014-15/CNS156b/um/all.dta";

	/** Training set prefix **/
	private static String TRAIN_FILE = "training.dta";

	/** Probe set prefix **/
	private static String PROBE_FILE = "probe.dta";

	/** Testing set prefix **/
	private static String TEST_FILE = "test.dta";

	/** Program entry point **/
	public static void main(String[] args) {
		BufferedReader iBr = null;
		BufferedReader dBr = null;

		// Read from file of indices and sort data into output files
		String data;
		Integer i;

		try {
			iBr = new BufferedReader(new FileReader(INDEX_FILE_LOC));
			dBr = new BufferedReader(new FileReader(DATA_FILE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			PrintWriter trnOut = new PrintWriter(TRAIN_FILE);
			PrintWriter prbOut = new PrintWriter(PROBE_FILE);
			PrintWriter tstOut = new PrintWriter(TEST_FILE);

			while ((data = dBr.readLine()) != null) {
				System.out.println(data);
				i =  Integer.parseInt(iBr.readLine());
				switch(i) {
				case 1: 
					trnOut.println(data);
					break;

				case 2:
					trnOut.println(data);
					break;

				case 3:
					trnOut.println(data);
					break;

				case 4:
					prbOut.println(data);
					break;

				case 5:
					tstOut.println(data);
					break;
				}
			}

			// Close files
			trnOut.close();
			prbOut.close();
			tstOut.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}