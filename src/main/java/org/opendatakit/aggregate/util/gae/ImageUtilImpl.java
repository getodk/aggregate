package org.opendatakit.aggregate.util.gae;

import org.opendatakit.aggregate.util.ImageUtil;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

public class ImageUtilImpl implements ImageUtil {

  @Override
  public byte[] resizeImage(byte[] imageBlob, int width, int height) {
    ImagesService imagesService = ImagesServiceFactory.getImagesService();
    Transform resize = ImagesServiceFactory.makeResize(width, height);
   
    Image oldImage = ImagesServiceFactory.makeImage(imageBlob);
    Image newImage = imagesService.applyTransform(resize, oldImage);

    imageBlob = newImage.getImageData();
    return imageBlob;
  }

}
