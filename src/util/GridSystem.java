package util;

import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.scene.Node;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
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

    // main member
    private double stepSizeX;
    private double stepSizeY;
    // step for grid
    private double lineStepX = 1;
    private double lineStepY = 1;
    private final static double STEP_CHANGE_LIMIT = 10;

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

        if (x2 - x1 <= STEP_CHANGE_LIMIT){
            lineStepX = 0.1;
        }
        if (y2 - y1 <= STEP_CHANGE_LIMIT){
            lineStepY = 0.1;
        }
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

        if (x2 - x1 <= STEP_CHANGE_LIMIT){
            lineStepX = 0.1;
        }
        else {
            lineStepX = 1;
        }
        if (y2 - y1 <= STEP_CHANGE_LIMIT){
            lineStepY = 0.1;
        }
        else{
            lineStepY = 1;
        }

    }

    public void setY2(double y2) throws Exception{
        this.y2 = y2;
        resetStepSize();
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
        return (x - x1) * stepSizeX;
    }

    public double translateToCanvasY(double y){
        return height - (y - y1) * stepSizeY;
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
        // create drop shadow
        DropShadow dropShadow = new DropShadow(BlurType.GAUSSIAN, Color.BLUE, 5, 0.2, 0, 0);
        // draw the gird lines
        // first draw the main axis
        Line xAxis = new Line(0, translateToCanvasY(0), width, translateToCanvasY(0));
        Line yAxis = new Line(translateToCanvasX(0), 0, translateToCanvasX(0), height);

        // set the color and line configurations
        if (axisColor == null){
            xAxis.setStroke(Color.BLUE);
            yAxis.setStroke(Color.BLUE);
        }
        else{
            xAxis.setStroke(axisColor);
            yAxis.setStroke(axisColor);
        }

        // set the unique identifier for grid lines
        xAxis.setUserData("gridLine");
        yAxis.setUserData("gridLine");

        xAxis.setEffect(dropShadow);
        yAxis.setEffect(dropShadow);

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
        if (lineStepX == 1){
            for (int i = (int) x1; i <= (int) x2; i++) {
                double v = translateToCanvasX(i);
                final Line line = new Line(v, 0, v, height);
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
        else if (lineStepX == 0.1){
            for (int i = (int) (x1*10); i <= (int) (x2*10); i++){
                double v = translateToCanvasX(((double) i)/10);
                final Line line = new Line(v, 0, v, height);
                if (lineColor == null) {
                    line.setStroke(Color.GRAY);
                } else {
                    line.setStroke(lineColor);
                }
                if (i%10 == 0){
                  line.setStrokeWidth(2.0);
                }
                else{
                    line.setStrokeWidth(0.5);
                }

                line.setUserData("gridLine");
                // add to the canvas
                canvas.getChildren().add(line);
            }
        }
    }

    private void drawYLines() {
        // draw the x lines
        if (lineStepY == 1){
            for (int i = (int) y1; i < (int) y2; i++) {
                final Line line = new Line(0, translateToCanvasY(i), width, translateToCanvasY(i));
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
        else if (lineStepY == 0.1){
            for (int i = (int) (y1*10); i <= (int) (y2*10); i++){
                double v = translateToCanvasY(((double) i)/10);
                final Line line = new Line(0, v, width, v);
                if (lineColor == null) {
                    line.setStroke(Color.GRAY);
                } else {
                    line.setStroke(lineColor);
                }
                if (i%10 == 0){
                    line.setStrokeWidth(2.0);
                }
                else{
                    line.setStrokeWidth(0.5);
                }

                line.setUserData("gridLine");
                // add to the canvas
                canvas.getChildren().add(line);
            }
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

    public GridSystem copy(){
        // create nre Grid System Instant using this instant value
        GridSystem grid = null;
        try {
            grid = new GridSystem(this.x1, this.y1, this.x2, this.y2, getWidth(), getHeight());
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        return grid;

    }
}

