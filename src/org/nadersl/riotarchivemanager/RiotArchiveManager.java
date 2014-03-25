/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.nadersl.riotarchivemanager;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Nader Sl
 *
 */
public class RiotArchiveManager {

    private final String rootPath;
    private String dirs[];
    private final HashMap<String, RiotArchive> archives = new HashMap<>();
    private final static Logger LOGGER = Logger.getLogger(RiotArchiveManager.class.getName());
    public static boolean VERBOSE;

    public RiotArchiveManager(final String sPath, final String... dirs) {
        this.rootPath = sPath;
        this.dirs = dirs;
    }

    protected void extract(final String dstPath) {
        for (RiotArchive ra : archives.values()) {
            if (RiotArchiveManager.VERBOSE) {
                LOGGER.log(Level.INFO, "Extracted " + ra.getRafName() + " to " + dstPath);
                ra.extract(rootPath, dstPath);
            }
        }
    }

    protected void unPack() {
        String rafName = null, rafDatName = null;
        try {
            if (VERBOSE) {
                LOGGER.log(Level.INFO, "A scan will start for  " + (dirs.length == 0 ? "all" : "specified") + " Riot archives ...");
            }
            final File archiveFolder = new File(rootPath);
            File[] archivesDirs = archiveFolder.listFiles();
            if (archivesDirs != null) {
                for (File archiveDir : archivesDirs) {
                    if (!archiveDir.isDirectory()) {
                        continue;//skip files         
                    }
                    if (dirs.length != 0) {
                        boolean dirFound = false;
                        for (String dir : dirs) {
                            if (dir.equals(archiveDir.getName())) {
                                dirFound = true;
                                break;
                            }
                        }
                        if (!dirFound) {
                            continue;//continue searching for other folders, this is not the specified one.
                        }
                    }

                    if (VERBOSE) {
                        LOGGER.log(Level.INFO, "Searching " + archiveDir.getName());
                    }
                    File[] rafFiles = archiveDir.listFiles();
                    if (rafFiles != null) {
                        for (File rafFile : rafFiles) {
                            if (rafFile.isDirectory()) {
                                continue;//we are searching for files
                            }
                            int i = rafFile.getName().lastIndexOf('.');
                            if (i >= 0) {
                                final String extension = rafFile.getName().substring(i + 1);
                                if (extension.equals("raf")) {
                                    rafName = rafFile.getName();
                                } else if (extension.equals("dat")) {
                                    rafDatName = rafFile.getName();
                                }
                            }
                        }
                    }
                    if (rafName == null || rafDatName == null) {
                        if (VERBOSE) {
                            LOGGER.log(Level.INFO, "Skipping current directory , couldn't find .raf or .raf.dat");
                        }
                        continue;
                    } else {

                        if (VERBOSE) {
                            LOGGER.log(Level.INFO, "Found raf file " + rafName);
                            LOGGER.log(Level.INFO, "Found raf dat file " + rafDatName);
                        }
                    }

                    RiotArchive riotArch = new RiotArchive(archiveDir.getName(), rafName, rafDatName);
                    if (riotArch.unpack(rootPath)) {
                        archives.put(archiveDir.getName(), riotArch);
                    } else {
                        LOGGER.log(Level.SEVERE, "couldn't unpack archive " + archiveDir + "/" + rafName);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public ArrayList<RiotEntry> findEntries(final String regex, final String... archs) {
        final ArrayList<RiotEntry> results = new ArrayList<>();

        for (Entry<String, RiotArchive> archive : archives.entrySet()) {
            if (archs.length == 0 || Arrays.asList(archs).contains(archive.getKey())) {
                results.addAll(archive.getValue().getEntries(regex));
            }

        }
        return results;
    }
}
