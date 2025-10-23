package phd.mauj;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

// ============================================================================
// EXPERIMENTAL EVALUATION
// ============================================================================

class ExperimentalEvaluation {
    private static final long TIMEOUT_MS = 30000; // 300000 = 5 minutes | 60000 = 1 minute
    private static int[] AGENT_COUNTS = {100, 300, 500, 700, 900, 1100, 1300, 1500, 1700, 1900, 2000};
    private static final int INSTANCES_PER_CONFIG = 50; // instances per config standard is : 50
    
    public static void main(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--agents") && i + 1 < args.length) {
                String range = args[i + 1];
                String[] parts = range.split("-");
                int start = Integer.parseInt(parts[0]);
                int end = Integer.parseInt(parts[1]);
                int step = 200;
                List<Integer> counts = new ArrayList<>();
                for (int j = start; j <= end; j += step) {
                    counts.add(j);
                }
                AGENT_COUNTS = counts.stream().mapToInt(Integer::intValue).toArray();
            }
        }
        
        System.out.println("ACBS Experimental Evaluation");
        System.out.println("============================\n");
        
        Map<String, GridMap> testMaps = new HashMap<>();

        try {
            // Placeholder: Assume map files exist or skip if not.
            testMaps.put("empty-32x32", MovingAIMapLoader.loadMap("maps/empty-32-32.map"));
            testMaps.put("warehouse-10-20", MovingAIMapLoader.loadMap("maps/warehouse-10-20-10-2-1.map"));
            testMaps.put("den520d", MovingAIMapLoader.loadMap("maps/den520d.map"));
            
            // To ensure the demo runs without actual map files:
            //testMaps.put("small-dummy", new GridMap(64, 64));
        } catch (Exception e) {
            System.err.println("ERROR: Could not load map files! Using dummy map. Full evaluation may not be accurate.");
             testMaps.put("small-dummy", new GridMap(64, 64));
        }
        
        try (PrintWriter writer = new PrintWriter(new FileWriter("results.csv"))) {
            writer.println("Algorithm,Map,Agents,Instance,Success,Optimal,Cost,Runtime(ms),Status");
            
            for (String mapName : testMaps.keySet()) {
                System.out.println("\nTesting map: " + mapName);
                GridMap map = testMaps.get(mapName);
                
                for (int agentCount : AGENT_COUNTS) {
                    System.out.println("  Agent count: " + agentCount);
                    Random random = new Random(42);
                    
                    for (int instance = 0; instance < INSTANCES_PER_CONFIG; instance++) {
                        List<Agent> agents = MovingAIMapLoader.generateRandomInstances(map, agentCount, random);
        
                        if (agents.size() < agentCount) {
                            continue;
                        }
                        
                        try {
                            Map<String, ACBS> algorithms = new HashMap<>();
                            algorithms.put("ACBS", new ACBS(map, agents, TIMEOUT_MS, 1.2));
                            algorithms.put("CBS", new CBS(map, agents, TIMEOUT_MS));
                            algorithms.put("ECBS", new ECBS(map, agents, TIMEOUT_MS, 1.2));
                            algorithms.put("EECBS", new EECBS(map, agents, TIMEOUT_MS, 1.2));
                            
                            for (String algName : algorithms.keySet()) {
                                ACBS.Result result = algorithms.get(algName).solve(agents);
                                
                                writer.printf("%s,%s,%d,%d,%s,%s,%d,%d,%s%n",
                                    algName, mapName, agentCount, instance,
                                    result.success, result.optimal, result.cost, 
                                    result.runtimeMs, result.status);
                                writer.flush();
                            }
                        } catch (Exception e) {
                            System.err.println("Error in instance " + instance + ": " + e.getMessage());
                        }
                        
                        System.gc();
                    }
                }
            }
        }
        
        System.out.println("\nExperiment completed. Results saved to results.csv");
        generateSummaryReport();
        System.exit(0);
    }
    
    // Summary report generation logic is retained but commented out since it relies on external files
    private static void generateSummaryReport() throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader("results.csv"));
             PrintWriter writer = new PrintWriter(new FileWriter("summary.txt"))) {
            
            writer.println("ACBS Implementation Validation Report");
            writer.println("=====================================\n");
            
            String line = reader.readLine();
            Map<String, List<Double>> runtimes = new HashMap<>();
            Map<String, Integer> successCounts = new HashMap<>();
            Map<String, Integer> totalInstances = new HashMap<>();
            
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                String algorithm = parts[0];
                boolean success = Boolean.parseBoolean(parts[4]);
                long runtime = Long.parseLong(parts[7]);
                
                runtimes.computeIfAbsent(algorithm, k -> new ArrayList<>()).add((double) runtime);
                totalInstances.put(algorithm, totalInstances.getOrDefault(algorithm, 0) + 1);
                if (success) {
                    successCounts.put(algorithm, successCounts.getOrDefault(algorithm, 0) + 1);
                }
            }
            
            writer.println("Performance Summary:");
            writer.println("===================");
            for (String alg : Arrays.asList("ACBS", "CBS", "ECBS", "EECBS")) {
                List<Double> times = runtimes.get(alg);
                int successes = successCounts.getOrDefault(alg, 0);
                int total = totalInstances.getOrDefault(alg, 0);
                
                if (times != null && !times.isEmpty()) {
                    double avgRuntime = times.stream().mapToDouble(d -> d).average().orElse(0);
                    double successRate = (double) successes / total * 100;
                    
                    writer.printf("%s: Success Rate: %.1f%%, Avg Runtime: %.0fms (Total Runs: %d)%n", 
                                alg, successRate, avgRuntime, total);
                }
            }
        }
    }
}