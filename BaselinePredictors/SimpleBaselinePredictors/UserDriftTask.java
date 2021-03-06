
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.LinkedList;

/** 
 * This class provides the implementation for getting the gradual drift
 * in a user's rating. It first creates a file called userDriftFunction.dta 
 * if it doesn't already exist, which contains the m and b (for mx + b) 
 * learned for each user. It also calls upon the AvgDateUserTask if 
 * avgDateUsers.dta doesn't already exist.
 * 
 * This task's main function is to report the user's bias at the time of 
 * interest specified by a test file. It outputs to a file called 
 * userDrift.dta. It is calculated using:
 * 		y = a * x + b
 * where x = sign(t - tu) * |t - tu|^0.4
 * t  is the date of the rating we are trying to predict
 * tu is the average date of rating
 **/
public class UserDriftTask implements Runnable {
	/** Name of output file **/
	private static String OUTPUT_FILE = 
			BaselinePredictorApp.OUTPUT_FOLDER + "userDrift.dta";

	/** Reference to our caller application **/
	private BaselinePredictorApp app;

	/** HashMaps with data from dependencies **/
	private HashMap<Integer, Double[]> userDriftFuncMap;
	private HashMap<Integer, Double> avgDateUsersMap;

	/** Constructor, takes in an instance of BaselinePredictorApp. **/
	public UserDriftTask(BaselinePredictorApp a) {
		this.app = a;
		this.userDriftFuncMap = new HashMap<Integer, Double[]>();
		this.avgDateUsersMap = new HashMap<Integer, Double>();
	}

	@Override
	public void run() {
		// Get dependencies
		this.getDependencies();

		// Start main task
		Integer targetDate, userID;

		// Variables related to reading and writing files
		String lineTest;
		String[] inArray = null;
		BufferedReader brTest = null;

		// Read in test file
		try {
			brTest = new BufferedReader(new FileReader(BaselinePredictorApp.TEST_FILE_LOC));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		try {
			PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(OUTPUT_FILE)));

			while ((lineTest = brTest.readLine()) != null) {
				double userDrift = 0;

				// Get the relevant movie test data
				inArray = lineTest.split("\\s+");
				userID = Integer.parseInt(inArray[0]);
				targetDate = Integer.parseInt(inArray[2]);

				double a = userDriftFuncMap.get(userID)[0];
				double b = userDriftFuncMap.get(userID)[1];
				double avgDate = avgDateUsersMap.get(userID);
				userDrift = a * Math.signum(targetDate - avgDate) 
						* Math.pow(Math.abs(targetDate - avgDate), 0.4) + b;

				out.println(userID + " " + targetDate + " " + BaselinePredictorApp.FORMAT_PRECISION.format(userDrift));
			}

			// Close file
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** Get dependencies **/
	@SuppressWarnings("null")
	public void getDependencies() {
		// Dependencies to do:
		Runnable[] tasks = new Runnable[2];
		int i = 0;

		// Check if userDriftFunction.dta exists, otherwise create it
		File driftFile = new File(BaselinePredictorApp.OUTPUT_FOLDER + "userDriftFunction.dta");
		if (!driftFile.exists())
		{
			tasks[i] = new UserDriftFunctionTask(app);
			i++;
		}

		// Check if avgDateUser.dta exists, otherwise call on AvgDayUserTask to create it
		File avgDateFile = new File(BaselinePredictorApp.OUTPUT_FOLDER + "avgDateUsers.dta");
		if (!avgDateFile.exists())
		{
			tasks[i] = new AvgDateUserTask(app);
		}

		// Set up array of threads and tasks
		int numTasks = tasks.length;
		Thread[] threadPool = new Thread[numTasks];

		// Use multi-threading to speed up the process
		for (int index = 0; index < numTasks; index++) {
			// Identify task
			Runnable task = tasks[index];

			// Create new thread
			Thread t = new Thread(task);
			threadPool[index] = t;
			t.start();
		}

		// Wait for all threads to terminate
		for (Thread activeThread : threadPool) {
			try {
				System.out.println("Computing...");
				activeThread.join();
			} catch (InterruptedException e) {
				// Continue
				System.out.println("Error: " + e + ", continuing...");
				return;
			}

		}

		// Load data into data structures

		// Read in each dependency file
		String lineDrift, lineAvgDate;
		String[] inArrDrift = null;
		String[] inArrAvgDate = null;
		BufferedReader brDrift = null;
		BufferedReader brAvgDate = null;

		try {
			brDrift = new BufferedReader(new FileReader(BaselinePredictorApp.OUTPUT_FOLDER + "userDriftFunction.dta"));
			brAvgDate = new BufferedReader(new FileReader(BaselinePredictorApp.OUTPUT_FOLDER + "avgDateUsers.dta"));
		
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// Read through each line
		try {
			while ((lineDrift = brDrift.readLine()) != null && 
					(lineAvgDate = brAvgDate.readLine()) != null)
			{
				inArrDrift = lineDrift.split("\\s+");
				inArrAvgDate = lineAvgDate.split("\\s+");
				Double[] data = {Double.parseDouble(inArrDrift[1]), 
				                 Double.parseDouble(inArrDrift[2])};
				
				// Add to HashMaps
				userDriftFuncMap.put(Integer.parseInt(inArrDrift[0]), data);
				avgDateUsersMap.put(Integer.parseInt(inArrAvgDate[0]), 
						Double.parseDouble(inArrAvgDate[1]));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}
}
