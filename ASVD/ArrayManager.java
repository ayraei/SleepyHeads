import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Creates hashmaps of arrays, where the key is the user ID and the values are
 * the user's ratings.
 */
public class ArrayManager {

	/** Fields **/
	private final HashMap<Integer, ArrayList<RateUnit>> map = new HashMap<Integer, ArrayList<RateUnit>>();
	private final int[] sums = new int[ASVD_App.NUM_USERS];
	private final double[] Rs = new double[ASVD_App.NUM_USERS];
	private final double[] Ns = new double[ASVD_App.NUM_USERS];
	
	/** Add a rating to the map **/
	public void add(Integer userID, Integer movieID, Integer rating) {

		// Check to make sure that rating is in the assumed range
		if (rating >= 0 && rating <= 5) {

			// Add rating to the user's array
			if (map.get(userID) == null) {
				map.put(userID, new ArrayList<RateUnit>());
			}
			
			map.get(userID).add(new RateUnit(movieID, rating));
		}

		// If rating is outside of range, notify user and abort
		else {
			System.out.println("Inappropriate rating of " + rating + " found, aborting.");
			System.exit(-1);
		}
	}
	
	/** Return the R array (ratings 1 through 5) for the specific user **/
	public ArrayList<RateUnit> getUserHistory_R(Integer userID) {
		ArrayList<RateUnit> arr = new ArrayList<RateUnit>();
		
		// Only care for the nonzero ratings
		for (RateUnit ru : map.get(userID)) {
			if (ru.getRating() != 0) {
				arr.add(ru);
			}
		}
		return arr;
	}
	
	/** Return the N array (ratings 0 through 5) for the specific user **/
	public ArrayList<RateUnit> getUserHistory_N(Integer userID) {
		return map.get(userID);
	}

	/** Return the rating for a movie, user in the training set **/
	public int getOriginalRating(ArrayList<RateUnit> userHistory , Integer movieID) {
		
		for (RateUnit ru : userHistory) {
			if (ru.getID().equals(movieID)) {
				return ru.getRating();
			}
		}
		
		return 0;
	}
	
	/** Return the sum of all the ratings for a user **/
	public int getRSum(Integer userID) {	
		return sums[userID];
	}
	
	/** Return the number of explicitly rated movies from a user **/
	public double getR(Integer userID) {	
		return Rs[userID];
	}
	
	/** Return the number of implicitly rated movies from a user **/
	public double getN(Integer userID) {	
		return Ns[userID];
	}
	
	/** Fill up the arrays for the constants **/
	public void initConstants() {
		Arrays.fill(sums, 0);
		
		for (int i = 0; i < ASVD_App.NUM_USERS; i++) {
			Rs[i] = Math.pow(this.getUserHistory_R(i).size(), -0.5);
			Ns[i] = Math.pow(this.getUserHistory_N(i).size(), -0.5);
			
			ArrayList<RateUnit> userHistory = this.getUserHistory_R(i);
			for (RateUnit ru : userHistory) {
				sums[i] += ru.getRating();
			}
		}
	}

}
