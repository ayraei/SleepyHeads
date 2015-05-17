import java.util.*;

/**
 * Creates two lists of maps. Each list contains 5 maps, as associated with
 * the 1 - 5 rating that a movie can receive. One list contains maps where
 * the keys are the movie id's. The other list of maps uses user id's as the
 * keys.
 */
public class MovieManager {

	/** Fields **/
	private final static int RATE_SIZE = 5;
	private final List<Map<Integer, List<Integer>>> userRatesTraining = new ArrayList<Map<Integer, List<Integer>>>();
	private final List<Map<Integer, List<Integer>>> userRatesTesting = new ArrayList<Map<Integer, List<Integer>>>();

	/** Constructor **/
	public MovieManager() {

		// Initialize the maps and add it to the lists
		for (int i = 0; i < RATE_SIZE; ++i) {
			Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
			userRatesTraining.add(map);
		}
		
		Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
		userRatesTesting.add(map);
	}

	/** Add a rating to the user map **/
	public void add(Integer movieID, Integer userID, Integer rating) {

		// Check to make sure that rating is in the assumed range
		if (rating >= 0 && rating <= 4) {

			// Add movieID to movie
			Map<Integer, List<Integer>> map = userRatesTraining.get(rating);
			List<Integer> list = map.get(userID);

			// Create a new list if this is the first entry for this user
			if (list == null) {
				list = new ArrayList<Integer>();
				map.put(userID, list);
			}
			
			list.add(movieID);
		}

		// If rating is outside of range, notify user and abort
		else {
			System.out.println("Inappropriate rating of " + rating + " found, aborting.");
			System.exit(-1);
		}
	}

	/** Add a rating to the user test map **/
	public void addToTesting(Integer movieID, Integer userID, Integer rating) {

		// Check to make sure that rating is unknown
		if (rating != -1) {

			// Add movieID to movie
			Map<Integer, List<Integer>> map = userRatesTesting.get(rating);
			List<Integer> list = map.get(userID);

			// Create a new list if this is the first entry for this user
			if (list == null) {
				list = new ArrayList<Integer>();
				map.put(userID, list);
			}
			
			list.add(movieID);
		}

		// If rating is outside of range, notify user and abort
		else {
			System.out.println("Inappropriate rating of " + rating + " found, aborting.");
			System.exit(-1);
		}
	}
	
	/** Return a list of movie, ratings that given by a specific user **/
	public List<RateUnit> getMoviesByUserID_Training(Integer userID) {
		List<RateUnit> list = new ArrayList<RateUnit>();

		// Need to go through each user map to find all the movies
		for (int i = 0; i < RATE_SIZE; ++i) {
			Map<Integer, List<Integer>> m = userRatesTraining.get(i);
			List<Integer> movies = m.get(userID);

			// Add all the movies if there are any
			if (movies != null) {
				for (Integer movieID : movies) {
					list.add(new RateUnit(movieID, i + 1));
				}
			}
		}
		return list;
	}
	
	/** Return a list of movie, ratings that given by a specific user **/
	public List<RateUnit> getMoviesByUserID_Testing(Integer userID) {
		List<RateUnit> list = new ArrayList<RateUnit>();

		// Need to go through map to find all the movies
		Map<Integer, List<Integer>> m = userRatesTesting.get(0);
		List<Integer> movies = m.get(userID);

		// Add all the movies if there are any
		if (movies != null) {
			for (Integer movieID : movies) {
				list.add(new RateUnit(movieID, 0));
			}
		}
		
		return list;
	}

}
