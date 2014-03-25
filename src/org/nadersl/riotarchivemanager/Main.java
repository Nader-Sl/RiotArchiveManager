package org.nadersl.riotarchivemanager;

import java.util.List;

public class Main {

    public static void main(String[] args) {
        try {
        
            //read the Raf file
            RiotArchiveManager rf = new RiotArchiveManager("C:\\League of Legends-OLD\\RADS\\projects\\lol_game_client\\filearchives\\");
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
               // do something with entry.
            }
            System.out.println("Search yielded : " + riotEntries.size() + " entries");
        } catch (Exception io) {
            io.printStackTrace();
        }
    }
}
