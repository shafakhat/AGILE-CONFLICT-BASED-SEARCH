package phd.mauj;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

// ============================================================================
// ENHANCED A* FOR ECBS
// ============================================================================

class EnhancedAStar extends AStar {
    private final double lowLevelW = 1.2;
    
    public EnhancedAStar(GridMap map) {
        super(map);
    }
    
    private int calculateInadmissibleHeuristic(Position pos, Position goal) {
        // Use a slight inflation for the inadmissible heuristic
        return (int) (map.getManhattanDistance(pos, goal) * 1.05); 
    }
    
    protected static class EnhancedAStarNode extends AStarNode {
        final int inadmissibleHScore;
        
        EnhancedAStarNode(TimePosition position, int gScore, int hScore, int inadmissibleHScore) {
            super(position, gScore, hScore);
            this.inadmissibleHScore = inadmissibleHScore;
        }
    }
    
    @Override
    public Path findPath(Agent agent, Set<Constraint> constraints, int startTime) {
        PriorityQueue<EnhancedAStarNode> OPEN = new PriorityQueue<>(
            Comparator.comparingInt((EnhancedAStarNode n) -> n.gScore + n.hScore)
        ); 
        
        Comparator<EnhancedAStarNode> focalComparator = Comparator.comparingInt(n -> n.inadmissibleHScore);
        PriorityQueue<EnhancedAStarNode> FOCAL = new PriorityQueue<>(focalComparator); 

        Map<TimePosition, Integer> gScore = new HashMap<>();
        Map<TimePosition, TimePosition> cameFrom = new HashMap<>();
        
        TimePosition start = new TimePosition(agent.start.x, agent.start.y, startTime); 
        
        int h = map.getManhattanDistance(agent.start, agent.goal);
        int h_prime = calculateInadmissibleHeuristic(agent.start, agent.goal);
        
        EnhancedAStarNode startNode = new EnhancedAStarNode(start, 0, h, h_prime);
        OPEN.add(startNode);
        FOCAL.add(startNode); 
        gScore.put(start, 0);

        while (!FOCAL.isEmpty()) { 
            int f_min = OPEN.peek() != null ? OPEN.peek().gScore + OPEN.peek().hScore : Integer.MAX_VALUE;
            
            // FOCAL: nodes whose f-score is within W * f_min
            FOCAL.clear();
            // OPEN rescan.
            for (EnhancedAStarNode n : OPEN) {
                if (n.gScore + n.hScore <= lowLevelW * f_min) { 
                    FOCAL.add(n);
                }
            }
            
            EnhancedAStarNode current = FOCAL.poll(); 
            if (current == null) continue; 

            OPEN.remove(current);
            TimePosition currentPos = current.position;
            
            if (currentPos.x == agent.goal.x && currentPos.y == agent.goal.y) {
                return reconstructPath(cameFrom, currentPos, current.gScore); 
            }
            
            for (Position neighbor : map.getNeighbors(new Position(currentPos.x, currentPos.y))) {
                int tentativeG = current.gScore + 1;
                TimePosition nextPos = new TimePosition(neighbor.x, neighbor.y, tentativeG);
                
                // Pass startTime
                if (isConstraintViolated(agent.id, currentPos, nextPos, constraints, startTime)) {
                    continue;
                }
                
                if (tentativeG < gScore.getOrDefault(nextPos, Integer.MAX_VALUE)) {
                    cameFrom.put(nextPos, currentPos);
                    gScore.put(nextPos, tentativeG);
                    
                    int neighborH = map.getManhattanDistance(neighbor, agent.goal);
                    int neighborH_prime = calculateInadmissibleHeuristic(neighbor, agent.goal);
                    
                    EnhancedAStarNode nextNode = new EnhancedAStarNode(nextPos, tentativeG, neighborH, neighborH_prime);
                    
                    // OPEN needs to handle updates if nextNode is already present with a worse gScore
                    OPEN.removeIf(n -> n.position.equals(nextPos)); // simple update
                    OPEN.add(nextNode);
                    
                    if (nextNode.gScore + nextNode.hScore <= lowLevelW * f_min) {
                        FOCAL.removeIf(n -> n.position.equals(nextPos)); // simple update
                        FOCAL.add(nextNode);
                    }
                }
            }
        }
        
        return null; 
    }
}