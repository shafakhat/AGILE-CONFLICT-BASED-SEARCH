package phd.mauj;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// ============================================================================
// MODERATE CONFLICT SCENARIO TESTER
// ============================================================================

class ModerateConflictTester {
    
    public static class TestScenario {
        public final String name;
        public final GridMap map;
        public final List<Agent> agents;
        public final String description;
        
        public TestScenario(String name, GridMap map, List<Agent> agents, String description) {
            this.name = name;
            this.map = map;
            this.agents = agents;
            this.description = description;
        }
    }
    
    // ============================================================================
    // PRE-DEFINED CONFLICT SCENARIOS
    // ============================================================================
    
    public static TestScenario createCrossingDiagonals() {
        System.out.println("Creating Crossing Diagonals Scenario...");
        
        // 8x8 grid with center obstacle to force interactions
        GridMap map = new GridMap(8, 8);
        // Add obstacles to create choke points
        map.setObstacle(3, 3, true);
        map.setObstacle(3, 4, true);
        map.setObstacle(4, 3, true);
        map.setObstacle(4, 4, true);
        
        List<Agent> agents = Arrays.asList(
            // These agents must cross paths and resolve conflicts
            new Agent(0, new Position(0, 0), new Position(7, 7)), // Top-left to bottom-right
            new Agent(1, new Position(7, 0), new Position(0, 7)), // Top-right to bottom-left
            new Agent(2, new Position(3, 0), new Position(3, 7)), // Top-center to bottom-center
            new Agent(3, new Position(0, 3), new Position(7, 3))  // Left-center to right-center
        );
        
        return new TestScenario(
            "Crossing Diagonals", 
            map, 
            agents,
            "4 agents with crossing paths through center, forcing vertex/edge conflicts"
        );
    }
    
    public static TestScenario createBottleneckScenario() {
        System.out.println("Creating Bottleneck Scenario...");
        
        // Create a map with narrow corridors
        GridMap map = new GridMap(10, 10);
        
        // Create horizontal barrier with small opening
        for (int x = 0; x < 10; x++) {
            if (x != 4 && x != 5) { // Leave openings at x=4,5
                map.setObstacle(x, 4, true);
                map.setObstacle(x, 5, true);
            }
        }
        
        // Create vertical barrier with small opening  
        for (int y = 0; y < 10; y++) {
            if (y != 4 && y != 5) { // Leave openings at y=4,5
                map.setObstacle(4, y, true);
                map.setObstacle(5, y, true);
            }
        }
        
        List<Agent> agents = Arrays.asList(
            // Agents competing for bottleneck spaces
            new Agent(0, new Position(0, 0), new Position(9, 9)), // Must go through center
            new Agent(1, new Position(9, 0), new Position(0, 9)), // Opposite direction
            new Agent(2, new Position(0, 9), new Position(9, 0)), // Cross direction
            new Agent(3, new Position(9, 9), new Position(0, 0)), // Return direction
            new Agent(4, new Position(2, 2), new Position(7, 7)), // Another through center
            new Agent(5, new Position(7, 2), new Position(2, 7))  // And another
        );
        
        return new TestScenario(
            "Bottleneck",
            map,
            agents,
            "6 agents competing for limited openings in barriers, testing priority resolution"
        );
    }
    
