import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class CalcStatistics {
    private final Date startTime = new Date();
    private final AtomicInteger processedCount = new AtomicInteger(0);
    private Integer totalCount = 1;

    public long incAndGet() {
        return processedCount.incrementAndGet();
    }

    public String getStatistics() {
        return "Processed " + processedCount.get() + " " + progress() + "% time " + timeElapse();
    }

    public long progress() {
        return processedCount.get() * 100 / totalCount;
    }

    public long getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(Integer totalCount) {
        this.totalCount = totalCount;
    }

    public String timeElapse() {
        Date now = new Date();
        long diff = now.getTime() - startTime.getTime();

        long diffSeconds = diff / 1000 % 60;
        long diffMinutes = diff / (60 * 1000) % 60;
        long diffHours = diff / (60 * 60 * 1000) % 24;
        long diffDays = diff / (24 * 60 * 60 * 1000);

        StringBuilder builder = new StringBuilder();
        if (diffDays > 0) {
            builder.append(diffDays + " days, ");
        }
        if (diffHours > 0) {
            builder.append(diffHours + " hours, ");
        }
        if (diffMinutes > 0) {
            builder.append(diffMinutes + " minutes, ");
        }
        builder.append(diffSeconds + " seconds.");

        return builder.toString();
    }

    @Override
    public String toString() {
        return getStatistics();
    }
}
