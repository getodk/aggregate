
function submitButton(self, targetUrl, xmlName, filesName) {

	/* get the file information for the submission or form definition */
	var xmlList = self.getElementById(xmlName).files;
	if ( xmlList == null ) {
		// must be IE...
		self.getElementById('ie_backward_compatible_form').submit();
		return;
	}
	
	/* display the progress bar region if there is a <div id="progress></div> on page */
	showProgressRegion();

	var xml = xmlList[0];	
    
    if ( xml == null ) {
    	setFinalStatus(-1, 1, "no file specified");
    	return;
    }
    
    /* filelist will hold the file information for all of the media files */
	var filelist = new Array();
	
	/* IE compatibility -- we need to copy the files property, which is immutable,
	 * into the mutable filelist array, then add values from the extra fields displayed
	 * in IE into that filelist.
	 */
	var primary = self.getElementById(filesName).files;
	if ( primary != null && primary.length != 0 ) {
		var curlen = filelist.length;
		for ( var j = 0 ; j < primary.length ; ++j, ++curlen ) {
			filelist[curlen] = primary[j];
		}
	}
	/* IE compatibility -- assumes there are additional file input fields with
	 * the numbers 2..16 appended to the primary field name (above).  Iterate
	 * through these, stopping after the first in the sequence that is not found.
	 * If we do find one, see if it has any file specified, and if it does, copy
	 * the file into the filelist. */
	for ( var i = 2 ; i < 17 ; ++i ) {
		var extras = self.getElementById(filesName + i);
		if ( extras == null ) break;
		var extraslist = extras.files;
		if ( extraslist != null && extraslist.length > 0 ) {
			var curlen = filelist.length;
			for ( var j = 0 ; j < extraslist.length ; ++j, ++curlen ) {
				filelist[curlen] = extraslist[j];
			}
		}
	}

	var startMedia = 0;
	var endMedia = computeEndMedia( xml, filelist, startMedia );

	var action = new PostSpan( targetUrl, this.headers, xmlName, xml, filesName, filelist, 1, startMedia, endMedia);

	action.submitSpan();
}

/**
 * Insert the progress bar into the page.  Assumes a <div id="progress"></div> in the page.
 */
function showProgressRegion() {
	var progress = document.getElementById('progress');
	if ( progress.childNodes.length == 0 ) {
		progress.innerHTML = 
			'<div id="progress_fatal_response" ></div><br/>\n' +
			'<table style="width: 100%;" >\n' +
			'<tr><td colspan="5" align="left">Upload Progress Meter:</td></tr>\n' +
			'<tr><td colspan="5" align="left">\n' +
			'<div id="progress_bar_container" style="width: 100%; height: 16px; border: 2px solid black;">\n' +
			'<div id="progress_bar" style="margin-top: 2px; left: 0%; background-color: blue; width: 0%; height: 75%;"></div>\n' +
			'</div>\n' +
			'</td></tr>\n' +
			'<tr><td align="left" style="margin: 0px 0px 0px 0px; font-family: Tahoma;">0%&nbsp;&nbsp;</td>\n' +
			'<td align="left" style="margin: 0px 0px 0px 0px; font-family: Tahoma;">25%</td>\n' +
			'<td align="center" style="margin: 0px 0px 0px 0px; font-family: Tahoma;">50%</td>\n' +
			'<td align="right" style="margin: 0px 0px 0px 0px; font-family: Tahoma;">75%</td>\n' +
			'<td align="right" style="margin: 0px 0px 0px 0px; font-family: Tahoma;">100%</td></tr>\n' +
			'</table>\n' +
			'<div id="progress_names"><br /></div>\n' +
			'<div id="progress_status" style="font-style: italic;"><br /></div>\n';
	}
}
/**
 * Determine the number of attachments that can fit in a submission such that it is less than 10Mb
 * 
 * @param xml the xml file being uploaded.
 * @param filelist the media filelist.
 * @param startMedia the index into filelist of the first attachment to include on this submission. 
 * @return index after the last attachment to add to the post that begins with startMedia.
 */
