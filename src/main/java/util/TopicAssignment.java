package util;

import java.io.Serializable;
import java.util.ArrayList;

public class TopicAssignment implements Serializable {

    private final ArrayList<Integer> tokens;
    private final int[] topics;
    private final String source;
    private final int type;
    private final ArrayList<Integer> labels;

    public TopicAssignment (int size) {
        this (new ArrayList<Integer>(size), new int[size], null, -1, new ArrayList<Integer>());
    }

    public TopicAssignment (ArrayList<Integer> tokens, String source, int type, ArrayList<Integer> labels) {
        this (tokens, new int[tokens.size()], source, type, labels);
    }

    public TopicAssignment (ArrayList<Integer> tokens, int[] topics, String source, int type, ArrayList<Integer> labels) {
        this.tokens = tokens;
        this.topics = topics;
        this.source = source;
        this.type = type;
        this.labels = labels;
    }

    public int getTopic(int position) { return topics[position]; }
    public int getToken(int position) { return tokens.get(position); }

    public void setTopic(int position, int topic) {
        topics[position] = topic;
    }

    public ArrayList<Integer> getTokens () { return tokens; }
    public int[] getTopics () { return topics; }
    public ArrayList<Integer> getLabels () { return labels; }

    public int size () { return tokens.size(); }

    public int getType () { return type; }
    public String getSource () { return source; }
}

