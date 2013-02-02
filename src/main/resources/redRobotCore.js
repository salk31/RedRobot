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
  } else if (node.getAttribute) {
    switch (node.getAttribute('role')) {
    case 'menuitem':
    case 'tab':
    case 'listitem':
    case 'treeitem':
    case 'button':
      return true;
    default:
    }
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
  } else if (node.role && node.role.toUpperCase() == 'TEXTBOX') {
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

RedRobot.isText = function(node) {
  if (node.nodeType == 1) {
    if (RedRobot.textMatch(node.title, RedRobot.textHack) > 0) return true;
    if (RedRobot.textMatch(node.value, RedRobot.textHack) > 0) return true;
      var kids = node.childNodes;
      for (var i = 0; i < kids.length; i++) {
        var e = kids[i];
        if (e.nodeType == 3 && RedRobot.textMatch(e.nodeValue, RedRobot.textHack) > 0) return true;
      }
  }

  return false;
}

RedRobot.findBestMatches = function(patterns, docm, matchFn) {
  var w = docm.defaultView;// window.frames[0];

  // TODO 00 fix this extra ugly bit
  if (matchFn == RedRobot.isText) {
    var text = patterns[patterns.length - 1];
    patterns.length--;
    RedRobot.textHack = RedRobot.digest(text);
  }

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
        var s = match['data-redrobotScore'];
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
    node['data-redrobotTotal'] = cands[i].score;
    result.push(node);
  }
  return result;
};

RedRobot.getMatch = function(text, matches, e) {
  switch (e.nodeName) {
  case "#text" :
    if ((score = RedRobot.textMatch(e.nodeValue, text)) > 0) {
      var parentNode = e.parentNode;
      switch (parentNode.nodeName) {
      case 'LABEL' :
        var id = parentNode.getAttribute('for');
        if (id) {
          RedRobot.pushMatch(matches, e.ownerDocument.getElementById(id), score);
        } else {
          RedRobot.pushMatch(matches, e, score);
        }
        break;
        
      case 'OPTION' :
        if (!e.parentNode.selected) {
          score = score * 0.5;
        }
        RedRobot.pushMatch(matches, e, score);
        break;
        
      case 'TH' :
        var row = parentNode.parentNode;
        var tbody = row.parentNode;
        var col0 = 0;
        for (var i = 0; i < parentNode.cellIndex; i++) {
          col0 += row.cells[i].colSpan;
          parentNode.title += row.cells[i].colSpan;
        
        }
        var col1 = col0 + parentNode.colSpan;

        for (var i = row.rowIndex + 1; i < tbody.rows.length; i++) {
          var currentCol = 0;
          var currentColIdx = 0;
          var currentRow = tbody.rows[i];
          while (currentCol < col1) {
            var currentCell = currentRow.cells[currentColIdx++];
            if (currentCol >= col0) {
              RedRobot.pushMatch(matches, currentCell, 0.9);
            }
            currentCol += currentCell.colSpan;
          }
        }
        break;
      default:
        RedRobot.pushMatch(matches, e, score);
      }
    }
    break;
  default : 
    if ((score = RedRobot.textMatch(e.title, text)) > 0 || (score = RedRobot.textMatch(e.value, text)) > 0) {
      RedRobot.pushMatch(matches, e, score);
    }
  }
}

RedRobot.pushMatch = function(matches, elmt, score) {
  elmt['data-redrobotScore'] = score;

  matches.push(elmt);
}

RedRobot.textMatch = function(candidateText, searchTextDigest) {
  var candidateTextDigest = RedRobot.digest(candidateText);
  if (RedRobot.digest(candidateTextDigest).indexOf(searchTextDigest) >= 0) {
    return searchTextDigest.length / candidateTextDigest.length;
  } else {
    return 0;
  }
}

RedRobot.digest = function(x) {
  return String(x).replace(/[^a-zA-Z0-9]/g, '').toLowerCase();
}


RedRobot.visit = function(node, fn) {
  for (var child = node.firstChild; child; child = child.nextSibling) {
    // XXX do different things for text vs element!
    if (child.nodeType == 1) {
      fn(child);
      if (child.getAttribute && child.getAttribute('aria-hidden') == 'true') continue;
      RedRobot.visit(child, fn);  
        
      if (child.nodeName == 'IFRAME') {
        child.contentDocument.redrobotParentNode = child;
        RedRobot.visit(child.contentDocument, fn);
      }
    } else if (child.nodeType == 3) {
      fn(child);
    }
  }
}

RedRobot.multiTextMatch = function(source, input) {
  var score;
  for (var i = 0; i < input.length; i++) {
    var searchTextDigest = RedRobot.digest(input[i]);
    var max = 0;
    for (var s = 0; s < source.length; s++) {
      var ss = RedRobot.textMatch(source[s], searchTextDigest);
      if (ss > max) {
        max = ss;
      }
    }
    if (i == 0) {
      score = max;
    } else {
      score = score * max;
    }
  }
  return score;
}

RedRobot.addDebug = function(docm, elmt, x, y) {
  var css = docm.createElement("style");
  css.type = "text/css";
  css.innerHTML = 
	  ".RedRobotDebug {"
	  + "position: relative;"
//	  + "width: 200px;"
//	  + "height: 100px;"
	  + "text-align: center;"
//	  + "line-height: 100px;"
	  + "background-color: #fff;"
	  + "border: 8px solid #666;"
	  + "-webkit-border-radius: 30px;"
	  + "-moz-border-radius: 30px;"
	  + "border-radius: 30px;"
	  + "-webkit-box-shadow: 2px 2px 4px #888;"
	  + "-moz-box-shadow: 2px 2px 4px #888;"
	  + "box-shadow: 2px 2px 4px #888;"
	  + "}\n"
	  + ".RedRobotDebug:before"
	  + "{"
	  + "font-size: 10px;"
	  + "color: #fff;"
	  + "content: '1';"
	  + "position: absolute;"
	  + "width: 12px;"
	  + "height: 12px;"
	  + "background-color: red;"
	  + "border: 2px solid #666;"
	  + "-webkit-border-radius: 5px;"
	  + "-moz-border-radius: 5px;"
	  + "border-radius: 5px;"
//	  + "right: 5px;"
//	  + "top: 5px;"
//	  + "border: 1px solid;"
//	  + "border-color: #666 transparent #666 transparent;"
	  + "}\n"
;
  docm.body.appendChild(css);

  elmt.setAttribute('class', 'RedRobotDebug');
  elmt.setAttribute('title', elmt['data-redrobotTotal']);

}
//RedRobot.findBestMatches(['Other', 'Other'], document, RedRobot.isClickable)
