import org.apache.lucene.store.FSDirectory;

/**
* 去重索引
*
* @author Luopy
*
*/
public class ReplaceIndex {
/**
* 单例搜索类
*/
private static ReplaceIndex singleRepaceIndex = null;
* 更新索引库
*/
public Thread trc = null;

public static ReplaceIndex GetInstance() {

if (getSingleReplaceIndex() == null) {

