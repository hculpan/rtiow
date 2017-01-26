package org.culpan;

import static org.junit.Assert.*;

/**
 * Created by USUCUHA on 1/26/2017.
 */
public class PpmToBitmapConverterTest {
    @org.junit.Test
    public void testBuildData() throws Exception {
        String data = "P3\n";
        data += "2 2\n";
        data += "255\n";
        data += "0 0 1\n";
        data += "0 0 2\n";
        data += "0 0 3\n";
        data += "0 0 4\n";
        PpmToBitmapConverter ppmToBitmapConverter = new PpmToBitmapConverter();
        ppmToBitmapConverter.buildData(data);
        assertEquals(4, ppmToBitmapConverter.data.length);
        assertEquals(1, ppmToBitmapConverter.getData(0, 0).b);

    }

}