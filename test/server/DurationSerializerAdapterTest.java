package server;

import com.google.gson.stream.JsonReader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DurationSerializerAdapterTest {
    private DurationSerializerAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new DurationSerializerAdapter();
    }

    @Test
    void testWriteDuration() throws JsonIOException {
        Duration duration = Duration.ofMinutes(120);
        StringWriter writer = new StringWriter();
        try {
            adapter.write(new Gson().newJsonWriter(writer), duration);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        assertEquals("120", writer.toString());
    }

    @Test
    void testReadDuration() throws IOException {
        JsonReader reader = new Gson().newJsonReader(new StringReader("120"));
        Duration duration = adapter.read(reader);
        assertEquals(Duration.ofMinutes(120), duration);
    }

    @Test
    void testSerializeAndDeserializeNull() throws IOException {
        String nullValue = "null";
        Duration result = adapter.read(new Gson().newJsonReader(new StringReader(nullValue)));
        assertNull(result);

        StringWriter writer = new StringWriter();
        adapter.write(new Gson().newJsonWriter(writer), null);
        assertEquals(nullValue, writer.toString());
    }
}
