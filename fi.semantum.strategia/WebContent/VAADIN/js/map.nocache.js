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

var a4ratio = 0.4;
var mainMargin = a4ratio*37.0;
var chartWidth = a4ratio*1920-2*mainMargin;
var chartHeight = a4ratio*1500;
var headerHeight = 0;
var overlayMargin = 4;
var textPad = 5;
var boxPad = a4ratio*10;
var visioX = 0.55*chartWidth;
var visioWidth = 0.4*chartWidth;
var visioHeight = a4ratio*100 + 25;
//var tavoiteSpacing = a4ratio*10;
var painopisteMeterHeight = a4ratio*30;
var innerBoxExtraHeight = a4ratio*100;
var painopisteTagHeight = a4ratio*20;
var tavoiteMeterHeight = a4ratio*25;
var tavoiteTagHeight = a4ratio*25;
var tavoiteHeight = a4ratio*265 + painopisteMeterHeight;
var visioBaseY = 0;
var painopisteHeight = a4ratio*120 + painopisteMeterHeight;
var painopisteY = tavoiteHeight - painopisteHeight - boxPad;
var meterHeight = a4ratio*50;

fi_semantum_strategia_widget_D3 = function() {	

	var self = this;

	create(this);
	
	this.onStateChange = function() {
		refresh(self);
	};

	this.onStateChange();

};

function create(rootFn) {

	var thisElement = rootFn.getElement();

	var tooltipDIV = document.createElement("DIV");
	
	var state = rootFn.getState();
	var strategiaKartta = state.model;

	var root = d3.select(thisElement);
	
	var svg = root.append("svg")
	.on("click", function(d) { 
		rootFn.select(d3.event.pageX, d3.event.pageY); 
	})
	.attr("id", strategiaKartta.elementId);

	document.body.appendChild(tooltipDIV);
	tooltipDIV.classList.add("tooltip");
	tooltipDIV.style.width = "600px";
	//tooltipDIV.style.height = "200px";
	tooltipDIV.style.position = "absolute";
	tooltipDIV.style.borderRadius = "10px";
	tooltipDIV.style.border = "1px solid black";
	tooltipDIV.style.padding = "10px";
	tooltipDIV.style.left = "100px";
	tooltipDIV.style.top = "100px";
	tooltipDIV.style.background = "#f1f1f1";
	tooltipDIV.innerHTML = "";
	
	var notification;

	function tryToGetInfo(e) {
		
		if(e == null) return null;

		var classList = e.classList; 
		if(classList.contains("header") || classList.contains("parent1") || (classList.contains("parent") && classList.contains("link"))) {
			var base = "<h2>Karttojen otsikot</h2><p>Otsikkoalue näyttää sekä nykyisen kartan nimen että reitin jota pitkin karttaan on päästy ylemmän tason karttojen kautta.<p>Voit siirtyä ylemmän tason karttoihin klikkaamalla karttojen nimiä.<p>Voit muuttaa kartan otsikkoa klikkaamalla sitä mikäli olet syöttötilassa ja tunnuksellasi on muutosoikeus tähän karttaan.";
			if(state.edit) {
				if(state.logged) {
					base += "<p><b>Olet syöttötilassa ja sinulla on kartan muutosoikeus. Voit muuttaa kartan otsikkoa klikkaamalla sitä.</b>";
				} else {
					base += "<p><b>Olet syöttötilassa, mutta tunnuksellasi ei ole tähän karttaan muutosoikeutta. Voit pyytää muutosoikeutta kartan ylläpitäjältä.</b>";
				}
			} else {
				if(state.logged) {
					base += "<p><b>Olet katselutilassa. Voit muuttaa kartan otsikkoa siirtymällä syöttötilaan yläpalkin toiminnolla ja tämän jälkeen klikkaamalla kartan otsikkoa.</b>";
				} else {
					base += "<p><b>Olet katselutilassa eikä tunnuksellasi ole kartan muutosoikeutta.</b>";
				}
			}
			return base;
		} else if(classList.contains("parent2")) {
//			var base = "<h2>Strategiakartta</h2>";
//			return base;
			return null;
		} else if(classList.contains("tavoite")) {
//			var base = "<h2>Ulompi laatikko</h2>";
//			return base;
			return null;
		} else if(classList.contains("meterElement")) {
			var base = "<h2>Käyttäjän arvio</h2>";
			var desc = e.dataset.meterDesc;
			if(desc != undefined && desc.length > 0) {
				base += "<p><b> Selite: " + desc + ".</b>";
			}

			base += "<p><b> Tämä arvio on kartan käyttäjän määrittelemä.</b>";

			if(state.edit) {
				if(state.logged) {
					base += "<p><b>Olet syöttötilassa ja sinulla on kartan muutosoikeus. Voit muuttaa mittarin arvoa klikkaamalla sitä.</b>";
				} else {
					base += "<p><b>Olet syöttötilassa, mutta tunnuksellasi ei ole tähän karttaan muutosoikeutta. Voit pyytää muutosoikeutta kartan ylläpitäjältä.</b>";
				}
			} else {
				if(state.logged) {
					base += "<p><b>Olet katselutilassa. Voit muuttaa mittarin arvoa siirtymällä syöttötilaan yläpalkin toiminnolla ja tämän jälkeen klikkaamalla mittari-ikonia.</b>";
				} else {
					base += "<p><b>Olet katselutilassa eikä tunnuksellasi ole kartan muutosoikeutta.</b>";
				}
			}

			return base;
			
		} else if(classList.contains("painopisteMeter")) {
			var base = "<h2>Laskennallinen arvio</h2>";
			var desc = e.dataset.meterDesc;
			base += "<p><b> Alirakenteen arvioiden perusteella laskettu arvio.</b>";
			if(desc != undefined && desc.length > 0) {
				base += "<p><b> Käyttäjän luonnehdinta: " + desc + "</b>";
			}

			if(e.dataset.link != "_") {
				base += "<p><b>Voit siirtyä toteuttavaan alikarttaan klikkaamalla arviolaatikkoa.</b>";
			}

			return base;
			
		} else if(classList.contains("tavoiteMeter")) {
			var base = "<h2>Laskennallinen arvio</h2>";
			base += "<p><b> Sisempien laatikoiden perusteella laskettu arvio.</b>";
			return base;
		}
		
		if(e.parentNode == document) return null;
		
		return tryToGetInfo(e.parentNode);
	    
	}

	document.addEventListener('mousemove', function (event) {

		tooltipDIV.style.visibility = "hidden";
		
        clearTimeout(notification);
        notification = setTimeout(function() { 
    		if(event.target == undefined) return;
    		console.log(event.target);
    		var info = tryToGetInfo(event.target);
    		if(info != null) {
    			tooltipDIV.style.left = event.clientX + "px";
    			tooltipDIV.style.top = event.clientY + "px";
    			tooltipDIV.innerHTML = info;
    			tooltipDIV.style.visibility = "visible";
    		}
        }, 3000);
		
	});
	
	var fillPattern = svg.append("defs").append("pattern")
	.attr("id", "tavoitePattern_" + strategiaKartta.elementId)
	.attr("patternUnits", "userSpaceOnUse")
	.attr("width", a4ratio*50)
	.attr("height", a4ratio*50)
	.attr("patternTransform", "rotate(65)");
	var fillPatternRectangle1 = fillPattern.append("rect")
	.attr("id", "fillRect1")
	.attr("x", 0)
	.attr("y", 0)
	.attr("width", a4ratio*50)
	.attr("height", a4ratio*26)
	.style("stroke-width", 0)
	.style("fill", "green");
	var fillPatternRectangle2 = fillPattern.append("rect")
	.attr("id", "fillRect2")
	.attr("x", 0)
	.attr("y", a4ratio*25)
	.attr("width", a4ratio*50)
	.attr("height", a4ratio*25)
	.style("stroke-width", 0)
	.style("fill", "red");
	
	var scaleGroup = svg.append("g").classed("scalegroup", true);

	var group = scaleGroup.append("g").classed("maingroup", true);

	var parentsBaseGroup = scaleGroup.append("g").classed("parentsBase", true);
	var parentsGroup = parentsBaseGroup.append("g").classed("parents", true);
	
	var headerGroup = scaleGroup.append("g").classed("headergroup", true);

	if(headerHeight > 0) {
	
		headerGroup.append('rect').classed("outerBoxHeader", true)
		.attr("transform", "translate(" + ((0.5*chartWidth) - 0.5*0.3*chartWidth) + ",5)")
		.attr("width", 0.3*chartWidth)
		.attr("height", headerHeight-4)
		.attr("rx", "2")
		.attr("ry", "2")
		.style("fill", "#00f");

		headerGroup.append('text').classed("outerBoxHeader", true)
		.attr("transform", "translate(" + ((0.5*chartWidth)) + ",15)")
		.attr("text-anchor", "middle")
		.style("font-size", "9px")
		.style("fill", "#fff");
	
	}

	headerGroup.append("text").classed("map lessColumns", true)
	.style("cursor", "pointer")
	.style("font-family", "FontAwesome" ) //Set font-family to FontAwesome for ICONs in the TEXT field
	.style("font-size", "12px" )
    .text(function(d) { return "\uf146";}) // fa-minus-square
	.on("click", function(d,i) {
		d3.event.stopPropagation();
		rootFn.columns(-1, -1, false); 
	});

	headerGroup.append("text").classed("map moreColumns", true)
	.style("cursor", "pointer")
	.style("font-family", "FontAwesome" )
	.style("font-size", "12px" )
    .text(function(d) { return "\uf0fe";}) // fa-plus-square
	.on("click", function(d,i) {
		d3.event.stopPropagation();
		rootFn.columns(-1, -1, true); 
	});

	var tavoiteGroup = scaleGroup.append("g").classed("tavoitegroup", true);

	var naviGroup = scaleGroup.append("g").classed("navigroup", true);

	parentsBaseGroup.append('text').classed("header edit", true);

	group.append('rect').classed("visio", true)
	.attr("rx", "2")
	.attr("ry", "2");

	group.append('text').classed("mittariStatus", true);

	group.append('text').classed("visio edit", true);

	naviGroup.append("text").classed("alas", true)
	.attr("y", "20")
	.text("Alatason kartat:");

}

