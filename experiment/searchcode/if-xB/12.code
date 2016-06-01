public static XBProcessServer create(String applyCode,HttpServletRequest request){
XBProcessServer xbProcessServer = null;
if(applyCode.equals(Global.XB_001)){ //物箱信息注册同步
}else if(applyCode.equals(Global.XB_002)){//上传同步交易信息
xbProcessServer = (XBProcessServer) SpringTool.getBean(request, &quot;XBProcessTransactionServer&quot;);

