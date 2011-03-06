// Flips the second level tabs so they "hang" under
// the border instead of sitting on top of it.
function flipTabs() {
	$(".javascript_tab_flip").each(function(index) {
		var row = $(this).children().children();
		$(row).addClass("inverted");
		var decorators = $(row).children().first();
		$(decorators).remove();
		$(decorators).insertAfter($(row).children().first());
	});
}

// Runs after gwt is finished putting elements on the page
function gwtContentLoaded() {
	flipTabs();
	// Set up window resize handler and call it.
	$(window).resize(onWindowResize);
	onWindowResize();
}
