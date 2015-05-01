import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DecimalFormat;

/**
 * Round off the prediction if its distance to the nearest integer is less than
 * or equal to 0.1
 **/
public class RoundOffApp {

	/** Input and output files **/
	private static String INPUT_PREDICT_LOC =
			"/Users/debbie1/Documents/NetflixData/output/KNN_predictions.dta";
	private static String OUTPUT_PREDICT_LOC =
			"/Users/debbie1/Documents/NetflixData/output/KNN_predictions_rounded.dta";

	/** Level of reported precision (3 decimal places) **/
	public static DecimalFormat FORMAT_PRECISION = new DecimalFormat("0.000");

	/** Round off **/
	private static float ro = (float) 0.1;

	/** Program entry point **/
	public static void main(String[] args) {

		// Delete if file already exists, otherwise will append to file
		try{
			File simFile = new File(OUTPUT_PREDICT_LOC);
			if (simFile.exists()) {
				simFile.delete();
			}
		} catch(Exception e){
			e.printStackTrace();
		}

		// Prepare to print out rounded-off prediction
		PrintWriter out = null;
		try {
			out = new PrintWriter(new FileOutputStream(OUTPUT_PREDICT_LOC, true));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		// Read in each line from original prediction file
		String line;
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(INPUT_PREDICT_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read in each line to store data to memory
		int count = 0;
		try {
			while ((line = br.readLine()) != null) {

				// Print progress
				if (count % 500000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array, cast to float
				String[] input = line.split("\\s+");
				float prediction = Float.parseFloat(input[0]);
				float nearestFloat = (float) Math.round(prediction);

				// Apply rounding
				if (Math.abs(nearestFloat - prediction) < ro) {
					prediction = nearestFloat;
				}

				out.println(FORMAT_PRECISION.format(prediction));
				out.flush();

			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("Done rounding!");
	}
}
