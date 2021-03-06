
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** 
 * This class provides the implementation for getting the average rating 
 * of each movie to an output file called avgMovies.dta. 
 **/
public class AvgMovieTask implements Runnable {
	/** Name of output file **/
	private static String OUTPUT_FILE = 
			BaselinePredictorApp.OUTPUT_FOLDER + "avgMovies.dta";

	/** Reference to our caller application **/
	private BaselinePredictorApp app;

	/** Constructor, takes in an instance of BaselinePredictorApp. **/
	public AvgMovieTask(BaselinePredictorApp a) {
		this.app = a;
	}

	@Override
	public void run() {
		double avgRating;
		PrintWriter out;

		try {
			// Write each average to file
			out = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_FILE)));

			for (Integer movieID:app.allMovies.keySet()) {
				avgRating = app.allMovies.get(movieID).getAvgRating();
				out.println(movieID + " " + BaselinePredictorApp.FORMAT_PRECISION.format(avgRating));
			}

			// Close file
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
