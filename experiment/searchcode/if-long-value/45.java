public class LongField extends Field<LongField>
{
Long longValue;
public LongField(Long fieldValue)
{
longValue = fieldValue;
// TODO Auto-generated constructor stub
if(longValue == null) {
return null;
} else {
return longValue.toString();
}
}

@Override

