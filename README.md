# AGILE-CONFLICT-BASED-SEARCH
## This repository contains the complete simulation code, benchmarks, and configuration files for the paper: "ACBS: A Bounded-Suboptimal Multi-Agent Path Finding Solver for Search-Based Problems."
==============================================================
ACBS Simulation using full Moving AI Lab benchmark Integration
==============================================================
Implementation Source code includes
-----------------------------------
1. All core data structures (Agent, Position, Path, Constraints, Conflicts)
2. Grid map with JGraphT integration
3. Goal decomposition (midpoint-based)
4. A* pathfinder with temporal constraints
5. Enhanced A* with Focal Search for ECBS
6. Complete ACBS, CBS, ECBS, EECBS algorithms
7. Moving AI Lab map loader
8. Full experimental evaluation framework
9. Summary report generation
10. Demo with small example
-----------
How to Run:
-----------
Compile (ensure JGraphT is in classpath)
javac -cp jgrapht-core-1.5.1.jar ACBSDemo.java

Quick demo
java -cp “.;jgrapht-core-1.5.1.jar” ACBSDemo

Full evaluation
java -cp “.;jgrapht-core-1.5.1.jar” ACBSDemo --full-evaluation

Custom agent range
java -cp “.;jgrapht-core-1.5.1.jar” ACBSDemo --agents 100-500 --full-evaluation

-------------
Requirements:
-------------
• JGraphT library (download from Maven Central)
• Map files (download from https://movingai.com/benchmarks/mapf/)
• Place maps in maps/ directory

-------------
Output Files:
-------------
• results.csv - Raw experimental data
• summary.txt - Performance summary with success rates and runtimes
 
Computational requirements:
---------------------------
Time Estimation for 2000 Agents
Configuration:
• 3 maps (empty-32x32, warehouse-10-20, den520d)
• 11 agent counts (100, 300, 500, 700, 900, 1100, 1300, 1500, 1700, 1900, 2000)
• 50 instances per configuration
• 4 algorithms (CBS, ECBS, EECBS, ACBS)
• 60-second timeout per instance

---------------
Best-Case Math:
---------------
Total instances = 3 maps × 11 agent counts × 50 instances × 4 algorithms
                = 6,600 runs

If all complete in ~1 second (very optimistic):
	Total time = 6,600 seconds ≈ 1.8 hours

-------------------
Realistic Scenario:
-------------------
The code will likely never complete for 2000 agents because:
1. CBS will timeout at ~500-700 agents (most instances hit 60s limit)
2. ECBS will timeout at ~900-1100 agents
3. EECBS might reach 1500-1700 agents before consistent timeouts
4. ACBS (claimed 5x speedup) probably reach 1900-2000 agents

-------------------------
Actual timeline estimate:
--------------------------------------------------------------------
Agent Range	Avg Time/Instance		Total Time for Range
--------------------------------------------------------------------
100-500		5-15 seconds			~8 hours
700-900		20-45 seconds			~20 hours
1100-1500	30-60 seconds			~40 hours
1700-2000	50-60 seconds (many timeouts)	~50 hours
--------------------------------------------------------------------
		Total: 5-7 days of continuous computation
--------------------------------------------------------------------

----------------
Critical Issues:
----------------
1. Memory exhaustion - 2000 agents will consume gigabytes of RAM for constraint trees
2. Most runs will timeout - You'll get more "TIMEOUT" results than actual solutions
3. The paper's claims are questionable - Without the actual optimized implementation, you won't see 5x speedup

-------------------------
Practical Recommendation:
-------------------------
Don't run the full benchmark. Instead: Test with reduced configuration
java ACBSDemo --agents 100-500 --full-evaluation

This gives you:
• Agents: 100, 300, 500 (3 counts instead of 11)
• Time: ~8-12 hours instead of days
• Meaningful data: You'll actually get solutions, not timeouts

If you want to test on 2000 agents, reduce instances: 5 instances instead of 50
INSTANCES_PER_CONFIG = 5;  // in code

This would reduce total time to 12-24 hours, but most 2000-agent instances will still timeout with 75%+ failure rates.

If you want to test on 2000 agents, with instances: 50

This will take an approximate total time of 23 days, but most 2000-agent instances will still timeout with 75%+ failure rates.

Bottom line: To obtain results with 2000 agents likely required cluster computing, not a single machine. Expect 5-7 days for the full benchmark, with diminishing returns as most high-agent-count runs will fail.

