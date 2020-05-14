package com.sansarip.st8m8;

import org.apache.commons.io.IOUtils;

import java.io.*;

public class Utilities {
    public static File resourceToFile(String fname, String resourcePath) {
        InputStream inputStream = Utilities.class.getClassLoader().getResourceAsStream(resourcePath);
        File file = new File(fname);
        try (OutputStream outputStream = new FileOutputStream(file)) {
            if (inputStream != null) {
                IOUtils.copy(inputStream, outputStream);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    public static String execCmd(String cmd) throws java.io.IOException {
        java.util.Scanner s = new java.util.Scanner(Runtime.getRuntime().exec(cmd).getInputStream()).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static void createScripts() {
        resourceToFile("parse.clj", "scripts/parse.clj");
        resourceToFile("bb", "scripts/bb");
        resourceToFile("bb.sh", "scripts/bb.sh");
        try {
            execCmd("chmod +x ./bb");
            execCmd("chmod +x ./bb.sh");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}



