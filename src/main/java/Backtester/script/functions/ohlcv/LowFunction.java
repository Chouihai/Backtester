package Backtester.script.functions.ohlcv;

import Backtester.objects.valueaccumulator.OhlcvField;

public class LowFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "low";

    public OhlcvField getField() {
        return OhlcvField.LOW;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}