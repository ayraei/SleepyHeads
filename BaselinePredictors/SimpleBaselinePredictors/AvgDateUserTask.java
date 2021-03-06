import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** 
 * This class provides the implementation for getting the average date 
 * of rating for the user. This information is placed to an output file
 * called avgDateUsers.dta.
 **/
public class AvgDateUserTask implements Runnable {
	
	/** Name of output file **/
	private static String OUTPUT_FILE = 
			BaselinePredictorApp.OUTPUT_FOLDER + "avgDateUsers.dta";

	/** Reference to our caller application **/
	private BaselinePredictorApp app;

	/** Constructor, takes in an instance of BaselinePredictorApp. **/
	public AvgDateUserTask(BaselinePredictorApp a) {
		this.app = a;
	}

	@Override
	public void run() {
		double avgDate;
		PrintWriter out;
		
		try {
			// Write each average to file
			out = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_FILE)));
			for (Integer userID:app.allUsers.keySet()) {
				avgDate = app.allUsers.get(userID).getAvgDate();
				
				// Write it out to file
				out.println(userID + " " + BaselinePredictorApp.FORMAT_PRECISION.format(avgDate));
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
