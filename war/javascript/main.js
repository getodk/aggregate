// Runs after gwt is finished putting elements on the page
function gwtContentLoaded() {
	// Set up window resize handler and call it.
	$(window).resize(onWindowResize);
	onWindowResize();
}
