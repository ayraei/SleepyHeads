
import java.util.LinkedList;

/** Movie object class. **/
public class MovieProfile {
	// Fields
	/** History of movie ratings. **/
	private LinkedList<MovieRating> ratingHistory;

	/** Get movie's history of ratings. **/
	public LinkedList<MovieRating> getHistory() {
		return this.ratingHistory;
	}

	/** Add new movies to user's rating history. **/
	public void addToHistory(MovieRating r) {
		this.ratingHistory.add(r);
	}

	/** Get the average of all ratings for this movie. **/
	public double getAvgRating() {
		double sum = 0;
		for (MovieRating r:this.getHistory()) {
			sum = sum + r.getRating();
		}
		return sum/(this.getHistory().size());
	}

	// Constructor
	public MovieProfile() {
		this.ratingHistory = new LinkedList<MovieRating>();
	}

}
