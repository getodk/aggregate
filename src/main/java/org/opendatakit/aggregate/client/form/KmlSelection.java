package org.opendatakit.aggregate.client.form;

import static org.opendatakit.aggregate.client.form.KmlSelection.KmlOptionType.GEO_POINT;
import static org.opendatakit.aggregate.client.form.KmlSelection.KmlOptionType.GEO_TRACE_N_SHAPE;
import static org.opendatakit.aggregate.client.form.KmlSelection.KmlOptionType.UNDEFINED;
import static org.opendatakit.aggregate.client.form.KmlSelection.KmlOptionType.valueOf;

import java.io.Serializable;
import java.util.Optional;

public class KmlSelection implements Serializable {
  /**
   * Serialization Version Identifier
   */
  private static final long serialVersionUID = -6226679138155053126L;

  private static final String DELIMITER = ":-:";
  // universal for all
  private String formId;
  private KmlOptionType geoType;
  private String geoElementKey;
  // for GEO_POINT
  private String titleElementKey;
  private String binaryElementKey;
  // for GEO_TRACE_N_SHAPE
  private String nameElementKey;

  // default constructor required
  KmlSelection() {

  }

  public KmlSelection(String formId) {
    this.formId = formId;
    clear();
  }

  public static Optional<KmlSelection> from(String encodedString) {
    String[] fieldStrings = encodedString.split(DELIMITER);

    if (fieldStrings.length < 3)
      return Optional.empty();

    String formId = fieldStrings[0];
    KmlOptionType type = valueOf(fieldStrings[1]);
    String geoElementKey = fieldStrings[2];

    // check basic constraints on size and nullability
    if (formId == null || geoElementKey == null)
      return Optional.empty();

    if (type == GEO_POINT && fieldStrings.length != 5)
      return Optional.empty();

    if (type == GEO_TRACE_N_SHAPE && fieldStrings.length != 4)
      return Optional.empty();

    if (type != GEO_POINT && type != GEO_TRACE_N_SHAPE)
      return Optional.empty();

    KmlSelection selection = new KmlSelection(formId);
    if (type == GEO_POINT)
      selection.setGeoPointSelections(geoElementKey, fieldStrings[3], fieldStrings[4]);

    if (type == GEO_TRACE_N_SHAPE)
      selection.setGeoTraceNShapeSelections(geoElementKey, fieldStrings[3]);

    return Optional.of(selection);
  }

  public static KmlSelection createKmlSelectionFromEncodedString(String encodedString) {
    String[] fieldStrings = encodedString.split(DELIMITER);

    if (fieldStrings.length < 3) {
      return null;
    }

    String formId = fieldStrings[0];
    KmlOptionType type = valueOf(fieldStrings[1]);
    String geoElementKey = fieldStrings[2];

    // check basic constraints on size and nullability
    if ((type == null) || (formId == null) || (geoElementKey == null) ||
        (type == GEO_POINT && fieldStrings.length != 5) ||
        (type == GEO_TRACE_N_SHAPE && fieldStrings.length != 4)) {
      return null;
    }


    KmlSelection selection = new KmlSelection(formId);
    if (type == GEO_POINT) {
      selection.setGeoPointSelections(geoElementKey, fieldStrings[3], fieldStrings[4]);
    } else if (type == GEO_TRACE_N_SHAPE) {
      selection.setGeoTraceNShapeSelections(geoElementKey, fieldStrings[3]);
    } else {
      return null;
    }

    return selection;
  }

  private void clear() {
    geoType = UNDEFINED;
    geoElementKey = null;
    titleElementKey = null;
    binaryElementKey = null;
    nameElementKey = null;
  }

  public void setGeoPointSelections(String geoKey, String titleKey,
                                    String binaryKey) {
    clear();
    geoType = GEO_POINT;
    this.geoElementKey = geoKey;
    this.titleElementKey = titleKey;
    this.binaryElementKey = binaryKey;
  }

  public void setGeoTraceNShapeSelections(String geoKey, String nameKey) {
    clear();
    geoType = GEO_TRACE_N_SHAPE;
    this.geoElementKey = geoKey;
    this.nameElementKey = nameKey;
  }

  public KmlOptionType getGeoType() {
    return geoType;
  }

  public String getGeoElementKey() {
    return geoElementKey;
  }

  public String getTitleElementKey() {
    return titleElementKey;
  }

  public String getBinaryElementKey() {
    return binaryElementKey;
  }

  public String getNameElementKey() {
    return nameElementKey;
  }

  public String generateEncodedString() {
    StringBuilder buffer = new StringBuilder();
    buffer.append(formId);
    buffer.append(DELIMITER);
    buffer.append(geoType);
    buffer.append(DELIMITER);
    buffer.append(geoElementKey);
    buffer.append(DELIMITER);
    switch (geoType) {
      case GEO_POINT:
        buffer.append(titleElementKey);
        buffer.append(DELIMITER);
        buffer.append(binaryElementKey);
        break;
      case GEO_TRACE_N_SHAPE:
        buffer.append(nameElementKey);
        break;
      default:
        return null;
    }
    return buffer.toString();
  }

  public enum KmlOptionType {
    GEO_TRACE_N_SHAPE, GEO_POINT, UNDEFINED;
  }
}
