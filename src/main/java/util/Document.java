package util;

import java.util.ArrayList;

public class Document {

    private final ArrayList<Integer> tokens;
    private final int[] topicAssignments;
    private final int[] typeAssignments;
    private final String source;
    private final ArrayList<Integer> types;
    private final ArrayList<Integer> topics;

    public Document(ArrayList<Integer> tokens, String source, ArrayList<Integer> types, ArrayList<Integer> topics) {
        this.tokens = tokens;
        this.topicAssignments = new int[tokens.size()];
        this.typeAssignments = new int[tokens.size()];

        this.source = source;
        this.types = types;
        this.topics = topics;
    }

    public int getTopic(int position) { return topicAssignments[position]; }
    public int getToken(int position) { return tokens.get(position); }
    public int getType(int position) { return typeAssignments[position]; }

    public void setTopic(int position, int topic) {
        topicAssignments[position] = topic;
    }

    public void setType(int position, int type) {
        typeAssignments[position] = type;
    }

    //public ArrayList<Integer> getTokens () { return tokens; }
    public int[] getTopicAssignments () { return topicAssignments; }
    public int[] getTypeAssignments () { return typeAssignments; }


    public int size () { return tokens.size(); }

    public ArrayList<Integer> getTypes () { return types; }
    public String getSource () { return source; }
    public ArrayList<Integer> getLabels () { return topics; }
}

