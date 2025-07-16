package server;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class LocalDateTimeSerializer extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");


    @Override
    public void write(final JsonWriter jsonWriter, final LocalDateTime localDateTime) throws IOException {
        if (localDateTime == null) {
            jsonWriter.nullValue();
            return;
        }
        jsonWriter.value(localDateTime.format(formatter));
    }

    @Override
    public LocalDateTime read(final JsonReader jsonReader) throws IOException {
        String string = jsonReader.nextString();
        if ("null".equals(string)) {
            return null;
        }
        return LocalDateTime.parse(string, formatter);
    }
}
