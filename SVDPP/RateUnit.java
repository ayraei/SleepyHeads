
/** 
 * Object pair for holding an ID (movie or user) and an associated rating
 */
public class RateUnit {
    private final Integer id;
    private final Integer rating;

    public RateUnit(Integer id, Integer rating) {
        this.id = id;
        this.rating = rating;
    }

    public Integer getID() {
        return id;
    }

    public Integer getRating() {
        return rating;
    }
}