function shadeColor(color, percent) {

    var R = parseInt(color.substring(1,3),16);
    var G = parseInt(color.substring(3,5),16);
    var B = parseInt(color.substring(5,7),16);

    R = parseInt(R * (100 + percent) / 100);
    G = parseInt(G * (100 + percent) / 100);
    B = parseInt(B * (100 + percent) / 100);

    R = (R<255)?R:255;  
    G = (G<255)?G:255;  
    B = (B<255)?B:255;  

    var RR = ((R.toString(16).length==1)?"0"+R.toString(16):R.toString(16));
    var GG = ((G.toString(16).length==1)?"0"+G.toString(16):G.toString(16));
    var BB = ((B.toString(16).length==1)?"0"+B.toString(16):B.toString(16));

    return "#"+RR+GG+BB;
}

// This is a tight height of the text area
function textHeight(linecount, fontsize) {
  if(linecount == 1) {
	  // a dy=0.2em is added for oneliners in wrapFocus
	  return 1.4*fontsize;
  } else {
	  return fontsize + (linecount-1)*1.1*fontsize;
  }
}

function refresh(rootFn) {

	var thisElement = rootFn.getElement();
	
	thisElement.style.width = "100%";
	thisElement.style.height = "100%";
	thisElement.style.overflowY = "auto";
	
	var state = rootFn.getState();
	var strategiaKartta = state.model;
	
	//var rect = thisElement.getBoundingClientRect();
	//strategiaKartta.width = rect.width;
	
	var overlayCount = strategiaKartta.path.length;
	
	for (var i=0;i<strategiaKartta.tavoitteet.length;i++) {
		var outer =  strategiaKartta.tavoitteet[i];
		outer.width = (1.0/strategiaKartta.columns) * (chartWidth -2*overlayCount*overlayMargin - (1+strategiaKartta.columns) * boxPad);
		for (var j=0;j<outer.innerboxes.length;j++) {
			var inner = outer.innerboxes[j];
			inner.width = (1.0/outer.columns) * (outer.width - (1 + outer.columns) * boxPad);
			for (var k=0;k<inner.meters.length;k++) {
				var meter = inner.meters[k];
				meter.width = inner.width;
			}
			for (var k=0;k<inner.extras.length;k++) {
				var extra = inner.extras[k];
				extra.width = (1.0/inner.columns) * (inner.width - (1 + inner.columns) * boxPad);
			}
			
		}
	}
	
	var root = d3.select(thisElement).datum(strategiaKartta);
	
	var ratio = strategiaKartta.width / chartWidth;

	root.select("#fillRect1").style("fill", strategiaKartta.tavoiteColor);
	root.select("#fillRect2").style("fill", shadeColor(strategiaKartta.tavoiteColor, 10));
	
	var svg = root.select("svg");
	
	svg.attr("width", chartWidth*ratio);
	svg.attr("height", "100%");

	var scaleGroup = root.select("g.scalegroup")
	.attr("transform", function(d) { return "scale(" + ratio + ")"; });

	var mainGroup = root.select("g.maingroup");
	var headerGroup = root.select("g.headergroup");
	var tavoiteGroup = root.select("g.tavoitegroup");
	var parentsGroup = root.select("g.parents");

	var parents = parentsGroup.selectAll("g.parent")
	.data(function(d) { 
		return d.path; 
		});
	
	creates = parents.enter().append("g").classed("parent", true);
	
	creates.append("rect").classed("parent1", true);
	creates.append("rect").classed("parent2", true);
	creates.append("text").classed("parent link", true);
	
	parents.exit().remove();
	
	let showVision = strategiaKartta.showVision;
	let nonEmptyVision = strategiaKartta.visio.length > 0;
	let currentVisionWidth = showVision ? (nonEmptyVision ? visioWidth : 80) : 0;
	let currentVisionX = nonEmptyVision ? visioX : (visioX + visioWidth - currentVisionWidth);
	
	svg.select("rect.visio")
	.attr("transform", "translate(" + currentVisionX + "," + visioBaseY + ")")
	.attr("width", currentVisionWidth)
	.attr("height", showVision ? (visioHeight-25) : "0");
	
	mainGroup.select("text.uusiTavoite")
	.attr("visibility", function(d) { return ((state.logged && state.edit) && state.edit) ? "visible" : "hidden"; });
	
	mainGroup.select("text.visio.edit")
	.attr("transform", "translate(" + (currentVisionX + 0.5*currentVisionWidth) + "," + (visioBaseY+a4ratio*40) + ")")
	.text(function(d) {
		if(d.visio.length > 0) return d.visio;
		else if(((state.logged && state.edit) && !d.copy)) return "Lisää visio";
		else return "(ei visiota)";
	})
	.attr("visibility", strategiaKartta.showVision ? "visible" : "hidden")
	.style("cursor", function(d) { return ((state.logged && state.edit) && state.edit) ? "text" : "default"; })
	.on("click", function(d) { 
		if((state.logged && state.edit))  {
			d3.event.stopPropagation();
			rootFn.editVision();
		}
	})
	.on("mouseover", function(d,i) { if((state.logged && state.edit) && !d.copy) d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", "#fff"); })
	.attr("fill", "#000000")
	.call(wrapVisio, currentVisionWidth-2*textPad);

	headerGroup.select("text.outerBoxHeader")
	.text(strategiaKartta.tavoiteDescription);

	headerGroup.select("rect.outerBoxHeader")
	.style("fill", strategiaKartta.tavoiteColor );

	var columnButtonOffset =  - overlayMargin*strategiaKartta.path.length;

	headerGroup.select("text.moreColumns")
	.attr("visibility", function(d) { return ((state.logged && state.edit) && state.edit) ? "visible" : "hidden"; })
	.attr("transform", "translate(" + ((chartWidth-28)) + "," + columnButtonOffset + ")");
	
	headerGroup.select("text.lessColumns")
	.attr("visibility", function(d) { return ((state.logged && state.edit) && state.edit) ? "visible" : "hidden"; })
	.attr("transform", "translate(" + ((chartWidth-16)) + "," + columnButtonOffset + ")");
	

	
/*	mainGroup.select("text.goalLegend")
	.text(function(d) { return strategiaKartta.tavoiteDescription; });

	mainGroup.select("rect.goalLegend")
	.style("fill", function(d) { return strategiaKartta.tavoiteColor; });

	mainGroup.select("text.focusLegend")
	.text(function(d) { return strategiaKartta.painopisteDescription; });

	mainGroup.select("rect.focusLegend")
	.style("fill", function(d) { return strategiaKartta.painopisteColor; });*/

	tavoitteet(rootFn, tavoiteGroup, strategiaKartta);
	
	// layout after text wrapping
	var pps = thisElement.querySelectorAll('g.painopiste');
	for (var i=0;i<pps.length;i++) {
		
		var pp = pps[i];
		var ppData = pp.__data__;
		
		var ppRect = pp.childNodes[0];
		var content = pp.childNodes[4];
		var id = content.childNodes[0];
		var text = content.childNodes[1];

		content.setAttribute('transform', 'translate(0,0)');
		ppRect.setAttribute('width', 10);

		var tags = pp.querySelectorAll('g.painopisteTag');
		var meters = pp.querySelectorAll('g.painopisteMeter');
		var extras = pp.querySelectorAll('g.innerBoxExtra');
		
		var tagHeight = ((tags.length > 0) ? (painopisteTagHeight+boxPad) : 0.0);
		var meterHeight = ((meters.length > 0) ? (painopisteMeterHeight+boxPad) : 0.0);
		var extraHeight = 0.0;

		var lines1 = parseInt(id.getAttribute('lineCount'),10);
		var lines2 = parseInt(text.getAttribute('lineCount'),10);

		var idAreaHeight = textHeight(lines1, 6.0);
		// 0.5 em offset on both sides of id element + baseline
		var textOffset = 1.0*6.0 + 1.0*9.0;
		
		text.setAttribute("transform", "translate(" + (0.5*ppData.width) + ", " + (textOffset+idAreaHeight) + ")");
		id.setAttribute("transform", "translate(" + (0.5*ppData.width) + ", " + (1.5*6.0) + ")");
		
		var contentHeight = (1.0*6.0 + 0.5*9.0 + idAreaHeight + textHeight(lines2, 9.0));
		
		var currentY = 0;
		var currentX = boxPad;
		for (var j=0;j<tags.length;j++) {
			
			var g = tags[j];
			
			var rect = g.childNodes[0];
			var text = g.childNodes[1];
			var wordLength = text.getComputedTextLength();
			
			rect.setAttribute('width', '' + (wordLength+4));

			var newX = currentX + wordLength + 4 + boxPad;
			
			if(newX > (ppData.width-2*boxPad)) {
				currentX = boxPad;
				newX = currentX + wordLength + 4 + boxPad;
				currentY += painopisteTagHeight+boxPad;
				tagHeight += painopisteTagHeight+boxPad;
			}
			
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			currentX = newX;

		}
		
		currentY = 0;
		currentX = boxPad;
		
		for (var j=0;j<meters.length;j++) {
			
			var g = meters[j];
			var rect = g.childNodes[0];
			var text = g.childNodes[1];
			var wordLength = text.getComputedTextLength();
			
			rect.setAttribute('width', '' + (wordLength+4));
			
			var newX = currentX + wordLength + 4 + boxPad;
			
			if(newX > (ppData.width-2*boxPad)) {
				currentX = boxPad;
				newX = currentX + wordLength + 4 + boxPad;
				currentY += painopisteMeterHeight+boxPad;
				meterHeight += painopisteMeterHeight+boxPad;
			}
			
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			currentX = newX;
			
		}

		var rowHeight = 0.0;
		currentY = 0;
		currentX = boxPad;

		for (var j=0;j<extras.length;j++) {
			
			var g = extras[j];
			var data = g.__data__;
			
			var rect = g.childNodes[0];
			var id = g.childNodes[1];
			var text = g.childNodes[2];
			
			var lines1 = parseInt(id.getAttribute('lineCount'),10);
			var lines2 = parseInt(text.getAttribute('lineCount'),10);

			var idAreaHeight = textHeight(lines1, 6.0);
			var textAreaHeight = textHeight(lines2, 6.0);
			
			var height = idAreaHeight + textAreaHeight + 10;
			
			if(height > rowHeight) rowHeight = height;
			
			//var wordLength = text.getComputedTextLength();
			
			rect.setAttribute('width', '' + data.width);
			
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			
			text.setAttribute("transform", "translate(" + 0.5 * data.width + ",0)");
			id.setAttribute("transform", "translate(" + 0.5 * data.width + ",0)");

			
			if((j%ppData.columns) == (ppData.columns-1)) {
				currentX = boxPad;
				currentY += rowHeight+boxPad;
				extraHeight += rowHeight+boxPad;
				rowHeight = 0.0;
			} else {
				currentX += boxPad + data.width;
			}

			rect.setAttribute('height', height);

		}
		
		// The current row
		if(rowHeight > 0)
			extraHeight += rowHeight + boxPad;
		
		// Font size is 9px
		var pxHeight = contentHeight + meterHeight + tagHeight + extraHeight;
		
		ppRect.setAttribute('height', pxHeight);
		ppRect.setAttribute('width', ppData.width);

		var tagsG = pp.querySelectorAll('g.painopisteTags');
		tagsG[0].setAttribute('transform', 'translate(0, ' + (pxHeight-meterHeight-tagHeight-extraHeight) + ')');
		var meterG = pp.querySelectorAll('g.painopisteMeters');
		meterG[0].setAttribute('transform', 'translate(0, ' + (pxHeight-meterHeight-extraHeight) + ')');
		var extraG = pp.querySelectorAll('g.innerBoxExtras');
		extraG[0].setAttribute('transform', 'translate(0, ' + (pxHeight-extraHeight) + ')');
		
	}

	var ts = thisElement.querySelectorAll('g.tavoite');
	for (var i=0;i<ts.length;i++) {
		
		var t = ts[i];
		var tData = t.__data__;
		
		var text = t.childNodes[2];
		
		var tags = t.querySelectorAll('g.tavoiteTag');
		var meters = t.querySelectorAll('g.tavoiteMeter');
		
		var tagHeight = ((tags.length > 0) ? (tavoiteTagHeight+boxPad) : 0.0);
		var meterHeight = 0.0;//((meters.length > 0) ? (tavoiteMeterHeight+boxPad) : 0.0);

		var lines = parseInt(text.getAttribute('lineCount'),10);
		var contentHeight = (25 + lines  * 11.0);
		
		var currentY = contentHeight;
		var currentX = boxPad;
		for (var j=0;j<tags.length;j++) {
			
			var g = tags[j];
			//var fillRect = g.childNodes[0];
			var rect = g.childNodes[0];
			var text = g.childNodes[1];
			var wordLength = text.getComputedTextLength();
			
			//fillRect.setAttribute('width', '' + g.__data__.fillRatio*(wordLength+4));
			rect.setAttribute('width', '' + (wordLength+4));
			
			var newX = currentX + wordLength + 4 + boxPad;
			
			if(newX > (tData.width-2*boxPad)) {
				currentX = boxPad;
				newX = currentX + wordLength + 4 + boxPad;
				currentY += tavoiteTagHeight+boxPad;
				tagHeight += tavoiteTagHeight+boxPad;
			}
			
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			currentX = newX;

		}
		
		currentY = 0;
		currentX = boxPad;
		for (var j=0;j<meters.length;j++) {
			
			var g = meters[j];
			var rect = g.childNodes[0];
			var text = g.childNodes[1];
			var wordLength = text.getComputedTextLength();
			
			rect.setAttribute('width', '' + (wordLength+4));
			
			var newX = currentX + wordLength + 4 + boxPad;
			
			if(newX > (tData.width-2*boxPad)) {
				currentX = boxPad;
				newX = currentX + wordLength + 4 + boxPad;
				currentY += tavoiteMeterHeight+boxPad;
				meterHeight += tavoiteMeterHeight+boxPad;
			}
			
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			currentX = newX;

		}
		
		var ps = t.querySelectorAll('g.painopiste');
		var currentY = contentHeight + tagHeight;
		var rowHeight = 0.0;
		currentX = boxPad;
		
		for (var j=0;j<ps.length;j++) {
			var g = ps[j];
			var ppData = g.__data__;
			var rect = g.childNodes[0];
			var height = parseInt(rect.getAttribute('height'),10);
			if(height > rowHeight) rowHeight = height;
			g.setAttribute('transform', 'translate(' + currentX + ',' + currentY + ')');
			if((j%tData.columns) == (tData.columns-1)) {
				currentY += rowHeight + boxPad;
				currentX = boxPad;
				rowHeight = 0.0;
			} else {
				 currentX += (boxPad + ppData.width);				
			}
		}

		currentY += meterHeight;

		var trect = t.childNodes[0];
		var h = currentY+rowHeight+boxPad;
		trect.setAttribute('height', h);
		trect.setAttribute('width', tData.width);
		
		var meterG = t.querySelectorAll('g.tavoiteMeters');
		meterG[0].setAttribute('transform', 'translate(0,' + boxPad + ')');
		
	}

	var targetElement;
	
	var maxY = 0;//tavoiteBaseline;
	var currentY = 0;//tavoiteBaseline + overlayCount*overlayMargin;
	var rowHeight = 0.0;
	var rowStart = 0;
	for (var i=0;i<ts.length;i++) {
		
		var g = ts[i];
		var data = g.__data__;
		
		var rect = g.childNodes[0];
		
		if(data.startNewRow) {
			
			for( var j=rowStart;j<i;j++) {
				var gg = ts[j];
				gg.childNodes[0].setAttribute('height', rowHeight);
			}
			
			currentY += rowHeight + boxPad;
			rowHeight = 0.0;
			rowStart = i;
		}
		
		var height = parseInt(rect.getAttribute('height'),10);
		if(height > rowHeight) rowHeight = height;
		
		var xOffset = strategiaKartta.width*data.xOffset;
		var yOffset = data.yOffset;
		rowHeight += yOffset;

		g.setAttribute('transform', 'translate(' + (overlayCount*overlayMargin + boxPad+xOffset+(data.width+boxPad)*(i-rowStart)) + ',' + (currentY+yOffset) + ')');
		
		if(strategiaKartta.scrollFocus == i) {
			targetElement = rect;
		}
		
		if((currentY+yOffset+height) > maxY) maxY = (currentY+yOffset+height); 
		
	}
	
	var tavoiteAreaHeight = maxY+boxPad;

	let parentHeaderWidth = nonEmptyVision ? (0.5*chartWidth) : (0.8*chartWidth);
	
	parentsGroup.selectAll("text.parent")
	.data(function(d) { 
		return d.path; 
		})
	.on("click", function(d,i) { 
		d3.event.stopPropagation();
		rootFn.navigate(d.uuid); 
	})
	.on("mouseover", function(d,i) { d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", d.textColor); })
	.style("cursor", function(d) { return "pointer"; })
    .style("fill", function(d,i) { return d.textColor; })
    .style("visibility", function(d,i) { 
    	return "visible"; 
    	})
    .style("font-size", function(d,i) { 
    	return "10px"; 
    	})
	.attr("transform", function(d,i) { return "translate(" + (boxPad+overlayMargin*(i+1)) + ",11)"; })
	.text(function(d,i) { return d.text; })
	.call(wrapLineX, parentHeaderWidth - 2*overlayMargin*strategiaKartta.path.length, 1);
	
	parentsGroup.selectAll("rect.parent1")
	.data(function(d) { 
		return d.path; 
		})
	.style("stroke-width", 0)
	.attr("rx", "2")
	.attr("ry", "2")
	.attr("width", function(d,i) { return (parentHeaderWidth - 2*overlayMargin*i);})
	.attr("x", function(d,i) { return boxPad+overlayMargin*i; })
	.style("fill", function(d,i) { return d.backgroundColor; });
	
	parentsGroup.selectAll("rect.parent2")
	.data(function(d) { 
		return d.path; 
		})
	.style("stroke-width", 0)
	.attr("rx", "2")
	.attr("ry", "2")
	.attr("width", function(d,i) { return chartWidth-2*overlayMargin*i - 2*boxPad; })
	.attr("height", function(d,i) {  return headerHeight+tavoiteAreaHeight+2*overlayMargin*(strategiaKartta.path.length-i-1); })
	.attr("x", function(d,i) { return boxPad+overlayMargin*i; })
	.style("fill", function(d,i) { return d.backgroundColor; });
	
	scaleGroup.select("text.header")
	.on("click", function(d) { 
		if((state.logged && state.edit)) {
			d3.event.stopPropagation();
			rootFn.editHeader();
		}
	})
	.style("cursor", function(d) { return (state.logged && state.edit) ? "text" : "default"; })
	.on("mouseover", function(d,i) { if((state.logged && state.edit) && !d.copy) d3.select(this).attr("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).attr("fill", "#000000"); })
	.attr("fill", "#000000")
	.text(function(d) { 
		return d.text.length > 0 ? d.text : "<ei nimeä>"; 
	})
	.call(wrapLineN, parentHeaderWidth - 2*overlayMargin*strategiaKartta.path.length);
	
	var parentGs = thisElement.querySelectorAll('g.parent');
	var parentGOffset = 0;
	
	for (var i=0;i<parentGs.length;i++) {
		
		var g = parentGs[i];
		
		var rect1 = g.childNodes[0];
		var rect2 = g.childNodes[1];
		var text = g.childNodes[2];
		var bBox = text.getBBox();

		if(i == parentGs.length - 1) {
			
			text.style.fontSize = '14px';
			text.style.visibility = 'hidden';

			var header = scaleGroup.select("text.header")
			.attr("transform", function(d) { return "translate(" + (5+boxPad + overlayMargin*strategiaKartta.path.length) + "," + (15 + parentGOffset) + ")"; });
			bBox = header[0][0].getBBox();
			
	    }
		
		//g.setAttribute('transform', 'translate(0,' + parentGOffset + ')');
		
		rect1.setAttribute('y', parentGOffset);
		text.setAttribute('y', parentGOffset);

		parentGOffset += overlayMargin + bBox.height;
		
	}
	
	for (var i=0;i<parentGs.length;i++) {
		
		var g = parentGs[i];
		
		var rect1 = g.childNodes[0];
		var rect2 = g.childNodes[1];

		var y = parentGOffset - overlayMargin*(parentGs.length-i-1);
		
		rect2.setAttribute('y', y);
		
		var rect1Y = rect1.getAttribute('y');
		rect1.setAttribute('height', (3+y-rect1Y));
		
	}

	
	var parentHeight = parentGOffset;
	var parentBaseline = 10;
	
	if(strategiaKartta.showVision) {
		var visioBaseline = visioBaseY+visioHeight+boxPad+overlayMargin*strategiaKartta.path.length;
		if(parentBaseline+parentHeight < visioBaseline)
			parentBaseline = visioBaseline-parentHeight;
	}
	
	var naviBaseline = parentBaseline + parentHeight + headerHeight + tavoiteAreaHeight + overlayMargin*(strategiaKartta.path.length-1);
	
	var naviGroup = root.select("g.navigroup")
	.attr("transform", function(d) { return "translate(0," + naviBaseline + ")"; });

	naviGroup.select("text.alas")
	.attr("transform", function(d) { return "translate(" + 0.5*chartWidth + ",0)"; })
	.attr("visibility", function(d) { return (strategiaKartta.alikartat.length > 0) ? "visible" : "hidden"; });
	
	naviGroup.select("text.uusiAliorganisaatio")
	.attr("visibility", function(d) { return (state.logged && state.edit) ? "visible" : "hidden"; })
	.attr("transform", function(d) { return "translate(" + (a4ratio*25) + ",0)"; });

	var naviHeight = navigointiLinkit(rootFn, naviGroup, strategiaKartta);

	var parentsBaseGroupElement = thisElement.querySelectorAll('g.parentsBase')[0]; 
	parentsBaseGroupElement.setAttribute('transform', 'translate(0,' + parentBaseline + ')');
	var headerGroupElement = thisElement.querySelectorAll('g.headergroup')[0]; 
	headerGroupElement.setAttribute('transform', 'translate(0,' + (parentBaseline+parentHeight) + ')');
	var tavoiteGroupElement = thisElement.querySelectorAll('g.tavoitegroup')[0]; 
	tavoiteGroupElement.setAttribute('transform', 'translate(0,' + (parentBaseline+parentHeight+headerHeight) + ')');
	
	let meterY = parentBaseline + parentHeight - overlayMargin*(strategiaKartta.path.length-1) - 12;
	svg.select("text.mittariStatus")
	.text(function(d) { return strategiaKartta.meterStatus; })
	.attr("visibility", function(d) { return strategiaKartta.meterStatus != "" ? "visible": "hidden"; })
	.attr("transform", "translate(" + (visioX + visioWidth - 80.0) + "," + meterY + ")");

	if(strategiaKartta.showNavigation) {
		svg.attr("height", (naviBaseline + naviHeight)*ratio);
	} else {
		svg.attr("height", naviBaseline*ratio);
	}
	
}

function tavoitteet(rootFn, svgs, map) {

	var state = rootFn.getState();
	var strategiaKartta = state.model;

	var tavoitteet = svgs.selectAll("g.tavoite")
	.data(function(d) { return d.tavoitteet; });
	
	creates = tavoitteet.enter().append("g").classed("tavoite", true);

	creates.append("rect").classed("tavoite", true)
	//.attr("width", "" + strategiaKartta.width+"px")
	.attr("rx", "2")
	.attr("ry", "2")
	.on("click", function(d,i) { 
		d3.event.stopPropagation();
		if(d.uuid.length > 0)
			rootFn.navi4(d3.event.pageX, d3.event.pageY, d.uuid);
		else
			rootFn.navi(d3.event.pageX, d3.event.pageY, d.realIndex); 
	});
	
	creates.append("text").classed("tavoite idElement", true);

	creates.append("text").classed("tavoite textElement", true);

	creates.append("text").classed("tavoite drillElement", true);

	creates.append("text").classed("tavoite moreColumns", true)
	.style("cursor", "pointer")
	.style("font-family", "FontAwesome" )
	.style("font-size", "11px" )
    .text(function(d) { return "\uf0fe";}) // fa-plus-square
	.on("click", function(d,i) {
		d3.event.stopPropagation();
		rootFn.columns(d.realIndex, -1, true); 
	});

	creates.append("text").classed("tavoite lessColumns", true)
	.style("cursor", "pointer")
	.style("font-family", "FontAwesome" ) //Set font-family to FontAwesome for ICONs in the TEXT field
	.style("font-size", "11px" )
    .text(function(d) { return "\uf146";}) // fa-minus-square
	.on("click", function(d,i) {
		d3.event.stopPropagation();
		rootFn.columns(d.realIndex, -1, false); 
	});

	
	creates.append("text").classed("tavoite changeSuggestionLinkElement", true);
	creates.append("text").classed("tavoite changeSuggestionChildLinkElement", true);
	
	creates.append("g").classed("tavoiteMeters", true);

	svgs.selectAll("text.tavoite.idElement")
	.data(function(d) { return d.tavoitteet; })
	.attr("transform", function(d) { return "translate(" + (0.5*d.width) + ", " + (a4ratio*26) + ")"; })
	.style("cursor", function(d) { return ((state.logged && state.edit) && !d.copy) ? "text" : "default"; })
	.style("fill", function(d) { return map.tavoiteTextColor; })
	.style("-moz-user-select", "text")
	.style("-webkit-user-select", "text")
	.style("-ms-user-select", "text")
	.on("click", function(d,i) {
		if((state.logged && state.edit) && !d.copy) {
			d3.event.stopPropagation();
			rootFn.editOuterBox(d.realIndex); 
		}
	}
	)
	.text(function(d) { return d.id; });

	svgs.selectAll("text.tavoite.textElement")
	.data(function(d) { return d.tavoitteet; })
	.attr("transform", function(d) { return "translate(" + (0.5*d.width) + ", " + (a4ratio*60) + ")"; })
	.style("cursor", function(d) { return ((state.logged && state.edit) && !d.copy) ? "text" : "default"; })
	.text(function(d) { return d.text.length > 0 ? d.text : "<ei nimeä>"; })
	.style("fill", function(d) { return map.tavoiteTextColor; })
	.style("-moz-user-select", "text")
	.style("-webkit-user-select", "text")
	.style("-ms-user-select", "text")
	.on("click", function(d,i) { 
		if((state.logged && state.edit) && !d.copy) { 
			d3.event.stopPropagation();
			rootFn.editOuterBox(d.realIndex); 
		}
	})
	.on("mouseover", function(d,i) { if((state.logged && state.edit) && !d.copy) d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", map.tavoiteTextColor); })
    .each(function(d) {
			wrapMargin(this, d, 2*textPad);
		});

	svgs.selectAll("text.tavoite.moreColumns")
	.attr("visibility", function(d) { return ((state.logged && state.edit) && state.edit) ? "visible" : "hidden"; })
	.data(function(d) { return d.tavoitteet; })
	.attr("transform", function(d) {
		let xPosition = state.shiftLessMoreColumns ? 45 : 5;
		return "translate(" + (xPosition) + ", " + (a4ratio*30) + ")";
	})
	.style("fill", function(d) { return map.tavoiteTextColor; })
	.on("mouseover", function(d,i) { d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", map.tavoiteTextColor); });
	
	svgs.selectAll("text.tavoite.lessColumns")
	.attr("visibility", function(d) { return ((state.logged && state.edit) && state.edit) ? "visible" : "hidden"; })
	.data(function(d) { return d.tavoitteet; })
	.attr("transform", function(d) {
		let xPosition = state.shiftLessMoreColumns ? 56 : 16;
		return "translate(" + (xPosition) + ", " + (a4ratio*30) + ")";
	})
	.style("fill", function(d) { return map.tavoiteTextColor; })
	.on("mouseover", function(d,i) { d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", map.tavoiteTextColor); });
	
	svgs.selectAll("text.tavoite.drillElement")
	.data(function(d) { return d.tavoitteet; })
	.attr("transform", function(d) { return "translate(" + (d.width-20) + ", " + (a4ratio*36) + ")"; })
	.style("cursor", "pointer")
	.style("fill", function(d) { return map.tavoiteTextColor; })
	.style("font-family", "FontAwesome" ) //Set font-family to FontAwesome for ICONs in the TEXT field
	.style("font-size", "16px" )
	.attr("visibility", function(d) { return d.drill ? "visible" : "hidden"; })
	.on("mouseover", function(d,i) { d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", map.tavoiteTextColor); })
	.on("click", function(d,i) {
		d3.event.stopPropagation();
		rootFn.drill(d.realIndex); 
	})
    .text(function(d) { return "\uf090";}); //This is the fa-sign-in icon from FontAwesome
	
	//ChangeSuggestion button element for child-element opening
	svgs.selectAll("text.tavoite.changeSuggestionChildLinkElement")
	.data(function(d) { return d.tavoitteet; })
	.attr("transform", function(d) { return "translate(" + (d.width-40) + ", " + (a4ratio*36 - 1) + ")"; })
	.style("cursor", "pointer")
	.style("fill", function(d) { return "#000000"; })
	.style("font-family", "FontAwesome" )
	.style("font-size", "14px" )
	.attr("visibility", function(d) { return d.childElementsHaveChangeSuggestions ? "visible" : "hidden"; })
	.on("mouseover", function(d,i) { d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", "#000000"); })
	.on("click", function(d,i) {
		d3.event.stopPropagation();
		rootFn.openRelatedChangeSuggestions(d.realIndex, true); 
	})
    .text(function(d) { return "\uf086";}); //Comments icon from Font Awesome.
	
	//ChangeSuggestion button elements for current element opening
	svgs.selectAll("text.tavoite.changeSuggestionLinkElement")
	.data(function(d) { return d.tavoitteet; })
	.attr("transform", function(d) { return "translate(" + (d.width-60) + ", " + (a4ratio*36 - 2) + ")"; })
	.style("cursor", "pointer")
	.style("fill", function(d) { return "#000000"; })
	.style("font-family", "FontAwesome" )
	.style("font-size", "12px" )
	.attr("visibility", function(d) { return d.hasChangeSuggestions ? "visible" : "hidden"; })
	.on("mouseover", function(d,i) { d3.select(this).style("fill", "#808080"); })
	.on("mouseout", function(d,i) { d3.select(this).style("fill", "#000000"); })
	.on("click", function(d,i) {
		d3.event.stopPropagation();
		rootFn.openRelatedChangeSuggestions(d.realIndex, false); 
	})
    .text(function(d) { return "\uf075";}); //Comment icon from Font Awesome.
	
	svgs.selectAll("rect.tavoite")
	.data(function(d) { return d.tavoitteet; })
	.style("fill", function(d) { return d.stripes ? "url(#tavoitePattern_" + strategiaKartta.elementId + ")" : d.color; })
	.on("mouseover", function(d,i) { d3.select(this).style("stroke-width", 1); })
	.on("mouseout", function(d,i) { d3.select(this).style("stroke-width", 0); });

	tavoitteet.exit().remove();

	// Tavoitteiden aihetunnisteet
	/*{

		var tagG = svgs.selectAll("g.tavoiteTags")
		.data(function(d) { 
			return d.tavoitteet; 
		});

		var tavoiteTags = tagG.selectAll("g.tavoiteTag")
		.data(function(d) {
			return d.tags;
		});
		
		creates = tavoiteTags.enter().append("g").classed("tavoiteTag", true);
		
		creates.append("rect").classed("tavoiteTag", true)
		.data(function(d) { return d.tags; })
		.attr("height", "" + (tavoiteTagHeight))
		.attr("rx", "" + a4ratio*2)
		//.style("cursor", "pointer")
		.style("stroke-width", 0)
		.style("fill", function(d) { return d.color;});
		*/

/*		creates.append("rect").classed("tavoiteTag", true)
		.data(function(d) { return d.tags; })
		.attr("rx", "" + a4ratio*2)
		.attr("height", "" + tavoiteTagHeight)
		.style("cursor", "pointer")
		.style("stroke-width", "1")
		.style("stroke", function(d) { return d.color;})
		.style("fill", "none");*/

	/*
		creates.append("text").classed("tavoiteTag", true)
		.data(function(d) { return d.tags; })
		.attr("x", "2")
		.attr("y", "7")
		.text(function(d,i) { return d.text; });
		
		tavoiteTags.exit().remove();
		
		tagG.selectAll("rect.tavoiteTag")
		.data(function(d,i) { return d.tags; })
		.style("fill", function(d) { return d.color;});
		
		tagG.selectAll("text.tavoiteTag")
		.data(function(d) { return d.tags; })
		.text(function(d) { return d.text; });
		
	}*/
	
	// Tavoitteiden mittarit
	{

		var meterG = svgs.selectAll("g.tavoiteMeters")
		.data(function(d,i) { 
			return d.tavoitteet; 
		});

		var tavoiteMeters = meterG.selectAll("g.tavoiteMeter")
		.data(function(d,i) {
			return d.meters;
		});

		creates = tavoiteMeters.enter().append("g").classed("tavoiteMeter", true);
		
		creates.append("rect").classed("tavoiteMeter", true)
		.attr("height", "" + tavoiteMeterHeight)
		.attr("rx", "1")
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.selectMeter(d.tavoite, d.painopiste, d.realIndex, d.link); 
		})
		.style("stroke", "#111")
		.style("stroke-width", 0)
		.style("fill", function(d) { return d.color;})
		.each(function(d) {
			wrapMargin(this, d, 2*textPad);
		});

		creates.append("text").classed("tavoiteMeter", true)
		.attr("x", "2")
		.attr("y", "8")
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.selectMeter(d.tavoite, d.painopiste, d.realIndex, d.link); 
		});

		tavoiteMeters.exit().remove();

		meterG.selectAll("rect.tavoiteMeter")
		.data(function(d,i) { return d.meters; })
		.style("fill", function(d) { return d.color;});
		
		meterG.selectAll("text.tavoiteMeter")
		.data(function(d) { return d.meters; })
		.text(function(d) { return d.text; });
		
	}
	
	{

		// Painopisteet
		var innerboxes = tavoitteet.selectAll("g.painopiste")
		.data(function(d) {
			return d.innerboxes;
		});

		creates = innerboxes.enter().append("g").classed("painopiste", true);

		creates.append("rect").classed("painopiste", true)
		.attr("width", function(d) {
			return 10;
			//return d.width;
		})
		.attr("height", painopisteHeight)
		.attr("rx", "2")
		.attr("ry", "2")
		.on("mouseover", function(d,i) { d3.select(this).style("stroke-width", 1); })
		.on("mouseout", function(d,i) { d3.select(this).style("stroke-width", 0); })
		//.style("cursor", function(d) { return "pointer"; })
		.on("click", function(d,i) {
			d3.event.stopPropagation();
			if(d.uuid.length != 0)
				rootFn.navi4(d3.event.pageX, d3.event.pageY, d.uuid);
			else
				rootFn.navi2(d3.event.pageX, d3.event.pageY, d.tavoite, d.realIndex);
		});
		
		creates.append("g").classed("painopisteMeters", true);
		creates.append("g").classed("painopisteTags", true);
		creates.append("g").classed("innerBoxExtras", true);

		creates = creates.append("g").classed("painopisteContent", true);

		// Vertical alignment for text is baseline - start text from 0.5em
		creates.append("text").classed("painopiste idElement", true)

		creates.append("text").classed("painopiste textElement", true);
		
		creates.append("text").classed("painopiste moreColumns", true)
		.style("cursor", "pointer")
		.style("font-family", "FontAwesome" )
		.style("font-size", "10px" )
	    .text(function(d) { return "\uf0fe";}) // fa-plus-square
		.on("click", function(d,i) {
			d3.event.stopPropagation();
			rootFn.columns(d.tavoite, d.realIndex, true); 
		});
		
		creates.append("text").classed("painopiste lessColumns", true)
		.style("cursor", "pointer")
		.style("font-family", "FontAwesome" ) //Set font-family to FontAwesome for ICONs in the TEXT field
		.style("font-size", "10px" )
	    .text(function(d) { return "\uf146";}) // fa-minus-square
		.on("click", function(d,i) {
			d3.event.stopPropagation();
			rootFn.columns(d.tavoite, d.realIndex, false); 
		});
		
		creates.append("text").classed("painopiste infoElement", true);
		creates.append("text").classed("painopiste meterElement", true);

		tavoitteet.selectAll("text.painopiste.infoElement")
		.data(function(d,i) {
			return d.innerboxes;
		})
		.attr("transform", function(d) {
			return "translate(" + (d.width-15) + ", 9)";
		});
		
		tavoitteet.selectAll("text.painopiste.meterElement")
		.data(function(d,i) {
			return d.innerboxes;
		})
		.attr("transform", "translate(" + (5) + ", 11)");

		tavoitteet.selectAll("text.painopiste.idElement")
		.data(function(d,i) {
			return d.innerboxes;
		})
	    .style("fill", function(d) { return map.painopisteTextColor; })
	    .style("-moz-user-select", "text")
	    .style("-webkit-user-select", "text")
		.style("-ms-user-select", "text")
		.text(function(d) { return d.id; })
		.each(function(d) {
			wrapMargin(this, d, 4*textPad);
		});

		tavoitteet.selectAll("text.painopiste.textElement")
		.style("-moz-user-select", "text")
	    .style("-webkit-user-select", "text")
		.style("-ms-user-select", "text")
		.data(function(d,i) {
			return d.innerboxes;
		})
	    .style("fill", function(d) { return map.painopisteTextColor; })
		.style("cursor", function(d) { return ((state.logged && state.edit)) ? "text" : "default"; })
		.text(function(d) { return d.text.length > 0 ? d.text : "<ei tekstiä>";})
		.on("click", function(d,i) { 
			if((state.logged && state.edit)) {
				d3.event.stopPropagation();
				rootFn.editInnerBox(d.tavoite, d.realIndex); 
			}
		})
		.on("mouseover", function(d,i) { if((state.logged && state.edit)) d3.select(this).style("fill", "#808080"); })
		.on("mouseout", function(d,i) { d3.select(this).style("fill", map.painopisteTextColor); })
		.each(function(d) {
			wrapMargin(this, d, 2*textPad);
		});

		tavoitteet.selectAll("text.painopiste.infoElement")
		.data(function(d,i) {
			return d.innerboxes;
		})
	    .style("fill", function(d) { return map.painopisteTextColor; })
		.style("cursor", function(d) { return ((state.logged)) ? "pointer" : "default"; })
		.text(function(d) { return "\uf003";}) //This is the fa-envelope-o (e.g. closed letter) icon of FontAwesome
	    .attr("visibility", function(d) {
	    	return d.hasInfo ? "visible" : "hidden"; })
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.displayInfo(d.tavoite, d.realIndex); 
		})
		.on("mouseover", function(d,i) { d3.select(this).style("fill", "#808080"); })
		.on("mouseout", function(d,i) { d3.select(this).style("fill", map.painopisteTextColor); })
		.each(function(d) {
			wrapMargin(this, d, 2*textPad);
		});

		tavoitteet.selectAll("text.painopiste.moreColumns")
		.data(function(d,i) {
			return d.innerboxes;
		})
		.attr("visibility", function(d) { return ((state.logged && state.edit) && state.edit) ? "visible" : "hidden"; })
		.attr("transform", function(d) {
			let xPosition = state.shiftLessMoreColumns ? 22 : 4;
			return "translate(" + (xPosition) + ", " + (a4ratio*28) + ")";
		})
		.style("fill", function(d) { return map.painopisteTextColor; })
		.on("mouseover", function(d,i) { d3.select(this).style("fill", "#808080"); })
		.on("mouseout", function(d,i) { d3.select(this).style("fill", map.painopisteTextColor); });
		
		tavoitteet.selectAll("text.painopiste.lessColumns")
		.data(function(d,i) {
			return d.innerboxes;
		})
		.attr("visibility", function(d) { return ((state.logged && state.edit) && state.edit) ? "visible" : "hidden"; })
		.attr("transform", function(d) {
			let xPosition = state.shiftLessMoreColumns ? 32 : 14;
			return "translate(" + (xPosition) + ", " + (a4ratio*28) + ")";
		})
		.style("fill", function(d) { return map.painopisteTextColor; })
		.on("mouseover", function(d,i) { d3.select(this).style("fill", "#808080"); })
		.on("mouseout", function(d,i) { d3.select(this).style("fill", map.painopisteTextColor); });
		
		tavoitteet.selectAll("text.painopiste.meterElement")
		.data(function(d,i) {
			return d.innerboxes;
		})
	    .style("fill", function(d) { return d.leafMeterColor; })
	    .style("font-size", "10px" )
	    .attr("data-meterDesc", function(d) { return d.leafMeterDesc; })
	    .attr("visibility", function(d) {
	    	return d.hasMeter ? "visible" : "hidden"; })
		.style("cursor", function(d) { return ((state.logged)) ? "pointer" : "default"; })
		.text(function(d) { return "\uf201";})
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			if(state.logged && state.edit) {
				rootFn.displayMeter(d.tavoite, d.realIndex);
			}
		})

		.on("mouseover", function(d,i) { if(state.logged && state.edit) d3.select(this).style("fill", "#808080"); })
		.on("mouseout", function(d,i) { d3.select(this).style("fill", d.leafMeterColor); })
		.each(function(d) {
			wrapMargin(this, d, 2*textPad);
		});

		tavoitteet.selectAll("rect.painopiste")
		.data(function(d,i) {
			return d.innerboxes;
		})
		.style("stroke", "#111")
		.on("mouseover", function(d,i) { d3.select(this).style("stroke-width", 1); })
		.on("mouseout", function(d,i) { d3.select(this).style("stroke-width", 0); });
		
		tavoitteet.selectAll("rect.painopiste")
		.data(function(d,i) {
			return d.innerboxes;
		})
		.style("fill", function(d) { return d.color; });

		innerboxes.exit().remove();

	}

	// Painopisteiden aihetunnisteet
	{

		var tagG = tavoitteet.selectAll("g.painopisteTags")
		.data(function(d) { 
			return d.innerboxes; 
		});

		var painopisteTags = tagG.selectAll("g.painopisteTag")
		.data(function(d) {
			return d.tags;
		});
		
		creates = painopisteTags.enter().append("g").classed("painopisteTag", true);

		creates.append("rect").classed("painopisteTag", true)
		.data(function(d) { return d.tags; })
		.attr("rx", "" + a4ratio*2)
		.attr("height", "" + (painopisteTagHeight))
		.style("cursor", "pointer")
		.style("stroke-width", 0)
		.style("fill", function(d) { return d.color;});

		/*creates.append("rect").classed("painopisteTag", true)
		.data(function(d) { return d.tags; })
		.attr("rx", "" + a4ratio*2)
		.attr("height", "" + painopisteTagHeight)
		.style("cursor", "pointer")
		.style("stroke-width", "1")
		.style("stroke", function(d) { return d.color;})
		.style("fill", "none");*/

		creates.append("text").classed("painopisteTag", true)
		.data(function(d) { return d.tags; })
		.attr("x", "2")
		.attr("y", "6")
		.text(function(d,i) { return d.text; });

		painopisteTags.exit().remove();
		
		tagG.selectAll("rect.painopisteTag")
		.data(function(d,i) { return d.tags; })
		.style("fill", function(d) { return d.color;});
		
		tagG.selectAll("text.painopisteTag")
		.data(function(d) { return d.tags; })
		.text(function(d) { return d.text; });

	}
	
	// Painopisteiden mittarit
	{

		var meterG = tavoitteet.selectAll("g.painopisteMeters")
		.data(function(d,i) { 
			return d.innerboxes;
		});

		var painopisteMeters = meterG.selectAll("g.painopisteMeter")
		.data(function(d,i) {
			return d.meters;
		});
		
		creates = painopisteMeters.enter().append("g").classed("painopisteMeter", true);
		
		creates.append("rect").classed("painopisteMeter", true)
		.attr("width", "" + a4ratio*30)
		.attr("height", "" + painopisteMeterHeight)
		.attr("rx", "1")
        .on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.selectMeter(d.tavoite, d.painopiste, d.realIndex, d.link); 
		})
		.style("stroke", "#111")
		.style("stroke-width", 0)
		.style("cursor", function(d,i) { return d.link.length > 0 ? "pointer" : "default"; })
		.style("fill", function(d) { return d.color;})
		.each(function(d) {
			wrapMargin(this, d, 2*textPad);
		});

		creates.append("text").classed("painopisteMeter", true)
		.attr("x", "2")
		.attr("y", "9")
		.on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.selectMeter(d.tavoite, d.painopiste, d.realIndex, d.link); 
		})
		.style("cursor", function(d,i) { return d.link.length > 0 ? "pointer" : "default"; })
		.on("mouseover", function(d,i) { 
			if(d.link.length > 0) {
				d3.select(this.previousSibling).style("stroke-width", 1); 
			}})
		.on("mouseout", function(d,i) { d3.select(this.previousSibling).style("stroke-width", 0); });

		painopisteMeters.exit().remove();

		meterG.selectAll("text.painopisteMeter")
		.data(function(d,i) { return d.meters; })
		.attr("data-link", function(d) { return d.link.length > 0 ? d.link : "_";})
        .attr("data-meterDesc", function(d) { return d.desc; })
		.text(function(d) { return d.text;});

		meterG.selectAll("rect.painopisteMeter")
		.data(function(d,i) { return d.meters; })
		.attr("data-link", function(d) { return d.link.length > 0 ? d.link : "_";})
        .attr("data-meterDesc", function(d) { return d.desc; })
		.style("fill", function(d) { return d.color;});

	}
	
	// Third level boxes
	{

		var extraG = tavoitteet.selectAll("g.innerBoxExtras")
		.data(function(d,i) { 
			return d.innerboxes;
		});

		var extras = extraG.selectAll("g.innerBoxExtra")
		.data(function(d,i) {
			return d.extras;
		});
		
		creates = extras.enter().append("g").classed("innerBoxExtra", true);
		
		creates.append("rect").classed("innerBoxExtra", true)
		.on("mouseover", function(d,i) { d3.select(this).style("stroke-width", 1); })
		.on("mouseout", function(d,i) { d3.select(this).style("stroke-width", 0); })
		.attr("height", "" + innerBoxExtraHeight)
		.attr("rx", "1")
        .on("click", function(d,i) { 
			d3.event.stopPropagation();
			rootFn.navi3(d3.event.pageX, d3.event.pageY, d.tavoite, d.painopiste, d.realIndex);
		})
		.style("stroke", "#111")
		.style("stroke-width", 0)
		.style("cursor", function(d,i) { return d.link.length > 0 ? "pointer" : "default"; })
		.style("fill", function(d) { return d.color;});

		creates.append("text").classed("innerBoxExtraId", true)
		.attr("y", "9")
		.style("cursor", function(d,i) { return d.link.length > 0 ? "pointer" : "default"; })
		.on("mouseover", function(d,i) { 
			if(d.link.length > 0) {
				d3.select(this.previousSibling).style("stroke-width", 1); 
			}})
		.on("mouseout", function(d,i) { d3.select(this.previousSibling).style("stroke-width", 0); });

		creates.append("text").classed("innerBoxExtraText", true)
		.attr("y", "20")
		.style("cursor", function(d,i) { return d.link.length > 0 ? "pointer" : "default"; })
		.on("mouseover", function(d,i) { 
			if(d.link.length > 0) {
				d3.select(this.previousSibling).style("stroke-width", 1); 
			}})
		.on("mouseout", function(d,i) { d3.select(this.previousSibling).style("stroke-width", 0); });
		
		extras.exit().remove();

		extraG.selectAll("text.innerBoxExtraId")
		.data(function(d,i) { return d.extras; })
		.attr("data-link", function(d) { return d.link.length > 0 ? d.link : "_";})
        .attr("data-meterDesc", function(d) { return d.desc; })
		.text(function(d) { return d.id;})
		.each(function(d) {
			wrapMargin(this, d, 2*textPad);
		});

		extraG.selectAll("text.innerBoxExtraText")
		.data(function(d,i) { return d.extras; })
		.attr("data-link", function(d) { return d.link.length > 0 ? d.link : "_";})
        .attr("data-meterDesc", function(d) { return d.desc; })
		.text(function(d) { return d.text;})
		.each(function(d) {
			wrapMargin(this, d, 2*textPad);
		});

		extraG.selectAll("rect.innerBoxExtra")
		.data(function(d,i) { return d.extras; })
		.attr("data-link", function(d) { return d.link.length > 0 ? d.link : "_";})
        .attr("data-meterDesc", function(d) { return d.desc; })
		.style("fill", function(d) { return d.color;});

	}
	
}

function navigointiLinkit(rootFn, svgs, map) {

	var alikartat = svgs.selectAll("g.alikartta")
	.data(function(d,i) {
		return d.alikartat;
	});

	creates = alikartat.enter().append("g").classed("alikartta", true);

	creates.append("text").classed("alikartta link", true)
	.attr("transform", "translate(0,5)")
	.on("click", function(d,i) { 
		d3.event.stopPropagation();
		rootFn.navigate(d.uuid); 
	});

	svgs.selectAll("g.alikartta")
	.data(function(d,i) {
		return d.alikartat;
	})
	.attr("transform", function(d, i){ 
		return "translate("+ 0.5*chartWidth + "," + (30 + i*a4ratio*37) + ")";
		});

	svgs.selectAll("text.alikartta.link")
	.data(function(d,i) {
		return d.alikartat;
	})
	.text(function(d,i) { return d.text; });

	alikartat.exit().remove();
	
	return 30 + map.alikartat.length * a4ratio*37; 
	
};

function wrapImpl(element, width, allowedNumberOfLines) {
	var text = d3.select(element),
	words = text.text().split(/\s+/).reverse(),
	word,
	line = [],
	y = text.attr("y"),
	tspan = null,
	firstSpan = null;
	text.text(null);
	//allowedNumberOfLines = 3;
	wordLength = 0;
	lineLength = 0;
	lineNumber = 0;
	allowedWidth = width;
	while (word = words.pop()) {
		tspan = text.append("tspan")/*.attr("y", y)*/.text(word+"\u00A0");
		if(firstSpan == null) firstSpan = tspan;
		wordLength = tspan.node().getComputedTextLength();
		lineLength += wordLength; 
		if(word.length > 0 && word.charAt(0) == "#") {
			if(word.indexOf("@") > -1)
				tspan.attr("fill", "#3000ff");
			else if (word.indexOf("%") > -1)
				tspan.attr("fill", "#30ff00");
			else
				tspan.attr("fill", "#ff0003");
		}
		if(lineLength > allowedWidth) {
			lineNumber++;
			if(lineNumber < allowedNumberOfLines) {
				tspan.attr("dy", "1.1em").attr("x", 0);
				lineLength = wordLength;
			} else {
				tspan.text("...");
				break;
			}
			if(lineNumber == allowedNumberOfLines) allowedWidth = 2*width / 3;
		}
	}
	text.attr("lineCount", lineNumber+1);
	return { ln : (lineNumber+1), span : firstSpan };
}

function wrapVisio(text, width) {
	text.each(function() {
		var result = wrapImpl(this, width, 2);
		if(result.ln == 0 && result.span != null)
			result.span.attr("dy", "0.2em");
	});
}

function wrapBrowseNode(text) {
	text.each(function() {
		var result = wrapImpl(this, this.__data__.w-40, 2);
		if(result.ln == 0 && result.span != null)
			result.span.attr("dy", "0.3em");
	});
}

function wrapWidth(text, width) {
	text.each(function() {
		var result = wrapImpl(this, width, 5);
		if(result.ln == 1 && result.span != null)
			result.span.attr("dy", "0.2em");
	});
}

function wrapMargin(ths, data, margin) {
	var text = d3.select(ths);
	text.each(function() {
		var result = wrapImpl(this, data.width, 5);
		if(result.ln == 1 && result.span != null)
			result.span.attr("dy", "0.2em");
	});
}

function wrapLine(text, width) {
	text.each(function() {
		var result = wrapImpl(this, width, 1);
	});
}

function wrapLineX(text, width, x) {
	text.each(function() {
		var result = wrapImpl(this, width, x);
	});
}


function wrapLineN(text, width) {
	text.each(function() {
		var result = wrapImpl(this, width, 100);
	});
}

var singletonBrowserRootFn;

fi_semantum_strategia_widget_Browser = function() {	

	var self = this;

	singletonBrowserRootFn = this;

	createBrowser(this);

	this.onStateChange = function() {
		refreshBrowser(self);
	};

	this.onStateChange();

};

var force;
var lastTranslate = "0,0";
var lastScale = "1";

var zoom = d3.behavior.zoom()
.scaleExtent([0.1, 10])
.on("zoom", zoomed);

var linkVis;
var nodeVis;

function zoomed() {
	lastScale = d3.event.scale;
	lastTranslate = d3.event.translate;
	linkVis.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
	nodeVis.attr("transform", "translate(" + d3.event.translate + ")scale(" + d3.event.scale + ")");
}

function createBrowser(rootFn) {

	var thisElement = rootFn.getElement();

	thisElement.style.backgroundColor = '#FFF';

	var root = d3.select(thisElement);

	force = d3.layout.force()
	.gravity(0.0)
	.charge(function(d) { 
		return d.charge; 
	})
	.chargeDistance(1600.0)
	.linkDistance(function(d) { 
		return d.linkDistance; 
	})
	.linkStrength(function(d) { 
		return d.linkStrength; 
	});
	
	var svg = root
	.append("svg:svg")
	.attr("id", "browser")
	.attr("class", "browser")
	.attr("width", 20)
	.attr("height", 20)
	.call(zoom);

	linkVis = svg.append("svg:g").classed("links", true);
	nodeVis = svg.append("svg:g").classed("nodes", true);

	linkVis.attr("transform", "translate(" + lastTranslate + ")scale(" + lastScale + ")");
	nodeVis.attr("transform", "translate(" + lastTranslate + ")scale(" + lastScale + ")");

}

var drag = d3.behavior.drag()
.origin(function(d) { return d; })
.on("dragstart", dragstarted)
.on("dragend", dragended);	

function dragstarted(d) {
	d3.event.sourceEvent.stopPropagation();
	force.stop();
}

function dragged(self, data, d) {
	
	d.x = d.px = d3.event.x;
	d.y = d.py = d3.event.y;
	d.fixed = true;
	
	d3.select(self).select("text.fixed").style("display", null);

	nodeMap[d.uuid] = {'x': d.x, 'y': d.y, 'px' : d.px, 'py:' : d.py, 'fixed' : d.fixed };
	d3.select(self).attr("transform", "translate(" + d.x+ "," + d.y + ")");
	tick(data, node, link);
	force.start();
	d3.event.sourceEvent.stopPropagation();
	
}

function dragended(d) {
	force.friction(0.9);
	d3.event.sourceEvent.stopPropagation();
}

function tick(data, node, link) {
	
	node.attr("transform", function(d, i) {
		nodeMap[d.uuid] = {'x': d.x, 'y': d.y, 'px' : d.px, 'py:' : d.py, 'fixed' : d.fixed };
		return "translate(" + d.x + "," + d.y + ")"; 
	});

	link.attr("x1", function(d)   { return d.source.x + 0.5*d.source.w; })
	.attr("y1", function(d)   { return d.source.y + 0.5*d.source.h; })
	.attr("x2", function(d)   { return d.target.x + 0.5*d.target.w; })
	.attr("y2", function(d)   { return d.target.y + 0.5*d.target.h; });

}

var nodeMap = new Object();
var node;
var link;

function doSaveBrowserState(name) {
	
	if (typeof singletonBrowserRootFn === 'undefined') return;
	singletonBrowserRootFn.save(name, nodeMap);
	
}

function refreshBrowser(rootFn) {
	
	var data = rootFn.getState();

	drag.on("drag", function(d) { 
		dragged(this, data, d); 
	});
	
	var thisElement = rootFn.getElement();
	var root = d3.select(thisElement).datum(data);

	var svg = root.select("svg");

	svg.attr("width", data.w);
	svg.attr("height", data.h);
	svg.attr("cursor", "move");

	var links = document.getElementsByClassName('links')[0];
	var nodes = document.getElementsByClassName('nodes')[0];

	if(!data.setPositions) {
		for (var i=0;i<data.nodes.length;i++) {
			var n = data.nodes[i];
			var cache = nodeMap[n.uuid];
			if(cache != null) {
				n.x = cache.x;
				n.y = cache.y;
				n.px = cache.px;
				n.py = cache.py;
				n.fixed = cache.fixed;
			}
		}
	}
	
	if(force != null) force.stop();
	
	force
	.nodes(data.nodes)
	.links(data.links)
	.size([data.w, data.h]);

	link = d3.select(links).selectAll(".browseLink").data(data.links);
	
	link.enter().append("line")
	.attr("class", "browseLink")
	.attr("fill", "none");

	link = d3.select(links).selectAll(".browseLink").data(data.links);

	link
	.attr("cursor", "default")
	.style("stroke", function(d) { 
		return d.color; 
	})
	.style("stroke-dasharray", function(d) { 
		return d.dash;
	})
	.style("stroke-width", function(d) { 
		return 0.25*d.weight;
	});
	
	link.exit().remove();

	node = d3.select(nodes).selectAll("g.node").data(data.nodes);
	
	var enterNode = node.enter().append("g")
	.attr("class", "node")
	.attr("id", function(d) { return d.uuid; })
	.call(drag);
	
	enterNode.append("svg:rect").classed("outline", true);
	enterNode.append("svg:rect").classed("status", true);
	enterNode.append("svg:rect").classed("main", true);
	enterNode.append("svg:text").classed("fixed", true);
	enterNode.append("svg:text").classed("required", true);
	enterNode.append("svg:text").classed("browseName", true);
	enterNode.append("svg:text").classed("description", true);

	uses = d3.select(nodes).selectAll("use").data([1]);
	var enterUses = uses.enter().append("use")
	.attr("xlink:href", "");
	
	svg.on("mouseover", function() {

		var target = d3.event.target;
		if(target.parentNode.parentNode == nodes) {
			
			var d = target.__data__;
			d3.select(target.parentNode).select("rect.outline").style("display", null);
			
			var links = document.getElementsByClassName('browseLink');
			for(var i=0;i<links.length;i++) {
				var link = links[i];
				var linkData = link.__data__;
				if(linkData.source == d || linkData.target == d) {
					link.style.stroke = "#ea2";
					link.style.strokeWidth = 0.75 * linkData.weight;
				}
			}
			
		}
		
	});

	svg.on("mouseout", function() {

		var target = d3.event.target;
		if(target.parentNode.parentNode == nodes) {
			var d = target.__data__;
			var links = document.getElementsByClassName('browseLink');
			for(var i=0;i<links.length;i++) {
				var link = links[i];
				var linkData = link.__data__;
				if(linkData.source == d || linkData.target == d) {
					link.style.stroke = linkData.color;
					link.style.strokeWidth = 0.25 * linkData.weight;

				}
			}
			
			if(d.emph) return;
			d3.select(target.parentNode).select("rect.outline").style("display", "none");
			
		}
		
	});

	node.select("rect.outline")
	.attr("width", function(d) { return  d.w+20; } )
	.attr("height", function(d) { return  d.h+20; } )
	.attr("x", -10)
	.attr("y", -10)
	.attr("rx", 10)
	.attr("ry", 10)
	.style("display", function(d) { return  d.emph ? null : "none"; } )
	.style("stroke",function(d,i) {
		return d.color; 
	})
	.style("stroke-opacity","0.7")
	.style("fill","none")
	.style("stroke-width", 20);

	node.select("rect.main")
	.attr("width", function(d) { return  d.w; } )
	.attr("height", function(d) { return  d.h; } )
	.attr("rx", 10)
	.attr("ry", 10)
	.style("stroke",function(d,i) { 
		return d.color; 
	})
	.style("stroke-width", 6)
	.attr("fill", "none")
	.attr("cursor", "move")
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		rootFn.select(d3.event.pageX, d3.event.pageY, d.uuid); 
	});

	node.select("rect.status")
	.attr("width", function(d) { return  d.w-16; } )
	.attr("height", function(d) { return  d.h-16; } )
	.attr("x", 8)
	.attr("y", 8)
	.attr("rx", 3)
	.attr("ry", 3)
	.attr("cursor", "move")
	.style("stroke", function(d) { return  d.circleFill; })
	.style("stroke-width", 10)
	.attr("fill", "#FFF" )
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		rootFn.select(d3.event.pageX, d3.event.pageY, d.uuid); 
	});

	node.select("text.fixed")
	.attr("x", function(d) { return  d.w-45; } )
	.attr("y", function(d) { return  d.h-15; } )
	.attr("cursor", "pointer")
	.text("\uf023")
	.style("display", function(d) { return d.fixed ? null : "none"; })
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		d.fixed = false;
		d3.select(this).style("display", "none");
		force.start();
	});

	node.select("text.required")
	.attr("x", function(d) { return  15; } )
	.attr("y", function(d) { return  d.h-15; } )
	.attr("cursor", "pointer")
	.style("font-size", "35px")
	.text("\uf046")
	.style("display", function(d) { return d.required ? null : "none"; })
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		d.fixed = false;
		d3.select(this).style("display", "none");
		force.start();
	});

	node.select("text.browseName")
	.text(function(d, i) { return d.name; })
	.attr("transform", function(d) { return "translate(" + (d.w/2) + "," + (d.nameSize + 0.05*d.h) + ")"; })
	.attr("cursor", "pointer")
	.style("text-decoration", "underline")
	.attr("fill", "#111" )
	.attr("font-size", function(d) { return d.nameSize + "px"; })
	.attr("text-anchor", "middle" )
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		rootFn.select(d3.event.pageX, d3.event.pageY, d.uuid); 
	});
	//.call(wrapBrowseNode, 200);
	
	node.select("text.description")
	.text(function(d, i) { return d.description; })
	.attr("transform", function(d) { return "translate(" + (d.w/2) + "," + (d.nameSize + 0.05*d.h + d.descriptionSize + 0.05*d.h) + ")"; })
//	.attr("x", function(d) { return  d.w/2; } )
//	.attr("y", function(d) { return (d.nameSize + 10 + d.descriptionSize + 10); })
	.attr("cursor", "pointer")
	.attr("fill", "#111" )
	.attr("font-size", function(d) { return d.descriptionSize + "px"; })
	.attr("text-anchor", "middle" )
	.on("click", function(d) {
		if (d3.event.defaultPrevented) return; // click suppressed
		rootFn.select(d3.event.pageX, d3.event.pageY, d.uuid); 
	})
	.call(wrapBrowseNode);

	node.exit().remove();

	force.on("tick", function(e) {
		tick(data, node, link);
	});

	force.start();

};



