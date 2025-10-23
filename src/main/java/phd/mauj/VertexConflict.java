package phd.mauj;

class VertexConflict extends Conflict {
    public final Position position;
    
    public VertexConflict(int agent1, int agent2, int time, Position position) {
        super(agent1, agent2, time);
        this.position = position;
    }
}