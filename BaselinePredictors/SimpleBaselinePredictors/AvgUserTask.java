
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** 
 * This class provides the implementation for getting the average rating 
 * of each user to an output file called avgUsers.dta. 
 **/
public class AvgUserTask implements Runnable {
	/** Name of output file **/
	private static String OUTPUT_FILE = 
			BaselinePredictorApp.OUTPUT_FOLDER + "avgUsers.dta";
	
	/** Reference to our caller application **/
	private BaselinePredictorApp app;
	
	/** Constructor, takes in an instance of BaselinePredictorApp. **/
	public AvgUserTask(BaselinePredictorApp a) {
		this.app = a;
	}

	@Override
	public void run() {
		double avgRating;
		PrintWriter out;
		
		try {
			// Write each average to file
			out = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_FILE)));
			
			for (Integer userID:app.allUsers.keySet()) {
				avgRating = app.allUsers.get(userID).getAvgRating();
				out.println(userID + " " + BaselinePredictorApp.FORMAT_PRECISION.format(avgRating));
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
