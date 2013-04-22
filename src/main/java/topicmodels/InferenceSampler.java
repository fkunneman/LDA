package topicmodels;

import util.Document;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;


public class InferenceSampler extends Sampler {

    public InferenceSampler (LearnSampler model) {
        this.alpha = model.alpha;
        this.beta = model.beta;
        this.betaSum = beta * model.numWords;
        this.gammaSum = gamma * numTypes;
        this.gamma = model.gamma;
        this.numTopics = model.numTopics;
        this.numTypes = model.numTypes;
        this.numWords = model.numWords;

        // Initialize the counting arrays.
        typeCounts = new int[numTypes];
        typeTopicCounts = new int[numTypes][numTopics];
        topicCounts = new int[numTopics];
        wordTopicCounts = new int[numWords][numTopics];

        // random
        random = model.random;

        // indexes
        labelIndex = model.labelIndex;
        typeIndex = model.typeIndex;
        wordIndex = model.wordIndex;
    }

    public void addDocument (Document document) {
        for (int position = 0; position < document.size(); position++) {
            int topic = random.nextInt(numTopics);
            int type = random.nextInt(numTypes);
            document.setTopic(position, topic);
            document.setType(position, type);
        }
    }

    public void increment (int topic, int word, int type) {}
    public void decrement (int topic, int word, int type) {}
}
