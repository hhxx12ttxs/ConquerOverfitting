import newtonERP.orm.field.type.FieldInt;
import newtonERP.viewers.viewerData.ListViewerData;

/**
* Entité PayableEmployee du module finances: représente les employés/périodes
public Fields initFields() throws Exception
{
Vector<Field<?>> fieldsInit = new Vector<Field<?>>();
FieldInt primaryKey = new FieldInt(&quot;Numéro&quot;, getPrimaryKeyName());

