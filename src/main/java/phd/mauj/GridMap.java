package phd.mauj;

import java.util.ArrayList;
import java.util.List;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

// ============================================================================
// GRID MAP
// ============================================================================

class GridMap {
    private final boolean[][] obstacles;
    protected final int width, height;
    private final Graph<Position, DefaultEdge> graph;
    
    public GridMap(int width, int height) {
        this.width = width;
        this.height = height;
        this.obstacles = new boolean[height][width];
        this.graph = new SimpleGraph<>(DefaultEdge.class);
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                graph.addVertex(new Position(x, y));
            }
        }
        
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Position pos = new Position(x, y);
                if (x > 0) graph.addEdge(pos, new Position(x-1, y));
                if (y > 0) graph.addEdge(pos, new Position(x, y-1));
            }
        }
    }
    
    public void setObstacle(int x, int y, boolean obstacle) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            obstacles[y][x] = obstacle;
        }
    }
    
    public boolean isObstacle(int x, int y) {
        return x < 0 || x >= width || y < 0 || y >= height || obstacles[y][x];
    }
    
    public List<Position> getNeighbors(Position pos) {
        List<Position> neighbors = new ArrayList<>();
        // Movement options: Wait (0,0), Right, Down, Left, Up
        int[] dx = {0, 1, 0, -1, 0};
        int[] dy = {0, 0, 1, 0, -1};
        
        for (int i = 0; i < dx.length; i++) {
            int nx = pos.x + dx[i];
            int ny = pos.y + dy[i];
            if (!isObstacle(nx, ny)) {
                neighbors.add(new Position(nx, ny));
            }
        }
        return neighbors;
    }
    
    public int getManhattanDistance(Position a, Position b) {
        return Math.abs(a.x - b.x) + Math.abs(a.y - b.y);
    }
    
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}