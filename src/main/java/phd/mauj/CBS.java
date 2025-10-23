package phd.mauj;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

//============================================================================
//CBS Algorithm
//============================================================================

class CBS extends ACBS {
    public CBS(GridMap map, List<Agent> agents, long timeoutMs) {
        super(map, agents, timeoutMs, 1.0); 
    }
    
    // Override createChildNode to use full pathfinding (standard CBS)
    @Override
    protected CBSNode createChildNode(CBSNode parent, Set<Constraint> constraints,
                                     int constrainedAgent, List<Agent> agents,
                                     GoalDecomposition goalDecomposition) {
        Map<Integer, Path> newSolution = new HashMap<>(parent.solution);
        
        Agent agent = agents.stream().filter(a -> a.id == constrainedAgent).findFirst().orElse(null);
        if (agent == null) return null;
        
        // Standard CBS: Full replan from original start (startTime=0)
        Path newPath = new AStar(map).findPath(agent, constraints, 0); 
        if (newPath == null) return null;
        
        newSolution.put(constrainedAgent, newPath);
        
        int newGCost = newSolution.values().stream().mapToInt(Path::getCost).sum();
        List<Conflict> newConflicts = findConflicts(newSolution);
        int newHCost = calculateHeuristic(newSolution, agents, map, goalDecomposition);
        
        return new CBSNode(newSolution, constraints, newGCost, newHCost, 
                          this.suboptimalityWeight, newConflicts);
    }
}