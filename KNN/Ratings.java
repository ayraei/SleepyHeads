
/**
 * Store rating information for finding intersection of set of users between
 * two movies.
 **/
public class Ratings {
    private final static int SIZE = 5;

    private int[][] intRates;
    private int counter;
    private float simularity;

    /** Constructor **/
    public Ratings() {
    	this.counter = 0;
    	this.intRates = new int[SIZE][SIZE];
    }
    
    /** Increment the count at the specified location **/
    public void rate(int i, int j) {
        ++counter;
        intRates[i - 1][j - 1]++;
    }

    /** Sum of all the ratings for movie X **/
    public int findSumX() {
        int sum = 0;
        
        for (int i = 0; i < SIZE; ++i) {
            int count = 0;
            for (int j = 0; j < SIZE; ++j) {
                count += intRates[i][j];
            }
            sum += (i + 1) * count;
        }
        return sum;
    }

    /** Sum of all the ratings for movie Y **/
    public int findSumY() {
        int sum = 0;
        
        for (int i = 0; i < SIZE; ++i) {
            int count = 0;
            for (int j = 0; j < SIZE; ++j) {
                count += intRates[j][i];
            }
            sum += (i + 1) * count;
        }
        return sum;
    }

    /** Sum of the products for the ratings movie X and Y **/
    public int findXY() {
        int sum = 0;
        
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                sum += (i + 1) * (j + 1) * intRates[i][j];
            }
        }
        return sum;
    }

    /** Sum of the squared of the ratings for movie X **/
    public int findXX() {
        int sum = 0;
        
        for (int i = 0; i < SIZE; ++i) {
            int count = 0;
            
            for (int j = 0; j < SIZE; ++j) {
                count += intRates[i][j];
            }
            sum += (i + 1) * (i + 1) * count;
        }
        return sum;
    }

    /** Sum of the squared of the ratings for movie Y **/
    public int findYY() {
        int sum = 0;
        
        for (int i = 0; i < SIZE; ++i) {
            int count = 0;
            
            for (int j = 0; j < SIZE; ++j) {
                count += intRates[j][i];
            }
            sum += (i + 1) * (i + 1) * count;
        }
        return sum;
    }

    public int getCounter() {
        return counter;
    }

    public float getSimularity() {
        return simularity;
    }

    public void setSimularity(float simularity) {
        this.simularity = simularity;
    }
}