function computeEndMedia( xml, filelist, startMedia ) {
    var endMedia = startMedia;
    var len = xml.fileSize;
    var first = true;
    while ( endMedia < filelist.length ) {
        var file = filelist[endMedia];
        len += file.fileSize;
        if ( len > 10000000 && !first ) break;
        first = false;
        ++endMedia;
    }
    return endMedia;
}

/**
 * Constructor for the PostSpan object.
 */
function PostSpan( targetUrl, headerCollection, xmlName, xml, filesName, filelist, postCount, startMedia, endMedia ) {
	this.postURL = targetUrl;
	this.headers = headerCollection;
	this.xmlPropertyName = xmlName;
	this.xmlFile = xml;
	this.attachmentsPropertyName = filesName;
	this.attachmentsList = filelist;
	this.postNumber = postCount;
	this.phase = -1;
    this.startAttachmentIdx = startMedia;
	this.endAttachmentIdx = endMedia;
    this.xhr = null;
}

/**
 * Mime types excluding application/... and .../vnd.... types.
 * From Apache's mime types https://svn.apache.org/repos/asf/httpd/httpd/trunk/docs/conf/mime.types
 * 
 * and adding:
 * '3gpp': 'audio/3gpp'
 * 'jar': 'application/java-archive'
 * 'js': 'application/javascript'
 * 'xhtml': 'application/xhtml+xml'
 * 'xml': 'application/xml'
 * 'xsl': 'application/xml'
 * 'xslt': 'application/xslt+xml'
 * 'zip': 'application/zip'
 */
