import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import Jama.Matrix;


public class EucDist implements Runnable {

	/** Fields **/
	private KNNApp app;
	private int start;
	private int end;
	private int threadID;

	/** Constructor **/
	public EucDist(KNNApp a, int s, int e, int t) {
		this.app = a;
		this.start = s;
		this.end = e;
		this.threadID = t;
	}

	@Override
	public void run() {
		// Delete if sim file already exists
		// If don't delete, then will only append to file
		try{
			File simFile = new File(Integer.toString(threadID) + ".dta");
			if (simFile.exists()) {
				simFile.delete();
			}   
		} catch(Exception e){
			e.printStackTrace();
		}

		// Calculate similarities
		try {
			PrintWriter out = new PrintWriter (new BufferedWriter
					         (new FileWriter(Integer.toString(threadID) + ".dta", true)));

			for (int i = start; i < end - 1; i++) {

				HashMap<Integer, Integer> m1 = app.movieHash.get(i);
				Set<Integer> m1_users = m1.keySet();

				for (int j = i + 1; j < end; j++) {
					HashMap<Integer, Integer> m2 = app.movieHash.get(j);
					Set<Integer> m2_users = m2.keySet();

					// Find intersection between user sets
					Set<Integer> user_intersect = new HashSet<Integer>();
					user_intersect.addAll(m1_users);
					user_intersect.retainAll(m2_users);

					// Don't bother with calculations if the intersection is empty
					if (user_intersect.size() == 0) {
						out.print(KNNApp.FORMAT_PRECISION.format(0) + " ");
						break;
					}

					// Extract the vectors of ratings of the overlapping users			
					int size = user_intersect.size();
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

					// Output similarities to text file
					out.print(KNNApp.FORMAT_PRECISION.format(sim) + " ");
				}

				// Start a new line for next movie
				out.println(" ");
			}

			// Close the file
			out.close();

		} catch (IOException e1) {
			e1.printStackTrace();
		}

	}

}
