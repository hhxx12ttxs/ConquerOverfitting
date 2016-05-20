package wwmm.pubcrawler.crawlers;

import java.util.Comparator;

// From:
// http://www.coderanch.com/t/328108/java/java/Ordering-AlphaNumeric-No-ascending-Order
public class IntuitiveComparator implements Comparator<String> {

    private String str1, str2;
    private int pos1, pos2, len1, len2;

    @Override
    public int compare(final String s1, final String s2)
    {
        str1 = s1;
        str2 = s2;
        len1 = str1.length();
        len2 = str2.length();
        pos1 = pos2 = 0;

        if (len1 == 0)
        {
            return len2 == 0 ? 0 : -1;
        }
        else if (len2 == 0)
        {
            return 1;
        }

        while (pos1 < len1 && pos2 < len2)
        {
            final char ch1 = str1.charAt(pos1);
            final char ch2 = str2.charAt(pos2);
            int result = 0;

            if (Character.isDigit(ch1))
            {
                result = Character.isDigit(ch2) ? compareNumbers() : -1;
            }
            else if (Character.isLetter(ch1))
            {
                result = Character.isLetter(ch2) ? compareOther(true) : 1;
            }
            else
            {
                result = Character.isDigit(ch2) ? 1
                        : Character.isLetter(ch2) ? -1
                        : compareOther(false);
            }

            if (result != 0)
            {
                return result;
            }
        }

        return len1 - len2;
    }

    private int compareNumbers()
    {
        int delta = 0;
        int zeroes1 = 0, zeroes2 = 0;
        char ch1 = (char)0, ch2 = (char)0;

        // Skip leading zeroes, but keep a count of them.
        while (pos1 < len1 && (ch1 = str1.charAt(pos1++)) == '0')
        {
            zeroes1++;
        }
        while (pos2 < len2 && (ch2 = str2.charAt(pos2++)) == '0')
        {
            zeroes2++;
        }

        // If one sequence contains more significant digits than the
        // other, it's a larger number.  In case they turn out to have
        // equal lengths, we compare digits at each position; the first
        // unequal pair determines which is the bigger number.
        while (true)
        {
            final boolean noMoreDigits1 = (ch1 == 0) || !Character.isDigit(ch1);
            final boolean noMoreDigits2 = (ch2 == 0) || !Character.isDigit(ch2);

            if (noMoreDigits1 && noMoreDigits2)
            {
                return delta != 0 ? delta : zeroes1 - zeroes2;
            }
            else if (noMoreDigits1)
            {
                return -1;
            }
            else if (noMoreDigits2)
            {
                return 1;
            }
            else if (delta == 0 && ch1 != ch2)
            {
                delta = ch1 - ch2;
            }

            ch1 = pos1 < len1 ? str1.charAt(pos1++) : (char)0;
            ch2 = pos2 < len2 ? str2.charAt(pos2++) : (char)0;
        }
    }

    private int compareOther(final boolean isLetters)
    {
        char ch1 = str1.charAt(pos1++);
        char ch2 = str2.charAt(pos2++);

        if (ch1 == ch2)
        {
            return 0;
        }

        if (isLetters)
        {
            ch1 = Character.toUpperCase(ch1);
            ch2 = Character.toUpperCase(ch2);
            if (ch1 != ch2)
            {
                ch1 = Character.toLowerCase(ch1);
                ch2 = Character.toLowerCase(ch2);
            }
        }

        return ch1 - ch2;
    }

}