PostSpan.mimeTypes = {
		'3g2': 'video/3gpp2',
		'3gp': 'video/3gpp',
		'3gpp': 'audio/3gpp',
		'aac': 'audio/x-aac',
		'adp': 'audio/adpcm',
		'aif': 'audio/x-aiff',
		'aifc': 'audio/x-aiff',
		'aiff': 'audio/x-aiff',
		'asf': 'video/x-ms-asf',
		'asm': 'text/x-asm',
		'asx': 'video/x-ms-asf',
		'au': 'audio/basic',
		'avi': 'video/x-msvideo',
		'bmp': 'image/bmp',
		'btif': 'image/prs.btif',
		'c': 'text/x-c',
		'cc': 'text/x-c',
		'cdx': 'chemical/x-cdx',
		'cgm': 'image/cgm',
		'cif': 'chemical/x-cif',
		'cmdf': 'chemical/x-cmdf',
		'cml': 'chemical/x-cml',
		'cmx': 'image/x-cmx',
		'conf': 'text/plain',
		'cpp': 'text/x-c',
		'csml': 'chemical/x-csml',
		'css': 'text/css',
		'csv': 'text/csv',
		'cxx': 'text/x-c',
		'def': 'text/plain',
		'dic': 'text/x-c',
		'dsc': 'text/prs.lines.tag',
		'eml': 'message/rfc822',
		'etx': 'text/x-setext',
		'f': 'text/x-fortran',
		'f4v': 'video/x-f4v',
		'f77': 'text/x-fortran',
		'f90': 'text/x-fortran',
		'fh': 'image/x-freehand',
		'fh4': 'image/x-freehand',
		'fh5': 'image/x-freehand',
		'fh7': 'image/x-freehand',
		'fhc': 'image/x-freehand',
		'fli': 'video/x-fli',
		'flv': 'video/x-flv',
		'for': 'text/x-fortran',
		'g3': 'image/g3fax',
		'gif': 'image/gif',
		'h': 'text/x-c',
		'h261': 'video/h261',
		'h263': 'video/h263',
		'h264': 'video/h264',
		'hh': 'text/x-c',
		'htm': 'text/html',
		'html': 'text/html',
		'ice': 'x-conference/x-cooltalk',
		'ico': 'image/x-icon',
		'ics': 'text/calendar',
		'ief': 'image/ief',
		'ifb': 'text/calendar',
		'iges': 'model/iges',
		'igs': 'model/iges',
		'in': 'text/plain',
		'jar': 'application/java-archive',
		'java': 'text/x-java-source',
		'jpe': 'image/jpeg',
		'jpeg': 'image/jpeg',
		'jpg': 'image/jpeg',
		'jpgm': 'video/jpm',
		'jpgv': 'video/jpeg',
		'jpm': 'video/jpm',
		'js': 'application/javascript',
		'kar': 'audio/midi',
		'list': 'text/plain',
		'log': 'text/plain',
		'm1v': 'video/mpeg',
		'm2a': 'audio/mpeg',
		'm2v': 'video/mpeg',
		'm3a': 'audio/mpeg',
		'm3u': 'audio/x-mpegurl',
		'm4v': 'video/x-m4v',
		'man': 'text/troff',
		'me': 'text/troff',
		'mesh': 'model/mesh',
		'mid': 'audio/midi',
		'midi': 'audio/midi',
		'mime': 'message/rfc822',
		'mj2': 'video/mj2',
		'mjp2': 'video/mj2',
		'mov': 'video/quicktime',
		'movie': 'video/x-sgi-movie',
		'mp2': 'audio/mpeg',
		'mp2a': 'audio/mpeg',
		'mp3': 'audio/mpeg',
		'mp4': 'video/mp4',
		'mp4a': 'audio/mp4',
		'mp4v': 'video/mp4',
		'mpe': 'video/mpeg',
		'mpeg': 'video/mpeg',
		'mpg': 'video/mpeg',
		'mpg4': 'video/mp4',
		'mpga': 'audio/mpeg',
		'ms': 'text/troff',
		'msh': 'model/mesh',
		'oga': 'audio/ogg',
		'ogg': 'audio/ogg',
		'ogv': 'video/ogg',
		'p': 'text/x-pascal',
		'pas': 'text/x-pascal',
		'pbm': 'image/x-portable-bitmap',
		'pct': 'image/x-pict',
		'pcx': 'image/x-pcx',
		'pgm': 'image/x-portable-graymap',
		'pic': 'image/x-pict',
		'png': 'image/png',
		'pnm': 'image/x-portable-anymap',
		'ppm': 'image/x-portable-pixmap',
		'qt': 'video/quicktime',
		'ra': 'audio/x-pn-realaudio',
		'ram': 'audio/x-pn-realaudio',
		'ras': 'image/x-cmu-raster',
		'rgb': 'image/x-rgb',
		'rmi': 'audio/midi',
		'rmp': 'audio/x-pn-realaudio-plugin',
		'roff': 'text/troff',
		'rtx': 'text/richtext',
		's': 'text/x-asm',
		'sgm': 'text/sgml',
		'sgml': 'text/sgml',
		'silo': 'model/mesh',
		'snd': 'audio/basic',
		'spx': 'audio/ogg',
		'svg': 'image/svg+xml',
		'svgz': 'image/svg+xml',
		't': 'text/troff',
		'text': 'text/plain',
		'tif': 'image/tiff',
		'tiff': 'image/tiff',
		'tr': 'text/troff',
		'tsv': 'text/tab-separated-values',
		'txt': 'text/plain',
		'uri': 'text/uri-list',
		'uris': 'text/uri-list',
		'urls': 'text/uri-list',
		'uu': 'text/x-uuencode',
		'vcf': 'text/x-vcard',
		'vcs': 'text/x-vcalendar',
		'vrml': 'model/vrml',
		'wav': 'audio/x-wav',
		'wax': 'audio/x-ms-wax',
		'wm': 'video/x-ms-wm',
		'wma': 'audio/x-ms-wma',
		'wmv': 'video/x-ms-wmv',
		'wmx': 'video/x-ms-wmx',
		'wrl': 'model/vrml',
		'wvx': 'video/x-ms-wvx',
		'xbm': 'image/x-xbitmap',
		'xhtml': 'application/xhtml+xml',
		'xml': 'application/xml',
		'xpm': 'image/x-xpixmap',
		'xsl': 'application/xml',
		'xslt': 'application/xslt+xml',
		'xwd': 'image/x-xwindowdump',
		'xyz': 'chemical/x-xyz',
		'zip': 'application/zip'
}

