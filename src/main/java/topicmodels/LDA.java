package topicmodels;

import util.*;

import java.io.*;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Latent Dirichlet Allocation
 *
 * This class implements a simplistic version of LDA using Gibbs
 * sampling. The code is probably much slower than it could and should
 * be, but it serves its educational purposes well.
 */
public class LDA implements Serializable {

    public static Logger logger = Logger.getLogger(LDA.class.getName());

    LDA.LearnSampler learnSampler;
    LDA.InferSampler inferSampler;

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

    /**
     * Initialize an instance of LDA.
     *
     * @param numTopics the number of topics to use;
     * @param alpha smoothing parameter over the topic distribution;
     * @param beta smoothing parameter over the unigram distribution;
     * @param corpus the corpus from which to learn the distributions;
     */
    public LDA (int numTopics, double alpha, double beta, Corpus corpus) {
        this.numTopics = numTopics;
        numWords = corpus.getNumWords();
        this.alpha = alpha;
        this.beta = beta;
        betaSum = beta * numWords;

        topicCounts = new int[numTopics];
        wordTopicCounts = new int[numWords][numTopics];

        topicIndex = new Index();
        for (int topic = 0; topic < numTopics; topic++) {
            topicIndex.put("Topic: " + topic);
        }
        wordIndex = corpus.getWordIndex();

        random = new Randoms(20);
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
        logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
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
        logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
        for (int iteration = 1; iteration <= iterations; iteration++) {
            logger.info("Sampling iteration " + iteration + " started.");
            for (Document document : corpus) {
                inferSampler.sampleForOneDocument(document);
            }
        }
    }

    /**
     * Write the either learned or inferred topic distributions to a file.
     *
     * @param file the name of the file to write the results;
     * @param corpus the corpus containing the topic and type assignments;
     * @param smooth parameter to use for smoothing the topic distributions on output;
     * @throws IOException
     */
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

    /**
     * Base sampler, sub-classed by LearnSampler and InferSampler
     */
    public class Sampler {

        /**
         * Sample the topics for all tokens of a document.
         *
         * @param document an instance of Document for which we sample the topics;
         */
        public void sampleForOneDocument (Document document) {
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
                topic = sample(word, docTopicCounts);
                increment(topic, word);
                docTopicCounts[topic]++;
                document.setTopic(position, topic);
            }
        }

        public void increment (int topic, int word) {}
        public void decrement (int topic, int word) {}

        /**
         * Add a document to the sampler, which means that we randomly assign to
         * each token a topic.
         *
         * @param document an instance of Document for which to do the random assignments;
         */
        public void addDocument (Document document) {
            for (int position = 0; position < document.size(); position++) {
                int topic = random.nextInt(numTopics);
                document.setTopic(position, topic);
                increment(topic, document.getToken(position));
            }
        }

        /**
         * Sample a topic for the current word. This method in particular is computationally
         * expensive and could probably be optimized (further).
         *
         * @param word the word for which we sample a topic;
         * @param docTopicCounts for each topic, how often does it occur in the document under investigation?
         * @return the newly sampled topic.
         */
        public int sample (int word, int[] docTopicCounts) {
            double[] termTopicScores = new double[numTopics];
            double sum = 0.0;
            for (int topic = 0; topic < numTopics; topic++) {
                double score = (alpha + docTopicCounts[topic]) *
                               (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]);
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
                throw new IllegalStateException("No topic sampled.");
            }
            return topic;
        }
    }

    /**
     * Sampler for training a model
     */
    public class LearnSampler extends LDA.Sampler {

        /**
         * Update the count matrices by incrementing the appropriate counts;
         *
         * @param topic the topic to update;
         * @param word the word to update;
         */
        public void increment (int topic, int word) {
            topicCounts[topic]++;
            wordTopicCounts[word][topic]++;
        }

        /**
         * Update the count matrices by decrementing the appropriate counts;
         *
         * @param topic the topic to update;
         * @param word the word to update;
         */
        public void decrement (int topic, int word) {
            topicCounts[topic]--;
            wordTopicCounts[word][topic]--;
        }
    }

    /**
     * Sampler for inference on unseen documents.
     */
    public class InferSampler extends LDA.Sampler {}

    /**
     * Read an existing serialized model from disk.
     *
     * @param file the filename of the model to read;
     * @return the model;
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static LDA read (File file) throws IOException, ClassNotFoundException {
        LDA lda;
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
        lda = (LDA) inputStream.readObject();
        inputStream.close();
        return lda;
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

    /**
     * Write a model serialized to disk.
     *
     * @param file the name of the file to write the model to;
     * @throws IOException
     */
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
