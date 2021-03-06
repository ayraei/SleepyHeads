import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

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
public class CopyOfKNNApp {

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
	public static int NUM_MOVIES = 17770;

	/** Arrays **/
	public HashMap<Integer, HashMap<Integer, Integer>> movieHash;

	private final static int QUEUE_CAPACITY = 400;
	private final static BlockingQueue<CopyOfCalcData> queue = new LinkedBlockingQueue<CopyOfCalcData>(QUEUE_CAPACITY);
	private final static CalcStatistics statistics = new CalcStatistics();
	private final static int THREAD_COUNT = 8;

	/** Constructor **/
	public CopyOfKNNApp() {
		this.movieHash = new HashMap<Integer, HashMap<Integer, Integer>>();
	}

	/** Program entry point **/
	public static void main(String[] args) {
		CopyOfKNNApp app = new CopyOfKNNApp();

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

		// Use a sorted set to guarantee the order
		SortedSet<Integer> sortedMoveIdSet = new TreeSet<Integer>();
		sortedMoveIdSet.addAll(app.movieHash.keySet());

		int rows = sortedMoveIdSet.size();
		int totalcount = (rows +  1) * rows / 2;
		statistics.setTotalCount(totalcount);

		Matrix results = new Matrix(rows, rows);

		// Calculate similarities with multi-threading
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		for (int t = 0; t < THREAD_COUNT; t++) {
			Runnable worker = new EucDist(t, queue, results, statistics);
			executor.execute(worker);
		}

		// Fill queue
		int row = 0;
		Iterator<Integer> m1Iterator = sortedMoveIdSet.iterator();
		
		while (m1Iterator.hasNext()) {
			int column = 0;
			HashMap<Integer, Integer> v = app.movieHash.get(m1Iterator.next());
			Iterator<Integer> m2Iterator = sortedMoveIdSet.iterator();
			
			while(m2Iterator.hasNext()) {
				HashMap<Integer, Integer> u = app.movieHash.get(m2Iterator.next());
				
				if (column >= row) {
					CopyOfCalcData calcData = new CopyOfCalcData(row, column, u, v);
					
					try {
						queue.put(calcData);
						
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
				column++;
			}
			row++;
			System.out.println(statistics.toString());
		}
		
		// Mark end of data
		for (int t = 0; t < THREAD_COUNT; t++) {
			CopyOfCalcData calcData = new CopyOfCalcData(-1, -1, null, null);
			
			try {
				queue.put(calcData);
				
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();
		
		try {
			// Print statistics every 5 seconds
			while (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
				System.out.println(statistics.toString());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		System.out.println(statistics.toString());
		System.out.println("done calculating sim");

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

		// Write data to file
		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_SIM_LOC, true)));

			for (int i = 0; i < rows; ++i) {
				for (int j = 0; j < rows; ++j) {
					out.print(CopyOfKNNApp.FORMAT_PRECISION.format(results.get(i, j)) + " ");
				}
				
				// Make a new line for next movie
				out.println(" ");
			}
			
			// Close the file, auto flushes
			out.close();
			
		} catch (IOException e) {
			e.printStackTrace();
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