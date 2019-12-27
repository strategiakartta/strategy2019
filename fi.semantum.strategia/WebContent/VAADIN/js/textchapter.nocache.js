fi_semantum_strategia_widget_TextChapterCommentsJS = function() {	

	var self = this;

	createTextChapterCommentsJS(self);
	
	this.onStateChange = function() {
		refreshTextChapterCommentsJS(self);
	};

	this.onStateChange();

};

function createTextChapterCommentsJS(rootFn){
	var thisElement = rootFn.getElement();
	var state = rootFn.getState();
	var clickableInnerHTMLs = state.clickableInnerHTMLs;

	var clickableChapterElements = document.getElementsByClassName("htmlDocumentSubElement");
	for (var i = 0; i < clickableChapterElements.length; i++) {
		let elem = clickableChapterElements[i];
		if(clickableInnerHTMLs.includes(elem.innerHTML)){
			let wrapper = document.createElement('div');
			elem.parentNode.insertBefore(wrapper, elem);
			wrapper.appendChild(elem);
			var b = document.createElement('button');
			wrapper.appendChild(b);
			
			let wrapperRect = wrapper.getBoundingClientRect();
			b.style.position = "relative";
			let bLeft = (parseInt(wrapperRect.width + 28.0)).toString(10);
			let bTop = (parseInt(wrapperRect.height / -2.0)).toString(10);
			
			b.style.left = bLeft.concat("px");
			b.style.top = bTop.concat("px");
			b.className = "v-button tiny v-button-tiny";
			b.innerHTML = "\uf086"; //speech bubble comment icon
			b.style.fontFamily = "FontAwesome";
			b.style.fontSize = "14px";
			
			const targetIndex = i;
			b.addEventListener("click", function() {
				rootFn.clickEvent(elem.innerHTML, targetIndex); 
		    }, false);
			
			b.style.cursor = "pointer";
			b.style.backgroundColor = "#efefef";
			b.addEventListener("mouseover", function() {
				this.style.backgroundColor = "#bfbfbf";
				wrapper.style.backgroundColor = "#dfdfdf";
		    }, false);
			b.addEventListener("mouseout", function() {
				this.style.backgroundColor = "#efefef";
				wrapper.style.backgroundColor = "";
		    }, false);
			
		}
	}
}

function refreshTextChapterCommentsJS(rootFn) {
	//Optional refresh logic here
}

