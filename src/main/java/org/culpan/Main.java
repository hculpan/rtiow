package org.culpan;

import org.apache.commons.cli.*;
import org.culpan.tracer.ImageProducer;
import org.culpan.tracer.TestImageProducer;

import java.io.*;

public class Main {

    public static Options getOptions() {
        Options result = new Options();

        result.addOption(Option.builder("h").argName("Help").longOpt("help").build());

        result.addOption(Option.builder("t").argName("Test image file").longOpt("test").build());
        result.addOption(Option.builder("o").argName("Output file").longOpt("output").argName("filename").hasArg().build());

        return result;
    }

    public static void main(String[] args) {
        CommandLineParser parser = new DefaultParser();
        try {
            String image = null;

            CommandLine cmd = parser.parse(getOptions(), args);
            if (cmd.hasOption("help") || args.length == 0) {
                HelpFormatter helpFormatter = new HelpFormatter();
                helpFormatter.printHelp("grunt-tracer", getOptions());
                System.exit(0);
            } else if (cmd.hasOption("test")) {
                ImageProducer imageProducer = new TestImageProducer();
                image = imageProducer.produceImage(cmd);
            }

            if (image != null) {
                if (cmd.hasOption("output")) {
/*                    File file = new File(cmd.getOptionValue("output"));
                    try (FileWriter fileWriter = new FileWriter(file)) {
                        fileWriter.write(image);
                        System.out.println("Wrote generated image to " + file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }*/
                    PpmToBitmapConverter ppmToBitmapConverter = new PpmToBitmapConverter();
                    ppmToBitmapConverter.convertPpmToBmp(image, cmd.getOptionValue("output"));
                } else {
                    System.out.println(image);
                }
            }
        } catch (ParseException | IOException e) {
            e.printStackTrace();
        }
    }
}
