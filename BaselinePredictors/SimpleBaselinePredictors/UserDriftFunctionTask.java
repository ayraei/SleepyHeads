import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.math3.stat.regression.SimpleRegression;

/** 
 * This class provides the implementation for getting the drift value 
 * of rating over time for the user. This information is placed to an 
 * output file called userDriftFunction.dta.
 **/
public class UserDriftFunctionTask implements Runnable {

	/** Name of output file **/
	private static String OUTPUT_FILE = 
			BaselinePredictorApp.OUTPUT_FOLDER + "userDriftFunction.dta";

	/** Reference to our caller application **/
	private BaselinePredictorApp app;

	/** Constructor, takes in an instance of BaselinePredictorApp. **/
	public UserDriftFunctionTask(BaselinePredictorApp a) {
		this.app = a;
	}

	@Override
	public void run() {
		double a, b;
		PrintWriter out;

		try {
			out = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_FILE)));

			// Write each average to file
			for (Integer userID:app.allUsers.keySet()) {

				// Run linear regression
				SimpleRegression regression = new SimpleRegression();
				for (UserRating r:app.allUsers.get(userID).getHistory()) {
					regression.addData(r.getDate(), r.getRating());
				}

				// Get constants
				a = regression.getSlope();
				b = regression.getIntercept();

				// Write it out to file
				out.println(userID + " " + 
						BaselinePredictorApp.FORMAT_PRECISION.format(a) + " " +
						BaselinePredictorApp.FORMAT_PRECISION.format(b));
			}

			// Close file
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
