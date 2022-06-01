import com.sun.javafx.scene.control.skin.TitledPaneSkin;
import com.sun.javaws.exceptions.InvalidArgumentException;
import dialog.GridLimitDialog;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.StringConverter;
import util.*;
import widgets.FunctionCell;
import widgets.ParametricFunctionPane;
import widgets.Rule;
import widgets.TangentLineBox;

import javax.swing.border.TitledBorder;
import java.util.Optional;


public class GraphProjector extends Application {

    // static data members
    private static final double posLineLength = 30;

    private  Pane canvas;
    private ScrollPane scrollPane;
    private Slider mainSlider;
    private TextArea mainFunctionBox;
    private Label XYLabel;
    private TangentLineBox tangentLineBox;
    private Text functionPositionText;

    //rules of the canvas
    Rule xRule;
    Rule yRule;
    // declare the main paint tools
    private Line xLine;
    private Line yLine;
    private Line tangentLine;
    private Ellipse functionPositionOval;
    private Rectangle XYBox;
    private Path integrateArea;
    // grid system
    private GridSystem gridSystem;
    // declare the function list
    private ObservableList<GraphFunction> functionList;
    private GraphFunction currentFunction = null;

    private String tangentLineMode = "tangent line";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // create the function list
        functionList = FXCollections.observableArrayList();

        // create the vbox for ad the menu bar
        final VBox vBox = new VBox();
        vBox.setSpacing(0);
        vBox.setPadding(new Insets(0));
//        vBox.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        // create the tab pane
        final TabPane tabPane = new TabPane();
        // create the main tab bar for pane
        final Tab mainTab = new Tab("Plane Equations", setUpMainTab());
        final Tab parametricTab = new Tab("Parametric Equations",
                    new ParametricFunctionPane(gridSystem.copy())); // use the copy of existing grid system
        // add all tab to the tab pane
        tabPane.getTabs().addAll(mainTab, parametricTab);
        tabPane.setFocusTraversable(false);

