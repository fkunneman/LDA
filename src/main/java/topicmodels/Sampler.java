package topicmodels;

import util.Index;

import java.util.ArrayList;
import java.util.Random;

abstract public class Sampler {

    // initialize some arrays for storing counts
    protected int[] typeCounts;
    protected int[][] typeTopicCounts;
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

    // Indexes
    public Index wordIndex;
    public Index labelIndex;
    public Index typeIndex;

    public int[] sample (int word, ArrayList<Integer> labels, ArrayList<Integer> types) {
        double[][] topicTermScores = new double[labels.size()][types.size()];
        double sum = 0.0;
        for (int i = 0; i < types.size(); i++) {
            int type = types.get(i);
            for (int j = 0; j < labels.size(); j++) {
                int topic = labels.get(j);
                double score = (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]) *
                               (gamma + typeTopicCounts[type][topic]) / (gammaSum + typeCounts[type]);
                sum += score;
                topicTermScores[j][i] = score;
            }
        }
        double sample = Math.random() * sum;
        int topic = -1; int type = 0;
        while (sample > 0.0) {
            topic++;
            if (topic == labels.size()) {
                type++;
                topic = 0;
            }
            sample -= topicTermScores[topic][type];
        }
        if (topic == -1) {
            throw new IllegalStateException("No topic sampled.");
        }
        if (type == -1) {
            throw new IllegalStateException("No type sampled");
        }
        return new int[]{word, labels.get(topic), types.get(type)};
    }
}
