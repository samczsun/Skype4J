package com.samczsun.skype4j.serialize;

import com.google.gson.JsonObject;

public interface JsonSerializable {
    public JsonObject serialize();

    public void deserialize(JsonObject object);
}
