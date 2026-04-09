package ru.practicum.ewm.stats.avro;

import org.apache.avro.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class AvroSchemas {

    public static final Schema USER_ACTION = load("/avro/UserActionAvro.avsc");
    public static final Schema EVENT_SIMILARITY = load("/avro/EventSimilarityAvro.avsc");
    public static final Schema ACTION_TYPE = USER_ACTION.getField("actionType").schema();

    private AvroSchemas() {
    }

    private static Schema load(String path) {
        try (InputStream inputStream = AvroSchemas.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IllegalStateException("Schema not found: " + path);
            }
            String schema = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return new Schema.Parser().parse(schema);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load schema: " + path, exception);
        }
    }
}
