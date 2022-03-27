package dialog;

import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.util.Callback;
import util.GridSystem;

import java.util.Arrays;

public class GridLimitDialog extends Dialog<double[]> {

    // declare the data members
    private double[] limits = new double[4];

    public GridLimitDialog(GridSystem gridSystem){

        // set the style sheets
        getDialogPane().getStylesheets().addAll(getClass().getResource("../style.css").toExternalForm(),
                                                getClass().getResource("../dialog_style.css").toExternalForm());
        setWidth(550);
        // create the x and y limit text fields and sliders
        // for x
        TextField x1Entry = new TextField(String.format("%.2f", gridSystem.getX1()));
        TextField x2Entry = new TextField(String.format("%.2f", gridSystem.getX2()));

        // create the sliders
        final Slider x1Slider = new Slider(0, 100, gridSystem.getX1());
        final Slider x2Slider = new Slider(0, 100, gridSystem.getX2());
        // bind the values of the slider and textfileds
        x1Entry.textProperty().bind(x1Slider.valueProperty().asString("%.2f"));
        x2Entry.textProperty().bind(x2Slider.valueProperty().asString("%.2f"));

        // then create the yy sliders and tetx fields
        final TextField y1Entry = new TextField(String.format("%.2f", gridSystem.getY1()));
        final TextField y2Entry = new TextField(String.format("%.2f", gridSystem.getY2()));

        // create the y sliders
        final Slider y1Slider = new Slider(0, 100, gridSystem.getY1());
        final Slider y2Slider = new Slider(0, 100, gridSystem.getY2());

        // bind the properties
        y1Entry.textProperty().bind(y1Slider.valueProperty().asString("%.2f"));
        y2Entry.textProperty().bind(y2Slider.valueProperty().asString("%.2f"));

        for (Slider slider : Arrays.asList(new Slider[] {x1Slider, x2Slider, y1Slider, y2Slider})){
            slider.setPrefWidth(450);
            slider.setShowTickMarks(true);
            slider.setBlockIncrement(1.0);
        }

        // create the grid for pack all of them
        final GridPane gridPane = new GridPane();
        gridPane.setVgap(10);
        gridPane.setHgap(10);

        gridPane.add(new Label("X"), 0, 0);
        gridPane.add(new Label("min"), 1, 0);
        gridPane.add(new Label("max"), 1, 1);
        gridPane.add(x1Slider, 2, 0);
        gridPane.add(x2Slider, 2, 1);
        gridPane.add(x1Entry, 3, 0);
        gridPane.add(x2Entry, 3, 1);

        gridPane.add(new Label("Y"), 0, 3);
        gridPane.add(new Label("min"), 1, 3);
        gridPane.add(new Label("max"), 1, 4);
        gridPane.add(y1Slider, 2, 3);
        gridPane.add(y2Slider, 2 ,4);
        gridPane.add(y1Entry, 3, 3);
        gridPane.add(y2Entry, 3,4);

        // set the content
        setGraphic(gridPane);
        setTitle("Grid Limit Change Dialog");

        // setup the dialog buttons
        final ButtonType apply_limits = new ButtonType("Apply Limits", ButtonBar.ButtonData.APPLY);
        final ButtonType cancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        getDialogPane().getButtonTypes().addAll(apply_limits, cancel);
        setResizable(true);

        // set the set and wait function
        setResultConverter(new Callback<ButtonType, double[]>() {
            @Override
            public double[] call(ButtonType param) {
                // check the of the buttons is the apply
                if (param == apply_limits){
                    // change the grid system limits
                    try{
                        limits[0] = Double.valueOf(x1Entry.getText());
                        limits[1] = Double.valueOf(x2Entry.getText());
                        limits[2] = Double.valueOf(y1Entry.getText());
                        limits[3] = Double.valueOf(y2Entry.getText());

                        // set to the grid system
                        if ((limits[0] >= limits[1]) || (limits[2] >= limits[3])){
                            final Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("limit format error");
                            alert.setContentText("please enter the correct limits to grid!");
                            alert.showAndWait();
                        }

                        try {
                            gridSystem.setX1(limits[0]);
                            gridSystem.setX2(limits[1]);
                            gridSystem.setY1(limits[2]);
                            gridSystem.setY2(limits[3]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    catch (NumberFormatException exception){
                        // show the error message
                        final Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setContentText("Please enter the numbers only in correct format");
                        alert.setTitle("Number Format Error");
                        alert.showAndWait();

                        return limits;
                    }
                }

                return limits;
            }
        });

    }
}
