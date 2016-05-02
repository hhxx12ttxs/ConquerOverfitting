package shoganai.hashdemo;

import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Builds perfect spatial hash.
 *
 * TODO: remove flag table?
 *
 * @author kosuke
 */
public class HashBuilder {

    //---
    // Static (package-private functions for tests).
    //---
    private static final java.util.Random RND = new java.util.Random();
    private static final Logger LOG = Logger.getLogger(
            HashBuilder.class.getSimpleName());
    /**
     * Number of attempts for offset table construction.
     */
    private static int MAX_ATTEMPTS = 100;

    /**
     * Tables returned from HashBuilder.build().
     */
    public static class Result {

        private byte[] offsetTable;
        private int[] hashTable;

        public byte[] offsetTable() {
            return offsetTable;
        }

        public int[] hashTable() {
            return hashTable;
        }
    }

    /**
     * Returns greatest common devisor.
     */
    static int gcd(int m, int n) {
        if (0 == n) {
            return m;
        }
        return gcd(n, m % n);
    }

    /**
     * Fills array with the specified value.
     */
    static void fill(int[] array, int value) {
        for (int i = 0; i < array.length; ++i) {
            array[i] = value;
        }
    }

    /**
     * Swaps elements i, j in the respective arrays.
     */
    static void swap(int i, int j, int[] array, int[] array2) {
        if (i == j) {
            return;
        }
        int temp = array[i];
        array[i] = array[j];
        array[j] = temp;
        temp = array2[i];
        array2[i] = array2[j];
        array2[j] = temp;
    }

    /**
     * Comb sort(large to small).
     */
    static void sort(int from, int to, int[] ii, int[] ii2) {
        int size = to - from + 1;
        int h = size * 10 / 13;
        for (;;) {
            boolean swapped = false;
            for (int i = from; i + h <= to; ++i) {
                if (ii[i] < ii[i + h]) {
                    swap(i, i + h, ii, ii2);
                    swapped = true;
                }
            }
            if (h != 1) {
                h = h * 10 / 13;
            } else if (!swapped) {
                break;
            }
        }
    }

    /**
     * Counts number of non-transparent elements.
     */
    private static int countElements(int[] buf) {
        int n = 0;
        for (int i : buf) {
            if (0 != (i >> 24)) {
                ++n;
            }
        }
        LOG.log(Level.INFO, "Number of elements: {0}", n);
        return n;
    }

    /**
     * Calculates hash table width(m_).
     */
    private static int calculateTableWidth(int elementCount) {
        int m_ = (int) Math.ceil(Math.sqrt(elementCount));
        if (m_ > 256) {
            m_ = (int) Math.ceil(Math.sqrt(1.01 * elementCount));
        }
        LOG.log(Level.INFO, "Hash table width(m_): {0}", m_);
        return m_;
    }

    /**
     * Returns index of one of four neighbors in offset table. If there's none
     * assigned prior to this call, returns -1.
     */
    private static int findNeighbor(int q, int r_, boolean[] flags) {
        int qx = q % r_;
        int qy = q / r_;
        if (qx > 0) {
            int a = qx - 1 + qy * r_;
            if (flags[a]) {
                return a;
            }
        }
        if (qy > 0) {
            int a = qx + (qy - 1) * r_;
            if (flags[a]) {
                return a;
            }
        }
        if (qx < r_ - 1) {
            int a = qx + 1 + qy * r_;
            if (flags[a]) {
                return a;
            }
        }

        if (qy < r_ - 1) {
            int a = qx + (qy + 1) * r_;
            if (flags[a]) {
                return a;
            }
        }
        return -1;
    }
    //---
    // Instance
    //--- 
    private final int width;
    private final int height;
    private final int[] buf;
    private int elementCount;
    private int m_;
    private Result result;

    public HashBuilder(BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();
        buf = image.getRGB(0, 0, width, height, null, 0, width);
    }

    /**
     * Tries to build hash table. Returns null if failed.
     */
    public Result build() {
        result = new Result();
        //Prepare storages
        elementCount = countElements(buf);
        m_ = calculateTableWidth(elementCount);
        assert m_ * m_ > elementCount; //Otherwise nonsense
        result.hashTable = new int[m_ * m_];//final result
        int[] elements = new int[elementCount];//unique elements
        //Fast r construction
        int r_ = (int) Math.ceil(Math.sqrt(elementCount / (2 * 2)));//2D
        for (int i = 0; i < MAX_ATTEMPTS; ++i) {
            int mod = m_ % r_;
            while (mod == 0 || mod == 1 || mod == r_ - 1 || gcd(r_, m_) != 1) {
                mod = m_ % ++r_;
            }
            if (bulidOffsetTable(elements, r_)) {
                return result;
            } else {
                //Clear hashTable
                fill(result.hashTable, 0);
                ++r_;//Or stay there for a couple more(< 5).
            }
        }
        return null;
    }

