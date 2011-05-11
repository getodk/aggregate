var MAX_SIDE_PANEL_WIDTH = 350;

// Handle resizing various parts of the page based on
// the screen size.
function onWindowResize() {
	var width = $(window).width();
	var height = $(window).height();
	
	var sidePanelPotentialWidth = width * .2;
	var sidePanelActualWidth = Math.min(MAX_SIDE_PANEL_WIDTH, sidePanelPotentialWidth);
	
	$(".mainNav").width(width/* - sidePanelActualWidth*/);
	//$("#help_container").width(sidePanelActualWidth);
	$("#filters_container").width(sidePanelActualWidth);
	//$("#submission_container").width(width - (2 * sidePanelActualWidth));
	$("#submission_container").width(width - sidePanelActualWidth);
	$("#form_management_table").width(width/* - sidePanelActualWidth*/);

	$("html").height(height);
	$("body").height(height);
	$("#dynamic_content").height(height);
	$("#layout_panel").height(height - $("#layout_panel").offset().top);
	$("#filters_container").height(height - $("#filters_container").offset().top);
	$("#submission_container").height(height - $("#submission_container").offset().top);
	//$("#help_container").height(height - $("#help_container").offset().top);
}
