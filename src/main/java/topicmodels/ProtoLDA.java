package topicmodels;

import util.Corpus;
import util.Document;
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
    protected int numWords;

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
    protected Boolean trained;

    public ProtoLDA(int numTopics, double alpha, double beta, double gamma,
                    Corpus corpus, HashMap<String, ArrayList<String>> protoTopics) {
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
        this.numWords = wordIndex.size();
        // define the new betaSum by incorporating the prototypical words and their gamma value.
        betaSum = corpus.getNumTypes() * beta + gamma * protoWords.size();
        random = new Randoms(20);

        // initialize the count matrices.
        topicCounts = new int[totalTopics];
        wordTopicCounts = new int[numWords][totalTopics];
    }

    /**
     * Given a corpus, learn the topic distribution per document and the
     * word distribution per topic.
     *
     * @param iterations the number of iterations to run the sampler;
     * @param corpus the corpus to run the sampler on;
     */
    public void train (int iterations, Corpus corpus) {
        learnSampler = new LearnSampler();
        for (Document document : corpus) {
            learnSampler.addDocument(document);
        }
        logger.info("Sampler initialized. " + regularTopics+ " regular topics, " +
                    (totalTopics - regularTopics) + "proto-topics and " + corpus.size() + " documents.");
        for (int iteration = 1; iteration <= iterations; iteration++) {
            logger.info("Sampling iteration " + iteration + " started.");
            for (Document document : corpus) {
                learnSampler.sampleForOneDocument(document);
            }
        }
        trained = true;
    }

    /**
     * Given a corpus of test documents, try to assign to each token
     * a topic based on a previously learned LLDA model.
     *
     * @param iterations how many iterations to run the sampler;
     * @param corpus the corpus to run the sampler on;
     */
    public void infer (int iterations, Corpus corpus) {
        inferSampler = new InferSampler();
        for (Document document : corpus) {
            inferSampler.addDocument(document);
        }
        logger.info("Sampler initialized. " + regularTopics+ " regular topics, " +
                    (totalTopics - regularTopics) + "proto-topics and " + corpus.size() + " documents.");
        for (int iteration = 1; iteration <= iterations; iteration++) {
            logger.info("Sampling iteration " + iteration + " started.");
            for (Document document : corpus) {
                inferSampler.sampleForOneDocument(document);
            }
        }
    }


    public class Sampler {

        public void sampleForOneDocument (Document document) {
            int[] docTopicCounts = new int[totalTopics];
            for (Integer topic : document.getTopicAssignments()) {
                docTopicCounts[topic]++;
            }
            for (int position = 0; position < document.size(); position++) {
                int word = document.getToken(position);
                if (word >= numWords) {
                    continue;
                }
                int topic = document.getTopic(position);
                decrement(topic, word);
                docTopicCounts[topic]--;
                topic = sample(word, docTopicCounts);
                increment(topic, word);
                docTopicCounts[topic]++;
                document.setTopic(position, topic);
            }
        }

        public void increment (int topic, int word) {}
        public void decrement (int topic, int word) {}

        public void addDocument (Document document) {
            for (int position = 0; position < document.size(); position++) {
                int topic = random.nextInt(totalTopics);
                document.setTopic(position, topic);
                increment(topic, document.getToken(position));
            }
        }

        public int sample(int word, int[] docTopicCounts) {
            double[] termTopicScores = new double[totalTopics];
            double sum = 0.0;
            for (int topic = 0; topic < totalTopics; topic++) {
                double score;
                if (topic < regularTopics) {
                    score = (alpha + docTopicCounts[topic]) *
                            (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]);
                } else {
                    score = (alpha + docTopicCounts[topic]) *
                            (protoBeta[(topic-regularTopics)][word] + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]);
                }
                sum += score;
                termTopicScores[topic] = score;
            }
            double sample = random.nextUniform() * sum;
            int topic = -1;
            while (sample > 0.0) {
                topic++;
                sample -= termTopicScores[topic];
            }
            if (topic == -1) {
                throw new IllegalStateException("No topic sampled!");
            }
            return topic;
        }
    }

    public class LearnSampler extends Sampler {

        public void increment (int topic, int word) {
            topicCounts[topic]++;
            wordTopicCounts[word][topic]++;
        }

        public void decrement (int topic, int word) {
            topicCounts[topic]--;
            wordTopicCounts[word][topic]--;
        }
    }

    public class InferSampler extends Sampler {}

}
