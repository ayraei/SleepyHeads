import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import Jama.Matrix;

/**
 * Makes predictions using SVD++ algorithm.
 **/
public class SVDPP_App {
	/** Run parameters **/
	public final static double LEARNING_RATE = 0.0002;
	public final static double REG_PENALTY = 0.04;
	public final static int NUM_EPOCHS = 2;

	/** Location of input files **/
	private static String TRAIN_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta";
	private static String TEST_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/probe.dta";

	/** Location of output file **/
	private static String OUTPUT_PREDICT_LOC =
			"/Users/debbie1/Documents/NetflixData/output/SVDPP_predictions_probe.dta";
	private static String OUTPUT_PERF_LOC =
			"/Users/debbie1/Documents/NetflixData/output/SVDPP_performance.dta";

	/** Level of reported precision (3 decimal places) **/
	public static DecimalFormat FORMAT_PRECISION = new DecimalFormat("0.000");

	/** Movie feature array **/
	public static int NUM_USERS = 458293;
	public static int NUM_FEATURES = 20;
	public static int NUM_MOVIES = 17770;
	public static Matrix q = new Matrix(NUM_MOVIES, NUM_FEATURES);    // auto initializes to zero
	public static Matrix p = new Matrix(NUM_USERS, NUM_FEATURES);
	public static Matrix y = new Matrix(NUM_MOVIES, NUM_FEATURES);

	/** Checking progress **/
	private final static CalcStatistics statistics = new CalcStatistics();  // used to track total run time and % complete

	/** Constructor **/
	public SVDPP_App() {
	}

	/** Initialize arrays from files **/
	public static void init(ArrayManager arrayManager) {

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

		// Initialize matrices to -0.001 to 0.001
		Random r = new Random();
		for (int i = 0; i < NUM_MOVIES; i++) {
			for (int j = 0; j < NUM_FEATURES; j++) {
				q.getArray()[i][j] = -0.001 + 0.002 * r.nextDouble();
				y.getArray()[i][j] = -0.001 + 0.002 * r.nextDouble();
			}
		}

		for (int i = 0; i < NUM_USERS; i++) {
			for (int j = 0; j < NUM_FEATURES; j++) {
				p.getArray()[i][j] = -0.001 + 0.002 * r.nextDouble();
			}
		}

		System.out.println("done initializing data\n");
	}

