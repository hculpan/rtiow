package org.culpan.tracer;

import org.apache.commons.cli.CommandLine;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * Created by USUCUHA on 1/26/2017.
 */
public class TestImageProducer implements ImageProducer {

    @Override
    public String produceImage(CommandLine commandLine) {
        StringWriter stringWriter = new StringWriter();
        PrintWriter result = new PrintWriter(stringWriter);
        result.println("P3");

        int nx = 200, ny = 100;
        result.println(nx + " " + ny);
        result.println("255");
        for (int j = ny - 1; j >= 0; j--) {
            for (int i = 0; i < nx; i++) {
                float r = (float)i / (float)nx;
                float g = (float)j / (float)ny;
                float b = 0.2f;
                int ir = (int)(255.99f * r);
                int ig = (int)(255.99f * g);
                int ib = (int)(255.99f * b);
                result.println(ir + " " + ig + " " + ib);
            }
        }

        return stringWriter.toString();
    }
}
