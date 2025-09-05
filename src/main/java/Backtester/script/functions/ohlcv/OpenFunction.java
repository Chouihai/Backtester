package Backtester.script.functions.ohlcv;

import Backtester.objects.valueaccumulator.OhlcvField;

public class OpenFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "open";

    public OhlcvField getField() {
        return OhlcvField.OPEN;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}