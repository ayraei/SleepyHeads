
public class TestingApp {

	public static void main(String[] args) {
		float rRaw = 0.99f;
		int commonViewers = 1898;
		
		System.out.println("inside log: " + (1 + (double) rRaw) / (1 - (double) rRaw));
		System.out.println("inside log numer: " + (1 + (double) rRaw));
		System.out.println("inside log denom: " + (1 - (double) rRaw));
		
		double zr = 0.5 * Math.log((1 + (double) rRaw) / (1 - (double) rRaw));
		System.out.println("zr1: " + zr);
		
		double interval = 1.96 * Math.sqrt(1 / ((double) commonViewers - 3.0));
		System.out.println("interval: " + interval);
		
		zr -= interval;
		System.out.println("zr2: " + zr);
		
		float rl = (float) ((Math.exp(2 * zr) - 1) / (Math.exp(2 * zr) + 1));
		System.out.println("rl1: " + rl);
		
		// Check if same sign
		if (rRaw * rl < 0) {
			rl = 0;
		}
		
		System.out.println("rl2: " + rl);
	}

}
