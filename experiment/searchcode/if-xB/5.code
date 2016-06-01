* 默认的配置项，全局唯一。在没有设定临时配置项时，以此配置为主。
*/
private static XBConfig globalXbConfig = XBConfig.newXBConfig();
protected static XBConfig getXbConfig() {
// 先查看是否有临时配置项
Integer current = count.get();
if (null == current || 0 == current.intValue()) {

