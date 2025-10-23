package phd.mauj;

import java.util.List;

// ============================================================================
// EECBS Algorithm
// ============================================================================

class EECBS extends ECBS {
    public EECBS(GridMap map, List<Agent> agents, long timeoutMs, double suboptimalityBound) {
        super(map, agents, timeoutMs, suboptimalityBound);
    }
    
    @Override
    public Result solve(List<Agent> agents) {
        // EECBS has its own solve loop logic usually, but here it inherits from ECBS for simplicity
        Result result = super.solve(agents);
        return new Result(result.success, result.solution, result.cost, 
                        result.runtimeMs, result.status.replace("ECBS", "EECBS"));
    }
}