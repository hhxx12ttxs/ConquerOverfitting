for (int i=0; i<FIELD_SIZE+2; i++)
for (int j=0; j<FIELD_SIZE+2; j++) {
if (!logicField[i][j].isEmpty &amp;&amp; logicField[i][j].nod != null) logicField[i][j].nod.remove();
for (int i=0; i<FIELD_SIZE+2; i++){
for (int j=0; j<FIELD_SIZE+2; j++){
NodRec rec = logicField[i][j];
if (!rec.isEmpty) this.addActor(rec.nod);

