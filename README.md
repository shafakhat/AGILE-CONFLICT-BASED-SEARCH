# AGILE CONFLICT BASED SEARCH (ACBS)

This repository contains the full source code for the **Agile Conflict-Based Search (ACBS)** algorithm, alongside the comparative evaluation framework used for the publication. ACBS is a high-performance, bounded-suboptimal solver for large-scale Multi-Agent Path Finding (MAPF), achieving significant speedups over state-of-the-art methods like EECBS while rigorously maintaining a user-defined suboptimality bound (ω).

---

## Implementation Details

The source code provides a complete, self-contained evaluation framework for MAPF algorithms, including:

1.  **Core Data Structures:** Agent, Position, Path, Constraints, and Conflict management.
2.  **Map Integration:** Grid map structures with **JGraphT** integration for efficient graph traversal.
3.  **Pathfinders:**
    * A* with temporal constraints.
    * Enhanced A* with Focal Search for ECBS and EECBS.
4.  **Complete Algorithms:** Implementations for **ACBS**, **CBS**, **ECBS**, and **EECBS** algorithms.
5.  **Benchmark Tools:** **Moving AI Lab** map and instance loader, a full experimental evaluation framework, and automated summary report generation.
6.  **Utility:** A quick demo mode with a small, verifiable example.

---

## How to Run

This project is implemented in Java and requires the JGraphT library for graph operations.

### Requirements

* **Java Development Kit (JDK) 8 or higher**
* **JGraphT library** (e.g., `jgrapht-core-1.5.1.jar`)
    * *Download from Maven Central or similar source.*
* **Map Files:** Standard MAPF maps and scenarios.
    * *Download from [Moving AI Lab Benchmarks](https://movingai.com/benchmarks/mapf/)*
* **Setup:** Place all downloaded map files inside a local `maps/` directory.

### Execution

1.  **Compile:** Ensure `jgrapht-core-1.5.1.jar` is in your classpath.
    ```bash
    javac -cp jgrapht-core-1.5.1.jar ACBSDemo.java
    ```

2.  **Quick Demo (Small Example):**
    ```bash
    java -cp ".;jgrapht-core-1.5.1.jar" ACBSDemo
    ```

3.  **Full Evaluation (Recommended Practical Run):**
    Runs a reduced set of agents (e.g., 100-500) to obtain meaningful, non-timeout results quickly.
    ```bash
    java -cp ".;jgrapht-core-1.5.1.jar" ACBSDemo --full-evaluation
    ```

4.  **Custom Agent Range:**
    Specify the exact range of agents for the full evaluation.
    ```bash
    java -cp ".;jgrapht-core-1.5.1.jar" ACBSDemo --agents 100-500 --full-evaluation
    ```

---

## Output Files

Upon completion of an evaluation run, the following files will be generated in the root directory:

* `results.csv`: The **raw experimental data**, including runtime, solution cost, and success status for every single instance run.
* `summary.txt`: A clean **performance summary** showing aggregated success rates, average runtimes, and average costs across all tested configurations.

---

## ⚠️ Important Computational Warning ⚠️

**Replicating the full paper's benchmark requires significant computational resources.** Please read this section before attempting a full run.

The default full configuration is: 3 maps × 11 agent counts (up to 2000) × 50 instances × 4 algorithms = **6,600 total runs**, with a 60-second timeout per instance.

| Agent Range | Avg Time/Instance | Estimated Total Time | Failure Rate |
| :--- | :--- | :--- | :--- |
| **100 - 500** | 5 - 15 seconds | ~8 hours | Low |
| **1700 - 2000**| 50 - 60 seconds | ~50 hours | **Very High** |
| **FULL RUN** | N/A | **5 - 7 Days** (Continuous) | High (due to timeouts) |

**Practical Recommendation:**

For initial testing and verification on a standard desktop machine, **do not run the full benchmark** above 1500 agents. Use the custom range flag to limit the agent count to a practical level:

```bash
# Recommended for meaningful results on a single machine (~8-12 hours)
java -cp ".;jgrapht-core-1.5.1.jar" ACBSDemo --agents 100-500 --full-evaluation
This would reduce total time to 12-24 hours, but most 2000-agent instances will still timeout with 75%+ failure rates.
```
If you want to test on 2000 agents, with instances: 50
This will take an approximate total time of 23 days, but most 2000-agent instances will still timeout with 75%+ failure rates.

**Bottom line:** 
To obtain results with 2000 agents likely required cluster computing, not a single machine. Expect 5-7 days for the full benchmark, with diminishing returns as most high-agent-count runs will fail.
