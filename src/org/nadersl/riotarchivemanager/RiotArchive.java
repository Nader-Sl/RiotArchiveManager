package org.nadersl.riotarchivemanager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.nadersl.riotarchivemanager.util.Compression;
import org.nadersl.riotarchivemanager.util.decoders.InibinDecoder;

/**
 * @author Nader Sl
 *
 */
public class RiotArchive {

    public final static String MAGIC_NUMBER = "18be0ef0";
    private final String name, rafName, rafDatName;
    private String magicN;
    private int versionN, managerIdx, entriesInfoTableOff, stringsTableOff, entriesInfoCount;
    private HashMap<String, RiotEntry> entries = new HashMap<>();
    private final static Logger LOGGER = Logger.getLogger(RiotArchive.class.getName());
    public static boolean VERBOSE;

    public RiotArchive(final String name, final String rafName, final String rafDatName) {
        this.name = name;
        this.rafName = rafName;
        this.rafDatName = rafDatName;
    }

    public void putRiotEntry(final String entryPath, final RiotEntry entry) {
        entries.put(entryPath, entry);
    }

    public RiotEntry getEntry(final String entryPath) {
        return entries.get(entryPath);
    }

    public ArrayList<RiotEntry> getEntries(final String regex) {
        final ArrayList<RiotEntry> results = new ArrayList<>();
        for (Entry<String, RiotEntry> riotEntriesMapEntries : entries.entrySet()) {
            if (regex != null) {
                final Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(riotEntriesMapEntries.getKey());
                if (m.find()) {
                    results.add(riotEntriesMapEntries.getValue());
                }
            } else {
                results.add(riotEntriesMapEntries.getValue());
            }
        }
        return results;
    }

    public boolean unpack(final String rootPath) {

        try (RandomAccessFile rafFile = new RandomAccessFile(rootPath + File.separator + name + File.separator + rafName, "r"); FileChannel inChannel = rafFile.getChannel()) {

            MappedByteBuffer rafBuffer = inChannel.map(FileChannel.MapMode.READ_ONLY, 0, inChannel.size());
            rafBuffer.order(ByteOrder.LITTLE_ENDIAN);
            rafBuffer.load();
            magicN = Integer.toHexString(rafBuffer.getInt());
            if (!magicN.equals(RiotArchive.MAGIC_NUMBER)) {
                //invalid raf file.
                rafBuffer.clear();
                return false;
            }
            versionN = rafBuffer.getInt();
            managerIdx = rafBuffer.getInt();
            entriesInfoTableOff = rafBuffer.getInt();
            stringsTableOff = rafBuffer.getInt();
            rafBuffer.position(entriesInfoTableOff);
            entriesInfoCount = rafBuffer.getInt();

            for (int i = 0; i < entriesInfoCount; i++) {
                rafBuffer.position(entriesInfoTableOff + 4 + i * 16);
                int entryPathHash = rafBuffer.getInt();
                int entryOff = rafBuffer.getInt();
                int entrySize = rafBuffer.getInt();
                int entryPathInfoIdx = rafBuffer.getInt();

                //seek to strings table to grab path info (skeep first 8 bytes of table, since we don't currently care about it).
                rafBuffer.position(stringsTableOff + 8 + entryPathInfoIdx * 8);
                int entryPathOff = rafBuffer.getInt();//get tabel relative entry path offset
                int entryPathSize = rafBuffer.getInt() - 1;//subtract one to take out null char @ end.
                rafBuffer.position(stringsTableOff + entryPathOff);
                byte pathData[] = new byte[entryPathSize];
                rafBuffer.get(pathData, 0, entryPathSize);//read the bytes into the path buff
                String entryPath = new String(pathData, "UTF-8");//convert pathData to a path string.
                RiotEntry riotEntry = new RiotEntry(entryPath, entryPathHash, entryOff, entrySize);
                putRiotEntry(entryPath, riotEntry);
                if (RiotArchive.VERBOSE) {
                    LOGGER.log(Level.INFO, "Stored " + entryPath + " to map");
                }
            }


            rafBuffer.clear();
            return true;

        } catch (Exception io) {
            io.printStackTrace();
            return false;
        }
    }

    public void decodeInibins(final String srcRootPath) {
        MappedByteBuffer datBuffer = null;
        try {
            try (RandomAccessFile aFile = new RandomAccessFile(srcRootPath + File.separator + name + File.separator + rafDatName, "r"); FileChannel inChannel2 = aFile.getChannel()) {

                datBuffer = inChannel2.map(FileChannel.MapMode.READ_ONLY, 0, inChannel2.size());
                datBuffer.order(ByteOrder.LITTLE_ENDIAN);
                datBuffer.load();

            }
            for (RiotEntry riotEntry : getEntries(".*?\\.inibin.*")) {

                byte rawEntryData[] = new byte[riotEntry.getEntrySize()];
                datBuffer.position(riotEntry.getEntryOffset());
                for (int i = 0; i < rawEntryData.length; i++) {
                    rawEntryData[i] = datBuffer.get();
                }
                byte checkedEntryData[] = Compression.zLibDecompress(rawEntryData);
                InibinDecoder.decode(checkedEntryData);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public boolean extract(final String srcRootPath, final String dstRootPath) {

        File dstRootDir = new File(dstRootPath + File.separator + name);
        if (!dstRootDir.exists()) {
            dstRootDir.mkdirs();
        }

        MappedByteBuffer datBuffer = null;
        try {
            try (RandomAccessFile aFile = new RandomAccessFile(srcRootPath + File.separator + name + File.separator + rafDatName, "r");
                    FileChannel inChannel2 = aFile.getChannel()) {
                datBuffer = inChannel2.map(FileChannel.MapMode.READ_ONLY, 0, inChannel2.size());
                datBuffer.order(ByteOrder.LITTLE_ENDIAN);
                datBuffer.load();

            }
            for (RiotEntry riotEntry : getEntries(".")) {
                if (RiotArchive.VERBOSE) {
                       LOGGER.log(Level.INFO, "extracted " + riotEntry.getPath()); 
                }
                byte rawEntryData[] = new byte[riotEntry.getEntrySize()];
                datBuffer.position(riotEntry.getEntryOffset());
                for (int i = 0; i < rawEntryData.length; i++) {
                    rawEntryData[i] = datBuffer.get();
                }
                byte checkedEntryData[] = Compression.zLibDecompress(rawEntryData);

                File entryFile = new File(dstRootDir.getAbsolutePath() + File.separator + riotEntry.getPath());
                entryFile.getParentFile().mkdirs();
                FileOutputStream fos = new FileOutputStream(entryFile);
                BufferedOutputStream bos = new BufferedOutputStream(fos);
                fos.write(checkedEntryData, 0, checkedEntryData.length);

                bos.close();
                fos.close();
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        } finally {

            if (datBuffer != null) {
                datBuffer.clear();
                //     datBuffer.put(new byte[0]);
                //   datBuffer.clear();
                //    datBuffer = null;
            }
        }
    }

    public String getName() {
        return name;
    }

    public String getRafName() {
        return rafName;
    }

    public String getRafDatName() {
        return rafDatName;
    }

    public String getMagicN() {
        return magicN;
    }

    public int getVersionN() {
        return versionN;
    }

    public int getManagerIdx() {
        return managerIdx;
    }

    public int getEntriesInfoTableOff() {
        return entriesInfoTableOff;
    }

    public int getStringsTableOff() {
        return stringsTableOff;
    }

    public int getEntriesInfoCount() {
        return entriesInfoCount;
    }
}
