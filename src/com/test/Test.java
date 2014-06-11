package com.test;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Created by martinliu on 14-4-24.
 */
public class Test {
    private static SimpleDateFormat getDateFormat(String pattern, TimeZone timezone, Locale locale) {
        SimpleDateFormat sdf = new SimpleDateFormat(pattern, locale);
        sdf.setTimeZone(timezone);
        return sdf;
    }
    public static void main(String[] args) throws ParseException {

//        TimeZone tz = TimeZone.getTimeZone("America/Los_Angeles");
//
//        SimpleDateFormat sdf = getDateFormat("yyyy-MM-dd HH:mm:ss", tz, Locale.ENGLISH);
//        String d = sdf.format(new Date());
//        System.out.println(d);

        int b = 7;
        F f = (int a) ->
            a + b + 1;
        System.out.println(f.cal(3));
        Integer i1 = 111;
        Integer i2 = 111;
        Integer i3 = 1111;
        Integer i4 = 1111;
        System.out.println(i1 == i2);
        System.out.println(i3 == i4);

        FF ff = T::new;
        ff.ff("this is a test");
    }
}

@FunctionalInterface
interface F {
    int cal(int a);
    default void t(String s){
        System.out.println(s);
    }
}

@FunctionalInterface
interface FF {
    void ff(String s);
}

class T implements F {
    T(String s) {
      this.t(s);
    }
    @Override
    public int cal(int a) {
        return a;
    }
}
