
import java.util.HashMap;

/** 
 * Each bin represents a different time range. Movie ratings are sorted into 
 * the corresponding bin based on the date the rating was given.
 */
public class Bin {
	// Fields
	/** HashMap of movies **/
	private HashMap<Integer, Double> movies;
	
	/** Minimum date in bin range **/
	private int binMin;
	
	/** Maximum date in bin range **/
	private int binMax;
	
	// Constructor
	public Bin(int a, int b) {
		this.binMin = a;
		this.binMax = b;
		this.movies = new HashMap<Integer, Double>();
	}
	
	// Methods
	
	/** Add a movie rating to the bin **/
	public void addMovieToBin(Integer movieID, Double rating) {
		// Check if this movie already exists in this bin
		if (this.movies.containsKey(movieID)) {
			Double newRating = 0.5 * (this.movies.get(movieID) + rating);
			this.movies.put(movieID, newRating);
		}		
		// Otherwise add new movie to the bin
		else {
			this.movies.put(movieID, rating);
		}
	}
	
	/** Get all the movie ratings in the bin **/
	public HashMap<Integer, Double> getBinContents() {
		return this.movies;
	}
	
	/** Get bin's range **/
	public int[] getBinRange() {
		int[] range = new int[2];
		range[0] = binMin;
		range[1] = binMax;
		return range;
	}
}