PostSpan.prototype.getMimeType = function(filename) {
    var ext = filename.substring(1+filename.lastIndexOf('.'));
    var mimeType = PostSpan.mimeTypes[ext];
    if ( mimeType == undefined || mimeType == null ) {
    	mimeType = "application/octet-stream";
    }
    return mimeType;
}

/**
 * Event handler method that updates status fields on the web page and initiates follow-on POST requests.
 */
PostSpan.prototype.onreadystatechange = function() {
	if ( this.xhr == null ) return;
    if ( this.xhr.readyState == 0 ) {
        setStatus("POST(" + this.postNumber + ") initializing");
        this.phase = this.xhr.readyState;
    } else if ( this.xhr.readyState == 1 ) {
        setStatus("POST(" + this.postNumber + ") connection established");
        this.phase = this.xhr.readyState;
    } else if ( this.xhr.readyState == 2 ) {
        setStatus("POST(" + this.postNumber + ") request sent");
        this.phase = this.xhr.readyState;
    } else if ( this.xhr.readyState == 3 ) {
        setStatus("POST(" + this.postNumber + ") awaiting response");
        this.phase = this.xhr.readyState;
    } else if ( this.xhr.readyState == 4 ) {
        if ( this.xhr.status == 201 ) {
            setStatus("POST(" + this.postNumber + ") successful");
            this.phase = this.xhr.readyState;
            /* continue with next batch of files... */
            this.postNumber = this.postNumber + 1;
            this.startAttachmentIdx = this.endAttachmentIdx;
            this.endAttachmentIdx = computeEndMedia(this.xmlFile, this.attachmentsList, this.startAttachmentIdx);
            this.xhr = null;
            if ( this.startAttachmentIdx != this.endAttachmentIdx ) {
                this.submitSpan();
            } else {
                setFinalStatus(this.phase);
            }
        } else {
            setStatus("POST(" + this.postNumber + ") failed");
            setFinalStatus(this.phase, this.xhr.status, this.xhr.responseText);
            this.xhr = null;
        }
    }
}

/**
 * submission method that constructs one POST request for Firefox 3.x.
 */
