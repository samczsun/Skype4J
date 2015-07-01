package com.samczsun.skype4j;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class StreamUtils {
    public static String readFully(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] chunk = new byte[2048];
        int read = 0;
        while ((read = in.read(chunk)) > 0) {
            out.write(chunk, 0, read);
        }
        return out.toString();
    }
}
