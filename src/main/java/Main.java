import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import topicmodels.DDLLDA;
import util.Corpus;

import java.io.File;
import java.io.IOException;


public class Main {

    public static void main(String[] args) throws ArgumentParserException, IOException, ClassNotFoundException {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("(DD)(L)LDA")
                .defaultHelp(true)
                .description("Simple implementation of LDA, LLDA and DDLLDA.");

        parser.addArgument("-f", "--file")
                .dest("file")
                .type(String.class)
                .help("The filename from which to read training or testing instances.");

        parser.addArgument("-o", "--output")
                .dest("output")
                .type(String.class)
                .help("The output directory.");

        parser.addArgument("-s", "--system")
                .dest("system")
                .type(String.class)
                .choices("LDA", "LLDA", "DDLLDA")
                .help("The model to use for training or inference (LDA, LLDA, DDLLDA)");

        parser.addArgument("-m", "--model")
                .dest("model")
                .type(String.class)
                .help("The filename pointing to the model learned during training.");

        parser.addArgument("-i", "--iterations")
                .dest("iterations")
                .type(Integer.class)
                .help("The number of iterations for Gibbs sampling.");

        parser.addArgument("--beta")
                .dest("beta")
                .type(Double.class)
                .setDefault(0.01)
                .help("Beta parameter: smoothing over unigram distribution.");

        parser.addArgument("--gamma")
                .dest("gamma")
                .type(Double.class)
                .setDefault(0.01)
                .help("Gamma parameter: smoothing over the topic distribution.");

        Namespace ns = parser.parseArgs(args);
        Double beta = ns.getDouble("beta");
        Double gamma = ns.getDouble("gamma");
        Integer iterations = ns.getInt("iterations");
        String file = ns.getString("file");
        String model = ns.getString("model");
        String system = ns.getString("system");
        String output = ns.getString("output");
        File outputDirectory;
        if (output == null && model != null) {
            outputDirectory = new File(new File(model).getParent());
        } else if (ns.getString("output") != null) {
            outputDirectory = new File(ns.getString("output"));
        } else {
            throw new IOException("No output directory given");
        }
        if (!outputDirectory.exists()) { outputDirectory.mkdir(); }

        if (model == null) {
            Corpus corpus = new Corpus();
            corpus.readFile(file);
            DDLLDA ddllda = new DDLLDA(50.0, beta, gamma, corpus);
            ddllda.train(iterations, corpus);
            ddllda.writeTopicDistributions(new File(outputDirectory + File.separator + "final-topics.txt"), corpus, 0.0);
            ddllda.write(new File(outputDirectory + File.separator + "model.lda"));
        } else {
            DDLLDA ddllda = DDLLDA.read(new File(model));
            Corpus corpus = new Corpus(ddllda.wordIndex, ddllda.topicIndex, ddllda.typeIndex);
            corpus.readFile(file);
            ddllda.infer(iterations, corpus);
            ddllda.writeTopicDistributions(new File(outputDirectory + File.separator + "inference-topics.txt"), corpus, gamma);
        }
    }
}
