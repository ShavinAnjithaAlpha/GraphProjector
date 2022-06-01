package widgets;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import util.ParametricGraphFunction;

public class ParametricFunctionCell extends ListCell<ParametricGraphFunction> {

    @Override
    protected void updateItem(ParametricGraphFunction item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null){
            // create the two labels
            Label xFunctionLabel = new Label(
                    String.format("X : %s",
                            item.getxFunction().toString().replaceAll("x", "t")));

            Label yFunctionLabel = new Label(
                    String.format("Y : %s",
                            item.getyFunction().toString().replaceAll("x", "t"))
            );
            xFunctionLabel.setFont(new Font(22));
            yFunctionLabel.setFont(new Font(22));

            // create the color box
            Rectangle colorRect = new Rectangle(30, 30);
            colorRect.setFill(item.getColor());
            colorRect.setStrokeWidth(0);
            // create the grid for pack the item
            VBox vBox = new VBox(xFunctionLabel, yFunctionLabel);
            HBox hBox = new HBox(vBox, colorRect);
            hBox.setSpacing(30);
            hBox.setAlignment(Pos.CENTER);

            setText(null);
            setGraphic(hBox);
        }
        else{
            setText(null);
            setGraphic(null);
        }
    }
}
