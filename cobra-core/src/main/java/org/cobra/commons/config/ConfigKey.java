package org.cobra.commons.config;

/**
 * A key-value pair hold information for application config
 */
public class ConfigKey {

    public final String key;
    public Object value;

    public ConfigKey(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    public String key() {
        return this.key;
    }

    @SuppressWarnings("unchecked")
    public <T> T value() {
        return (T) this.value;
    }

    public void set(Object value) {
        this.value = value;
    }

}
