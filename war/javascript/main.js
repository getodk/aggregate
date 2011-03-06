function flipTabs() {
	var tabs = $(".javascript_tab_flip").each(function(index, element) {
		alert(element);
	});
}

function gwtContentLoaded() {
	flipTabs();
	$(window).resize(onWindowResize);
	onWindowResize();
}
