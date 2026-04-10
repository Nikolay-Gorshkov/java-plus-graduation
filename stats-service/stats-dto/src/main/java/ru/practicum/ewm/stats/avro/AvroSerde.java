package ru.practicum.ewm.stats.avro;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumReader;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryDecoder;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DecoderFactory;
import org.apache.avro.io.EncoderFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;

public final class AvroSerde {

    private AvroSerde() {
    }

    public static byte[] serialize(UserActionAvro action) {
        GenericRecord record = new GenericData.Record(AvroSchemas.USER_ACTION);
        record.put("userId", action.userId());
        record.put("eventId", action.eventId());
        record.put("actionType", new GenericData.EnumSymbol(AvroSchemas.ACTION_TYPE, action.actionType().name()));
        record.put("timestamp", action.timestamp().toEpochMilli());
        return write(record, AvroSchemas.USER_ACTION);
    }

    public static UserActionAvro deserializeUserAction(byte[] payload) {
        GenericRecord record = read(payload, AvroSchemas.USER_ACTION);
        return new UserActionAvro(
                (Long) record.get("userId"),
                (Long) record.get("eventId"),
                ActionTypeAvro.valueOf(record.get("actionType").toString()),
                Instant.ofEpochMilli((Long) record.get("timestamp"))
        );
    }

    public static byte[] serialize(EventSimilarityAvro similarity) {
        GenericRecord record = new GenericData.Record(AvroSchemas.EVENT_SIMILARITY);
        record.put("eventA", similarity.eventA());
        record.put("eventB", similarity.eventB());
        record.put("score", similarity.score());
        record.put("timestamp", similarity.timestamp().toEpochMilli());
        return write(record, AvroSchemas.EVENT_SIMILARITY);
    }

    public static EventSimilarityAvro deserializeEventSimilarity(byte[] payload) {
        GenericRecord record = read(payload, AvroSchemas.EVENT_SIMILARITY);
        return new EventSimilarityAvro(
                (Long) record.get("eventA"),
                (Long) record.get("eventB"),
                (Double) record.get("score"),
                Instant.ofEpochMilli((Long) record.get("timestamp"))
        );
    }

    private static byte[] write(GenericRecord record, Schema schema) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(outputStream, null);
            new GenericDatumWriter<GenericRecord>(schema).write(record, encoder);
            encoder.flush();
            return outputStream.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to serialize Avro payload", exception);
        }
    }

    private static GenericRecord read(byte[] payload, Schema schema) {
        try {
            BinaryDecoder decoder = DecoderFactory.get().binaryDecoder(payload, null);
            return new GenericDatumReader<GenericRecord>(schema).read(null, decoder);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to deserialize Avro payload", exception);
        }
    }
}
