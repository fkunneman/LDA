import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import topicmodels.Inferencer;
import topicmodels.Learner;
import topicmodels.LearnSampler;
import topicmodels.Learner;
import util.Corpus;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.SimpleFormatter;

public class Main {

    public static void main(String[] args) throws ArgumentParserException, IOException, ClassNotFoundException {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("CD-LLDA")
                .defaultHelp(true)
                .description("Document class dependent Labeled-Learner.");

        parser.addArgument("-f", "--file")
                .dest("file")
                .type(String.class)
                .help("The filename from which to read training or testing instances.");

        parser.addArgument("-o", "--output")
                .dest("output")
                .type(String.class)
                .help("The output directory.");

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
                .help("Gamma parameter: smoothin over the topic distribution.");

        parser.addArgument("--logging")
                .dest("logging")
                .type(Integer.class)
                .choices(Arguments.range(1, 4))
                .setDefault(1)
                .help("The level of detail in logging events (low = 1, fine 3)");

        parser.addArgument("--intermediate-results")
                .dest("save")
                .action(Arguments.storeTrue())
                .help("Save the model at every 100 iterations.");

        Namespace ns = parser.parseArgs(args);
        Double beta = ns.getDouble("beta");
        Double gamma = ns.getDouble("gamma");
        Integer iterations = ns.getInt("iterations");
        Integer logging = ns.getInt("logging");
        String file = ns.getString("file");
        String model = ns.getString("model");
        Boolean save = ns.getBoolean("save");
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

        FileHandler logHandler = new FileHandler(outputDirectory + File.separator + "logfile.log");
        logHandler.setFormatter(new SimpleFormatter());
        Level logLevel;
        switch (logging) {
            case 1: logLevel = Level.INFO;
                break;
            case 2: logLevel = Level.FINE;
                break;
            case 3: logLevel = Level.FINER;
                break;
            case 4: logLevel = Level.FINEST;
                break;
            default: logLevel = Level.INFO;
                break;
        }
        if (model == null) {
            Corpus corpus = new Corpus();
            corpus.readFile(file);
            Learner lda = new Learner(50.0, beta, gamma);
            lda.logger.setLevel(logLevel);
            lda.logger.addHandler(logHandler);
            lda.initSampler(corpus);
            lda.sample(iterations);
            lda.writeTopicDistributions(new File(outputDirectory + File.separator + "final-topics.txt"));
            lda.sampler.write(new File(outputDirectory + File.separator + "model.lda"));
        } else {
            LearnSampler sampler = LearnSampler.read(new File(model));
            Corpus corpus = new Corpus(sampler.wordIndex, sampler.labelIndex, sampler.typeIndex);
            corpus.readFile(file);
            Inferencer inferencer = new Inferencer();
            inferencer.initSampler(corpus, sampler);
            inferencer.sample(iterations);
            inferencer.writeTopicDistributions(new File(outputDirectory + File.separator + "inference-topics.txt"));
        }
    }
}
