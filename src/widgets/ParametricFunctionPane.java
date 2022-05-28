package widgets;

import com.sun.javaws.exceptions.InvalidArgumentException;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import util.*;

import javax.swing.*;

public class ParametricFunctionPane extends BorderPane {

    private GridSystem gridSystem;
    private Rule xRule;
    private Rule yRule;
    private Slider mainSlider;

    private ParametricGraphFunction currentFunction;
    private final ObservableList<ParametricGraphFunction> functionList = FXCollections.observableArrayList();

    private Pane canvas;
    private Line tangentLine;
    private Ellipse positionOval;

    public ParametricFunctionPane(GridSystem gridSystem){
        this.gridSystem = gridSystem;

        // create the canvas and setup tool boxes
        setUpCenter();
        setUpLeft();
        setUpRight();
    }

    private void setUpLeft(){
        // create the vbox add the tool boxes
        final VBox vbox = new VBox();
        vbox.setPrefWidth(350);
        vbox.setPadding(new Insets(15));

        setUpFunctionBox(vbox);

        setLeft(vbox);
    }

    private void setUpRight(){
        // create the vbox for package sub nodes
        VBox vBox = new VBox();
        vBox.setMinWidth(300);
        vBox.setPadding(new Insets(10));

        // set up the function list node
        setUpFunctionList(vBox);

        setRight(vBox);
        setMargin(vBox, new Insets(15));
    }

