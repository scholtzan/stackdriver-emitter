package net.scholtzan.emitter.stackdriver;

import com.fasterxml.jackson.databind.InjectableValues;
import com.fasterxml.jackson.databind.ObjectMapper;
import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.druid.jackson.DefaultObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StackdriverEmitterConfigTest extends TestCase {
    private ObjectMapper mapper = new DefaultObjectMapper();

    @BeforeEach
    public void setUp() {
        mapper.setInjectableValues(new InjectableValues.Std().addValue(ObjectMapper.class, new DefaultObjectMapper()));
    }

    @Test
    public void testSerDeserStackdriverEmitterConfig() throws Exception {
        StackdriverEmitterConfig stackdriverEmitterConfig = new StackdriverEmitterConfig("localhost", 9999, 2000, 2000, 200L, "test", null);
        String stackdriverEmitterConfigString = mapper.writeValueAsString(stackdriverEmitterConfig);
        StackdriverEmitterConfig expectedStackdriverEmitterConfig = mapper.readerFor(StackdriverEmitterConfig.class)
                .readValue(stackdriverEmitterConfigString);
        Assert.assertEquals(expectedStackdriverEmitterConfig, stackdriverEmitterConfig);
    }
}