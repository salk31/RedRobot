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
Cand.prototype.isDescendantOf=function(t) {
	var x = this.node;

	while (t.ownerDocument !== x.ownerDocument) {
		var doc;
		if (x.ownerDocument) {
			doc = x.ownerDocument;
		} else {
			doc = x;
		}
		if (doc.redrobotParentNode) {
			x = doc.redrobotParentNode;
		} else {
			return false;
		}
	}
	
	while (x != null) {
		if (x === t) {
			return true;
		}
		x = x.parentNode;
	}
	return false;
}
Cand.prototype.add=function(node, score) {
	match.push(new Hit(node, score));
}
Cand.fn = function(a, b) {
	if (a.score < b.score) return 1;
	if (a.score > b.score) return -1;
	return 0;
}
function redrobotIsClickable(node) {
	if (node.nodeName=='A') {
		return true;
	} else if (node.nodeName=='BUTTON') {
		return true;
	} else if (node.nodeName=='INPUT') {
		return (node.type=='submit' || node.type=='button' || node.type=='radio' || node.type=='checkbox');
	} else if (node.nodeName=='SELECT') {
		return true;
	} else if (node.nodeName=='TEXTAREA') {
		return true;
	}
	return false;
}


PageBot.prototype.locateElementByFuzzyClickable = function(text, docm) {
	return redrobotFindBestMatch(text, docm, redrobotIsClickable);
}

function redrobotIsKey(node) {
	if (node.nodeName=='INPUT') {
		return node.type == 'text' || node.type == 'password';
    } else if (node.nodeName =='TEXTAREA') {
    	return true;
    } else if (node.contenteditable || node.designMode=='on') {
		return true;
	} else if (node.nodeName == 'SELECT') {
		return true;
	}
    return false;
}

PageBot.prototype.locateElementByFuzzyKey = function(text, docm) {
	return redrobotFindBestMatch(text, docm, redrobotIsKey);
}

function redrobotIsCheckable(node) {
	if (node.nodeName=='INPUT') {
		return (node.type=="checkbox" || node.type=="radio");
	}
	return false;
}

PageBot.prototype.locateElementByFuzzyCheckable = function(text, docm) {
	return redrobotFindBestMatch(text, docm, redrobotIsCheckable);
}

function redrobot_onbeforeunload() {
	var x = redrobot_onbeforeunload.orig();
	if (x) {
		redrobot_onbeforeunload.confirm(x);
	}
}

function redrobotFindBestMatch(argx, docm, matchFn) {
	var patterns = argx.split(',');
	var w = docm.defaultView;//window.frames[0];

	if (w.onbeforeunload !== redrobot_onbeforeunload) {
		redrobot_onbeforeunload.orig = w.onbeforeunload;
		w.onbeforeunload = redrobot_onbeforeunload;
		redrobot_onbeforeunload.confirm = w.confirm;	
	}

	// work out all candidate elements
    var cands = new Array();    

	redrobotIterate(docm, function(nd) {if (matchFn(nd)) cands.push(new Cand(nd))});


	for (var p = 0; p < patterns.length; p++) { // fake loop for patterns
	    var text = patterns[p];
	    
		// work out matching elements
    	var matches = new Array();
    	var digest = redrobotDigest(text);
		redrobotIterate(docm, function(node) {redrobotGetMatch(digest, matches, node)});
		if (matches.length == 0) return null;
	
		// assign matches to candidates
		for (var j = 0; j < cands.length; j++) {
			var c = cands[j];
			var max = 0;
			for (var i = 0; i < matches.length; i++) {
				var match = matches[i];
				var s = 1.0;
				while (match != null) {
					if (c.isDescendantOf(match)) {
						if (s > max) max = s;
						break;
					}
					match = match.parentNode;
	      			s = s * 0.9;
	      		}
			}		
			c.score = c.score + max;
		}	
	}
	
	cands.sort(Cand.fn);
	if (cands.length == 0) {
    	return null;
	} else {
		if (cands[0].node.body) return cands[0].node.body;
		return cands[0].node;
	}
};

function redrobotGetMatch(text, matches, e) {
	var match = null;
	if (redrobotDigest(e.nodeValue) == text) {
		//e.parentNode.style.color='yellow';
		match = e;
		if (e.parentNode.nodeName == 'LABEL') {
			var id = e.parentNode.getAttribute('for');
			if (id) {
				match = e.ownerDocument.getElementById(id);
			}
		} 
	} else if (redrobotDigest(e.title) == text || redrobotDigest(e.value) == text) {
		match = e;
	}
	if (match) matches.push(match);
}
function redrobotDigest(x) {
	return String(x).replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
}

function redrobotIterate(node, fn) {
	fn(node);

	var kids = node.childNodes;
	
	for (var i = 0; i < kids.length; i++) {
		var e = kids[i];
		redrobotIterate(e, fn);	
		
		if (e.nodeName == 'IFRAME') {
			e.contentDocument.redrobotParentNode = e;
			redrobotIterate(e.contentDocument, fn);
		}
	}
}
