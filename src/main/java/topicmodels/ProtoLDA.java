package topicmodels;

import util.Corpus;
import util.Index;
import util.Randoms;

import java.util.*;
import java.util.logging.Logger;

public class ProtoLDA {

    public static Logger logger = Logger.getLogger(ProtoLDA.class.getName());

    LearnSampler learnSampler;
    InferSampler inferSampler;

    protected int regularTopics;
    protected int totalTopics;
    protected int numTypes;

    protected double alpha;
    protected double beta;
    protected double gamma;
    protected double betaSum;
    protected double[][] protoBeta;

    protected int[] topicCounts;
    protected int[][] wordTopicCounts;

    public Index topicIndex;
    public Index wordIndex;

    protected Randoms random;

    public ProtoLDA(int numTopics, double alpha, double beta, double gamma, Corpus corpus, HashMap<String, ArrayList<String>> protoTopics) {
        this.regularTopics = numTopics;
        this.alpha = alpha;
        this.beta = beta;
        this.gamma = gamma;

        topicIndex = new Index();
        for (int topic = 0; topic < numTopics; topic++) {
            topicIndex.put("Topic: " + topic);
        }

        wordIndex = corpus.getWordIndex();

        HashSet<String> protoWords = new HashSet<String>();
        // iterate over the proto-topics.
        for (Map.Entry<String, ArrayList<String>> entry : protoTopics.entrySet()) {
            String topic = entry.getKey();
            // add each topic to the topicIndex
            topicIndex.put(topic);
            // loop over the prototypical words of this topic
            ArrayList<String> words = entry.getValue();
            for (String word : words) {
                wordIndex.put(word);
                protoWords.add(word);
            }
        }
        // we only need a asymmetric beta prior for our proto-topics.
        protoBeta = new double[protoTopics.size()][wordIndex.size()];
        // first, fill the array with the default beta value.
        Arrays.fill(protoBeta, beta);
        int topic = 0;
        // now for each topic and its prototypical words
        for (ArrayList<String> words : protoTopics.values()) {
            for (String word : words) {
                // add the gamma prior to the beta prior.
                protoBeta[topic][wordIndex.getId(word)] += gamma;
            }
            topic++;
        }

        this.totalTopics = topicIndex.size();
        this.numTypes = wordIndex.size();
        // define the new betaSum by incorporating the prototypical words and their gamma value.
        betaSum = corpus.getNumTypes() * beta + gamma * protoWords.size();
        random = new Randoms(20);

        // initialize the count matrices.
        topicCounts = new int[totalTopics];
        wordTopicCounts = new int[numTypes][totalTopics];
    }

    public class Sampler {}

    public class LearnSampler extends Sampler {}

    public class InferSampler extends Sampler {}

}
