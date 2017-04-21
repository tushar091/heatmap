package com.example.tushar.greedygames;

import java.util.List;

/**
 * Created by tushar on 19/4/17.
 */

public class StringUtils {
    public static String toString(List<String> stringList) {
        return toString(stringList, ",");
    }

    public static String toString(List<String> stringList, String separator) {
        String strings = "";
        if (stringList != null && stringList.size() > 0) {
            strings = stringList.get(0);
            for (int index = 1; index < stringList.size(); index++) {
                strings += separator + stringList.get(index);
            }
        }
        return strings;
    }
}
