package widgets;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.TextAlignment;
import util.SimpleLine;

import java.awt.*;

public class TangentLineBox extends VBox {

    SimpleLine line;

    private Label equationLabel;
    private Label tangentLabel;
    private Label intersectLabel;

    public TangentLineBox(){

        this.line = line;
        Label label1 = new Label("equation");
        label1.setTextAlignment(TextAlignment.RIGHT);
        // create the labels
        equationLabel = new Label("None");
        equationLabel.setTextAlignment(TextAlignment.CENTER);

        Label label2 = new Label("tangent");
        Label label3 = new Label("intersect");

        label3.setTextAlignment(TextAlignment.RIGHT);
        label2.setTextAlignment(TextAlignment.RIGHT);

        tangentLabel = new Label("0");
        intersectLabel = new Label("0");

        tangentLabel.setTextAlignment(TextAlignment.RIGHT);
        intersectLabel.setTextAlignment(TextAlignment.RIGHT);

        // create the h box
        final HBox hBox = new HBox(15);
        hBox.getChildren().addAll(label2, label3);

        final HBox hBox1 = new HBox(15);
        hBox1.getChildren().addAll(tangentLabel, intersectLabel);

        setSpacing(10);
        setPadding(new Insets(15));
        getChildren().addAll(label1, equationLabel, hBox, hBox1);

        for (Label label : new Label[] {equationLabel, tangentLabel, intersectLabel}){
            label.setId("tangentLabel");
            label.setMinWidth(100);
        }

        getStylesheets().add(getClass().getResource("tangent_style.css").toExternalForm());
        setMinWidth(200);
    }

    // setter
    public void setLine(SimpleLine line){
        if (line != null) {
            this.line = line;

            // set the label texts
            String equation = String.format("Y = %.2fX", line.getTangent());
            equation += (line.getIntersect() < 0) ? "" : "+";
            equation += String.format(" %.2f", line.getIntersect());

            equationLabel.setText(equation);
            tangentLabel.setText(String.format("%.2f", line.getTangent()));
            intersectLabel.setText(String.format("%.2f", line.getIntersect()));
        }
    }
}
