package util;

import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.StrokeLineCap;

public class GridSystem {

    // declare the members
    private double x1;
    private double y1;
    private double x2;
    private double y2;

    // other variables
    private double width;
    private double height;

    // main menber
    private double stepSizeX;
    private double stepSizeY;

    public final static double WIDTH = 1400;
    public final static double HEIGHT = 1000;

    // declare the canvas object for draw the grid lines
    Pane canvas = null;

    // create the data mebers for grid colors
    Color axisColor;
    Color lineColor;

    public GridSystem(double x1, double y1, double x2, double y2, double width, double height) throws InvalidArgumentException {
        if (width < 0 && height < 0) {
            throw new InvalidArgumentException(new String[]{"cannot take the negative values for width or height."});
        }
        if (x1 >= x2 || y1 >= y2) {
            throw new InvalidArgumentException(new String[]{"cannot be a same value for x and y."});
        }
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.width = width;
        this.height = height;

        // calculate the step sizes
        stepSizeX = width / (x2 - x1);
        stepSizeY = height / (y2 - y1);
    }

    public void setCanvas(Pane canvas) {
        this.canvas = canvas;
        // set the canvas width and height
        canvas.setPrefWidth(width);
        canvas.setPrefHeight(height);
        canvas.setMinWidth(width);
        canvas.setMinHeight(height);
    }

    public GridSystem(Point p1, Point p2, double width, double height) throws Exception {
        this(p1.getX(), p1.getY(), p2.getX(), p2.getY(), width, height);
    }

    // declare the setter and getter

    public double getX1() {
        return x1;
    }

    public void setX1(double x1) throws Exception {
        this.x1 = x1;
        resetStepSize();
    }

    public double getY1() {
        return y1;
    }

    public void setY1(double y1) throws Exception {
        this.y1 = y1;
        resetStepSize();
    }

    public double getX2() {
        return x2;
    }

    public void setX2(double x2) throws Exception {
        this.x2 = x2;
        resetStepSize();
    }

    public double getY2() {
        return y2;
    }

    private void resetStepSize() throws Exception {
        if (x1 >= x2 || y1 >= y2) {
            throw new Exception("cannot be change the level of x and y.");
        }
        stepSizeX = width / (x2 - x1);
        stepSizeY = height / (y2 - y1);
    }

    public void setY2(double y2) {
        this.y2 = y2;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width){
        this.width = width;
        try {
            resetStepSize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (canvas != null){
            canvas.setPrefWidth(width);
        }
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height){
        this.height = height;
        try {
            resetStepSize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (canvas != null){
            canvas.setPrefHeight(height);
        }
    }

    public double getStepSizeX() {
        return stepSizeX;
    }

    public double getStepSizeY() {
        return stepSizeY;
    }

    // declare the translate methods
    public Point translateToCanvas(Point point) {
        double x = (point.getX() - x1) * stepSizeX;
        double y = (point.getY() - y1) * stepSizeY;
        return new Point(x, height - y);
    }

    public Point translateToGrid(Point point) {
        double x = (point.getX() / stepSizeX) + x1;
        double y = (height - point.getY()) / stepSizeY + y1;
        return new Point(x, y);
    }

    public double translateToCanvasX(double x){
        return (x * stepSizeX - x1);
    }

    public double translateToCanvasY(double y){
        return height - (y * stepSizeY - y1);
    }

    public double translateToGridX(double x){
        return (x/stepSizeX + x1);
    }

    public double translateToGridY(double y){
        return ((height - y) /stepSizeY + y1);
    }

    public void setAxisColor(Color color){
        axisColor = color;
    }

    public void setLineColor(Color color){
        lineColor = color;
    }

    // method for draw the grid lines
    public void draw() {
        // draw the gird lines
        // first draw the main axis
        Line xAxis = new Line(0, y2 * stepSizeY, width, y2 * stepSizeY); // x axis
        Line yAxis = new Line(stepSizeX * Math.abs(x1), 0, stepSizeX * Math.abs(x1), height);

        // set the color and line configurations
        if (axisColor == null){
            xAxis.setStroke(Color.BLUE);
            yAxis.setStroke(Color.BLUE);
        }
        else{
            xAxis.setStroke(axisColor);
            yAxis.setStroke(axisColor);
        }

        // set the unique udentifier for grid lines
        xAxis.setUserData("gridLine");
        yAxis.setUserData("gridLine");


        xAxis.setStrokeWidth(4);
        yAxis.setStrokeWidth(4);

        xAxis.setStrokeLineCap(StrokeLineCap.ROUND);
        yAxis.setStrokeLineCap(StrokeLineCap.ROUND);
        // add to the grid system
        canvas.getChildren().addAll(xAxis, yAxis);

        // draw the x-axis grid lines
        drawXLines();
        drawYLines();
    }

    private void drawXLines() {
        // draw the x lines
        for (int i = 0; i < (x2 - x1); i++) {
            final Line line = new Line(i * stepSizeX, 0, i * stepSizeX, height);
            if (lineColor == null) {
                line.setStroke(Color.GRAY);
            } else {
                line.setStroke(lineColor);
            }
            line.setStrokeWidth(1);
            line.setUserData("gridLine");
            // add to the canvas
            canvas.getChildren().add(line);
        }
    }

    private void drawYLines() {
        // draw the x lines
        for (int i = 0; i < (y2 - y1); i++) {
            final Line line = new Line(0, i * stepSizeY, width, stepSizeY * i);
            if (lineColor == null) {
                line.setStroke(Color.GRAY);
            } else {
                line.setStroke(lineColor);
            }
            line.setStrokeWidth(1);
            line.setUserData("gridLine");
            // add to the canvas
            canvas.getChildren().add(line);
        }
    }

    public void refresh(){
        try {
            resetStepSize();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // delete the grid lines
        deleteLines();
        // draw the grid again
        draw();
    }

    private void deleteLines(){
        for (Node line  : canvas.getChildren()){
            if (line.getClass() == Line.class){
                if ((String) line.getUserData() == "gridLine"){
                    canvas.getChildren().remove(line);
                }
            }
        }
    }

    public static double getBaseWidth(){
        return WIDTH;
    }

    public static double getBasicHeight(){
        return HEIGHT;
    }
}

