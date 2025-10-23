package phd.mauj;

class VertexConstraint extends Constraint {
    public final Position position;
    
    public VertexConstraint(int agent, int time, Position position) {
        super(agent, time);
        this.position = position;
    }
}