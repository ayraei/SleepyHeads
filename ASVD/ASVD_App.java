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

/**
 * Makes predictions using Asymmetric SVD algorithm.
 **/
public class ASVD_App {
	/** Run parameters **/
	public final static double LEARNING_RATE = 0.002;
	public final static double REG_PENALTY = 0.04;
	public final static int NUM_EPOCHS = 30;

	/** Location of input files **/
	private static String TRAIN_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta";
	private static String TEST_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/probe.dta";

	/** Location of output file **/
	private static String OUTPUT_PREDICT_LOC =
			"/Users/debbie1/Documents/NetflixData/output/ASVD_predictions.dta";

	/** Level of reported precision (3 decimal places) **/
	public static DecimalFormat FORMAT_PRECISION = new DecimalFormat("0.000");

	/** Movie feature array **/
	public static int NUM_MOVIES = 17770;
	public static int NUM_USERS = 458293;
	public static int NUM_FEATURES = 20;
	public static double[][] q = new double[NUM_MOVIES][NUM_FEATURES];

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
				int userID = Integer.parseInt(input[0]);
				int rating = Integer.parseInt(input[3]);

				// Add to hashmap
				arrayManager.addToTraining(userID, rating);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Read in testing data to memory
		count = 0;
		try {
			while ((lineTesting = brTesting.readLine()) != null) {

				// Print progress
				if (count % 100000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array, cast to Integers
				String[] input = lineTesting.split("\\s+");
				Integer userID = Integer.parseInt(input[0]);
				Integer rating = Integer.parseInt(input[3]);

				// Add to hashmap
				arrayManager.addToTesting(userID, rating);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		// Initialize x and y arrays
		arrayManager.init_xy();

		// Initialize q array to -0.001 to 0.001
		Random r = new Random();
		for (int i = 0; i < NUM_USERS; i++) {
			for (int j = 0; j < NUM_MOVIES; j++) {
				q[i][j] = -0.001 + 0.002 * r.nextDouble();	
			}
		}

		System.out.println("done initializing data");
	}

	/** Program entry point **/
	public static void main(String[] args) {

		// Initialize arrays
		ArrayManager arrayManager = new ArrayManager();
		init(arrayManager);

		// Create buffered reader for getting reading in data
		String lineTraining;
		BufferedReader brTraining = null;
		try {
			brTraining = new BufferedReader(new FileReader(TRAIN_FILE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Run ASVD via stochastic gradient descent
		for (int e = 0; e < NUM_EPOCHS; e++) {

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
					Integer userID = Integer.parseInt(input[0]);
					Integer movieID = Integer.parseInt(input[1]);
					Integer rating = Integer.parseInt(input[3]);

					ArrayList<Integer> R_list = arrayManager.getUserHistory_train(userID);
					ArrayList<Integer> N_list = arrayManager.getUserHistory_test(userID);
					
					ArrayList<Double> xi = arrayManager.getX(userID);
					ArrayList<Double> yi = arrayManager.getY(userID);

					double R_count = Math.pow(R_list.size(), -0.5);
					double N_count = Math.pow(N_list.size(), -0.5);
					int R_sum = getSum_I(R_list);
					int N_sum = getSum_I(N_list);

					double err = rating - predictedRating(arrayManager, userID, movieID);

					for (int f = 0; f < NUM_FEATURES; f++) {

						// Update q
						q[movieID][f] += LEARNING_RATE * (err * (R_count * R_sum + N_count * N_sum) - REG_PENALTY * q[movieID][f]);

						// Update x
						for (int j = 0; j < R_list.size(); j++) {
							double newValue = LEARNING_RATE * (err * R_count - REG_PENALTY * xi.get(j));
							xi.set(j, newValue);
						}

						// Update y
						for (int j = 0; j < N_list.size(); j++) {
							double newValue = LEARNING_RATE * (err * N_count - REG_PENALTY * yi.get(j));
							yi.set(j, newValue);
						}
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
				Integer userID = Integer.parseInt(input[0]);
				Integer movieID = Integer.parseInt(input[1]);
				
				double prediction = predictedRating(arrayManager, userID, movieID);
				out.println(FORMAT_PRECISION.format(prediction));
                out.flush();
			}

		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		System.out.println("done making predictions!");
        out.close();
	}


	/** Return the predicted rating **/
	private static double predictedRating(ArrayManager arrayManager, Integer userID, Integer movieID) {
		double sum = 0;

		ArrayList<Double> xi = arrayManager.getX(userID);
		ArrayList<Double> yi = arrayManager.getY(userID);
		ArrayList<Integer> userTrainRatings = arrayManager.getUserHistory_train(userID);
		ArrayList<Integer> userTestRatings = arrayManager.getUserHistory_test(userID);

		double R = Math.pow(userTrainRatings.size(), -0.5);
		double N = Math.pow(userTestRatings.size(), -0.5);
		double yi_sum = getSum_D(yi);

		for (int j = 0; j < userTrainRatings.size(); j++) {
			sum += R * userTrainRatings.get(movieID) * xi.get(j);
		}

		return sum * N * yi_sum;
	}

	/** Return the sum of an array **/
	public static double getSum_D(ArrayList<Double> arr) {
		double sum = 0;
		for (Double rating : arr) {
			sum += rating;
		}

		return sum;
	}

	public static int getSum_I(ArrayList<Integer> arr) {
		int sum = 0;
		for (Integer rating : arr) {
			sum += rating;
		}

		return sum;
	}

}



