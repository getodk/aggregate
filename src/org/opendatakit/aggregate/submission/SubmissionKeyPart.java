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

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for extracting parts of a SubmissionKey
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class SubmissionKeyPart {

	private static final String K_OPEN_BRACKET = "[";
	private static final String K_OPEN_BRACKET_KEY_EQUALS = "[@key=";
	private static final String K_CLOSE_BRACKET = "]";
	final String elementName;
	final String auri;

	SubmissionKeyPart(String part) {
		int idx = part.indexOf(K_OPEN_BRACKET);
		if (idx == -1) {
			if (part.indexOf(K_CLOSE_BRACKET) == -1) {
				elementName = part;
				auri = null;
			} else {
				throw new IllegalArgumentException("submission key part "
						+ part + " not well formed");
			}
		} else {
			elementName = part.substring(0, idx);
			String remainder = part.substring(idx);
			if (!remainder.startsWith(K_OPEN_BRACKET_KEY_EQUALS)) {
				throw new IllegalArgumentException("submission key part "
						+ part + " is not well formed");
			}
			if (!remainder.endsWith(K_CLOSE_BRACKET)) {
				throw new IllegalArgumentException("submission key part "
						+ part + " is not well formed");
			}
			auri = remainder.substring(K_OPEN_BRACKET_KEY_EQUALS.length(), remainder.length() - 1);
		}
	}

	public String getElementName() {
		return elementName;
	}

	public String getAuri() {
		return auri;
	}
	
	public static final List<SubmissionKeyPart> splitSubmissionKey(SubmissionKey submissionKey) {
		List<SubmissionKeyPart> parts = new ArrayList<SubmissionKeyPart>();
		String[] stringParts = submissionKey.toString().split(Submission.K_SL);
		for ( String s : stringParts ) {
			parts.add( new SubmissionKeyPart(s));
		}
		return parts;
	}
}