/**
 * Use Pearson coefficient to calculate similarity between two movies
 */

import Jama.Matrix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class PearsonDist implements Runnable {

	/** Fields **/
	private int threadID;
	private final BlockingQueue<Integer> queue;
	private final CalcStatistics statistics;
	private KNNApp app;
	private char[][][] vCount;
	private short[][][] overflows;

	/** Constructor **/
	public PearsonDist(int t, BlockingQueue<Integer> queue, CalcStatistics statistics, KNNApp a) {
		this.threadID = t;
		this.queue = queue;
		this.statistics = statistics;
		this.app = a;
	}

	@Override
	public void run() {
		while (true) {
			try {
				int m1 = queue.take();
				if (m1 == -1) {
					break;
				}

				calcSimilarity(m1);
				statistics.incAndGet();

			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		System.out.println("ThreadId " + threadID + " ended.");
	}

	public void calcSimilarity(int m1) {
		vCount = new char[5][KNNApp.NUM_MOVIES][5];
		overflows = new short[5][KNNApp.NUM_MOVIES][5];
		HashMap<Integer, Integer> m1History = app.movieHashMap.get(m1);
		
		// For each user who rated movie X
		for (Integer viewer : m1History.keySet()) {
			int r1 = m1History.get(viewer);                      // The rating viewer gave to m1

			// For each movie Y rated by the user
			for (Integer m2 : app.userHashMap.get(viewer).values()) {
				int r2 = app.userHashMap.get(viewer).get(m2);    // The rating viewer gave to m2

				// Increment the rating
				vCount[r1][m2][r2]++;

				// Catch overflow beyond 255
				if (0 == vCount[r1][m2][r2]) {
					overflows[r1][m2][r2]++;
				}
			}
		}
		
		// Done caching data, time to calculate sims
		for (int m2 = 0; m2 < KNNApp.NUM_MOVIES; m2++) {
			float s1;    //sum of ratings for movie X
		    float s2;    //sum of ratings for movie Y
		    float p12;   //sum of product of ratings for movies X and Y
		    float p11;   //sum of square of ratings for movie X
		    float p22;   //sum of square of ratings for movie Y
		    int numIntersect; //number of viewers who rated both movies
		    
		    
		}
		

	}

}