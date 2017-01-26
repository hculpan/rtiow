package org.culpan.tracer;

import org.apache.commons.cli.CommandLine;

/**
 * Created by USUCUHA on 1/26/2017.
 */
public interface ImageProducer {

    /**
     * Base method for producing an image.  Returns the result as a
     * string of image format ppm
     * @param commandLine
     * @return
     */
    String produceImage(CommandLine commandLine);
}
