package com.redspr.redrobot;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



public class Scorer {

//	final List<Candidate> result = new ArrayList<Candidate>();
//
//	final Bingo bingo;
//
//	public interface Bingo {
//		boolean match(Object node);
//	}
//
//	public class Candidate implements Comparable<Candidate> {
//		private double score;
//
//		private List<Object> path = new ArrayList<Object>();
//
//		Candidate(Object node2) {
//			Object p = node2;
//			this.score = 0;
//			while (p != null) {
//				path.add(p);
//		
//				p = feck.getParent(p);
//			}
//		}
//
//		public int compareTo(Candidate b) {
//			if (this.score < b.score)
//				return 1;
//			if (this.score > b.score)
//				return -1;
//			return 0;
//		}
//
//		public Object getNode() {
//			return path.get(0);
//		}
//	}
//
//	Scorer(Bingo bingo2, Object start, String[] x) {
//		this.bingo = bingo2;
//		//System.out.println("START " + start.asXml());
//		// create candidates
//		Iterator it = feck.descIt(start);
//		while (it.hasNext()) {
//			Object c = it.next();
//			if (bingo2.match(c)) {
//				result.add(new Candidate(c));
//			}
//		}
//		
//		// find matches and score candidates
//		
//		for (int i = 0; i < x.length; i++) {
//            List<Object> matches = new ArrayList<Object>();
//			 getCont(matches, start, x[i]);
//            
//			if (matches.size() == 0) {
//				throw new RuntimeException("No match for " + x[i]);
//			}
//            for (Object match: matches) {
//            //    System.out.println("MATCH " + match);
//			double s = 1.0;
//			while (match != null) {
//				for (Candidate c: result) {
//					if (c.path.contains(match)) {
//						c.score+=s;
//						
//					}
//				}
//				s = 0.9 * s;
//				match = feck.getParent(match);
//			}
//            }
//		}
//
//		Collections.sort(result);
//		
////		for (Candidate c : result) {
////            System.out.println("Score=" + c.score + "  " + c.getNode());
////        }
//	}
//
//	private void getCont(List<Object> result2, Object n, String x) {
//		Iterator it = n.getChildIterator();
//		while (it.hasNext()) {
//			DomNode c = (DomNode) it.next();
//			if (c instanceof DomText) {
//				DomText t = (DomText) c;
//				// System.out.println("X '" + t.getData() + "'");
//				if (x.equals(t.getData())) result2.add(c);
//			} else
//			if (c instanceof HtmlSubmitInput) {
//				HtmlSubmitInput s = (HtmlSubmitInput) c;
//				if (x.equalsIgnoreCase(s.getValueAttribute())) {
//					result2.add(s);
//				}
//			} else 
//			if (c instanceof HtmlElement) {
//				HtmlElement e = (HtmlElement) c;
//				if (x.equalsIgnoreCase(e.getAttributeValue("title"))) {
//					result2.add(c);
//				}
//			}
//
//			getCont(result2, c, x);
//		}
//	}
//
//	public DomNode getBest() {
//		if (result.size() > 0) {
//			return result.iterator().next().getNode();
//		} else {
//			throw new RuntimeException("No match");
//		}
//	}
}
