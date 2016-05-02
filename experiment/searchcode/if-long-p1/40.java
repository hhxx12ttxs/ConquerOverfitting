/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package wa.util;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import wa.common.Context;
import wa.common.TaggedWord;
import wa.sejong.FileNameExchange;
import wa.sejong.IndexInfo;
import wa.sejong.SejongUtility;
import wa.sejong.WordPosition;
import wa.sejong.TaggedWordUnit;
/**
 *
 * @author lagrion
 */
public class RawScanner {
    private Map<String, Long> corpusIndex;
    private static final String INDEX_NAME_L1 = "dic/corpus_index";
    private int nextWordPositionIndex;
    private WordPosition currentWordPosition;
    private long lastFilePointer;
    private IndexInfo index;
    private String key;
    public RawScanner(String key, Map<String, Long> corpusIndex){
        this.key = key;
        this.corpusIndex = corpusIndex;
        this.index = readIndexInfo(key);
        FileNameExchange.init();
        //this.corpusIndex = corpusIndex;
    }
    public boolean hasNext(){
        if(index == null) return false;
        if(nextWordPositionIndex < index.getWordList().size()) return true;
        return false;
    }
    public TaggedWordUnit next(Context ctx){
        if(hasNext() == false) return null;

        RandomAccessFile corpusRAF = null;
        currentWordPosition = index.getWordList().get(nextWordPositionIndex);
        String fName = FileNameExchange.indexToName(currentWordPosition.getDocumentNum());
        TaggedWordUnit twu = null;
        String line = null;
        try{
            corpusRAF = new RandomAccessFile("dic/pos/new/"+fName, "r");
            corpusRAF.seek(currentWordPosition.getPosition());

            String read = corpusRAF.readLine();
            if(read == null) return null;
            //마지막으로 읽은 file의 위치를 기억
            lastFilePointer = corpusRAF.getFilePointer();
            byte[] bytes = read.getBytes("ISO8859_1");
            line = new String(bytes, "UTF-8");
            twu = SejongUtility.parse(line);

            ctx.setLeftContext(prevLineElements(corpusRAF, currentWordPosition));
            ctx.setCenterContext(twu.getRaw());
            ctx.setRightContext(nextLineElements(corpusRAF, currentWordPosition));
            
            nextWordPositionIndex++;
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            if(corpusRAF != null) try{ corpusRAF.close(); }catch(Exception e){}
        }
        return twu;
    }
    /**
     * 현재 이전 문맥을 읽음, pointer를 변경하지 않는다
     * @return
     */
    private String prevLineElements(RandomAccessFile raf, WordPosition wp) throws IOException{
        StringBuffer sb = new StringBuffer();

        WordPosition temp = new WordPosition();
        temp.setDocumentNum(wp.getDocumentNum());
        temp.setPosition(wp.getPosition());

        long prevP = temp.getPosition() - 250;
        if(prevP < 0) prevP = 0;

        raf.seek(prevP);

        String read = raf.readLine();
        long currentP;
        if(read == null) return null;
        while(true){
            //read line
            read = raf.readLine();
            currentP = raf.getFilePointer();
            if(read == null) break;

            if(currentP >= wp.getPosition()) break;
            
            byte[] bytes = read.getBytes("ISO8859_1");
            String line = new String(bytes, "UTF-8");

            //parse
            TaggedWordUnit twu = SejongUtility.parse(line);
            if(twu == null) break;

            sb.append(twu.getRaw()+ " ");
        }
        //다시 file pointer 위치 지정
        raf.seek(wp.getPosition());

        return sb.toString();
    }
    /**
     * 현재 다음 문맥을 읽음, pointer를 변경하지 않는다
     * @return
     */
    private String nextLineElements(RandomAccessFile raf, WordPosition wp) throws IOException{
        StringBuffer sb = new StringBuffer();

        WordPosition temp = new WordPosition();
        temp.setDocumentNum(wp.getDocumentNum());
        temp.setPosition(wp.getPosition());

        raf.seek(temp.getPosition());

        String read = raf.readLine();
        if(read == null) return null;
        for(int i = 0; i < 3; i++){
            //read line
            read = raf.readLine();
            if(read == null) break;
            
            byte[] bytes = read.getBytes("ISO8859_1");
            String line = new String(bytes, "UTF-8");

            //parse
            TaggedWordUnit twu = SejongUtility.parse(line);
            if(twu == null) break;
            
            sb.append(twu.getRaw()+ " ");
            if(twu != null){
                //마지막이 '.'이면 끝
                List<TaggedWord> twList = twu.getTaggedWordList();
                if(twList.get(twList.size() -1 ).getTag().equals("SF")) break;
            }
        }
        //다시 file pointer 위치 지정
        raf.seek(wp.getPosition());
        return sb.toString();
    }
    /**
     * file pointer를 변경
     * @return
     */
    public TaggedWordUnit skipLine(){
        RandomAccessFile corpusRAF = null;
        String fName = FileNameExchange.indexToName(currentWordPosition.getDocumentNum());
        TaggedWordUnit twu = null;
        String line = null;
        try{
            corpusRAF = new RandomAccessFile("dic/pos/new/"+fName, "r");

            corpusRAF.seek(lastFilePointer);
            String read = corpusRAF.readLine();
            if(read == null) return null;

            //마지막으로 읽은 file의 위치를 기억
            lastFilePointer = corpusRAF.getFilePointer();

            byte[] bytes = read.getBytes("ISO8859_1");
            line = new String(bytes, "UTF-8");
            twu = SejongUtility.parse(line);
        } catch(Exception e){
            e.printStackTrace();
        } finally{
            if(corpusRAF != null) try{ corpusRAF.close(); }catch(Exception e){}
        }
        return twu;
    }
    /**
     * 검색 key의 정보를 index에서 읽음
     * @param key
     * @return
     */
    private IndexInfo readIndexInfo(String key){
        Long p1 = corpusIndex.get(key);
        if(p1 == null) return null;
        RandomAccessFile raf = null;
        IndexInfo i = null;
        try{
            raf = new RandomAccessFile(INDEX_NAME_L1, "r");
            raf.seek(p1);
            String read = raf.readLine();
            if(read == null) return null;

            byte[] bytes = read.getBytes("ISO8859_1");
            String line = new String(bytes, "UTF-8");

             i = parseIndex(line);
        } catch(IOException e){
            e.printStackTrace();
        } finally{
            if(raf != null) try{ raf.close(); }catch(Exception e){}
        }
        return i;
    }
    /**
     * corpus의 한 line을 parsing
     * @param line
     * @return
     */
    private IndexInfo parseIndex(String line){
        String regx = "^(\\S+)\\s(\\d+)\\s";
        Pattern p = Pattern.compile(regx);
        Matcher m = p.matcher(line);

        IndexInfo i = null;
        if(m.find()){
            i = new IndexInfo();
            String key = m.group(1);
            int frequency = Integer.valueOf(m.group(2));

            i.setName(key);
            i.setFrequency(frequency);

            String regx2 = "\\((.*?)\\)";
            Pattern p2 = Pattern.compile(regx2);
            Matcher m2 = p2.matcher(line);
            while(m2.find()){
                String str = m2.group(1);
                String[] sarray = str.split(",");

                if(sarray.length < 2) return null;

                try{
                    WordPosition wp = new WordPosition(Integer.valueOf(sarray[0]), Long.valueOf(sarray[1]));
                    i.add(wp);
                } catch(Exception e){
                    return null;
                }
            }
        }
        return i;
    }
}

