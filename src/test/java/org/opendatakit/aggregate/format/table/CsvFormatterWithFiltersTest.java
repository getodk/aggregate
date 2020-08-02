package org.opendatakit.aggregate.format.table;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.aggregate.constants.common.FormElementNamespace;
import org.opendatakit.aggregate.datamodel.FormElementModel;
import org.opendatakit.aggregate.form.IForm;
import org.opendatakit.aggregate.format.Row;
import org.opendatakit.aggregate.format.element.ElementFormatter;
import org.opendatakit.aggregate.submission.Submission;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

public class CsvFormatterWithFiltersTest {

  private IForm form = mock(IForm.class);
  private StringWriter actual;
  private ArrayList<String> testList = new ArrayList<>();
  private ArrayList<Submission> subs = new ArrayList<>();
  private CsvFormatterWithFilters csv;

  @SuppressWarnings("unchecked")
  @Before
  public void setup() throws ODKDatastoreException {
    actual = new StringWriter();
    PrintWriter pr = new PrintWriter(actual);
    testList.clear();
    
    subs.clear();
    Submission s = mock(Submission.class);
    Row row = mock(Row.class);
    when(row.getFormattedValues()).thenReturn(testList);
    when(s.getFormattedValuesAsRow((List<FormElementNamespace>)any(), 
        (List<FormElementModel>)any(), 
        (ElementFormatter)any(), 
        anyBoolean(), 
        (CallingContext)any())).thenReturn(row);
    subs.add(s);

    csv = new CsvFormatterWithFilters(form, "https://getodk.org", pr, null);
  }

  @Test
  public void testNoNullInExport() throws ODKDatastoreException {
    assertRowCSVOutput(",\"foo\",,\"27\"\n", null, "foo", null, "27");
  }

  @Test
  public void testEscapeQuotes() throws ODKDatastoreException {
    assertRowCSVOutput("\"One does not simply \"\"escape quotation marks\"\"\"\n", "One does not simply \"escape quotation marks\"");
  }

  private void assertRowCSVOutput(String expected, String ... values) throws ODKDatastoreException {
    Collections.addAll(testList, values);
    csv.processSubmissionSegment(subs, null);
    Assert.assertEquals(expected, actual.toString());
  }

}
