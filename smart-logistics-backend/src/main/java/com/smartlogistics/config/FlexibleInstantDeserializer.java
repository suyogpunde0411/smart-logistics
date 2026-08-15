package com.smartlogistics.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class FlexibleInstantDeserializer extends JsonDeserializer<Instant> {

    @Override
    public Instant deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String text = p.getText();
        if (!StringUtils.hasText(text)) {
            return null;
        }

        text = text.trim();

        // 1. Try numeric timestamp (epoch millis or epoch seconds)
        try {
            long epoch = Long.parseLong(text);
            if (epoch > 100000000000L) {
                return Instant.ofEpochMilli(epoch);
            } else {
                return Instant.ofEpochSecond(epoch);
            }
        } catch (NumberFormatException ignored) {}

        // 2. Try standard ISO-8601 Instant (e.g. 2026-08-16T12:00:00Z or 2026-08-16T12:00:00.000Z)
        try {
            return Instant.parse(text);
        } catch (DateTimeParseException ignored) {}

        // 3. Try date-only format (e.g. 2026-08-16)
        try {
            LocalDate date = LocalDate.parse(text, DateTimeFormatter.ISO_LOCAL_DATE);
            return date.atStartOfDay(ZoneOffset.UTC).toInstant();
        } catch (DateTimeParseException ignored) {}

        // 4. Try LocalDateTime without timezone (e.g. 2026-08-16T10:00:00 or 2026-08-16 10:00:00)
        try {
            String isoDateTime = text.replace(" ", "T");
            LocalDateTime ldt = LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            return ldt.toInstant(ZoneOffset.UTC);
        } catch (DateTimeParseException ignored) {}

        // 5. Try OffsetDateTime (e.g. 2026-08-16T10:00:00+05:30)
        try {
            return OffsetDateTime.parse(text).toInstant();
        } catch (DateTimeParseException ignored) {}

        throw new IllegalArgumentException("Unable to parse date string: '" + text + "' to Instant");
    }
}
