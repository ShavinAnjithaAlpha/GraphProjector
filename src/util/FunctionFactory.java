package util;

public class FunctionFactory {

    // function for get the position tex for the canvas
    public static String getCurrentPointAsString(GraphFunction function, double x){

        if (function == null){
            return "Nothing";
        }
        // calculate current position based on the function mode
        double y;
        switch (function.getFunctionMode()){
            case NORMAL:{
                y = function.getValue(x);
                return String.format(
                        "X : %.2f\nY : %.2f",
                        x, y
                );
            }
            case DERIVATIVE:{
                y = function.getTangent(x);
                return String.format(
                        "X : %.2f\nY : %.2f",
                        x, y
                );
            }
            case INTEGRATE:{
                return "No result in\nINTEGRATE MODE";
            }
            default:{
                return "Nothing";
            }
        }
    }

    public static String getCurrentPointAsString(ParametricGraphFunction function, double t){
        if (function == null){
            return "Nothing";
        }
        // calculate current position based on the function mode
        double y;
        double x;
        switch (function.getFunctionMode()){
            case NORMAL:{
                x = function.getxFunction().getValue(t);
                y = function.getyFunction().getValue(t);
                return String.format(
                        "X : %.2f\nY : %.2f",
                        x, y
                );
            }
            case DERIVATIVE:{
                x = function.getxFunction().getTangent(t);
                y = function.getyFunction().getTangent(t);
                return String.format(
                        "X : %.2f\nY : %.2f",
                        x, y
                );
            }
            case INTEGRATE:{
                return "No result in\nINTEGRATE MODE";
            }
            default:{
                return "Nothing";
            }
        }
    }

    public static Point[] getTangentLineToCanvas(GraphFunction function, double x, GridSystem grid){
        // convert the value to double
        Point center = null;
        SimpleLine simpleLine = null;
        switch (function.getFunctionMode()){
            case NORMAL:{
                // and create the new tangent line object
                center = function.getPoint(x);
                // build the new simple line
                simpleLine = function.getTangentLine(x);
                break;
            }
            case DERIVATIVE:{
                // and create the new tangent line object
                center = new Point(x, function.getTangent(x));
                // build the new simple line
                simpleLine = function.getSecondTangentLine(x);
                break;
            }
            case INTEGRATE:{
                return null;
            }
        }
        Point[] points = simpleLine.getPoints(center);

        // draw the line between these two lines
        Point startPoint = grid.translateToCanvas(points[0]);
        Point endPoint = grid.translateToCanvas(points[1]);

        Point canvasPoint = grid.translateToCanvas(center);

        return new Point[] {canvasPoint, startPoint, endPoint};
    }

    public static Point[] getNormalLineToCanvas(GraphFunction function, double x ,GridSystem grid){
        // create the normal line
        Point center = null;
        SimpleLine normalLine = null;

        switch (function.getFunctionMode()){
            case NORMAL:{
                center = function.getPoint(x);
                normalLine = function.getNormalLine(x);
                break;
            }
            case DERIVATIVE:{
                center = new Point(x, function.getTangent(x));
                normalLine = function.getSecondNormalLine(x);
                break;
            }
            case INTEGRATE:{
                return null;
            }
        }
        // points of simple line
        Point[] normalPoints = normalLine.getPoints(center);

        Point canvasPoint = grid.translateToCanvas(center);
        Point normalStartPoint = grid.translateToCanvas(normalPoints[0]);
        Point normalEndPoint = grid.translateToCanvas(normalPoints[1]);

        return new Point[] {canvasPoint, normalStartPoint, normalEndPoint};
    }

    public static SimpleLine getNormalLine(GraphFunction function, double x) {
        // create the normal line
        Point center = null;
        SimpleLine normalLine = null;

        switch (function.getFunctionMode()) {
            case NORMAL: {
                center = function.getPoint(x);
                normalLine = function.getNormalLine(x);
                break;
            }
            case DERIVATIVE: {
                center = new Point(x, function.getTangent(x));
                normalLine = function.getSecondNormalLine(x);
                break;
            }
            case INTEGRATE: {
                return null;
            }
        }
        return normalLine;
    }

    public static SimpleLine getTangentLine(GraphFunction function, double x) {
        // create the normal line
        Point center = null;
        SimpleLine tangentLine = null;

        switch (function.getFunctionMode()) {
            case NORMAL: {
                center = function.getPoint(x);
                tangentLine = function.getTangentLine(x);
                break;
            }
            case DERIVATIVE: {
                center = new Point(x, function.getTangent(x));
                tangentLine = function.getSecondTangentLine(x);
                break;
            }
            case INTEGRATE: {
                return null;
            }
        }
        return tangentLine;
    }
}
