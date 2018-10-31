package fr;

import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Set;
import java.util.TreeSet;

public class Pendu {
    private static String word;
    private static String tmp;
    private static String[] clear;
    private static String wrong;
    private static int n;
    private static Set<String> letterProposersId;
    private static String wordProposerId;

    private static DatabaseUsers databaseUsers = new DatabaseUsers();

    public static String C_BASIC = "\033[0m"; // User in JEUX
    public static String C_YELLOW = "\033[33m"; // Bot in JEUX


    public Pendu() {
        setUnused();
    }

    private static void setUnused() {
        word = "";
        tmp = "";
        clear = new String[0];
        wrong = "";
        n = -1;
        wordProposerId = "";
    }

    private void setNewWord(String word, MessageChannel channel) {
        if (word.length() > 1)
        {
            n = 10;
            wrong = "";
            letterProposersId = new TreeSet();
            clear = new String[word.length()];
            clear[0] = word.substring(0, 1);
            tmp = clear[0];
            byte[] letters = word.getBytes(StandardCharsets.UTF_8);
            int byteIndex = (letters[0] < 0) ? 2 : 1;
            for (int stringIndex = 1; stringIndex < word.length(); ++stringIndex) {
                byte letter = letters[byteIndex];
                if (97 <= letter && letter <= 122) {
                    clear[stringIndex] = "\\_";
                } else {
                    if (letter < 0)
                        ++byteIndex;
                    clear[stringIndex] = word.substring(stringIndex, stringIndex + 1);
                    channel.sendMessage("Caractère '" + clear[stringIndex] + "' non reconnu.").queue();
                }
                tmp += " " + clear[stringIndex];
                ++byteIndex;
            }
        }
        // (byte & 0xff) to see real code
        // ((char) byte) to print byte
        // (new String(byte[] {byte1, byte2})) to print composed byte
    }

    public void wordProposal(String word, String userId, String name, MessageChannel channel) {
        databaseUsers.addUser(userId, name);
        setNewWord(word, channel);
        wordProposerId = userId;
    }

    public void letterProposal(String user, User author, MessageChannel channel, String msg) {
        databaseUsers.addUser(user, author.getName());
        if (n < 0) {                                                    // Le mot n'existe pas
            try {
                word = DataBase.getWord();
                setNewWord(word, channel);
                System.out.println(C_YELLOW + "New random word : " + word + C_BASIC);
            } catch (IOException e) {
                e.printStackTrace();
                setUnused();
                channel.sendMessage("Echec de génération du mot. Essayez à nouveau.").queue();
            }

            channel.sendMessage("Pas de mot en cours.").queue();
        }
        if (n >= 0) {                                                   // Le mot existe
            if (msg.length() == 1)                                      // Proposition de lettre
            {
                letterProposersId.add(user);

                msg = msg.toLowerCase();
                tmp = clear[0];
                boolean found = false;
                for (int i = 1; i < word.length(); ++i) {
                    if (word.substring(i, i + 1).equals(msg) && !clear[i].equals(msg)) {
                        found = true;
                        clear[i] = msg;
                    }
                    tmp += " " + clear[i];
                }
                if (!found)
                {
                    --n;
                    wrong += msg;
                }
            }
            if (n <= 0)                                                 // Defaite suite à la proposition
            {
                channel.sendMessage("Perdu. Le bon mot etait *" + word + "*.").queue();
                Html def = new Html(word);
                if (def.hasDef())
                    channel.sendMessage(def.toShortString()).queue();
                if (!wordProposerId.equals(""))
                    databaseUsers.addPenduVictory(wordProposerId);
                for (String id : letterProposersId) {
                    databaseUsers.addPenduDefeat(id);
                }
                channel.sendMessage(databaseUsers.printScores(wordProposerId)).queue();
                setUnused();
            }
            else
            {
                channel.sendMessage("<" + n + " chances, (" + wrong + ")> " + tmp).queue();

                boolean gagne = true;
                for (int i = 1; i < word.length(); ++i) {
                    if (clear[i].equals("\\_"))
                        gagne = false;
                }
                if (gagne) {                                            // Victoire suite à la proposition
                    Html def = new Html(word);
                    if (def.hasDef())
                        channel.sendMessage(def.toShortString()).queue();
                    channel.sendMessage("Gagné!").queue();
                    if (!wordProposerId.equals(user))
                        databaseUsers.addPenduVictory(user);
                    channel.sendMessage(databaseUsers.printScores(user)).queue();
                    setUnused();
                }
            }
        }
    }

    public String getScores() {
        return databaseUsers.printScores("");
    }

    public boolean rename(String user, User author, String newName) {
        databaseUsers.addUser(user, author.getName());
        newName = databaseUsers.rename(user, newName);
        if (newName.length() > 0)
            return true;
        return false;
    }
}
