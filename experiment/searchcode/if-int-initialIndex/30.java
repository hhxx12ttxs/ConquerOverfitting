String nextPage = &quot;lista_formandas_para_permutar.jsp&quot;;
Integer initialIndex = new Integer(request.getParameter(&quot;initial_index_formanda&quot;));
if(initialIndex != 0){
formandas = formandaDao.getAllFormandaBeanAtivas();
for (int i = initialIndex; i < initialIndex + 10; i++) {
if(i<formandas.size()) formandaspaginada.add(formandas.get(i));