        // add the all menu bar and tab pane to the vbox
        vBox.getChildren().addAll(setUpMenu(), tabPane);

        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();
        // create the scene and add the border pane
        final Scene scene = new Scene(vBox, screenWidth, screenHeight ,Color.rgb(50, 50, 50));
        scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());
        primaryStage.setTitle("Graph Projector v0.01");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(false);
        primaryStage.setX(0);
        primaryStage.setY(0);

        primaryStage.show();


    }

    private BorderPane setUpMainTab(){
        // create the main layout widget of the window
        final BorderPane borderPane = new BorderPane();

        // set the display panel
        setUpRight(borderPane);
        // set up the center
        setUpCenter(borderPane);
        // set up the tool box
        setUpToolBox(borderPane);

        return borderPane;
    }

    private final MenuBar setUpMenu(){
        // create the menu bar
        final MenuBar menuBar = new MenuBar();
        // create the menu for menu bar
        final Menu optionMenu = new Menu("Option");
        createOptionMenu(optionMenu);


        // add to the menu bar
        menuBar.getMenus().addAll(optionMenu);
        return menuBar;

    }

    private final void createOptionMenu(Menu optionMenu){
        // create the tangent line change mode menu item
        Menu tangentModeMenu = new Menu("tangent line mode");
        // create the radio button for this
        ToggleGroup toggleGroup = new ToggleGroup();

        for (String text : new String[] {"tangent line", "normal line", "tangent line with circle"}){
            RadioMenuItem radioMenuItem = new RadioMenuItem(text);
            radioMenuItem.setUserData(text);
            radioMenuItem.setOnAction((event -> {
                // action for change the tangent line mode
                tangentLineMode = (String) toggleGroup.getSelectedToggle().getUserData();
            }));
            toggleGroup.getToggles().add(radioMenuItem);
            // add to the mode menu
            tangentModeMenu.getItems().add(radioMenuItem);
        }
        toggleGroup.getToggles().iterator().next().setSelected(true);

        optionMenu.getItems().add(tangentModeMenu);
    }

    private void setUpRight(BorderPane borderPane){

        // create the accordion for pack the tiles pane
        final Accordion accordion = new Accordion();

        // create the title pane for add panels
        final TitledPane statusTiledPane = new TitledPane();
        statusTiledPane.setText("Status");
        setUpRightStatusPanel(statusTiledPane);

        final TitledPane sliderPane = new TitledPane();
        sliderPane.setText("Function Sliders");
        setUpRightSliderPanel(sliderPane);

        // add titles pane to accordion
        accordion.getPanes().addAll(statusTiledPane, sliderPane);
        accordion.setExpandedPane(statusTiledPane);
        accordion.setPadding(new Insets(10));
        // add to the border pane
        borderPane.setRight(accordion);
        BorderPane.setMargin(accordion, new Insets(10));
    }

    private void setUpRightStatusPanel(TitledPane panel){
        // create the vbox for pack the all of items
        final VBox vBox = new VBox(5);
        vBox.setPadding(new Insets(5)); // set the padding

        Label label1 = new Label("Cursor Position");
        label1.setTextAlignment(TextAlignment.LEFT);
        // create the XY label for display the current position in the screen
        XYLabel = new Label("(0, 0)");
        XYLabel.setMinWidth(150);
        XYLabel.setTextAlignment(TextAlignment.CENTER);
        XYLabel.setPadding(new Insets(2, 20, 2, 20));
        XYLabel.setId("XYLabel");
        XYLabel.setFont(new Font("verdana", 14));

        // create the horizontal separator
        final Separator separator1 = new Separator();
        separator1.setPadding(new Insets(10, 0, 10, 0));
        separator1.setOrientation(Orientation.HORIZONTAL);

        final Label label2 = new Label("Tangent Line");
        label2.setTextAlignment(TextAlignment.LEFT);
        // create the tangent line node object
        tangentLineBox = new TangentLineBox();

        // create the another separator
        final Separator separator2 = new Separator();
        // create the label
        final Label label3 = new Label("function position");
        label3.setTextAlignment(TextAlignment.LEFT);

        // create the label for X ana Y
        functionPositionText = new Text(String.format("X : %.3f%nY : %.3f", 0.0, 0.0));
        functionPositionText.setFont(new Font("verdana", 25));
        functionPositionText.setFill(Color.ORANGE);

        // add to the vbox
        vBox.getChildren().addAll(label1, XYLabel, separator1, label2, tangentLineBox, separator2, label3,
                functionPositionText);


        setUpFunctionList(vBox);
        // set the border pane left as the vbox
        panel.setContent(vBox);
        panel.setPadding(new Insets(5));
    }

    private void setUpRightSliderPanel(TitledPane panel){

        // create the grid pane for add the sliders
        final GridPane gridPane = new GridPane();
        gridPane.setVgap(10);

        panel.setContent(gridPane);
        panel.setPadding(new Insets(5));
    }

    private final void setUpCenter(BorderPane borderPane){

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
        scrollPane = new ScrollPane();
        scrollPane.prefWidthProperty().bind(borderPane.prefWidthProperty());
//        scrollPane.setMaxSize(1400, 1000);
        scrollPane.setContent(gridPane);
        //set the scroll bar policy
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //set the border pane center

//        Popup canvasPopup = createCanvasPopup();

        canvas.setOnMouseMoved(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                drawCurrentLine(event);
                // update th rules
                xRule.update(event);
                yRule.update(event);
            }
        });

        canvas.setOnMousePressed(event -> {
            Point gridPoint = gridSystem.translateToGrid(new Point(event.getX(), event.getY()));
            // show the canvas popup
            Point2D point = canvas.localToScreen(event.getX(), event.getY());
//            ((Text) canvasPopup.getContent().get(1)).setText(String.format("X : %.2f\nY : %.2f",
//                                                            gridPoint.getX() , gridPoint.getY()));
//            canvasPopup.show(canvas, point.getX(), point.getY());
        });

        HBox sliderBox = setUpSlider();
        vBox.getChildren().addAll(scrollPane, sliderBox);
        // set the vbox as the center fo the border pane
        borderPane.setCenter(vBox);

    }

    // setup the slider
    private final HBox setUpSlider(){
        // create the hox for pack the widgets
        final HBox hbox = new HBox(10);
        // create the main slider of the window
        mainSlider = new Slider(gridSystem.getX1() , gridSystem.getX2() , gridSystem.getX1());
        mainSlider.prefWidthProperty().bind(hbox.prefWidthProperty().subtract(150));
        mainSlider.setOrientation(Orientation.HORIZONTAL);
        mainSlider.setMinorTickCount(1);
        mainSlider.setDisable(true);
        // create the slider value box
        final Label sliderLabel = new Label(String.format("X : %.2f", gridSystem.getX1()));

        // add the slider value property listener
        mainSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        // set the label text
                        setSliderText(sliderLabel , newValue);
                        try {
                            updateTangentLine(newValue);
                        } catch (NullPointerException e) {
                            System.out.println(e.getMessage());
                        }
                        // scroll the scroll area
                        if (gridSystem.translateToCanvasX(newValue.doubleValue()) > scrollPane.getWidth()) {
                            scrollPane.setHvalue(gridSystem.translateToCanvasX(newValue.doubleValue()));
                        }
                        else if (gridSystem.translateToCanvasX(newValue.doubleValue()) < scrollPane.getHvalue()){
                            scrollPane.setHvalue(0);
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

    private final void setUpBottom(BorderPane borderPane){
        // create the hox for pack the widgets
        final HBox hbox = new HBox(10);
        // create the main slider of the window
        mainSlider = new Slider(gridSystem.getX1() , gridSystem.getX2() , gridSystem.getX1());
        mainSlider.prefWidthProperty().bind(hbox.prefWidthProperty().subtract(150));
        mainSlider.setOrientation(Orientation.HORIZONTAL);
        mainSlider.setMinorTickCount(1);

        // create the slider value box
        final Label sliderLabel = new Label(String.format("X : %.2f", gridSystem.getX1()));
        // add the slider value property linstner
        mainSlider.valueProperty().addListener(
                new ChangeListener<Number>() {
                    @Override
                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                        // set the label text
                        setSliderText(sliderLabel , newValue);
                        try {
                            updateTangentLine(newValue);
                        } catch (NullPointerException e) {
                            e.printStackTrace();
                        }
                    }
                }
        );

        // add the slider and label
        hbox.getChildren().addAll(mainSlider, sliderLabel);
        hbox.setPadding(new Insets(10, 5, 25, 5));
        hbox.setPrefWidth(1400);
        // add to the main layout
        borderPane.setBottom(hbox);
    }

    private final void setUpToolBox(BorderPane borderPane){
        // create the mein vbox for this
        final VBox vBox = new VBox(20);
        vBox.setPrefWidth(400);
        // set the paddiing
        vBox.setPadding(new Insets(10));

        // call to the other builders
        setUpFunctionBox(vBox);
        // set up the zomm box
        setUpZoomBox(vBox);
        // create the grid limits change button
        final Button changeLimitsButton = new Button("Change Limits");
        changeLimitsButton.setOnAction((event -> {
            // call to the grid change dialog box
            final GridLimitDialog gridLimitDialog = new GridLimitDialog(gridSystem);
            Optional<double[]> array = gridLimitDialog.showAndWait();

            if (array.isPresent()){
                // call to the update method
                updateCanvas();
                // chnage the main slider limits
                mainSlider.setMin(gridSystem.getX1());
                mainSlider.setMax(gridSystem.getX2());
            }
        }));
        vBox.getChildren().add(changeLimitsButton);

        // create the integrate tool box
        setUpIntegrateBox(vBox);

        // set the left side
        borderPane.setLeft(vBox);
    }

    private void setUpFunctionList(VBox vBox){
        // create the title label
        final Label titleLabel = new Label("Function List");
        titleLabel.setTextAlignment(TextAlignment.LEFT);
        // grid pane for pack the buttons and lit view
        final GridPane gridPane = new GridPane();
        gridPane.setId("functionListGrid");
        gridPane.setVgap(10);
        gridPane.setPadding(new Insets(15));

        final ListView<GraphFunction> graphFunctionListView = new ListView<>(functionList);
        graphFunctionListView.prefWidthProperty().bind(gridPane.prefWidthProperty().subtract(10));
        graphFunctionListView.setPlaceholder(new Label("Nothing"));
        graphFunctionListView.setMinWidth(250);
        graphFunctionListView.setId("functionBox");
//        graphFunctionListView.setMinWidth(250);
        graphFunctionListView.setCellFactory(new Callback<ListView<GraphFunction>, ListCell<GraphFunction>>() {
            @Override
            public ListCell<GraphFunction> call(ListView<GraphFunction> param) {
                return new FunctionCell();
            }
        });
        graphFunctionListView.setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // set the current function
                if (graphFunctionListView.getSelectionModel().getSelectedItem() != null){
                    currentFunction = graphFunctionListView.getSelectionModel().getSelectedItem();
                }

            }
        });

        graphFunctionListView.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.DELETE) {
                    if (graphFunctionListView.getSelectionModel().getSelectedItem() != null){
                        deleteFunction(graphFunctionListView.getSelectionModel().getSelectedItem());
                    }
                }
            }
        });

        // create the delete button
        final Button deleteButton = new Button("",
                new ImageView(
                        new Image("img/trash.png", 30, 30, true, true, true)
                ));
        deleteButton.setId("delete-button");
        // set the action to the delete button
        deleteButton.setOnAction((event -> {
            // call to delete the function from the list view
            if (graphFunctionListView.getSelectionModel().getSelectedItem() != null){
                deleteFunction(graphFunctionListView.getSelectionModel().getSelectedItem());
            }
        }));

        // delete all button
        final Button deleteAllButton = new Button("Delete All",
                new ImageView(
                        new Image("img/trash.png", 30, 30, true, true, true)
                ));
        deleteAllButton.setId("delete-button");
        // set the action to the delete button
        deleteAllButton.setOnAction((event -> {
           // delete all the function from the list view
            for (GraphFunction function : functionList) {
                deleteFunction(function);
            }
            mainSlider.setDisable(true);

        }));
        // add to the grid pane
        gridPane.add(graphFunctionListView, 0, 0, 2, 1);
        gridPane.add(deleteButton, 0, 1);
        gridPane.add(deleteAllButton, 1, 1);

        vBox.getChildren().addAll(titleLabel , gridPane);
    }

    private final void setUpFunctionBox(VBox vBox){
        // create the group box
        // create the line edit and add button for this
        mainFunctionBox = new TextArea();
        mainFunctionBox.prefWidthProperty().bind(vBox.prefWidthProperty().subtract(30));
        mainFunctionBox.setPrefHeight(80);
        mainFunctionBox.setPromptText("Enter a function");

        // create the color chooser
        final ColorPicker colorPicker = new ColorPicker(Color.ORANGE);
        // create the h box
        final HBox hBox = new HBox(mainFunctionBox, colorPicker);
        hBox.setSpacing(10);

        // create the combo box for select the graph function mode
        final ComboBox<FunctionMode> modeComboBox = new ComboBox<>();
        modeComboBox.getItems().addAll(
                FunctionMode.NORMAL,
                FunctionMode.DERIVATIVE,
                FunctionMode.INTEGRATE);
        modeComboBox.setTooltip(new Tooltip("How function plot in the canvas"));
        modeComboBox.setPlaceholder(new Label("Function Mode"));
        modeComboBox.getSelectionModel().selectFirst();

        // tex fields for enter the initial condition for integrating function
        final TextField initXField = new TextField();
        initXField.setMaxWidth(60);
        initXField.setPromptText("X0");

        final TextField initYField = new TextField();
        initYField.setMaxWidth(60);
        initYField.setPromptText("Y0");

        initXField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER){
                initYField.requestFocus();
            }
        });

        // initially hide the tex field for open in nesassary situations
        initXField.setVisible(false);
        initYField.setVisible(false);
        // listener for changing text of function nox
        mainFunctionBox.setOnKeyPressed(event -> {
            // get the string of the function box
            String text = mainFunctionBox.getText() + event.getText();
            if (event.getCode() == KeyCode.BACK_SPACE){
                text = text.substring(0, text.length() - 1);
            }

            if (text.indexOf('y') >= 0){
                initXField.setVisible(true);
                initYField.setVisible(true);
                return;
            }
            if (modeComboBox.getSelectionModel().getSelectedItem() == FunctionMode.INTEGRATE){
                return;
            }
            initXField.setVisible(false);
            initYField.setVisible(false);
            return;
        });

        modeComboBox.valueProperty().addListener(new ChangeListener<FunctionMode>() {
            @Override
            public void changed(ObservableValue<? extends FunctionMode> observable, FunctionMode oldValue, FunctionMode newValue) {
                if (newValue == FunctionMode.INTEGRATE){
                    initXField.setVisible(true);
                    initYField.setVisible(true);
                }
                else{
                    initXField.setVisible(false);
                    initYField.setVisible(false);
                }
            }
        });

        // create h box for add these fields
        final HBox hBox1 = new HBox(modeComboBox, initXField, initYField);
        hBox1.setPadding(new Insets(5));
        hBox1.setSpacing(10);


        // draw button
        final Button drawButton = new Button("Plot");
        drawButton.prefWidthProperty().bind(vBox.prefWidthProperty().subtract(30));
        drawButton.setTooltip(new Tooltip("Plot the function"));
        drawButton.setPadding(new Insets(10, 10, 10, 10));
        drawButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    // draw the function
                    String functionText = mainFunctionBox.getText();
                    GraphFunction function = new GraphFunction(functionText , gridSystem, canvas);
                    function.setColor(colorPicker.getValue());
                    function.changeMode(modeComboBox.getSelectionModel().getSelectedItem());

                    // check if init boxes are visible
                    if (initXField.isVisible() && initYField.isVisible()){
                        if (!initXField.getText().isEmpty() && !initYField.getText().isEmpty()){
                            function.setStartPoint(
                                    new Point(
                                            Double.parseDouble(initXField.getText()),
                                            Double.parseDouble(initYField.getText())
                                    )
                            );
                        }
                    }

                    addFunction(function , true);
                } catch (NumberFormatException e) {
                    // show the alert message
                    Alert warningBox =new Alert(Alert.AlertType.ERROR);
                    warningBox.setTitle("Function Format");
                    warningBox.setContentText("Please Enter the valid function for drawing!!!");
                    warningBox.show();
                }
            }
        });

        // create the new another vbox
        VBox functionBox = new VBox(10);
        functionBox.setId("functionVBox");
        functionBox.getChildren().addAll(hBox , drawButton, hBox1);

        final Label titleLabel = new Label("Function");
        titleLabel.setTextAlignment(TextAlignment.LEFT);

        vBox.getChildren().addAll(titleLabel, functionBox);
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

    private final void setUpIntegrateBox(VBox vBox){

        TextField beginXField = new TextField();
        beginXField.setPromptText("start X");
        TextField endXField = new TextField();
        endXField.setPromptText("end X");
        beginXField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER){
                    endXField.requestFocus();
                }
            }
        });

        // create the progress indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(40, 40);
        progressIndicator.setVisible(false);

        Button integrateButton = new Button("Integrate"); // button for fire the integrate
        Button deleteButton = new Button("delete", new ImageView(
                new Image("img/trash.png", 30, 30, true, true, true)
        ));
        deleteButton.setId("delete-button");
        deleteButton.setPadding(new Insets(5));
        deleteButton.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);

        Label integrateLabel = new Label(); // label for display the integrate value
        integrateLabel.setId("integrateLabel");
        integrateButton.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                try {
                    integrateFunction(Double.valueOf(beginXField.getText()), Double.valueOf(endXField.getText()), integrateLabel,
                            progressIndicator);
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        });

        deleteButton.setOnAction((event -> {
            // clear the label text
            integrateLabel.setText("");
            integrateArea.getElements().clear(); // clear integrate area

        }));

        endXField.setOnKeyPressed(new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent event) {
                if (event.getCode() == KeyCode.ENTER){
                    try {
                        integrateFunction(Double.valueOf(beginXField.getText()), Double.valueOf(endXField.getText()),integrateLabel,
                                progressIndicator);
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                    }
                }
                else if (event.getCode() == KeyCode.LEFT){
                    beginXField.requestFocus();
                }
            }
        });



        GridPane gridPane = new GridPane();
        gridPane.prefWidthProperty().bind(vBox.prefWidthProperty());
        gridPane.setVgap(12);
        gridPane.setHgap(20);
        gridPane.setId("functionVBox");

        gridPane.add(new Label("from : "), 0, 0);
        gridPane.add(new Label("to : "), 1, 0);
        gridPane.add(beginXField, 0, 1);
        gridPane.add(endXField, 1,1);
        gridPane.add(integrateButton, 0, 2);
        gridPane.add(deleteButton, 1, 2);
        gridPane.add(integrateLabel,0, 3);
        gridPane.add(progressIndicator, 1, 3);

        gridPane.setAlignment(Pos.CENTER);

        vBox.getChildren().add(new Label("Integrate"));
        vBox.getChildren().add(gridPane);
    }

    final private void setSliderText(Label sliderLabel , Number value){
        sliderLabel.setText(String.format("X : %.2f", value.doubleValue()));
    }

    private void setUpGrid(){
        // create the new grid
        try {
            gridSystem = new GridSystem(-10, -10, 10, 10,
                    GridSystem.WIDTH, GridSystem.HEIGHT);
        } catch (InvalidArgumentException e) {
            e.printStackTrace();
        }
        gridSystem.setCanvas(canvas); // set the canvas
        gridSystem.setAxisColor(Color.BLUE);
        gridSystem.setLineColor(Color.rgb(100, 100, 100, 0.7));
        // draw the grid in the canvas
        gridSystem.draw();
    }

    private void addFunction(GraphFunction function, boolean isDraw){
        // add the new function to function list
        currentFunction = function;
        // add to the list
        functionList.add(function);
        if (isDraw){
            currentFunction.draw();
            // activate the main slider
            mainSlider.setDisable(false);
        }


    }

    private void drawCurrentLine(MouseEvent event){
        if (xLine == null && yLine == null){
            xLine = new Line(event.getX(), event.getY() - posLineLength,
                            event.getX(), event.getY() + posLineLength);
            yLine = new Line(event.getX() - posLineLength, event.getY(),
                            event.getX() + posLineLength, event.getY());

            xLine.setStrokeWidth(1);
            yLine.setStrokeWidth(1);

            xLine.setStroke(Color.LAWNGREEN);
            yLine.setStroke(Color.LAWNGREEN);
            // added to the canvas
            canvas.getChildren().addAll(xLine, yLine);

        }
        else{
            xLine.setStartX(event.getX());
            xLine.setEndX(event.getX());
            xLine.setStartY(event.getY() - posLineLength);
            xLine.setEndY(event.getY() + posLineLength);

            yLine.setStartY(event.getY());
            yLine.setEndY(event.getY());
            yLine.setStartX(event.getX() - posLineLength);
            yLine.setEndX(event.getX() + posLineLength);
        }

        if (!(canvas.getChildren().contains(xLine) && canvas.getChildren().contains(yLine))){
            xLine.setEndY(gridSystem.getHeight());
            yLine.setEndX(gridSystem.getWidth());
            // add to the canvas
            canvas.getChildren().addAll(xLine, yLine);
        }
        // change the XY label text
        Point tempPoint = gridSystem.translateToGrid(new Point(event.getX(), event.getY()));
        XYLabel.setText(String.format("(%.3f, %.3f)", tempPoint.getX(), tempPoint.getY()));

    }

    private void updateTangentLine(Number value){
        if (currentFunction == null){
            throw new NullPointerException("current function point to the null value.");
        }

        // execute when tangent line mode is tangent line
        if (tangentLineMode.equals("tangent line")){

            // call to the tangent line producer function in FunctionFactory Class
            Point[] points = FunctionFactory.getTangentLineToCanvas(
                    currentFunction, value.doubleValue(), gridSystem
            );

            if (points == null){
                // change the function position text
                functionPositionText.setText(
                        FunctionFactory.getCurrentPointAsString(currentFunction, value.doubleValue()));
                return;
            }

            if (this.tangentLine == null){
                // create the new tangent line
                this.tangentLine = new Line(points[1].getX(), points[1].getY(),
                        points[2].getX(), points[2].getY());
                this.tangentLine.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));
                this.tangentLine.setStroke(Color.RED);
                this.tangentLine.setStrokeWidth(2);
                this.tangentLine.setStrokeLineCap(StrokeLineCap.ROUND);
                this.tangentLine.setSmooth(true);

                // create the function circle
                functionPositionOval = new Ellipse(points[0].getX(), points[0].getY(),
                                            7, 7);
                functionPositionOval.setStrokeWidth(5);
                functionPositionOval.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.BLACK, 20, 0.1, 0, 0));
                functionPositionOval.setStroke(Color.RED);
                functionPositionOval.setFill(Color.RED);

                // add to the canvas
                canvas.getChildren().addAll(this.tangentLine, functionPositionOval);
            }
            else{
                setPoints(this.tangentLine, points[1], points[2]);
                // set the oval position
                functionPositionOval.setCenterX(points[0].getX());
                functionPositionOval.setCenterY(points[0].getY());
            }

            if (!(canvas.getChildren().contains(tangentLine) && canvas.getChildren().contains(functionPositionOval))){
                canvas.getChildren().addAll(tangentLine, functionPositionOval);
            }

            tangentLineBox.setLine(FunctionFactory.getTangentLine(currentFunction, value.doubleValue()));

        }
        // execute when tangent line mode in normal line
        else if (tangentLineMode.equals("normal line")){

            Point[] points = FunctionFactory.getNormalLineToCanvas(
                    currentFunction, value.doubleValue(), gridSystem
            );
            if (points == null){
                // change the function position text
                functionPositionText.setText(
                        FunctionFactory.getCurrentPointAsString(currentFunction, value.doubleValue()));
                return;
            }

            if (this.tangentLine == null){
                // create the new tangent line
                this.tangentLine = new Line(points[1].getX(), points[1].getY(),
                        points[2].getX(), points[2].getY());
                this.tangentLine.setStroke(Color.RED);
                this.tangentLine.setStrokeWidth(3);
                this.tangentLine.setSmooth(true);

                // create the function circle
                functionPositionOval = new Ellipse(points[0].getX(), points[0].getY(), 7, 7);
                functionPositionOval.setStrokeWidth(5);
                functionPositionOval.setStroke(Color.HOTPINK);
                functionPositionOval.setFill(Color.RED);

                // add to the canvas
                canvas.getChildren().addAll(functionPositionOval, tangentLine);
            }
            else{
                setPoints(this.tangentLine, points[1], points[2]);
                // set the oval position
                functionPositionOval.setCenterX(points[0].getX());
                functionPositionOval.setCenterY(points[0].getY());
            }

            if (!(canvas.getChildren().contains(tangentLine) && canvas.getChildren().contains(functionPositionOval))){
                canvas.getChildren().addAll(tangentLine, functionPositionOval);
            }
            tangentLineBox.setLine(FunctionFactory.getNormalLine(currentFunction, value.doubleValue()));

        }

        // change the function position text
        functionPositionText.setText(
                FunctionFactory.getCurrentPointAsString(currentFunction, value.doubleValue()));
    }

    private static void setPoints(Line line , Point p1, Point p2){
        if (line != null){
            line.setStartX(p1.getX());
            line.setStartY(p1.getY());

            line.setEndX(p2.getX());
            line.setEndY(p2.getY());
        }
    }

    private final  void zoomCanvas(Number value){
        double factor = value.doubleValue() / 100;
        // first delete all canvas system
        canvas.getChildren().clear();

        for (GraphFunction function : functionList){
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
        // draw the all of the function again
        for (GraphFunction function : functionList) {
            function.draw();
        }

        // update the rules
        xRule.update();
        yRule.update();
    }

    private final void deleteFunction(GraphFunction function){
        if (function != null){
            if (function.equals(currentFunction)){
                currentFunction = null;
            }
            // remove from the list and canvas
            // first remove from the canvas
            Path deletedPath = function.getGraphPath();
            deletedPath.getElements().clear();
            // remove from the canvas
            canvas.getChildren().remove(deletedPath);
            // lastly remove from the list view
            functionList.remove(function);
        }
    }

    private final void updateCanvas(){
        // first delete the canvas grid system and redraw
        canvas.getChildren().clear();
        gridSystem.refresh();
        // delete the all function graph
        for (GraphFunction function : functionList){
            function.delete();
            function.draw();
        }
        // update the rule
        xRule.update();
        yRule.update();

    }

    private final Popup createCanvasPopup(){
        // create the new popup widget
        final Popup popup = new Popup();
        popup.setAutoHide(true);
        popup.setHideOnEscape(true);
        // create the popup rectangle
        final Rectangle rect = new Rectangle(0, 0, 100, 80);
        rect.setFill(Color.DARKORANGE);
        rect.setStrokeWidth(0);

        // create the text for this
        final Text position = new Text(10, 40, "");
        position.setFont(Font.font("verdana", 18));
        position.setFill(Color.WHITE);
        // add to the popup
        popup.getContent().addAll(rect, position);

        return popup;
    }

    private void integrateFunction(double x1 , double x2 , Label label, ProgressIndicator indicator){

        indicator.setVisible(true);
        Thread integrateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                if (currentFunction != null){
                    double intgerateValue = currentFunction.integrate(x1, x2);
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            label.setText(String.format("= %.5f", intgerateValue));
                            indicator.setVisible(false);
                        }
                    });

                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            // draw integrate area in the canvas
                            if (integrateArea == null){
                                integrateArea = new Path();
                                integrateArea.setStrokeWidth(1);
                                integrateArea.setStroke(Color.ORANGERED);
                                canvas.getChildren().add(integrateArea);
                            }
                            // clear integrate area path
                            integrateArea.getElements().clear();
                            integrateArea.getElements().addAll(
                                    new MoveTo(gridSystem.translateToCanvasX(x2), gridSystem.translateToCanvasY(currentFunction.getValue(x2))),
                                    new LineTo(gridSystem.translateToCanvasX(x2), gridSystem.translateToCanvasY(0)),
                                    new LineTo(gridSystem.translateToCanvasX(x1), gridSystem.translateToCanvasY(0)),
                                    new LineTo(gridSystem.translateToCanvasX(x1), gridSystem.translateToCanvasY(currentFunction.getValue(x1)))
                            );
                            // start the loop for paths
                            double x = x1;
                            // choose increment for this loop
                            double increment;
                            if (gridSystem.getX1() - gridSystem.getX2() >= 10){
                                increment = 0.1;
                            }
                            else {
                                increment = 0.01;
                            }
                            while (x <= x2){
                                x += increment;
                                integrateArea.getElements().add(
                                        new LineTo(gridSystem.translateToCanvasX(x), gridSystem.translateToCanvasY(currentFunction.getValue(x)))
                                );
                            }
                            integrateArea.setFill(Color.rgb(240, 70, 7, 0.3));
                        }
                    });
                }
                else
                    indicator.setVisible(false);
            }
        });

        integrateThread.start();
    }

}
