package org.markdownj.test;

import java.util.Arrays;
import java.util.Collection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.markdownj.MarkdownProcessor;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class PreserveHtmlBlockTagsTest {

    private MarkdownProcessor m;

    @BeforeEach
    public void setup() {
        m = new MarkdownProcessor();
    }

    public static Collection<String[]> testHtml() {
        return Arrays.asList(new String[][]{
            {"<h1>Chapter One</h1>"},
            {"<H1>Chapter One</H1>"},
            {"<div>\n  <div>Text</div>\n</div>"},
            {"<DIV>\n  <DIV>Text</DIV>\n</DIV>"},
            {"<TABLE>\n<TR>\n<TD>Cell</TD>\n</TR>\n</TABLE>"},
            {"<BlockQuote>All the world’s a stage…</BlockQuote>"},
            {"<iFrame src='http://microsoft.com/'></IFRAME>"},
            {"<hr/>"},
            {"<HR>"},
            {"<!-- a comment -->"}
        });
    }

    @ParameterizedTest(name = "{index}: {1}")
    @MethodSource("testHtml")
    public void testRoundtripPreservesTags(String value) {
        assertEquals(value, m.markdown(value).trim());
    }

}
