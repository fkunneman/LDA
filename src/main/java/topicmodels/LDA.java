package topicmodels;


import util.Corpus;
import util.TopicAssignment;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;

public class LDA {
    public Logger logger = Logger.getLogger(LDA.class.getName());

    // initialize the vocabulary size and the number of topics
    private int numTopics;

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
        this.corpus = corpus;
        numTopics = corpus.getNumTopics();
        alpha = alphaSum / numTopics;
        sampler = new Sampler(numTopics, corpus.getNumWords(), corpus.getNumTypes(), alpha, beta, gamma);
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

    public void writeTopicDistributions (File file) throws IOException {
        PrintWriter printer = new PrintWriter(file);
        printer.print("type\tsource\ttopic:proportion...\n");
        for (TopicAssignment document : corpus) {
            printer.print(corpus.getTypeIndex().getItem(document.getType()) + "\t");
            printer.print(document.getSource() + "\t");
            int[] topicCounts = new int[numTopics];
            for (int topic : document.getTopics()) {
                topicCounts[topic]++;
            }
            // TODO compute proportion of topics per document!
        }
    }
}
