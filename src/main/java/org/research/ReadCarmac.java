package org.research;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class ReadCarmac {


    public static String hex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte aByte : bytes) {
            result.append(String.format("%02X", aByte));
        }
        return result.toString();
    }

    public static void main(String[] args) {
        try {
            byte[] headbytes = new FileInputStream("MAPHEAD.WL6").readAllBytes();
            byte[] mapbytes = new FileInputStream("GAMEMAPS.WL6").readAllBytes();

            ByteBuffer headbb = ByteBuffer.wrap(headbytes).order(ByteOrder.LITTLE_ENDIAN);
            System.out.println(headbb.getChar() == 0xABCD);

            ByteBuffer mapbb = ByteBuffer.wrap(mapbytes).order(ByteOrder.LITTLE_ENDIAN);

            for (int i = 0; i < 100; i++) {
                int mapOffset = headbb.getInt();
                if (mapOffset == 0) continue;

                mapbb.position(mapOffset);
                int plane1Offset = mapbb.getInt();
                int plane2Offset = mapbb.getInt();
                int plane3Offset = mapbb.getInt();
                int plane1Length = mapbb.getChar() & 0xFFFF;
                int plane2Length = mapbb.getChar() & 0xFFFF;
                int plane3Length = mapbb.getChar() & 0xFFFF;
                int width = mapbb.getChar() & 0xFFFF;
                int height = mapbb.getChar() & 0xFFFF;

                byte[] buff = new byte[16];
                mapbb.get(buff, 0, 16);
                System.out.println(i + " = " + new String(buff).split("\0")[0]);
/*
                byte[] plane1 = new byte[plane1Length];
                mapbb.position(plane1Offset);
                mapbb.get(plane1, 0, plane1Length);
                FileOutputStream fStream = new FileOutputStream("plane1.carm");
                fStream.write(plane1);
                fStream.flush();
                fStream.close();
                break;
 */
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
