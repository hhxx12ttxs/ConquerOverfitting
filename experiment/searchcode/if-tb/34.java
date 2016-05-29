TbCatPropRefer tbCatPropRefer=getTbCatPropReferByTbCatPropId(tbCatPropId);
if(tbCatPropRefer==null){
tbCatPropRefer=new TbCatPropRefer();
save(tbCatPropRefer);
//检查name
}else{
//检查name
String tbCatPropName=tbCatPropRefer.getTbCatPropName();
if(StringUtils.isBlank(tbCatPropName)){

