package topicmodels;

import util.*;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.logging.Logger;

/**
 * Document Dependent Labeled Latent Dirichlet Allocation.
 *
 * This class implements a simplistic version of DDLLDA using Gibbs sampling.
 * This code is probably much slower than what could be achieved, but it primarily
 * serves educational purposes
 *
 * @author Folgert Karsdorp
 */
public class DDLLDA implements Serializable {

    public static Logger logger = Logger.getLogger(DDLLDA.class.getName());

    public DDLLDA.LearnSampler learnSampler;
    public DDLLDA.InferSampler inferSampler;

    public int numTopics;
    public int numTypes;
    public int numWords;

    // hyper-parameters
    public double[] alpha;
//    public double alphaSum;
    public double beta;
    public double[] gamma;
    public double betaSum;
    public double gammaSum; // TODO, check if this is correct

    // count matrices
    protected int[] topicCounts;
    protected int[][] wordTopicCounts;
    protected int[] typeCounts;
    protected int[][] typeTopicCounts;

    // indexes
    public Index topicIndex;
    public Index typeIndex;
    public Index wordIndex;

    protected Randoms random;
    protected Boolean trained = false;


    public DDLLDA(double alpha, double beta, double gamma, Corpus corpus) {
        this(alpha, beta, gamma, corpus, 20L);
    }
    /**
     * Initialize an instance of DDLLDA.
     *
     * @param beta smoothing over the unigram distribution;
     * @param corpus the corpus from which to learn the distributions;
     */
    public DDLLDA(double alpha, double beta, double gamma, Corpus corpus, long seed) {
        this.numTopics = corpus.getNumTopics();
        this.numTypes = corpus.getNumTypes();
        this.numWords = corpus.getNumWords();

        this.beta = beta;

        this.alpha = new double[numTopics];
        Arrays.fill(this.alpha, alpha);

        this.gamma = new double[numTypes];
        Arrays.fill(this.gamma, gamma);

        this.betaSum = beta * numWords;
        this.gammaSum = gamma * numTypes;

        topicCounts = new int[numTopics];
        wordTopicCounts = new int[numWords][numTopics];
        typeCounts = new int[numTypes];
        typeTopicCounts = new int[numTypes][numTopics];

        topicIndex = corpus.getLabelIndex();
        typeIndex = corpus.getTypeIndex();
        wordIndex = corpus.getWordIndex();

        random = new Randoms(seed);

        //logger.setUseParentHandlers(false);
    }

    /**
     * Given a corpus, where each document has been assigned to a category and a number
     * of labels or topics have been assigned to the document, learn the type-topic
     * distributions, the distributions of types over documents, the distributions
     * of topics over documents and the word distributions of topics.
     *
     * @param iterations how many iterations to run the sampler;
     * @param corpus the corpus to run the sampler on;
     */
    public void train (int iterations, Corpus corpus) {
        learnSampler = new DDLLDA.LearnSampler();
        for (Document document : corpus) {
            learnSampler.addDocument(document);
        }
        logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
        for (int iteration = 1; iteration <= iterations; iteration++) {
            //logger.info("Sampling iteration " + iteration + " started.");
            for (Document document: corpus) {
                learnSampler.sampleForOneDocument(document);
            }
        }
        trained = true;
    }

    /**
     * Given a corpus of test documents, try to assign to each token
     * a type and a topic based on a previously learned DDLLDA model.
     *
     * @param iterations how many iterations to run the sampler;
     * @param corpus the corpus to run the sampler on;
     */
    public void infer (int iterations, Corpus corpus, double alpha, double gamma) {
        if (!trained) {
            throw new IllegalStateException("The model is not trained yet!");
        }
        Arrays.fill(this.alpha, alpha / numTopics);
        Arrays.fill(this.gamma, gamma / numTypes);
        this.gammaSum = gamma;
//        this.alphaSum = alpha;
        //logger.setUseParentHandlers(false);
        logger.info("Sampler initialized. " + numTopics + " topics and " + corpus.size() + " documents.");
        for (Document document : corpus) {
            inferSampler = new DDLLDA.InferSampler();
            inferSampler.addDocument(document);
            for (int iteration = 1; iteration <= iterations; iteration++) {
                //logger.info("Sampling iteration " + iteration + " started.");
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
                if (word >= numWords) {
                    continue;
                }
                docLen++;
                topicCounts[document.getTopic(position)]++;
            }
            // TODO this break with an asymmetric prior...
            for (int topic = 0; topic < numTopics; topic++) {
                sortedTopics[topic] = new IDSorter(topic, (alpha[topic] + topicCounts[topic]) / (docLen + numTopics * alpha[topic]));
//                sortedTopics[topic] = new IDSorter(topic, (alpha[topic] + topicCounts[topic]) / (docLen + alphaSum));
            }

            Arrays.sort(sortedTopics);
            for (int index = 0; index < numTopics; index++) {
                double score = sortedTopics[index].getValue();
                if (score == 0.0) { break; }
                printer.print(topicIndex.getItem(sortedTopics[index].getIndex()) + " " + score + " ");
            }
            printer.print("\n");
        }
        printer.close();
    }

//    public void printTopicDistribution (File file) throws IOException {
//        PrintStream output = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)));
//        output.print(getTopicDistribution());
//        output.close();
//    }

