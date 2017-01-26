package org.culpan;

import org.apache.commons.cli.*;

import java.io.PrintWriter;

public class Main {

    public static Options getOptions() {
        Options result = new Options();

        return result;
    }

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(getOptions(), args);
            if (cmd.hasOption("help") || args.length == 0) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("grunt-tracer", getOptions());
                System.exit(0);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
