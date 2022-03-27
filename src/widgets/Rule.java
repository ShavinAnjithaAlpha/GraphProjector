package widgets;

import javafx.geometry.Orientation;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import util.GridSystem;
import util.Point;


public class Rule extends Pane {

    // declare the data members
    private Orientation orientation;
    private GridSystem gridSystem;
    // increment
    double increment = 0.5;
    // declare the main line and text
    Line sliderLine;
    Text text;


    public Rule(Orientation orientation , GridSystem gridSystem){
        this.gridSystem = gridSystem;
        this.orientation = orientation;

        // build the pane width and height
        if (orientation == Orientation.HORIZONTAL){
            setPrefWidth(this.gridSystem.getWidth());
            setHeight(50);
        }
        else{
            setWidth(50);
            setPrefHeight(this.gridSystem.getHeight());
        }

        // create the rule tick lines
        createTickLines();
        // build the slider line and text
        createSliderLineAndText();

        setId("pane");
        getStylesheets().add(getClass().getResource("rule_style.css").toExternalForm());
    }

    private void createSliderLineAndText(){

        if (orientation == Orientation.HORIZONTAL){
            sliderLine = new Line(0, 0, 0, 50);
        }
        else{
            sliderLine = new Line(0, 0, 50, 0);
        }
        sliderLine.setStroke(Color.RED);
        sliderLine.setStrokeWidth(3);

        // create the text
        text = new Text(String.format("%d", 0));
        text.setX(0);
        text.setY(0);
        text.setFill(Color.WHITE);
        text.setFont(new Font("verdana", 18));
        // add to the pane
        getChildren().addAll(sliderLine, text);

    }

    private final void createTickLines(){
        if (orientation == Orientation.HORIZONTAL){
            final Line line1 = new Line(0, 0, gridSystem.getWidth(), 0);
            line1.setStroke(Color.rgb(150, 150, 150));

            getChildren().add(line1);
            // build the lines horizontally
            double stepSize = gridSystem.getStepSizeX();
            double start = gridSystem.getX1();
            double end = gridSystem.getX2();

            double x = 0;
            while (x <= (end - start)){
                // check if the main line
                if (isMainLine(x)){
                    final Line line = new Line(x * stepSize, 0, x * stepSize, 30);
                    line.setStroke(Color.rgb(100, 100, 100));
                    line.setStrokeWidth(2);
                    // add to the pane
                    getChildren().add(line);

                }
                else{
                    final Line line = new Line(x * stepSize, 0, x * stepSize, 15);
                    line.setStroke(Color.rgb(100, 100, 100));
                    // add the line
                    getChildren().add(line);
                }
                if (x%getTextStep() == 0){
                    // create the text for rule
                    final Text text = new Text(String.format("%.0f", gridSystem.translateToGridX(x * stepSize)));
                    text.setFill(Color.GRAY);
                    text.setX(x * stepSize);
                    text.setY(40);
                    getChildren().add(text);
                }
                // increment
                x += increment;
            }
        }
        else{
            final Line line1 = new Line(50, 0, 50, gridSystem.getHeight());
            line1.setStroke(Color.rgb(150, 150, 150));

            getChildren().add(line1);
            // build the lines horizontally
            double stepSize = gridSystem.getStepSizeY();
            double start = gridSystem.getY1();
            double end = gridSystem.getY2();

            double y = 0;
            while (y <= (end - start)){
                // check if the main line
                if (isMainLine(y)){
                    final Line line = new Line(20, y * stepSize , 50, y * stepSize);
                    line.setStroke(Color.rgb(100, 100, 100));
                    line.setStrokeWidth(2);
                    // add to the pane
                    getChildren().add(line);

                }
                else{
                    final Line line = new Line(35, y * stepSize ,50, y * stepSize);
                    line.setStroke(Color.rgb(100, 100, 100));
                    // add the line
                    getChildren().add(line);
                }
                if (y%getTextStep() == 0){
                    // add the text to the rule
                    final Text text = new Text(String.format("%.0f", gridSystem.translateToGridY(stepSize * y)));
                    text.setFill(Color.GRAY);
                    text.setX(5);
                    text.setY(y * stepSize);
                    getChildren().add(text);
                }
                // increment
                y += increment;
            }
        }
    }

    private boolean isMainLine(double x){
        double diff;
        if (orientation == Orientation.HORIZONTAL){
            diff = gridSystem.getX2() - gridSystem.getX1();
        }
        else{
            diff = gridSystem.getY2() - gridSystem.getY1();
        }

        if (diff > 200){
            return (x % 10 == 0);
        }
        else if (diff > 100){
            return (x % 5 == 0);
        }
        else if (diff >= 20){
            return (x % 1 == 0);
        }
        else{
            return true;
        }
    }

    public void update(MouseEvent event){
        if (event != null){
            // set the slider line and text
            if (orientation == Orientation.HORIZONTAL){
                sliderLine.setStartX(event.getX());
                sliderLine.setEndX(event.getX());

                text.setText(String.format("%.2f", gridSystem.translateToGrid(new Point(event.getX(), event.getY())).getX()));
                text.setX(event.getX() + 20);
                text.setY(20);
            }
            else{
                sliderLine.setStartY(event.getY());
                sliderLine.setEndY(event.getY());

                text.setText(String.format("%.2f", gridSystem.translateToGrid(new Point(event.getX(), event.getY())).getY()));
                text.setY(event.getY() + 30);
            }
        }

    }

    public void update(){
        // delete the all of the lines from the pane
        getChildren().clear();
        // darw the rule lines againg
        createTickLines();
        // re create the slider line and text
        createSliderLineAndText();
    }

    private final int getTextStep(){
        return 2;
    }
}