    private void setUpFunctionList(VBox vBox){
        // create List Node for add the parametric functions
        ListView<ParametricGraphFunction> functionListView = new ListView<>();
        functionListView.setItems(functionList);
        functionListView.prefWidthProperty().bind(vBox.prefWidthProperty().subtract(10));
        functionListView.setMinWidth(250);
        functionListView.setPlaceholder(new Label("Nothing"));
        functionListView.setId("functionBox");

        // set the cell factory
        functionListView.setCellFactory(new Callback<ListView<ParametricGraphFunction>, ListCell<ParametricGraphFunction>>() {
            @Override
            public ListCell<ParametricGraphFunction> call(ListView<ParametricGraphFunction> param) {
                return new ParametricFunctionCell();
            }
        });

        // declare the another method on the function list view
        functionListView.setOnMouseClicked(event -> {
            // get the selected function and set as the current function
            if (functionListView.getSelectionModel().getSelectedItem() != null){
                currentFunction = functionListView.getSelectionModel().getSelectedItem();
                // change main slider limits
                mainSlider.setMin(currentFunction.getT1());
                mainSlider.setMax(currentFunction.getT2());
            }
        });

        functionListView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.DELETE){
                if (functionListView.getSelectionModel().getSelectedItem() != null){
                    deleteFunction(functionListView.getSelectionModel().getSelectedItem());
                }
            }
        });


        // label for title
        final Label titleLabel = new Label("Parametric Equations");
        // delete button for function
        final Button deleteButton = new Button("Delete");
        deleteButton.setOnMouseClicked((event -> {
            // remove from the selected function from the list
            if (functionListView.getSelectionModel().getSelectedItem() != null){
                deleteFunction(functionListView.getSelectionModel().getSelectedItem());
            }
        }));

        // create the grid for add border
        final GridPane gridPane = new GridPane();
        gridPane.setVgap(15);
        gridPane.setId("functionListGrid");
        gridPane.prefWidthProperty().bind(vBox.prefWidthProperty().subtract(15));
        gridPane.add(functionListView, 0, 0, 2, 1);
        gridPane.add(deleteButton, 0, 1);

        vBox.getChildren().addAll(titleLabel, gridPane);
    }

    private void setUpFunctionBox(VBox vBox){
        // create the two text field for enter the parametric equations as the text
        final TextArea xEquationText = new TextArea("");
        final TextArea yEquationText = new TextArea("");

        for (TextArea textArea : new TextArea[] {xEquationText, yEquationText}){
            textArea.prefWidthProperty().bind(vBox.prefWidthProperty().subtract(20));
            textArea.setPrefHeight(100);
            textArea.setId("parametric-text-area");
        }

        xEquationText.setPromptText("Equation for X");
        yEquationText.setPromptText("Equation for Y");

        // create the color picker for this
        ColorPicker colorPicker = new ColorPicker(Color.rgb(240, 70, 5));
        colorPicker.setPrefWidth(50);

        HBox hBox = new HBox();
        hBox.setSpacing(10);
        TextField limitBox1 = new TextField();
        TextField limitBox2 = new TextField();

        limitBox1.setPrefWidth(80);
        limitBox2.setPrefWidth(80);

        hBox.getChildren().addAll(new Label("t: "), limitBox1, limitBox2, colorPicker);


        // draw button for plot
        Button drawButton = new Button("Plot");
        drawButton.prefWidthProperty().bind(vBox.prefWidthProperty());
        drawButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    // draw the function
                    String functionX = xEquationText.getText();
                    String functionY = yEquationText.getText();

                    double t1, t2;
                    t1 = Double.parseDouble(limitBox1.getText());
                    t2 = Double.parseDouble(limitBox2.getText());

                    // create the Parametric Function
                    ParametricGraphFunction function
                             = new ParametricGraphFunction(functionX, functionY,
                                                            gridSystem, canvas, t1, t2);
                    function.setColor(colorPicker.getValue()); // set the function path color
                    addFunction(function , true); // draw function and add to the list

                } catch (NumberFormatException e) {
                    // show the alert message
                    Alert warningBox =new Alert(Alert.AlertType.ERROR);
                    warningBox.setTitle("Function Format");
                    warningBox.setContentText("Please Enter the valid function for drawing!!!");
                    warningBox.show();
                } catch (Exception e) {
                    // show the alert message
                    Alert warningBox =new Alert(Alert.AlertType.ERROR);
                    warningBox.setTitle("Function Format");
                    warningBox.setContentText(e.getMessage());
                    warningBox.show();
                }
            }
        });

        //grid box for packing this items
        GridPane gridPane = new GridPane();
        gridPane.setPadding(new Insets(10));
        gridPane.setVgap(15);
        gridPane.setHgap(15);
        gridPane.setId("functionVBox");

        Label paraText = new Label("Enter equations using symbol 't' \n" +
                "as the X and Y Equations Separately");
        paraText.setId("paraText");

        gridPane.add(paraText, 0, 0, 2, 1);
        gridPane.add(new Label("X"), 0, 1);
        gridPane.add(new Label("Y"), 0, 2);
        gridPane.add(xEquationText, 1, 1);
        gridPane.add(yEquationText, 1, 2);
        gridPane.add(hBox, 0, 3, 2, 1);
        gridPane.add(drawButton, 0, 4, 2, 1);

        // create the vbox for add to the main vbox

        final Label titleLabel = new Label("Function");
        titleLabel.setTextAlignment(TextAlignment.LEFT);

        vBox.getChildren().addAll(titleLabel, gridPane);

    }

    private final void setUpCenter(){

        // create the vbox for pack the slider and canvas
        final VBox vBox = new VBox(10);
        // create the grid for pack the rukes and canvas
        final GridPane gridPane = new GridPane();
        gridPane.setHgap(0);
        gridPane.setVgap(0);
        gridPane.setPadding(new Insets(0));
        // create the main canvas widget for all drawing capabilities
        canvas = new Pane();
        canvas.setId("canvas");
        //set uo the grid system
        setUpGrid();

        // create the x and y rules
        xRule = new Rule(Orientation.HORIZONTAL, gridSystem);
        yRule = new Rule(Orientation.VERTICAL, gridSystem);

        gridPane.add(yRule, 0, 0, 1, 2);
        gridPane.add(canvas, 1, 0);
        gridPane.add(xRule, 1, 1);
        // create the scroll area for set up the canvas
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.prefWidthProperty().bind(prefWidthProperty());
//        scrollPane.setMaxSize(1400, 1000);
        scrollPane.setContent(gridPane);
        //set the scroll bar policy
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //set the border pane center

        HBox sliderBox = setUpSlider();
        vBox.getChildren().addAll(scrollPane, sliderBox);
        // set the vbox as the center fo the border pane
        setCenter(vBox);

    }

    private final HBox setUpSlider(){
        // create the hox for pack the widgets
        final HBox hbox = new HBox(10);
        // create the main slider of the window
        mainSlider = new Slider(0 , 50, 0);
        mainSlider.prefWidthProperty().bind(hbox.prefWidthProperty().subtract(150));
        mainSlider.setOrientation(Orientation.HORIZONTAL);
        mainSlider.setMinorTickCount(1);
        mainSlider.setDisable(true);
        // create the slider value box
        final Label sliderLabel = new Label(String.format("X : %.2f", gridSystem.getX1()));

//        // add the slider value property listener
        mainSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        // set the label text
//                        setSliderText(sliderLabel , newValue);
                        try {
                            updateTangentLine(newValue);
                        } catch (NullPointerException e) {
                            System.out.println();
                        }
                    }
                }
        );

        // add the slider and label
        hbox.getChildren().addAll(mainSlider, sliderLabel);
        hbox.setPadding(new Insets(10, 5, 25, 5));
        hbox.setPrefWidth(gridSystem.getWidth() - 50);

        return hbox;
    }

    private void setUpGrid(){

        gridSystem.setCanvas(canvas); // set the canvas
        gridSystem.setAxisColor(Color.BLUE);
        gridSystem.setLineColor(Color.rgb(100, 100, 100, 0.7));
        // draw the grid in the canvas
        gridSystem.draw();
    }

    final private void addFunction(ParametricGraphFunction function, boolean isDraw){
        // add the new function to function list
        currentFunction = function;
        // set the slider limits
        mainSlider.setMin(currentFunction.getT1());
        mainSlider.setMax(currentFunction.getT2());
        // add to the list
        functionList.add(function);
        if (isDraw){
            currentFunction.draw();
            // activate the main slider
            mainSlider.setDisable(false);
        }
    }

    final private void deleteFunction(ParametricGraphFunction function){
        if (function != null){
            if (function == currentFunction){
                currentFunction = null;
            }

            // get the path
            Path graphPath = function.getGraphPath();
            graphPath.getElements().clear();
            // remove from the canvas
            canvas.getChildren().remove(graphPath);
            // remove from the list
            functionList.remove(function);
        }
    }

    final private void updateTangentLine(Number value){
        if (currentFunction != null){
            double t = value.doubleValue();
            // get the current position from the function
            Point currentPosition = currentFunction.getPoint(t);
            // get line
            SimpleLine tangentLine_ = currentFunction.getTangentLine(t);
            Point[] points = tangentLine_.getPoints(currentPosition);

            // convert all the points to canvas coordinates
            Point pStart = gridSystem.translateToCanvas(points[0]);
            Point pEnd = gridSystem.translateToCanvas(points[1]);
            Point pCenter = gridSystem.translateToCanvas(currentPosition);

            if (tangentLine == null){
                // create the new one
                tangentLine = new Line(pStart.getX(), pStart.getY(), pEnd.getX(), pEnd.getY());
                tangentLine.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));
                tangentLine.setStroke(Color.ORANGERED);
                tangentLine.setStrokeWidth(2);
                tangentLine.setStrokeLineCap(StrokeLineCap.ROUND);
                tangentLine.setSmooth(true);

                // create the position oval
                positionOval = new Ellipse(pCenter.getX(), pCenter.getY(), 8, 8);
                positionOval.setFill(Color.ORANGERED);
                positionOval.setStroke(Color.RED);
                positionOval.setStrokeWidth(3);
                positionOval.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));

                canvas.getChildren().addAll(tangentLine, positionOval);
            }
            else{
                tangentLine.setStartX(pStart.getX());
                tangentLine.setStartY(pStart.getY());
                tangentLine.setEndX(pEnd.getX());
                tangentLine.setEndY(pEnd.getY());

                positionOval.setCenterX(pCenter.getX());
                positionOval.setCenterY(pCenter.getY());
            }

            if (!(canvas.getChildren().contains(tangentLine)) && !(canvas.getChildren().contains(positionOval))){
                canvas.getChildren().addAll(tangentLine, positionOval);
            }

        }
    }
}
