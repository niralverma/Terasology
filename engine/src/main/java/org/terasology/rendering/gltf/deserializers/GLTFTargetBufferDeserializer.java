// Copyright 2021 The Terasology Foundation
// SPDX-License-Identifier: Apache-2.0
package org.terasology.engine.rendering.gltf.deserializers;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import org.terasology.engine.rendering.gltf.model.GLTFTargetBuffer;

import java.lang.reflect.Type;

/**
 * Json deserializer for GLTFTargetBuffer enum, translating the GLTF code to an enum entry
 */
public class GLTFTargetBufferDeserializer implements JsonDeserializer<GLTFTargetBuffer> {
    @Override
    public GLTFTargetBuffer deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return GLTFTargetBuffer.getTypeFromCode(json.getAsInt());
    }
}
