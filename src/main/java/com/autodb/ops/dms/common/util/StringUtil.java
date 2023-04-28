package com.autodb.ops.dms.common.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wuying on 17/1/13.
 */
public class StringUtil {

    public static  boolean match(String s,String t){
        Pattern p = Pattern.compile(t);
        Matcher m = p.matcher(s);
        return m.find();
    }

    public static String removeBackQuote(String string){

        String backQuote="`";
        Pattern p= Pattern.compile(backQuote);
        Matcher m= p.matcher(string);
        String newString = m.replaceAll("");
        return  newString;
    }

    public static String removeSingleQuote(String string){

        String backQuote="'";
        Pattern p= Pattern.compile(backQuote);
        Matcher m= p.matcher(string);
        String newString = m.replaceAll("");
        return  newString;
    };


    public static String removeDoubleQuote(String string){

        String backQuote="\"";
        Pattern p= Pattern.compile(backQuote);
        Matcher m= p.matcher(string);
        String newString = m.replaceAll("");
        return  newString;
    };


    public static Integer subStringToInteger(String src, String start, String to) {
        return stringToInteger(subString(src, start, to));
    }

    public static String subString(String src, String start, String to) {
        int indexFrom = start == null?0:src.indexOf(start);
        int indexTo = to == null?src.length():src.indexOf(to);
        if(indexFrom >= 0 && indexTo >= 0 && indexFrom <= indexTo) {
            indexFrom += start.length();
            return src.substring(indexFrom, indexTo);
        } else {
            return null;
        }
    }

    public static Integer stringToInteger(String in) {
        if(in == null) {
            return null;
        } else {
            in = in.trim();
            if(in.length() == 0) {
                return null;
            } else {
                try {
                    return Integer.valueOf(Integer.parseInt(in));
                } catch (NumberFormatException var2) {
                    return null;
                }
            }
        }
    }

    public static boolean equals(String a, String b) {
        return a == null?b == null:a.equals(b);
    }

    public static boolean equalsIgnoreCase(String a, String b) {
        return a == null?b == null:a.equalsIgnoreCase(b);
    }

    public static boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    public static int lowerHashCode(String text) {
        if(text == null) {
            return 0;
        } else {
            int h = 0;

            for(int i = 0; i < text.length(); ++i) {
                char ch = text.charAt(i);
                if(ch >= 65 && ch <= 90) {
                    ch = (char)(ch + 32);
                }

                h = 31 * h + ch;
            }

            return h;
        }
    }


    public static void main1(String[] args) {
        System.out.println(removeBackQuote("`sdfds`.`sfsdf`"));
        System.out.println(removeDoubleQuote("\"sdfsdfdsf\""));

    }
}
