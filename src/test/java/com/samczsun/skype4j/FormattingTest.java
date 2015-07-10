package com.samczsun.skype4j;

import com.samczsun.skype4j.formatting.Message;
import com.samczsun.skype4j.formatting.PlainText;
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
        String html = "Plain<b>Bold<i>Italic</i></b>\n<a href=\"http://google.com\">google</a><s>Strikethrough</s><blink>Blink</blink><u>Underline</u>";
        Assert.assertEquals(message, Message.fromHtml(html));
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
        Assert.assertEquals(html, message.write());
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
        RichText text = Text.rich();
        Assert.assertNotNull(text.withBold());
        Assert.assertNotNull(text.withItalic());
        Assert.assertNotNull(text.withUnderline());
        Assert.assertNotNull(text.withStrikethrough());
        Assert.assertNotNull(text.withBlink());
        Assert.assertNotNull(text.withColor(Color.BLACK));
        Assert.assertNotNull(text.withSize(10));
        Assert.assertNotNull(text.withLink("http://google.com"));
        Assert.assertNotNull(text.with(Text.plain("plain")));
        Assert.assertNotNull(text.toString());
        Assert.assertNotNull(Message.create());
        PlainText plainString = Text.plain("plain");
        PlainText plainObject = Text.plain(new Object());
        PlainText plainByte = Text.plain((byte) 1);
        PlainText plainFloat = Text.plain(1F);
        PlainText plainDouble = Text.plain(1D);
        PlainText plainShort = Text.plain((short) 1);
        PlainText plainLong = Text.plain(1L);
        PlainText plainChar = Text.plain('1');
        PlainText plainInt = Text.plain(1);
        Assert.assertNotNull(plainString.toString());
    }
}
