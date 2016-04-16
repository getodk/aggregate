var contentLoaded = false;

// Runs after gwt is finished putting elements on the page
function gwtContentLoaded() {
	// Set up window resize handler and call it.
	contentLoaded = true;
}


//Runs before gwt is finished putting elements on the page
function gwtBeforeContentLoaded() {
	// Set up window resize handler
	$(window).resize(onWindowResize);
}
