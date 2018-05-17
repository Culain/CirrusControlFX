package CirrusControl.Main;

import java.util.Locale;

public class Position {
    private double X, Y, Z, W, P, R;

    Position(float x, float y, float z, float w, float p, float r) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        this.W = w;
        this.P = p;
        this.R = r;
    }

    public void add(Offset offset) {
        this.X += offset.X;
        this.Y += offset.Y;
        this.Z += offset.Z;
        this.W += offset.W;
        this.P += offset.P;
        this.R += offset.R;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%.3f,%.3f,%.3f,%.3f,%.3f,%.3f", X, Y, Z, W, P, R);
    }
}

class Offset /*extends Position*/ {
    float X, Y, Z, W, P, R;

    Offset(float x, float y, float z, float w, float p, float r) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        this.W = w;
        this.P = p;
        this.R = r;
    }
}