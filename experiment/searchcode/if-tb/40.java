public void create(TbUsuarios tbUsuarios) {
if (tbUsuarios.getTbUsuarioRecorridoList() == null) {
tbUsuarioRecorridoListTbUsuarioRecorrido = em.merge(tbUsuarioRecorridoListTbUsuarioRecorrido);
if (oldIdUsuarioOfTbUsuarioRecorridoListTbUsuarioRecorrido != null) {

