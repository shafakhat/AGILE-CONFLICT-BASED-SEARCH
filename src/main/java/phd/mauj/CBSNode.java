package phd.mauj;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

// ============================================================================
// CBS NODE
// ============================================================================

class CBSNode implements Comparable<CBSNode> {
    public final Map<Integer, Path> solution;
    public final Set<Constraint> constraints;
    public final int gCost;
    public final int hCost;
    public final int fCost;
    public final int eCost;
    public final List<Conflict> conflicts;
    public String strategyType = "NONE";
    
    public CBSNode(Map<Integer, Path> solution, Set<Constraint> constraints, 
                  int gCost, int hCost, double w, List<Conflict> conflicts) {
        this.solution = new HashMap<>(solution);
        this.constraints = new HashSet<>(constraints);
        this.gCost = gCost;
        this.hCost = hCost;
        this.fCost = gCost + hCost;
        this.eCost = (int) (gCost + w * hCost);
        this.conflicts = new ArrayList<>(conflicts);
    }
    
    @Override
    public int compareTo(CBSNode other) {
        int fComp = Integer.compare(this.fCost, other.fCost);
        if (fComp != 0) return fComp;
        return Integer.compare(this.gCost, other.gCost);
    }
}