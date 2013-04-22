package topicmodels;


import util.Corpus;
import util.Document;

import java.util.ArrayList;

public class Inferencer extends TopicModel {

    protected InferenceSampler sampler;

    public ArrayList<Integer> documentTypes;
    public ArrayList<Integer> documentTopics;

    public Inferencer () {}

    public void initSampler (Corpus corpus, LearnSampler sampler) {
        this.corpus = corpus;
        this.sampler = new InferenceSampler(sampler);
        for (Document document : corpus) {
            this.sampler.addDocument(document);
        }
        for (int type = 0; type < this.sampler.numTypes; type++) {
            documentTypes.add(type);
        }
        for (int topic = 0; topic < this.sampler.numTopics; topic++) {
            documentTopics.add(topic);
        }
        logger.info("Sampler initialized. " + this.sampler.numTopics + " topics and " + corpus.size() + " documents.");
    }

    public void sampleForOneDocument (Document document) {
        int[] documentTopicCounts = new int[sampler.numTopics];
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
            assignment = sampler.sample(word, documentTopics, documentTypes);
            topic = assignment[1]; type = assignment[2];
            sampler.increment(topic, word, type);
            documentTopicCounts[topic]++;
            document.setTopic(position, topic);
            document.setType(position, type);
        }
    }
}
