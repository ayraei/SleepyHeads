import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class FindNhbrsApp {
	/** Location of input files **/
	private static String TRAIN_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta";
	private static String SIM_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/output/sim_pearson.dta";
	private static String AVG_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/output/avgMovies.dta";
	private static String COMMONV_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/output/commonViewers.dta";
	private static String TEST_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/test.dta";

	/** Location of output files **/
	private static String OUTPUT_PREDICT_LOC =
			"/Users/debbie1/Documents/NetflixData/output/KNN_predictions.dta";

	/** Level of reported precision (3 decimal places) **/
	public static DecimalFormat FORMAT_PRECISION = new DecimalFormat("0.000");

	/** Max number of neighbors and min num of common viewers **/
	private static int K = 20;
	private static int minCV = 16;

	/** Number of movies **/
	public static int NUM_MOVIES = 17770;
	
	/** Arrays **/
	public static float[][] sims = new float[NUM_MOVIES + 1][NUM_MOVIES + 1];
	public static int[][] cv = new int[NUM_MOVIES + 1][NUM_MOVIES + 1];
	public static float[] movieAvgs = new float[NUM_MOVIES + 1];

	/** Program entry point **/
	public static void main(String[] args) {
		MovieManager movieManager = new MovieManager();

		// Delete if file already exists, otherwise will append to file
		try{
			File simFile = new File(OUTPUT_PREDICT_LOC);
			if (simFile.exists()) {
				simFile.delete();
			}
		} catch(Exception e){
			e.printStackTrace();
		}

// Create hashmaps ============================================================

		// Create buffered reader for getting reading in data
		String lineHash;
		BufferedReader brHash = null;
		try {
			brHash = new BufferedReader(new FileReader(TRAIN_FILE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read in each line to store data to memory
		int count = 0;
		try {
			while ((lineHash = brHash.readLine()) != null) {

				// Print progress
				if (count % 10000000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array, cast to Integers
				String[] input = lineHash.split("\\s+");
				int userID = Integer.parseInt(input[0]);
				int movieID = Integer.parseInt(input[1]);
				int rating = Integer.parseInt(input[3]);

				// Fill in the hash maps
				// Use rating - 1 because indexing from 0 - 4, but rating is from 1 - 5
				movieManager.add(movieID, userID, rating - 1);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("done creating hashmaps\n");

// Load Sims and CVs ==========================================================

		// Create buffered reader for getting reading in data
		String lineSims;
		String lineCV;
		BufferedReader brSims = null;
		BufferedReader brCV = null;
		try {
			brSims = new BufferedReader(new FileReader(SIM_FILE_LOC));
			brCV = new BufferedReader(new FileReader(COMMONV_FILE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read in each line to store data to memory
		count = 0;
		try {
			while ((lineSims = brSims.readLine()) != null) {
				lineCV = brCV.readLine();
				
				// Print progress
				if (count % 1000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array
				String[] input = lineSims.split("\\s+");
				String[] input2 = lineCV.split("\\s+");
				int movieID = Integer.parseInt(input[0]);

				for (int i = 0; i < NUM_MOVIES; i++) {
					sims[movieID][i] = Float.parseFloat(input[i]);
					cv[movieID][i] = Integer.parseInt(input2[i]);
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("done loading sims and cv's into arrays \n");

// Load movie avgs ============================================================
		// Create buffered reader for getting reading in data
		String lineAvg;
		BufferedReader brAvg = null;
		try {
			brAvg = new BufferedReader(new FileReader(AVG_FILE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read in each line to store data to memory
		count = 0;
		try {
			while ((lineAvg = brAvg.readLine()) != null) {

				// Print progress
				if (count % 2000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array
				String[] input = lineAvg.split("\\s+");
				int movieID = Integer.parseInt(input[0]);
				movieAvgs[movieID] = Float.parseFloat(input[1]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("done creating avg array\n");

// Make Predictions ===========================================================
		// Prepare to print out prediction
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileOutputStream(OUTPUT_PREDICT_LOC, true));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}
				
		// Create buffered reader for getting reading in test data
		String lineTest;
		BufferedReader brTest = null;
		try {
			brTest = new BufferedReader(new FileReader(TEST_FILE_LOC));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read in each line to figure out what we want to predict
		count = 0;
		try {
			while ((lineTest = brTest.readLine()) != null) {
				//System.out.println(lineTest);
				
				// Print progress
				if (count % 200000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array
				String[] input = lineTest.split("\\s+");
				int userID = Integer.parseInt(input[0]);
				int targetMovie = Integer.parseInt(input[1]);
				
				List<RateUnit> movieHistory = movieManager.getMoviesByUserId(userID);
				ArrayList<MovieNeighbor> nhbrs = new ArrayList<MovieNeighbor>();
				
				for (RateUnit movie : movieHistory) {
					if (cv[targetMovie][movie.getID()] > minCV) {
						
						MovieNeighbor n = new MovieNeighbor();
						
						n.setCV(cv[targetMovie][movie.getID()]);
						n.setMAvg(movieAvgs[targetMovie]);
						n.setNAvg(movieAvgs[movie.getID()]);
						n.setNRating(movie.getRating());
						n.setRRaw(sims[targetMovie][movie.getID()]);
						n.calcRLower();
						n.calcWeight();
						
						nhbrs.add(n);
						
					} else {
						continue;
					}
				}
				
				// Create dummy neighbor for prediction
				MovieNeighbor d = new MovieNeighbor();
				d.setMAvg(movieAvgs[targetMovie]);
				d.setNAvg(0);
				d.setWeight((float) Math.log(minCV));
				
				nhbrs.add(d);
				
				// Pick at most K number neighbors with greatest weights
				Collections.sort(nhbrs, new CompNhbrs());
				
				int total;
				if (nhbrs.size() < K) {
					total = nhbrs.size();
				} else {
					total = K;
				}
				
				// Get predictions from neighbors
				float prediction = 0;
				for (int q = 0; q < total; q++) {
					float dif = nhbrs.get(q).getNRating() - nhbrs.get(q).getNAvg();
					
				    if (nhbrs.get(q).getRRaw() < 0) {
				    	dif = -dif;
				    }
				    prediction += nhbrs.get(q).getMAvg() + dif;
				}
				
				prediction = prediction / total;			
				out.println(FORMAT_PRECISION.format(prediction));
				out.flush();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("done making predictions!");
	}
}
