package com.test;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Created by martinliu on 15/7/21.
 */
public class Concurrent {
    private static String getFirstResult(String question, List<String> engines){
        AtomicReference<String> result = new AtomicReference<String>();
        for (String base : engines){
            String url = base + question;
        }
        return null;
    }
}
