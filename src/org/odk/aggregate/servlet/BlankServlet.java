package org.odk.aggregate.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.constants.BasicConsts;

public class BlankServlet extends ServletUtilBase {

   /**
    * Serial number for serialization
    */
   private static final long serialVersionUID = -2220857379519391127L;

   /**
    * URI from base
    */
   public static final String ADDR = "blank";

   /**
    * Handler for HTTP Get request to create blank page that is navigable
    * 
    * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
    *      javax.servlet.http.HttpServletResponse)
    */
   @Override
   public void doGet(HttpServletRequest req, HttpServletResponse resp)
         throws IOException {

      // verify user is logged in
      if (!verifyCredentials(req, resp)) {
        return;
      }
      
      beginBasicHtmlResponse(BasicConsts.EMPTY_STRING, resp, req, true); // header info
      finishBasicHtmlResponse(resp);
   }

}
