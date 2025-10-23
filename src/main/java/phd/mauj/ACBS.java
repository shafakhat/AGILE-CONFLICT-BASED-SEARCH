package phd.mauj;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

//import phd.mauj.ACBS.Result;
//import phd.mauj.ACBS.ScoredChild;

// ============================================================================
// ACBS ALGORITHM
// ============================================================================

class ACBS {
    protected final GridMap map;
    protected final AStar pathfinder;
    protected final long timeoutMs;
    public final double suboptimalityWeight;
    protected volatile boolean interrupted = false;
    
    private final ExecutorService strategyExecutor;
    private static final int MAX_CHILDREN_PER_CONFLICT = 4;
    
    public ACBS(GridMap map, List<Agent> agents, long timeoutMs, double w) {
        this.map = map;
        // ACBS uses AgileAStar
        this.pathfinder = new AgileAStar(map); 
        this.timeoutMs = timeoutMs;
        this.suboptimalityWeight = w;
        this.strategyExecutor = Executors.newFixedThreadPool(3);
    }
    
    public void shutdown() {
        strategyExecutor.shutdown();
        try {
            if (!strategyExecutor.awaitTermination(1, TimeUnit.SECONDS)) {
                strategyExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            strategyExecutor.shutdownNow();
        }
    }
    
    protected List<CBSNode> generateChildren(CBSNode parent, Conflict conflict, 
                                            List<Agent> agents, GoalDecomposition gd) {
        List<Callable<List<CBSNode>>> strategyTasks = new ArrayList<>();
        
        // Add all strategies
        strategyTasks.add(() -> generateTemporalChildren(parent, conflict, agents, gd));
        strategyTasks.add(() -> generateSpatialChildren(parent, conflict, agents, gd));
        strategyTasks.add(() -> generatePriorityChildren(parent, conflict, agents, gd));
        
        List<CBSNode> allChildren = new ArrayList<>();
        
        try {
            // Run strategies concurrently with a timeout
            List<Future<List<CBSNode>>> futures = strategyExecutor.invokeAll(
                strategyTasks, 500, TimeUnit.MILLISECONDS
            );
            
            for (Future<List<CBSNode>> future : futures) {
                try {
                    if (!future.isCancelled()) {
                        List<CBSNode> children = future.get();
                        if (children != null) {
                            allChildren.addAll(children);
                        }
                    }
                } catch (ExecutionException e) {
                    // Strategy failed, continue with others
                }
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return new ArrayList<>();
        }
        
        return pruneChildren(allChildren, parent, conflict, agents, gd);
    }
    
    private List<CBSNode> generateTemporalChildren(CBSNode parent, Conflict conflict,
                                                   List<Agent> agents, GoalDecomposition gd) {
        List<CBSNode> children = new ArrayList<>();
        
        if (conflict instanceof VertexConflict vc) {
            // Agent 1 temporal constraint
            Set<Constraint> constraints1 = new HashSet<>(parent.constraints);
            constraints1.add(new TemporalConstraint(vc.agent1, vc.time, vc.position, 2));
            CBSNode child1 = createChildNode(parent, constraints1, vc.agent1, agents, gd);
            if (child1 != null) {
                child1.strategyType = "TEMPORAL-A1";
                children.add(child1);
            }
            
            // Agent 2 temporal constraint
            Set<Constraint> constraints2 = new HashSet<>(parent.constraints);
            constraints2.add(new TemporalConstraint(vc.agent2, vc.time, vc.position, 2));
            CBSNode child2 = createChildNode(parent, constraints2, vc.agent2, agents, gd);
            if (child2 != null) {
                child2.strategyType = "TEMPORAL-A2";
                children.add(child2);
            }
        }
        
        return children;
    }
    
    private List<CBSNode> generateSpatialChildren(CBSNode parent, Conflict conflict,
                                                  List<Agent> agents, GoalDecomposition gd) {
        List<CBSNode> children = new ArrayList<>();
        
        if (conflict instanceof VertexConflict vc) {
            // Agent 1 vertex constraint
            Set<Constraint> constraints1 = new HashSet<>(parent.constraints);
            constraints1.add(new VertexConstraint(vc.agent1, vc.time, vc.position));
            CBSNode child1 = createChildNode(parent, constraints1, vc.agent1, agents, gd);
            if (child1 != null) {
                child1.strategyType = "SPATIAL-A1";
                children.add(child1);
            }
            
            // Agent 2 vertex constraint
            Set<Constraint> constraints2 = new HashSet<>(parent.constraints);
            constraints2.add(new VertexConstraint(vc.agent2, vc.time, vc.position));
            CBSNode child2 = createChildNode(parent, constraints2, vc.agent2, agents, gd);
            if (child2 != null) {
                child2.strategyType = "SPATIAL-A2";
                children.add(child2);
            }
            
        } else if (conflict instanceof EdgeConflict ec) {
            // Agent 1 edge constraint (A -> B)
            Set<Constraint> constraints1 = new HashSet<>(parent.constraints);
            constraints1.add(new EdgeConstraint(ec.agent1, ec.time, ec.from, ec.to));
            CBSNode child1 = createChildNode(parent, constraints1, ec.agent1, agents, gd);
            if (child1 != null) {
                child1.strategyType = "SPATIAL-EDGE-A1";
                children.add(child1);
            }
            
            // Agent 2 reverse edge constraint (B -> A)
            Set<Constraint> constraints2 = new HashSet<>(parent.constraints);
            constraints2.add(new EdgeConstraint(ec.agent2, ec.time, ec.to, ec.from));
            CBSNode child2 = createChildNode(parent, constraints2, ec.agent2, agents, gd);
            if (child2 != null) {
                child2.strategyType = "SPATIAL-EDGE-A2";
                children.add(child2);
            }
        }
        
        return children;
    }
    
    private List<CBSNode> generatePriorityChildren(CBSNode parent, Conflict conflict,
                                                   List<Agent> agents, GoalDecomposition gd) {
        List<CBSNode> children = new ArrayList<>();
        
        Path path1 = parent.solution.get(conflict.agent1);
        Path path2 = parent.solution.get(conflict.agent2);
        
        Agent a1 = agents.stream().filter(a -> a.id == conflict.agent1).findFirst().orElse(null);
        Agent a2 = agents.stream().filter(a -> a.id == conflict.agent2).findFirst().orElse(null);
        
        if (a1 == null || a2 == null) return children;
        
        // Priority based on remaining distance to final goal (Greedy priority)
        int remainingDist1 = map.getManhattanDistance(path1.getFinalPosition(), a1.goal);
        int remainingDist2 = map.getManhattanDistance(path2.getFinalPosition(), a2.goal);
        
        // Agent with longer remaining path gets priority (other agent constrained)
        int constrainedAgent = (remainingDist1 <= remainingDist2) ? conflict.agent2 : conflict.agent1;
        int agentToConstrain = constrainedAgent;
        
        if (conflict instanceof VertexConflict vc) {
            Set<Constraint> constraints = new HashSet<>(parent.constraints);
            constraints.add(new VertexConstraint(agentToConstrain, vc.time, vc.position));
            CBSNode child = createChildNode(parent, constraints, agentToConstrain, agents, gd);
            if (child != null) {
                child.strategyType = "PRIORITY-" + agentToConstrain;
                children.add(child);
            }
        } else if (conflict instanceof EdgeConflict ec) {
            Set<Constraint> constraints = new HashSet<>(parent.constraints);
            // Constrain the lower-priority agent on the conflicting edge
            Position from = (agentToConstrain == ec.agent1) ? ec.from : ec.to;
            Position to = (agentToConstrain == ec.agent1) ? ec.to : ec.from;
            constraints.add(new EdgeConstraint(agentToConstrain, ec.time, from, to));
            CBSNode child = createChildNode(parent, constraints, agentToConstrain, agents, gd);
            if (child != null) {
                child.strategyType = "PRIORITY-EDGE-" + agentToConstrain;
                children.add(child);
            }
        }
        
        return children;
    }
    
    private List<CBSNode> pruneChildren(List<CBSNode> children, CBSNode parent, 
                                       Conflict conflict, List<Agent> agents, 
                                       GoalDecomposition gd) {
        if (children.size() <= MAX_CHILDREN_PER_CONFLICT) {
            return children;
        }
        
        List<ScoredChild> scoredChildren = new ArrayList<>();
        
        for (CBSNode child : children) {
            double score = computeChildScore(child, parent, conflict, agents);
            scoredChildren.add(new ScoredChild(child, score));
        }
        
        // Select the children with the lowest scores (best trade-off)
        return scoredChildren.stream()
            .sorted(Comparator.comparingDouble(sc -> sc.score))
            .limit(MAX_CHILDREN_PER_CONFLICT)
            .map(sc -> sc.node)
            .collect(Collectors.toList());
    }
    
    private double computeChildScore(CBSNode child, CBSNode parent, 
                                     Conflict conflict, List<Agent> agents) {
        double score = 0.0;
        
        // 1. Cost increase (prioritize children with lower cost increase)
        int costIncrease = child.gCost - parent.gCost;
        score += 0.4 * costIncrease;
        
        // 2. Number of conflicts (prioritize children with fewer conflicts)
        score += 0.3 * child.conflicts.size() * 10;
        
        // 3. F-cost (f=g+h)
        score += 0.2 * child.fCost;
        
        return score;
    }
    
    public static class ScoredChild {
        final CBSNode node;
        final double score;
        
        ScoredChild(CBSNode node, double score) {
            this.node = node;
            this.score = score;
        }
    }
    
    protected int calculateHeuristic(Map<Integer, Path> paths, List<Agent> agents, 
                                    GridMap map, GoalDecomposition goalDecomposition) {
        int h = 0;
        for (Agent agent : agents) {
            Path currentPath = paths.get(agent.id);
            if (currentPath == null) continue;
            
            Position current = currentPath.getFinalPosition();
            // Get the effective goal (next waypoint or final goal)
            Position effectiveGoal = goalDecomposition.getEffectiveGoal(agent, currentPath);
            
            if (effectiveGoal != null) {
                h += map.getManhattanDistance(current, effectiveGoal);
            }
        }
        return h;
    }
    
    public Result solve(List<Agent> agents) {
        long startTime = System.currentTimeMillis();
        
        try {
            GoalDecomposition goalDecomposition = new GoalDecomposition(agents, this.map);
            
            Map<Integer, Path> initialSolution = new HashMap<>();
            Set<Constraint> emptyConstraints = new HashSet<>();
            int gCost = 0;
            
            for (Agent agent : agents) {
                // Initial path finding starts at global time 0
                Path path = pathfinder.findPath(agent, emptyConstraints, 0); 
                if (path == null) {
                    return new Result(false, null, 0, 
                        System.currentTimeMillis() - startTime, "No initial solution");
                }
                initialSolution.put(agent.id, path);
                gCost += path.getCost();
            }
            
            List<Conflict> initialConflicts = findConflicts(initialSolution);
            int hCost = calculateHeuristic(initialSolution, agents, map, goalDecomposition);
            
            CBSNode root = new CBSNode(initialSolution, emptyConstraints, gCost, hCost,
                                     this.suboptimalityWeight, initialConflicts);
            
            if (initialConflicts.isEmpty() && allAgentsAtFinalGoals(root, agents)) {
                return new Result(true, initialSolution, gCost, 
                    System.currentTimeMillis() - startTime, "Optimal");
            }
            
            PriorityQueue<CBSNode> openList = new PriorityQueue<>();
            openList.add(root);
            
            CBSNode bestSolution = root;
            int iterationCount = 0;
            
            while (!openList.isEmpty() && 
                   (System.currentTimeMillis() - startTime) < timeoutMs && 
                   !interrupted) {
                
                iterationCount++;
                if (iterationCount > 20000) { // Safety limit
                    break;
                }
                
                CBSNode current = openList.poll();
                if (current == null) break;
                
                if (current.conflicts.isEmpty()) {
                    if (allAgentsAtFinalGoals(current, agents)) {
                        return new Result(true, current.solution, current.gCost,
                                         System.currentTimeMillis() - startTime, "Solved");
                    } else {
                        // Advance subgoals and replan the next segment
                        CBSNode advancedNode = advanceSubgoalsAndReplan(current, agents, goalDecomposition);
                        if (advancedNode != null) {
                            openList.add(advancedNode);
                        }
                        continue;
                    }
                }
                
                Conflict conflict = current.conflicts.get(0);
                List<CBSNode> children = generateChildren(current, conflict, agents, goalDecomposition);
                
                for (CBSNode child : children) {
                    if (child != null) {
                        openList.add(child);
                    }
                }
                
                // Track best solution found so far (in terms of conflicts, then cost)
                if (current.conflicts.size() < bestSolution.conflicts.size() ||
                    (current.conflicts.size() == bestSolution.conflicts.size() && 
                     current.gCost < bestSolution.gCost)) {
                    bestSolution = current;
                }
            }
            
            boolean success = bestSolution.conflicts.isEmpty() && allAgentsAtFinalGoals(bestSolution, agents);
            return new Result(success, bestSolution.solution, bestSolution.gCost,
                             System.currentTimeMillis() - startTime,
                             success ? "Optimal" : "Suboptimal");
                             
        } catch (Exception e) {
            return new Result(false, null, 0, 
                            System.currentTimeMillis() - startTime, "Exception: " + e.getMessage());
        } finally {
            shutdown();
        }
    }
    
    // To find effective goal and replan a segment from there.
    protected CBSNode createChildNode(CBSNode parent, Set<Constraint> constraints,
                                     int constrainedAgent, List<Agent> agents,
                                     GoalDecomposition goalDecomposition) {
        Map<Integer, Path> newSolution = new HashMap<>(parent.solution);
        
        Agent agent = agents.stream().filter(a -> a.id == constrainedAgent).findFirst().orElse(null);
        if (agent == null) return null;
        
        Path oldPath = parent.solution.get(constrainedAgent);
        
        // 1. Determine the path segment that needs replanning.
        
        // Get the list of all waypoints including start and final goal.
        List<Position> waypoints = new ArrayList<>();
        waypoints.add(agent.start);
        waypoints.addAll(goalDecomposition.getDecompositionChain(constrainedAgent));
        
        Position currentEffectiveGoal = goalDecomposition.getEffectiveGoal(agent, oldPath);
        
        if (currentEffectiveGoal == null || currentEffectiveGoal.equals(agent.goal)) {
            // Replan the full path from start to goal (Standard ACBS initial pathfinding)
            // Use agent.start for simplicity in this case.
            int startTime = 0;
            
            // Replan the full path
            Path newPath = pathfinder.findPath(agent, constraints, startTime); 
            if (newPath == null) return null;
            newSolution.put(constrainedAgent, newPath);
            
        } else {
            // Find the start of the current segment (the previous waypoint).
            Position segmentStart = agent.start;
            int startTime = 0;
            
            // Find the waypoint before the current effective goal.
            for (int i = 0; i < waypoints.size() - 1; i++) {
                if (waypoints.get(i+1).equals(currentEffectiveGoal)) {
                    segmentStart = waypoints.get(i);
                    break;
                }
            }
            
            // Find the global time T that segmentStart was reached in oldPath.
            for (int t = 0; t < oldPath.getLength(); t++) {
                if (oldPath.getPosition(t).equals(segmentStart)) {
                    // Check if this is the start of the segment.
                    if (t == 0 || !oldPath.getPosition(t - 1).equals(segmentStart)) {
                         startTime = t;
                    }
                }
                if (oldPath.getPosition(t).equals(currentEffectiveGoal)) { break; }
            }
                        
            // Replan the segment: from segmentStart to currentEffectiveGoal
            Agent tempAgent = new Agent(constrainedAgent, segmentStart, currentEffectiveGoal);
            Path newSegment = pathfinder.findPath(tempAgent, constraints, startTime); 

            if (newSegment == null) return null;
            
            // New Path: prefix (oldPath up to time startTime) + newSegment
            List<Position> newPathPositions = new ArrayList<>();
            
            // Prefix: oldPath up to the position *at* startTime (index 0 to startTime-1)
            if (startTime > 0) {
                newPathPositions.addAll(oldPath.getPositions().subList(0, startTime));
            }
            
            newPathPositions.addAll(newSegment.getPositions());
            
            Path fullNewPath = new Path(newPathPositions);
            newSolution.put(constrainedAgent, fullNewPath);
        }
        
        int newGCost = newSolution.values().stream().mapToInt(Path::getCost).sum();
        List<Conflict> newConflicts = findConflicts(newSolution);
        int newHCost = calculateHeuristic(newSolution, agents, map, goalDecomposition);
        
        return new CBSNode(newSolution, constraints, newGCost, newHCost, 
                          this.suboptimalityWeight, newConflicts);
    }
    
    protected List<Conflict> findConflicts(Map<Integer, Path> solution) {
        List<Conflict> conflicts = new ArrayList<>();
        List<Integer> agentIds = new ArrayList<>(solution.keySet());
        
        for (int i = 0; i < agentIds.size(); i++) {
            for (int j = i + 1; j < agentIds.size(); j++) {
                int agent1 = agentIds.get(i);
                int agent2 = agentIds.get(j);
                Path path1 = solution.get(agent1);
                Path path2 = solution.get(agent2);
                
                conflicts.addAll(findConflictsBetween(agent1, agent2, path1, path2));
            }
        }
        
        return conflicts;
    }
    
    private List<Conflict> findConflictsBetween(int agent1, int agent2, Path path1, Path path2) {
        List<Conflict> conflicts = new ArrayList<>();
        // Max time to check is the length of the longest path (number of positions)
        int maxTime = Math.max(path1.getLength(), path2.getLength());
        
        for (int t = 0; t < maxTime; t++) {
            Position pos1 = path1.getPosition(t);
            Position pos2 = path2.getPosition(t);
            
            // 1. Vertex Conflict
            if (pos1.equals(pos2)) {
                conflicts.add(new VertexConflict(agent1, agent2, t, pos1));
            }
            
            if (t > 0) {
                Position prev1 = path1.getPosition(t - 1);
                Position prev2 = path2.getPosition(t - 1);
                
                // 2. Edge Conflict (A->B at t-1/t, B->A at t-1/t)
                if (pos1.equals(prev2) && pos2.equals(prev1)) {
                    conflicts.add(new EdgeConflict(agent1, agent2, t, prev1, pos1));
                }
            }
        }
        
        return conflicts;
    }
    
    private CBSNode advanceSubgoalsAndReplan(CBSNode parent, List<Agent> agents, GoalDecomposition gd) {
        Map<Integer, Path> newSolution = new HashMap<>(parent.solution);
        boolean replanned = false;
        
        for (Agent agent : agents) {
            Path currentPath = parent.solution.get(agent.id);
            if (currentPath == null) continue;
            
            Position currentEffectiveGoal = gd.getEffectiveGoal(agent, currentPath);
            Position currentPathEnd = currentPath.getFinalPosition();

            // Check if the agent reached its effective goal, but not its final goal
            if (currentPathEnd.equals(currentEffectiveGoal) && !currentPathEnd.equals(agent.goal)) {
                
                // 1. Determine the global time the new segment must start.
                // Path length N means the path is (P0, P1, ..., PN-1).
                // The final position PN-1 is reached at time T = N-1.
                // The next move starts at global time T + 1 = N (currentPath.getLength()).
                int actualStartTime = currentPath.getLength(); 
                
                // 2. Get the next effective goal
                // The current path has already reached the old effective goal (currentEffectiveGoal).
                // We use a temporary minimal path (just the final position) to find the next target.
                Path pathFromEnd = new Path(Collections.singletonList(currentPathEnd)); 
                Position nextEffectiveGoal = gd.getEffectiveGoal(agent, pathFromEnd);

                if (nextEffectiveGoal != null) {
                    // 3. Replanning segment: from currentPathEnd to nextEffectiveGoal
                    Agent tempAgent = new Agent(agent.id, currentPathEnd, nextEffectiveGoal);
                    
                    // Pass the actual global start time (actualStartTime)
                    Path newSegment = pathfinder.findPath(tempAgent, parent.constraints, actualStartTime); 
                    
                    if (newSegment != null) {
                                               
                        List<Position> stitchedPath = new ArrayList<>();
                        stitchedPath.addAll(currentPath.getPositions());
                        stitchedPath.addAll(newSegment.getPositions().subList(1, newSegment.getLength()));
                        
                        newSolution.put(agent.id, new Path(stitchedPath));
                        replanned = true;
                    } else {
                                return null; 
                    }
                }
            } else {
                newSolution.put(agent.id, currentPath);
            }
        }

        if (replanned) {
            int newGCost = newSolution.values().stream().mapToInt(Path::getCost).sum();
            int newHCost = calculateHeuristic(newSolution, agents, map, gd);
            
            return new CBSNode(newSolution, parent.constraints, newGCost, newHCost, 
                              this.suboptimalityWeight, findConflicts(newSolution));
        }
        
        return null; 
    }
    
    protected boolean allAgentsAtFinalGoals(CBSNode node, List<Agent> agents) {
        for (Agent agent : agents) {
            Path currentPath = node.solution.get(agent.id);
            if (currentPath == null) return false;
            
            Position currentPos = currentPath.getFinalPosition();
            Position finalGoal = agent.goal;
            
            if (!currentPos.equals(finalGoal)) {
                return false;
            }
        }
        return true;
    }
    
    public static class Result {
        public final boolean success;
        public final boolean optimal;
        public final Map<Integer, Path> solution;
        public final int cost;
        public final long runtimeMs;
        public final String status;
        
        public Result(boolean success, Map<Integer, Path> solution, int cost, 
                     long runtimeMs, String status) {
            this.success = success;
            this.optimal = status.contains("Optimal");
            this.solution = solution;
            this.cost = cost;
            this.runtimeMs = runtimeMs;
            this.status = status;
        }
    }
}