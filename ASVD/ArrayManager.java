import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Creates hashmaps of arrays, where the key is the user ID and the values are
 * the user's ratings.
 */
public class ArrayManager {

	/** Fields **/
	private final HashMap<Integer, ArrayList<Integer>> trainMap = new HashMap<Integer, ArrayList<Integer>>();
	private final HashMap<Integer, ArrayList<Integer>> testMap  = new HashMap<Integer, ArrayList<Integer>>();
	private final HashMap<Integer, ArrayList<Double>> x = new HashMap<Integer, ArrayList<Double>>();
	private final HashMap<Integer, ArrayList<Double>> y = new HashMap<Integer, ArrayList<Double>>();

	/** Add a rating to the training map **/
	public void addToTraining(Integer userID, Integer rating) {

		// Check to make sure that rating is in the assumed range
		if (rating >= 1 && rating <= 5) {

			// Add rating to the user's array
			ArrayList<Integer> userHistory = trainMap.get(userID);
			userHistory.add(rating);
			
			ArrayList<Integer> userHistoryTest = testMap.get(userID);
			userHistoryTest.add(rating);
		}

		// If rating is outside of range, notify user and abort
		else {
			System.out.println("Inappropriate rating of " + rating + " found, aborting.");
			System.exit(-1);
		}
	}

	/** Add a rating to the user testing map **/
	public void addToTesting(Integer userID, Integer rating) {

		// Check to make sure that rating is unknown
		if (rating != 0) {

			// Add rating to the user's array
			ArrayList<Integer> userHistory = testMap.get(userID);
			userHistory.add(rating);
		}

		// If rating is outside of range, notify user and abort
		else {
			System.out.println("Inappropriate rating of " + rating + " found, aborting.");
			System.exit(-1);
		}
	}
	
	/** Initialize the x and y arrays for each userID to zeros **/
	public void init_xy() {
		for (Integer userID : trainMap.keySet()) {
			int R_size = trainMap.get(userID).size();
			int N_size = testMap.get(userID).size() + R_size;
			ArrayList<Double> xi = new ArrayList<Double>(Collections.nCopies(R_size, 0.0));
			ArrayList<Double> yi = new ArrayList<Double>(Collections.nCopies(N_size, 0.0));
			x.put(userID, xi);
			y.put(userID, yi);
		}
	}
	
	/** Return the training rating array for the specific user **/
	public ArrayList<Integer> getUserHistory_train(Integer userID) {
		return trainMap.get(userID);
	}
	
	/** Return the testing rating array for the specific user **/
	public ArrayList<Integer> getUserHistory_test(Integer userID) {
		return testMap.get(userID);
	}
	
	/** Return the training rating array for the specific user **/
	public ArrayList<Double> getX(Integer userID) {
		return x.get(userID);
	}
	
	/** Return the testing rating array for the specific user **/
	public ArrayList<Double> getY(Integer userID) {
		return y.get(userID);
	}

}
