
/** 
 * Basic object representing a movie to be queued
 */
public class CalcData {
    private final Integer index;
    private final Integer movieId;
    private final Integer totalMovies;

    public CalcData(Integer index, Integer movieId, Integer totalMovies) {
        this.index = index;
        this.movieId = movieId;
        this.totalMovies = totalMovies;
    }

    public Integer getIndex() {
        return index;
    }

    public Integer getMovieID() {
        return movieId;
    }

    public Integer getTotalMovies() {
        return totalMovies;
    }
}
