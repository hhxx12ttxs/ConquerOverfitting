import newtonERP.orm.field.Fields;
import newtonERP.orm.field.type.FieldInt;
import newtonERP.orm.field.type.FieldText;
public Fields initFields() throws Exception
{
Vector<Field<?>> fieldList = new Vector<Field<?>>();

fieldList.add(new FieldInt(&quot;Numéro&quot;, getPrimaryKeyName()));

