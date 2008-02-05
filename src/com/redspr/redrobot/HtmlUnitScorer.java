/*
 * Copyright 2007 Sam Hough
 * 
 * This file is part of REDROBOT.
 *
 * REDROBOT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * REDROBOT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with REDROBOT.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.redspr.redrobot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.gargoylesoftware.htmlunit.html.DomNode;
import com.gargoylesoftware.htmlunit.html.DomText;
import com.gargoylesoftware.htmlunit.html.HtmlElement;
import com.gargoylesoftware.htmlunit.html.HtmlLabel;
import com.gargoylesoftware.htmlunit.html.HtmlSubmitInput;

public class HtmlUnitScorer {

    final List<Candidate> candidates = new ArrayList<Candidate>();

    final Bingo bingo;

    public interface Bingo {
        boolean match(Object node);
    }

    static private String digest(String p) {
        return p.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    public class Candidate implements Comparable<Candidate> {
        private double score;

        private List<DomNode> path = new ArrayList<DomNode>();

        Candidate(DomNode node2) {
            DomNode p = node2;
            this.score = 0;
            while (p != null) {
                path.add(p);

                p = p.getParentDomNode();
            }
        }

        public int compareTo(Candidate b) {
            if (this.score < b.score)
                return 1;
            if (this.score > b.score)
                return -1;
            return 0;
        }

        public DomNode getNode() {
            return path.get(0);
        }
        
        public String toString() {
            return path.get(0).toString();
        }
    }

    HtmlUnitScorer(Bingo bingo2, DomNode start, String[] x) {
        this.bingo = bingo2;
        // System.out.println("START " + start.asXml());
        // create candidates
        Iterator<DomNode> it = start.getAllHtmlChildElements();
        while (it.hasNext()) {
            DomNode c = it.next();
            if (bingo.match(c)) {
                candidates.add(new Candidate(c));
            }
        }

        // find matches and score candidates
        
        for (int i = 0; i < x.length; i++) {
            List<DomNode> matches = new ArrayList<DomNode>();
            getCont(matches, start, x[i]);

            if (matches.size() == 0) {
                throw new RuntimeException("No match for " + x[i]);
            }
        
            for (Candidate c : candidates) {
                double max = 0.0;
                for (DomNode match : matches) {
                    double s = 1.0;
                    while (match != null) {
                        if (c.path.contains(match)) {
                            if (s >  max) max = s;
                            break;
                        }
                        s = 0.9 * s;
                        match = match.getParentDomNode();
                    }
                }
                c.score = max;
            }
        }

        Collections.sort(candidates);
        // TODO 00 debugging
        //for (Candidate c : candidates) {
        //    System.out.println("Score=" + c.score + " " + c.getNode());
        //}
    }

    static void getCont(List<DomNode> result2, DomNode n, String x2) {
        String x = digest(x2); // XXX doing every time!
        Iterator it = n.getChildIterator();
        while (it.hasNext()) {
            DomNode c = (DomNode) it.next();
            if (c instanceof HtmlLabel) {
                HtmlLabel l = (HtmlLabel) c;
                if (x.equals(digest(l.asText()))) {
                    DomNode tar = l.getReferencedElement();
                    if (tar != null)
                        result2.add(tar);
                }
            } else if (c instanceof DomText) {
                DomText t = (DomText) c;
                // System.out.println("X '" + t.getData() + "'");
                if (x.equals(digest(t.getData()))) {
                    result2.add(c);
                }
            } else if (c instanceof HtmlSubmitInput) {
                HtmlSubmitInput s = (HtmlSubmitInput) c;
                if (x.equals(digest(s.getValueAttribute()))) {
                    result2.add(s);
                }
            }
            if (c instanceof HtmlElement) {
                HtmlElement e = (HtmlElement) c;
                if (x.equals(digest(e.getAttributeValue("title")))) {
                    result2.add(e);
                }
                getCont(result2, c, x);
            }
        }
    }

    public DomNode getBest() {
        if (candidates.size() > 0) {
            return candidates.iterator().next().getNode();
        } else {
            throw new RuntimeException("No match");
        }
    }
}
