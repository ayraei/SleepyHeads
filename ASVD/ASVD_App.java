import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import Jama.Matrix;

/**
 * Makes predictions using Asymmetric SVD algorithm.
 **/
public class ASVD_App {
	/** Run parameters **/
	public final static double LEARNING_RATE = 0.0005;
	public final static double REG_PENALTY = 0.04;
	public final static int NUM_EPOCHS = 2;

	/** Location of input files **/
	private static String TRAIN_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta";
	private static String TEST_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/probe.dta";

	/** Location of output file **/
	private static String OUTPUT_PREDICT_LOC =
			"/Users/debbie1/Documents/NetflixData/output/ASVD_predictions_probe.dta";
	private static String OUTPUT_PERF_LOC =
			"/Users/debbie1/Documents/NetflixData/output/performance.dta";

	/** Level of reported precision (3 decimal places) **/
	public static DecimalFormat FORMAT_PRECISION = new DecimalFormat("0.000");

	/** Movie feature array **/
	public static int NUM_USERS = 458293;
	public static int NUM_FEATURES = 20;
	public static int NUM_MOVIES = 17770;
	public static Matrix q = new Matrix(NUM_MOVIES, NUM_FEATURES);    // auto initializes to zero
	public static Matrix x = new Matrix(NUM_MOVIES, NUM_FEATURES);
	public static Matrix y = new Matrix(NUM_MOVIES, NUM_FEATURES);

	/** Multi-threading objects **/
	private final static int QUEUE_CAPACITY = 400;
	private final static BlockingQueue<CalcData> queue = new LinkedBlockingQueue<CalcData>(QUEUE_CAPACITY);
	private final static int THREAD_COUNT = 8;
	private final static CalcStatistics statistics = new CalcStatistics();  // used to track total run time and % complete

