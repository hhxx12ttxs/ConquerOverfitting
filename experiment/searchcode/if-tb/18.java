TbProfessor tbProfessorProCodigo = tbExames.getTbProfessorProCodigo();

if (tbProfessorProCodigo != null) {
TbGraduacao tbGraduacaoGraCodigo = tbExames.getTbGraduacaoGraCodigo();
if (tbGraduacaoGraCodigo != null) {
tbGraduacaoGraCodigo = em.getReference(tbGraduacaoGraCodigo.getClass(), tbGraduacaoGraCodigo.getGraCodigo());

