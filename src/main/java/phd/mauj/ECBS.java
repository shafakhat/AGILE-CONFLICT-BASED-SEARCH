package phd.mauj;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

//============================================================================
//ECBS Algorithm
//============================================================================

class ECBS extends ACBS {
    private final EnhancedAStar enhancedPathfinder;
    
    public ECBS(GridMap map, List<Agent> agents, long timeoutMs, double suboptimalityBound) {
        super(map, agents, timeoutMs, suboptimalityBound); 
        this.enhancedPathfinder = new EnhancedAStar(map); 
    }
    
    @Override
    protected CBSNode createChildNode(CBSNode parent, Set<Constraint> constraints, int constrainedAgent, List<Agent> agents, GoalDecomposition goalDecomposition) {
        Map<Integer, Path> newSolution = new HashMap<>(parent.solution);
        
        Agent agent = agents.stream().filter(a -> a.id == constrainedAgent).findFirst().orElse(null);
        if (agent == null) return null;
        
        // ECBS: Full replan from original start (startTime=0)
        Path newPath = enhancedPathfinder.findPath(agent, constraints, 0);
        if (newPath == null) return null;
        
        newSolution.put(constrainedAgent, newPath);
        
        int newGCost = newSolution.values().stream().mapToInt(Path::getCost).sum();
        List<Conflict> newConflicts = findConflicts(newSolution);
        int newHCost = calculateHeuristic(newSolution, agents, map, goalDecomposition);
        
        return new CBSNode(newSolution, constraints, newGCost, newHCost, suboptimalityWeight, newConflicts);
    }
    
    // ECBS solve loop uses FOCAL list based on the E-cost (g + W*h)
    @Override
    public Result solve(List<Agent> agents) {
        long startTime = System.currentTimeMillis();
        GoalDecomposition goalDecomposition = new GoalDecomposition(agents, map);

        Map<Integer, Path> initialSolution = new HashMap<>();
        Set<Constraint> emptyConstraints = new HashSet<>();
        int gCost = 0;
        
        for (Agent agent : agents) {
            Path path = enhancedPathfinder.findPath(agent, emptyConstraints, 0);
            if (path == null) {
                return new Result(false, null, 0, System.currentTimeMillis() - startTime, "No initial solution");
            }
            initialSolution.put(agent.id, path);
            gCost += path.getCost();
        }
        
        List<Conflict> initialConflicts = findConflicts(initialSolution);
        int hCost = calculateHeuristic(initialSolution, agents, map, goalDecomposition);
        
        CBSNode root = new CBSNode(initialSolution, emptyConstraints, gCost, hCost, suboptimalityWeight, initialConflicts);
        
        if (initialConflicts.isEmpty() && allAgentsAtFinalGoals(root, agents)) {
            return new Result(true, initialSolution, gCost, System.currentTimeMillis() - startTime, "Optimal-ECBS");
        }
        
        PriorityQueue<CBSNode> OPEN = new PriorityQueue<>(Comparator.comparingInt(n -> n.fCost)); 
        PriorityQueue<CBSNode> FOCAL = new PriorityQueue<>(
            Comparator.comparingInt((CBSNode n) -> n.eCost).thenComparingInt(n -> n.conflicts.size())
        );

        OPEN.add(root);
        CBSNode bestSolution = root;
        
        while (!OPEN.isEmpty() && (System.currentTimeMillis() - startTime) < timeoutMs) {
            int f_min = OPEN.peek().fCost;
            
            FOCAL.clear();
            for (CBSNode n : OPEN) {
                if (n.fCost <= suboptimalityWeight * f_min) { 
                    FOCAL.add(n);
                }
            }
            
            CBSNode current = FOCAL.poll(); 
            if (current == null) continue;

            OPEN.remove(current);
            
            if (current.conflicts.isEmpty()) {
                if (allAgentsAtFinalGoals(current, agents)) { 
                    return new Result(true, current.solution, current.gCost, 
                                     System.currentTimeMillis() - startTime, "Optimal-ECBS");
                }
                continue;
            }
            
            if (current.conflicts.size() < bestSolution.conflicts.size() || 
                (current.conflicts.size() == bestSolution.conflicts.size() && current.gCost < bestSolution.gCost)) {
                bestSolution = current;
            }
            
            Conflict conflict = current.conflicts.get(0);
            List<CBSNode> children = generateChildren(current, conflict, agents, goalDecomposition);
            
            for (CBSNode child : children) {
                if (child != null) {
                    OPEN.add(child); 
                }
            }
        }
        
        boolean success = bestSolution.conflicts.isEmpty() && allAgentsAtFinalGoals(bestSolution, agents);
        return new Result(success, bestSolution.solution, bestSolution.gCost, 
                         System.currentTimeMillis() - startTime, 
                         success ? "Optimal-ECBS" : "Suboptimal-ECBS");
    }
}