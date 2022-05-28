package widgets;

import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.GridPane;
import javafx.scene.shape.Rectangle;
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

            // create the color box
            Rectangle colorRect = new Rectangle(20, 80);
            colorRect.setFill(item.getColor());
            // create the grid for pack the item
            GridPane gridPane = new GridPane();
            gridPane.setVgap(10);
            gridPane.add(xFunctionLabel, 0, 0);
            gridPane.add(yFunctionLabel, 0, 1);
            gridPane.add(colorRect, 1, 0, 1, 2);

            setText(null);
            setGraphic(gridPane);
        }
        else{
            setText(null);
            setGraphic(null);
        }
    }
}
