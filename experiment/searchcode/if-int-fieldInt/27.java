import newtonERP.orm.field.type.FieldInt;
import newtonERP.viewers.viewerData.BaseViewerData;
import newtonERP.viewers.viewerData.PromptViewerData;
Vector<Field<?>> fieldList = new Vector<Field<?>>();

FieldInt pKzoneID = new FieldInt(&quot;Numéro&quot;, getPrimaryKeyName());
fieldList.add(pKzoneID);

FieldInt positionX = new FieldInt(&quot;Position X&quot;, &quot;PositionX&quot;);