PostSpan.prototype.constructFirefox3Span = function(xhrValue) {
    var boundary = '------multipartformboundary' + (new Date()).getTime();
    var dashdash = '--';
    var crlf = '\r\n';

	var curMedia = this.startAttachmentIdx;
	
	/* the build string */
	var builder = '';
	
    builder += crlf;
	builder += dashdash;
	builder += boundary;

    /** emit the xml file */
   
    /* Generate headers */
    builder += crlf;
    builder += 'Content-Disposition: form-data; name="' + this.xmlPropertyName + '"';
    if ( this.xmlFile.fileName ) {
 	    builder +='; filename="' + this.xmlFile.fileName + '"';
    }
    builder += crlf;
    builder += 'Content-Type: ' + this.getMimeType(this.xmlFile.fileName);
    builder += crlf;

    /* Append binary data */
	builder += crlf;
	builder += this.xmlFile.getAsBinary();
	
	/* write boundary */
	builder += crlf;
	builder += dashdash;
	builder += boundary;

    var first = true;
    var firstFilename = null;
    var lastFilename = null;
    while ( curMedia < this.endAttachmentIdx && curMedia < this.attachmentsList.length ) {
	   
        var file = this.attachmentsList[curMedia];

	    if ( first ) {
		    firstFilename = file.fileName;
	    } else {
		    lastFilename = file.fileName;
	    }
	    first = false;

 	    /* finish prior boundary */
        builder += crlf;
       
	    /* Generate headers */
	    builder += 'Content-Disposition: form-data; name="' + this.attachmentsPropertyName + '"';
	    if ( file.fileName ) {
		    builder +='; filename="' + file.fileName + '"';
	    }
	    builder += crlf;
	    builder += 'Content-Type: ' + this.getMimeType(file.fileName);
	    builder += crlf;
	   
	    /* Append binary data */
	    builder += crlf;
	    builder += file.getAsBinary();

	    /* write boundary */
        builder += crlf;
	    builder += dashdash;
	    builder += boundary;
  
	    ++curMedia;
    }

    if ( curMedia < this.attachmentsList.length ) {
        /* finish prior boundary */
        builder += crlf;
        /* Generate headers */
        builder += 'Content-Disposition: form-data; name="*isIncomplete*"';
        builder += crlf;
        /* indicate that this is partial data */
        builder += crlf;
        builder += 'yes';

        /* write boundary */
        builder += crlf;
        builder += dashdash;
        builder += boundary;
    }
    
    /* Mark end of the request. */
    builder += dashdash;
    builder += crlf;

    xhrValue.setRequestHeader('Content-Type', 'multipart/form-data; boundary=' + boundary);

    setNames( "POST(" + this.postNumber + ") beginning", 
            firstFilename, lastFilename, (this.attachmentsList.length == 0) ? 0 : (this.startAttachmentIdx / this.attachmentsList.length) );

    return builder;
}

/**
 * submission method that constructs one POST request using
 * the XMLHttpRequest level 2 FormData feature.
 */
PostSpan.prototype.constructFormDataSpan = function() {

	var builder = new FormData();

    var curMedia = this.startAttachmentIdx;
    
    /** emit the xml file */
    builder.append( this.xmlPropertyName, this.xmlFile );

    var first = true;
    var firstFilename = null;
    var lastFilename = null;
    while ( curMedia < this.endAttachmentIdx && curMedia < this.attachmentsList.length ) {
       
        var file = this.attachmentsList[curMedia];

        if ( first ) {
            firstFilename = file.fileName;
        } else {
            lastFilename = file.fileName;
        }
        first = false;

        /* emit the file */
        builder.append( this.attachmentsPropertyName, file );
  
        ++curMedia;
    }

    if ( curMedia < this.attachmentsList.length ) {
        /* emit incomplete flag */
        builder.append( "*isIncomplete*", "yes" );
    }

    setNames( "POST(" + this.postNumber + ") beginning", 
            firstFilename, lastFilename, (this.attachmentsList.length == 0) ? 0 : (this.startAttachmentIdx / this.attachmentsList.length) );

    return builder;
}

/**
 * submission method that constructs and submits one POST request.
 */
PostSpan.prototype.submitSpan = function() {
    /* create the request object... */
    this.xhr = new XMLHttpRequest();

    var thisObject = this;
    
    // callback function invokes callback method... 
    this.xhr.onreadystatechange = function() {
        thisObject.onreadystatechange();
    }

    this.xhr.open("POST", this.postURL, true);
    /* preserve all the session cookies in the ajax post... */
    for ( var header in this.headers ) {
        this.xhr.setRequestHeader(header, this.headers[header] );
    }

    var builder;
    if ( window.FormData ) {
        builder = this.constructFormDataSpan();
        this.xhr.send(builder);
    } else {
        /* need xhr to set content type header (and boundary string) */
        builder = this.constructFirefox3Span(this.xhr);
        this.xhr.sendAsBinary(builder);
    }
}

/**
 * Set the Uploading progress names tag and the progress bar fraction 
 */
function setNames(status, firstFilename, lastFilename, barFraction) {
	setStatus(status);
	setProgressBarPercent(Math.ceil(barFraction*100));
	if ( firstFilename == null ) {
		setNamesString("Uploading xml file");
	} else if ( lastFilename == null ) {
		setNamesString("Uploading xml file + " + firstFilename);
	} else {
		setNamesString("Uploading xml file + " + firstFilename + " ... " + lastFilename);
	}
}

