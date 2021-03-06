/**
 * Use Euclidean Distance to calculate similarity between two movies
 */

import Jama.Matrix;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

public class EucDist implements Runnable {

    /** Fields **/
    private int threadID;
    private final BlockingQueue<CopyOfCalcData> queue;
    private final Matrix results;
    private final CalcStatistics statistics;

    /** Constructor **/
    public EucDist(int t, BlockingQueue<CopyOfCalcData> queue, Matrix results, CalcStatistics statistics) {
        this.threadID = t;
        this.queue = queue;
        this.results = results;
        this.statistics = statistics;
    }

    @Override
    public void run() {
        while (true) {
            try {
            	CopyOfCalcData calcData = queue.take();
                if (calcData.getRow() == -1) {
                    break;
                }
                
                double result = calcSimilarity(calcData);
                results.set(calcData.getRow(), calcData.getColumn(), result);
                results.set(calcData.getColumn(), calcData.getRow(), result);
                statistics.incAndGet();
                
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("ThreadId " + threadID + " ended.");
    }

    public double calcSimilarity(CopyOfCalcData calcData) {
        HashMap<Integer, Integer> m1 = calcData.getM1();
        HashMap<Integer, Integer> m2 = calcData.getM2();

        Set<Integer> m1_users = m1.keySet();
        Set<Integer> m2_users = m2.keySet();

        // Find intersection between user sets
        Set<Integer> user_intersect = new HashSet<Integer>();
        user_intersect.addAll(m1_users);
        user_intersect.retainAll(m2_users);

        // Extract the vectors of ratings of the overlapping users
        int size = user_intersect.size();

        // Don't bother with calculations if the intersection is empty
        if (size == 0) {
            return (double) 0;
        }

        Matrix u = new Matrix(size, 1);
        Matrix v = new Matrix(size, 1);

        int k = 0;
        for (int ui : user_intersect) {
            u.set(k, 0, m1.get(ui));
            v.set(k, 0, m2.get(ui));
            k++;
        }

        // Calculate similarity
        Matrix diff = u.minus(v);
        double temp = diff.transpose().times(diff).get(0, 0);
        double sim = Math.sqrt(temp);

        return sim;
    }

}