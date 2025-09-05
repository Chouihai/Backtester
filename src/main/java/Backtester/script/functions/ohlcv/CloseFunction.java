package Backtester.script.functions.ohlcv;

import Backtester.objects.valueaccumulator.OhlcvField;

public class CloseFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "close";

    public OhlcvField getField() {
        return OhlcvField.CLOSE;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}