package com.redspr.redrobot;

import java.io.IOException;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.ClickableElement;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.redspr.redrobot.HtmlUnitScorer.Bingo;

public class HtmlUnitRobot implements Robot {
    final WebClient webClient = new WebClient();
    HtmlPage page;

    public void back() {
        // TODO Auto-generated method stub

    }

    public Bingo CLICKABLE = new Bingo() {
        public boolean match(Object node) {
            if (node instanceof HtmlAnchor) return true;
            if (node instanceof HtmlSubmitInput) return true;
            return false;
        }
    };
    
    public Bingo VALUE = new Bingo() {
        public boolean match(Object node) {
            return (node instanceof HtmlTextInput);
        }
    };

    public void click(String... x) {
        HtmlUnitScorer scorer = new HtmlUnitScorer(CLICKABLE, page
                .getDocumentHtmlElement(), x);
        try {
            ((ClickableElement) scorer.getBest()).click();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int findText(String x) {
        // TODO Auto-generated method stub
        return 0;
    }

    public String get(String... x) {
        HtmlUnitScorer scorer = new HtmlUnitScorer(VALUE, page
                .getDocumentHtmlElement(), x);
       Object v = scorer.getBest();
       if (v instanceof HtmlTextInput) {
           return ((HtmlTextInput) v).getValueAttribute();
       }
       // TODO 00 blowup
        return null;
    }

    public String getConfirmation() {
        // TODO Auto-generated method stub
        return null;
    }

    public boolean isChecked(String... x) {
        // TODO Auto-generated method stub
        return false;
    }

    public void open(String path) {
        try {
            page = (HtmlPage) webClient.getPage(path);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void set(String... x) {
        String[] n = new String[x.length - 1];
        for (int i = 0; i < n.length; i++) {
            n[i] = x[i];
        }
        String v = x[x.length - 1];
        HtmlUnitScorer scorer = new HtmlUnitScorer(VALUE, page.getDocumentHtmlElement()
                , n);
       Object o = scorer.getBest();
       if (o instanceof HtmlTextInput) {
           ((HtmlTextInput) o).setValueAttribute(v);
       }
       // TODO 00 blowup
    }
}
