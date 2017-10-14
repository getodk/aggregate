package org.opendatakit.aggregate.util.gretty;

import org.opendatakit.aggregate.util.ImageUtil;
import org.opendatakit.tomcatutil.ImageResize;

public class ImageUtilImpl implements ImageUtil {
  @Override
  public byte[] resizeImage(byte[] imageBlob, int width, int height) {
    ImageResize resize = new ImageResize();
    return resize.resizeImage(imageBlob, width, height);
  }
}
