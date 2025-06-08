package HaitamStockProject.strategies;

import HaitamStockProject.objects.Bar;
import HaitamStockProject.script.EvaluationContext;
import HaitamStockProject.script.statements.FunctionCall;
import HaitamStockProject.script.statements.Literal;

import java.util.*;

public class ContextBuilder {

//    private final List<FunctionCall> functionCalls;
//    private final Map<String, ValueAccumulator> valueAccumulators;
//    private final List<Bar> initialValues;
//
//    public ContextBuilder(List<Bar> initialValues, List<FunctionCall> functionCalls) {
//        this.functionCalls = functionCalls;
//        this.valueAccumulators = new HashMap<>();
//        this.initialValues = initialValues;
//    }
//
//    public EvaluationContext build() {
//        for (FunctionCall fct: functionCalls) {
//            if (Objects.equals(fct.functionName, "sma")) {
//                try {
//                    if (fct.argumentSize() != 1) throw new RuntimeException("Invalid amount of arguments for function sma, expected 1 argument.");
//                    if (!(fct.arguments.getFirst() instanceof Literal daysArguments)) throw new RuntimeException("Invalid type of argument for function sma");
//                    int days = Integer.parseInt(daysArguments.value.toString());
//                    SmaCalculator smaCalculator = new SmaCalculator(days, initialValues);
//                    valueAccumulators.put(fct.toString(), smaCalculator);
//                } catch (Exception e) {
//                    // TODO: log something
//                    return null;
//                }
//            }
//        }
//        return new EvaluationContext(valueAccumulators);
//    }
}
