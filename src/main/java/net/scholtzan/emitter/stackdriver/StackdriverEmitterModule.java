package net.scholtzan.emitter.stackdriver;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import org.apache.druid.guice.JsonConfigProvider;
import org.apache.druid.guice.ManageLifecycle;
import org.apache.druid.initialization.DruidModule;
import org.apache.druid.java.util.emitter.core.Emitter;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

public class StackdriverEmitterModule implements DruidModule {
    private static final String EMITTER_TYPE = "stackdriver";

    @Override
    public List<? extends Module> getJacksonModules()
    {
        return Collections.EMPTY_LIST;
    }

    @Override
    public void configure(Binder binder)
    {
        JsonConfigProvider.bind(binder, "druid.emitter." + EMITTER_TYPE, StackdriverEmitterConfig.class);
    }

    @Provides
    @ManageLifecycle
    @Named(EMITTER_TYPE)
    public Emitter getEmitter(StackdriverEmitterConfig config, ObjectMapper mapper) throws IOException
    {
        return StackdriverEmitter.of(config, mapper);
    }
}
