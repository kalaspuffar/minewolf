package org.research;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class UnRlew {
    public static void main(String[] args) {
        try {
            FileInputStream fin = new FileInputStream("plane1.rlew");
            byte[] allBytes = fin.readAllBytes();
            ByteBuffer mapbb = ByteBuffer.wrap(allBytes).order(ByteOrder.LITTLE_ENDIAN);
            int uncompressedSize = mapbb.getChar() & 0xFFFF;
            System.out.println(uncompressedSize);

            ByteBuffer output = ByteBuffer.allocate(uncompressedSize).order(ByteOrder.LITTLE_ENDIAN);

            while (output.position() < uncompressedSize - 2) {
                char v = mapbb.getChar();
                if (v == 0xABCD) {
                    int count = mapbb.getChar() & 0xFFFF;
                    char val = mapbb.getChar();
                    for (int i = 0; i < count; i++) {
                        output.putChar(val);
                        if (output.position() > uncompressedSize - 4) break;
                    }
                } else {
                    output.putChar(v);
                }
            }

            FileOutputStream fStream = new FileOutputStream("plane1.clear");
            byte[] outputBytes = new byte[uncompressedSize];
            output.position(0);
            output.get(outputBytes, 0, uncompressedSize);
            fStream.write(outputBytes);
            fStream.flush();
            fStream.close();

            /*
            byte[] allWithoutSize = Arrays.copyOfRange(allBytes, 2, allBytes.length - 2);

            FileOutputStream fStream = new FileOutputStream("plane1.rlew_nosize");
            fStream.write(allWithoutSize);
            fStream.flush();
            fStream.close();
            */
        } catch (Exception e) {
          e.printStackTrace();
        }
    }
}
