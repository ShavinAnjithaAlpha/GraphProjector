package util;

import javax.script.ScriptException;

public class Function {
    // static data member
    public final static char[] validChars = new char[] {'a', 'b', 'd', 'e', 'f', 'i', 'j', 'k'};

    // declare the main data members
    final protected int ID;
    protected String function;
    protected GridSystem grid = null;

    private static int Counter = 0;
    public static double increment = 0.000001;

    public Function(String function){
        this.function = function;
        // increment the counter
        ID = ++Counter;
    }

    public Function(String function , GridSystem grid){
        this(function);
        this.grid = grid;
    }

    public Point getPoint(double x){
        double y = getValue(x);
        return new Point(x ,y);

    }

    public double getValue(double x){
        try{
            return FunctionValue.getValue(function , x);
        }
        catch (ScriptException ex){
            ex.printStackTrace();
        }
        return 0;
    }

    public double getValueWithY(double x, double y){
        try{
            return FunctionValue.getValue(
                    function.replaceAll("y", String.format("(%s)", String.valueOf(y))), x);
        }
        catch (ScriptException ex){
//            ex.printStackTrace();
        }
        return 0;
    }

    public double getValueWithY(Point point){
        try{
            return FunctionValue.getValue(
                    function.replaceAll("y", String.format("(%s)", String.valueOf(point.getY()))), point.getY());
        }
        catch (ScriptException ex){
            ex.printStackTrace();
        }
        return 0;
    }

    public void setGrid(GridSystem grid) {
        this.grid = grid;
    }

    public double getTangent(double x){
        // return the tangent at the point x
        return (getValue(x + increment/2) - getValue(x - increment/2)) / increment;
    }

    private double getSecondDerivative(double x){
        return (getTangent(x + increment/2) - getTangent(x - increment/2)) / increment;
    }

    public SimpleLine getTangentLine(double x){
        Point point = new Point(x , getValue(x));
        return new SimpleLine(getTangent(x), point);
    }

    public SimpleLine getSecondTangentLine(double x){
        Point point = new Point(x, getTangent(x));
        return new SimpleLine(getSecondDerivative(x), point);
    }

    public SimpleLine getNormalLine(double x){
        Point point = new Point(x, getValue(x));
        return new SimpleLine(-1/getTangent(x), point);
    }

    public SimpleLine getSecondNormalLine(double x){
        Point point = getPoint(x);
        return new SimpleLine(-1/getSecondDerivative(x), point);
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public static double getIncrement() {
        return increment;
    }

    public static void setIncrement(double increment){
        if (increment <= 0){
            return;
        }
        Function.increment = increment;
    }

    public double integrate(double x1 , double x2){
        boolean change = false;
        if (x1 > x2){
            double temp = x1;
            x1 = x2;
            x2 = temp;
        }
        double value = 0;
        double x = x1;
        while (x <= x2){
            value += getValue(x + increment/2) * increment;
            x += increment;
        }

        if (change)
            value = -value;
        return value;
    }

    @Override
    public String toString() {
        return String.format("%s" , function);
    }

    public int getID() {
        return ID;
    }
}
