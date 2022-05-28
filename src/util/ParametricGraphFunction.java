package util;

import com.sun.javaws.exceptions.InvalidArgumentException;
import com.sun.org.apache.xpath.internal.functions.FuncId;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import sun.java2d.pipe.SpanShapeRenderer;
import widgets.ParametricFunctionPane;

public class ParametricGraphFunction {
    // functions
    private Function xFunction;
    private Function yFunction;
    // limits
    private double t1;
    private double t2;
    // declare the data members
    private Path graphPath;
    private Pane canvas;
    private GridSystem grid;
    private Color color  = Color.ORANGE;

    private static int counter = 0;

    private static double stepSize = 0.01;

    public ParametricGraphFunction(String xFunction, String yFunction, GridSystem grid,Pane canvas, double t1, double t2){
        this.xFunction = new Function(xFunction);
        this.yFunction = new Function(yFunction);
        this.grid = grid;
//        if (t1 >= t2)
//            throw new Exception("must be t1 > t2");
        this.t1 = t1;
        this.t2 = t2;

        this.canvas = canvas;
        this.graphPath = new Path();
        this.graphPath.setStroke(Color.ORANGE);
        graphPath.setUserData(String.format("function|%d", ParametricGraphFunction.counter++));

    }

    public void setColor(Color color){
        this.color = color;
        graphPath.setFill(color);
    }

    public Color getColor(){
        return (Color)graphPath.getFill();
    }

    public double getT1() {
        return t1;
    }

    public double getT2(){
        return t2;
    }

    public void draw(){
        graphPath.setStroke(color);
        graphPath.setFill(null);
        graphPath.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));
        // draw the graph on the canvas use the grid system
        final Point startPoint = grid.translateToCanvas(
                new Point(
                        xFunction.getValue(t1),
                        yFunction.getValue(t1)
                ));
        final MoveTo moveTo = new MoveTo(startPoint.getX(), startPoint.getY());

        // add to the Path elemtn
        graphPath.setStrokeWidth(4);
        graphPath.getElements().add(moveTo);

        double t = t1;
        while (t <= t2){
            // increment the x value
            t += stepSize;
            // get the new point
            Point endPoint = grid.translateToCanvas(
                    new Point(
                            xFunction.getValue(t),
                            yFunction.getValue(t)
                    )
            );
            // draw the line
            final LineTo lineTo = new LineTo(endPoint.getX(), endPoint.getY());
            // get the path and add
            graphPath.getElements().add(lineTo);

        }

        // add to the canvas
        if (!(canvas.getChildren().contains(graphPath))){
            canvas.getChildren().add(graphPath);

        }

    }

    public Function getxFunction(){
        return xFunction;
    }

    public Point getPoint(double value){
        return new Point(xFunction.getValue(value), yFunction.getValue(value));
    }

    public SimpleLine getTangentLine(double value){
        // get the derivatives from the function x and y
        // and calculate the full derivative
        double tangent = yFunction.getTangent(value) / xFunction.getTangent(value);
        return new SimpleLine(tangent, getPoint(value));
    }

    public SimpleLine getNormalLine(double value){
        // get the derivatives from the function x and y
        // and calculate the full derivative
        double tangent = -xFunction.getTangent(value) / yFunction.getTangent(value);
        return new SimpleLine(tangent, getPoint(value));
    }

    public Function getyFunction(){
        return yFunction;
    }

    public Path getGraphPath(){
        return graphPath;
    }


}
