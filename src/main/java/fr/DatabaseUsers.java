/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.*;

/**
 *
 * @author paul
 */
public class DatabaseUsers {
    private Map<String, List<String>> users;
    private final String usersFile = "src/main/java/fr/databaseUsers.txt";

    private static final String DATABASE_STRUCT = "LOGIN;NAME;VICTOIRES;USERNAME;DEFAITES";

    public DatabaseUsers() {
        this.users = new HashMap();
        fillDatabase(users, usersFile, 5);
    }

    /**
     * Add the user to database.
     * @param id key
     * @return 
     */
    public boolean addUser(String id, String name) { //TODO add verification
        if (!users.containsKey(id)) {
            List l = new LinkedList();
            
            // fields of a data input
            l.add(name);
            l.add("0");
            l.add(name);
            l.add("0");
            
            users.put(id, l);
            refreshDataFile();
            return true;
        }
        return false;
    }

    
    
    
    
    /**
     * Fill the given database with the data from the file.
     * @param database
     * @param dataFile
     * @param nb_columns nb total de colonnes dans database
     */
    private void fillDatabase(Map<String, List<String>> database, String dataFile, int nb_columns) {
        String data = null;
        try {
            data = readDataFile(dataFile);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (data.length() > 0) {
            String[] rows = data.split("\n");
            for (int i = 1; i < rows.length; ++i) {
                String row = rows[i];
                if (row.length() > 0) {
                    String[] columns = row.split(";");
                    List l = new LinkedList();
                    database.put(columns[0], l);
                    for (int columnIndex = 1; columnIndex < nb_columns; ++columnIndex) {
                        if (columnIndex < columns.length)
                            l.add(columns[columnIndex].split("\r")[0]);
                        else
                        {
                            switch (columnIndex) {
                            case 2:
                                l.add("0");
                                break;
                            case 3:
                                l.add(columns[1]);
                                break;
                            case 4:
                                l.add("0");
                                break;
                            default:
                                l.add("");
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Read the file and returns the String representing data.
     * @param dataFile
     * @return
     * @throws IOException 
     */
    private String readDataFile(String dataFile) throws IOException {
        FileInputStream fis = null;
        FileChannel fc = null;
        try {
            fis = new FileInputStream(new File(dataFile));
            fc = fis.getChannel();
            int size = (int)fc.size();
            ByteBuffer bBuff = ByteBuffer.allocate(size);
            fc.read(bBuff);
            bBuff.flip();
            byte[] tabByte = bBuff.array();
            return new String(tabByte);
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            if (fis != null) fis.close();
            if (fc != null) fc.close();
        }
        return "";
    }
    
    
    
    
    
    /**
     * Write the whole input to the File.
     * @param dataFile
     * @param input 
     */
    private void writeDataFile(String dataFile, String input) {
        FileWriter fw;
        try {
            fw = new FileWriter(new File(dataFile));
            fw.write(input);
            fw.close();
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    
    /**
     * Write database in File.
     */
    private void refreshDataFile() {
        String data = getDataString();
        writeDataFile(usersFile, data);
    }
    
    /**
     * Returns the adapted String that representes database.
     * @return 
     */
    private String getDataString() {
        String res = DATABASE_STRUCT + "\n";
        for (String id : users.keySet()) {
            res += id;
            List<String> l = users.get(id);
            for (Object e : l)
                res += ";" + e;
            res += "\n";
        }
        return res;
    }

    public void addPenduVictory(String user) {
        users.get(user).set(1, String.valueOf(new Integer(users.get(user).get(1)) + 1));
        refreshDataFile();
    }

    public String printScores(String winner) {
        String res = "SCORES";
        for (Map.Entry<String, List<String>> entry : users.entrySet()) {
            List<String> list = entry.getValue();
            String id = entry.getKey();
            String user = list.get(2);
            res += '\n' + user + " : ";
            if (id.equals(winner))
                res += "**";
            res += list.get(1);
            if (id.equals(winner))
                res += "**";
            res += " victoires, " + list.get(3) + " defaites";
        }
        return res;
    }

    public String rename(String id, String name) {
        List<String> list = users.get(id);
        if (list.size() > 2 && name.length() > 0)
        {
            list.set(2, name);
            refreshDataFile();
            return name;
        }
        return "";
    }

    public void addPenduDefeat(String id) {
        users.get(id).set(3, String.valueOf(new Integer(users.get(id).get(3)) + 1));
        refreshDataFile();
    }
}
