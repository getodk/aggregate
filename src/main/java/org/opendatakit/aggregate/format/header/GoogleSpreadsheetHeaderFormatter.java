package org.opendatakit.aggregate.format.header;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.constants.externalservice.SpreadsheetConsts;
import org.opendatakit.aggregate.constants.format.FormatConsts;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.datamodel.FormElementModel.ElementType;
import org.opendatakit.aggregate.form.Form;
import org.opendatakit.common.web.constants.BasicConsts;

/**
 * Determines how tables are represented in GoogleSpreadsheets
 * 
 * @author the.dylan.price@gmail.com
 */
public class GoogleSpreadsheetHeaderFormatter extends BasicHeaderFormatter implements
    HeaderFormatter {

  // Specified by BasicHeaderFormatter's constructor.
  public GoogleSpreadsheetHeaderFormatter(boolean separateGpsCoordinates,
      boolean includeGpsAltitude, boolean includeGpsAccuracy) {
    super(separateGpsCoordinates, includeGpsAltitude, includeGpsAccuracy);
  }

  /**
   * Iterates the forms and creates the headers and types based off the passed
   * in FormElementModel. Generates the same headers as BasicHeaderFormatter,
   * but additionally includes the parent UID of the form as the first header,
   * and removes all unsafe characters from the headers.
   * 
   * @param formDefinition
   *          the xform that is being used to create the header
   * @param rootGroup
   *          the group of the xform that contains several values that will be
   *          used to generate the headers. The node should correspond to a
   *          SubmissionSet, not a SubmissionValue.
   * @param propertyNames
   *          list of properties to include in headers, if null is passed will
   *          return all properties for the SubmissionSet
   */
  @Override
  public List<String> generateHeaders(Form form, FormElementModel rootGroup,
      List<FormElementModel> propertyNamesArg) {
    propertyNames = propertyNamesArg;
    headers = new ArrayList<String>();
    types = new ArrayList<ElementType>();

    if (!form.getTopLevelGroupElement().equals(rootGroup)) {
      headers.add(FormatConsts.HEADER_PARENT_UID);
      types.add(ElementType.METADATA);
    }

    processElementForColumnHead(rootGroup, rootGroup, BasicConsts.EMPTY_STRING);

    // remove bad characters from headers
    for (int i = 0; i < headers.size(); i++)
    {
      String header = headers.get(i);
      header = header.replaceAll(SpreadsheetConsts.UNSAFE_CHAR_CLASS, "");
      headers.set(i, header);
    }

    return headers;
  }

}
