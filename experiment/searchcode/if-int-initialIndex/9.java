Integer initialIndex = new Integer(request.getParameter(&quot;initial_index_freira&quot;));
if(initialIndex != 0){
freiras = freiraDao.getAllFreiraBeanComAgregacao();
for (int i = initialIndex; i < initialIndex + 10; i++) {

