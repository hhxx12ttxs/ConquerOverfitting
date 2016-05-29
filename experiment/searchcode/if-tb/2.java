public void create(TbAluno tbAluno) throws PreexistingEntityException, Exception {
if (tbAluno.getTbAulaCollection() == null) {
tbAluno.setTbAulaCollection(new ArrayList<TbAula>());
}
if (tbAluno.getTbPagamentosCollection() == null) {
tbAluno.setTbPagamentosCollection(new ArrayList<TbPagamentos>());

