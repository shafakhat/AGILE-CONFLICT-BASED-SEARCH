package phd.mauj;

class EdgeConflict extends Conflict {
    public final Position from, to;
    
    public EdgeConflict(int agent1, int agent2, int time, Position from, Position to) {
        super(agent1, agent2, time);
        this.from = from;
        this.to = to;
    }
}