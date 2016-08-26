/*
 * Copyright (C) 2016 University of Washington
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
package org.opendatakit.aggregate.odktables;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.http.HeaderElement;
import org.apache.http.message.BasicHeaderValueParser;
import org.apache.http.message.HeaderValueParser;
import org.apache.wink.common.model.multipart.InMultiPart;
import org.apache.wink.common.model.multipart.InPart;
import org.opendatakit.aggregate.odktables.api.InstanceFileService;
import org.opendatakit.aggregate.odktables.exception.InstanceFileModificationException;
import org.opendatakit.aggregate.odktables.exception.ODKTablesException;
import org.opendatakit.aggregate.odktables.exception.PermissionDeniedException;
import org.opendatakit.aggregate.odktables.relation.DbTableInstanceFiles;
import org.opendatakit.aggregate.odktables.relation.DbTableInstanceManifestETags;
import org.opendatakit.aggregate.odktables.relation.DbTableInstanceManifestETags.DbTableInstanceManifestETagEntity;
import org.opendatakit.aggregate.odktables.rest.entity.TableRole.TablePermission;
import org.opendatakit.aggregate.odktables.security.TablesUserPermissions;
import org.opendatakit.common.datamodel.BinaryContentManipulator.BlobSubmissionOutcome;
import org.opendatakit.common.ermodel.BlobEntitySet;
import org.opendatakit.common.persistence.PersistenceUtils;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKEntityNotFoundException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

/**
 * Implementation of file management APIs for row-level attachments.
 * 
 * 
 * @author mitchellsundt@gmail.com
 *
 */
public class InstanceFileManager {

  public interface FetchBlobHandler {
    byte[] getBlob() throws ODKDatastoreException;
  }

  public interface FileContentHandler {
    void processFileContent(FileContentInfo content, FetchBlobHandler fetcher);
  };

  private static final String ERROR_FILE_VERSION_DIFFERS = "File on server does not match file being uploaded. Aborting upload. ";

  private String appId;

  private CallingContext cc;

  public InstanceFileManager(String appId, CallingContext cc) {
    this.appId = appId;
    this.cc = cc;
  }

