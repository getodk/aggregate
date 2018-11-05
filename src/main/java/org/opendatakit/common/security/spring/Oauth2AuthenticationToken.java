/*
 * Copyright (C) 2012 University of Washington
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
package org.opendatakit.common.security.spring;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import org.opendatakit.common.utils.WebUtils;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

/**
 * Authentication that is returned by Oauth 2.0 processing.
 * Structure liberally copied from Spring OpenId and Oauth2 authentication token classes.
 *
 * @author mitchellsundt@gmail.com
 */
public class Oauth2AuthenticationToken extends AbstractAuthenticationToken {

  /**
   *
   */
  private static final long serialVersionUID = 957149283924292479L;

  //~ Instance fields ================================================================================================
  private final Object principal;
  private String accessToken;
  private String email;
  private Date expiration;

  public Oauth2AuthenticationToken(String accessToken, String email, Date expiration) {
    super(new ArrayList<GrantedAuthority>(0));
    this.principal = accessToken;
    this.accessToken = accessToken;
    this.email = email;
    this.expiration = expiration;

    setAuthenticated(false);
  }

  /**
   * Created by the <tt>Oauth2AuthenticationProvider</tt> on successful authentication.
   *
   * @param principal usually the <tt>UserDetails</tt> returned by the the configured <tt>UserDetailsService</tt>
   *                  used by the <tt>Oauth2AuthenticationProvider</tt>.
   */
  public Oauth2AuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities,
                                   String accessToken, String email, Date expiration) {
    super(authorities);
    this.principal = principal;
    this.accessToken = accessToken;
    this.email = email;
    this.expiration = expiration;

    setAuthenticated(true);
  }

  //~ Methods ========================================================================================================

  /**
   * Returns 'null' always, as no credentials are processed by the OAuth2 provider.
   *
   * @see org.springframework.security.core.Authentication#getCredentials()
   */
  public Object getCredentials() {
    return null;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public String getEmail() {
    return email;
  }

  public Date getExpiration() {
    return expiration;
  }

  /**
   * Returns the <tt>principal</tt> value.
   *
   * @see org.springframework.security.core.Authentication#getPrincipal()
   */
  public Object getPrincipal() {
    return principal;
  }

  @Override
  public String toString() {
    return "[" + super.toString() + ", email : " + email +
        ", expiration : " + WebUtils.iso8601Date(expiration) + "]";
  }
}
