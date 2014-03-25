/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nadersl.riotarchivemanager.util;

import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.InflaterInputStream;
import java.util.zip.ZipException;

public class Compression {

    public static byte[] zLibDecompress(final byte[] compressedData) throws IOException {
        byte[] decompressedBuff = compressedData;
        ByteArrayOutputStream oStream = new ByteArrayOutputStream();
        ByteInputStream bis = new ByteInputStream(compressedData, 0, compressedData.length);

        InputStream in =
                new InflaterInputStream(bis);
        byte[] buffer = new byte[1000];
        int len;
        try {
            while ((len = in.read(buffer)) > 0) {
                oStream.write(buffer, 0, len);
            }
            decompressedBuff = oStream.toByteArray();
        } catch (ZipException ze) {
        } finally {
            in.close();
            oStream.close();
            return decompressedBuff;
        }

    }
}