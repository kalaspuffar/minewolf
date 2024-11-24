package org.research;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class ReadMineCraftList {
    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("entities_list.html"));
        Map<String, String> names = new HashMap<>();
        String line;
        String currentId = null;
        while ((line = br.readLine()) != null) {
            if (line.contains("class=\"id\"")) {
                line = line.substring(line.indexOf("class=\"id\">") + "class=\"id\">".length());
                line = line.substring(0, line.length() - 5);
                if (!line.contains(":")) {
                    currentId = line;
                }
            }
            if (currentId != null && line.contains("class=\"name\"")) {
                line = line.substring(line.indexOf("class=\"name\">") + "class=\"name\">".length());
                line = line.substring(0, line.indexOf("<"));
                line = line.toUpperCase();
                line = line.replace("(", "");
                line = line.replace(")", "");
                line = line.replace(" ", "_");
                names.put(currentId, line);
                currentId = null;
            }
        }

        for (Map.Entry<String, String> entry : names.entrySet()) {
            System.out.print("int ");
            System.out.print(entry.getValue());
            System.out.print(" = ");
            System.out.print(entry.getKey());
            System.out.println(";");
        }
    }
}
