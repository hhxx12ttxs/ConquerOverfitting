package net.kotek.jdbm;


import org.junit.After;
import org.junit.Before;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * JUnit test case which provides JDBM specific staff
 */
abstract public class JdbmTestCase {


    File fileName;

    File testDir;

    Storage recman;

     @Before
     public void setUp() throws Exception {
        testDir = new File(new File(System.getProperty("java.io.tmpdir")), "testdb");
        testDir.mkdirs();
        fileName = new File(testDir.getPath()+"test"+Math.random());

        recman = openRecordManager();
    }

    protected Storage openRecordManager() {
        return new StorageDirect(fileName,true,false, false,false);
    }

    @After
    public void tearDown() throws Exception {
        recman.close();
        for(File f:testDir.listFiles()){
            if(f!=null && !f.delete())f.deleteOnExit();
        }
    }


    void reopenStore() {
        recman.close();
        recman = openRecordManager();
    }


    DataInput2 swap(DataOutput2 d){
        byte[] b = d.copyBytes();
        return new DataInput2(ByteBuffer.wrap(b),0);
    }


    int countIndexRecords(){
        int ret = 0;
        final long indexFileSize = recman.index.buffers[0].getLong(StorageDirect.RECID_CURRENT_INDEX_FILE_SIZE*8);
        for(int pos = StorageDirect.INDEX_OFFSET_START * 8;
            pos<indexFileSize;
            pos+=8){
            if(0!=recman.index.getLong(pos)){
                ret++;
            }
        }
        return ret;
    }

    long getIndexRecord(long recid){
        return recman.index.getLong(recid*8);
    }

    List<Long> getLongStack(long recid){
        ArrayList<Long> ret =new ArrayList<Long>();

        long pagePhysid = recman.index.getLong(recid*8) & StorageDirect.PHYS_OFFSET_MASK;

        ByteBuffer dataBuf = recman.phys.buffers[((int) (pagePhysid / ByteBuffer2.BUF_SIZE))];

        while(pagePhysid!=0){
            final byte numberOfRecordsInPage = dataBuf.get((int) (pagePhysid% ByteBuffer2.BUF_SIZE));

            for(int rec = numberOfRecordsInPage; rec>0;rec--){
                final long l = dataBuf.getLong((int) (pagePhysid% ByteBuffer2.BUF_SIZE+ rec*8));
                ret.add(l);
            }

            //read location of previous page
            pagePhysid = dataBuf.getLong((int)(pagePhysid% ByteBuffer2.BUF_SIZE)) & StorageDirect.PHYS_OFFSET_MASK;
        }


        return ret;
    }

    int readUnsignedShort(ByteBuffer buf, long pos) throws IOException {
        return (( (buf.get((int) pos) & 0xff) << 8) |
                ( (buf.get((int) (pos+1)) & 0xff)));
    }


    final List<Long> arrayList(long... vals){
        ArrayList<Long> ret = new ArrayList<Long>();
        for(Long l:vals){
            ret.add(l);
        }
        return ret;
    }

    final Map<Long, Integer> getDataContent(){
        Map<Long,Integer> ret = new TreeMap<Long, Integer>();
        final long indexFileSize = recman.index.buffers[0].getLong(StorageDirect.RECID_CURRENT_INDEX_FILE_SIZE*8);
        for(long recid = StorageDirect.INDEX_OFFSET_START ;
            recid*8<indexFileSize;
            recid++){
            Integer val = recman.recordGet(recid, Serializer.HASH_DESERIALIZER);
            ret.put(recid, val);
        }
        return ret;
    }


}

