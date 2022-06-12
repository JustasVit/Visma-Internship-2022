package com.visma.task.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class JsonDateSerializer extends JsonSerializer<LocalDateTime>
{
    @Override
    public void serialize(LocalDateTime date, JsonGenerator gen, SerializerProvider provider)
            throws IOException
    {
        gen.writeString(date.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));
    }
}