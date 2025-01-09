package org.cobra.commons.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigDef {

    private final Map<String, ConfigKey> configKeyMap = new LinkedHashMap<>();

    public ConfigDef define(ConfigKey config) {
        this.configKeyMap.put(config.key, config);
        return this;
    }

    public ConfigDef define(String key, Object value) {
        return define(new ConfigKey(key, value));
    }

    public ConfigKey get(String key) {
        return configKeyMap.get(key);
    }

    @SuppressWarnings("unchecked")
    public <T> T valueOf(String key) {
        return (T) configKeyMap.get(key).value;
    }

    public void merge(ConfigKey config) {
        if (!configKeyMap.containsKey(config.key))
            throw new IllegalStateException("unknown key: " + config.key);

        get(config.key).set(config.value);
    }

    public void merge(String key, Object value) {
        merge(new ConfigKey(key, value));
    }
}
