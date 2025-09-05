package Backtester.script.functions.ohlcv;


import Backtester.objects.valueaccumulator.OhlcvField;

public class HighFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "high";

    public OhlcvField getField() {
        return OhlcvField.HIGH;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}