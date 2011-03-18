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
package org.odk.aggregate.form.remoteserver;

/**
 * 
 * @author wbrunette@gmail.com
 * @author mitchellsundt@gmail.com
 * 
 */
public class OAuthToken {

	private final String _token;
	private final String _tokenSecret;
	
	public OAuthToken(String token, String tokenSecret)
	{
		_token = token;
		_tokenSecret = tokenSecret;
	}

	public String getToken() {
		return _token;
	}

	public String getTokenSecret() {
		return _tokenSecret;
	}	
}
