var MAX_SIDE_PANEL_WIDTH = 200;

// Handle resizing various parts of the page based on
// the screen size.
function onWindowResize() {
	var width = $(window).width();
	var height = $(window).height();
	
	var sidePanelPotentialWidth = width * .2;
	var sidePanelActualWidth = Math.min(MAX_SIDE_PANEL_WIDTH, sidePanelPotentialWidth);
	
	$(".mainNav").width(width - sidePanelActualWidth);
	$("#help_container").width(sidePanelActualWidth);
	$("#filters_container").width(sidePanelActualWidth);
	$("#submission_container").width(width - (2 * sidePanelActualWidth));
	$("#form_management_table").width(width - sidePanelActualWidth);
	
	$("#dynamic_content").height(height - 24);
	$("#layout_panel").height(height - $("#layout_panel").offset().top - 24);
	$("#filters_container").height(height - $("#filters_container").offset().top);
	$("#submission_container").height(height - $("#submission_container").offset().top);
	$("#help_container").height(height - $("#help_container").offset().top);
}
