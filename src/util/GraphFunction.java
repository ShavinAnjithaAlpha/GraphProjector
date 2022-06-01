package util;

import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

public class GraphFunction extends Function{

    // declare the data members
    Path graphPath;
    Pane canvas;
    Color color  = Color.ORANGE;
    FunctionMode functionMode = FunctionMode.NORMAL;

    Point startValue = new Point();

    private static double stepSize = 0.01;

    public GraphFunction(String function, Pane canvas){
        super(function);
        this.graphPath = new Path();
        this.graphPath.setStroke(Color.ORANGE);
        graphPath.setUserData(String.format("function|%d", getID()));

        this.canvas = canvas;
    }

    public GraphFunction(String function , GridSystem grid, Pane canvas){
        super(function , grid);
        this.graphPath = new Path();
        graphPath.setUserData(String.format("function|%d", getID()));

        this.canvas = canvas;
    }

    public void setColor(Color color){
        this.color = color;
    }

    public void draw(){
        if (functionMode == FunctionMode.NORMAL){
            this.drawNormal();
        }
        else if (functionMode == FunctionMode.DERIVATIVE){
            this.drawDerivative();
        }
        else if (functionMode == FunctionMode.INTEGRATE){
            drawIntegrate();
        }

    }

    public void drawNormal(){
        graphPath.setStroke(color);
        graphPath.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));
        // draw the graph on the canvas use the grid system
        final Point startPoint = grid.translateToCanvas(getPoint(grid.getX1()));
        final MoveTo moveTo = new MoveTo(startPoint.getX(), startPoint.getY());

        // add to the Path elemtn
        graphPath.setStrokeWidth(4);
        graphPath.getElements().add(moveTo);

        double x = grid.getX1();
        while (x <= grid.getX2()){
            // increment the x value
            x += stepSize;
            // get the new point
            Point endPoint = grid.translateToCanvas(getPoint(x));
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

    public void drawDerivative(){
        graphPath.setStroke(color);
        graphPath.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));
        // draw the graph on the canvas use the grid system
        final Point startPoint = grid.translateToCanvas(new Point(grid.getX1(), getTangent(grid.getX1())));
        final MoveTo moveTo = new MoveTo(startPoint.getX(), startPoint.getY());

        // add to the Path elemtn
        graphPath.setStrokeWidth(4);
        graphPath.getElements().add(moveTo);

        double x = grid.getX1();
        while (x <= grid.getX2()){
            // increment the x value
            x += stepSize;
            // get the new point
            Point endPoint = grid.translateToCanvas(new Point(x, getTangent(x)));
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

    public void drawIntegrate(){
        graphPath.setStroke(color);
        graphPath.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));
        graphPath.setStrokeWidth(4);
        // draw the graph on the canvas use the grid system
        Point startPoint = grid.translateToCanvas(startValue);
        graphPath.getElements().add(
                new MoveTo(startPoint.getX(), startPoint.getY())
        );

        double x = startValue.getX();
        double y0 = startValue.getY();

        while (x <= grid.getX2()){
            // increment the x value
            x += stepSize;
            // get the new point
            // calculation according to rung kutta method
            double k1 = getValueWithY(x, y0);
            double k2 = getValueWithY(x + stepSize, stepSize*k1);
            y0 = y0 + stepSize*(k1 + k2)/2;
            Point endPoint = grid.translateToCanvas(new Point(x, y0));
            // draw the line
            final LineTo lineTo = new LineTo(endPoint.getX(), endPoint.getY());
            // get the path and add
            graphPath.getElements().add(lineTo);

        }

        if (startValue != null){
            x = startValue.getX();
            y0 = startValue.getY();
        }
        else{
            x = 0;
            y0 = 0;
        }
        graphPath.getElements().add(new MoveTo(startPoint.getX(), startPoint.getY()));

        while (x >= grid.getX1()){
            // increment the x value
            x -= stepSize;
            // get the new point
            // calculation according to rung kutta method
            double k1 = getValueWithY(x, y0);
            double k2 = getValueWithY(x + stepSize, stepSize*k1);
            y0 = y0 - stepSize*(k1 + k2)/2;
            Point endPoint = grid.translateToCanvas(new Point(x, y0));
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

    public Color getColor() {
        return color;
    }

    public void refresh(){
        // remove the all the element in the path
        graphPath.getElements().clear();
        // re draw the graph
        draw();
    }

    public void delete(){
        // remove the line from the canvas
        try {
            graphPath.getElements().clear();
            canvas.getChildren().remove(graphPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Path getGraphPath(){
        return graphPath;
    }

    public void changeMode(FunctionMode mode){
        this.functionMode = mode;
    }

    public FunctionMode getFunctionMode(){
        return functionMode;
    }

    public void setStartPoint(Point point){
        startValue = point;
    }

    public double getIntegrateValue(double x){
        double startX = 0;
        if (startValue != null){
            startX = startValue.getX();
        }

        double area = integrate(startX, x);
        if (startValue != null){
            return startValue.getY() + area;
        }
        else {
            return area;
        }
    }
}
