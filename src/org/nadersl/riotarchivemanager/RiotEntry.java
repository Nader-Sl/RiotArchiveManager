package org.nadersl.riotarchivemanager;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import org.nadersl.riotarchivemanager.util.Compression;

/**
 * @author Nader Sl
 *
 */
public class RiotEntry {

    private final String path;
    private final int pathHash, entryOffset, entrySize;


    public RiotEntry(final String path, final int pathHash, final int entryOffset, final int entrySize) {

        this.path = path;
        this.pathHash = pathHash;
        this.entryOffset = entryOffset;
        this.entrySize = entrySize;
    }

    public String getPath() {
        return path;
    }

    public int getPathHash() {
        return pathHash;
    }

    public int getEntryOffset() {
        return entryOffset;
    }

    public int getEntrySize() {
        return entrySize;
    }
}
