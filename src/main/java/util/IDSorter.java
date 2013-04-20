package util;


public class IDSorter implements Comparable {

    private final Integer index;
    private final Double value;

    public IDSorter(Integer index, Double value) {
        this.index = index;
        this.value = value;
    }

    public final int compareTo(Object other_) {
        IDSorter other = (IDSorter) other_;
        if (value > other.value) {
            return -1;
        }
        if (other.value > value) {
            return 1;
        }
        if (index > other.index) {
            return -1;
        }
        if (other.index > index) {
            return 1;
        }
        return 0;
    }

    public int getIndex() {
        return index;
    }

    public double getValue() {
        return value;
    }
}

