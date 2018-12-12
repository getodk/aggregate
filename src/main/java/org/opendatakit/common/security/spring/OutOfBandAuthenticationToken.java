/*
 * Copyright (C) 2012 University of Washington.
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
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;

/**
 * Authentication token returned when an out-of-band user identity is processed.
 * Structure liberally copied from Spring OpenId and Oauth2 authentication token classes.
 * These are considered token-based authentications.
 *
 * @author mitchellsundt@gmail.com
 */
public class OutOfBandAuthenticationToken extends AbstractAuthenticationToken {

  private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;

  //~ Instance fields ================================================================================================
  private final Object principal;
  private final String email;

  //~ Constructors ===================================================================================================

  public OutOfBandAuthenticationToken(String email) {
    super(new ArrayList<GrantedAuthority>(0));
    this.principal = email;
    this.email = email;

    setAuthenticated(false);
  }

  /**
   * Created by the <tt>OutOfBandAuthenticationProvider</tt> on successful authentication.
   *
   * @param principal usually the <tt>UserDetails</tt> returned by the the configured <tt>UserDetailsService</tt>
   *                  used by the <tt>OutOfBandAuthenticationProvider</tt>.
   */
  public OutOfBandAuthenticationToken(Object principal, Collection<? extends GrantedAuthority> authorities,
                                      String email) {
    super(authorities);
    this.principal = principal;
    this.email = email;

    setAuthenticated(true);
  }

  //~ Methods ========================================================================================================

  /**
   * Returns 'null' always, as no credentials are processed by the OutOfBand provider.
   *
   * @see org.springframework.security.core.Authentication#getCredentials()
   */
  public Object getCredentials() {
    return null;
  }

  public String getEmail() {
    return email;
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
    return "[" + super.toString() + ", email : " + email + "]";
  }
}
