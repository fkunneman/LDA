package topicmodels;

import util.Corpus;
import util.Index;
import util.Randoms;

import java.util.ArrayList;
import java.util.logging.Logger;

public class ProtoLDA {

    public static Logger logger = Logger.getLogger(ProtoLDA.class.getName());

    LearnSampler learnSampler;
    InferSampler inferSampler;

    protected int numTopics;
    protected int numTypes;

    protected double alpha;
    protected double beta;
    protected double betaSum;
    protected double[][]

    protected int[] topicCounts;
    protected int[][] wordTopicCounts;

    public Index topicIndex;
    public Index wordIndex;

    protected Randoms random;

    public ProtoLDA(int numTopics, double alpha, double beta, Corpus corpus, ) {
        this.numTopics = numTopics;
        this.alpha = alpha;
        this.beta = beta;
        this.numTypes = corpus.getNumTypes();
        betaSum = numTypes * beta;

        topicIndex = new Index();
        for (int topic = 0; topic < numTopics; topic++) {
            topicIndex.put("Topic: " + topic);
        }
        wordIndex = corpus.getWordIndex();

        random = new Randoms(20);
    }

    public void addProtoTopics (ArrayList<String> topics) {
        for (String topic : topics) {
            numTopics += 1;
            topicIndex.put(topic);
        }
    }

    public class Sampler {}

    public class LearnSampler extends Sampler {}

    public class InferSampler extends Sampler {}

}
