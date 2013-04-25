package topicmodels;

import util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
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
    public Index topicIndex;
    public Index wordIndex;

    protected Randoms random;
    protected Boolean trained = false;

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
        learnSampler = new LLDA.LearnSampler();
        for (Document document : corpus) {
            learnSampler.addDocument(document);
        }
        logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
        for (int iteration = 1; iteration <= iterations; iteration++) {
            logger.info("Sampling iteration " + iteration + " started.");
            for (Document document : corpus) {
                learnSampler.sampleForOneDocument(document);
            }
        }
        trained = true;
    }

    public void infer (int iterations, Corpus corpus) {
        inferSampler = new LLDA.InferSampler();
        for (Document document : corpus) {
            inferSampler.addDocument(document);
        }
        logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
        for (int iteration = 1; iteration <= iterations; iteration++) {
            logger.info("Sampling iteration " + iteration + " started.");
            for (Document document : corpus) {
                inferSampler.sampleForOneDocument(document);
            }
        }
    }

    public void writeTopicDistributions (File file, Corpus corpus, double smooth) throws IOException {
        PrintWriter printer = new PrintWriter(file);
        printer.print("source\ttopic:proportion...\n");
        for (Document document : corpus) {
            printer.print(document.getSource() + "\t");
            IDSorter[] sortedTopics = new IDSorter[numTopics];
            int[] topicCounts = new int[numTopics];
            int docLen = 0;
            for (int position = 0; position < document.size(); position++) {
                int word = document.getToken(position);
                if (word >= numWords) { continue; }
                docLen++;
                topicCounts[document.getTopic(position)]++;
            }
            for (int topic = 0; topic < numTopics; topic++) {
                sortedTopics[topic] = new IDSorter(topic, (smooth + topicCounts[topic]) / (docLen));
            }
            Arrays.sort(sortedTopics);
            for (int index = 0; index < numTopics; index++) {
                double score = sortedTopics[index].getValue();
                if (score == 0.0) { break; }
                printer.print(corpus.getLabelIndex().getItem(sortedTopics[index].getIndex()) + " " + score + " ");
            }
            printer.print("\n");
        }
        printer.close();
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

    public static LLDA read (File file) throws IOException, ClassNotFoundException {
        LLDA llda;
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
        llda = (LLDA) inputStream.readObject();
        inputStream.close();
        return llda;
    }

    private void readObject (ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        topicCounts = (int[]) inputStream.readObject();
        wordTopicCounts = (int[][]) inputStream.readObject();

        alpha = inputStream.readDouble();
        beta = inputStream.readDouble();
        betaSum = inputStream.readDouble();

        numTopics = inputStream.readInt();
        numWords = inputStream.readInt();

        random = (Randoms) inputStream.readObject();

        topicIndex = (Index) inputStream.readObject();
        wordIndex = (Index) inputStream.readObject();

        trained = inputStream.readBoolean();
    }

    public void write (File file) throws IOException {
        ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(file));
        outputStream.writeObject(this);
        outputStream.close();
    }

    private void writeObject (ObjectOutputStream outputStream) throws IOException {
        outputStream.writeObject(topicCounts);
        outputStream.writeObject(wordTopicCounts);

        outputStream.writeDouble(alpha);
        outputStream.writeDouble(beta);
        outputStream.writeDouble(betaSum);

        outputStream.writeInt(numTopics);
        outputStream.writeInt(numWords);

        outputStream.writeObject(random);

        outputStream.writeObject(topicIndex);
        outputStream.writeObject(wordIndex);

        outputStream.writeBoolean(trained);
    }
}
