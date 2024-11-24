package org.research;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class UnCarmac {
    public static void main(String[] args) {
        try {
            FileInputStream fin = new FileInputStream("plane1.carm");
            byte[] allBytes = fin.readAllBytes();

            ByteBuffer mapbb = ByteBuffer.wrap(Arrays.copyOfRange(allBytes, 0, 2)).order(ByteOrder.LITTLE_ENDIAN);
            int uncompressedSize = mapbb.getChar() & 0xFFFF;
            System.out.println(uncompressedSize);

            ByteBuffer output = ByteBuffer.allocate(uncompressedSize).order(ByteOrder.LITTLE_ENDIAN);

            int len = allBytes.length;
            for (int i = 2; i < len; i++) {
                if (i < len - 2 && allBytes[i + 1] == (byte)0xA7) {
                    int count = allBytes[i] & 0xFF;
                    if (count == 0) {
                        output.put(allBytes[i + 2]);
                        output.put(allBytes[i + 1]);
                    }
                    int ago = allBytes[i + 2] & 0xFF;
                    int start = output.position() - (ago * 2);

                    for (int j = 0; j < count; j++) {
                        output.putChar(output.getChar(start + (j * 2)));
                    }
                    System.out.println("A7 count " + count + " ago " + ago);
                    i += 2;
                } else if (i < len - 3 && allBytes[i + 1] == (byte)0xA8) {
                    int count = allBytes[i] & 0xFF;
                    if (count == 0) {
                        output.put(allBytes[i + 2]);
                        output.put(allBytes[i + 1]);
                    }
                    ByteBuffer startbb = ByteBuffer.wrap(Arrays.copyOfRange(allBytes, i + 2, i + 4)).order(ByteOrder.LITTLE_ENDIAN);
                    int start = (startbb.getChar() & 0xFFFF) * 2;

                    for (int j = 0; j < count; j++) {
                        output.putChar(output.getChar(start + (j * 2)));
                    }
                    System.out.println("A8 count " + count + " starting " + start);
                    i += 3;
                } else {
                    output.put(allBytes[i]);
                }
            }

            FileOutputStream fStream = new FileOutputStream("plane1.rlew");
            byte[] outputBytes = new byte[uncompressedSize];
            output.position(0);
            output.get(outputBytes, 0, uncompressedSize);
            fStream.write(outputBytes);
            fStream.flush();
            fStream.close();
        } catch (Exception e) {
          e.printStackTrace();
        }
    }
}
