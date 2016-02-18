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
