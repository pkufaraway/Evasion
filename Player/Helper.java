import java.awt.geom.*;
import java.util.ArrayList;

public class Helper {
    public static boolean wallIntersect(Point2D hunter, Point2D prey, Wall wall){
        Line2D lineOne = new Line2D.Double(hunter, prey);
        Point2D p3,p4;
        if(wall.wallType == 0){
            p3 = new Point2D.Double(wall.start, wall.position);
            p4 = new Point2D.Double(wall.end, wall.position);
            if(!lineOne.intersectsLine(new Line2D.Double(p3,p4))){
                return false;
            }
        }
        else {
            p3 = new Point2D.Double(wall.position, wall.start);
            p4 = new Point2D.Double(wall.position, wall.end);
            if(!lineOne.intersectsLine(new Line2D.Double(p3,p4))){
                return false;
            }
        }
        return true;
    }

    public static boolean getCloser(Point2D hunter, Point2D prey, Point2D direction){
        double x1 = prey.getX() - hunter.getX();
        double y1 = prey.getY() - hunter.getY();
        double x2 = direction.getX();
        double y2 = direction.getY();
        return (x1 * x2 + y1 * y2) > 0;
    }
}
