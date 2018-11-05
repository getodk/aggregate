/*
 * Copyright (C) 2011 University of Washington
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
package org.opendatakit.aggregate.datamodel;

import org.opendatakit.common.web.CallingContext;

/**
 * Visitor interface for the {@link FormElementModel#depthFirstTraversal }
 * method.
 *
 * @author mitchellsundt@gmail.com
 */
public interface FormElementModelVisitor {

  /**
   * Invoked when traversing a form element without children (includes selectN,
   * binary and geopoint).
   *
   * @param element
   * @param cc
   * @return true if the traversal should immediately stop, false otherwise.
   */
  public boolean traverse(FormElementModel element, CallingContext cc);

  /**
   * Invoked when entering a form element with children (e.g., group or repeat).
   *
   * @param element
   * @param cc
   * @return true if the traversal should immediately stop, false otherwise.
   */
  public boolean enter(FormElementModel element, CallingContext cc);

  /**
   * Invoked prior to descending a repeat element's concrete instance. This
   * predicate is used to determine how many repetitions of a repeat group are
   * manifest within a given submission. The return value does not figure into
   * the short-circuit logic of the enter() and traverse() methods.
   *
   * @param element - a repeat element
   * @param ordinal - the 1st, 2nd, etc. repeat
   * @param cc
   * @return true if the children should be traversed (this ordinal exists),
   *     false otherwise.
   */
  public boolean descendIntoRepeat(FormElementModel element, int ordinal, CallingContext cc);

  /**
   * Invoked after all completing the traversal of all the children of a
   * concrete instance of a repeat element.
   *
   * @param element - a repeat element
   * @param ordinal
   * @param cc
   */
  public void ascendFromRepeat(FormElementModel element, int ordinal, CallingContext cc);

  /**
   * Invoked when a form element with children is popped from the stack.
   *
   * @param element
   * @param cc
   */
  public void leave(FormElementModel element, CallingContext cc);
}
