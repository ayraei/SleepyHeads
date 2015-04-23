
import java.util.HashMap;

/** Manages the creation and processing of bins **/
public class BinManager {
	// Fields
	/** Number of bins and items in a bin **/
	public static int NUM_BINS = 32;
	public static int BIN_SIZE = 70;
	
	/** HashMap of bins **/
    public HashMap<Integer, Bin> allBins;	
	
	/** Reference to our caller application **/
	private BaselinePredictorApp app;
	
	// Constructor
	public BinManager(BaselinePredictorApp a) {
		this.allBins = new HashMap<Integer, Bin>();
		this.app = a;
	}
	
	// Methods
	/** Sort input data into bins **/
	public void sortToBins() {
		// Date ranges
		int start = 1;
		int end = BIN_SIZE;
		
		// Create bins
		for (Integer i = 0; i < NUM_BINS; i++) {			
			// Last bin absorbs a few extra days
			if (i == NUM_BINS - 1) {
				end += (2243 - NUM_BINS * BIN_SIZE);
			}
			
			this.allBins.put(i, new Bin(start + i * BIN_SIZE, end + i * BIN_SIZE));
		}
		
		// Put data into correct bin. Integer division in Java rounds down
		for (Integer movieID:app.allMovies.keySet()) {
			for (MovieRating r : app.allMovies.get(movieID).getHistory()) {
				int binNum = r.getDate() / (BIN_SIZE + 1);
				this.allBins.get(binNum).addMovieToBin(movieID, (double) r.getRating());
			}
		}
		
	}
}
