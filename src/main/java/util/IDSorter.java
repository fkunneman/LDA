package util;


public class IDSorter implements Comparable {

    private final int index;
    private final double value;

    public IDSorter(int index, double value) {
        this.index = index;
        this.value = value;
    }

    public IDSorter(int index, int value) {
        this.index = index;
        this.value = value;
    }

    public final int compareTo(Object other) {
        double otherP = ((IDSorter) other).value;
        if (value > ((IDSorter) other).value) {
            return -1;
        }
        if (value < ((IDSorter) other).value) {
            return 1;
        }
        int otherID = ((IDSorter) other).index;
        if (index > otherID) { return -1; }
        else if (index < otherID) { return 1; }

        return 0;
    }

    public int getIndex() {
        return index;
    }

    public double getValue() {
        return value;
    }
}

