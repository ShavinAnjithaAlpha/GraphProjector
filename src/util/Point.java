package util;


import java.awt.event.MouseEvent;
import java.util.Objects;

public class Point {

    // declare the x and y
    private double x;
    private double y;

    public Point(double x, double y){
        this.x = x;
        this.y =y;
    }

    public Point(){
        this(0, 0);
    }

    public Point(MouseEvent event){
        this(event.getX() , event.getY());
    }

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Point point = (Point) o;
        return Double.compare(point.x, x) == 0 && Double.compare(point.y, y) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    // reset method
    public void reset(){
        x = 0;
        y = 0;
    }

    public double length(){
        return Math.sqrt(Math.pow(x , 2) + Math.pow(y , 2));
    }

    public boolean isXPositive(){
        if (x > 0){
            return true;
        }
        return false;
    }

    public boolean isYPositive(){
        if (y > 0){
            return true;
        }
        return false;
    }

    public void translateToCanvas(GridSystem grid){
        Point point = grid.translateToCanvas(this);
        setX(point.getX());
        setY(point.getY());

    }

    public void translaeToGrid(GridSystem grid){
        Point point = grid.translateToGrid(this);
        setX(point.getX());
        setY(point.getY());
    }
}
