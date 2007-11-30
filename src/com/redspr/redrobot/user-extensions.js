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
function Cand(e) {
	this.node = e;
	this.score = 0.0;
}
Cand.prototype.isDesc=function(t) {
	var x = this.node;
	while (x != null) {
		if (x === t) return true;
		x = x.parentNode;
	}
	return false;
}
Cand.fn = function(a, b) {
	if (a.score < b.score) return 1;
	if (a.score > b.score) return -1;
	return 0;
}
PageBot.prototype.locateElementByFuzzy = function(text, docm) {
	return anyk(text, docm, ["a", "button", "input", "option", "textarea", "td"], false);
}
PageBot.prototype.locateElementByFuzzyKey = function(text, docm) {
	return anyk(text, docm, ["input", "textarea", "select"], true);
}
PageBot.prototype.locateElementByFuzzyCheckable = function(text, docm) {
	return anyk(text, docm, ["input"], true); // XXX further narrow to radio and checkbox?
}

		function xxfart() {
			var x = xxfart.orig();
			if (x) {
				xxfart.confirm(x);
			}
		}

function anyk(text, docm, n, flag) {

	var w = window.frames[0];
	if (w.document.body.onbeforeunload !== xxfart) {
		xxfart.orig = w.document.body.onbeforeunload;
		w.document.body.onbeforeunload = xxfart;
		xxfart.confirm = w.confirm;	
	}

    	var r = new Array();    

    	for (var i = 0; i < n.length; i++) {
		var allt = docm.getElementsByTagName(n[i]);
		for (var j = 0; j < allt.length; j++) {
			if (flag && allt[j].type=='radio') continue;
            	r.push(new Cand(allt[j]));
		}
    	}

    	var matches = new Array();
    	
	getMatch(digest(text), docm, matches, docm);
    	for (var i = 0; i < matches.length; i++) {
		var match = matches[i];
		var s = 1.0;
		while (match != null) {
			for (var j = 0; j < r.length; j++) {
				var c = r[j];
				if (c.isDesc(match)) {
					c.score=c.score+s;
				}
			}

			match = match.parentNode;
	      	s = s * 0.9;
		}	
	}
	r.sort(Cand.fn);
	if (r.length == 0) {
    		return null;
	} else {
		return r[0].node;
	}
};

function getMatch(text, docm, matches, node) {
	var kids = node.childNodes;

	for (var i = 0; i < kids.length; i++) {
		var e = kids[i];
		var match = null;
		if (digest(e.nodeValue) == text) {
		e.parentNode.style.color='yellow';
			match = e;
			if (e.parentNode.nodeName == 'LABEL') {
				var id = e.parentNode.getAttribute('for');
				if (id) {
					match = docm.getElementById(id);
				}
			} 
		} else if (digest(e.title) == text || digest(e.value) == text) {
			match = e;
			
		}
		if (match) matches.push(match);
		getMatch(text, docm, matches, e);
	}
}
function digest(x) {
	return String(x).replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
}


