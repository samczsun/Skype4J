package com.samczsun.skype4j;

import junit.framework.TestCase;
import org.junit.*;

import java.io.ByteArrayInputStream;

/**
 * Created by sam on 2015-07-10.
 */
public class StreamUtilsTest extends TestCase {

    @org.junit.Test
    public void testReadFully() throws Exception {
        String data = "This is a test";
        ByteArrayInputStream stream = new ByteArrayInputStream(data.getBytes());
        Assert.assertEquals(StreamUtils.readFully(stream), data);
    }
}