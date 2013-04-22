package topicmodels;


import util.Corpus;
import util.Document;
import util.IDSorter;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.logging.Logger;

public class Learner extends TopicModel {

    public LearnSampler sampler;

    public Learner(double alphaSum, double beta, double gamma) {
        this.alphaSum = alphaSum;
        this.beta = beta;
        this.gamma = gamma;
    }

    public void initSampler (Corpus corpus) {
        this.corpus = corpus;
        numTopics = corpus.getNumTopics();
        alpha = alphaSum / numTopics;
        sampler = new LearnSampler(corpus, alpha, beta, gamma);
        for (Document document : corpus) {
            sampler.addDocument(document);
        }
        logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
    }

    public void sampleForOneDocument (Document document) {
        int[] documentTopicCounts = new int[numTopics];
        int[] assignment;
        // count for each assigned topic in this document how often it has been assigned.
        // (This could be stored in a separate array, but is more memory efficient)
        for (int topic : document.getTopicAssignments()) {
            documentTopicCounts[topic]++;
        }
        for (int position = 0; position < document.size(); position++) {
            int word = document.getToken(position);
            int topic = document.getTopic(position);
            int type = document.getType(position);
            sampler.decrement(topic, word, type);
            documentTopicCounts[topic]--;
            assignment = sampler.sample(word, document.getLabels(), document.getTypes());
            topic = assignment[1]; type = assignment[2];
            sampler.increment(topic, word, type);
            documentTopicCounts[topic]++;
            document.setTopic(position, topic);
            document.setType(position, type);
        }
    }


}
