package util;


import java.util.ArrayList;
import java.util.Random;

public class Randoms extends Random {
    private static final long serialVersionUID = 1L;

    public Randoms() {
        super();
    }

    public Randoms(long seed) {
        super(seed);
    }

    public synchronized double nextUniform() {
        long l = ((long)(next(26)) << 27) + next(27);
        return l / (double)(1L << 53);
    }

    public int choice (ArrayList<Integer> items) {
        return items.get(nextInt(items.size()));
    }
}
