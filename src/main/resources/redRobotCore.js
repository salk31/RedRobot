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

RedRobot = {}

// a possible result candidate
RedRobot.Cand = function(e) {
  this.node = e;
  this.score = 0.0;
}

// return true if candidate is a dependent of t
RedRobot.Cand.prototype.isDescendantOf = function(t) {
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

RedRobot.Cand.fn = function(a, b) {
  if (a.score < b.score) return 1;
  if (a.score > b.score) return -1;
  return 0;
}

// true iff node is something it would make sense for the user to click
RedRobot.isClickable = function(node) {
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
  } else if (node.nodeName=='OPTION') {
    return true;
  } else if (node.click && node.nodeName != 'BODY' && node.nodeName != 'HTML') {
    // firefox seems to have click attribute for body and html!?
    return true;
  }
  return false;
}

// true iff the node is something the user can enter text or get text
RedRobot.isKey = function(node) {
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


// XXX redundant
RedRobot.isCheckable = function(node) {
  if (node.nodeName=='INPUT') {
    return (node.type=="checkbox" || node.type=="radio");
  }
  return false;
}


RedRobot.findBestMatches = function(patterns, docm, matchFn) {
  var w = docm.defaultView;// window.frames[0];

  // work out all candidate elements that match the function provided
  var cands = new Array();
  RedRobot.visit(docm, function(nd) {if (matchFn(nd)) cands.push(new RedRobot.Cand(nd))});

  for (var p = 0; p < patterns.length; p++) { // fake loop for patterns
    var text = patterns[p];

    // work out matching elements
    var matches = new Array();
    var digest = RedRobot.digest(text);
    RedRobot.visit(docm, function(node) {RedRobot.getMatch(digest, matches, node)});
    if (matches.length == 0) return new Array();

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

  cands.sort(RedRobot.Cand.fn);
  
  // turn into a simple array of nodes
  var result = new Array();
  for (var i = 0; i < cands.length; i++) {
    var node = cands[i].node;
    result.push(node);
  }
  return result;
};

RedRobot.getMatch = function(text, matches, e) {
  var match = null;
  if (RedRobot.digest(e.nodeValue) == text) {
    // e.parentNode.style.color='yellow';
    match = e;
    if (e.parentNode.nodeName == 'LABEL') {
      var id = e.parentNode.getAttribute('for');
      if (id) {
        match = e.ownerDocument.getElementById(id);
      }
    } 
  } else if (RedRobot.digest(e.title) == text || RedRobot.digest(e.value) == text) {
    match = e;
  }
  if (match) matches.push(match);
}

RedRobot.digest = function(x) {
  return String(x).replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
}

RedRobot.visit = function(node, fn) {
  fn(node);
  var kids = node.childNodes;

  for (var i = 0; i < kids.length; i++) {
    var e = kids[i];
    RedRobot.visit(e, fn);  
    
    if (e.nodeName == 'IFRAME') {
      e.contentDocument.redrobotParentNode = e;
      RedRobot.visit(e.contentDocument, fn);
    }
  }
}
