@RequestMapping(value = &quot;/createAndUpdateMiddle&quot;,method = RequestMethod.GET)
public List<predictMmMiddle> createAndUpdateMmMiddle(predictMmMiddleForm mmMiddleForm) {
long recordId=mmMiddleForm.getId();//获取记录ID
predictMmMiddle mmMiddle= null;
if(recordId==0){

