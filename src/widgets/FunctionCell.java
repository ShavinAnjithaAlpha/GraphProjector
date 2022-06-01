package widgets;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.TextAlignment;
import util.FunctionMode;
import util.GraphFunction;
import util.GridSystem;

public final class FunctionCell extends ListCell<GraphFunction> {

    @Override
    protected void updateItem(GraphFunction item, boolean empty) {
        super.updateItem(item, empty);

        if (item != null){
            // grid system
            // build the label and color box
            // declare the data memebers
            Label functionLabel = new Label(item.toString());
            functionLabel.setTextAlignment(TextAlignment.LEFT);

            // create the color box
            Rectangle colorBox = new Rectangle(20, 20);
            colorBox.setFill(item.getColor());
            colorBox.setStrokeWidth(0);

//            Image modeIcon = null;
//            switch (item.getFunctionMode()){
//                case NORMAL:{
//                    modeIcon = new Image(getClass().getResourceAsStream("../img/function.jpg"));
//                    break;}
//                case DERIVATIVE:{
//                    modeIcon = new Image("img/infinity.png", 30, 30, true, true, true);
//                    break;
//                }
//                case INTEGRATE:{
//                    modeIcon = new Image("img/integral.png", 30, 30, true, true, true);
//                    break;
//                }
//            }

            // mode label
            Label modeLabel = new Label(item.getFunctionMode().toString());

            // create the h box for pack the items
            final HBox hBox = new HBox(20);
            hBox.setAlignment(Pos.CENTER);
            hBox.getChildren().addAll(functionLabel, colorBox, modeLabel);

            setText(null);
            setGraphic(hBox);
        }
        else{
            setText(null);
            setGraphic(null);
        }

    }
}
