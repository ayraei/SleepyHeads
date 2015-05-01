
/** Movie n as neighbor of movie m **/
public class MovieNeighbor {

	/** Fields **/
	int commonViewers;
	float mAvg;    //average rating of movie m
	float nAvg;    //average rating of movie n

	int nRating; //current viewer's rating for movie n

	float rRaw;    //raw Pearson's r value
	float rLower; 
	float weight;

	/** Methods **/
	void setCV(int cv) {
		this.commonViewers = cv;
	}

	void setMAvg(float m) {
		this.mAvg = m;
	}

	void setNAvg(float n) {
		this.nAvg = n;
	}

	void setNRating(int r) {
		this.nRating = r;
	}

	void setRRaw(float r) {
		this.rRaw = r;
	}

	void setRLower(float rl) {
		this.rLower = rl;
	}

	void setWeight(float w) {
		this.weight = w;
	}

	void calcRLower() {
		/*
		 * Steps for computing a confidence interval: 
		 * 1. Convert r to z with Fisher's z transformation
		 * 2. Compute a confidence interval in terms of z
		 * 3. Convert the confidence interval back to r
		 * where Z for a 95% confidence interval is 1.96. Alternatively, we
		 * could use a stricter 99% confidence interval (Z = 2.58).
		 */
		double zr = 0.5 * Math.log((1 + (double) this.rRaw) / (1 - (double) this.rRaw));
		double interval = 1.96 * Math.sqrt(1 / ((double) this.commonViewers - 3.0));
		zr -= interval;
		float rl = (float) ((Math.exp(2 * zr) - 1) / (Math.exp(2 * zr) + 1));
		
		// Check if same sign
		if (this.rRaw * rl < 0) {
			rl = 0;
		}
		setRLower(rl);	
	}

	void calcWeight() {
		float weight = this.rLower * this.rLower * (float) Math.log(this.commonViewers);
		setWeight(weight);
	}
	
	float getWeight() {
		return this.weight;
	}

	float getMAvg() {
		return this.mAvg;
	}

	float getNAvg() {
		return this.nAvg;
	}

	float getNRating() {
		return this.nRating;
	}

	float getRRaw() {
		return this.rRaw;
	}
}
