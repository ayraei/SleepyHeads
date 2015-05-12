import java.util.Comparator;

public class CompNhbrs implements Comparator<MovieNeighbor>{

	@Override
	public int compare(MovieNeighbor m1, MovieNeighbor m2) {
		Float f1 = new Float(m1.getWeight());
		Float f2 = new Float(m2.getWeight());
		return f2.compareTo(f1);
	}
}