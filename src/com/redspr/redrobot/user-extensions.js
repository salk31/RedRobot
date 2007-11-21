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
	return anyk(text, docm, ["a", "button", "input", "textarea", "td"], false);
}
PageBot.prototype.locateElementByFuzzyKey = function(text, docm) {
	return anyk(text, docm, ["input", "textarea"], true);
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
    	
	getMatch(matches, docm, text);
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

function getMatch(matches, node, text) {
	var kids = node.childNodes;

	for (var i = 0; i < kids.length; i++) {
		var e = kids[i];
		if (e.nodeValue == text || e.title == text || e.value == text) {
			matches.push(e);
			e.parentNode.style.color='yellow';
		}
		getMatch(matches, e, text);
	}
}


