package topicmodels;

import util.Document;


public class InferenceSampler extends Sampler {

    public InferenceSampler (LearnSampler model) {
        super(model.numTopics, model.numTypes, model.numWords, model.alpha, model.beta, model.gamma);

        // Initialize the counting arrays.
        typeCounts = model.typeCounts;
        typeTopicCounts = model.typeTopicCounts;
        topicCounts = model.topicCounts;
        wordTopicCounts = model.wordTopicCounts;

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