    public void printTopicDistribution (File file) throws IOException {
        PrintWriter printer = new PrintWriter(file);
        IDSorter[] sortedWords = new IDSorter[numWords];
        for (int topic = 0; topic < numTopics; topic++) {
            for (int word = 0; word < numWords; word++) {
                sortedWords[word] = new IDSorter(word, (double) wordTopicCounts[word][topic]);
            }
            Arrays.sort(sortedWords);
            printer.print(topicIndex.getItem(topic) + " count: " + topicCounts[topic] + " ");
            for (int word = 0; word < numWords; word++) {
                printer.print(wordIndex.getItem(sortedWords[word].getIndex()) + ":" + sortedWords[word].getValue() + " ");
            }
            printer.print("\n");
        }
        printer.close();
    }

    /**
     * Read an existing serialized model from disk.
     *
     * @param file the filename of the model to read
     * @return the model
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public static DDLLDA read (File file) throws IOException, ClassNotFoundException {
        DDLLDA ddllda;
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
        ddllda = (DDLLDA) inputStream.readObject();
        inputStream.close();
        return ddllda;
    }

    private void readObject (ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        typeCounts = (int[]) inputStream.readObject();
        typeTopicCounts = (int[][]) inputStream.readObject();
        topicCounts = (int[]) inputStream.readObject();
        wordTopicCounts = (int[][]) inputStream.readObject();

        alpha = (double[]) inputStream.readObject();
        beta = inputStream.readDouble();
        betaSum = inputStream.readDouble();
        gamma = (double[]) inputStream.readObject();
        gammaSum = inputStream.readDouble();

        numTopics = inputStream.readInt();
        numTypes = inputStream.readInt();
        numWords = inputStream.readInt();

        random = (Randoms) inputStream.readObject();

        topicIndex = (Index) inputStream.readObject();
        typeIndex = (Index) inputStream.readObject();
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
        outputStream.writeObject(typeCounts);
        outputStream.writeObject(typeTopicCounts);
        outputStream.writeObject(topicCounts);
        outputStream.writeObject(wordTopicCounts);

        outputStream.writeObject(alpha);
        outputStream.writeDouble(beta);
        outputStream.writeDouble(betaSum);
        outputStream.writeObject(gamma);
        outputStream.writeDouble(gammaSum);

        outputStream.writeInt(numTopics);
        outputStream.writeInt(numTypes);
        outputStream.writeInt(numWords);

        outputStream.writeObject(random);

        outputStream.writeObject(topicIndex);
        outputStream.writeObject(typeIndex);
        outputStream.writeObject(wordIndex);

        outputStream.writeBoolean(trained);
    }

    /**
     * Base sampler, sub-classed by LearnSampler and InferSampler.
     */
    public class Sampler {

        /**
         * Sample the topics and types for all tokens of a document.
         *
         * @param document an instance of Document for which we sample the topics and types;
         * @param labels the set of possible labels to sample from for this document;
         * @param types the set of possible types to sample from fro this document;
         */
        public void sampleForOneDocument (Document document, ArrayList<Integer> labels, ArrayList<Integer> types) {
            int[] assignment;
            int[] docTypeCounts = new int[numTypes];
            for (int position = 0; position < document.size(); position++) {
                if (document.getToken(position) >= numWords) {
                    continue;
                }
                docTypeCounts[document.getType(position)]++;
            }
            for (int position = 0; position < document.size(); position++) {
                int word = document.getToken(position);
                if (word >= numWords) {
                    continue;
                }
                int topic = document.getTopic(position);
                int type = document.getType(position);
                decrement(topic, word, type);
                docTypeCounts[type]--;
                assignment = sample(word, labels, types, docTypeCounts);
                topic = assignment[1]; type = assignment[2];
                increment(topic, word, type);
                docTypeCounts[type]++;
                document.setTopic(position, topic);
                document.setType(position, type);
            }
        }

