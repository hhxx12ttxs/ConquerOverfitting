public void create(TbCurso tbCurso) throws PreexistingEntityException, Exception {
if (tbCurso.getTbGraduacaoCollection() == null) {
tbCurso.setTbGraduacaoCollection(new ArrayList<TbGraduacao>());
}
if (tbCurso.getTbAlunoCollection() == null) {
tbCurso.setTbAlunoCollection(new ArrayList<TbAluno>());

