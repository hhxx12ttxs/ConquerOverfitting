TbGraduacao tbGraduacaoGraCodigo = tbExercicio.getTbGraduacaoGraCodigo();

if (tbGraduacaoGraCodigo != null) {
tbGraduacaoGraCodigo = em.getReference(tbGraduacaoGraCodigo.getClass(), tbGraduacaoGraCodigo.getGraCodigo());
tbGraduacaoGraCodigo = em.getReference(TbGraduacao.class, tbExercicio.getTmptbGraduacaoGraCodigo());
tbExercicio.setTbGraduacaoGraCodigo(tbGraduacaoGraCodigo);
}
if (tbExercicio.getExeCodigo() == null) {

