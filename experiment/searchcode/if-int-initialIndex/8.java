Integer initialIndex = new Integer(request.getParameter(&quot;initial_index&quot;));
if(initialIndex != 0){
initialIndex = initialIndex-1;
initialIndex = initialIndex*10;
fraternidades = fraternidadeDao.getAllFraternidade();
for (int i = initialIndex; i < initialIndex + 10; i++) {

