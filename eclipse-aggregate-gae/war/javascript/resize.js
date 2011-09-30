var MAX_SIDE_PANEL_WIDTH = 350;

// Handle resizing various parts of the page based on
// the screen size.
function onWindowResize() {
	if (contentLoaded == false)
		return;

	// First set all heights to ensure there is not a vertical scrollbar when we calculate width.
	setHeights();
	setWidths();
	setHeights();	
}

function setHeights() {
	var height = $(window).height();
	
	var helpPanel = $("#help_panel");
	var helpPanelHeight = helpPanel.length ? helpPanel.height() : 0;
	if (helpPanel.length) {
		var helpTreeHeight = $("#help_tree").height();
		helpPanelHeight = Math.min(helpTreeHeight, height / 2);
	}
	var layoutHeight = height - $("#layout_panel").offset().top - helpPanelHeight - 1;
    var tab1Height = $(".tab_measure_1").first().height();
    var tab2Height = $(".tab_measure_2").first().height();
    var contentHeight = layoutHeight - tab1Height - tab2Height;
	var filterContentHeight = contentHeight - $("#submission_nav_table").height();
	var filterPaginationHeight = $("#filter_submission_pagination").height();
	
	// All tabs
	$("html").height(height);
	$("body").height(height);
	$("#dynamic_content").height(height);
	$("#mainNav").height(layoutHeight);
	$("#layout_panel").height(layoutHeight);
	if (helpPanel.length) {
	    helpPanel.height(helpPanelHeight);
    }
	$(".second_level_menu").height(layoutHeight - tab1Height);
	$(".tab_content").height(contentHeight);
	
	// Submissions tab
	$("#filters_container").height(filterContentHeight - /* border width */ 1);
	$("#submission_container").height(filterContentHeight - filterPaginationHeight - /* border width */ 1);
}

function setWidths() {
	var width = $(window).width();
	var sidePanelPotentialWidth = Math.floor(width * .2);
	var sidePanelActualWidth = Math.min(MAX_SIDE_PANEL_WIDTH, sidePanelPotentialWidth);
	
	// Form Management tab
	$("#form_management_table").width(width);
	
	// All tabs
	$("#mainNav").width(width);
	$(".error_message").width(width);
	$("#nav_bar_help_login").offset({left: width - $("#nav_bar_help_login").width() - 10});
	
	// Submissions tab
	var submissionWidth = width - sidePanelActualWidth;
	$("#filters_container").width(sidePanelActualWidth - /* border width */ 1);
	$("#submission_container").width(submissionWidth);
	var submissionTable = $("#submission_table");
	ensureWidth(submissionTable, submissionWidth);
}

function ensureWidth(table, width) {
	if (table.length) {
		// Get the cells in the first row of the table
		// table > (colgroup, tbody) > colgroup > tbody > [tr, tr...tr] > tr > [td, td...td]
		cells = table.children().first().next().children().first().children();
		var totalCellWidth = 0;
		cells.each(function(index, element) {
			totalCellWidth += $(element).width();
		});
		totalCellWidth -= cells.last().width();
		if (totalCellWidth < width) {
			cells.last().width(width - totalCellWidth);
		}
	}
}
