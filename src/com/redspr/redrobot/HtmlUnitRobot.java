package com.redspr.redrobot;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.gargoylesoftware.htmlunit.ConfirmHandler;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.ClickableElement;
import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.HtmlAnchor;
import com.gargoylesoftware.htmlunit.html.HtmlCheckBoxInput;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.redspr.redrobot.HtmlUnitScorer.Bingo;

// TODO select
public class HtmlUnitRobot implements Robot, ConfirmHandler {
    final WebClient webClient = new WebClient();

    private Stack<URL> history = new Stack<URL>();

    HtmlPage page;

    public void back() {
        try {
            page = (HtmlPage) webClient.getPage(history.pop());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private Bingo CLICKABLE = new Bingo() {
        public boolean match(Object node) {
            if (node instanceof HtmlAnchor)
                return true;
            if (node instanceof HtmlSubmitInput)
                return true;
            return false;
        }
    };

    private Bingo VALUE = new Bingo() {
        public boolean match(Object node) {
            return (node instanceof HtmlTextInput);
        }
    };

    private Bingo CHECKABLE = new Bingo() {
        public boolean match(Object node) {
            if (node instanceof HtmlCheckBoxInput)
                return true;
            if (node instanceof HtmlRadioButtonInput)
                return true;
            return false;
        }
    };

    public void click(String... x) {
        try {
            history.push(page.getFullyQualifiedUrl(""));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        HtmlUnitScorer scorer = new HtmlUnitScorer(CLICKABLE, page
                .getDocumentHtmlElement(), x);
        try {
            ((ClickableElement) scorer.getBest()).click();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public int findText(String x) {
        List<DomNode> result = new ArrayList();
        HtmlUnitScorer.getCont(result, page.getDocumentHtmlElement(), x);
        return result.size();
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
        return lastConfirm; // TODO 00 clear on transition (make part of page)
    }

    public boolean isChecked(String... x) {
        HtmlUnitScorer scorer = new HtmlUnitScorer(CHECKABLE, page
                .getDocumentHtmlElement(), x);
        Object v = scorer.getBest();
        if (v instanceof HtmlCheckBoxInput) {
            return ((HtmlCheckBoxInput) v).isChecked();
        }
        if (v instanceof HtmlRadioButtonInput) {
            return ((HtmlRadioButtonInput) v).isChecked();
        }
        // TODO 00 blowup
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
        HtmlUnitScorer scorer = new HtmlUnitScorer(VALUE, page
                .getDocumentHtmlElement(), n);
        Object o = scorer.getBest();
        if (o instanceof HtmlTextInput) {
            ((HtmlTextInput) o).setValueAttribute(v);
        }
        // TODO 00 textarea

        // TODO 00 blowup
    }

    private String lastConfirm;

    public boolean handleConfirm(Page arg0, String arg1) {
        lastConfirm = arg1;
        return true;
    }
}
