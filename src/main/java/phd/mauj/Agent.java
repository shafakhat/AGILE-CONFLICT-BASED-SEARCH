package phd.mauj;

class Agent {
    public final int id;
    public final Position start;
    public final Position goal;
    
    public Agent(int id, Position start, Position goal) {
        this.id = id;
        this.start = start;
        this.goal = goal;
    }
}