package util;

import javafx.application.Platform;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

import javax.script.ScriptException;

public class ParametricGraphFunction {
    // functions
    private Function xFunction;
    private Function yFunction;
    private FunctionMode functionMode = FunctionMode.NORMAL;
    // limits
    private double t1;
    private double t2;
    private Point startPoint = new Point(-1, 6);
    // declare the data members
    private Path graphPath;
    private Pane canvas;
    private GridSystem grid;
    private Color color  = Color.ORANGE;

    private static int counter = 0;

    private static double stepSize = 0.01;

    public ParametricGraphFunction(String xFunction, String yFunction, GridSystem grid,Pane canvas, double t1, double t2){
        this.xFunction = new Function(xFunction.replaceAll("[tT]", "x"));
        this.yFunction = new Function(yFunction.replaceAll("[tT]", "x"));
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
        return color;
    }

    public double getT1() {
        return t1;
    }

    public double getT2(){
        return t2;
    }

    public void draw(){
        switch (functionMode){
            case NORMAL:{
                drawNormal();
                break;
            }
            case DERIVATIVE:{
                drawDerivative();
                break;
            }
            case INTEGRATE:{
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        drawIntegrate();
                    }
                });
                break;
            }
        }
    }

    private final void drawNormal(){
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

    private final void drawDerivative(){
        graphPath.setStroke(color);
        graphPath.setFill(null);
        graphPath.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));
        // draw the graph on the canvas use the grid system
        final Point startPoint = grid.translateToCanvas(
                new Point(
                        xFunction.getTangent(t1),
                        yFunction.getTangent(t1)
                ));
        final MoveTo moveTo = new MoveTo(startPoint.getX(), startPoint.getY());

        // add to the Path element
        graphPath.setStrokeWidth(4);
        graphPath.getElements().add(moveTo);

        double t = t1;
        while (t <= t2){
            // increment the x value
            t += stepSize;
            // get the new point
            Point endPoint = grid.translateToCanvas(
                    new Point(
                            xFunction.getTangent(t),
                            yFunction.getTangent(t)
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

    private final void drawIntegrate(){
        graphPath.setStroke(color);
        graphPath.setFill(null);
        graphPath.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));
        graphPath.setStrokeWidth(4);

        Point startPoint_ = grid.translateToCanvas(getStartPoint());
        graphPath.getElements().add(
                new MoveTo(startPoint_.getX(), startPoint_.getY())
        );

        double x0 = getStartPoint().getX();
        double y0 = getStartPoint().getY();
        double t = 0;

        while (x0 <= grid.getX2() && x0 >= grid.getX1() && y0 <= grid.getY2() && y0 >= grid.getY1()){
            t += stepSize;
            // get he coordinates using algorithm
            double[] xy = getNextValuesForDEs(t, x0, y0);
            x0 = xy[0];
            y0 = xy[1];
            // translate to the canvas
            Point endPoint = grid.translateToCanvas(new Point(x0, y0));
            // draw the line
            final LineTo lineTo = new LineTo(endPoint.getX(), endPoint.getY());
            // get the path and add
            graphPath.getElements().add(lineTo);

            if (t > 100){
                break;
            }
        }

        // add to the canvas
        if (!(canvas.getChildren().contains(graphPath))){
            canvas.getChildren().add(graphPath);

        }

    }

    private double[] getNextValuesForDEs(double t, double x0, double y0){
        // implement the numerical method for system of diff equations
        double[] xy = new double[2];

        double m1 = getXFunctionValue(t, x0, y0);
        double k1 = getYFunctionValue(t, x0, y0);

        double m2 = getXFunctionValue(t + 0.5*stepSize, x0 + 0.5*m1*stepSize, y0+0.5*k1*stepSize);
        double k2 = getYFunctionValue(t + 0.5*stepSize,x0 + 0.5*m1*stepSize, y0 + 0.5*k1*stepSize);

        double m3 = getXFunctionValue(t + 0.5*stepSize, x0 + 0.5*m2*stepSize, y0+0.5*k2*stepSize);
        double k3 = getYFunctionValue(t + 0.5*stepSize,x0 + 0.5*m2*stepSize, y0 + 0.5*k2*stepSize);

        double m4 = getXFunctionValue(t + 0.5*stepSize, x0 + 0.5*m3*stepSize, y0+0.5*k3*stepSize);
        double k4 = getYFunctionValue(t + 0.5*stepSize,x0 + 0.5*m3*stepSize, y0 + 0.5*k3*stepSize);

        xy[0] = x0 + stepSize/6*(m1 + 2*m2 + 2*m3 + m4);
        xy[1] = y0 + stepSize/6*(k1 + 2*k2 + 2*k3 + k4);

        return xy;
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

    public void setFunctionMode(FunctionMode mode){
        this.functionMode = mode;
    }

    public FunctionMode getFunctionMode(){
        return functionMode;
    }

    public void setStartPoint(Point point){
        startPoint = point;
    }

    public void setStartPoint(double x, double y){
        startPoint = new Point(x, y);
    }

    public Point getStartPoint(){
        return startPoint;
    }

    public double getXFunctionValue(double t, double x, double y){
        // first replace the x with value x
        String replcaeText = xFunction.getFunction().replaceAll("[xX]",
                String.format("(%s)", String.valueOf(x)));
        // then replace the y values
        replcaeText = replcaeText.replaceAll("[yY]",
                String.format("(%s)", String.valueOf(y)));
        replcaeText = replcaeText.replaceAll("[tT]", "x");

        try{
            return FunctionValue.getValue(replcaeText, t);
        }
        catch (ScriptException ex){
            ex.printStackTrace();
            return 0;
        }
    }

    public double getYFunctionValue(double t, double x, double y){
        // first replace the x with value x
        String replcaeText = yFunction.getFunction().replaceAll("[xX]",
                String.format("(%s)", String.valueOf(x)));
        // then replace the y values
        replcaeText = replcaeText.replaceAll("[yY]",
                String.format("(%s)", String.valueOf(y)));
        replcaeText = replcaeText.replaceAll("[tT]", "x");

        try{
            return FunctionValue.getValue(replcaeText, t);
        }
        catch (ScriptException ex){
            ex.printStackTrace();
            return 0;
        }
    }

    // delete the graph nodes from canvas
    public void delete(){
        // remove the line from the canvas
        try {
            graphPath.getElements().clear();
            canvas.getChildren().remove(graphPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
