package com.concourse.tools;

import java.util.UUID;

public class StringTools {

    /**
     * Length must be <= 32
     * @param length
     * @return
     */
    public static String generateID(int length){
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, Math.min(32, length));
    }

    public static void main(String[] args) {
        System.out.println(generateID(32));
    }
}
