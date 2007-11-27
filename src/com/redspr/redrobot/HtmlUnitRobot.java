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
import com.gargoylesoftware.htmlunit.html.HtmlImageInput;
import com.gargoylesoftware.htmlunit.html.HtmlInput;
import com.gargoylesoftware.htmlunit.html.HtmlLabel;
import com.gargoylesoftware.htmlunit.html.HtmlOption;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.html.HtmlPasswordInput;
import com.gargoylesoftware.htmlunit.html.HtmlRadioButtonInput;
import com.gargoylesoftware.htmlunit.html.HtmlSelect;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;
import com.gargoylesoftware.htmlunit.html.HtmlTextArea;
import com.gargoylesoftware.htmlunit.html.HtmlTextInput;
import com.redspr.redrobot.HtmlUnitScorer.Bingo;

public class HtmlUnitRobot implements Robot, ConfirmHandler {
    public HtmlUnitRobot() {
        webClient.setJavaScriptEnabled(false);
    }
    public Object getImpl() {
        return webClient;
    }
    private Bingo CHECKABLE = new Bingo() {
        public boolean match(Object node) {
            if (node instanceof HtmlCheckBoxInput)
                return true;
            if (node instanceof HtmlRadioButtonInput)
                return true;
            return false;
        }
    };

    private Bingo CLICKABLE = new Bingo() {
        public boolean match(Object node) {
            if (node instanceof HtmlAnchor)
                return true;
            if (node instanceof HtmlSubmitInput)
                return true;
            if (node instanceof HtmlImageInput)
                return true;
            if (node instanceof HtmlLabel)
                return true;
            if (node instanceof HtmlOption)
                return true;

            return false;
        }
    };

    private Stack<URL> history = new Stack<URL>();

    private String lastConfirm;

    private HtmlPage page;

    private Bingo VALUE = new Bingo() {
        public boolean match(Object node) {
            if (node instanceof HtmlTextInput)
                return true;
            if (node instanceof HtmlTextArea)
                return true;
            if (node instanceof HtmlPasswordInput)
                return true;
            if (node instanceof HtmlSelect)
                return true;

            return false;
        }
    };

    final WebClient webClient = new WebClient();

    public void back() {
        try {
            page = (HtmlPage) webClient.getPage(history.pop());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    public void click(String... x) {
        try {
            history.push(page.getFullyQualifiedUrl(""));
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        HtmlUnitScorer scorer = new HtmlUnitScorer(CLICKABLE, page
                .getDocumentHtmlElement(), x);
        try {
            page = (HtmlPage) ((ClickableElement) scorer.getBest()).click();
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
        Object n = scorer.getBest();

        if (n instanceof HtmlInput) {
            HtmlInput i = (HtmlInput) n;
            return i.getValueAttribute();
        }

        if (n instanceof HtmlTextArea) {
            HtmlTextArea ta = (HtmlTextArea) n;
            return ta.getText();
        }

        if (n instanceof HtmlSelect) {
            HtmlSelect s = (HtmlSelect) n;
            List<HtmlOption> options = s.getSelectedOptions();
            StringBuilder sb = new StringBuilder();
            for (HtmlOption option : options) {
                sb.append(option.asText());
            }
            return sb.toString();
        }

        // TODO 00 blowup
        return null;
    }

    public String getConfirmation() {
        return lastConfirm; // TODO 00 clear on transition (make part of page)
    }

    public boolean handleConfirm(Page page, String msg) {
        lastConfirm = msg;
        return true;
    }

    public boolean isChecked(String... x) {
        HtmlUnitScorer scorer = new HtmlUnitScorer(CHECKABLE, page
                .getDocumentHtmlElement(), x);
        Object elmt = scorer.getBest();
        if (elmt instanceof HtmlCheckBoxInput) {
            return ((HtmlCheckBoxInput) elmt).isChecked();
        }
        if (elmt instanceof HtmlRadioButtonInput) {
            return ((HtmlRadioButtonInput) elmt).isChecked();
        }
        // TODO 00 blowup
        return false;
    }

    public void open(URL url) {
        try {
            // TODO 00 config base?
            page = (HtmlPage) webClient.getPage(url);
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
        if (o instanceof HtmlInput) {
            ((HtmlInput) o).setValueAttribute(v);
        } else if (o instanceof HtmlTextArea) {
            ((HtmlTextArea) o).setText(v);
        }

        // TODO 00 blowup
    }
}
