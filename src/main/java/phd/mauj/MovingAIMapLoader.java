package phd.mauj;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

// ============================================================================
// MAP LOADER
// ============================================================================

class MovingAIMapLoader {
    public static GridMap loadMap(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            int width = 0, height = 0;
            
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("height")) {
                    height = Integer.parseInt(line.split("\\s+")[1]);
                } else if (line.startsWith("width")) {
                    width = Integer.parseInt(line.split("\\s+")[1]);
                } else if (line.equals("map")) {
                    break;
                }
            }
            
            if (width == 0 || height == 0) {
                throw new IOException("Invalid map file: missing dimensions");
            }
            
            GridMap map = new GridMap(width, height);
            
            for (int y = 0; y < height; y++) {
                line = reader.readLine();
                if (line == null) break;
                
                for (int x = 0; x < width && x < line.length(); x++) {
                    char c = line.charAt(x);
                    if (c == '@' || c == 'O' || c == 'T' || c == 'W') {
                        map.setObstacle(x, y, true);
                    }
                }
            }
            
            return map;
            
        } catch (FileNotFoundException e) {
            System.err.println("Map file not found: " + filename);
            throw e;
        }
    }
    
    public static List<Agent> generateRandomInstances(GridMap map, int numAgents, Random random) {
        List<Position> freePositions = new ArrayList<>();
        
        for (int x = 0; x < map.getWidth(); x++) {
            for (int y = 0; y < map.getHeight(); y++) {
                if (!map.isObstacle(x, y)) {
                    freePositions.add(new Position(x, y));
                }
            }
        }
        
        if (freePositions.size() < numAgents * 2) {
            System.err.println("WARNING: Not enough free positions for " + numAgents + " agents");
            return new ArrayList<>();
        }
        
        Collections.shuffle(freePositions, random);
        List<Agent> agents = new ArrayList<>();
        
        for (int i = 0; i < numAgents; i++) {
            Position start = freePositions.get(i * 2);
            Position goal = freePositions.get(i * 2 + 1);
            agents.add(new Agent(i, start, goal));
        }
        
        return agents;
    }
}