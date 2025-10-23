package phd.mauj;

import java.util.ArrayList;
import java.util.List;

class Path {
    private final List<Position> positions;
    private final int cost;
    
    public Path(List<Position> positions) {
        this.positions = new ArrayList<>(positions);
        this.cost = positions.size() - 1;
    }
    
    public Position getPosition(int time) {
        // If time is beyond path length, return the final position (agent is waiting)
        return time < positions.size() ? positions.get(time) : positions.get(positions.size() - 1);
    }

    public Position getFinalPosition() { 
        // Path length is positions.size(), final position is at index positions.size() - 1
        return positions.get(positions.size() - 1);
    }
    
    public int getCost() { return cost; }
    public List<Position> getPositions() { return new ArrayList<>(positions); }
    public int getLength() { return positions.size(); }
}