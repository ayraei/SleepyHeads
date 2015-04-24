
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Date;
import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * Calculates baseline predictors for Netflix data.
 * Static predictors:
 *     1. Average across all ratings for all movies
 *     2. Average rating for each movie
 *     3. Average rating given by each user
 * Temporal predictors:
 *     4. Movie popularity at a given time
 *     5. Average of a user rating's in a given day
 * TBD 6. User rating drifts over time
 **/
public class BaselinePredictorApp {
	// Fields
	/** Location of training data file **/
	public static String TRAIN_FILE_LOC = 
			"/Users/debbie1/Documents/NetflixData/um_sorted/trainingAll.dta";

	/** Location of test data file **/
	public static String TEST_FILE_LOC = 
			"/Users/debbie1/Documents/NetflixData/um_sorted/test.dta";
	
	/** Location of folder for output files **/
	public static String OUTPUT_FOLDER = 
			"/Users/debbie1/Documents/NetflixData/output/";

	/** Collections of all user IDs and movie IDs. **/
	public HashMap<Integer, UserProfile> allUsers;
	public HashMap<Integer, MovieProfile> allMovies;

	/** Manages the bins of movies, bins sorted by rating date. **/
	public BinManager binManager;
	
	/** Level of reported precision (3 decimal places) **/
	public static DecimalFormat FORMAT_PRECISION = new DecimalFormat("0.000"); 

	/** Track if we are still trying to get user input **/
	private boolean gettingUserInput = true;

	/** Number of tasks **/
	private int numTasks;

	/** Array of tasks to complete **/
	private Runnable[] tasks;

	/** Constructor **/
	public BaselinePredictorApp() {
		this.allUsers = new HashMap<Integer, UserProfile>();
		this.allMovies = new HashMap<Integer, MovieProfile>();
		this.binManager = new BinManager(this);
	}

	/** Program entry point **/
	public static void main(String[] args) {
		BaselinePredictorApp app = new BaselinePredictorApp();

		// Fill Maps based on input file
		System.out.println("Preparing the hashmaps. Please wait... \n");
		app.fillMaps();

		// Ask and obtain user for input
		System.out.println("Done preparing. Which tasks do you wish to run? ");
		System.out.println("Please enter the corresponding numbers separated by spaces.");
		System.out.println("1. Average across all ratings for all movies");
		System.out.println("2. Average rating for each movie");
		System.out.println("3. Average rating given by each user");
		System.out.println("4. Movie popularity at a given time");
		System.out.println("5. Average of a user rating's in a given day");
		System.out.println("6. User rating drifts over time \n");

		BufferedReader buffReader = new BufferedReader(new InputStreamReader(System.in));
		while (app.gettingUserInput) {
			app.getUserInput(buffReader);
		}

		// Close reader since done getting user input
		try {
			buffReader.close();
		} catch (IOException e) {
			// Unable to close reader
			System.out.println("Error message: " + e);
			return;
		}

		// Set up array of threads and tasks
		Thread[] threadPool = new Thread[app.numTasks];

		// Use multi-threading to speed up the process of getting predictors
		for (int index = 0; index < app.numTasks; index++) {
			// Identify task
			Runnable task = app.tasks[index];

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

		System.out.println("Done! ");
	}

	// Methods
	/** Check if user already exists. **/
	public boolean checkUser(int userID) {
		return this.allUsers.containsKey(userID);
	}

	/** Check if movie already exists. **/
	public boolean checkMovie(int movieID) {
		return this.allMovies.containsKey(movieID);
	}

	/** Fill HashMaps with data from input files. **/
	public void fillMaps() {
		// Set up reader to read from file
		String line;
		String[] strArray = null;
		Integer[] intArray = new Integer[4];
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(TRAIN_FILE_LOC));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		int count = 0;
		
		// Read from file until reach the end of file
		try {
			while ((line = br.readLine()) != null) {
				
				count++;
				if (count % 10000000 == 0) {
					System.out.println(count);
				}
				
				/*
				 * Splits line data into a data array such that entries:
				 * 0: user number
				 * 1: movie number
				 * 2: date
				 * 3: rating
				 * Note that these are strings and need to be casted into Integers
				 */
				strArray = line.split("\\s+");
				for(int i = 0;i < strArray.length;i++)
				{
					intArray[i] = Integer.parseInt(strArray[i]);
				}				
				
				UserRating ur = new UserRating(intArray[1], intArray[2], intArray[3]);
				MovieRating mr = new MovieRating(intArray[0], intArray[2], intArray[3]);

				// Check if user already exists
				if (this.checkUser(intArray[0])) {
					this.allUsers.get(intArray[0]).addToHistory(ur);
				} 
				// Otherwise create new user
				else {
					UserProfile up = new UserProfile();
					up.addToHistory(ur);
					this.allUsers.put(intArray[0], up);
				}

				// Check if movie already exists
				if (this.checkMovie(intArray[1])) {
					this.allMovies.get(intArray[1]).addToHistory(mr);
				} 
				// Otherwise create new movie
				else {
					MovieProfile mp = new MovieProfile();
					mp.addToHistory(mr);
					this.allMovies.put(intArray[1], mp);
				}

			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/** 
	 * Takes user input and splits tasks based via white spaces.
	 **/
	private void getUserInput(BufferedReader br) {
		String[] inArray = null;

		try {
			String inString = br.readLine();
			inArray = inString.split("\\s+");

			numTasks = inArray.length;
			this.tasks = new Runnable[numTasks];	

			int index = 0;
			for (String s : inArray) {
				Integer t = Integer.parseInt(s);
				switch (t) {
				case 1: tasks[index] = new AllAvgTask(this);
						break;

				case 2: tasks[index] = new AvgMovieTask(this);
						break;

				case 3: tasks[index] = new AvgUserTask(this);
						break;

				case 4: 
					this.binManager.sortToBins();
					tasks[index] = new MoviePopularityTask(this);
					break;

				case 5: tasks[index] = new AvgDayUserTask(this);
						break;

				case 6: tasks[index] = new UserDriftTask(this);
						break;
				}

				index++;
			}


		} catch (IOException e) {
			// Unable to read, unlikely to occur
			System.out.println("Unable to read input stream. Error message: "  + e);
		}

		// Reach this point means that input is okay
		this.gettingUserInput = false;
	}

}
