package Backtester.script;

import Backtester.caches.BarCache;
import Backtester.caches.ValueAccumulatorCache;
import Backtester.objects.Bar;
import Backtester.objects.valueaccumulator.OhlcvValueAccumulator;
import Backtester.script.functions.ohlcv.*;
import Backtester.script.functions.result.NonVoidScriptFunctionResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OhlcvFunctionsTest {

    private BarCache barCache;
    private ValueAccumulatorCache vaCache;
    private CloseFunction closeFunction;
    private OpenFunction openFunction;
    private HighFunction highFunction;
    private LowFunction lowFunction;
    private VolumeFunction volumeFunction;

    @BeforeEach
    void setUp() {
        barCache = new BarCache();
        vaCache = new ValueAccumulatorCache();
        closeFunction = new CloseFunction(vaCache, barCache);
        openFunction = new OpenFunction(vaCache, barCache);
        highFunction = new HighFunction(vaCache, barCache);
        lowFunction = new LowFunction(vaCache, barCache);
        volumeFunction = new VolumeFunction(vaCache, barCache);

        List<Bar> bars = new ArrayList<>();
        LocalDate startDate = LocalDate.of(2024, 1, 1);

        bars.add(new Bar(0, startDate, 100.0, 105.0, 98.0, 102.0, 1000L));
        bars.add(new Bar(1, startDate.plusDays(1), 102.0, 108.0, 101.0, 106.0, 1200L));
        bars.add(new Bar(2, startDate.plusDays(2), 106.0, 110.0, 104.0, 108.0, 1500L));

        barCache.loadCache(bars);
    }

    @Test
    void testCloseFunction() {
        EvaluationContext context = new EvaluationContext(2);

        var result = closeFunction.execute(List.of(), context);
        OhlcvValueAccumulator closeVa = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(108.0, closeVa.getValue());

        result = closeFunction.execute(List.of(1), context);
        OhlcvValueAccumulator closeVaLookback = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(106.0, closeVaLookback.getValue());
    }

    @Test
    void testOpenFunction() {
        EvaluationContext context = new EvaluationContext(2);

        var result = openFunction.execute(List.of(), context);
        OhlcvValueAccumulator openVa = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(106.0, openVa.getValue());

        result = openFunction.execute(List.of(1), context);
        OhlcvValueAccumulator openVaLookback = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(102.0, openVaLookback.getValue());
    }

    @Test
    void testHighFunction() {
        EvaluationContext context = new EvaluationContext(2);

        var result = highFunction.execute(List.of(), context);
        OhlcvValueAccumulator highVa = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(110.0, highVa.getValue());

        result = highFunction.execute(List.of(1), context);
        OhlcvValueAccumulator highVaLookback = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(108.0, highVaLookback.getValue());
    }

    @Test
    void testLowFunction() {
        EvaluationContext context = new EvaluationContext(2);

        var result = lowFunction.execute(List.of(), context);
        OhlcvValueAccumulator lowVa = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(104.0, lowVa.getValue());

        result = lowFunction.execute(List.of(1), context);
        OhlcvValueAccumulator lowVaLookback = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(101.0, lowVaLookback.getValue());
    }

    @Test
    void testVolumeFunction() {
        EvaluationContext context = new EvaluationContext(2);

        var result = volumeFunction.execute(List.of(), context);
        OhlcvValueAccumulator volumeVa = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(1500.0, volumeVa.getValue());

        result = volumeFunction.execute(List.of(1), context);
        OhlcvValueAccumulator volumeVaLookback = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(1200.0, volumeVaLookback.getValue());
    }

    @Test
    void testRollFunctionality() {
        EvaluationContext context = new EvaluationContext(1);

        var result = closeFunction.execute(List.of(), context);
        OhlcvValueAccumulator closeVa = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;
        assertEquals(106.0, closeVa.getValue());

        Bar nextBar = new Bar(2, LocalDate.of(2024, 1, 3), 106.0, 110.0, 104.0, 108.0, 1500L);
        closeVa.roll(nextBar);

        assertEquals(108.0, closeVa.getValue());
    }

    @Test
    void testInvalidLookback() {
        EvaluationContext context = new EvaluationContext(0);

        var result = closeFunction.execute(List.of(1), context);
        OhlcvValueAccumulator closeVa = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result).getValue().value;

        assertThrows(RuntimeException.class, () -> closeVa.getValue());
    }

    @Test
    void testInvalidArguments() {
        EvaluationContext context = new EvaluationContext(0);

        assertThrows(IllegalArgumentException.class, () ->
                closeFunction.execute(List.of(1, 2), context));

        assertThrows(IllegalArgumentException.class, () ->
                closeFunction.execute(List.of(-1), context));

        assertThrows(IllegalArgumentException.class, () ->
                closeFunction.execute(List.of("invalid"), context));
    }

    @Test
    void testEquality() {
        EvaluationContext context = new EvaluationContext(1);

        var result1 = closeFunction.execute(List.of(), context);
        OhlcvValueAccumulator va1 = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result1).getValue().value;

        var result2 = closeFunction.execute(List.of(), context);
        OhlcvValueAccumulator va2 = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result2).getValue().value;

        assertSame(va1, va2);

        var result3 = closeFunction.execute(List.of(1), context);
        OhlcvValueAccumulator va3 = (OhlcvValueAccumulator) ((NonVoidScriptFunctionResult) result3).getValue().value;

        assertNotSame(va1, va3);
    }
}