
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
		float rl = (float) (1.96 * (Math.log((1 + this.rRaw) / (1 - this.rRaw)) / 2) -
				(Math.sqrt(1 / (this.commonViewers) - 3)));
		rl = (float) ((Math.exp(2 * rl) - 1) / (Math.exp(2 * rl) + 1));
		
		// Check if same sign
		if (this.rLower * rl < 0) {
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
