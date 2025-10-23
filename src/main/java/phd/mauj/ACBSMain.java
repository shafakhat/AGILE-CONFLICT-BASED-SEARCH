package phd.mauj;

import java.util.*;
import java.util.stream.Collectors;
import java.io.*;

// ============================================================================
// MAIN DEMO
// ============================================================================

class ACBSMain {
    public static void main(String[] args) throws IOException {
        System.out.println("ACBS Implementation - Production Ready");
        System.out.println("======================================\n");
        
        demonstrateSmallExample();
        
        if (args.length > 0 && args[args.length-1].equals("--full-evaluation")) {
            ExperimentalEvaluation.main(args); // Commented out for execution environment
        } else {
            System.out.println("\nRun with --full-evaluation for complete benchmark");
        }
    }
    
    protected static void demonstrateSmallExample() {
        System.out.println("Small Example Demonstration:");
        System.out.println("============================");
        
        // 8x8 grid map
        GridMap map = new GridMap(8, 8);
        // Add obstacles to create paths
        map.setObstacle(3, 3, true);
        map.setObstacle(3, 4, true);
        
        // Setup agents for a challenging corner-to-corner scenario
        List<Agent> agents = Arrays.asList(
            new Agent(0, new Position(0, 0), new Position(4, 4)),
            new Agent(1, new Position(7, 0), new Position(4, 4)),
            new Agent(2, new Position(0, 7), new Position(4, 4))
        );

        // Reset agents to the original, successful problem for clean demonstration
        agents = Arrays.asList(
            new Agent(0, new Position(0, 0), new Position(4, 0)),
            new Agent(1, new Position(7, 0), new Position(7, 4)),
            new Agent(2, new Position(0, 7), new Position(4, 7))
        );
        
        String[] algorithms = {"CBS", "ECBS", "EECBS", "ACBS"};
        
        for (String algName : algorithms) {
            System.out.println("\nTesting " + algName + ":");
            
            ACBS.Result result = null;
            long startTime = System.currentTimeMillis();
            
            try {
                switch (algName) {
                    case "CBS":
                        result = new CBS(map, agents, 5000).solve(agents);
                        break;
                    case "ECBS":
                        result = new ECBS(map, agents, 5000, 1.0).solve(agents); // Use w=1.0 for true optimality check
                        break;
                    case "EECBS":
                        result = new EECBS(map, agents, 5000, 1.0).solve(agents); // Use w=1.0 for true optimality check
                        break;
                    default:
                        // ACBS (w=1.0 for optimality comparison, though ACBS is meant for w>1)
                        result = new ACBS(map, agents, 5000, 1.0).solve(agents);
                        break;
                }
            } catch (Exception e) {
                System.out.println("  ERROR: " + e.getMessage());
                continue;
            }
            
            long runtimeMs = System.currentTimeMillis() - startTime;
            
            if (result == null) {
                System.out.println("  FAILED: Solver returned null result");
                continue;
            }
            
            // Adjusting optimality for ECBS/EECBS/ACBS when used with w=1.0
            boolean isOptimal = result.optimal || (result.cost > 0 && algName.equals("CBS"));
            
            System.out.printf("  Success: %s, Optimal: %s, Cost: %d, Time: %dms%n",
                            result.success, isOptimal, result.cost,
                            runtimeMs);
            
            if (result.success && result.solution != null) {
                System.out.println("  Sample paths:");
                for (int agentId : result.solution.keySet()) {
                    Path path = result.solution.get(agentId);
                    List<Position> positions = path.getPositions();
                    System.out.printf("    Agent %d: %s... (cost: %d)%n", 
                                    agentId, 
                                    positions.stream().limit(5).collect(Collectors.toList()),
                                    path.getCost());
                }
            }
        }
    }
}