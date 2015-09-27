package org.locationtech.spatial4j.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class Spatial4jMapper extends ObjectMapper
{
    private static final long serialVersionUID = 1L;

    public Spatial4jMapper() {
        registerModule(new Spatial4jModule());
    }

    /**
     * Convenience method that is shortcut for:
     *<pre>
     *  module.isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
     *<pre>
     */
    public boolean getWriteDatesAsTimestamps() {
        return getSerializationConfig().isEnabled(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /**
     * Convenience method that is shortcut for:
     *<pre>
     *  configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, state)
     *<pre>
     */
    public void setWriteDatesAsTimestamps(boolean state) {
        configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, state);
    }
}