	/** Program entry point **/
	public static void main(String[] args) {

		// Initialize arrays
		ArrayManager arrayManager = new ArrayManager();
		init(arrayManager);

		// Prepare to print out performance
		PrintWriter outperf = null;
		try {
			outperf = new PrintWriter(new FileOutputStream(OUTPUT_PERF_LOC, true));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Constants
		int maxIndex = NUM_FEATURES - 1;
		double LRtRP = LEARNING_RATE * REG_PENALTY;

		// Create buffered reader for getting reading in data
		String lineTraining;
		BufferedReader brTraining = null;

		// Run ASVD via stochastic gradient descent TODO: PARALLIZE UPDATES
		for (int e = 0; e < NUM_EPOCHS; e++) {

			// Print progress
			System.out.println("Epoch: " + e);

			try {
				brTraining = new BufferedReader(new FileReader(TRAIN_FILE_LOC));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			// Read in training data to memory
			try {
				while ((lineTraining = brTraining.readLine()) != null) {

					// Read in data as a string array, cast to Integers
					String[] input = lineTraining.split("\\s+");
					Integer userID = Integer.parseInt(input[0]) - 1;
					Integer movieID = Integer.parseInt(input[1]) - 1;
					Integer rating = Integer.parseInt(input[3]);

					// Set up
					double N = arrayManager.getN(userID);
					ArrayList<RateUnit> N_list = arrayManager.getUserHistory_N(userID);
					Matrix q_i = q.getMatrix(movieID, movieID, 0, maxIndex);
					Matrix p_u = p.getMatrix(userID, userID, 0, maxIndex);
					Matrix y_sum = new Matrix(1, NUM_FEATURES);					

					for (RateUnit ru : N_list) {
						int movie = ru.getID();

						for (int f = 0; f < maxIndex; f++) {
							y_sum.set(0, f, y_sum.get(0, f) + y.get(movie, f));
						}
					}
					y_sum.timesEquals(N);

					// Calculate error
					double predictedRating = q_i.times((y_sum.plusEquals(p_u)).transpose()).get(0, 0);
					double err = rating - predictedRating;
					if (movieID % 100 == 0) {
						outperf.println(userID + " " + movieID + " " + rating + " " + err);
						outperf.flush();
					}

					double LRtE  = LEARNING_RATE * err;

					// Update q
					// q_i = q_i + LEARNING_RATE * err * (p_u + y_sum) - LEARNING_RATE * REG_PENALTY * q_i
					Matrix q_inew = q_i.plus(y_sum.timesEquals(LRtE).minusEquals(q_i.times(LRtRP)));
					q.setMatrix(movieID, movieID, 0, maxIndex, q_inew);

					// Update p
					// p_u = p_u + LEARNING_RATE * err * q_i - LEARNING_RATE * REG_PENALTY * p_u
					p_u.plusEquals(q_i.times(LRtE).minus(p_u.times(LRtRP)));
					p.setMatrix(userID, userID, 0, maxIndex, p_u);

					// Update y
					// y_j = y_j + q_i * LEARNING_RATE * err * N - LEARNING_RATE * REG_PENALTY * y_j
					q_i.timesEquals(LRtE * N);
					for (RateUnit ru : N_list) {
						int movie = ru.getID();

						for (int f = 0; f < maxIndex; f++) {
							y.set(movie, f, y.get(movie, f) + q_i.get(0, f) - (LRtRP * y.get(movie, f)));
						}
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		outperf.close();

		// Delete if output file already exist, otherwise will append to file
		try{
			File simFile = new File(OUTPUT_PREDICT_LOC);
			if (simFile.exists()) {
				simFile.delete();
			}
		} catch(Exception e){
			e.printStackTrace();
		}

		// Prepare to print out prediction
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileOutputStream(OUTPUT_PREDICT_LOC, true));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Make predictions TODO: PARALLIZE PREDICTIONS
		String lineTesting;
		BufferedReader brTesting = null;
		try {
			brTesting = new BufferedReader(new FileReader(TEST_FILE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		int count = 0;
		try {
			while ((lineTesting = brTesting.readLine()) != null) {

				// Print progress
				if (count % 10000000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array, cast to Integers
				String[] input = lineTesting.split("\\s+");
				Integer userID = Integer.parseInt(input[0]) - 1;
				Integer movieID = Integer.parseInt(input[1]) - 1;

				double prediction = predictedRating(arrayManager, userID, movieID);
				out.println(FORMAT_PRECISION.format(prediction));
				out.flush();
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}

		System.out.println("done making predictions!\n");
		out.close();
	}


	/** Return the predicted rating **/
	private static double predictedRating(ArrayManager arrayManager, Integer userID, Integer movieID) {

		ArrayList<RateUnit> userTestRatings = arrayManager.getUserHistory_N(userID);
		double N = arrayManager.getN(userID);

		// r hat = q[movie] * (pu + N * sum(yj))
		int max = NUM_FEATURES - 1;
		Matrix q_i = q.getMatrix(movieID, movieID, 0, max);
		Matrix y_sum = new Matrix(1, NUM_FEATURES);

		for (RateUnit nu : userTestRatings) {
			int movie = nu.getID();
			y_sum.plusEquals(y.getMatrix(movie, movie, 0, max));
		}
		y_sum.times(N);

		Matrix ans = q_i.arrayTimes((p.getMatrix(userID, userID, 0, max).plus(y_sum)).transpose());

		return ans.get(0, 0);
	}

}

