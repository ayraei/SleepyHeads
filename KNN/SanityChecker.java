import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class SanityChecker {
    /**
     * Location of input files *
     */
    private static String SANITY_FILE_LOC =
            "/Users/debbie1/Documents/NetflixData/output/sanity.dta";
    private static PrintWriter sanityOut = null;

    static {
        try {
            sanityOut = new PrintWriter(new FileOutputStream(SANITY_FILE_LOC, true));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    static public void checkRatingAvg(String module, int cv, float sum, Ratings ratings, int m1, int m2) {
        if (cv == 0) {
            return;
        }

        float avg = sum / cv;
        if (avg > 5.0 || avg < 1.0) {
            sanityOut.println(module + " Rating avg=" + avg +
                    "cv=" + cv + " sum=" + sum +
                    " m1=" + m1 + " m2=" + m2 +
                    " rating=" + ratings);
            sanityOut.flush();
        }
    }

    static public void checkRatingAvg(String module, int cv, float sum, int m1, int m2) {
        if (cv == 0) {
            return;
        }

        float avg = sum / cv;
        if (avg > 5.0 || avg < 1.0) {
            sanityOut.println(module + " Rating avg=" + avg +
                    "cv=" + cv + " sum=" + sum +
                    " m1=" + m1 + " m2=" + m2);
            sanityOut.flush();
        }
    }


}
