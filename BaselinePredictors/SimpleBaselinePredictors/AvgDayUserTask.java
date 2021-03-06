
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** 
 * This class provides the implementation for getting the average rating 
 * of each user for a particular day. This information is placed to an 
 * output file called avgDayUsers.dta.
 **/
public class AvgDayUserTask implements Runnable {
	/** Name of output file **/
	private static String OUTPUT_FILE = 
			BaselinePredictorApp.OUTPUT_FOLDER + "avgDayUsers.dta";

	/** Reference to our caller application **/
	private BaselinePredictorApp app;

	/** Constructor, takes in an instance of BaselinePredictorApp. **/
	public AvgDayUserTask(BaselinePredictorApp a) {
		this.app = a;
	}

	@Override
	public void run() {
		Integer targetDate, userID;
		
		// Check if file already exists, exit if it does
		File file = new File(OUTPUT_FILE);
		if(file.exists() && !file.isDirectory()) 
		{
			return;
		}
		
		// Variables related to reading and writing files
		String line;
		String[] inArray = null;
		BufferedReader br = null;
		
		// Read in test file
		try {
			br = new BufferedReader(new FileReader(BaselinePredictorApp.TEST_FILE_LOC));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_FILE)));
			
			while ((line = br.readLine()) != null) {
				double count = 0;
				double avgRating = 0;
				
				// Get the relevant movie data
				inArray = line.split("\\s+");
				userID = Integer.parseInt(inArray[0]);
				targetDate = Integer.parseInt(inArray[2]);
				
				for (UserRating r:app.allUsers.get(userID).getHistory()) {
					if (r.getDate() == targetDate) {
						avgRating += r.getRating();
						count++;
					}
				}
				
				out.println(userID + " " + targetDate + " " + BaselinePredictorApp.FORMAT_PRECISION.format(avgRating / count));
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
