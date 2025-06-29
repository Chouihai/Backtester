package HaitamStockProject.objects;

import HaitamStockProject.script.statements.expressions.FunctionCall;
import HaitamStockProject.script.statements.Statement;

import java.util.List;
import java.util.Map;
import java.util.Set;

public record CompiledScript(List<Statement> statements, Map<String, Set<FunctionCall>> functionCalls) {}