    public static TestScenario createDeadlockScenario() {
        System.out.println("Creating Deadlock Scenario...");
        
        // 6x6 grid with circular pattern
        GridMap map = new GridMap(6, 6);
        
        List<Agent> agents = Arrays.asList(
            // Circular deadlock scenario
            new Agent(0, new Position(1, 1), new Position(1, 2)), // Small moves that conflict
            new Agent(1, new Position(1, 2), new Position(2, 2)),
            new Agent(2, new Position(2, 2), new Position(2, 1)), 
            new Agent(3, new Position(2, 1), new Position(1, 1)),
            
            // Additional conflicting agents
            new Agent(4, new Position(4, 1), new Position(4, 4)),
            new Agent(5, new Position(1, 4), new Position(4, 1))
        );
        
        return new TestScenario(
            "Deadlock",
            map,
            agents,
            "6 agents in potential deadlock situations, testing advanced conflict resolution"
        );
    }
    
	
    public static TestScenario createWarehouseLikeScenario() {
        System.out.println("Creating Warehouse-like Scenario...");
        
        GridMap map = new GridMap(12, 8);
        
        // Create warehouse-like shelves/obstacles
        for (int y = 1; y < 7; y++) {
            if (y % 2 == 1) { // Create alternating gaps
                map.setObstacle(3, y, true);
                map.setObstacle(4, y, true);
                map.setObstacle(7, y, true);
                map.setObstacle(8, y, true);
            }
        }
        
        List<Agent> agents = Arrays.asList(
            // Pickers moving in aisles
            new Agent(0, new Position(0, 1), new Position(11, 1)),
            new Agent(1, new Position(0, 3), new Position(11, 3)),
            new Agent(2, new Position(0, 5), new Position(11, 5)),
            
            // Cross traffic
            new Agent(3, new Position(5, 0), new Position(5, 7)),
            new Agent(4, new Position(6, 7), new Position(6, 0)),
            
            // Additional agents
            new Agent(5, new Position(1, 0), new Position(1, 7)),
            new Agent(6, new Position(10, 7), new Position(10, 0))
        );
        
        return new TestScenario(
            "Warehouse-like",
            map,
            agents,
            "7 agents in warehouse layout with narrow aisles and crossing paths"
        );
    }
    
	
	public static TestScenario createComplexWarehouseScenario() {
		System.out.println("Creating Complex Warehouse Scenario...");
		
		GridMap map = new GridMap(16, 12); // Larger map for more complexity
		
		// Create dense warehouse shelving with narrow aisles
		for (int x = 0; x < 16; x++) {
			for (int y = 0; y < 12; y++) {
				// Create main shelving blocks
				if ((x >= 2 && x <= 4) || (x >= 6 && x <= 8) || (x >= 10 && x <= 12)) {
					if (y % 3 != 0) { // Leave gaps every 3 rows for cross aisles
						map.setObstacle(x, y, true);
					}
				}
				
				// Add central receiving area obstacles
				if (x >= 5 && x <= 9 && y >= 4 && y <= 7) {
					if ((x == 5 || x == 9) && (y >= 5 && y <= 6)) {
						map.setObstacle(x, y, true); // Vertical barriers
					}
					if ((y == 4 || y == 7) && (x >= 6 && x <= 8)) {
						map.setObstacle(x, y, true); // Horizontal barriers
					}
				}
			}
		}
		
		// Add some random obstacles to break symmetry
		map.setObstacle(3, 3, true);
		map.setObstacle(7, 6, true);
		map.setObstacle(11, 9, true);
		map.setObstacle(13, 2, true);

		List<Agent> agents = Arrays.asList(
			// === PICKERS IN NARROW AISLES ===
			// Long horizontal routes with potential head-on conflicts
			new Agent(0, new Position(0, 1), new Position(15, 1)),
			new Agent(1, new Position(15, 1), new Position(0, 1)),
			new Agent(2, new Position(0, 4), new Position(15, 4)),
			new Agent(3, new Position(15, 4), new Position(0, 4)),
			new Agent(4, new Position(0, 7), new Position(15, 7)),
			new Agent(5, new Position(15, 7), new Position(0, 7)),
			new Agent(6, new Position(0, 10), new Position(15, 10)),
			new Agent(7, new Position(15, 10), new Position(0, 10)),
			
			// === CROSS TRAFFIC === 
			// Vertical movers that must cross all horizontal aisles
			new Agent(8, new Position(1, 0), new Position(1, 11)),
			new Agent(9, new Position(5, 11), new Position(5, 0)),
			new Agent(10, new Position(9, 0), new Position(9, 11)),
			new Agent(11, new Position(13, 11), new Position(13, 0)),
			new Agent(12, new Position(14, 0), new Position(14, 11)),
			
			// === DOCK TO SHELF MOVEMENT ===
			// Complex paths through central area
			new Agent(13, new Position(7, 0), new Position(3, 8)),
			new Agent(14, new Position(7, 11), new Position(11, 3)),
			new Agent(15, new Position(0, 5), new Position(15, 8)),
			new Agent(16, new Position(15, 2), new Position(2, 11)),
			
			// === SHELF TO PACKING STATION ===
			// Diagonal and complex routes
			new Agent(17, new Position(4, 2), new Position(12, 9)),
			new Agent(18, new Position(12, 2), new Position(4, 9)),
			new Agent(19, new Position(2, 9), new Position(13, 2))
		);
		
		return new TestScenario(
			"Complex Warehouse",
			map,
			agents,
			"20 agents in dense warehouse with narrow aisles, crossing paths, " +
			"central obstacles, and complex diagonal routes. Tests advanced " +
			"conflict resolution and goal decomposition capabilities."
		);
	}
	
	
    // ============================================================================
    // TEST RUNNER
    // ============================================================================
    
