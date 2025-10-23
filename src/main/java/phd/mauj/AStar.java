package phd.mauj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

// ============================================================================
// A* PATHFINDER (Accepts startTime and handle constraints correctly)
// ============================================================================

class AStar {
    protected final GridMap map;
    
    public AStar(GridMap map) {
        this.map = map;
    }
    
    // StartTime argument to map local time to global time for constraint check
    public Path findPath(Agent agent, Set<Constraint> constraints, int startTime) {
        PriorityQueue<AStarNode> openList = new PriorityQueue<>();
        // Maps TimePosition to G-Score (local time steps from start)
        Map<TimePosition, Integer> gScore = new HashMap<>(); 
        Map<TimePosition, TimePosition> cameFrom = new HashMap<>();
        
        // The pathfinder starts its search at the agent's start position, but the first move is at startTime + 1
        // Use of startTime in TimePosition to correctly check constraints on the start node if needed
        TimePosition start = new TimePosition(agent.start.x, agent.start.y, startTime); 
        
        // The G-score (steps taken) is always 0 for the start node
        openList.add(new AStarNode(start, 0, map.getManhattanDistance(agent.start, agent.goal)));
        gScore.put(start, 0);
        
        while (!openList.isEmpty()) {
            AStarNode current = openList.poll();
            TimePosition currentPos = current.position;
            
            // Check if goal reached at the earliest possible time
            if (currentPos.x == agent.goal.x && currentPos.y == agent.goal.y) {
                // Pass the true local time (gScore) to reconstructPath
                return reconstructPath(cameFrom, currentPos, current.gScore); 
            }
            
            // Iterate over all possible moves (including wait)
            for (Position neighbor : map.getNeighbors(new Position(currentPos.x, currentPos.y))) {
                
                // The new local time (gScore) is current steps + 1
                int tentativeG = current.gScore + 1;
                // nextPos time is the local time (gScore) of the next position
                TimePosition nextPos = new TimePosition(neighbor.x, neighbor.y, tentativeG); 
                
                // isConstraintViolated checks the move into nextPos at global time "startTime + tentativeG"
                if (isConstraintViolated(agent.id, currentPos, nextPos, constraints, startTime)) {
                    continue;
                }
                
                if (tentativeG < gScore.getOrDefault(nextPos, Integer.MAX_VALUE)) {
                    cameFrom.put(nextPos, currentPos);
                    gScore.put(nextPos, tentativeG);
                    int h = map.getManhattanDistance(neighbor, agent.goal);
                    openList.add(new AStarNode(nextPos, tentativeG, h));
                }
            }
        }
        
        return null;
    }
    
    // StartTime to map local time to global time
    protected boolean isConstraintViolated(int agentId, TimePosition from, TimePosition to, 
                                            Set<Constraint> constraints, int startTime) {
        // Global time is the time *step* we enter the 'to' position.
        // The local time 'to.time' is the G-score (steps from segment start), so global time is startTime + G-score.
        int globalTime = startTime + to.time; 

        for (Constraint c : constraints) {
            if (c.agent != agentId) continue;
            
            if (c instanceof TemporalConstraint tc) {
                // Temporal constraints block a position for a duration
                if (globalTime >= tc.time && globalTime < tc.time + tc.delaySteps) {
                    if (to.x == tc.position.x && to.y == tc.position.y) {
                        return true;
                    }
                }
            } else if (c instanceof VertexConstraint vc) {
                // Vertex constraints block a position at a specific time
                if (vc.time != globalTime) continue;
                if (to.x == vc.position.x && to.y == vc.position.y) return true;
            } else if (c instanceof EdgeConstraint ec) {
                // Edge constraints block a transition at a specific time
                if (ec.time != globalTime) continue;
                // Check if moving from 'from' to 'to' violates constraint on edge 'ec.from' to 'ec.to'
                if (from.x == ec.from.x && from.y == ec.from.y &&
                    to.x == ec.to.x && to.y == ec.to.y) return true;
            }
        }
        return false;
    }
    
    protected Path reconstructPath(Map<TimePosition, TimePosition> cameFrom, TimePosition goal, int finalCost) {
        List<Position> path = new ArrayList<>();
        TimePosition current = goal;
        
        // Reconstruct the path backwards
        while (current != null) {
            // current.time is the G-score (local time)
            path.add(new Position(current.x, current.y));
            current = cameFrom.get(current);
        }
        
        Collections.reverse(path);
        
        // Add wait-at-goal positions for consistency (Path length = Cost + 1)
        int requiredLength = finalCost + 1;
        if (path.size() < requiredLength) {
            Position finalPos = path.get(path.size() - 1);
            while (path.size() < requiredLength) {
                path.add(finalPos);
            }
        }
        
        return new Path(path);
    }
    
    protected static class AStarNode implements Comparable<AStarNode> {
        final TimePosition position;
        final int gScore; // Pathfinding cost (local time steps from segment start)
        final int hScore;
        
        AStarNode(TimePosition position, int gScore, int hScore) {
            this.position = position;
            this.gScore = gScore;
            this.hScore = hScore;
        }
        
        @Override
        public int compareTo(AStarNode other) {
            int fComp = Integer.compare(gScore + hScore, other.gScore + other.hScore);
            if (fComp != 0) return fComp;
            
            // [******] Tie-breaking: prefer shorter paths (higher gScore for same f-score)
            return Integer.compare(other.gScore, gScore);
        }
    }
}