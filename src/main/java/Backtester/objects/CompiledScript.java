package Backtester.objects;

import Backtester.script.statements.expressions.FunctionCall;
import Backtester.script.statements.Statement;

import java.util.List;
import java.util.Set;

public record CompiledScript(List<Statement> statements, Set<String> functionCalls) {}