        /**
         * Sample the topics and types for all tokens of a document.
         *
         * @param document an instance of Document for which we sample the topics and types;
         */
        public void sampleForOneDocument (Document document) {
            sampleForOneDocument(document, document.getLabels(), document.getTypes());
        }

        public void increment (int topic, int word, int type) {}
        public void decrement (int topic, int word, int type) {}

        /**
         * Sample a type and a topic for the current word. This method is computationally
         * quite heavy and should and could probably be optimized further.
         *
         * @param word the word for which we sample a topic and a type;
         * @param labels the set of labels to sample a topic from;
         * @param types the set of types to sample a type from;
         * @param docTypeCounts for each type, how often does it occur in the document under investigation?
         * @return an array consisting of a word, a topic and a type;
         */
        public int[] sample (int word, ArrayList<Integer> labels, ArrayList<Integer> types, int[] docTypeCounts) {
            double[][] topicTermScores = new double[labels.size()][types.size()];
            double sum = 0.0;
            for (int i = 0; i < types.size(); i++) {
                int type = types.get(i);
                double P_T = (gammaSum + typeCounts[type]);
                double P_Dt = (gamma[type] + docTypeCounts[type]);
                for (int j = 0; j < labels.size(); j++) {
                    int topic = labels.get(j);
                    double score = P_Dt * // P(T|D)
                            (beta + wordTopicCounts[word][topic]) / (betaSum + topicCounts[topic]) * // P(w|t)
                            (alpha[topic] + typeTopicCounts[type][topic]) / P_T;  // P(t|T)
                    sum += score;
                    topicTermScores[j][i] = score;
                }
            }
            double sample = random.nextUniform() * sum;
            int topic = -1; int type = 0;
            while (sample > 0.0) {
                topic++;
                if (topic == labels.size()) {
                    type++;
                    topic = 0;
                }
                sample -= topicTermScores[topic][type];
            }
            if (topic == -1) {
                throw new IllegalStateException("No topic sampled.");
            }
            return new int[]{word, labels.get(topic), types.get(type)};
        }
    }

    /**
     * Sampler for training a model.
     */
    public class LearnSampler extends DDLLDA.Sampler {

        /**
         * Add a document to the sampler, which means that we randomly assign to each token
         * a type (sampled from the set of types associated with this document) and
         * a topic (again sampled from the set of topics associated with this document).
         * Increment the new assigned topic and type in the count matrices.
         *
         * @param document an instance of Document for which to do the random assignments;
         */
        public void addDocument (Document document) {
            ArrayList<Integer> types = document.getTypes();
            ArrayList<Integer> labels = document.getLabels();
            for (int position = 0; position < document.size(); position++) {
                int topic = random.choice(labels);
                int type = random.choice(types);
                document.setTopic(position, topic);
                document.setType(position, type);
                increment(topic, document.getToken(position), type);
            }
        }

        /**
         * Update the count matrices by decrementing the appropriate counts.
         *
         * @param topic the topic to update;
         * @param word the word to update;
         * @param type the type to update;
         */
        public void decrement (int topic, int word, int type) {
            typeCounts[type]--;
            typeTopicCounts[type][topic]--;
            topicCounts[topic]--;
            wordTopicCounts[word][topic]--;
        }

        /**
         * Update the count matrices by incrementing the appropriate counts.
         *
         * @param topic the topic to update.
         * @param word the word to update.
         * @param type the type to update.
         */
        public void increment (int topic, int word, int type) {
            typeCounts[type]++;
            typeTopicCounts[type][topic]++;
            topicCounts[topic]++;
            wordTopicCounts[word][topic]++;
        }
    }

    /**
     *  Sampler for inference on unseen documents.
     */
    public class InferSampler extends DDLLDA.Sampler {

        private ArrayList<Integer> documentTypes;
        private ArrayList<Integer> documentTopics;

        private int[] bestTopicForWord;
        private int[] bestTypeForTopic;

        private int[] docTypeCounts;
        private int[][] docTypeTopicCounts;
        private int[] docTopicCounts;
        private int[][] docWordTopicCounts;

