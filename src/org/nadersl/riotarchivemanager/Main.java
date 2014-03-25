package org.nadersl.riotarchivemanager;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {

            /* load the RAF file*/
            RiotArchiveManager rf = new RiotArchiveManager("C:\\League of Legends-OLD\\RADS\\projects\\lol_game_client\\filearchives\\");
            RiotArchiveManager.VERBOSE = true;
            //RiotArchive.VERBOSE = true;
            rf.unPack();
            
            /*Regex search examples:
             * All: "."
             * Ends with an extension: ".*?\\.extension.*"
             * Contains any: "(word1|word2|word3)"
             * 
             * 
             */
            final List<RiotEntry> riotEntries = rf.findEntries(".");
            for (RiotEntry entry : riotEntries) {
                //check entries..
            }
            System.out.println("Search yielded : " + riotEntries.size() + " entries");

            rf.extract("C:\\League of Legends-OLD\\RADS\\projects\\lol_game_client\\filearchivesextract\\");
        } catch (Exception io) {
            io.printStackTrace();
        }
    }
}
