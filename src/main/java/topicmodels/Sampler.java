package topicmodels;

import java.util.ArrayList;
import java.util.Random;

abstract public class Sampler {

    // initialize some arrays for storing counts
    protected int[] typeCounts;
    protected int[][] typeWordCounts;
    protected int[] topicCounts;
    protected int[][] wordTopicCounts;

    // hyper-parameters
    public double alpha;
    public double beta;
    public double gamma;
    public double betaSum;
    public double gammaSum;

    // statistics
    public int numTopics;
    public int numTypes;
    public int numWords;

    public Random random;

    public void increment (int topic, int word, int type) {}
    public void decrement (int topic, int word, int type) {}

    public int sample (int word, int type, int[] documentTopicCounts, ArrayList<Integer> labels) {
        double[] topicTermScores = new double[numTopics];
        double sum = 0.0;
        for (Integer topic : labels) {
            // P(z=t,T=t|z_-i, etc.)
            double score = (alpha + documentTopicCounts[topic]) *
                    (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]) *
                    (typeWordCounts[type][topic] + gamma) / (gammaSum + typeCounts[type]);
            sum += score;
            topicTermScores[topic] = score;
        }
        double sample = Math.random() * sum;
        int topic = -1;
        while (sample > 0.0) {
            topic++;
            sample -= topicTermScores[topic];
        }
        if (topic == -1) {
            throw new IllegalStateException("No index sampled.");
        }
        return topic;
    }
}
