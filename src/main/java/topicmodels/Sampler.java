package topicmodels;

import util.Index;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

abstract public class Sampler implements Serializable {

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

    //public int[] sample (int word, int[] documentTopicCounts, ArrayList<Integer> labels, ArrayList<Integer> types) {
    public int[] sample (int word, ArrayList<Integer> labels, ArrayList<Integer> types) {
        double[][] topicTermScores = new double[labels.size()][types.size()];
        double sum = 0.0;
        for (int i = 0; i < types.size(); i++) {
            int type = types.get(i);
            for (int j = 0; j < labels.size(); j++) {
                int topic = labels.get(j);
                //double score = (alpha + documentTopicCounts[topic]) *
                //        (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]) *
                //        (typeWordCounts[type][topic] + gamma) / (gammaSum + typeCounts[type]);
                double score = (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]) *
                               (gamma + typeTopicCounts[type][topic]) / (gammaSum + typeCounts[type]);
                sum += score;
                topicTermScores[j][i] = score;
            }
        }
        double sample = Math.random() * sum;
        int topic = -1; int type = -1;
        while (sample > 0.0) {
            topic++;
            type++;
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
        return new int[]{word, topic, 1};
    }

    public void write (File file) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        outputStream.writeObject(this);
        outputStream.close();
    }

    public void writeObject (ObjectOutputStream outputStream) throws IOException {
        outputStream.writeObject(typeCounts);
        outputStream.writeObject(typeTopicCounts);
        outputStream.writeObject(topicCounts);
        outputStream.writeObject(wordTopicCounts);

        outputStream.writeDouble(alpha);
        outputStream.writeDouble(beta);
        outputStream.writeDouble(betaSum);
        outputStream.writeDouble(gamma);
        outputStream.writeDouble(gammaSum);

        outputStream.writeInt(numTopics);
        outputStream.writeInt(numTypes);
        outputStream.writeInt(numWords);

        outputStream.writeObject(random);

        outputStream.writeObject(labelIndex);
        outputStream.writeObject(typeIndex);
        outputStream.writeObject(wordIndex);

    }
}
