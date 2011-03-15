// Runs after gwt is finished putting elements on the page
function gwtContentLoaded() {
	// Set up window resize handler and call it.
	$("#help_container").offset({left: $("#help_container").offset().left, top: $("#second_level_menu").offset().top});
	$(window).resize(onWindowResize);
	onWindowResize();
}
