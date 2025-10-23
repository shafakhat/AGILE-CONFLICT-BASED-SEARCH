package phd.mauj;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

// ============================================================================
// AGILE A* PATHFINDER (FOR ACBS)
// ============================================================================

class AgileAStar extends AStar {
    private final double lowLevelW = 2.0; 
    private final double heuristicMultiplier = 1.1;

    public AgileAStar(GridMap map) {
        super(map);
    }
    
    protected static class AgileAStarNode implements Comparable<AgileAStarNode> {
        final TimePosition position;
        final int gScore;
        final int hScore;
        final int inadmissibleHScore;
        
        AgileAStarNode(TimePosition position, int gScore, int hScore, int inadmissibleHScore) {
            this.position = position;
            this.gScore = gScore;
            this.hScore = hScore;
            this.inadmissibleHScore = inadmissibleHScore;
        }
        
        public int getFScore() {
            return gScore + hScore;
        }
        
        @Override
        public int compareTo(AgileAStarNode other) {
            // F-score comparison
            int fComp = Integer.compare(this.getFScore(), other.getFScore());
            if (fComp != 0) return fComp;
            // Tie-breaking: prefer shorter paths (higher gScore for same f-score)
            return Integer.compare(other.gScore, gScore);
        }
    }
    
    private int calculateInadmissibleHeuristic(Position pos, Position goal) {
        return (int) (map.getManhattanDistance(pos, goal) * heuristicMultiplier);
    }
    
    private List<Position> getPrioritizedNeighbors(Position currentPos) {
        // Simple prioritization: check wait, then neighbors
        return map.getNeighbors(currentPos);
    }

    // Added startTime argument
    @Override
    public Path findPath(Agent agent, Set<Constraint> constraints, int startTime) {
        Comparator<AgileAStarNode> openComparator = Comparator.comparingInt(AgileAStarNode::getFScore);
        Comparator<AgileAStarNode> focalComparator = Comparator.comparingInt(n -> n.inadmissibleHScore);
        
        PriorityQueue<AgileAStarNode> OPEN = new PriorityQueue<>(openComparator);
        PriorityQueue<AgileAStarNode> FOCAL = new PriorityQueue<>(focalComparator);
        
        Map<TimePosition, Integer> gScore = new HashMap<>();
        Map<TimePosition, TimePosition> cameFrom = new HashMap<>();
        Set<TimePosition> closed = new HashSet<>();
        
        TimePosition start = new TimePosition(agent.start.x, agent.start.y, startTime); 
        int h = map.getManhattanDistance(agent.start, agent.goal);
        int h_prime = calculateInadmissibleHeuristic(agent.start, agent.goal);
        
        AgileAStarNode startNode = new AgileAStarNode(start, 0, h, h_prime);
        OPEN.add(startNode);
        FOCAL.add(startNode);
        gScore.put(start, 0);

        while (!OPEN.isEmpty()) {
            int f_min = OPEN.peek().getFScore();
            
            // FOCAL: nodes whose f-score is within W * f_min
            FOCAL.clear();
            for (AgileAStarNode node : OPEN) {
                if (node.getFScore() <= lowLevelW * f_min) {
                    FOCAL.add(node);
                }
            }
            
            AgileAStarNode current = FOCAL.poll();
            if (current == null) break;
            
            OPEN.remove(current);
            
            // ACBS often uses a simple Closed list only checking position+time
            if (closed.contains(current.position)) continue;
            closed.add(current.position);
            
            TimePosition currentPos = current.position;
            
            if (currentPos.x == agent.goal.x && currentPos.y == agent.goal.y) {
                return reconstructPath(cameFrom, currentPos, current.gScore); 
            }
            
            for (Position neighbor : getPrioritizedNeighbors(new Position(currentPos.x, currentPos.y))) {
                int tentativeG = current.gScore + 1;
                TimePosition nextPos = new TimePosition(neighbor.x, neighbor.y, tentativeG); 
                
                if (closed.contains(nextPos)) continue;
                
                // Pass startTime
                if (isConstraintViolated(agent.id, currentPos, nextPos, constraints, startTime)) continue;
                
                Integer existingG = gScore.get(nextPos);
                
                if (existingG == null || tentativeG < existingG) {
                    cameFrom.put(nextPos, currentPos);
                    gScore.put(nextPos, tentativeG);
                    
                    int neighborH = map.getManhattanDistance(neighbor, agent.goal);
                    int neighborH_prime = calculateInadmissibleHeuristic(neighbor, agent.goal);
                    
                    AgileAStarNode nextNode = new AgileAStarNode(nextPos, tentativeG, neighborH, neighborH_prime);
                    
                    OPEN.removeIf(n -> n.position.equals(nextPos));
                    OPEN.add(nextNode);
                }
            }
        }
        
        return null;
    }
}