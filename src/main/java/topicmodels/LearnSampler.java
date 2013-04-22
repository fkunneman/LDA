package topicmodels;


import util.Corpus;
import util.Document;
import util.Index;

import java.io.*;
import java.util.ArrayList;
import java.util.Random;

public class LearnSampler extends Sampler implements Serializable {

    public LearnSampler (Corpus corpus, double alpha, double beta, double gamma) {
        this.alpha = alpha;
        this.beta = beta;
        this.betaSum = beta * numWords;
        this.gammaSum = gamma * numTopics;
        this.gamma = gamma;
        this.numTopics = corpus.getNumTopics();
        this.numTypes = corpus.getNumTypes();
        this.numWords = corpus.getNumWords();

        // Initialize the counting arrays.
        typeCounts = new int[numTypes];
        typeTopicCounts = new int[numTypes][numTopics];
        topicCounts = new int[numTopics];
        wordTopicCounts = new int[numWords][numTopics];

        // random
        random = new Random();

        // indexes
        labelIndex = corpus.getLabelIndex();
        typeIndex = corpus.getTypeIndex();
        wordIndex = corpus.getWordIndex();
    }

    public void addDocument (Document document) {
        ArrayList<Integer> types = document.getTypes();
        for (int position = 0; position < document.size(); position++) {
            ArrayList<Integer> labels = document.getLabels();
            int topic = labels.get(random.nextInt(labels.size()));
            int type = types.get(random.nextInt(types.size()));
            document.setTopic(position, topic);
            document.setType(position, type);
            increment(topic, document.getToken(position), type);
        }
    }

    public void decrement (int topic, int word, int type) {
        typeCounts[type]--;
        typeTopicCounts[type][topic]--;
        topicCounts[topic]--;
        wordTopicCounts[word][topic]--;
    }

    public void increment (int topic, int word, int type) {
        typeCounts[type]++;
        typeTopicCounts[type][topic]++;
        topicCounts[topic]++;
        wordTopicCounts[word][topic]++;
    }

    public static LearnSampler read (File file) throws IOException, ClassNotFoundException {
        LearnSampler sampler;
        ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(file));
        sampler = (LearnSampler) inputStream.readObject();
        inputStream.close();
        return sampler;
    }

    private void readObject (ObjectInputStream inputStream) throws IOException, ClassNotFoundException {
        typeCounts = (int[]) inputStream.readObject();
        typeTopicCounts = (int[][]) inputStream.readObject();
        topicCounts = (int[]) inputStream.readObject();
        wordTopicCounts = (int[][]) inputStream.readObject();

        alpha = inputStream.readDouble();
        beta = inputStream.readDouble();
        betaSum = inputStream.readDouble();
        gamma = inputStream.readDouble();
        gammaSum = inputStream.readDouble();

        numTopics = inputStream.readInt();
        numTypes = inputStream.readInt();
        numWords = inputStream.readInt();

        random = (Random) inputStream.readObject();

        labelIndex = (Index) inputStream.readObject();
        typeIndex = (Index) inputStream.readObject();
        wordIndex = (Index) inputStream.readObject();
    }

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

        outputStream.writeDouble(alpha);
        outputStream.writeDouble(beta);
        outputStream.writeDouble(betaSum);
        outputStream.writeDouble(gamma);
        outputStream.writeDouble(gammaSum);

        outputStream.writeInt(numTopics);
        outputStream.writeInt(numTypes);
        outputStream.writeInt(numWords);

        outputStream.writeObject(random);

        outputStream.writeObject(labelIndex);
        outputStream.writeObject(typeIndex);
        outputStream.writeObject(wordIndex);

    }
}
