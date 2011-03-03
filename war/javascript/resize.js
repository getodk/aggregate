var MAX_SIDE_PANEL_WIDTH = 200;

function onWindowResize() {
	var height = $(window).height();
	var width = $(window).width();
	$(".mainNav").width(width - 6);
	$(".mainNav").height(height - 26);
	$("#spacer_tab").width(width - 300);
	
	var sidePanelPotentialWidth = width * .15;
	var sidePanelActualWidth = Math.min(MAX_SIDE_PANEL_WIDTH, sidePanelPotentialWidth);
	$(".filters_panel").width(sidePanelActualWidth);
	$(".help_panel").width(sidePanelActualWidth);
	$("#data_table").width(width - 22 - (sidePanelActualWidth * 2));
}

function gwtContentLoaded() {
	$(window).resize(onWindowResize);
	onWindowResize();
}
