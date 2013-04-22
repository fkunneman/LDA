package topicmodels;


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
    }

    public void increment (int topic, int word, int type) {}
    public void decrement (int topic, int word, int type) {}
}
