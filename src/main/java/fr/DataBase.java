/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package game.tests;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author paul
 */
public class DataBase {
    public static String getWord() throws FileNotFoundException, IOException {

        FileInputStream fis = null;
        FileChannel fc = null;
        String dataFile = "src/main/java/fr/database.txt";
        fis = new FileInputStream(new File(dataFile));
        fc = fis.getChannel();
        int size = (int)fc.size();
        ByteBuffer bBuff = ByteBuffer.allocate(size);
        fc.read(bBuff);
        bBuff.flip();
        byte[] tabByte = bBuff.array();

        int n = (int) (Math.random() * (tabByte.length-1));
        while (tabByte[n] != 10 && n >= 0)
            --n;
        int m = ++n;
        while (tabByte[m] != 10 && m < tabByte.length - 1)
            ++m;
        byte[] tmp = new byte[m-- -n];
        for (int i = n; i <= m; ++i)
            tmp[i-n] = tabByte[i];


        return new String(tmp);
    }
}
