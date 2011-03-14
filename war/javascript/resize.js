var MAX_SIDE_PANEL_WIDTH = 200;

// Handle resizing various parts of the page based on
// the screen size.
function onWindowResize() {
	var width = $(window).width();
	var sidePanelPotentialWidth = width * .15;
	var sidePanelActualWidth = Math.min(MAX_SIDE_PANEL_WIDTH, sidePanelPotentialWidth);
	$(".mainNav").width(width - sidePanelActualWidth);

	$("#help_panel").width(sidePanelActualWidth);
	$("#filters_panel").width(sidePanelActualWidth);
	$("#submission_container").width(width - (2 * sidePanelActualWidth));
	$("#form_management_table").width(width - sidePanelActualWidth);
}
