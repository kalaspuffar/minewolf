package org.minecraft;

import java.io.*;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class WolfDrawer {

    public static void main(String[] args) {

        try {
            byte[] headbytes = new FileInputStream("MAPHEAD.WL6").readAllBytes();
            byte[] mapbytes = new FileInputStream("GAMEMAPS.WL6").readAllBytes();

            int MAP = 10;

            byte[] mapdata = fetchMapData(headbytes, mapbytes, MAP, 1);
            mapdata = unCarmackize(mapdata);
            mapdata = unRLEW(mapdata);
            sendMap(mapdata);
            Thread.sleep(1000);
            byte[] objdata = fetchMapData(headbytes, mapbytes, MAP, 2);
            objdata = unCarmackize(objdata);
            objdata = unRLEW(objdata);
            sendEntities(objdata);
            //compWithFile(mapdata, "w1m1_walls.csv");
        } catch (Exception e) {}
    }

    private static void compWithFile(byte[] allBytes, String mapFile) throws Exception {
        int[] map = new int[64*64];
        BufferedReader br = new BufferedReader(new FileReader(mapFile));
        int i = 0;
        String line;
        while ((line = br.readLine()) != null) {
            String[] lineSplit = line.split(",");
            for (String num : lineSplit) {
                map[i] = Integer.parseInt(num);
                i++;
            }
        }

        Map<Integer, Integer> fromOldToNew = new HashMap();

        ByteBuffer mapbb = ByteBuffer.wrap(allBytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int y = 0; y < 64; y++) {
            boolean first = true;
            for (int x = 0; x < 64; x++) {
                int val = mapbb.getChar() & 0xFFFF;
                fromOldToNew.put(map[(y * 64) + x], val);
            }
            System.out.println();
        }

        for (Map.Entry<Integer, Integer> entry : fromOldToNew.entrySet()) {
            System.out.println(entry.getKey() + " = " + entry.getValue());
        }
    }

    private static void sendBlock(PrintWriter out, int x, int y, int z, int block, int alt) {
        StringBuilder sb = new StringBuilder();
        sb.append("world.setBlock(");
        sb.append(x);
        sb.append(",");
        sb.append(y);
        sb.append(",");
        sb.append(z);
        sb.append(",");
        sb.append(block);
        sb.append(",");
        sb.append(alt);
        sb.append(")");
        out.println(sb);
    }
    private static void sendEntity(PrintWriter out, int x, int y, int z, int entityId) {
        StringBuilder sb = new StringBuilder();
        sb.append("world.spawnEntity(");
        sb.append(x);
        sb.append(",");
        sb.append(y);
        sb.append(",");
        sb.append(z);
        sb.append(",");
        sb.append(entityId);
        sb.append(")");
        out.println(sb);
    }

    private static void sendEntities(byte[] allBytes) throws IOException {
        String serverAddress = "localhost"; // Server address
        int port = 4711; // Server port number
        Socket socket = new Socket(serverAddress, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        ByteBuffer mapbb = ByteBuffer.wrap(allBytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int val = mapbb.getChar() & 0xFFFF;
                if (val == 19 || val == 20 || val == 21 || val == 22) {
                    out.println("player.setTile(" + x + ",1," + y + ")");
                } else if (val == WolfObjects.TABLE_WITH_CHAIRS || val == WolfObjects.TABLE) {
                    sendBlock(out, x, 1, y, MinecraftBlocks.CRAFTING_TABLE, 0);
                } else if (val == WolfObjects.VASE) {
                    sendBlock(out, x, 1, y, MinecraftBlocks.FLOWER_POT, MinecraftColors.BLUE);
                } else if (val == WolfObjects.CEILING_LIGHT_GREEN) {
                    sendBlock(out, x, 4, y, MinecraftBlocks.SEA_LANTERN, 0);
                } else if (val == WolfObjects.AMMO) {
                    //sendBlock(out, x, 1, y, MinecraftBlocks.ARROW, 0);
                } else if (val == WolfObjects.CHANDELIER) {
                    sendBlock(out, x, 4, y, MinecraftBlocks.JACK_O_LANTERN, 0);
                } else if (val == WolfObjects.LIFE) {
                    sendBlock(out, x, 1, y, MinecraftBlocks.CAKE, 0);
                } else if (val == WolfObjects.DOG_FOOD) {
                    //sendBlock(out, x, 1, y, MinecraftBlocks.CARROT, 0);
                } else if (val == WolfObjects.MEDKIT) {
                    sendBlock(out, x, 1, y, MinecraftBlocks.CAKE, 0);
                } else if (val == WolfObjects.FOOD) {
                    sendBlock(out, x, 1, y, MinecraftBlocks.WHEAT_CROPS, 0);
                } else if (val >= WolfObjects.CROSS && val <= WolfObjects.CROWN) {
                    sendBlock(out, x, 1, y, MinecraftBlocks.GOLD_BLOCK, 0);
                } else if (val >= WolfObjects.PATROLLING_GUARD_EASY_NORTH && val <= WolfObjects.PATROLLING_GUARD_EASY_WEST) {
                    sendEntity(out, x, 1, y, MinecraftEntities.ZOMBIE);
                } else if (val >= WolfObjects.STANDING_GUARD_EASY_NORTH && val <= WolfObjects.STANDING_GUARD_EASY_WEST) {
                    sendEntity(out, x, 1, y, MinecraftEntities.ZOMBIE);
                } else if (val > 74) {
                    //sendEntity(out, x, 1, y, 59);
                }
            }
            out.flush();
        }
        socket.close();
    }

    private static void sendMap(byte[] allBytes) throws Exception {
        String serverAddress = "localhost"; // Server address
        int port = 4711; // Server port number
        Socket socket = new Socket(serverAddress, port);
        PrintWriter out = new PrintWriter(socket.getOutputStream(), true);

        out.println("world.setBlocks(-5,-2,-5,69,4,69,0)");

        Thread.sleep(5000);

        out.println("world.setBlocks(-5,-2,-5,69,4,69,1)");
        out.println("world.setBlocks(-4,-1,-4,68,3,68,0)");

        ByteBuffer mapbb = ByteBuffer.wrap(allBytes).order(ByteOrder.LITTLE_ENDIAN);
        for (int y = 0; y < 64; y++) {
            for (int x = 0; x < 64; x++) {
                int val = mapbb.getChar() & 0xFFFF;
                if (val == WolfWalls.ELEVATOR) {
                    drawWall(out, x, y, MinecraftBlocks.DIAMOND_BLOCK, 0);
                } else if (val == WolfWalls.ELEVATOR_WALL) {
                    drawWall(out, x, y, MinecraftBlocks.GOLD_BLOCK, 0);
                } else if (val == WolfWalls.GREY_BRICK_1 || val == WolfWalls.GREY_WALL_VENT) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_WOOL, MinecraftColors.GRAY);
                } else if (val == WolfWalls.GREY_BRICK_2 || val == WolfWalls.GREY_WALL_MAP) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_WOOL, MinecraftColors.LIGHT_GRAY);
                } else if (val == WolfWalls.GREY_BRICK_3 || val == WolfWalls.GREY_WALL_HITLER) {
                    drawWall(out, x, y, MinecraftBlocks.BRICKS, MinecraftColors.LIGHT_GRAY);
                } else if (val == WolfWalls.GREY_WALL_1 || val == WolfWalls.MULTICOLOR_BRICK) {
                    drawWall(out, x, y, MinecraftBlocks.BRICKS, MinecraftColors.GRAY);
                } else if (val == WolfWalls.GREY_WALL_2) {
                    drawWall(out, x, y, MinecraftBlocks.STONE, 1);
                } else if (val == WolfWalls.GREY_BRICK_FLAG || val == WolfWalls.RAMPART_STONE_1 || val == WolfWalls.RAMPART_STONE_2) {
                    drawWall(out, x, y, MinecraftBlocks.STONE, 2);
                } else if (val == WolfWalls.STONE_WALL_1 || val == WolfWalls.STONE_WALL_2 || val == WolfWalls.STONE_WALL_FLAG || val == WolfWalls.STONE_WALL_WREATH) {
                    drawWall(out, x, y, MinecraftBlocks.STONE, 1);
                } else if (val == WolfWalls.WHITE_PANEL) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_WOOL, 0);
                } else if (val == WolfWalls.LANDSCAPE || val == WolfWalls.BLOOD_WALL) {
                    drawWall(out, x, y, MinecraftBlocks.LAPIS_LAZULI_BLOCK, 0);
                } else if (val == WolfWalls.PURPLE || val == WolfWalls.PURPLE_BLOOD || val == WolfWalls.PURPLE_BRICK) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_WOOL, MinecraftColors.PURPLE);
                } else if (val == WolfWalls.DEAD_ELEVATOR) {
                    drawWall(out, x, y, MinecraftBlocks.IRON_ORE, 0);
                } else if (val == WolfWalls.DIRTY_BRICK_1) {
                    drawWall(out, x, y, MinecraftBlocks.DIRT, 0);
                } else if (val == WolfWalls.DIRTY_BRICK_2) {
                    drawWall(out, x, y, MinecraftBlocks.GRASS, 0);
                } else if (val == WolfWalls.BROWN_WEAVE || val == WolfWalls.BROWN_WEAVE_BLOOD_1 || val == WolfWalls.BROWN_WEAVE_BLOOD_2 || val == WolfWalls.BROWN_WEAVE_BLOOD_3) {
                    drawWall(out, x, y, MinecraftBlocks.BROWN_GLAZED_TERRACOTTA, 0);
                } else if (val == WolfWalls.BROWN_MARBLE_1 || val == WolfWalls.BROWN_MARBLE_2 || val == WolfWalls.BROWN_MARBLE_FLAG) {
                    drawWall(out, x, y, MinecraftBlocks.BROWN_GLAZED_TERRACOTTA, 0);
                } else if (val == WolfWalls.GREY_BRICK_HITLER) {
                    drawWall(out, x, y, MinecraftBlocks.STONE, 3);
                } else if (val == WolfWalls.GREY_BRICK_EAGLE) {
                    drawWall(out, x, y, MinecraftBlocks.IRON_BLOCK, 0);
                } else if (val == WolfWalls.GREY_BRICK_SIGN) {
                    drawWall(out, x, y, MinecraftBlocks.BRICKS, 0);
                } else if (val == WolfWalls.GREY_CONCRETE_DARK) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_CONCRETE, MinecraftColors.GRAY);
                } else if (val == WolfWalls.GREY_CONCRETE_LIGHT) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_CONCRETE, MinecraftColors.LIGHT_GRAY);
                } else if (val == WolfWalls.BROWN_CONCRETE) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_CONCRETE, MinecraftColors.BROWN);
                } else if (val == WolfWalls.CONCRETE) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_CONCRETE, 0);
                } else if (val == WolfWalls.ENTRANCE) {
                    drawWall(out, x, y, MinecraftBlocks.DIAMOND_BLOCK, 0);
                } else if (val == WolfWalls.STEEL) {
                    drawWall(out, x, y, MinecraftBlocks.IRON_BLOCK, 0);
                } else if (val == WolfWalls.STEEL_SIGN) {
                    drawWall(out, x, y, MinecraftBlocks.IRON_BLOCK, 0);
                } else if (val == WolfWalls.BLUE_BRICK_1 || val == WolfWalls.BLUE_WALL || val == WolfWalls.BLUE_WALL_SKULL || val == WolfWalls.BLUE_WALL_SWASTIKA) {
                    drawWall(out, x, y, MinecraftBlocks.BLUE_GLAZED_TERRACOTTA, 0);
                } else if (val == WolfWalls.BLUE_BRICK_2 || val == WolfWalls.BLUE_BRICK_SIGN) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_WOOL, MinecraftColors.BLUE);
                } else if (val == WolfWalls.STAINED_GLASS) {
                    drawWall(out, x, y, MinecraftBlocks.GLASS, 0);
                } else if (val == WolfWalls.RED_BRICK) {
                    drawWall(out, x, y, MinecraftBlocks.RED_GLAZED_TERRACOTTA, 0);
                } else if (val == WolfWalls.RED_BRICK_SWASTIKA) {
                    drawWall(out, x, y, MinecraftBlocks.WHITE_WOOL, MinecraftColors.RED);
                } else if (val == WolfWalls.RED_BRICK_FLAG) {
                    drawWall(out, x, y, MinecraftBlocks.RED_SANDSTONE, 0);
                } else if (val == WolfWalls.WOOD) {
                    drawWall(out, x, y, MinecraftBlocks.OAK_WOOD, 0);
                } else if (val == WolfWalls.WOOD_PANEL) {
                    drawWall(out, x, y, MinecraftBlocks.OAK_WOOD_PLANK, 0);
                } else if (val == WolfWalls.WOOD_EAGLE) {
                    drawWall(out, x, y, MinecraftBlocks.ACACIA_WOOD, 0);
                } else if (val == WolfWalls.WOOD_HITLER) {
                    drawWall(out, x, y, MinecraftBlocks.NETHER_BRICK, 0);
                } else if (val == WolfWalls.WOOD_IRON_CROSS) {
                    drawWall(out, x, y, MinecraftBlocks.IRON_ORE, 0);
                } else if (val == WolfWalls.BROWN_STONE_1) {
                    drawWall(out, x, y, MinecraftBlocks.STONE, MinecraftColors.BROWN);
                } else if (val == WolfWalls.BROWN_STONE_2) {
                    drawWall(out, x, y, MinecraftBlocks.STONE, MinecraftColors.BROWN);
                } else if (val == WolfWalls.CELL || val == WolfWalls.CELL_SKELETON) {
                    drawWall(out, x, y, MinecraftBlocks.IRON_BARS, 0);
                } else if (val < 64) {
                    drawWall(out, x, y, MinecraftBlocks.STONE, 0);
                }
                if (val > 89 && val < 102) { // Doors
                    sendBlock(out, x, 0, y, MinecraftBlocks.STONE, 0);

                    sendEntity(out, x, 1, y, MinecraftBlocks.IRON_DOOR_BLOCK);
                    //sendBlock(out, x, 2, y, MinecraftBlocks.IRON_DOOR_BLOCK, 0);
                    //out.println("world.setSign(" + x + ",1," + y + ",63,0,Hello)");
                    //out.println("chat.post(weather rain)");
                }
                if (val > 105) { // Open area
                    sendBlock(out, x, 0, y, MinecraftBlocks.STONE, 0);
                }
            }
            out.flush();
        }
        socket.close();
    }

    private static void drawWall(PrintWriter out, int x, int y, int block, int alt) {
        sendBlock(out, x, 0, y, block, alt);
        sendBlock(out, x, 1, y, block, alt);
        sendBlock(out, x, 2, y, block, alt);
        sendBlock(out, x, 3, y, block, alt);
    }

    private static void printMap(byte[] allBytes) {
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
    }

    private static byte[] unRLEW(byte[] allBytes) {
        ByteBuffer mapbb = ByteBuffer.wrap(allBytes).order(ByteOrder.LITTLE_ENDIAN);
        int uncompressedSize = mapbb.getChar() & 0xFFFF;
        //System.out.println(uncompressedSize);

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

        byte[] outputBytes = new byte[uncompressedSize];
        output.position(0);
        output.get(outputBytes, 0, uncompressedSize);
        return outputBytes;
    }

    private static byte[] unCarmackize(byte[] allBytes) {
        ByteBuffer mapbb = ByteBuffer.wrap(Arrays.copyOfRange(allBytes, 0, 2)).order(ByteOrder.LITTLE_ENDIAN);
        int uncompressedSize = mapbb.getChar() & 0xFFFF;
        //System.out.println(uncompressedSize);

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
                //System.out.println("A7 count " + count + " ago " + ago);
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
                //System.out.println("A8 count " + count + " starting " + start);
                i += 3;
            } else {
                output.put(allBytes[i]);
            }
        }

        byte[] outputBytes = new byte[uncompressedSize];
        output.position(0);
        output.get(outputBytes, 0, uncompressedSize);
        return outputBytes;
    }

    private static byte[] fetchMapData(byte[] headbytes, byte[] mapbytes, int map, int plane) throws IOException {
        ByteBuffer headbb = ByteBuffer.wrap(headbytes).order(ByteOrder.LITTLE_ENDIAN);
        System.out.println(headbb.getChar() == 0xABCD);

        ByteBuffer mapbb = ByteBuffer.wrap(mapbytes).order(ByteOrder.LITTLE_ENDIAN);

        int mapOffset = headbb.getInt(2 + (map * 4));
        if (mapOffset == 0) return null;

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
        System.out.println(new String(buff).split("\0")[0]);
        if (plane == 1) {
            byte[] planeBuff = new byte[plane1Length];
            mapbb.position(plane1Offset);
            mapbb.get(planeBuff, 0, plane1Length);
            return planeBuff;
        }
        if (plane == 2) {
            byte[] planeBuff = new byte[plane2Length];
            mapbb.position(plane2Offset);
            mapbb.get(planeBuff, 0, plane2Length);
            return planeBuff;
        }
        if (plane == 3) {
            byte[] planeBuff = new byte[plane3Length];
            mapbb.position(plane3Offset);
            mapbb.get(planeBuff, 0, plane3Length);
            return planeBuff;
        }
        return null;
    }
}
