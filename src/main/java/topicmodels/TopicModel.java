package topicmodels;


import util.Corpus;
import util.Document;
import util.IDSorter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Logger;

abstract public class TopicModel {
    public Logger logger = Logger.getLogger(Learner.class.getName());
    // initialize the vocabulary size and the number of topics
    protected int numTopics;

    // initialize the hyper-parameters
    protected double alphaSum;
    protected double alpha;
    protected double beta;
    protected double gamma;

    protected Corpus corpus;

    public void sample (int iterations) {
        for (int iteration = 1; iteration <= iterations; iteration++) {
            logger.info("Sampling iteration " + iteration + " started.");
            for (Document document: corpus) {
                sampleForOneDocument(document);
            }
        }
    }

    public void sampleForOneDocument (Document document) {}

    public void writeTopicDistributions (File file) throws IOException {
        PrintWriter printer = new PrintWriter(file);
        printer.print("type\tsource\ttopic:proportion...\n");
        for (Document document : corpus) {
            //printer.print(corpus.getTypeIndex().getItem(document.getType()) + "\t");
            printer.print(document.getSource() + "\t");
            IDSorter[] sortedTopics = new IDSorter[numTopics];
            int[] topicCounts = new int[numTopics];
            for (int topic : document.getTopicAssignments()) {
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
