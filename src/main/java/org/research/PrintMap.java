package org.research;

import java.io.FileInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class PrintMap {
    public static void main(String[] args) {
        try {
            FileInputStream fin = new FileInputStream("plane1.clear");
            byte[] allBytes = fin.readAllBytes();
            ByteBuffer mapbb = ByteBuffer.wrap(allBytes).order(ByteOrder.LITTLE_ENDIAN);
            for (int y = 0; y < 64; y++) {
                boolean first = true;
                for (int x = 0; x < 64; x++) {
                    if (!first) {
                        System.out.print(",");
                    }
                    int val = mapbb.getChar() & 0xFFFF;
                    if (val < 64) {
                        System.out.print("#");
                    }
                    if (val > 89 && val < 102) {
                        System.out.print("/");
                    }
                    if (val > 105) {
                        System.out.print("0");
                    }
                    first = false;
                }
                System.out.println();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
