Integer initialIndex = new Integer(request.getParameter(&quot;initial_index_freira&quot;));
if(initialIndex != 0){
initialIndex = initialIndex-1;
initialIndex = initialIndex*10;
fraternidadeBean = fraternidadeDao.getFraternidade(idFraternidade);
freiras = freiraDao.getAllFreiraBeanSemAgregacao();
for (int i = initialIndex; i < initialIndex + 10; i++) {

