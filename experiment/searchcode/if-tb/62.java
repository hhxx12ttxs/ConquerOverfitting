em.getTransaction().begin();

if (tbProfessor.getProCodigo() == null) {
//valida se o usuário já foi cadastrado no sistema
for (TbAula tbAulaCollectionOldTbAula : tbAulaCollectionOld) {
if (!tbAulaCollectionNew.contains(tbAulaCollectionOldTbAula)) {
if (illegalOrphanMessages == null) {

