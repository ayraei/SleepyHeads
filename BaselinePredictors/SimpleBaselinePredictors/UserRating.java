
/** User-rating object, used to keep a history of ratings given by a user. **/
public class UserRating {
	/** Movie ID number **/
	private int movie;
	
	/**
	 * Date: falls between 1 and 2243 (in days): Day 1 is Thursday,
	 * November 11, 1999, and Day 2243 is Saturday, December 31, 2005.
	 **/
	private int date;
	
	/** Rating: between 1 and 5. Zero signifies an unknown rating. **/
	private int rating;
	
	// Methods
	public int getMovie() {
		return this.movie;
	}
	
	public int getDate() {
		return this.date;
	}
	
	public int getRating() {
		return this.rating;
	}
	
	// Constructor
	public UserRating(int m, int d, int r) {
		this.movie = m;
		this.date = d;
		this.rating = r;
	}
}
