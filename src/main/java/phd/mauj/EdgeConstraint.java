package phd.mauj;

class EdgeConstraint extends Constraint {
    public final Position from, to;
    
    public EdgeConstraint(int agent, int time, Position from, Position to) {
        super(agent, time);
        this.from = from;
        this.to = to;
    }
}