package com.sansarip.st8m8;

import java.io.IOException;

import static com.sansarip.st8m8.Utilities.execCmd;

public class Parse {
    public static void parse(String content) {
        String s = "";
        try {
            s = execCmd(String.format("./bb.sh \"%s\"", content));
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("S: " + s);
    }
}
