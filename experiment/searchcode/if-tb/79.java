public TbCategoryRefer addTbCategoryRefer(Long tbCategoryId) {
TbCategoryRefer tbCategoryRefer=getTbCategoryReferByTbCId(tbCategoryId);
if(tbCategoryRefer==null){
String catName=tbCategoryRefer.getTbCategoryName();
if(StringUtils.isBlank(catName)){
//查name
catName=getTaobaoName(tbCategoryId);

