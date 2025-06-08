package HaitamStockProject.objects;

import HaitamStockProject.script.statements.Statement;

import java.util.List;

public record CompiledScript(List<Statement> statements) {}
