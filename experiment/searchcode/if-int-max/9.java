package org.nutz.lang.random;

/**
 * @author zozohtnt
 * @author wendal(wendal1985@gmail.com)
 */
public class StringGenerator {

    /**
     * 
     * @param max
     *            ????0
     */
    public StringGenerator(int max) {
        maxLen = max;
        minLen = 1;
    }

    /**
     * 
     * @param min
     *            ????0
     * @param max
     *            ?????min
     */
    public StringGenerator(int min, int max) {
        maxLen = max;
        minLen = min;
    }

    /**
     * min length of the string
     */
    private int maxLen;

    /**
     * max length of the string
     */
    private int minLen;

    /**
     * 
     * @param min
     *            ????0
     * @param max
     *            ?????min
     */
    public void setup(int min, int max) {
        minLen = min;
        maxLen = max;
    }

    /**
     * ?????max?min???,???????.
     * <p/>
     * ?max?min??0,???null
     * 
     * @return ??????
     */
    public String next() {
        if (maxLen <= 0 || minLen <= 0 || minLen > maxLen)
            return null;
        char[] buf = new char[R.random(minLen, maxLen)];
        for (int i = 0; i < buf.length; i++)
            buf[i] = CharGenerator.next();
        return new String(buf);
    }

}

