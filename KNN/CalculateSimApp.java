import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.*;

/**
 * This application provides the implementation for finding at most k number
 * of nearest neighbors (kNN) for each movie item. This application uses the
 * Pearson coefficient as the distance metric. It saves the calculated 
 * similarity matrix to sim_pearson.dta and the number of common viewers per
 * movie pair to commonViewers.dta.
 */
public class CalculateSimApp {

	/** Location of training file **/
	private static String TRAIN_FILE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/trainingAll.dta";

	/** Location of output file **/
	private static String OUTPUT_SIM_LOC =
			"/Users/debbie1/Documents/NetflixData/output/sim_pearson.dta";
	private static String OUTPUT_NUM_COMMON_VIEWERS_LOC =
			"/Users/debbie1/Documents/NetflixData/output/commonViewers.dta";
	private static String OUTPUT_SUM_COMMON_VIEWERS_LOC =
			"/Users/debbie1/Documents/NetflixData/output/commonViewers_sums.dta";

	/** Level of reported precision (3 decimal places) **/
	public static DecimalFormat FORMAT_PRECISION = new DecimalFormat("0.000");

	/** Number of movies **/
	public static int NUM_MOVIES = 17770;

	/** Multi-threading objects **/
	private final static int QUEUE_CAPACITY = 400;
	private final static BlockingQueue<CalcData> queue = new LinkedBlockingQueue<CalcData>(QUEUE_CAPACITY);
	private final static int THREAD_COUNT = 8;
	private final static CalcStatistics statistics = new CalcStatistics();  // used to track total run time and % complete

	/** Constructor **/
	public CalculateSimApp() {
	}

	/** Program entry point **/
	public static void main(String[] args) {
		MovieManager movieManager = new MovieManager();

		// Delete if output files already exist, otherwise will append to files
		try{
			File simFile = new File(OUTPUT_SIM_LOC);
			File ncmFile = new File(OUTPUT_NUM_COMMON_VIEWERS_LOC);
			File scmFile = new File(OUTPUT_SUM_COMMON_VIEWERS_LOC);
			if (simFile.exists()) {
				simFile.delete();
			}
			if (ncmFile.exists()) {
				ncmFile.delete();
			}
			if (scmFile.exists()) {
				scmFile.delete();
			}
		} catch(Exception e){
			e.printStackTrace();
		}

		// Create buffered reader for getting reading in data
		String line;
		BufferedReader br = null;
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

				// Fill in the hash maps
				// Use rating - 1 because indexing from 0 - 4, but rating is from 1 - 5
				movieManager.add(movieID, userID, rating - 1);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("done reading in data");

		// Prepare to print out similarities
		PrintWriter out = null;
		PrintWriter out2 = null;
		PrintWriter out3 = null;
		try {
			out = new PrintWriter(new FileOutputStream(OUTPUT_SIM_LOC, true));
			out2 = new PrintWriter(new FileOutputStream(OUTPUT_NUM_COMMON_VIEWERS_LOC, true));
			out3 = new PrintWriter(new FileOutputStream(OUTPUT_SUM_COMMON_VIEWERS_LOC, true));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Use a sorted set to guarantee the order of movies
		SortedSet<Integer> sortedMoveIDSet = movieManager.moviesSet();
		int totalMovies = sortedMoveIDSet.size();
		statistics.setTotalCount(totalMovies);

		// Calculate similarities with multi-threading
		ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		for (int t = 0; t < THREAD_COUNT; t++) {
			Runnable worker = new PearsonDist(t, queue, statistics, movieManager, out, out2, out3);
			executor.execute(worker);
		}

		// Fill queue with each movie id
		Iterator<Integer> iterator = sortedMoveIDSet.iterator();
		int index = 0;
		while(iterator.hasNext()) {      	
			CalcData calcData = new CalcData(index++, iterator.next(), totalMovies);      
			try {
				queue.put(calcData);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (index % 1000 == 0) {
				System.out.println(statistics.toString());
			}
		}
		System.out.println(statistics.toString());

		// Mark the end of data
		for (int t = 0; t < THREAD_COUNT; t++) {
			try {
				CalcData calcData = new CalcData(-1, null, totalMovies);
				queue.put(calcData);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		executor.shutdown();

		try {
			// Print statistics every 5 seconds
			while (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
				System.out.println(statistics.toString());
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		System.out.println(statistics.toString());
		System.out.println("done calculating similarities");
		out.close();
		out2.close();
		out3.close();
	}
}