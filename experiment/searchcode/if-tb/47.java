List<TbCollect> tbUserCollectList = tbCollectDAO.findByExample(tbCollect);
if(tbUserCollectList.size() > 0){
if(tbUserCollectList.size() == 1){
tbCollect = tbUserCollectList.get(0);
}else{
tbCollect = null;

