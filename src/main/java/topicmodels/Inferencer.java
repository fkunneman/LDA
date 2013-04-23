package topicmodels;


import util.Corpus;
import util.Document;

import java.util.ArrayList;

public class Inferencer extends TopicModel {

    protected InferenceSampler sampler;

    public ArrayList<Integer> documentTypes;
    public ArrayList<Integer> documentTopics;

    public Inferencer () {
        documentTypes = new ArrayList<Integer>();
        documentTopics = new ArrayList<Integer>();
    }

    public void initSampler (Corpus corpus, LearnSampler sampler) {
        this.corpus = corpus;
        this.sampler = new InferenceSampler(sampler);
        numTypes = this.sampler.numTypes;
        numWords = this.sampler.numWords;
        numTopics = this.sampler.numTopics;
        for (Document document : corpus) {
            this.sampler.addDocument(document);
        }
        for (int type = 0; type < numTypes; type++) {
            documentTypes.add(type);
        }
        for (int topic = 0; topic < numTopics; topic++) {
            documentTopics.add(topic);
        }
        logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
    }

    public void sampleForOneDocument (Document document) {
        int[] assignment;
        int[] docTypeCounts = new int[numTypes];
        for (Integer type: document.getTypeAssignments()) {
            docTypeCounts[type]++;
        }
        for (int position = 0; position < document.size(); position++) {
            int word = document.getToken(position);
            if (word >= numWords) {
                continue;
            }
            int topic = document.getTopic(position);
            int type = document.getType(position);
            sampler.decrement(topic, word, type);
            docTypeCounts[type]--;
            assignment = sampler.sample(word, documentTopics, documentTypes, docTypeCounts);
            topic = assignment[1]; type = assignment[2];
            sampler.increment(topic, word, type);
            docTypeCounts[type]++;
            document.setTopic(position, topic);
            document.setType(position, type);
        }
    }
}
