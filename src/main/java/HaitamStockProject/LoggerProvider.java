package HaitamStockProject;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.spi.InjectionPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class LoggerProvider implements Provider<Logger> {

    private final Class<?> clazz;

    @Inject
    public LoggerProvider(Set<InjectionPoint> injectionPoints) {
        Class<?> foundClass = Object.class;
        for (InjectionPoint ip : injectionPoints) {
            foundClass = ip.getMember().getDeclaringClass();
            break;
        }
        this.clazz = foundClass;
    }

    @Override
    public Logger get() {
        return LoggerFactory.getLogger(clazz);
    }
}
