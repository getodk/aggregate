var MAX_SIDE_PANEL_WIDTH = 350;

// Handle resizing various parts of the page based on
// the screen size.
function onWindowResize() {
	if (contentLoaded == false)
		return;
	var width = $(window).width();
	var height = $(window).height();
	
	var sidePanelPotentialWidth = width * .2;
	var sidePanelActualWidth = Math.min(MAX_SIDE_PANEL_WIDTH, sidePanelPotentialWidth);
	
	$(".mainNav").width(width);
	$("#filters_container").width(sidePanelActualWidth);
	$("#submission_container").width(width - sidePanelActualWidth);
	$("#form_management_table").width(width);

	$("html").height(height);
	$("body").height(height);
	$("#dynamic_content").height(height);
	$("#layout_panel").height(height - $("#layout_panel").offset().top - $("#help_panel").height());
	$("#filters_container").height($("#layout_panel").height() - $("#filter_sub_tab").height());
	$("#submission_container").height($("#filters_container").height());
	$("#nav_bar_help_login").offset({left: width - $("#nav_bar_help_login").width() - 32});
	$(".error_message").width(width);
}
