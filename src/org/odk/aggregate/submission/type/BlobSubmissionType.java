/*
 * Copyright (C) 2009 Google Inc.
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

package org.odk.aggregate.submission.type;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.HtmlUtil;
import org.odk.aggregate.constants.ServletConsts;
import org.odk.aggregate.exception.ODKConversionException;
import org.odk.aggregate.servlet.ImageViewerServlet;
import org.odk.aggregate.submission.SubmissionBlob;

import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.repackaged.com.google.common.util.Base64;
import com.google.gson.JsonObject;

/**
 * Data Storage Converter for Blob Type
 * 
 * @author wbrunette@gmail.com
 * 
 */
public class BlobSubmissionType extends SubmissionSingleValueBase<Key> {
	private static final Map<String, String> extMap;
	static {
		extMap = new HashMap<String, String>();
		extMap.put("text/xml", ".xml");
		extMap.put("image/jpeg", ".jpg");
		extMap.put("audio/3gpp", ".3gpp");
		extMap.put("video/3gpp", ".3gp");
		extMap.put("video/mp4", ".mp4");
		extMap.put("text/csv", ".csv");
		extMap.put("audio/amr", ".amr");
		extMap.put("application/vnd.ms-excel", ".xls");
	}

	/**
	 * Constructor
	 * 
	 * @param propertyName
	 *            Name of submission element
	 */
	public BlobSubmissionType(String propertyName) {
		super(propertyName, true);
	}

	/**
	 * Convert value from byte array to data store blob type. Store blob in blob
	 * storage and save the key of the blob storage into submission set.
	 * 
	 * @param byteArray
	 *            byte form of the value
	 * @param submissionSetKey
	 *            key of submission set that will reference the blob
	 * @param contentType
	 *            type of binary data (NOTE: only used for binary data)
	 * @throws ODKConversionException
	 * 
	 */
	@Override
	public void setValueFromByteArray(byte[] byteArray, Key submissionSetKey,
			String submissionType) {
		SubmissionBlob blob = new SubmissionBlob(byteArray, submissionSetKey,
				submissionType);
		setValue(blob.getKey());
	}

	/**
	 * Cannot convert blob from a string
	 * 
	 * @param value
	 * @throws ODKConversionException
	 */
	@Override
	public void setValueFromString(String value) throws ODKConversionException {
		throw new ODKConversionException(ErrorConsts.NO_STRING_TO_BLOB_CONVERT);
	}

	/**
	 * Add submission field value to JsonObject
	 * 
	 * @param JSON
	 *            Object to add value to
	 */
	@Override
	public void addValueToJsonObject(JsonObject jsonObject,
			List<String> propertyNames) {
		if (getValue() == null) {
			return;
		}
		if (!propertyNames.contains(propertyName)) {
			return;
		}
		try {
			SubmissionBlob blobStore = new SubmissionBlob(getValue());
			byte[] imageBlob = blobStore.getBlob();
			if (imageBlob.length > 0) {
				jsonObject.addProperty(propertyName, Base64.encode(imageBlob));
			}

		} catch (EntityNotFoundException e) {
			// TODO: consider better error handling, right now just skip adding
			// to object
		}
	}

	private String newMD5HashFilename(String value) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] asBytes = value.getBytes();
			md.update(asBytes);

			byte[] messageDigest = md.digest();

			BigInteger number = new BigInteger(1, messageDigest);
			String md5 = number.toString(16);
			while (md5.length() < 32)
				md5 = "0" + md5;
			return md5;
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(
					"Unexpected problem computing md5 hash", e);
		}
	}

	private String newMD5HashUri(byte[] asBytes) {
        try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			md.update(asBytes);
			
            byte[] messageDigest = md.digest();

            BigInteger number = new BigInteger(1, messageDigest);
            String md5 = number.toString(16);
            while (md5.length() < 32)
                md5 = "0" + md5;
            return "md5:" + md5;
        } catch (NoSuchAlgorithmException e) {
        	throw new IllegalStateException("Unexpected problem computing md5 hash", e);
		}
	}

	@Override
	public void addValueToXmlSerialization(StringBuilder b) {
		Key value = getValue();
		if (value == null) {
			b.append("<" + propertyName + "/>");
		} else {
			String md5Filename = newMD5HashFilename(KeyFactory
					.keyToString(value));

			SubmissionBlob blobStore;
			try {
				blobStore = new SubmissionBlob(value);
				String type = blobStore.getContentType();
				b.append("<" + propertyName + ">");
				b.append(StringEscapeUtils.escapeXml(md5Filename + extMap.get(type)));
				b.append("</" + propertyName + ">");
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
				b.append("<" + propertyName + "/>");
			}

		}
	}

	@Override
	public void addValueToXmlAttachmentSerialization(StringBuilder b, String baseServerUrl) {
		Key value = getValue();
		if (value != null) {
			String md5Filename = newMD5HashFilename(KeyFactory
					.keyToString(value));

			SubmissionBlob blobStore;
			try {
				blobStore = new SubmissionBlob(value);
				String type = blobStore.getContentType();
			    Map<String, String> properties = new HashMap<String, String>();
			    properties.put(ServletConsts.BLOB_KEY, KeyFactory.keyToString(value));
			    String urlLink = HtmlUtil.createLinkWithProperties(baseServerUrl + ImageViewerServlet.ADDR, properties);
				// parallel to XFormsManifestXmlTable
			    String xmlString = "<mediaFile>" +
			    		"<filename>" + StringEscapeUtils.escapeXml(md5Filename + extMap.get(type)) + "</filename>" +
			    		"<hash>"	+ StringEscapeUtils.escapeXml(newMD5HashUri(blobStore.getBlob())) + "</hash>" +
			    		"<downloadUrl>"	+ StringEscapeUtils.escapeXml(urlLink) + "</downloadUrl>" +
			    	"</mediaFile>\n";

				b.append(xmlString);
			} catch (EntityNotFoundException e) {
				e.printStackTrace();
			}

		}
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof BlobSubmissionType)) {
			return false;
		}
		if (!super.equals(obj)) {
			return false;
		}
		return true;
	}

}
