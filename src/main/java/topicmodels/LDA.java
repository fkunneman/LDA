package topicmodels;


import util.Corpus;
import util.Document;
import util.IDSorter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
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
        for (Document document : corpus) {
            sampler.addDocument(document);
        }
        logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
    }

    public void sample (int iterations) {
        for (int iteration = 1; iteration <= iterations; iteration++) {
            logger.info("Sampling iteration " + iteration + " started.");
            for (Document document: corpus) {
                sampleForOneDocument(document);
            }
        }
    }

    public void sampleForOneDocument (Document document) {
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
            topic = sampler.sample(word, type, documentTopicCounts, document.getLabels());
            sampler.increment(topic, word, type);
            documentTopicCounts[topic]++;
            document.setTopic(position, topic);
        }
    }

    public void writeTopicDistributions (File file) throws IOException {
        PrintWriter printer = new PrintWriter(file);
        printer.print("type\tsource\ttopic:proportion...\n");
        for (Document document : corpus) {
            printer.print(corpus.getTypeIndex().getItem(document.getType()) + "\t");
            printer.print(document.getSource() + "\t");
            IDSorter[] sortedTopics = new IDSorter[numTopics];
            int[] topicCounts = new int[numTopics];
            for (int topic : document.getTopics()) {
                topicCounts[topic]++;
            }
            for (int topic = 0; topic < numTopics; topic++) {
                sortedTopics[topic] = new IDSorter(topic, (alpha + topicCounts[topic]) / (document.size() + alphaSum));
            }
            Arrays.sort(sortedTopics);
            for (int index = 0; index < numTopics; index++) {
                double score = sortedTopics[index].getValue();
                if (score == 0.0) { break; }
                printer.print(corpus.getLabelIndex().getItem(sortedTopics[index].getIndex()) + " " + score);
            }
            printer.print("\n");
        }
        printer.close();
    }
}
