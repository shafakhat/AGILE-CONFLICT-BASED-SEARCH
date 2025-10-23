package phd.mauj;

abstract class Constraint {
    public final int agent;
    public final int time;
    
    public Constraint(int agent, int time) {
        this.agent = agent;
        this.time = time;
    }
}