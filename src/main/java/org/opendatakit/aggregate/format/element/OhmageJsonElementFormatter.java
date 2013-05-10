/*
 * Copyright (C) 2010 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.aggregate.format.element;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.http.entity.mime.content.ByteArrayBody;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.externalservice.OhmageJsonTypes;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.submission.SubmissionRepeat;
import org.opendatakit.aggregate.submission.SubmissionSet;
import org.opendatakit.aggregate.submission.type.BlobSubmissionType;
import org.opendatakit.aggregate.submission.type.GeoPoint;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * TODO: comment
 *
 * @author the.dylan.price@gmail.com
 */
public class OhmageJsonElementFormatter implements ElementFormatter {

	private List<OhmageJsonTypes.Response> responses;
	private Map<UUID, ByteArrayBody> photos;

	public OhmageJsonElementFormatter() {
		this.responses = new ArrayList<OhmageJsonTypes.Response>();
		this.photos = new HashMap<UUID, ByteArrayBody>();
	}

	@Override
	public void formatUid(String uri, String propertyName, Row row) {
		// unneeded so unimplemented
	}

	@Override
	public void formatBinary(BlobSubmissionType blobSubmission,
			FormElementModel element, String ordinalValue, Row row,
			CallingContext cc) throws ODKDatastoreException {
		if (!(blobSubmission == null
				|| (blobSubmission.getAttachmentCount(cc) == 0) || (blobSubmission
					.getContentHash(1, cc) == null))) {
			byte[] imageBlob = null;
			if (blobSubmission.getAttachmentCount(cc) == 1) {
				imageBlob = blobSubmission.getBlob(1, cc);
			}
			if (imageBlob != null && imageBlob.length > 0) {
				UUID photoUUID = UUID.randomUUID();
				OhmageJsonTypes.photo photo = new OhmageJsonTypes.photo(
						element.getElementName(), photoUUID);
				responses.add(photo);
				photos.put(photoUUID,
	new ByteArrayBody(imageBlob, blobSubmission.getContentType(1, cc), photoUUID.toString()));
			}
		}
	}

	@Override
	public void formatBoolean(Boolean bool, FormElementModel element,
			String ordinalValue, Row row) {
		OhmageJsonTypes.text resp = new OhmageJsonTypes.text(
				element.getElementName(), bool.toString());
		responses.add(resp);
	}

	@Override
	public void formatChoices(List<String> choices, FormElementModel element,
			String ordinalValue, Row row) {
		OhmageJsonTypes.Response choice;
		// TODO: how to retrieve all possible choices?
		choice = new OhmageJsonTypes.multi_choice_custom(
				element.getElementName(), choices, null);
		responses.add(choice);
	}

	@Override
	public void formatDate(Date date, FormElementModel element,
			String ordinalValue, Row row) {
		OhmageJsonTypes.timestamp resp = new OhmageJsonTypes.timestamp(
				element.getElementName(), date);
		responses.add(resp);
	}

	@Override
	public void formatDateTime(Date date, FormElementModel element,
			String ordinalValue, Row row) {
		formatDate(date, element, ordinalValue, row);
	}

	@Override
	public void formatTime(Date date, FormElementModel element,
			String ordinalValue, Row row) {
		formatDate(date, element, ordinalValue, row);
	}

	@Override
	public void formatDecimal(BigDecimal dub, FormElementModel element,
			String ordinalValue, Row row) {
		OhmageJsonTypes.text resp = new OhmageJsonTypes.text(
				element.getElementName(), dub.toString());
		responses.add(resp);
	}

	@Override
	public void formatGeoPoint(GeoPoint coordinate, FormElementModel element,
			String ordinalValue, Row row) {
		OhmageJsonTypes.text resp = new OhmageJsonTypes.text(
				element.getElementName(), coordinate.toString());
		responses.add(resp);
	}

	@Override
	public void formatLong(Long longInt, FormElementModel element,
			String ordinalValue, Row row) {
		OhmageJsonTypes.number resp = new OhmageJsonTypes.number(
				element.getElementName(), longInt);
		responses.add(resp);
	}

	@Override
	public void formatRepeats(SubmissionRepeat repeat,
			FormElementModel repeatElement, Row row, CallingContext cc)
			throws ODKDatastoreException {
		// TODO: how to get skipped, not_displayed?
		OhmageJsonTypes.RepeatableSet repeatSet = new OhmageJsonTypes.RepeatableSet(
				repeatElement.getElementName(), false, false);
		for (SubmissionSet set : repeat.getSubmissionSets()) {
			OhmageJsonElementFormatter formatter = new OhmageJsonElementFormatter();
			set.getFormattedValuesAsRow(null, formatter, false, cc);
			repeatSet.addRepeatableSetIteration(formatter.getResponses());
		}
		responses.add(repeatSet);
	}

	@Override
	public void formatString(String string, FormElementModel element,
			String ordinalValue, Row row) {
		OhmageJsonTypes.text resp = new OhmageJsonTypes.text(
				element.getElementName(), string);
		responses.add(resp);
	}

	/**
	 * @return the responses
	 */
	public List<OhmageJsonTypes.Response> getResponses() {
		return responses;
	}

	/**
	 * @return the photos
	 */
	public Map<UUID, ByteArrayBody> getPhotos() {
		return photos;
	}

	public static String getBinaryContentTransferEncoding() {
		return "base64";
	}
}