    public static void runAllTests() {
        System.out.println("=================================================");
        System.out.println("MODERATE CONFLICT SCENARIO TEST SUITE");
        System.out.println("=================================================\n");
        
        TestScenario[] scenarios = {
            createCrossingDiagonals(),
            createBottleneckScenario(), 
            createDeadlockScenario(),
            createWarehouseLikeScenario()
        };
		       
        String[] algorithms = {"CBS", "ECBS", "EECBS", "ACBS"};
        
        for (TestScenario scenario : scenarios) {
            System.out.println("\n" + "=".repeat(60));
            System.out.println("SCENARIO: " + scenario.name);
            System.out.println("DESCRIPTION: " + scenario.description);
            System.out.println("AGENTS: " + scenario.agents.size());
            System.out.println("MAP: " + scenario.map.getWidth() + "x" + scenario.map.getHeight());
            System.out.println("=".repeat(60));
            
            Map<String, TestResult> results = new HashMap<>();
            
            for (String algName : algorithms) {
                System.out.println("\nTesting " + algName + ":");
                
                try {
                    ACBS.Result result = runAlgorithm(algName, scenario.map, scenario.agents, 120000); // 3000000 = 5mints |30000 = 30s timeout
                    TestResult testResult = analyzeResult(result, scenario.agents);
                    results.put(algName, testResult);
                    
                    printResultDetails(algName, testResult);
                    
                } catch (Exception e) {
                    System.out.println("  ERROR: " + e.getMessage());
                    results.put(algName, new TestResult(false, false, 0, 0, 0, 0, "Error: " + e.getMessage()));
                }
            }
            
            printComparativeAnalysis(results, scenario.name);
        }
    }
    
    private static ACBS.Result runAlgorithm(String algName, GridMap map, List<Agent> agents, long timeout) {
        long startTime = System.currentTimeMillis();
        ACBS.Result result = null;
        
        switch (algName) {
            case "CBS":
                result = new CBS(map, agents, timeout).solve(agents);
                break;
            case "ECBS":
                result = new ECBS(map, agents, timeout, 1.2).solve(agents);
                break;
            case "EECBS":
                result = new EECBS(map, agents, timeout, 1.2).solve(agents);
                break;
            case "ACBS":
                result = new ACBS(map, agents, timeout, 1.2).solve(agents);
                break;
        }
        
        return result;
    }
    
    private static TestResult analyzeResult(ACBS.Result result, List<Agent> agents) {
        if (result == null) {
            return new TestResult(false, false, 0, 0, 0, 0, "Null result");
        }
        
        // Analyze conflicts in the solution
        int vertexConflicts = 0;
        int edgeConflicts = 0;
        int totalPathLength = 0;
        
        if (result.success && result.solution != null) {
            // Check for remaining conflicts (should be 0 for valid solution)
            ACBS tempSolver = new ACBS(new GridMap(1,1), new ArrayList<>(), 1000, 1.0);
            List<Conflict> remainingConflicts = tempSolver.findConflicts(result.solution);
            
            for (Conflict conflict : remainingConflicts) {
                if (conflict instanceof VertexConflict) vertexConflicts++;
                else if (conflict instanceof EdgeConflict) edgeConflicts++;
            }
            
            // Calculate total path length
            totalPathLength = result.solution.values().stream()
                .mapToInt(Path::getLength)
                .sum();
        }
        
        return new TestResult(
            result.success,
            result.optimal,
            result.cost,
            result.runtimeMs,
            vertexConflicts,
            edgeConflicts,
            result.status
        );
    }
    
    private static void printResultDetails(String algName, TestResult result) {
        System.out.printf("  Success: %s, Optimal: %s, Cost: %d%n",
            result.success, result.optimal, result.cost);
        System.out.printf("  Time: %dms, Conflicts: %d vertex, %d edge%n",
            result.runtimeMs, result.vertexConflicts, result.edgeConflicts);
        System.out.printf("  Status: %s%n", result.status);
        
        if (result.success && result.vertexConflicts + result.edgeConflicts > 0) {
            System.out.println("  ⚠️  WARNING: Solution has remaining conflicts!");
        }
    }
    
