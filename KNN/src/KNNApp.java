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
import java.util.Set;

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
	private HashMap<Integer, HashMap<Integer, Integer>> movieHash;

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

		// Delete if sim file already exists
		// If don't delete, then will only append to file
		try{
			File simFile = new File(OUTPUT_SIM_LOC);
			if (simFile.exists()) {
				simFile.delete();
			}   
		} catch(Exception e){
			e.printStackTrace();
		}

		// Calculate similarities
		count = 0;
		try {
			PrintWriter out = new PrintWriter (new BufferedWriter
							 (new FileWriter(OUTPUT_SIM_LOC, true)));

			for (HashMap<Integer, Integer> m1 : app.movieHash.values()) {

				// Print progress
				if (count % 2000 == 0) {
					System.out.println(count);
				}
				count++;

				Set<Integer> m1_users = m1.keySet();

				for (HashMap<Integer, Integer> m2 : app.movieHash.values()) {

					// Find intersection between user sets
					Set<Integer> user_intersect = new HashSet<Integer>();
					user_intersect.addAll(m1_users);
					Set<Integer> m2_users = m2.keySet();
					user_intersect.retainAll(m2_users);

					// Extract the vectors of ratings from the overlapping users			
					int size = user_intersect.size();
					Matrix u = new Matrix(1, size);
					Matrix v = new Matrix(size, 1);

					int i = 0;
					double uSum = 0;
					double vSum = 0;
					for (int ui : user_intersect) {
						u.set(0, i, m1.get(ui));
						v.set(i, 0, m2.get(ui));

						uSum += u.get(0, i);
						vSum += v.get(i, 0);

						i++;
					}

					// Calculate similarity
					// 1-(u-Mean[u]).(v-Mean[v])/(Norm[u-Mean[u]]Norm[v-Mean[v]])
					double numer;
					double denom;
					Matrix uAvg = new Matrix(1, size, uSum / size);
					Matrix vAvg = new Matrix(size, 1, vSum / size);
					u.minusEquals(uAvg);
					v.minusEquals(vAvg);

					numer = u.times(v).get(0, 0);
					denom = u.normF() * v.normF();

					double sim = 1 - (numer / denom);

					// Output similarities to text file
					out.print(FORMAT_PRECISION.format(sim) + " ");
				}

				// Start a new line for next movie
				out.println(" ");
				break;
			}

			// Close the file
			out.close();

		} catch (IOException e1) {
			e1.printStackTrace();
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