package server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assertions;

import com.google.gson.Gson;
import com.google.gson.JsonIOException;

import java.io.IOException;
import java.io.StringWriter;
import java.time.LocalDateTime;

class LocalDateTimeSerializerTest {
    private LocalDateTimeSerializer adapter;

    @BeforeEach
    void setUp() {
        adapter = new LocalDateTimeSerializer();
    }

    @Test
    void testWriteLocalDateTime() throws JsonIOException {
        LocalDateTime localDateTime = LocalDateTime.of(2023, 10, 5, 14, 30);
        StringWriter writer = new StringWriter();
        try {
            adapter.write(new Gson().newJsonWriter(writer), localDateTime);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        String expected = "\"2023-10-05T14:30\"";
        Assertions.assertEquals(expected, writer.toString());
    }
}
