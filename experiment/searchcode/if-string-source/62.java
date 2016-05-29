import org.springframework.core.convert.converter.Converter;

public class StringTrimConverter implements Converter<String, String> {
@Override
public String convert(String source) {
if(source==null){
return null;
}
return source.trim();
}
}

