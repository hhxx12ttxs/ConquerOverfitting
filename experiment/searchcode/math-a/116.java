//==============================================================================
// RemoteDCNData.java
// Copyright (c) 2000 WAP Solutions Ltd.
//==============================================================================

package wsl.fw.datasource;

import java.io.Serializable;

//------------------------------------------------------------------------------
/**
 * A Serializable data holder used by the DataManager to send remote data change
 * notifications. Any notification of type DataManager.NT_DATA_CHANGE should
 * send a RemoteDCNData as its notification data.
 */
public class RemoteDCNData implements Serializable
{
    // version tag
    private final static String _ident = "$Date: 2002/06/11 23:11:42 $  $Revision: 1.1.1.1 $ "
        + "$Archive: /Framework/Source/wsl/fw/datasource/RemoteDCNData.java $ ";

    // attributes
    /** A string identifying the listener belonging to the sending DataManager,
     * used to determine if a DM is recieving a notification it originated */
    public String                 _sendingDMListener;

    /** the data change notification from the source DataManager */
    public DataChangeNotification _dcn;

    //--------------------------------------------------------------------------
    /**
     * Constructor.
     * @param sendingDMListener, string identifying the listener belonging to
     *   the sending DataManager.
     * @param dcn, the data change notification from the source DataManager.
     */
    public RemoteDCNData(String sendingDMListener,
        DataChangeNotification dcn)
    {
        _sendingDMListener = sendingDMListener;
        _dcn = dcn;
    }
}

//==============================================================================
// end of file RemoteDCNData.java
//==============================================================================



