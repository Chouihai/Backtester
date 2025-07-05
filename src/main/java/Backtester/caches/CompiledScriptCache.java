package Backtester.caches;

import Backtester.objects.CompiledScript;

import java.util.Optional;

public interface CompiledScriptCache {

    Optional<CompiledScript> getCompiledScript(String name);
}
