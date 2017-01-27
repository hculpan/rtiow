package org.culpan;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Arrays;

/**
 * Created by USUCUHA on 1/26/2017.
 */
public class PpmToBitmapConverter {
    enum PpmState { ppmStart, ppmHeader, ppmDimensions, ppmColorDepth, ppmData, ppmDone };

    public final static int BMP_HEADER_SIZE = 14;

    public final static int DIB_HEADER_SIZE = 40;

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
            result.r = (byte)Integer.parseInt(fields[0]);
            result.g = (byte)Integer.parseInt(fields[1]);
            result.b = (byte)Integer.parseInt(fields[2]);

            return result;
        }

        static public RGB create(byte r, byte g, byte b) {
            RGB result = new RGB();
            result.r = r;
            result.g = g;
            result.b = b;
            return result;
        }

        public byte[] asArray() {
            byte [] result = new byte[3];
            result[2] = r;
            result[1] = g;
            result[0] = b;
            return result;
        }

        public Color asColor() {
            return new Color(r, g, b);
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
            writeHeader(output);
            writeData(output);
        }
    }

    protected int calculateFileSize() {
        if (data == null) {
            throw new RuntimeException("Data has not been initialized");
        }

        int eff_width = width + (width % 4);
        return BMP_HEADER_SIZE + DIB_HEADER_SIZE + (eff_width * height);
    }

    protected void buildData(String ppm) throws IOException {
        PpmState ppmState = PpmState.ppmStart;

        try (StringReader reader = new StringReader(ppm)) {
            BufferedReader bufferedReader = new BufferedReader(reader);
            String line;
            int pos, currrow = height - 1, currcol = 0;
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
                    currrow = height - 1;

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
                        currrow--;
                        currcol = 0;
                        if (currrow < 0) {
                            throw new RuntimeException("Found more data than image size supports");
                        }
                    }
                    setData(RGB.create(line), currcol, currrow);
                }
            }
            bufferedReader.close();

            if (!ppmState.equals(PpmState.ppmData) && hasAllData()) {
                throw new RuntimeException("Invalid PPM data: Premature end of data");
            }
        }
    }

    private boolean hasAllData() {
        for (int i = 0; i < data.length; i++) {
            if (data[i] == null) {
                return false;
            }
        }

        return true;
    }

    protected void writeHeader(FileOutputStream output) throws IOException {
        // BMP header
        output.write(new byte[] {'B', 'M'});
        output.write(intToDWord(calculateFileSize()));
        output.write(intToDWord(0));  // reserved
        output.write(intToDWord(BMP_HEADER_SIZE + DIB_HEADER_SIZE));

        // DIB header
        output.write(intToDWord(DIB_HEADER_SIZE));
        output.write(intToDWord(width));
        output.write(intToDWord(height));
        output.write(intToWord(1)); // color planes
        output.write(intToWord(24));  // color depth
        output.write(intToDWord(0));   // compression method
        output.write(intToDWord(0));   // size of raw bitmap data; can use 0 for RGB
        output.write(intToDWord(0));  // horiz resolution of the image in pixel per meter
        output.write(intToDWord(0)); // vert resolution of the image in pixel per meter
        output.write(intToDWord(0));   // number of colors in palette, or 0
        output.write(intToDWord(0));   // number of important colors in palette

    }

    protected void writeData(FileOutputStream output) throws IOException {
        byte [] buff = null;
        if (width % 4 > 0) {
            buff = new byte[width % 4];
            Arrays.fill(buff, (byte)0);
        }

        int index = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                output.write(data[index++].asArray());
            }

            if (buff != null) {
                output.write(buff);
            }
        }
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

        data[x + (y * width)] = rgb;
    }

    public RGB getData(int x, int y) {
        if (data == null) {
            throw new RuntimeException("Data buffer not initialized");
        } else if (x < 0 || x >= width || y < 0 || y >= height) {
            throw new RuntimeException("Invalid location: " + x + ", " + y);
        }

        return data[x + (y * width)];
    }

    private byte [] intToWord (int parValue) {
        byte retValue [] = new byte [2];
        retValue [0] = (byte) (parValue & 0x00FF);
        retValue [1] = (byte) ((parValue >>  8) & 0x00FF);
        return (retValue);
    }

    /*
     *
     * intToDWord converts an int to a double word, where the return
     * value is stored in a 4-byte array.
     *
     */
    private byte [] intToDWord (int parValue) {
        byte retValue [] = new byte [4];
        retValue [0] = (byte) (parValue & 0x00FF);
        retValue [1] = (byte) ((parValue >>  8) & 0x000000FF);
        retValue [2] = (byte) ((parValue >>  16) & 0x000000FF);
        retValue [3] = (byte) ((parValue >>  24) & 0x000000FF);
        return (retValue);
    }
}