    private static void printComparativeAnalysis(Map<String, TestResult> results, String scenarioName) {
        System.out.println("\n" + "-".repeat(50));
        System.out.println("COMPARATIVE ANALYSIS: " + scenarioName);
        System.out.println("-".repeat(50));
        
        TestResult cbsResult = results.get("CBS");
        if (cbsResult == null || !cbsResult.success) {
            System.out.println("CBS failed - cannot compute speedup factors");
            //return;
        }
        
        System.out.printf("%-8s | %-8s | %-6s | %-12s | %-10s%n", 
            "Algo", "Time(ms)", "Speedup", "Cost", "Conflicts");
        System.out.println("-".repeat(65));
        
        for (String alg : new String[]{"CBS", "ECBS", "EECBS", "ACBS"}) {
            TestResult r = results.get(alg);
            if (r != null) {
                double speedup = (double) cbsResult.runtimeMs / Math.max(1, r.runtimeMs);
                String conflictStr = r.vertexConflicts + "V/" + r.edgeConflicts + "E";
                
                System.out.printf("%-8s | %-8d | %-6.1fx | %-12d | %-10s%n",
                    alg, r.runtimeMs, speedup, r.cost, conflictStr);
            }
        }
        
        // Highlight the best performer
        String bestAlg = findBestPerformer(results);
        if (bestAlg != null) {
            System.out.println("\n🏆 BEST PERFORMER: " + bestAlg);
        }
    }
    
    private static String findBestPerformer(Map<String, TestResult> results) {
        String bestAlg = null;
        double bestScore = Double.NEGATIVE_INFINITY;
        
        for (Map.Entry<String, TestResult> entry : results.entrySet()) {
            TestResult r = entry.getValue();
            //if (!r.success) continue;
            
            // Score: combination of speed and solution quality
            /*double score = 1000.0 / (r.runtimeMs + 1) // Speed component
                        + (r.optimal ? 100 : 0)       // Optimality bonus
                        - (r.vertexConflicts + r.edgeConflicts) * 10; // Conflict penalty
            */
			
			double score = ((1000.0 / (r.runtimeMs + 1)) + (r.optimal ? 100 : 0) - ((r.vertexConflicts + r.edgeConflicts) * 10)); 

			if (score > bestScore) {
                bestScore = score;
                bestAlg = entry.getKey();
            }
        }
        
        return bestAlg;
    }
    
    // ============================================================================
    // INDIVIDUAL SCENARIO TEST METHOD
    // ============================================================================
    
    public static void testSingleScenario(String scenarioType) {
        TestScenario scenario = null;
        
        switch (scenarioType.toLowerCase()) {
            case "crossing":
                scenario = createCrossingDiagonals();
                break;
            case "bottleneck":
                scenario = createBottleneckScenario();
                break;
            case "deadlock":
                scenario = createDeadlockScenario();
                break;
            case "warehouse":
                scenario = createWarehouseLikeScenario();
                break;
			case "warehouse1":
                scenario = createComplexWarehouseScenario();
                break;
            default:
                System.out.println("Unknown scenario type: " + scenarioType);
                return;
        }
        
        System.out.println("\n" + "=".repeat(60));
        System.out.println("TESTING SINGLE SCENARIO: " + scenario.name);
        System.out.println("=".repeat(60));
        
        String[] algorithms = {"CBS", "ECBS", "EECBS", "ACBS"};
        Map<String, TestResult> results = new HashMap<>();
        
        for (String algName : algorithms) {
            System.out.println("\nTesting " + algName + ":");
            try {
                ACBS.Result result = runAlgorithm(algName, scenario.map, scenario.agents, 30000);
                TestResult testResult = analyzeResult(result, scenario.agents);
                results.put(algName, testResult);
                printResultDetails(algName, testResult);
            } catch (Exception e) {
                System.out.println("  ERROR: " + e.getMessage());
            }
        }
        
        printComparativeAnalysis(results, scenario.name);
    }
    
    // ============================================================================
    // TEST RESULT CLASS
    // ============================================================================
    
    static class TestResult {
        final boolean success;
        final boolean optimal;
        final int cost;
        final long runtimeMs;
        final int vertexConflicts;
        final int edgeConflicts;
        final String status;
        
        TestResult(boolean success, boolean optimal, int cost, long runtimeMs, 
                  int vertexConflicts, int edgeConflicts, String status) {
            this.success = success;
            this.optimal = optimal;
            this.cost = cost;
            this.runtimeMs = runtimeMs;
            this.vertexConflicts = vertexConflicts;
            this.edgeConflicts = edgeConflicts;
            this.status = status;
        }
    }
    
    // ============================================================================
    // MAIN METHOD FOR DIRECT EXECUTION
    // ============================================================================
    
    public static void main(String[] args) {
        if (args.length > 0) {
            // Test specific scenario: "crossing", "bottleneck", "deadlock", "warehouse"
            testSingleScenario(args[0]);
        } else {
            // Run all tests
            runAllTests();
        }
    }
}