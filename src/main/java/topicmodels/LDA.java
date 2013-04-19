package topicmodels;


import util.Corpus;
import util.TopicAssignment;

import java.util.logging.Logger;

public class LDA {
    public Logger logger = Logger.getLogger(LDA.class.getName());

    // initialize the vocabulary size and the number of topics
    private int numTopics;
    private int numTypes;
    private int numWords;

    // initialize the hyper-parameters
    protected double alphaSum;
    protected double alpha;
    protected double beta;
    protected double gamma;

    protected Corpus corpus;
    protected Sampler sampler;

    public LDA (double alphaSum, double beta, double gamma) {
        this.alphaSum = alphaSum;
        this.beta = beta;
        this.gamma = gamma;
    }

    public void initSampler (Corpus corpus) {
        numTopics = corpus.getNumTopics();
        alpha = alphaSum / numTopics;
        numTypes = corpus.getNumTypes();
        numWords = corpus.getNumWords();
        sampler = new Sampler(numTopics, numWords, numTypes, alpha, beta, gamma);
        for (TopicAssignment document : corpus) {
            sampler.addDocument(document);
        }
    }

    public void sample (int iterations) {
        for (int iteration = 1; iteration <= iterations; iteration++) {
            for (TopicAssignment document: corpus) {
                sampleForOneDocument(document);
            }
        }
    }

    public void sampleForOneDocument (TopicAssignment document) {
        int type = document.getType();
        int[] documentTopicCounts = new int[numTopics];
        // count for each assigned topic in this document how often it has been assigned.
        // (This could be stored in a separate array, but is more memory efficient)
        for (int topic : document.getTopics()) {
            documentTopicCounts[topic]++;
        }
        for (int position = 0; position < document.size(); position++) {
            int word = document.getToken(position);
            int topic = document.getTopic(position);
            sampler.decrement(topic, word, type);
            documentTopicCounts[topic]--;
            topic = sampler.sample(word, type, documentTopicCounts);
            sampler.increment(topic, word, type);
            documentTopicCounts[topic]++;
            document.setTopic(position, topic);
        }
    }
}
