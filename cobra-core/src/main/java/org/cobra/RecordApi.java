package org.cobra;

public interface RecordApi {

    byte[] getRaw(String key);

    <T> T query(String key);
}
