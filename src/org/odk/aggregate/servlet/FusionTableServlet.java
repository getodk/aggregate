package org.odk.aggregate.servlet;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.odk.aggregate.constants.ErrorConsts;
import org.odk.aggregate.constants.ServletConsts;


public class FusionTableServlet extends ServletUtilBase {
    private static final int DELAY = 6000;
    /**
     * Serial number for serialization
     */
    // TODO: replace this
    // private static final long serialVersionUID = 456146061385437109L;
    private static final long serialVersionUID = 109713025906710021L;
    /**
     * URI from base
     */
    public static final String ADDR = "fusiontables";


    /**
     * Handler for HTTP Get request to create a google spreadsheet
     * 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // verify user is logged in
        if (!verifyCredentials(req, resp)) {
            return;
        }

        // TODO: rename params so not spreadsheet

        // get parameter
        // really the table id
        String spreadsheetName = getParameter(req, ServletConsts.SPREADSHEET_NAME_PARAM);
        String odkFormKey = getParameter(req, ServletConsts.ODK_FORM_KEY);
        String esTypeString = getParameter(req, ServletConsts.EXTERNAL_SERVICE_TYPE);

        if (spreadsheetName == null || odkFormKey == null || esTypeString == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, ErrorConsts.MISSING_FORM_INFO);
            return;
        }

    }

}
