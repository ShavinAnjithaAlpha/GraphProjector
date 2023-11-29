package util;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.sql.*;

public class FunctionValue {

    private final static ScriptEngineManager manager = new ScriptEngineManager();
    private final static ScriptEngine functionEngineParser =
            manager.getEngineByName("JavaScript");
    private static Connection connection;
    private static Statement statement;

    static {
        try {
            connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/sys", "root", "Sha2002@vin");
            statement = connection.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static double valueOf(String functionText ,double x) throws ScriptException{
        return getValue(functionText, x);
    }

//    public static double getValue(String functionText, double x){
//
//        double value = 0;
//        short opertaor = 0;
//        double temp = 0;
//        int position = 0;
//        StringBuffer functionPart = new StringBuffer(10);
//
//        while (position < functionText.length()){
//
//            if (functionText.charAt(position) == '-'){
//
//                if (!(functionPart.toString().equals("") ||  functionPart.toString().equals("-")))
//                    value += getPartValue(functionPart.toString(), x);
//                functionPart = new StringBuffer("-");
//                position++;
//            }
//            else if (functionText.charAt(position) == '+'){
//                if (!(functionPart.toString().equals("") || functionPart.toString().equals("-")))
//                    value += getPartValue(functionPart.toString(), x);
//                functionPart = new StringBuffer();
//                position++;
//            }
//            else if(functionText.charAt(position) == '('){
//                // collect the bracket inside parts
//                StringBuffer bracketPart = new StringBuffer(20);
//
//                while (position < functionText.length()){
//                    position++;
//                    if (functionText.charAt(position) == ')'){
//                        break;
//                    }
//                    bracketPart.append(functionText.charAt(position));
//                }
//                position++;
//                if (position >= functionText.length() - 1){
//                    double temp2 = 0;
//                    if (opertaor == 1){
//                        temp2 = (temp  * getValue(bracketPart.toString(), x));
//                    }
//                    else if (opertaor == 2){
//                        temp2 = temp / getValue(bracketPart.toString(), x);
//                    }
//
//                    if (functionPart.toString().equals("-")){
//                        value -= temp2;
//                    }
//                    else {
//                        value += temp2;
//                    }
//                    break;
//                }
//
//                if (functionText.charAt(position) == '+' || functionText.charAt(position) == '-'){
//                    // calculate the brackets part values
//                    double temp2 = 0;
//                    if (opertaor == 1){
//                        temp2 = (temp  * getValue(bracketPart.toString(), x));
//                    }
//                    else if (opertaor == 2){
//                        temp2 = temp / getValue(bracketPart.toString(), x);
//                    }
//                    else{
//                        temp2 = getValue(bracketPart.toString(), x);
//                    }
//
//                    // add to the  main function part
//                    if (functionPart.toString().equals("-")){
//                        value -= temp2;
//                    }
//                    else {
//                        value += temp2;
//                    }
//                    opertaor = 0;
//                }
//                else {
//                    double temp2 = 0;
//                    if (opertaor == 1){
//                        temp = (temp  * getValue(bracketPart.toString(), x));
//                    }
//                    else if (opertaor == 2){
//                        temp = temp / getValue(bracketPart.toString(), x);
//                    }
//                    else
//                        temp = getValue(bracketPart.toString(), x);
//                    if (functionText.charAt(position) == '*')
//                        opertaor = 1;
//                    else if (functionText.charAt(position) == '/')
//                        opertaor = 2;
//                    position++;
//                }
//
//            }
//
//            else{
//                functionPart.append(functionText.charAt(position));
//                position++;
//            }
//
//            if (position >= functionText.length() && functionPart.length() > 0
//                    && !(functionPart.toString().equals("-"))){
//                value += getPartValue(functionPart.toString(), x);
//            }
//        }
//
//        return value;
//    }

    public static double getValue(String text, double x) throws ScriptException{
        try {
            String replaceText = null;
            replaceText = text.replaceAll("exp|EXP", "e##");
            // first replace the variable x with the value of the x
            replaceText = replaceText.replaceAll("[xX]", String.valueOf(x));
            replaceText = replaceText.replaceAll("e##", "exp");
            // pars to the function parser engine
            ResultSet set = statement.executeQuery(String.format("SELECT %s", replaceText));
            set.next();
            return set.getDouble(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static double getPartValue(String part, double x){
//        if (!(part.matches("/d*[x]/w/d*"))){
//            return Double.valueOf(part);
//        }
        double value = 0;
        String[] split = part.split("x");

        if (split.length == 2){
            if (split[0].equals("")){
                value = Math.pow(x, Double.valueOf(split[1].replace("^", "")));
            }
            else if (split[0].equals("-")){
                value = -Math.pow(x, Double.valueOf(split[1].replace("^", "")));
            }
            else{
                double coff = Double.valueOf(split[0]);
                double power = Double.valueOf(split[1].replace("^", ""));
                value = coff * Math.pow(x, power);
            }

        }
        else if (split.length == 1){
            value = x * Double.valueOf(split[0]);
        }
        else{
            value = x;
        }

        return value;
    }

    public static void main(String[] args) {
        // create the function text object
//        System.out.println(FunctionValue.getValue("2x+5", 5));
    }
}
