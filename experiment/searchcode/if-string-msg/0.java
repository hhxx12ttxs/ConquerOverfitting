private String MsgState;
/**
* 是否读取（针对离线消息和文件）
*/
private String ifRead;

private String MsgTime;

public ChatMessage() {
public ChatMessage(String msgFrom, String msgTo, String msgBody, String msgPath, String msgType, String msgState,
String ifRead, String msgTime) {
super();
MsgFrom = msgFrom;

