import newtonERP.orm.field.type.FieldCurrency;
import newtonERP.orm.field.type.FieldDate;
import newtonERP.orm.field.type.FieldInt;
import newtonERP.viewers.viewerData.ListViewerData;
public Fields initFields() throws Exception
{
Vector<Field<?>> fieldsInit = new Vector<Field<?>>();
FieldInt primaryKey = new FieldInt(&quot;Numéro Transaction&quot;,

