package org.culpan;

import java.io.*;

/**
 * Created by USUCUHA on 1/26/2017.
 */
public class PpmToBitmapConverter {
    enum PpmState { ppmStart, ppmHeader, ppmDimensions, ppmColorDepth, ppmData, ppmDone };

    static class RGB {
        public byte r;
        public byte g;
        public byte b;

        static public RGB create(String value) {
            RGB result = new RGB();

            String fields[] = value.split("\\s+");
            if (fields.length != 3) {
                throw new RuntimeException("Expecting three numbers for image data; found '" + value + "'");
            }
            result.r = Byte.parseByte(fields[0]);
            result.g = Byte.parseByte(fields[1]);
            result.b = Byte.parseByte(fields[2]);

            return result;
        }

        static public RGB create(byte r, byte g, byte b) {
            RGB result = new RGB();
            result.r = r;
            result.g = g;
            result.b = b;
            return result;
        }
    }

    protected int width;

    protected int height;

    protected RGB data[];

    /**
     * This will take a PPM-formatted image as a string and save it
     * out to the specified file in BMP fomrat
     * @param ppm
     * @param bmpFilename
     */
    public void convertPpmToBmp(String ppm, String bmpFilename) throws IOException {
        buildData(ppm);


        try (FileOutputStream output = new FileOutputStream(bmpFilename)) {

            writeBmpHeader(output);
        }
    }

    protected void buildData(String ppm) throws IOException {
        PpmState ppmState = PpmState.ppmStart;

        try (StringReader reader = new StringReader(ppm)) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            int pos, currrow = 0, currcol = 0;
            while ((line = bufferedReader.readLine()) != null) {
                line = line.trim();
                if ((pos = line.indexOf('#')) > -1) { // strip out any comments
                    line = line.substring(0, pos).trim();
                }

                if (line.isEmpty()) {  // ignore all blank lines
                    continue;
                }

                if (ppmState.equals(PpmState.ppmStart)) {
                    if (line.isEmpty()) {
                        continue;
                    } else if (line.equalsIgnoreCase("P3")) {
                        ppmState = PpmState.ppmHeader;
                        continue;
                    }
                }

                if (ppmState.equals(PpmState.ppmHeader)) {
                    String fields[] = line.split("\\s+");
                    if (fields.length != 2) {
                        throw new RuntimeException("Expecting two numbers for image dimensions; found '" + line + "'");
                    }
                    width = Integer.parseInt(fields[0]);
                    height = Integer.parseInt(fields[1]);

                    if (width < 1 || height < 1) {
                        throw new RuntimeException("Invalid image size: " + line);
                    }

                    ppmState = PpmState.ppmDimensions;
                    data = new RGB[width * height];
                    continue;
                }

                if (ppmState.equals(PpmState.ppmDimensions)) {
                    if (line.equals("255")) {
                        ppmState = PpmState.ppmColorDepth;
                        continue;
                    } else {
                        throw new RuntimeException("Expecting color depth of 255; found '" + line + "'");
                    }
                }

                if (ppmState.equals(PpmState.ppmColorDepth)) {
                    if (data == null) {
                        throw new RuntimeException("Missing image dimensions");
                    }

                    ppmState = PpmState.ppmData;
                    setData(RGB.create(line), currcol, currrow);
                    continue;
                }

                if (ppmState.equals(PpmState.ppmData)) {
                    currcol++;
                    if (currcol >= width) {
                        currrow++;
                        currcol = 0;
                        if (currrow >= height) {
                            throw new RuntimeException("Found more data than image size supports");
                        }
                    }
                    setData(RGB.create(line), currcol, currrow);

                    if (currcol * currrow == (width - 1) * (height - 1)) {
                        ppmState = PpmState.ppmDone;
                    }
                }
            }
            bufferedReader.close();

            if (!ppmState.equals(PpmState.ppmDone)) {
                throw new RuntimeException("Invalid PPM data: Premature end of data");
            }
        }
    }

    protected void writeBmpHeader(FileOutputStream output) throws IOException {
        output.write(new byte[] {'B', 'M'});
    }

    public void setData(byte r, byte g, byte b, int x, int y) {
        setData(RGB.create(r, g, b), x, y);
    }

    public void setData(RGB rgb, int x, int y) {
        if (data == null) {
            throw new RuntimeException("Data buffer not initialized");
        } else if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new RuntimeException("Invalid location: " + x + ", " + y);
        }

        data[(x * width) + y] = rgb;
    }

    public RGB getData(int x, int y) {
        if (data == null) {
            throw new RuntimeException("Data buffer not initialized");
        } else if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new RuntimeException("Invalid location: " + x + ", " + y);
        }

        return data[(x * width) + y];
    }
}
