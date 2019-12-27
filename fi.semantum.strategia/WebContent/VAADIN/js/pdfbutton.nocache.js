/*******************************************************************************
 * Copyright (c) 2014 Ministry of Transport and Communications (Finland).
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Semantum Oy - initial API and implementation
 *******************************************************************************/

var $wnd = $wnd || window.parent;
$wnd.extractSVG = function extractSVG () {
  var v = document.getElementById('map');
  var s = new XMLSerializer();
  return s.serializeToString(v);
};

function printPageAfterRefresh(){
	printPageDelay(0);
}

function printPageDelay(counter){
	let newCounter = counter + 1;
	var documentPrintBody = document.getElementById("documentPrintBody");
	if(documentPrintBody == null && counter < 100){
		setTimeout(function(){printPageDelay(newCounter);},100);
	} else {
		var documentOptionsWindow = document.getElementById("printOptionsWindow");
		if(documentOptionsWindow == null){
			print();
		} else {
			setTimeout(function(){printPageDelay(newCounter);},100);
		}
	}
}