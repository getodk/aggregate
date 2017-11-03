var MAX_SIDE_PANEL_WIDTH = 350;
var resizeInProgress = false;

// Handle resizing various parts of the page based on
// the screen size.
function onWindowResize() {
	onAggregateResize();
}

function onAggregateResize() {
	if (contentLoaded == false)
		return;

	if ( !resizeInProgress ) {
		resizeInProgress = true;
		setTimeout(function() {
		  // First set all heights to ensure there is not a vertical scrollbar when we calculate width.
		  try {
			  setHeights();
			  setWidths();
			  setHeights();
		  } catch(e) {
			console.log(e);
		  }
		  setTimeout(function() {
			resizeInProgress = false;
		  }, 100);
		}, 0);
	}
}

function setHeights() {
	var height = $(window).height();

	var helpPanelHeight = 0;
	if ( $("#help_panel").filter(':visible').get(0) !== undefined ) {
		helpPanelHeight = $("#help_tree").height();
	}

    var imagePosition = $("#odk_aggregate_logo").position();
    var imageHeight = $("#odk_aggregate_logo").outerHeight(true);

    var navBarHelpLoginHeight = $("#nav_bar_help_login").height();
    
    var maxExtrasHeight = imageHeight;
    maxExtrasHeight = (navBarHelpLoginHeight < maxExtrasHeight) ? maxExtrasHeight : navBarHelpLoginHeight;
    
    var imageOffset = (maxExtrasHeight - imageHeight) / 2;
    var navBarOffset = (maxExtrasHeight - navBarHelpLoginHeight) / 2;
    
    var tab1Height = $(".tab_measure_1").first().height();

    // compute and set the offset for the top of the layout_panel
    console.log("helpPanelHeight " + helpPanelHeight + " imageHeight " + imageHeight + " tab1Height " + tab1Height);
    var layoutPanelTop = maxExtrasHeight - tab1Height;
    if ( layoutPanelTop < 0 ) layoutPanelTop = 0;
    // push down to leave room for error messages
    var allAlertMessagesHeight = 0;
    var errorMessageOffset = 0;
    if ( ($("#not_secure_content").filter(':visible').get(0) !== undefined) ||
         ($("#error_content").filter(':visible').get(0) !== undefined) ) {
    	errorMessageOffset = $("#not_secure_content").height();
    	allAlertMessagesHeight = $("#not_secure_content").height() + $("#error_content").height();
    }
    layoutPanelTop = layoutPanelTop + allAlertMessagesHeight;
    console.log("layoutPanelTop " + layoutPanelTop);
	
	if ( $("#layout_panel").get(0) !== undefined && $("#layout_panel").offset().top !== layoutPanelTop ) {
		$("#layout_panel").offset({top: layoutPanelTop });
	}
	if ( $("#odk_aggregate_logo").offset().top !== allAlertMessagesHeight + imageOffset ) {
		$("#odk_aggregate_logo").offset({top: allAlertMessagesHeight + imageOffset });
	}
	if ( $("#nav_bar_help_login").offset().top !== allAlertMessagesHeight + navBarOffset ) {
		$("#nav_bar_help_login").offset({top: allAlertMessagesHeight + navBarOffset });
	}
    if ( $("#error_content").filter(':visible').get(0) !== undefined ) {
		if ( $("#error_content").offset().top !== errorMessageOffset ) {
			$("#error_content").offset({top: errorMessageOffset});
		}
    }

    if ( $("#help_panel").filter(':visible').get(0) !== undefined ) {
		if ( $("#help_panel").height() !== helpPanelHeight ) {
			$("#help_panel").height(helpPanelHeight);
		}
    }

    var tab2Height = $(".tab_measure_2").first().height();
    // now get remaining height and resize everything
    var minLayoutHeight = $("#layout_panel").height;
    var maxLayoutHeight = height - $("#layout_panel").offset().top - helpPanelHeight - 1;
    var layoutHeight = (minLayoutHeight > maxLayoutHeight) ? minLayoutHeight : maxLayoutHeight;
    var contentHeight = layoutHeight - tab1Height - tab2Height;
	var filterContentHeight = contentHeight - $("#submission_nav_table").height();
	var filterPaginationHeight = $("#filter_submission_pagination").height();

	// All tabs
	if ( $("html").height() !== height ) {
		$("html").height(height);
	}
	if ( $("body").height() !== height ) {
		$("body").height(height);
	}
	if ( $("#dynamic_content").height() !== height-allAlertMessagesHeight ) {
		$("#dynamic_content").height(height-allAlertMessagesHeight);
	}
	if ( $("#mainNav").height() !== layoutHeight ) {
		$("#mainNav").height(layoutHeight);
	}
	if ( $("#layout_panel") !== undefined && $("#layout_panel").height() !== layoutHeight ) {
		$("#layout_panel").height(layoutHeight);
	}
	if ( $(".second_level_menu").height() !== layoutHeight - tab1Height ) {
		$(".second_level_menu").height(layoutHeight - tab1Height);
	}
	if ( $(".tab_content").height() !== contentHeight ) {
		$(".tab_content").height(contentHeight);
	}

    if ( $("#help_panel").filter(':visible').get(0) !== undefined ) {
		if ( $("#help_panel").offset().top !== $("#layout_panel").offset().bottom +  /* border width */ 1 ) {
			$("#help_panel").offset({top: ($("#layout_panel").offset().bottom +  /* border width */ 1) });
		}
    }
    
	// Submissions tab
	if ( $("#filters_container").height() !== filterContentHeight - /* border width */ 1 ) {
		$("#filters_container").height(filterContentHeight - /* border width */ 1);
	}
	if ( $("#submission_container").height() !== filterContentHeight - filterPaginationHeight - /* border width */ 1 ) {
		$("#submission_container").height(filterContentHeight - filterPaginationHeight - /* border width */ 1);
	}
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
	$("#submission_admin_bar").width(width);
	$("#submission_admin_list").width(width);

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
