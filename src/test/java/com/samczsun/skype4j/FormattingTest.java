package com.samczsun.skype4j;

import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.RichText;
import com.samczsun.skype4j.formatting.Text;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.NodeVisitor;
import org.junit.*;
import org.junit.Test;

import java.awt.*;
import java.awt.List;
import java.util.*;

/**
 * Created by sam on 2015-07-09.
 */
public class FormattingTest {
    @Test
    public void testParsing() {
        String html = "Plain<b>Bold<i>Italic</i></b>\n<a href=\"http://google.com\">google</a><s>Strikethrough</s><blink>Blink</blink><u>Underline</u>";
        Message parsed = Message.fromHtml(html);
        Assert.assertEquals(html, parsed.asHtml());
    }

    @Test
    public void testBuilding() {
        Message message = Message.create()
                .with(Text.plain("Plain"))
                .with(Text.rich().withBold()
                                .with(Text.plain("Bold"))
                                .with(Text.rich().withItalic()
                                                .with(Text.plain("Italic"))
                                )
                )
                .with(Text.NEW_LINE)
                .with(Text.rich().withLink("http://google.com")
                                .with(Text.plain("google"))
                )
                .with(Text.BLANK)
                .with(Text.rich().withStrikethrough()
                                .with(Text.plain("Strikethrough"))
                )
                .with(Text.rich().withBlink()
                                .with(Text.plain("Blink"))
                )
                .with(Text.rich().withUnderline()
                                .with(Text.plain("Underline"))
                )
                .with(Text.rich().withColor(Color.BLACK).withSize(10)
                                .with(Text.plain("Black"))
                );

        String html = "Plain<b>Bold<i>Italic</i></b>\n<a href=\"http://google.com\">google</a><s>Strikethrough</s><blink>Blink</blink><u>Underline</u><font size=\"10\" color=\"#000000\">Black</font>";

        Assert.assertEquals(html, message.asHtml());
    }

    @Test
    public void testPlainText() {
        Object randomObject = new Object();
        String text = "This is %s formatted";
        Assert.assertEquals(Text.plain((byte) 1).toString(), String.valueOf((byte) 1));
        Assert.assertEquals(Text.plain((short) 1).toString(), String.valueOf((short) 1));
        Assert.assertEquals(Text.plain((double) 1).toString(), String.valueOf((double) 1));
        Assert.assertEquals(Text.plain(1).toString(), String.valueOf(1));
        Assert.assertEquals(Text.plain((float) 1).toString(), String.valueOf((float) 1));
        Assert.assertEquals(Text.plain((long) 1).toString(), String.valueOf((long) 1));
        Assert.assertEquals(Text.plain(randomObject).toString(), String.valueOf(randomObject));
        Assert.assertEquals(Text.plain(text, randomObject).toString(), String.format(text, randomObject));
    }

    @Test
    public void testChildren() {
        RichText text = Text.rich().withBold()
                .with(Text.plain("Hello"))
                .with(
                        Text.rich().withItalic()
                                .with(Text.plain("Bye"))
                );

        Assert.assertEquals(2, text.children().size());
        Assert.assertEquals(Text.plain("Hello"), text.child(0));
        Assert.assertEquals(Text.rich().withItalic()
                .with(Text.plain("Bye")), text.child(1));
        Assert.assertEquals(Text.rich().withItalic()
                .with(Text.plain("Bye")).hashCode(), text.child(1).hashCode());
    }

    @Test
    public void testReturns() {
        Assert.assertNotNull(Text.rich().withUnderline());
    }
}
