package org.opendatakit.aggregate.client;

import java.util.List;

import org.opendatakit.aggregate.client.form.FormServiceAsync;
import org.opendatakit.aggregate.client.form.KmlSettingOption;
import org.opendatakit.aggregate.client.form.KmlSettings;
import org.opendatakit.aggregate.client.submission.SubmissionServiceAsync;
import org.opendatakit.aggregate.client.submission.UIGeoPoint;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.maps.client.MapWidget;
import com.google.gwt.maps.client.Maps;
import com.google.gwt.maps.client.control.LargeMapControl;
import com.google.gwt.maps.client.geom.LatLng;
import com.google.gwt.maps.client.overlay.Marker;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CreateNewMapPopup extends PopupPanel {
  private List<KmlSettingOption> geoPoints = null;
  private FlowPanel space = new FlowPanel();
  private boolean mapsApiLoaded = false;
  
  public CreateNewMapPopup (final String formId, final FormServiceAsync formSvc, final SubmissionServiceAsync submissionSvc) {
    super(true);
    
    Maps.loadMapsApi("", "2", false, new Runnable() {
      public void run() {
        mapsApiLoaded = true;
      }
    });
    
    FlexTable selectionTable = new FlexTable();
    selectionTable.setWidget(0, 0, new HTML("Form: " + formId + " "));
    
    final ListBox kmlStuff = new ListBox();
    
    formSvc.getGpsCoordnates(formId, new AsyncCallback<KmlSettings>() {
      public void onFailure(Throwable c) {}
      public void onSuccess(KmlSettings result) {
        geoPoints = result.getGeopointNodes();
      for (KmlSettingOption kSO : geoPoints)
        kmlStuff.addItem(kSO.getDisplayName());
      }
    });
    
    selectionTable.setText(0, 0, "Form: " + formId + " ");
    selectionTable.setWidget(0, 1, kmlStuff);
    
    Button trigger = new Button("Map It!");
    trigger.addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        submissionSvc.getGeoPoints(formId, geoPoints.get(kmlStuff.getSelectedIndex()).getElementKey(),
            new AsyncCallback<UIGeoPoint[]>() {
              @Override
              public void onFailure(Throwable caught) {
                // TODO Auto-generated method stub
              }
              @Override
              public void onSuccess(UIGeoPoint[] result) {
                if (!mapsApiLoaded)
                  return;
                LatLng center;
                if (result == null || result.length == 0)
                  center = LatLng.newInstance(0.0, 0.0);
                else
                  center = LatLng.newInstance(Double.parseDouble(result[0].getLatitude()), Double.parseDouble(result[0].getLongitude()));
                final MapWidget map = new MapWidget(center, 2);
                map.setSize("100%", "100%");
                map.addControl(new LargeMapControl());
                space.add(map);
                // TODO Auto-generated method stub
                for (UIGeoPoint u : result) {
                  if (u == null || u.getLatitude() == null || u.getLongitude() == null)
                    continue;
                  try {
                    LatLng ll = LatLng.newInstance(Double.parseDouble(u.getLatitude()), Double.parseDouble(u.getLongitude()));
                    map.addOverlay(new Marker(ll));
                  } catch (NumberFormatException e) {
                    continue;
                  }
                }
              }
            });
      }
    });
    
    selectionTable.setWidget(0, 2, trigger);
    VerticalPanel layout = new VerticalPanel();
    layout.add(selectionTable);
    space.getElement().setId("map_area");
    layout.add(space);
    setWidget(layout);
  }
  
  private void buildUi(final MapWidget map) {
  }
}
