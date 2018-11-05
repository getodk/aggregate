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

package org.opendatakit.aggregate.util.gae;

import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import org.opendatakit.aggregate.util.ImageUtil;

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
