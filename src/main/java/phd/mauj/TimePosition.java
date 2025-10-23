package phd.mauj;

import java.util.Objects;

class TimePosition extends Position {
    public final int time;
    
    public TimePosition(int x, int y, int time) {
        super(x, y);
        this.time = time;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof TimePosition)) return false;
        TimePosition tp = (TimePosition) obj;
        return super.equals(tp) && time == tp.time;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), time);
    }
}