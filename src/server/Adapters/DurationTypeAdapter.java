package server.Adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationTypeAdapter extends TypeAdapter<Duration> {
    @Override
    public void write(final JsonWriter out, final Duration value) throws IOException {
        if (value != null) {
            out.value(value.toMinutes());
        } else {
            out.nullValue();
        }

    }

    @Override
    public Duration read(final JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            return Duration.ofMinutes(0);
        }
        return Duration.ofMinutes(in.nextInt());
    }
}
