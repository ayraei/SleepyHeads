import java.util.Arrays;

public class Ratings {
    private final static int SIZE = 5;

    private int[][] intRates = new int[SIZE][SIZE];

    private int counter = 0;
    private float simularity;

    public void rate(int i, int j) {
        ++counter;
        intRates[i - 1][j - 1]++;
    }

    public int get(int i, int j) {
        return intRates[i][j];
    }

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

    public int findXY() {
        int sum = 0;
        for (int i = 0; i < SIZE; ++i) {
            for (int j = 0; j < SIZE; ++j) {
                sum += (i + 1) * (j + 1) * intRates[i][j];
            }
        }
        return sum;
    }

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

    @Override
    public String toString() {
        return "Ratings{" +
                "intRates=" + (intRates == null ? null : Arrays.asList(intRates)) +
                ", counter=" + counter +
                ", simularity=" + simularity +
                '}';
    }
}
