package widgets;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Line;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Callback;
import javafx.util.StringConverter;
import util.*;
import util.Point;

import java.awt.event.MouseEvent;


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

    private Label cursorPositionLabel;
    private TangentLineBox tangentLineBox;
    private Text functionPositionLabel;

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

        setUpFunctionBox(vbox); // add the function entering panel
        setUpZoomBox(vbox); // add zooming panel

        setLeft(vbox); // set left side as the v box
    }

    private void setUpRight(){
        // create an accordion
        final Accordion accordion = new Accordion();
        accordion.setPadding(new Insets(10));

        // create titles pane for graph status
        final TitledPane statusPane = new TitledPane();
        statusPane.setText("Status");
        setStatusPanel(statusPane);

        // add to the accordion
        accordion.getPanes().addAll(statusPane);
        accordion.setExpandedPane(statusPane);
        setRight(accordion);

    }

    private final void setStatusPanel(TitledPane panel){
        // create the vbox for package sub nodes
        VBox vBox = new VBox();
        vBox.setMinWidth(300);
        vBox.setPadding(new Insets(10));

        setUpInfoPanel(vBox);
        // set up the function list node
        setUpFunctionList(vBox);

        setMargin(vBox, new Insets(15));
        // add to the panel
        panel.setContent(vBox);
    }

    private final void setUpInfoPanel(VBox vBox){
        // create the current position labels
        Label cursorPositionText = new Label("Cursor Position");
        // crate the position label
        cursorPositionLabel = new Label();
        cursorPositionLabel.setTextAlignment(TextAlignment.CENTER);
        cursorPositionLabel.setId("XYLabel");
        cursorPositionLabel.setPadding(new Insets(20, 10, 20, 10));
        cursorPositionLabel.setFont(new Font("Ubuntu mono", 15));

        Separator separator = new Separator(Orientation.HORIZONTAL);
//        separator.setPadding(new Insets(10, 5, 10, 5));

        Label tangentLineText = new Label("Tangent Line");

        // create the tangent line panel
        tangentLineBox = new TangentLineBox();
        // create the another separator
        Separator separator1 = new Separator(Orientation.HORIZONTAL);
        separator1.setPadding(new Insets(10, 5, 10, 5));

        // create the function position label
        Label functionPositionText = new Label("Function Position");

        // create the function position label
        // create the label for X ana Y
        functionPositionLabel = new Text(String.format("X : %.3f%nY : %.3f", 0.0, 0.0));
        functionPositionLabel.setFont(new Font("verdana", 25));
        functionPositionLabel.setFill(Color.ORANGERED);

        // create the vbox for pack the every node in this method
        VBox mainVBox = new VBox(cursorPositionText, cursorPositionLabel, separator, tangentLineText,
                tangentLineBox, separator1, functionPositionText, functionPositionLabel);
        vBox.setPadding(new Insets(0, 10, 0, 10));
        vBox.getChildren().add(mainVBox);

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
        final Button deleteButton = new Button("Delete",
                new ImageView(
                        new Image("img/trash.png", 30, 30, true, true, true)
                ));
        deleteButton.setId("delete-button");
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

        xEquationText.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.RIGHT){
                yEquationText.requestFocus();
            }
        });

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

        yEquationText.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.RIGHT){
                limitBox1.requestFocus();
            }
        });

        limitBox1.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER){
                limitBox2.requestFocus();
            }
        });

        hBox.getChildren().addAll(new Label("t: "), limitBox1, limitBox2, colorPicker);

        // create the function mode combo box
        final ComboBox<FunctionMode> functionModeComboBox = new ComboBox<>();
        functionModeComboBox.getItems().addAll(
                FunctionMode.NORMAL,
                FunctionMode.DERIVATIVE,
                FunctionMode.INTEGRATE
        );
        functionModeComboBox.setTooltip(new Tooltip("how draw the parametric function in plane"));
        functionModeComboBox.getSelectionModel().selectFirst();

        // create the initial condition box for DEs
        final TextField x0Box = new TextField();
        final TextField y0Box = new TextField();

        x0Box.setPromptText("X0");
        y0Box.setPromptText("Y0");
        // hide the x0 and y0 box
        x0Box.setVisible(false);
        y0Box.setVisible(false);

        // create the h box for add the combo box and these initial condition boxes
        final HBox hBox1 = new HBox(functionModeComboBox, x0Box, y0Box);
        // add listener to the combo box for show anf hide the these boxes
        functionModeComboBox.valueProperty().addListener(
                new ChangeListener<FunctionMode>() {
                    @Override
                    public void changed(ObservableValue<? extends FunctionMode> observable, FunctionMode oldValue, FunctionMode newValue) {
                        if (newValue == FunctionMode.INTEGRATE){
                            // show the initial condition boxes
                            x0Box.setVisible(true);
                            y0Box.setVisible(true);
                        }
                        else{
                            x0Box.setVisible(false);
                            y0Box.setVisible(false);
                        }
                    }
                }
        );

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
                    function.setFunctionMode(functionModeComboBox.getSelectionModel().getSelectedItem());

                    if (functionModeComboBox.getSelectionModel().getSelectedItem() == FunctionMode.INTEGRATE){
                        // get the initial condition
                        if (x0Box.getText().isEmpty() || y0Box.getText().isEmpty()){
                            throw new IllegalArgumentException("Please enter the initial conditions.");
                        }
                        //set the initial condition
                        function.setStartPoint(
                                Double.parseDouble(x0Box.getText()),
                                Double.parseDouble(y0Box.getText())
                        );
                    }
                    else{
                        // check if function have x or y characters
                        if (!functionX.equals(functionX.replaceAll("[xyXY]", "")) ||
                        !functionY.equals(functionY.replaceAll("[xyXY]", ""))){
                            throw new NumberFormatException("Remove the X and Y characters from the equations.");
                        }
                    }

                    addFunction(function , true); // draw function and add to the list

                } catch (NumberFormatException e) {
                    // show the alert message
                    Alert warningBox =new Alert(Alert.AlertType.ERROR);
                    warningBox.setTitle("Function Format");
                    warningBox.setContentText(e.getMessage());
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
        gridPane.add(hBox1,  0, 4, 2, 1);
        gridPane.add(drawButton, 0, 5, 2, 1);

        // create the vbox for add to the main vbox

        final Label titleLabel = new Label("Function");
        titleLabel.setTextAlignment(TextAlignment.LEFT);

        vBox.getChildren().addAll(titleLabel, gridPane);

    }

    private final void setUpZoomBox(VBox vBox){
        // create the grid pane for this
        final GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(15);
        gridPane.setPrefWidth(350);

        // create the title label
        Label titleLabel = new Label("zoom");
        titleLabel.setTextAlignment(TextAlignment.LEFT);

        // create the zomm slider
        final Slider zoomSlider = new Slider();
        zoomSlider.setOrientation(Orientation.HORIZONTAL);
        zoomSlider.setMin(100);
        zoomSlider.setMax(500);
        zoomSlider.setPrefWidth(350);
        zoomSlider.setLabelFormatter(new StringConverter<Double>() {
            @Override
            public String toString(Double object) {
                return String.format("%.1f %%", object);
            }

            @Override
            public Double fromString(String string) {
                return null;
            }
        });

        // create the label for zoom slider value
        Label zoomLabel = new Label("100%");

        // set the zoom slider actions
        zoomSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        // zooming the canvas
                        zoomCanvas(newValue);
                        // set the zoom slider vale
                        zoomLabel.setText(String.format("%.1f %%", newValue.doubleValue()));
                    }
                }
        );

        // create the zoom in and zoom out buttons
        final Button zoomInButton = new Button("+");
        final Button zoomOutButton = new Button("-");

        zoomInButton.setId("zoomButton");
        zoomOutButton.setId("zoomButton");
        // create the setOnaction to the slider
        zoomInButton.setOnAction((event -> {
            if (zoomSlider.getValue() < zoomSlider.getMax() - 20){
                zoomSlider.setValue(zoomSlider.getValue() + 20);

            }
        }));

        zoomOutButton.setOnAction(event -> {
            if (zoomSlider.getValue() > zoomSlider.getMin() + 20){
                zoomSlider.setValue(zoomSlider.getValue() - 20);
            }
        });

        // add to the grid
        gridPane.add(zoomSlider, 0, 0, 2, 1);
        gridPane.add(zoomLabel, 2, 0);
        gridPane.add(zoomInButton, 0, 1);
        gridPane.add(zoomOutButton, 1, 1);

        gridPane.setId("zoomBox");

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

        // implement the canvas event handlers
        canvas.setOnMouseMoved((event) -> {
            // set the canvas position on cursor position text label
            Point point = gridSystem.translateToGrid(new Point(event));
            cursorPositionLabel.setText(
                    String.format("(%.3f, %.3f)", point.getX(), point.getY())
            );
            // update the x and y rules
            xRule.update(event);
            yRule.update(event);
        });

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

            // update the function position text label
            functionPositionLabel.setText(
                    FunctionFactory.getCurrentPointAsString(currentFunction, value.doubleValue()));


        }
    }

    // zooming the canvas
    private final  void zoomCanvas(Number value){
        double factor = value.doubleValue() / 100;
        // first delete all canvas system
        canvas.getChildren().clear();

        for (ParametricGraphFunction function : functionList){
            function.delete();
        }
        // redraw the grid system
        // set the new value for the grid limits
        try {
            gridSystem.setWidth(GridSystem.getBaseWidth() * factor);
            gridSystem.setHeight(GridSystem.getBasicHeight() * factor);
            gridSystem.refresh();
        } catch (Exception e) {
            e.printStackTrace();
        }
        // draw the all the  function again
        for (ParametricGraphFunction function : functionList) {
            function.draw();
        }

        // update the rules
        xRule.update();
        yRule.update();
    }

}
