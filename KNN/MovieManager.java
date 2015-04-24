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
	private final List<Map<Integer, List<Integer>>> movieRates = new ArrayList<Map<Integer, List<Integer>>>();
	private final List<Map<Integer, List<Integer>>> userRates = new ArrayList<Map<Integer, List<Integer>>>();

	/** Constructor **/
	public MovieManager() {

		// Initialize each map and add it to the lists
		for (int i = 0; i < RATE_SIZE; ++i) {
			Map<Integer, List<Integer>> map = new HashMap<Integer, List<Integer>>();
			movieRates.add(map);

			map = new HashMap<Integer, List<Integer>>();
			userRates.add(map);
		}
	}

	/** Add a rating to the appropriate movie and user maps **/
	public void add(Integer movieID, Integer userID, Integer rating) {

		// Check to make sure that rating is in the assumed range
		if (rating >= 0 && rating <= 4) {

			// Add userId to movie
			Map<Integer, List<Integer>> map = movieRates.get(rating);
			List<Integer> list = map.get(movieID);

			// Create a new list if this is the first entry for this movie
			if (list == null) {
				list = new ArrayList<Integer>();
				map.put(movieID, list);
			}
			list.add(userID);

			// Add movieId to user
			map = userRates.get(rating);
			list = map.get(userID);

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

	/** Return a list of user, ratings that rated the given movie **/
	public List<RateUnit> getUsersByMovieID(Integer movieId) {
		List<RateUnit> list = new ArrayList<RateUnit>();

		// Need to go through each movie map to find all the users
		for (int i = 0; i < RATE_SIZE; ++i) {
			Map<Integer, List<Integer>> m = movieRates.get(i);
			List<Integer> users = m.get(movieId);
			
			// Add all the users if there are any
			if (users != null) {
				for (Integer userID : users) {
					list.add(new RateUnit(userID, i + 1));
				}
			}
		}
		return list;
	}

	/** Return a list of movie, ratings that given by a specific user **/
	public List<RateUnit> getMoviesByUserId(Integer userId) {
		List<RateUnit> list = new ArrayList<RateUnit>();

		// Need to go through each user map to find all the movies
		for (int i = 0; i < RATE_SIZE; ++i) {
			Map<Integer, List<Integer>> m = userRates.get(i);
			List<Integer> movies = m.get(userId);

			// Add all the movies if there are any
			if (movies != null) {
				for (Integer movieID : movies) {
					list.add(new RateUnit(movieID, i + 1));
				}
			}
		}
		return list;
	}


	/** Return a sorted set of movie id's **/
	public SortedSet<Integer> moviesSet() {
		SortedSet<Integer> set = new TreeSet<Integer>();

		for (Map<Integer, List<Integer>> m : movieRates) {
			set.addAll(m.keySet());
		}
		return set;
	}

}
