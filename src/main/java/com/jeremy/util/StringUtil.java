package com.jeremy.util;

import java.util.regex.Pattern;

/**
 * Created by Jeremy on 24/03/2017.
 */
public class StringUtil {

    public static boolean isEmpty(String str) {
        return (str == null) || ("".equals(str.trim()));
    }

    public static boolean startsWith(String str, String regexp) {
        return Pattern.compile(regexp).matcher(str).find();
    }
}
