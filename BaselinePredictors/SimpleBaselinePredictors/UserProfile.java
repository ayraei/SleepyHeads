
import java.util.LinkedList;

/** User-based object class **/
public class UserProfile {
	// Fields
	/** History of movie ratings. **/
	private LinkedList<UserRating> ratingHistory;

	//Methods
	/** Get user's history of movie ratings. **/
	public LinkedList<UserRating> getHistory() {
		return this.ratingHistory;
	}

	/** Add new movies to user's rating history. **/
	public void addToHistory(UserRating r) {
		this.ratingHistory.add(r);
	}

	/** Get the average of all ratings from the user. **/
	public double getAvgRating() {
		double sum = 0;
		for (UserRating r:this.getHistory()) {
			sum = sum + r.getRating();
		}
		return sum/(this.getHistory().size());
	}

	/** Calculate the average of the date of rating for the user. **/
	public double getAvgDate() {
		double sum = 0;
		for (UserRating r:this.getHistory()) {
			sum = sum + r.getDate();
		}
		return sum/(this.getHistory().size());
	}
	
	// Constructor
	public UserProfile() {
		this.ratingHistory = new LinkedList<UserRating>();
	}
}
