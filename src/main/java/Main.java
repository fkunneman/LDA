import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;

public class Main {

    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("CD-LLDA")
                .defaultHelp(true)
                .description("Document class dependent Labeled-LDA.");

        parser.addArgument("-f", "--file")
                .dest("file")
                .type(String.class)
                .help("The filename from which to read training or testing instances.");

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
                .help("Beta parameter: smoothing over unigram distribution.");

        parser.addArgument("--gamma")
                .dest("gamma")
                .type(Double.class)
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
    }
}
