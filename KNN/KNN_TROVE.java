import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;

import gnu.trove.iterator.TIntObjectIterator;
import gnu.trove.map.hash.TIntIntHashMap;
import gnu.trove.map.hash.TIntObjectHashMap;

/** 
 * This application provides the implementation for finding at most k number
 * of nearest neighbors (kNN) for each movie item. This application has a few
 * improvements on top of the vanilla kNN model:
 *      1. Distance Metric: Mahalanobis Distance
 *      2. Cost Function: Large Margin KNN
 *      3. Distance Weighting
 * It saves the calculated similarity matrix and the list of neighbors into
 * the respective output files: sim.dta and nhbrs.dta.
 */
public class KNN_TROVE {

	/** Location of training file **/
	private static String TRAIN_FILE_LOC = 
			"/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta";

	/** Max number of neighbors **/
	private static int K = 20;

	/** Number of movies **/
	private static int NUM_MOVIES = 1770;

	/** Arrays **/
	private TIntObjectHashMap<TIntIntHashMap> movieHash;
	private int[][] sim;
	private int[][] nhbr;

	/** Location of output files **/
	private static String OUTPUT_SIM_LOC = 
			"/Users/debranangel/Documents/2014-15/NetflixData/output/sim.dta";
	private static String OUTPUT_NHBRS_LOC = 
			"/Users/debranangel/Documents/2014-15/NetflixData/output/nhbrs.dta";

	/** Constructor **/
	public KNN_TROVE() {
		this.movieHash = new TIntObjectHashMap<TIntIntHashMap>();
	}

	/** Program entry point **/
	public static void main(String[] args) {
		KNN_TROVE app = new KNN_TROVE();

		String line;
		BufferedReader br = null;


		// Create buffered reader
		try {
			br = new BufferedReader(new FileReader(TRAIN_FILE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read in each line to store data to memory
		int count = 0;
		try {
			while ((line = br.readLine()) != null) {

				// Print progress
				if (count % 10000000 == 0) {
					System.out.println(count);
				}
				count++;		

				// Read in data as a string array, cast to Integers
				String[] input = line.split("\\s+");
				int userID = Integer.parseInt(input[0]);
				int movieID = Integer.parseInt(input[1]);
				int rating = Integer.parseInt(input[3]);

				TIntIntHashMap userHash;

				// Check if already exists in hash map
				if (app.movieHash.containsKey(movieID)) {
					userHash = app.movieHash.get(movieID);
				}

				else 
				{
					userHash = new TIntIntHashMap();
				}

				// Put new user, ratings in
				userHash.put(userID, rating);
				app.movieHash.put(movieID, userHash);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Calculate similarities
		for (TIntObjectIterator it1 = app.movieHash.iterator(); it1.hasNext();) {
			it1.advance();
			
			// Get the users for this movie
			Collection<TIntIntHashMap> m1 = app.movieHash.valueCollection();


			for (TIntObjectIterator it2 = app.movieHash.iterator(); it2.hasNext();) {
				it2.advance();
				Object h2 = it2.value();
				
				
				
			}
		}

		// Output similarities to text file


		// Identify neighbors


		// Output neighbors to text file



		
		// Print the line to the output file
//		PrintWriter out = new PrintWriter(new FileOutputStream("training.dta", true));
//		out.println(line);
//
//		// Close files
//		out.close();

	}
}
