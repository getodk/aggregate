var MAX_SIDE_PANEL_WIDTH = 200;

// Handle resizing various parts of the page based on
// the screen size.
function onWindowResize() {
	var width = $(window).width();
	$(".mainNav").width(width);
	
	var sidePanelPotentialWidth = width * .15;
	var sidePanelActualWidth = Math.min(MAX_SIDE_PANEL_WIDTH, sidePanelPotentialWidth);
	$("#filters_panel").width(sidePanelActualWidth);
	$("#help_panel").width(sidePanelActualWidth);
	$("#filters_data_help").width(width);
}
