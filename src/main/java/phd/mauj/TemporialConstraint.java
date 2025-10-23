package phd.mauj;

class TemporalConstraint extends Constraint {
    public final Position position;
    public final int delaySteps;
    
    public TemporalConstraint(int agent, int time, Position position, int delaySteps) {
        super(agent, time);
        this.position = position;
        this.delaySteps = delaySteps;
    }
}