	/** Constructor **/
	public ASVD_App() {
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

		// Initialize q matrix to -0.001 to 0.001
		Random r = new Random();
		for (int i = 0; i < NUM_MOVIES; i++) {
			for (int j = 0; j < NUM_FEATURES; j++) {
				q.getArray()[i][j] = -0.001 + 0.002 * r.nextDouble();
				x.getArray()[i][j] = -0.001 + 0.002 * r.nextDouble();
				y.getArray()[i][j] = -0.001 + 0.002 * r.nextDouble();
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
		
		int maxIndex = NUM_FEATURES - 1;
		
		// Create buffered reader for getting reading in data
		String lineTraining;
		BufferedReader brTraining = null;
		
		// Run ASVD via stochastic gradient descent TODO: PARALLIZE UPDATES
		for (int e = 0; e < NUM_EPOCHS; e++) {

			// Print progress
			if (e % 10 == 0) {
				System.out.println(e);
			}

			try {
				brTraining = new BufferedReader(new FileReader(TRAIN_FILE_LOC));
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}

			statistics.setTotalCount(98291669);
			
			// Read in training data to memory
			try {
				while ((lineTraining = brTraining.readLine()) != null) {
					//statistics.incAndGet();

					// Read in data as a string array, cast to Integers
					String[] input = lineTraining.split("\\s+");
					Integer userID = Integer.parseInt(input[0]) - 1;
					Integer movieID = Integer.parseInt(input[1]) - 1;
					Integer rating = Integer.parseInt(input[3]);

					ArrayList<RateUnit> R_list = arrayManager.getUserHistory_R(userID);
					ArrayList<RateUnit> N_list = arrayManager.getUserHistory_N(userID);

					double R = arrayManager.getR(userID);
					double N = arrayManager.getN(userID);

					Matrix x_sum = new Matrix(1, NUM_FEATURES);
					Matrix y_sum = new Matrix(1, NUM_FEATURES);

					for (RateUnit ru : R_list) {
						Matrix x_i = x.getMatrix(ru.getID(), ru.getID(), 0, maxIndex);
						x_sum.plusEquals(x_i.timesEquals(ru.getRating()));
					}
					x_sum.timesEquals(R);					
					
					for (RateUnit ru : N_list) {
						Matrix y_i = y.getMatrix(ru.getID(), ru.getID(), 0, maxIndex);
						y_sum.plusEquals(y_i);
					}
					y_sum.timesEquals(N);
					
					Matrix q_i = q.getMatrix(movieID, movieID, 0, maxIndex);
					Matrix XYsum = x_sum.plus(y_sum);
					double predictedRating = q_i.times(XYsum.transpose()).get(0,0);;
					double err = rating - predictedRating;
					
					if (movieID % 100 == 0) {
						//System.out.println(statistics.toString());
						outperf.println(userID + " " + movieID + " " + rating + " " + err);
						outperf.flush();
					}

					// Update q
					double lrte = LEARNING_RATE * err;
					Matrix c = XYsum.times(lrte);
					q_i.plusEquals(c.minus(q_i.times(REG_PENALTY)));
					q.setMatrix(movieID, movieID, 0, maxIndex, q_i);

					// Update x
					Matrix c1 = q_i.times(lrte * R * arrayManager.getRSum(userID));
					for (RateUnit ru : R_list) {
						int movie = ru.getID();
						
						// x_i += q_i * LEARNING_RATE * err * R * sum(r_ui) - REG_PENALTY * x_i
						for (int f = 0; f < maxIndex; f++) {
							x.set(movie, f, x.get(movie, f) + c1.get(0, f) - (x.get(movie, f) * REG_PENALTY));
						}
						
						//Matrix x_i = x.getMatrix(movie, movie, 0, maxIndex);
						//x_i.plusEquals(c1.minus(x_i.times(REG_PENALTY)));
						//x.setMatrix(movie, movie, 0, maxIndex, x_i);
					}

					// Update y
					Matrix c2 = q_i.times(lrte * N);
					for (RateUnit ru : N_list) {
						int movie = ru.getID();
						
						// x_i += q_i * LEARNING_RATE * err * R * sum(r_ui) - REG_PENALTY * x_i
						for (int f = 0; f < maxIndex; f++) {
							y.set(movie, f, y.get(movie, f) + c2.get(0, f) - (y.get(movie, f) * REG_PENALTY));
						}
						
						// y_i += q_i * LEARNING_RATE * err * N - REG_PENALTY * y_i
						//Matrix y_i = y.getMatrix(movie, movie, 0, maxIndex);
						//y_i.plusEquals(c2.minus(y_i.times(REG_PENALTY)));
						//y.setMatrix(movie, movie, 0, maxIndex, y_i);
					}
				}
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

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
		outperf.close();
	}


	/** Return the predicted rating **/
	private static double predictedRating(ArrayManager arrayManager, Integer userID, Integer movieID) {

		ArrayList<RateUnit> userTrainRatings = arrayManager.getUserHistory_R(userID);
		ArrayList<RateUnit> userTestRatings = arrayManager.getUserHistory_N(userID);
		
		assert(userTrainRatings.size() > 0);
		
		double R = arrayManager.getR(userID);
		double N = arrayManager.getN(userID);

		// r hat = q[movie] * (R * sum((ruj - buj) * xj) + N * sum(yj))
		int max = NUM_FEATURES - 1;
		Matrix q_i = q.getMatrix(movieID, movieID, 0, max);
		Matrix temp1 = new Matrix(1, NUM_FEATURES);
		Matrix temp2 = new Matrix(1, NUM_FEATURES);

		for (RateUnit ru : userTrainRatings) {
			int movie = ru.getID();
			Matrix x_i = x.getMatrix(movie, movie, 0, max);

			temp1.plusEquals(x_i.times(ru.getRating()));
		}
		temp1.times(R);
		
		for (RateUnit nu : userTestRatings) {
			int movie = nu.getID();
			Matrix y_i = y.getMatrix(movie, movie, 0, max);

			temp2.plusEquals(y_i);
		}
		temp2.times(N);
		
		Matrix ans = q_i.arrayTimes((temp1.plus(temp2)));
		
		return ans.get(0, 0);
	}

	/** Return the sum of arrays **/
	public static double getSum_D(double[][] arr) {
		double sum = 0;
		for (int i = 0; i < arr.length; i++) {
			sum += arr[0][i];
		}

		return sum;
	}

	public static int getSum_R(ArrayList<RateUnit> arr) {
		int sum = 0;
		for (RateUnit ru : arr) {
			sum += ru.getRating();
		}

		return sum;
	}

}

