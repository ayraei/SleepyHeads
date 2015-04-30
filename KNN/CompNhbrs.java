import java.util.Comparator;

public class CompNhbrs implements Comparator<MovieNeighbor>{
	 
    @Override
    public int compare(MovieNeighbor m1, MovieNeighbor m2) {
        if(m1.getWeight() < m2.getWeight()){
            return 1;
        } else {
            return -1;
        }
    }
}