package HaitamStockProject.caches;

import HaitamStockProject.objects.CompiledScript;

import java.util.Optional;

public interface CompiledScriptCache {

    Optional<CompiledScript> getCompiledScript(String name);
}
