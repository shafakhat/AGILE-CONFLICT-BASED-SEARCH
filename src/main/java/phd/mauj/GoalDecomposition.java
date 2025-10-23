package phd.mauj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ============================================================================
// GOAL DECOMPOSITION 
// ============================================================================

class GoalDecomposition {
    private final Map<Integer, List<Position>> goalsDecomposition;
    private final GridMap map;
    
    public GoalDecomposition(List<Agent> agents, GridMap map) {    
        this.map = map;
        this.goalsDecomposition = new HashMap<>();
        for (Agent agent : agents) {
            goalsDecomposition.put(agent.id, computeIntermediateGoals(agent));
        }
    }

    private Position computeIntermediateGoal(Position start, Position goal, GridMap map) {
        int dx = goal.x - start.x;
        int dy = goal.y - start.y;
        
        // Target 1/3 point
        double t = 1.0 / 3.0;
        int cx = (int) Math.round(start.x + t * dx);
        int cy = (int) Math.round(start.y + t * dy);

        Position best = null;
        int bestDist = Integer.MAX_VALUE;
        // Search a 5x5 area around the target 1/3 point
        for (int ox = -2; ox <= 2; ox++) {
            for (int oy = -2; oy <= 2; oy++) {
                int nx = cx + ox, ny = cy + oy;
                if (nx < 0 || nx >= map.getWidth() || ny < 0 || ny >= map.getHeight()) continue;
                if (map.isObstacle(nx, ny)) continue;
                
                Position np = new Position(nx, ny);
                // Heuristic: prefer spots that are closer to the 1/3 point
                int dist = Math.abs(cx - nx) + Math.abs(cy - ny); 
                if (dist < bestDist) { bestDist = dist; best = np; }
            }
        }
        
        return best;
    }

    private List<Position> computeIntermediateGoals(Agent agent) {
        List<Position> waypoints = new ArrayList<>();
        Position currentStart = agent.start;
        Position currentGoal = agent.goal;

        // Recursive decomposition until distance is small
        while (map.getManhattanDistance(currentStart, currentGoal) > 6) {
             Position intermediate = computeIntermediateGoal(currentStart, currentGoal, map);
             
             if (intermediate == null || intermediate.equals(currentStart) || intermediate.equals(currentGoal)) break;
             
             waypoints.add(intermediate);
             currentStart = intermediate;
        }

        waypoints.add(agent.goal);
        return waypoints;
    }

    // Utility method to get the *next* unachieved waypoint
    public Position getEffectiveGoal(Agent agent, Path currentPath) {
        Position currentPos = currentPath.getFinalPosition();
        List<Position> decompositionChain = goalsDecomposition.get(agent.id);
        
        if (decompositionChain == null || decompositionChain.isEmpty()) return agent.goal;
        
        // Find the next unachieved waypoint
        Position lastReachedWaypoint = agent.start;
        boolean foundStart = currentPos.equals(agent.start);
        
        for (Position waypoint : decompositionChain) {
            if (currentPos.equals(lastReachedWaypoint)) {
                return waypoint; // Return the next waypoint after the last reached one
            }
            lastReachedWaypoint = waypoint;
        }
        
        return currentPos.equals(agent.goal) ? agent.goal : null; 
    }
    
    // Exposes the list of all waypoints including the final goal
    public List<Position> getDecompositionChain(int agentId) {
        return new ArrayList<>(goalsDecomposition.getOrDefault(agentId, new ArrayList<>()));
    }
}