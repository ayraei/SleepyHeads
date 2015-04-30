
import java.io.PrintWriter;
import java.util.List;
import java.util.concurrent.BlockingQueue;

/**
 * Use Pearson coefficient to calculate similarity between two movies
 */
public class PearsonDist implements Runnable {

	/** Fields **/
	private int threadID;
	private final BlockingQueue<CalcData> queue;
	private final CalcStatistics statistics;
	private MovieManager movieManager;
	private PrintWriter outSims;
	private PrintWriter outCount;
	private Ratings[] vCount;
	private int[] commonViewers;

	/** Constructor **/
	public PearsonDist(int t, BlockingQueue<CalcData> queue, CalcStatistics statistics, MovieManager movieManager, PrintWriter out, PrintWriter out2) {
		this.threadID = t;
		this.queue = queue;
		this.statistics = statistics;
		this.movieManager = movieManager;
		this.outSims = out;
		this.outCount = out2;
	}

	@Override
	public void run() {

		// Keep grabbing from queue
		while (true) {
			try {
				CalcData calcData = queue.take();

				// End of data, signals thread to stop running
				if (calcData.getIndex() == -1) {
					break;
				}

				calcSimilarity(calcData);
				statistics.incAndGet();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("ThreadID " + threadID + " ended.");
	}

	/** Calculate the similarities between a given movie and the rest of the movies **/
	public void calcSimilarity(CalcData calcData) {

		// Create an empty array of 17,770 elements
		vCount = new Ratings[calcData.getTotalMovies()];
		commonViewers = new int[calcData.getTotalMovies()];

		List<RateUnit> m1History = movieManager.getUsersByMovieID(calcData.getMovieID());

		// For each user who rated movie 1
		for (RateUnit viewer : m1History) {
			int r1 = viewer.getRating();    // The rating viewer gave to m1

			// For each movie 2 rated by the user
			for (RateUnit m2 : movieManager.getMoviesByUserId(viewer.getID())) {
				int r2 = m2.getRating();    // The rating viewer gave to m2

				// Increment the rating
				if (vCount[m2.getID() - 1] == null) {
					vCount[m2.getID() - 1] = new Ratings();
				}

				vCount[m2.getID() - 1].rate(r1, r2);

			}
		}

		// Done caching data, time to calculate sims
		for (int m = 0; m < calcData.getTotalMovies(); m++) {
			Ratings ratings = vCount[m];

			// Check if there is no intersection
			if (ratings == null) {
				return;
			}

			float sumX = ratings.findSumX();
			float sumY = ratings.findSumY();
			float sumXY = ratings.findXY();
			float sumXX = ratings.findXX();
			float sumYY = ratings.findYY();
			int num = ratings.getCounter();

			float denomitor = (float) Math.sqrt((num * sumXX - sumX * sumX) * (num * sumYY - sumY * sumY));
			float simularity;
			if (denomitor == 0) {
				simularity = 0;
			} else {
				simularity = ((sumXY * num) - (sumX * sumY)) / denomitor;
			}

			ratings.setSimularity(simularity);
			commonViewers[m] = num;

		}

		// Synchronize out to prevent threads from interleaving prints
		synchronized(outSims) {

			// Movies may be printed out of order, so we begin the line with movie ID
			outSims.print(calcData.getMovieID() + " ");

			for(int i = 0; i < calcData.getTotalMovies(); ++ i) {

				// Print zero if vCount is null (no intersection), otherwise print similarity
				float sim = (vCount[i] == null)? 0 : vCount[i].getSimularity();
				outSims.print(CalculateSimApp.FORMAT_PRECISION.format(sim) + " ");
			}

			// New line for next movie
			outSims.println();
		}

		// Synchronize out to prevent threads from interleaving prints
		synchronized(outCount) {

			// Movies may be printed out of order, so we begin the line with movie ID
			outCount.print(calcData.getMovieID() + " ");

			for(int i = 0; i < calcData.getTotalMovies(); i++){
				int count = commonViewers[i];
				outCount.print(count + " ");
			}

			// New line for next movie
			outCount.println();
		}
	}
}