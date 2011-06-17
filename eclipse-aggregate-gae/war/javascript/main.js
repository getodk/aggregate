var contentLoaded = false;

// Runs after gwt is finished putting elements on the page
function gwtContentLoaded() {
	// Set up window resize handler and call it.
	contentLoaded = true;
	$(window).resize(onWindowResize);
	onWindowResize();
}