  /**
   * Retrieve the content info for a given file. Access to the blob entity set
   * needs to be guarded by a task lock.
   * 
   * @param tableId
   * @param rowId
   * @param partialPath
   * @param userPermissions
   * @return the file content if found; otherwise returns null
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   */
  public FileContentInfo getFile(String tableId, String rowId, String partialPath,
      TablesUserPermissions userPermissions)
      throws ODKDatastoreException, ODKTaskLockException, PermissionDeniedException {

    try {
      if (tableId == null) {
        throw new IllegalArgumentException("tableId cannot be null!");
      }

      if (rowId == null) {
        throw new IllegalArgumentException("rowId cannot be null!");
      }

      if (partialPath == null) {
        throw new IllegalArgumentException("partialPath cannot be null!");
      }

      userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

      OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId, rowId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.LONG, cc);
      try {
        propsLock.acquire();

        DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
        BlobEntitySet instance = blobStore.getBlobEntitySet(rowId, cc);

        int count = instance.getAttachmentCount(cc);
        for (int i = 1; i <= count; ++i) {
          String path = instance.getUnrootedFilename(i, cc);
          if (path != null && path.equals(partialPath)) {
            byte[] fileBlob = instance.getBlob(i, cc);
            String contentType = instance.getContentType(i, cc);
            String contentHash = instance.getContentHash(i, cc);
            Long contentLength = instance.getContentLength(i, cc);

            // And now prepare everything to be returned to the caller.
            if (fileBlob != null && contentType != null && contentLength != null
                && contentLength != 0L) {

              FileContentInfo fo = new FileContentInfo(path, contentType, contentLength,
                  contentHash, fileBlob);
              return fo;
            } else {
              return null;
            }
          }
        }
        return null;

      } finally {
        propsLock.release();
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      throw e;
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw e;
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Save a given file content under this tableId and rowId. Manipulations of
   * the blob entity set (which is being updated) needs to be guarded by a task
   * lock.
   * 
   * @param tableId
   * @param rowId
   * @param fi
   * @param userPermissions
   * @return
   * @throws ODKDatastoreException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   */
  public InstanceFileChangeDetail putFile(String tableId, String rowId, FileContentInfo fi,
      TablesUserPermissions userPermissions)
      throws ODKDatastoreException, ODKTaskLockException, PermissionDeniedException {

    try {
      if (tableId == null) {
        throw new IllegalArgumentException("tableId cannot be null!");
      }

      if (rowId == null) {
        throw new IllegalArgumentException("rowId cannot be null!");
      }

      if (fi.partialPath == null) {
        throw new IllegalArgumentException("partialPath cannot be null!");
      }

      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_ROW);

      OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId, rowId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.LONG, cc);
      try {
        propsLock.acquire();

        // we are adding a file -- delete any cached ETag value for this row's
        // attachments manifest
        try {
          DbTableInstanceManifestETagEntity entity = DbTableInstanceManifestETags
              .getRowIdEntry(tableId, rowId, cc);
          entity.delete(cc);
        } catch (ODKEntityNotFoundException e) {
          // ignore...
        }

        DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
        BlobEntitySet instance = blobStore.newBlobEntitySet(rowId, cc);
        int count = instance.getAttachmentCount(cc);
        for (int i = 1; i <= count; ++i) {
          String path = instance.getUnrootedFilename(i, cc);
          if (path != null && path.equals(fi.partialPath)) {
            // we already have this in our store -- check that it is identical.
            // if not, we have a problem!!!
            if (fi.contentHash.equals(instance.getContentHash(i, cc))) {
              return InstanceFileChangeDetail.FILE_PRESENT;
            } else {
              return InstanceFileChangeDetail.FILE_INCOMPATIBLE;
            }
          }
        }
        BlobSubmissionOutcome outcome = instance.addBlob(fi.fileBlob, fi.contentType,
            fi.partialPath, false, cc);

        switch (outcome) {
        case FILE_UNCHANGED:
          return InstanceFileChangeDetail.FILE_PRESENT;
        case NEW_FILE_VERSION:
          return InstanceFileChangeDetail.FILE_INCOMPATIBLE;
        case COMPLETELY_NEW_FILE:
          return InstanceFileChangeDetail.FILE_PRESENT;
        default:
          throw new IllegalStateException("Unexpected extra status for BlobSubmissionOutcome");
        }

      } finally {
        propsLock.release();
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      throw e;
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw e;
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw e;
    }
  }

  /**
   * Retrieve the BlobEntitySet for a given tableId and rowId. This should be
   * treated as read-only by the caller. Updates should be done through the
   * putFile or postFiles APIs. Manipulations of the blob entity set (which is
   * being updated) needs to be guarded by a task lock.
   * 
   * It is safe to fetch the Blob from this set because the set is write-only.
   * 
   * @param tableId
   * @param rowId
   * @param cb
   *          -- callback to process each file manifest entry
   * @param userPermissions
   * @return map of partial path of file to the FileContentInfo for that file.
   * @throws IOException
   * @throws ODKTaskLockException
   * @throws PermissionDeniedException
   * @throws ODKDatastoreException
   */
  public void getInstanceAttachments(String tableId, String rowId, FileContentHandler cb,
      TablesUserPermissions userPermissions)
      throws IOException, ODKTaskLockException, PermissionDeniedException, ODKDatastoreException {

    try {
      if (tableId == null) {
        throw new IllegalArgumentException("tableId cannot be null!");
      }

      if (rowId == null) {
        throw new IllegalArgumentException("rowId cannot be null!");
      }

      userPermissions.checkPermission(appId, tableId, TablePermission.READ_ROW);

      OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId, rowId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.LONG, cc);

      try {
        propsLock.acquire();

        DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
        final BlobEntitySet instance = blobStore.getBlobEntitySet(rowId, cc);

        int count = instance.getAttachmentCount(cc);
        for (int i = 1; i <= count; ++i) {
          final int iSafe = i;
          FileContentInfo info = new FileContentInfo(instance.getUnrootedFilename(i, cc),
              instance.getContentType(i, cc), instance.getContentLength(i, cc),
              instance.getContentHash(i, cc), null);

          cb.processFileContent(info, new FetchBlobHandler() {
            @Override
            public byte[] getBlob() throws ODKDatastoreException {
              return instance.getBlob(iSafe, cc);
            }
          });
        }
      } finally {
        propsLock.release();
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      throw e;
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw e;
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw e;
    }
  }

  public void postFiles(String tableId, String rowId, InMultiPart inMP,
      TablesUserPermissions userPermissions)
      throws IOException, ODKTaskLockException, ODKTablesException, ODKDatastoreException {

    try {
      if (tableId == null) {
        throw new IllegalArgumentException("tableId cannot be null!");
      }

      if (rowId == null) {
        throw new IllegalArgumentException("rowId cannot be null!");
      }

      userPermissions.checkPermission(appId, tableId, TablePermission.WRITE_ROW);

      OdkTablesLockTemplate propsLock = new OdkTablesLockTemplate(tableId, rowId,
          ODKTablesTaskLockType.TABLES_NON_PERMISSIONS_CHANGES, OdkTablesLockTemplate.DelayStrategy.LONG, cc);

      try {
        propsLock.acquire();

        // fetch these once and then continue to re-use them.
        DbTableInstanceFiles blobStore = new DbTableInstanceFiles(tableId, cc);
        BlobEntitySet instance = blobStore.newBlobEntitySet(rowId, cc);

        ODKTablesException e = null;
        // Parse the request
        while (inMP.hasNext()) {
          InPart part = inMP.next();
          MultivaluedMap<String, String> headers = part.getHeaders();
          String disposition = (headers != null) ? headers.getFirst("Content-Disposition") : null;
          if (disposition == null) {
            e = new ODKTablesException(InstanceFileService.ERROR_MSG_MULTIPART_FILES_ONLY_EXPECTED);
            continue;
          }
          String partialPath = null;
          {
            HeaderValueParser parser = new BasicHeaderValueParser();
            HeaderElement[] values = BasicHeaderValueParser.parseElements(disposition, parser);
            for (HeaderElement v : values) {
              if (v.getName().equalsIgnoreCase("file")) {
                partialPath = v.getParameterByName("filename").getValue();
                break;
              }
            }
          }
          if (partialPath == null) {
            e = new ODKTablesException(
                InstanceFileService.ERROR_MSG_MULTIPART_CONTENT_FILENAME_EXPECTED);
            continue;
          }

          String contentType = (headers != null) ? headers.getFirst("Content-Type") : null;

          ByteArrayOutputStream bo = new ByteArrayOutputStream();
          InputStream bi = null;
          try {
            bi = new BufferedInputStream(part.getInputStream());
            int length = 1024;
            // Transfer bytes from in to out
            byte[] data = new byte[length];
            int len;
            while ((len = bi.read(data, 0, length)) >= 0) {
              if (len != 0) {
                bo.write(data, 0, len);
              }
            }
          } finally {
            bi.close();
          }
          byte[] content = bo.toByteArray();
          String md5Hash = PersistenceUtils.newMD5HashUri(content);

          // we are adding one or more files -- delete any cached ETag value for
          // this row's attachments manifest
          try {
            DbTableInstanceManifestETagEntity entity = DbTableInstanceManifestETags
                .getRowIdEntry(tableId, rowId, cc);
            entity.delete(cc);
          } catch (ODKEntityNotFoundException ex) {
            // ignore... it might already be deleted or have never existed
          }

          int count = instance.getAttachmentCount(cc);
          boolean found = false;
          for (int i = 1; i <= count; ++i) {
            String path = instance.getUnrootedFilename(i, cc);
            if (path != null && path.equals(partialPath)) {
              // we already have this in our store -- check that it is
              // identical.
              // if not, we have a problem!!!
              if (md5Hash.equals(instance.getContentHash(i, cc))) {
                // no-op
                found = true;
              } else {
                // this is an error case; indicated by setting exception
                found = true;
                e = new InstanceFileModificationException(
                    ERROR_FILE_VERSION_DIFFERS + "\n" + partialPath);
                break;
              }
            }
          }
          if (!found) {
            BlobSubmissionOutcome outcome = instance.addBlob(content, contentType, partialPath,
                false, cc);
            if (outcome == BlobSubmissionOutcome.NEW_FILE_VERSION) {
              e = new InstanceFileModificationException(
                  ERROR_FILE_VERSION_DIFFERS + "\n" + partialPath);
            }
          }
        }
        if (e != null) {
          throw e;
        }
      } finally {
        propsLock.release();
      }
    } catch (NullPointerException e) {
      e.printStackTrace();
      throw e;
    } catch (IllegalArgumentException e) {
      e.printStackTrace();
      throw e;
    } catch (IndexOutOfBoundsException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKTaskLockException e) {
      e.printStackTrace();
      throw e;
    } catch (PermissionDeniedException e) {
      e.printStackTrace();
      throw e;
    } catch (ODKDatastoreException e) {
      e.printStackTrace();
      throw e;
    }
  }
}