/**
 * Change the contents within an element tag to match that of the body.
 * Used for displaying server error responses.
 * 
 * @param element
 * @param body
 * @return
 */
function setElementInnerHTML(element, body) {
    while ( element.hasChildNodes() ) {
        var node = element.lastChild;
        element.removeChild(node);
    }
    element.innerHTML = body;
}

/**
 * Set the text of a form element.
 * 
 * @param element
 * @param text
 */
function setElementText(element, text) {
	var textNode = document.createTextNode(text);
	if ( element.hasChildNodes() ) {
		var node = element.lastChild;
		element.replaceChild( textNode, node );
	} else {
		element.appendChild( textNode );
	}
}

/**
 * Set the progress names text to the given string.
 * 
 * @param names
 */
function setNamesString(names) {
	setElementText( document.getElementById('progress_names'), names);
}

/**
 * Set the individual upload status string to the given string.
 * 
 * @param status
 */
function setStatus(status) {
	document.getElementById('progress_fatal_response').innerHTML = null;
	setElementText( document.getElementById('progress_status'), status);    
}

/**
 * Set the progress bar percentage and the color of the bar.
 * Do this by replacing the element, as otherwise it doesn't seem to
 * reliably refresh.
 * 
 * @param fraction
 * @param color
 */
function setProgressBarPercent(fraction, color) {
	if ( color == null ) {
		color = 'blue';
	}
	var element = 
		'<div id="progress_bar" ' +
		'style="margin-top: 2px; left: 0%; background-color: ' + color +
	    '; width: ' + fraction + '%; height: 75%;"></div>';
	document.getElementById('progress_bar_container').innerHTML = element;
}

/**
 * Set the final (overall) upload status.  If we have transitioned to phase4,
 * then it is a success.
 *  
 * @param phase
 * @param statusCode
 * @param responseFailureText
 */
function setFinalStatus(phase, statusCode, responseFailureText) {
	if ( phase == 4 ) {
		setProgressBarPercent(100);
        setNamesString("all files");
		setStatus("success!");
	} else {
		setProgressBarPercent(100,'red');
	    var failureContent;
	    if ( responseFailureText != null && responseFailureText.length > 0 ) {
	        var bodyStart = responseFailureText.indexOf('<body');
	        bodyStart = 1 + responseFailureText.indexOf('>', 1+bodyStart);
	        var bodyEnd = responseFailureText.lastIndexOf('</body>');
	        if ( bodyEnd == -1 ) {
		        bodyEnd = responseFailureText.length;
	        }
	        failureContent = responseFailureText.slice(bodyStart, bodyEnd);
	    } else {
		    if ( phase == 1 ) {
			    failureContent = "failed to establish connection to server";
		    } else if ( phase == 2 ) {
			    failureContent = "failed during transmission of data to server";
		    } else if ( phase == 3 ) {
			    failureContent = "failed to receive a response from the server";
		    } else {
			    failureContent = "error " + statusCode + " received from the server";
		    }
	    } 
		setElementInnerHTML( document.getElementById('progress_fatal_response'), failureContent); 
	}
}

/**
 * Clear the media file input field.
 *   
 * @param fieldId
 */
function clearMediaInputField(fieldId) {
	var field = document.getElementById(fieldId);
	if ( field != null ) {
		field.value = '';
		if ( field.value != '') {
			/* must be IE */
			var id = field.getAttribute('id');
			var size = field.getAttribute('size');
			var name = field.getAttribute('name');
			var replacement = document.createElement("input");
			replacement.setAttribute("id", id);
			replacement.setAttribute("size", size);
			replacement.setAttribute("name", name);
			replacement.setAttribute("type", "file");
			field.parentNode.replaceChild(replacement, field);
		}
	}
}
