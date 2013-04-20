package topicmodels;


import util.Document;

import java.util.ArrayList;
import java.util.Random;

public class Sampler {
    // initialize some arrays for storing counts
    protected int[] typeCounts;
    protected int[][] typeWordCounts;
    protected int[] topicCounts;
    protected int[][] wordTopicCounts;

    // hyper-parameters
    private double alpha;
    private double beta;
    private double gamma;
    private double betaSum;
    private double gammaSum;

    // statistics
    private int numTopics;

    private Random random;

    public Sampler (int numTopics, int numWords, int numTypes, double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.betaSum = beta * numWords;
        this.gammaSum = gamma * numTypes;
        this.gamma = gamma;
        this.numTopics = numTopics;

        // Initialize the counting arrays.
        typeCounts = new int[numTypes];
        typeWordCounts = new int[numTypes][numWords];
        topicCounts = new int[numTopics];
        wordTopicCounts = new int[numWords][numTopics];

        // random
        random = new Random();
    }

    public void addDocument (Document document) {
        int type = document.getType();
        for (int position = 0; position < document.size(); position++) {
            ArrayList<Integer> labels = document.getLabels();
            int topic = labels.get(random.nextInt(labels.size()));
            document.setTopic(position, topic);
            increment(topic, document.getToken(position), type);
        }
    }

    public void decrement (int topic, int word, int type) {
        typeCounts[type]--;
        typeWordCounts[type][word]--;
        topicCounts[topic]--;
        wordTopicCounts[word][topic]--;
    }

    public void increment (int topic, int word, int type) {
        typeCounts[type]++;
        typeWordCounts[type][word]++;
        topicCounts[topic]++;
        wordTopicCounts[word][topic]++;
    }

    public int sample (int word, int type, int[] documentTopicCounts, ArrayList<Integer> labels) {
        double[] topicTermScores = new double[numTopics];
        double score;
        double sum = 0.0;
        for (Integer topic : labels) {
            // P(z=t,T=t|z_-i, etc.)
            score = (alpha + documentTopicCounts[topic]) *
                    (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]) *
                    (typeWordCounts[type][word] + gamma) / (gammaSum + typeCounts[type]);
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
