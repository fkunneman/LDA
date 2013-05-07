package util;


import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class ProtoTopics {

    /**
     * Reads a file consisting of topics and prototypical words for that topic
     * separated by TAB into a Map structure with topics as keys and words
     * as values.
     *
     * @param filename the name of the file to read the topics from;
     * @return Map structure with topics as keys and their prototypical words as value;
     * @throws IOException
     */
    public static HashMap<String, ArrayList<String>> read (String filename) throws IOException {
        HashMap<String, ArrayList<String>> topics = new HashMap<String, ArrayList<String>>();
        BufferedReader in = new BufferedReader(new FileReader(filename));
        for (String line = in.readLine(); line != null; line = in.readLine()) {
            if (line.trim().length() == 0) {
                continue;
            }
            String[] fields = line.split("\t");
            String topic = fields[0];
            ArrayList<String> words = new ArrayList<String>();
            Collections.addAll(words, fields[1].split("\\s+"));
            topics.put(topic, words);
        }
        return topics;
    }
}
