package topicmodels;


import util.Document;

import java.util.ArrayList;
import java.util.Random;

public class LearnSampler extends Sampler {

    public LearnSampler (int numTopics, int numWords, int numTypes, double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.betaSum = beta * numWords;
        this.gammaSum = gamma * numTypes;
        this.gamma = gamma;
        this.numTopics = numTopics;
        this.numTypes = numTypes;
        this.numWords = numWords;

        // Initialize the counting arrays.
        typeCounts = new int[numTypes];
        typeTopicCounts = new int[numTypes][numTopics];
        topicCounts = new int[numTopics];
        wordTopicCounts = new int[numWords][numTopics];

        // random
        random = new Random();
    }

    public void addDocument (Document document) {
        ArrayList<Integer> types = document.getTypes();
        for (int position = 0; position < document.size(); position++) {
            ArrayList<Integer> labels = document.getLabels();
            int topic = labels.get(random.nextInt(labels.size()));
            int type = types.get(random.nextInt(types.size()));
            document.setTopic(position, topic);
            document.setType(position, type);
            increment(topic, document.getToken(position), type);
        }
    }

    public void decrement (int topic, int word, int type) {
        typeCounts[type]--;
        typeTopicCounts[type][topic]--;
        topicCounts[topic]--;
        wordTopicCounts[word][topic]--;
    }

    public void increment (int topic, int word, int type) {
        typeCounts[type]++;
        typeTopicCounts[type][topic]++;
        topicCounts[topic]++;
        wordTopicCounts[word][topic]++;
    }
}
