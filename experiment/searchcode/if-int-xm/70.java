public String getContent(XMessage xm) {
final int msgType = xm.getType();
if(msgType == XMessage.TYPE_VOICE){
return LocalAvatar.FriendVerify;
}else{
final int fromType = xm.getFromType();
if(fromType == XMessage.FROMTYPE_GROUP){

