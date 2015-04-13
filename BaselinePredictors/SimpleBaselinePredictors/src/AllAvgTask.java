import java.io.FileNotFoundException;
import java.io.PrintWriter;

/** 
 * This class provides the implementation for getting the average of all 
 * movie ratings and stores the value in an output file called 
 * "allAvgMovies.dta".
 **/
public class AllAvgTask implements Runnable {
	// Fields
	/** Running Summation **/
	private double avgAllRatings = 0;
	
	/** Name of output file **/
	private static String OUTPUT_FILE = "allAvgMovies.dta";
	
	/** Reference to our caller application **/
	private BaselinePredictorApp app;
	
	/** Constructor, takes in an instance of BaselinePredictorApp. **/
	public AllAvgTask(BaselinePredictorApp a) {
		this.app = a;
	}
	
	// Methods
	public double getAvgAllRating() {
		return this.avgAllRatings;
	}
	
	@Override
	public void run() {	
		for (MovieProfile movie:app.allMovies.values()) {
			avgAllRatings = avgAllRatings + movie.getAvgRating();
		}
		avgAllRatings = avgAllRatings/app.allMovies.size();
		
		try {
			PrintWriter out = new PrintWriter(OUTPUT_FILE);
			out.println(BaselinePredictorApp.FORMAT_PRECISION.format(avgAllRatings));
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
