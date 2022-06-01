package util;

public enum FunctionMode {
    NORMAL(0),
    DERIVATIVE(1),
    INTEGRATE(2);

    private final int index;

    FunctionMode(int i){index = i;}

    public int getIndex() {
        return index;
    }
}
