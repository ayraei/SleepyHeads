import java.util.HashMap;

public class CalcData {
    private final Integer row;
    private final Integer column;
    private final HashMap<Integer, Integer> m1;
    private final HashMap<Integer, Integer> m2;

    public CalcData(Integer row, Integer column, HashMap<Integer, Integer> m1, HashMap<Integer, Integer> m2) {
        this.row = row;
        this.column = column;
        this.m1 = m1;
        this.m2 = m2;
    }

    public Integer getRow() {
        return row;
    }

    public Integer getColumn() {
        return column;
    }

    public HashMap<Integer, Integer> getM1() {
        return m1;
    }

    public HashMap<Integer, Integer> getM2() {
        return m2;
    }
}