    private boolean bulidOffsetTable(int[] elements, int r_) {
        LOG.log(Level.INFO, "Building offset table with  r_= {0}.", r_);
        //1. Count occurences on q
        int[] qCountList = new int[r_ * r_]; //counter
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int p = i + j * width;
                if (0 != (buf[p] >> 24)) {
                    int q = i % r_ + ((j % r_) * r_);//h1
                    ++qCountList[q];
                }
            }
        }
        int[] qIndexList = new int[r_ * r_];//indices
        for (int q = 0; q < qIndexList.length; ++q) {
            qIndexList[q] = q;
        }
        //2. Sort qIndexList with qCountList; q: large -> small
        sort(0, r_ * r_ - 1, qCountList, qIndexList);
        //2a. Invert qIndexList to map q -> k in both qCountList and qIndexList.
        int[] qInverseList = new int[r_ * r_];
        for (int k = 0; k < qIndexList.length; ++k) {
            qInverseList[qIndexList[k]] = k; // or qCountList[i]
        }
        //3. Convert qCountList ->: qRow, an "offset array" in a prefix sum fashion.
        int[] qRow = new int[r_ * r_ + 1];
        int total = qCountList[0];
        qCountList[0] = qRow[0] = 0;
        for (int i = 1; i < qCountList.length; ++i) {
            int add = qCountList[i];
            qCountList[i] = qRow[i] = total;
            total += add;
        }
        qRow[qCountList.length] = qCountList.length;//terminator element
        //4. Fill element buckets
        for (int i = 0; i < width; ++i) {
            for (int j = 0; j < height; ++j) {
                int p = i + j * width;
                if (0 != (buf[p] >> 24)) {
                    int q = i % r_ + ((j % r_) * r_);//h1
                    int k = qInverseList[q];
                    elements[qCountList[k]] = p;
                    qCountList[k]++;
                }
            }
        }
        //5. Build offset table.
        boolean[] flags = new boolean[r_ * r_];//all false
        byte[] offsetTable = new byte[r_ * r_ * 2];//2D
        if (findOffsetTable(offsetTable, flags, elements, qIndexList, qRow, r_)) {
            result.offsetTable = offsetTable;
            return true;
        }
        return false;
    }

    private boolean findOffsetTable(byte[] offsetTable, boolean[] flags,
            int[] elements, int[] qIndexList, int[] qRow, int r_) {
        byte[] offset = {0, 0};
        for (int i = 0; i < qIndexList.length; ++i) {
            int start = qRow[i];
            int next = qRow[i + 1];
            int q = qIndexList[i];
            byte offsetX, offsetY;//Initial offset value
            //Try neighbors to get coherent offset
            int n = findNeighbor(q, r_, flags);
            if (n >= 0) {
                offsetX = offsetTable[n * 2];
                offsetY = offsetTable[n * 2 + 1];
            } else {
                int max = m_ < 255 ? m_ : 255;
                offsetX = (byte) RND.nextInt(max);
                offsetY = (byte) RND.nextInt(max);
            }
            if (search(elements, start, next, offsetX, offsetY, offset)) {
                offsetTable[q * 2 + 0] = offset[0];
                offsetTable[q * 2 + 1] = offset[1];
                flags[q] = true;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * Main loop.
     */
    private boolean search(int[] pp, int start, int next,
            byte offsetX, byte offsetY, byte[] result) {
        //Search
        int max = m_ < 255 ? m_ : 255;
        for (short bx = 0; bx < max; ++bx) {
            for (short by = 0; by < max; ++by) {
                short x = (short) ((offsetX + bx) & 0xff);
                short y = (short) ((offsetY + by) & 0xff);
                if (testOffset(pp, start, next, x, y)) {
                    result[0] = (byte) x;
                    result[1] = (byte) y;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean testOffset(int[] pp, int start,
            int next, short offsetX, short offsetY) {
        for (int j = start; j < next; ++j) {
            int x = pp[j] % width;
            int y = pp[j] / width;
            int h = ((x % m_) + offsetX) % m_
                    + (((y % m_) + offsetY) % m_) * m_;
            if (result.hashTable[h] == 0) {
                //ok
            } else {
                //failed. already assigned.
                return false;
            }
        }
        //success, mark it.
        for (int j = start; j < next; ++j) {
            int x = pp[j] % width;
            int y = pp[j] / width;
            int h = ((x % m_) + offsetX) % m_
                    + (((y % m_) + offsetY) % m_) * m_;
            result.hashTable[h] = buf[pp[j]];
        }
        return true;
    }
}

