public class LocalDateAdapter extends XmlAdapter<String, LocalDate> {

@Override
public String marshal(LocalDate v) throws Exception {
if (v == null) return null;
public LocalDate unmarshal(String v) throws Exception {
if (v == null) return null;
return new LocalDate(v);
}

}

