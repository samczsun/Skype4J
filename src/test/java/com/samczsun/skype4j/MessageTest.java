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

import java.awt.*;
import java.util.Stack;

/**
 * Created by sam on 2015-07-09.
 */
public class MessageTest {
    public static void main(String[] args) {
        String text = "Hello";
        Message parsed = Message.fromHtml(text);
        if (!text.equalsIgnoreCase(parsed.toString())) {
            System.out.println("FAILED");
        } else {
            System.out.println("yay");
        }
    }
}
