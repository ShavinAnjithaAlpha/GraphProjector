package util;

import javafx.scene.layout.Pane;

import java.security.InvalidParameterException;
import java.util.InputMismatchException;

public class SimpleLine {

    // declare the tangent and intersect
    private double tangent;
    private double intersect;

    private static double length = 5;

    public SimpleLine(double tangent, double intersect){
        this.tangent = tangent;
        this.intersect = intersect;
    }

    public SimpleLine(double tangent , double x, double y){
        this.tangent = tangent;
        this.intersect = y - x * tangent;
    }

    public SimpleLine(double tangent, Point point){
        this(tangent, point.getX(), point.getY());
    }

    public SimpleLine(double x1, double y1, double x2, double y2){
        if (x1 == x2){
            throw new InvalidParameterException("cannot be equals the x coordinates.");
        }
        // calculate the tangent and intersect
        tangent = (y1 - y2) / (x1 - x2);
        intersect = y2 - tangent * x2;
    }

    public SimpleLine(Point p1, Point p2){
        this(p1.getX() , p1.getY(), p2.getX(), p2.getY());

    }

    public double getTangent() {
        return tangent;
    }

    public void setTangent(double tangent) {
        this.tangent = tangent;
    }

    public double getIntersect() {
        return intersect;
    }

    public void setIntersect(double intersect) {
        this.intersect = intersect;
    }

    // create the util methods for class
    public double getY(double x){
        return tangent * x + intersect;
    }

    public boolean isOnLine(double x , double y){
        return Math.abs(y - (tangent * x + intersect))  < 0.05;
    }

    public boolean isOnLine(Point point){
        return point.getY() == tangent * point.getX() + intersect;
    }

    public Point getPoint(double x){
        return new Point(x , tangent * x + intersect);
    }

    public Point[] getPoints(Point center) {
        if (length <= 0) {
            throw new InputMismatchException("cannot get the negative or zero values.");
        }
//        if (!(isOnLine(center))) {
//            throw new InputMismatchException("cannot get point that out of the line.");
//        }

        // calculate the t values
        double t = length / (Math.sqrt(Math.pow(tangent , 2) + 1));
        final Point startPoint = new Point(t + center.getX() , tangent * t + center.getY());
        final Point endPoint = new Point(-t + center.getX(), -t * tangent + center.getY());

        // declare the new ponit array
        Point[] points = {startPoint, endPoint};
        return points;
    }


}
