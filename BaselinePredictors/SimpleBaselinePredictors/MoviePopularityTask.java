
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/** 
 * This class provides the implementation for getting the popularity of a 
 * movie within a roughly 10 week period (30 bins to span the entire date range)
 * for each movie given in a text file. It assumes that the given text files is
 * arranged as (user) (movie) (date) (rating) in each line.
 * 
 * If a movie is not rated in the 10 week time frame, it checks a previous or 
 * later bin for a possible rating.
 * 
 * File outputs to "moviePopularities.dta".
 **/
public class MoviePopularityTask implements Runnable {
	// Fields
	/** Name of output file **/
	private static String OUTPUT_FILE = 
			BaselinePredictorApp.OUTPUT_FOLDER + "moviePopularities.dta";

	/** Reference to our caller application **/
	private BaselinePredictorApp app;

	/** Constructor, takes in an instance of BaselinePredictorApp. **/
	public MoviePopularityTask(BaselinePredictorApp a) {
		this.app = a;
	}

	@Override
	public void run() {	
		// Bin related variables
		int binNum, direction;
		Bin bin;
		Integer date, movieID;
		
		// Variables related to reading and writing files
		String line;
		String[] inArray = null;
		BufferedReader br = null;

		try {
			br = new BufferedReader(new FileReader(BaselinePredictorApp.TEST_FILE_LOC));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}


		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_FILE)));
			
			while ((line = br.readLine()) != null) {
				// Get the relevant movie data
				inArray = line.split("\\s+");
				movieID = Integer.parseInt(inArray[1]);
				date = Integer.parseInt(inArray[2]);
				
				// Get the bin number from the date
				binNum = date / (BinManager.BIN_SIZE + 1);
				
				// Keep looking for a later rating if the movie is not in current bin
				direction = 1;
				Double popRating = null;
				
				while (popRating == null) {
					bin = app.binManager.allBins.get(binNum);
					popRating = bin.getBinContents().get(movieID);
					binNum += direction;
					
					if (binNum == BinManager.NUM_BINS) {
						binNum = date / (BinManager.BIN_SIZE + 1) - 1;
						direction = -1;
					}
					
					else if (binNum < 0) {
						throw new IndexOutOfBoundsException("Trying to access a bin that doesn't exist!");
					}
				}
					
				// Write each average to file
				out.println(movieID + " " + BaselinePredictorApp.FORMAT_PRECISION.format(popRating));
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
