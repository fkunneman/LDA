package topicmodels;

import util.Corpus;
import util.Document;
import util.Index;
import util.Randoms;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.logging.Logger;

public class LLDA implements Serializable {
    public static Logger logger = Logger.getLogger(LLDA.class.getName());

    LLDA.LearnSampler learnSampler;
    LLDA.InferSampler inferSampler;

    public int numTopics;
    public int numWords;

    // hyper-parameters
    public double alpha;
    public double beta;
    public double betaSum;

    // count matrices
    protected int[] topicCounts;
    protected int[][] wordTopicCounts;

    // Indexes
    protected Index topicIndex;
    protected Index wordIndex;

    protected Randoms random;
    protected Boolean learned;

    public LLDA (double alpha, double beta, Corpus corpus) {
        numTopics = corpus.getNumTopics();
        numWords = corpus.getNumWords();
        this.alpha = alpha;
        this.beta = beta;
        betaSum = beta * numWords;

        topicCounts = new int[numTopics];
        wordTopicCounts = new int[numWords][numTopics];

        topicIndex = corpus.getLabelIndex();
        wordIndex = corpus.getWordIndex();

        random = new Randoms(20);
    }

    public void train (int iterations, Corpus corpus) {

    }

    public void infer (int iterations, Corpus corpus) {

    }

    public class Sampler {

        public void sampleForOneDocument (Document document, ArrayList<Integer> labels) {
            int[] docTopicCounts = new int[numTopics];
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
                topic = sample(word, labels, docTopicCounts);
                increment(topic, word);
                docTopicCounts[topic]++;
                document.setTopic(position, topic);
            }
        }

        public void sampleForOneDocument (Document document) {
            sampleForOneDocument(document, document.getLabels());
        }

        public void addDocument (Document document, ArrayList<Integer> labels) {
            for (int position = 0; position < document.size(); position++) {
                int topic = random.choice(labels);
                document.setTopic(position, topic);
                increment(topic, document.getToken(position));
            }
        }

        public void increment (int topic, int word) {}
        public void decrement (int topic, int word) {}

        public int sample (int word, ArrayList<Integer> labels, int[] docTopicCounts) {
            double[] topicTermScores = new double[labels.size()];
            double sum = 0.0;
            for (int i = 0; i < labels.size(); i++) {
                int topic = labels.get(i);
                double score = (alpha + docTopicCounts[topic]) *
                               (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]);
                sum += score;
                topicTermScores[i] = score;
            }
            double sample = random.nextUniform() * sum;
            int topic = -1;
            while (sample > 0.0) {
                topic++;
                sample -= topicTermScores[topic];
            }
            return labels.get(topic);
        }
    }

    public class LearnSampler extends LLDA.Sampler {

        public void addDocument (Document document) {
            addDocument(document, document.getLabels());
        }

        public void increment (int topic, int word) {
            topicCounts[topic]++;
            wordTopicCounts[word][topic]++;
        }

        public void decrement (int topic, int word) {
            topicCounts[topic]--;
            wordTopicCounts[word][topic]--;
        }
    }

    public class InferSampler extends LLDA.Sampler {

        private ArrayList<Integer> documentTopics;

        public InferSampler () {
            documentTopics = new ArrayList<Integer>();
            for (int topic = 0; topic < numTopics; topic++) {
                documentTopics.add(topic);
            }
        }

        public void addDocument (Document document) {
            addDocument(document, documentTopics);
        }

        public void sampleForOneDocument (Document document) {
            sampleForOneDocument(document, documentTopics);
        }
    }
}
