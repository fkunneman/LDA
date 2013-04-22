package topicmodels;

import util.Document;


public class InferenceSampler extends Sampler {

    public InferenceSampler (LearnSampler model) {
        this.alpha = model.alpha;
        this.beta = model.beta;
        this.betaSum = beta * model.numWords;
        this.gammaSum = gamma * numTopics;
        this.gamma = model.gamma;
        this.numTopics = model.numTopics;
        this.numTypes = model.numTypes;
        this.numWords = model.numWords;

        // Initialize the counting arrays.
        typeCounts = model.typeCounts;
        typeTopicCounts = model.typeTopicCounts;
        topicCounts = model.topicCounts;
        wordTopicCounts = model.wordTopicCounts;

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
