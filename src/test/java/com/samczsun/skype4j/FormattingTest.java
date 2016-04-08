package com.samczsun.skype4j;

import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.Text;
import org.junit.Assert;
import org.junit.Test;

import java.awt.*;

/**
 * Created by sam on 2015-07-09.
 */
public class FormattingTest {
    @Test
    public void testParsing() {
        Message message = Message.create()
                .with(Text.plain("Plain"))
                .with(Text.rich("Bold").withBold().append("Italic").withItalic())
                .with(Text.NEW_LINE)
                .with(Text.rich("google").withLink("http://google.com"))
                .with(Text.BLANK)
                .with(Text.rich("Strikethrough").withStrikethrough())
                .with(Text.rich("Blink").withBlink())
                .with(Text.rich("Underline").withUnderline())
                .with(Text.rich("Black").withColor(Color.BLACK).withSize(10));
        String html = "Plain<b>Bold<i>Italic</i></b>" + Text.NEW_LINE +
                      "<a href=\"http://google.com\">google</a>" +
                      "<s>Strikethrough</s>" +
                      "<blink>Blink</blink>" +
                      "<u>Underline</u>" +
                      "<font size=\"10\" color=\"#000000\">Black</font>";
        Assert.assertEquals(message, Message.fromHtml(html));
    }

    @Test
    public void testFont() {
        Message message = Message.create()
                .with(Text.rich("test").withColor(Color.BLACK).withSize(10));
        String html = "<font color=\"#000000\" size=\"10\">test</font>";
        Assert.assertEquals(Message.fromHtml(html), message);
    }

    @Test
    public void testLink() {
        Message message = Message.create()
                .with(Text.rich("test").withLink("http://google.com"));
        String html = "<a href=\"http://google.com\">test</a>";
        Assert.assertEquals(Message.fromHtml(html), message);
    }

    @Test
    public void testBuilding() {
        Message message = Message.create()
                .with(Text.plain("Plain"))
                .with(Text.rich("Bold").withBold().append("Italic").withItalic())
                .with(Text.NEW_LINE)
                .with(Text.rich("google").withLink("http://google.com"))
                .with(Text.BLANK)
                .with(Text.rich("Strikethrough").withStrikethrough())
                .with(Text.rich("Blink").withBlink())
                .with(Text.rich("Underline").withUnderline())
                .with(Text.rich("Black").withColor(Color.BLACK).withSize(10));

        String html = "Plain" +
                      "<b>Bold<i>Italic</i></b>" +
                      Text.NEW_LINE +
                      "<a href=\"http://google.com\">google</a>" +
                      "<s>Strikethrough</s>" +
                      "<blink>Blink</blink>" +
                      "<u>Underline</u>" +
                      "<font size=\"10\" color=\"#000000\">Black</font>";
        Assert.assertEquals(html, message.write());
    }
}
