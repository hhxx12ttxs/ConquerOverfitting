package open.dolphin.stampbox;

import java.util.List;
import javax.swing.DropMode;
import open.dolphin.infomodel.IInfoModel;
import open.dolphin.tr.StampTreeTransferHandler;

/**
 * UserStampBox
 *
 * @author Kazushi Minagawa, Digital Globe, Inc.
 */
public class UserStampBox extends AbstractStampBox {
    
    private static final String BOX_INFO = "個人用スタンプボックス";
    
    /** テキストスタンプのタブ番号 */
    private int textIndex;
    
    /** パススタンプのタブ番号 */
    private int pathIndex;
    
    /** ORCA セットのタブ番号 */
    private int orcaIndex;

    /**
     * StampBox を構築する。
     */
    @Override
    protected void buildStampBox() {
        
        try {
            // Build stampTree
//masuda^
            StampTreeXmlParser parser = new StampTreeXmlParser(StampTreeXmlParser.MODE.DEFAULT);
            List<StampTree> userTrees = parser.parse(stampTreeModel.getTreeXml());
//masuda$
            stampTreeModel.setTreeXml(null);
            stampTreeModel.setTreeBytes(null);
            
            // StampTreeへ設定するPopupMenuとTransferHandlerを生成する
            StampTreePopupAdapter popAdapter = new StampTreePopupAdapter();
            StampTreeTransferHandler transferHandler = new StampTreeTransferHandler();
            
            // StampBox(TabbedPane) へリスト順に格納する
            // 一つのtabへ一つのtreeが対応
            int index = 0;
            for (StampTree stampTree : userTrees) {
                stampTree.setUserTree(true);
                stampTree.setTransferHandler(transferHandler);
                stampTree.setDropMode(DropMode.INSERT);         // INSERT
                stampTree.setStampBox(getContext());
                StampTreePanel treePanel = new StampTreePanel(stampTree);
                this.addTab(stampTree.getTreeName(), treePanel);
                // Text、Path、ORCA のタブ番号を保存する
                String entity = stampTree.getEntity();
                if (entity != null) {
                    switch (stampTree.getEntity()) {
                        case IInfoModel.ENTITY_TEXT:
                            textIndex = index;
                            stampTree.addMouseListener(popAdapter);
                            break;
                        case IInfoModel.ENTITY_PATH:
                            pathIndex = index;
                            stampTree.addMouseListener(popAdapter);
                            break;
                        case IInfoModel.ENTITY_ORCA:
                            orcaIndex = index;
                            break;
                        default:
                            stampTree.addMouseListener(popAdapter);
                            break;
                    }
                } else {
                    stampTree.addMouseListener(popAdapter);
                }
                
                index++;
            }
            
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }
    }
	
    /**
     * 引数のタブ番号に対応するStampTreeにエディタから発行があるかどうかを返す。
     * @param index タブ番号
     * @return エディタから発行がある場合に true 
     */
    @Override
    public boolean isHasEditor(int index) {
//masuda^
        //return (index == textIndex || index == pathIndex || index == orcaIndex) ? false : true;
        return !(index == pathIndex || index == orcaIndex);
//masuda$
    }

    @Override
    public void setHasNoEditorEnabled(boolean b) {
//masuda
        //this.setEnabledAt(textIndex, b);
        this.setEnabledAt(pathIndex, b);
        this.setEnabledAt(orcaIndex, b);
    }
    
    @Override
    public String getInfo() {
        return BOX_INFO;
    }
}
