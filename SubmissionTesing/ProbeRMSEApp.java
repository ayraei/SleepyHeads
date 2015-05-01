import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;


/**
 * Return the RMSE on probe
 **/
public class ProbeRMSEApp {

	/** Input files **/
	private static String INPUT_PREDICT_LOC =
			"/Users/debbie1/Documents/NetflixData/output/KNN_predictions_probe.dta";
	private static String INPUT_TRUE_LOC =
			"/Users/debbie1/Documents/NetflixData/mu_sorted/probe.dta";
	
	/** RMSE **/
	private static float rmse = 0;

	/** Program entry point **/
	public static void main(String[] args) {

		// Read in each line from both files
		String lineP;
		String lineT;
		BufferedReader brP = null;
		BufferedReader brT = null;
		try {
			brP = new BufferedReader(new FileReader(INPUT_PREDICT_LOC));
			brT = new BufferedReader(new FileReader(INPUT_TRUE_LOC));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read in each line to store data to memory
		int count = 0;
		try {
			while ((lineP = brP.readLine()) != null) {
				lineT = brT.readLine();
				
				// Print progress
				if (count % 500000 == 0) {
					System.out.println(count);
				}
				count++;

				// Read in data as a string array, cast to float
				String[] inputP = lineP.split("\\s+");
				String[] inputT = lineT.split("\\s+");
				float prediction = Float.parseFloat(inputP[0]);
				float trueValue = Float.parseFloat(inputT[3]);
				
				rmse += Math.pow(prediction - trueValue, 2);

			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		
		rmse = (float) Math.sqrt(rmse / count);
		System.out.println("RMSE on probe: " + rmse);
	}
}
