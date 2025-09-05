package Backtester.script.functions.ohlcv;

import Backtester.objects.valueaccumulator.OhlcvField;

public class VolumeFunction extends OhlcvFunction {

    public static final String FUNCTION_NAME = "volume";

    public OhlcvField getField() {
        return OhlcvField.VOLUME;
    }

    public String getFunctionName() {
        return FUNCTION_NAME;
    }
}