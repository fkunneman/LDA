package util;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;


public class Corpus implements Iterable<Document> {
    private final Index wordIndex;
    private final Index labelIndex;
    private final Index typeIndex;
    private final ArrayList<Document> documents;

    public Corpus () {
        this (new Index());
    }

    public Corpus (Index wordIndex) {
        this (wordIndex, new Index(), new Index());
    }

    public Corpus (Index wordIndex, Index labelIndex, Index typeIndex) {
        this.wordIndex = wordIndex;
        this.labelIndex = labelIndex;
        this.typeIndex = typeIndex;
        this.documents = new ArrayList<Document>();
    }

    public Iterator<Document> iterator () {
        return documents.iterator();
    }

    public Index getWordIndex () { return wordIndex; }
    public ArrayList<Document> getDocuments () { return documents; }
    public Index getLabelIndex () { return labelIndex; }
    public Index getTypeIndex () { return typeIndex; }
    public int size () { return documents.size(); }
    public int getNumTopics () { return labelIndex.size(); }
    public int getNumTypes () { return typeIndex.size(); }
    public int getNumWords () { return wordIndex.size(); }

    public void readFile (String filename) throws IOException {
        BufferedReader in = new BufferedReader(new FileReader(filename));
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            if (line.trim().length() == 0) {
                continue;
            }
            String[] fields = line.split("\t");
            String source = fields[0];
            String type = fields[1];
            String[] labels = fields[2].split(",");
            ArrayList<Integer> indexedLabels = new ArrayList<Integer>();
            for (String label : labels) {
                if (labelIndex.getId(label) == null) {
                    labelIndex.put(label);
                }
                indexedLabels.add(labelIndex.getId(label));
            }
            // We expect the document to be nicely tokenized, e.g. by Ucto
            String[] words = fields[3].split("\\s+");
            ArrayList<Integer> tokens = new ArrayList<Integer>();
            for (String word : words) {
                if (wordIndex.getId(word) == null) {
                    wordIndex.put(word);
                }
                tokens.add(wordIndex.getId(word));
            }
            if (typeIndex.getId(type) == null) {
                typeIndex.put(type);
            }
            documents.add(new Document(tokens, source, typeIndex.getId(type), indexedLabels));
        }
        in.close();
    }

}
