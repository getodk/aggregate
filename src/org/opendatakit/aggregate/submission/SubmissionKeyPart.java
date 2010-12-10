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
package org.opendatakit.aggregate.submission;


/**
 * Helper class for extracting parts of a SubmissionKey
 * 
 * @author mitchellsundt@gmail.com
 * @author wbrunette@gmail.com
 * 
 */
public class SubmissionKeyPart {

	public static final String K_OPEN_BRACKET = "[";
	public static final String K_OPEN_BRACKET_KEY_EQUALS = "[@key=";
	public static final String K_OPEN_BRACKET_ORDINAL_EQUALS = "[@ordinal=";
	public static final String K_AND_VERSION_EQUALS = " and @version=";
	public static final String K_CLOSE_BRACKET = "]";
	final String elementName;
	final String auri;
	final Long ordinal;
	final String version;

	SubmissionKeyPart(String part) {
		int idx = part.indexOf(K_OPEN_BRACKET);
		if (idx == -1) {
			if (part.indexOf(K_CLOSE_BRACKET) == -1) {
				elementName = part;
				auri = null;
				ordinal = null;
				version = null;
			} else {
				throw new IllegalArgumentException("submission key part "
						+ part + " not well formed");
			}
		} else {
			elementName = part.substring(0, idx);
			String remainder = part.substring(idx);
			if (remainder.startsWith(K_OPEN_BRACKET_KEY_EQUALS)) {
				if (!remainder.endsWith(K_CLOSE_BRACKET)) {
					throw new IllegalArgumentException("submission key part "
							+ part + " is not well formed");
				}
				auri = remainder.substring(K_OPEN_BRACKET_KEY_EQUALS.length(), remainder.length() - 1);
				ordinal = null;
				version = null;
			} 
			else if (remainder.startsWith(K_OPEN_BRACKET_ORDINAL_EQUALS)) {
				if (!remainder.endsWith(K_CLOSE_BRACKET)) {
					throw new IllegalArgumentException("submission key part "
							+ part + " is not well formed");
				}
				auri = null;
				String ordinalStr = remainder.substring(K_OPEN_BRACKET_ORDINAL_EQUALS.length(), remainder.length() - 1);
				if ( ordinalStr.contains(K_AND_VERSION_EQUALS) ) {
					idx = ordinalStr.indexOf(K_AND_VERSION_EQUALS);
					version = ordinalStr.substring(idx+K_AND_VERSION_EQUALS.length());
					ordinalStr = ordinalStr.substring(0,idx);
				} else {
					version = null;
				}
				ordinal = Long.valueOf(ordinalStr);
			}
			else {
				throw new IllegalArgumentException("submission key part "
						+ part + " is not well formed");
			}
		}
	}

	public String getElementName() {
		return elementName;
	}

	public String getAuri() {
		return auri;
	}
	
	public Long getOrdinalNumber() {
		return ordinal;
	}

	public String getVersion() {
		return version;
	}
}