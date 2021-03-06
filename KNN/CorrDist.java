
import Jama.Matrix;

public class CorrDist {

	private static int size, uSum, vSum;
	private static Matrix u, v;

	public static double func() {
		// 1-(u-Mean[u]).(v-Mean[v]) / (Norm[u-Mean[u]] Norm[v-Mean[v]]
		double numer;
		double denom;

		Matrix uAvg = new Matrix(1, size, uSum / size);
		Matrix vAvg = new Matrix(size, 1, vSum / size);
		u.minusEquals(uAvg);
		v.minusEquals(vAvg);

		numer = u.times(v).get(0, 0);
		denom = u.normF() * v.normF();

		double sim = 1 - (numer / denom);
		
		return sim;
	}
}
