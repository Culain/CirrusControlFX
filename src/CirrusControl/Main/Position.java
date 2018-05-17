package CirrusControl.Main;

import java.util.Locale;

public class Position implements ToListEntry {
    private double X, Y, Z, W, P, R;

    Position(float x, float y, float z, float w, float p, float r) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        this.W = w % 360;
        this.P = p % 360;
        this.R = r % 360;
    }

    public void add(Offset offset) {
        this.X += offset.X;
        this.Y += offset.Y;
        this.Z += offset.Z;
        this.W = (this.W + offset.W) % 360;
        this.P = (this.P + offset.P) % 360;
        this.R = (this.R + offset.R) % 360;
    }

    @Override
    public String toString() {
        return String.format(Locale.US, "%.3f, %.3f, %.3f, %.3f, %.3f, %.3f", X, Y, Z, W, P, R);
    }

    @Override
    public String toListEntry() {
        return String.format(Locale.US,
                "\t\t\t\tX:\t%.3f\n" +
                        "\t\t\t\tY:\t%.3f\n" +
                        "\t\t\t\tZ:\t%.3f\n" +
                        "\t\t\t\tW:\t%.3f\n" +
                        "\t\t\t\tP:\t%.3f\n" +
                        "\t\t\t\tR:\t%.3f", X, Y, Z, W, P, R);
    }
}

class Offset implements ToListEntry {
    float X, Y, Z, W, P, R;

    Offset(float x, float y, float z, float w, float p, float r) {
        this.X = x;
        this.Y = y;
        this.Z = z;
        this.W = w;
        this.P = p;
        this.R = r;
    }

    @Override
    public String toListEntry() {
        return String.format(Locale.US,
                "\t\t\t\tX:\t%.3f\n" +
                        "\t\t\t\tY:\t%.3f\n" +
                        "\t\t\t\tZ:\t%.3f\n" +
                        "\t\t\t\tW:\t%.3f\n" +
                        "\t\t\t\tP:\t%.3f\n" +
                        "\t\t\t\tR:\t%.3f", X, Y, Z, W, P, R);
    }
}