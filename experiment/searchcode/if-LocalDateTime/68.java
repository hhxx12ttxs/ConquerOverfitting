import java.time.LocalDateTime;

public class LocalDateTimeStringSerializer extends JsonSerializer<LocalDateTime> {
@Override
public void serialize(LocalDateTime localDateTime, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {

