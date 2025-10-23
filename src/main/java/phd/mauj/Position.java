package phd.mauj;

import java.util.Objects;

class Position {
    public final int x, y;
    
    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Position)) return false;
        Position p = (Position) obj;
        return x == p.x && y == p.y;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }
    
    @Override
    public String toString() {
        return "(" + x + "," + y + ")";
    }
}