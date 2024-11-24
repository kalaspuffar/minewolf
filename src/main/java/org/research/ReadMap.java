package org.research;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class ReadMap {
    public static void main(String[] args) {
        try {
            Map<String, Integer> mapVis = new HashMap<>();
            BufferedImage bi = ImageIO.read(new File(args[0]));
            BufferedReader br = new BufferedReader(new FileReader(args[2]+"_pieces.txt"));
            String line;
            while ((line = br.readLine()) != null) {
                if (line.isBlank()) continue;
                String[] arr = line.split("#");
                mapVis.put(arr[0], Integer.parseInt(arr[1]));
            }
            br.close();

            BufferedWriter mapWrite = new BufferedWriter(new FileWriter(args[1]));

            int found = mapVis.size();
            for (int y = 0; y < 64; y++) {
                boolean first = true;
                for (int x = 0; x < 64; x++) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ImageIO.write(bi.getSubimage(x * 8,  y * 8, 8, 8), "jpg", baos);
                    byte[] bytes = baos.toByteArray();
                    String s = Base64.getEncoder().encodeToString(bytes);
                    if (!mapVis.containsKey(s)) {
                        ImageIO.write(bi.getSubimage(x * 8,  y * 8, 8, 8), "png", new File("data/" + args[2] + "_" + found+".png"));
                        mapVis.put(s, found);
                        found++;
                    }
                    if (!first) {
                        mapWrite.write(",");
                    }
                    mapWrite.write("" + mapVis.get(s));
                    first = false;
                }
                mapWrite.newLine();
            }

            mapWrite.flush();
            mapWrite.close();

            BufferedWriter bw = new BufferedWriter(new FileWriter(args[2]+"_pieces.txt"));
            for (Map.Entry<String, Integer> entry : mapVis.entrySet()) {
                bw.write(entry.getKey());
                bw.write("#");
                bw.write(""+entry.getValue());
                bw.newLine();
            }
            bw.flush();
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
