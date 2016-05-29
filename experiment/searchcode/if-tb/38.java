return emf.createEntityManager();
}

public void create(TbActividades tbActividades) {
if (tbActividades.getTbRecorridoActividadList() == null) {
tbRecorridoActividadListTbRecorridoActividad.setIdActividad(tbActividades);
tbRecorridoActividadListTbRecorridoActividad = em.merge(tbRecorridoActividadListTbRecorridoActividad);
if (oldIdActividadOfTbRecorridoActividadListTbRecorridoActividad != null) {

