package phd.mauj;

abstract class Conflict {
    public final int agent1, agent2;
    public final int time;
    
    public Conflict(int agent1, int agent2, int time) {
        this.agent1 = agent1;
        this.agent2 = agent2;
        this.time = time;
    }
}
