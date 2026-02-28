package eus.elkarmedia.apnea;

public class PostureCalculator {

    public final static int LEFT = 0;
    public final static int RIGHT = 1;
    public final static int BACK = 2;
    public final static int STOMACH = 3;
    public final static int UP = 4;
    public final static int UNKNOWN = -1;

    public static int getPosition(double x, double y, double z, String orientation) {
        if (orientation == null) {
            orientation = "Left"; // Default orientation
        }

        if (orientation.equals("Right")) {
            y = -y;
        }
        if (orientation.equals("Up")) {
            double lag = x;
            x = y;
            y = lag;
        }
        if (orientation.equals("Down")) {
            double lag = x;
            x = y;
            y = -lag;
        }
        
        double absX = Math.abs(x);
        double absY = Math.abs(y);
        double absZ = Math.abs(z);

        if (absX >= absY && absX >= absZ) {
            // gotten up
            return UP;
        } else if (absY >= absX && absY >= absZ) {
            // correct
            if (y > 0) {
                return RIGHT;
            } else {
                return LEFT;
            }
        } else if (absZ >= absX && absZ >= absY) {
            // back or stomach
            if (z > 0) {
                return BACK;
            } else {
                return STOMACH;
            }
        } else {
            return UNKNOWN;
        }
    }
}
