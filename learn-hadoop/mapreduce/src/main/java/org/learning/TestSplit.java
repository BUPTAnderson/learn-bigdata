package org.learning;

import org.apache.hadoop.util.StringUtils;

import java.util.ArrayList;

/**
 * Created by anderson on 17-7-10.
 */
public class TestSplit
{
    public static final char COMMA = ',';
    public static final char ESCAPE_CHAR = '\\';

    public static String[] split(String str) {
        return split(str, ESCAPE_CHAR, COMMA);
    }

    public static String[] split(
            String str, char escapeChar, char separator) {
        if (str == null) {
            return null;
        }
        ArrayList<String> strList = new ArrayList<String>();
        StringBuilder split = new StringBuilder();
        int index = 0;
        while ((index = findNext(str, separator, escapeChar, index, split)) >= 0) {
            ++index; // move over the separator for next search
            strList.add(split.toString());
            split.setLength(0); // reset the buffer
        }
        strList.add(split.toString());
        // remove trailing empty split(s)
        int last = strList.size(); // last split
        while (--last >= 0 && "".equals(strList.get(last))) {
            strList.remove(last);
        }
        return strList.toArray(new String[strList.size()]);
    }

    public static int findNext(String str, char separator, char escapeChar,
            int start, StringBuilder split) {
        int numPreEscapes = 0;
        for (int i = start; i < str.length(); i++) {
            char curChar = str.charAt(i);
            if (numPreEscapes == 0 && curChar == separator) { // separator
                return i;
            } else {
                split.append(curChar);
                numPreEscapes = (curChar == escapeChar)
                        ? (++numPreEscapes) % 2
                        : 0;
            }
        }
        return -1;
    }

    public static void main(String[] args)
    {
        String inputPath = "/test/input1, /test/input2";
        String[] inputs = split(inputPath);
        for (String input : inputs) {
            System.out.println("--" + StringUtils.unEscapeString(input) + "--");
        }
    }
}
