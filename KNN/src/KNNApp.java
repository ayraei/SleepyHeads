import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import Jama.Matrix;

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
public class KNNApp {

	/** Location of training file **/
	private static String TRAIN_FILE_LOC = 
			"/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta";

	/** Location of output files **/
	private static String OUTPUT_SIM_LOC = 
			"/Users/debbie1/Documents/NetflixData/output/sim.dta";
	private static String OUTPUT_NHBRS_LOC = 
			"/Users/debbie1/Documents/NetflixData/output/nhbrs.dta";

	/** Level of reported precision (3 decimal places) **/
	public static DecimalFormat FORMAT_PRECISION = new DecimalFormat("0.000"); 

	/** Max number of neighbors **/
	private static int K = 20;

	/** Number of movies **/
	private static int NUM_MOVIES = 17770;

	/** Arrays **/
	public HashMap<Integer, HashMap<Integer, Integer>> movieHash;

	/** Constructor **/
	public KNNApp() {
		this.movieHash = new HashMap<Integer, HashMap<Integer, Integer>>();
	}

	/** Program entry point **/
	public static void main(String[] args) {
		KNNApp app = new KNNApp();

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
				Integer userID = Integer.parseInt(input[0]);
				Integer movieID = Integer.parseInt(input[1]);
				Integer rating = Integer.parseInt(input[3]);

				HashMap<Integer, Integer> userHash;

				// Check if already exists in hash map
				if (app.movieHash.containsKey(movieID)) {
					userHash = app.movieHash.get(movieID);
				}

				else 
				{
					userHash = new HashMap<Integer, Integer>();
				}

				// Put new user, ratings in
				userHash.put(userID, rating);
				app.movieHash.put(movieID, userHash);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("done reading in data");

		// Calculate similarities with multi-threading
		ExecutorService executor = Executors.newFixedThreadPool(10);
		for (int t = 0; t < 10; t++) {
			Runnable worker = new EucDist(app, t * 1777, (t + 1) * 1777 - 1, t);
			executor.execute(worker);
		}
		executor.shutdown();
		while (!executor.isTerminated()) {
		}

		System.out.println("done calculating similarities");

		// Identify neighbors


		// Output neighbors to text file


		/*
		// Print the line to the output file
		PrintWriter out = new PrintWriter(new FileOutputStream("training.dta", true));
		out.println(line);

		// Close files
		out.close();*/

	}
}
