/*
 * Copyright (C) 2014 University of Washington
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
package org.opendatakit.aggregate.odktables.rest;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;

/**
 * The OpenCSV library does not implement RFC4180 properly for the CSVReader.
 * Implement the writer here for what we need, and do away with the opencsv
 * dependency entirely.  The RFC4180CsvReader is a working reader.
 *
 * @author mitchellsundt@gmail.com
 *
 */
public class RFC4180CsvWriter {

  private final BufferedWriter bw;

  private final char cr = 13;

  private final char lf = 10;

  private final char separator = ',';

  private final char quotechar = '"';

  public RFC4180CsvWriter(Writer writer) {
    this.bw = new BufferedWriter(writer);
  }

  /**
   * Writes the next line to the file. All non-null fields
   * are written surrounded by
   *
   * @param nextLine
   *            a string array with each comma-separated element as a separate entry.
   * @throws IOException
   */
  public void writeNext(String[] nextLine) throws IOException {

    if (nextLine == null)
       return;

    boolean first = true;
    for ( String term : nextLine ) {
      if ( !first ) {
        bw.write(separator);
      }
      first = false;
      if ( term != null ) {
        // wrap an empty string in double-quotes to
        // distinguish between null and empty string
        if ( term.length() == 0 ||
             term.indexOf(cr) != -1 ||
             term.indexOf(lf) != -1 ||
			 term.indexOf(separator) != -1 ||
             term.indexOf(quotechar) != -1 ) {
          // this string needs to be quoted
          bw.write(quotechar);
          // and any quotes within need to be doubled-up
          term = term.replaceAll("\"", "\"\"");
          bw.write(term);
          bw.write(quotechar);
        } else {
          // simple string -- just emit it
          bw.write(term);
        }
      }
    }
    bw.write(cr);
    bw.write(lf);
  }

  /**
   * Flush underlying stream to writer.
   *
   * @throws IOException if bad things happen
   */
  public void flush() throws IOException {

      bw.flush();

  }

  /**
   * Close the underlying stream writer flushing any buffered content.
   *
   * @throws IOException if bad things happen
   *
   */
  public void close() throws IOException {
      flush();
      bw.close();
  }

}