        public InferSampler () {
            documentTypes = new ArrayList<Integer>();
            for (int type = 0; type < numTypes; type++) {
                documentTypes.add(type);
            }
            documentTopics = new ArrayList<Integer>();
            for (int topic = 0; topic < numTopics; topic++) {
                documentTopics.add(topic);
            }

            docTypeCounts = new int[numTypes];
            docTypeTopicCounts = new int[numTypes][numTopics];
            docTopicCounts = new int[numTopics];
            docWordTopicCounts = new int[numWords][numTopics];

            bestTopicForWord = new int[numWords];
            for (int word = 0; word < numWords; word++) {
                int bestTopic = -1;
                int count = 0;
                for (int topic = 0; topic < numTopics; topic++) {
                    if (wordTopicCounts[word][topic] > count) {
                        count = wordTopicCounts[word][topic];
                        bestTopic = topic;
                    }
                }
                if (bestTopic == -1) {
                    throw new IllegalStateException("No topic sampled.");
                }
                bestTopicForWord[word] = bestTopic;
            }

            bestTypeForTopic = new int[numTopics];
            for (int topic = 0; topic < numTopics; topic++) {
                int bestType = -1;
                int count = 0;
                for (int type = 0; type < numTypes; type++) {
                    if (typeTopicCounts[type][topic] > count) {
                        count = typeTopicCounts[type][topic];
                        bestType = type;
                    }
                }
                bestTypeForTopic[topic] = bestType;
            }
        }

        /**
         * Add a document to the sampler, which means that we randomly assign to each
         * token a type (sampled from all possible types discovered during training) and
         * a topic (sampled from all possible topics discovered during training).
         *
         * @param document an instance of Document for which to do the random assignments;
         */
        public void addDocument (Document document) {
            for (int position = 0; position < document.size(); position++) {
                if (document.getToken(position) >= numWords) {
                    continue;
                }
                int word = document.getToken(position);
                // We ignore all OOV types
                if (word < numWords) {
                    int topic = bestTopicForWord[word];
                    int type = bestTypeForTopic[topic];
                    increment(topic, word, type);
                    document.setTopic(position, topic);
                    document.setType(position, type);
                }
//                int topic = random.choice(documentTopics);
//                int type = random.choice(documentTypes);
//                document.setTopic(position, topic);
//                document.setType(position, type);
//                increment(topic, document.getToken(position), type);
            }
        }

        /**
         * Sample a type and a topic for the current word. This method is computationally
         * quite heavy and should and could probably be optimized further.
         *
         * @param word the word for which we sample a topic and a type;
         * @param labels the set of labels to sample a topic from;
         * @param types the set of types to sample a type from;
         * @param docTypeCounts for each type, how often does it occur in the document under investigation?
         * @return an array consisting of a word, a topic and a type;
         */
        public int[] sample (int word, ArrayList<Integer> labels, ArrayList<Integer> types, int[] docTypeCounts) {
            double[][] topicTermScores = new double[labels.size()][types.size()];
            double sum = 0.0;
            for (int i = 0; i < types.size(); i++) {
                int type = types.get(i);
                double P_T = (gammaSum + typeCounts[type] + this.docTypeCounts[type]);
                double P_Dt = (gamma[type] + docTypeCounts[type]);
                for (int j = 0; j < labels.size(); j++) {
                    int topic = labels.get(j);
                    double score = P_Dt * // P(T|D)
                            (beta + wordTopicCounts[word][topic] + docWordTopicCounts[word][topic]) /
                            (betaSum + topicCounts[topic] + docTopicCounts[topic]) * // P(w|t)
                            (alpha[topic] + typeTopicCounts[type][topic] + docTypeTopicCounts[type][topic]) / P_T;  // P(t|T)
                    sum += score;
                    topicTermScores[j][i] = score;
                }
            }
            double sample = random.nextUniform() * sum;
            int topic = -1; int type = 0;
            while (sample > 0.0) {
                topic++;
                if (topic == labels.size()) {
                    type++;
                    topic = 0;
                }
                sample -= topicTermScores[topic][type];
            }
            if (topic == -1) {
                throw new IllegalStateException("No topic sampled.");
            }
            return new int[]{word, labels.get(topic), types.get(type)};
        }

        /**
         * Update the count matrices by decrementing the appropriate counts.
         *
         * @param topic the topic to update;
         * @param word the word to update;
         * @param type the type to update;
         */
        public void decrement (int topic, int word, int type) {
            docTypeCounts[type]--;
            docTypeTopicCounts[type][topic]--;
            docTopicCounts[topic]--;
            docWordTopicCounts[word][topic]--;
        }

        /**
         * Update the count matrices by incrementing the appropriate counts.
         *
         * @param topic the topic to update.
         * @param word the word to update.
         * @param type the type to update.
         */
        public void increment (int topic, int word, int type) {
            docTypeCounts[type]++;
            docTypeTopicCounts[type][topic]++;
            docTopicCounts[topic]++;
            docWordTopicCounts[word][topic]++;
        }

        /**
         * Sample the topics and types for all tokens of a document.
         *
         * @param document an instance of Document for which we sample the topics and types;
         */
        public void sampleForOneDocument (Document document) {
            sampleForOneDocument(document, documentTopics, documentTypes);
        }
    }
}
