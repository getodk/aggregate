package org.opendatakit.aggregate.client.widgets;

import java.util.ArrayList;

import org.opendatakit.aggregate.client.form.FormSummary;
import org.opendatakit.aggregate.constants.common.FormActionStatusTimestamp;
import org.opendatakit.common.web.constants.BasicConsts;

import com.google.gwt.event.dom.client.ChangeHandler;

public final class FormListBox extends AggregateListBox {
  private static final String TOOLTIP_TEXT = "Form to use";

  private static final String NO_FORM = "none";

  private ArrayList<FormSummary> displayedFormList;

  public FormListBox() {
    super(TOOLTIP_TEXT, false);
  }

  public FormListBox(ChangeHandler handler) {
    this();
    addChangeHandler(handler);
  }

  public void updateFormDropDown(ArrayList<FormSummary> forms) {

    FormSummary currentFormSelected = getSelectedForm();

    // what the selected index should be set to
    int selectedIndex = 0; // default to the top position, update if available

    if (forms == null || forms.size() == 0) {
      forms = new ArrayList<FormSummary>();
      forms.add(new FormSummary(NO_FORM, BasicConsts.EMPTY_STRING, null, null, false, false, null, 0));
    }

    clear();

    // populate the form box
    for (int i = 0; i < forms.size(); i++) {
      FormSummary form = forms.get(i);
      // don't show forms marked for deletion...
      FormActionStatusTimestamp deletionStatus = form.getMostRecentDeletionRequestStatus();
      if (deletionStatus != null && deletionStatus.getStatus().isActiveRequest())
        continue;

      addItem(form.getTitle(), form.getId());
      if (form.equals(currentFormSelected)) {
        selectedIndex = getItemCount() - 1;
      }
    }

    if (getItemCount() > 0) {
      // update the panel to display the right form
      setItemSelected(selectedIndex, true);
    }

    displayedFormList = forms;
  }

  public FormSummary getSelectedForm() {
    int selectedIndex = getSelectedIndex();
    if (selectedIndex > -1 && displayedFormList != null) {
      String formId = getValue(selectedIndex);
      for (FormSummary form : displayedFormList) {
        String formIdToMatch = form.getId();
        // check if we have a form id
        if (formIdToMatch == null) {
          // if there is no formId this should be the 'NO_FORM' form
          if (form.getTitle() != null && form.getTitle().equals(NO_FORM)
              && formId.equals(BasicConsts.EMPTY_STRING)) {
            return null;
          } else {
            throw new IllegalStateException(
                "Some how a form that is not the 'NO_FORM' had a formId of null");
          }
        } else if (formIdToMatch.equals(formId)) { // check if the formId from
                                                   // listbox matches the form
          return form;
        }
      }
    }
    // return null if the form is not found
    return null;
  }

}
