
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
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
	private static String OUTPUT_FILE = 
			BaselinePredictorApp.OUTPUT_FOLDER + "allAvgMovies.dta";
	
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
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_FILE)));
			out.println(BaselinePredictorApp.FORMAT_PRECISION.format(avgAllRatings));
			out.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
