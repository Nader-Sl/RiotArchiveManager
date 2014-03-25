package org.nadersl.riotarchivemanager.util.decoders;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * @author Nader Sl
 *
 */
public class InibinDecoder {

    public final static TreeMap<Integer, DecoderTemplate> decodeDef = new TreeMap<>();

    static {

        decodeDef.put(0x0001, new DecoderTemplate() {
            @Override
            public void decode(ByteBuffer buff, int idx, int key, int oldStyleOffset) {
            }
        });
    }

    public static void decode(final byte[] entry) {
        ByteBuffer buffer = ByteBuffer.wrap(entry, 0, entry.length);
        buffer.order(ByteOrder.nativeOrder());
        byte version = buffer.get();
        short oldLength = buffer.getShort();
        short dataFormat = buffer.getShort();
        int oldStyleOffset = entry.length - oldLength;
        //decode segments
        for (Entry<Integer, DecoderTemplate> e : decodeDef.entrySet()) {
            if ((dataFormat & e.getKey()) != 0) {
                int keyCount = buffer.getShort();
                int keys[] = new int[keyCount];
                for (int i = 0; i < keyCount; ++i) {
                    keys[i] = buffer.getInt();
                }
            }
        }
    }

    private interface DecoderTemplate {

        public void decode(ByteBuffer buff, int idx, int key, int oldStyleOffset);
    }
}
