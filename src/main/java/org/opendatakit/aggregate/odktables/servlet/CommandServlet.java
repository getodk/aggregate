package org.opendatakit.aggregate.odktables.servlet;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.aggregate.odktables.command.CommandConverter;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.common.web.CallingContext;

import com.google.gson.JsonParseException;

public class CommandServlet extends ServletUtilBase {
    private static Log logger = LogFactory.getLog(CommandServlet.class);
    /**
     * Serial number for serialization.
     */
    private static final long serialVersionUID = -7810505933356321858L;

    // TODO: should I have GET and POST or just POST?
    // @Override
    // protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    // throws ServletException, IOException
    // {
    // // TODO: check if query, i.e. something that should be a GET
    // executeCommandAndReturnResponse(req, resp);
    // }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	    throws ServletException, IOException {
	// TODO: check if modifies datastore, i.e. something that should be a
	// POST
	executeCommandAndReturnResponse(req, resp);
    }

    private void executeCommandAndReturnResponse(HttpServletRequest req,
	    HttpServletResponse resp) throws IOException {
	CallingContext cc = ContextFactory.getCallingContext(this, req);
	InputStreamReader reader = new InputStreamReader(req.getInputStream());
	try {
	    String methodName = req.getPathInfo();

	    Class<? extends Command> commandClass = CommandConverter
		    .getInstance().getCommandClass(methodName);

	    if (commandClass == null) {
		resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
			"No method by the name of: " + methodName);
		return;
	    }

	    Command command = CommandConverter.getInstance()
		    .deserializeCommand(reader, commandClass);

	    CommandLogic<?> commandLogic = CommandLogic.newInstance(command);

	    CommandResult<?> result = commandLogic.execute(cc);

	    if (result.successful()) {
		resp.setStatus(HttpServletResponse.SC_OK);
	    } else {
		resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
	    }

	    resp.getWriter().println(
		    CommandConverter.getInstance().serializeResult(result));
	    resp.flushBuffer();
	    return;
	} catch (JsonParseException e) {
	    resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
		    "Malformed json syntax: " + e.getMessage());
	    return;
	} catch (AggregateInternalErrorException e) {
	    logger.warn(e.toString());
	    resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    "Could not complete request. Please try again later.");
	    return;
	} catch (SnafuException e) {
	    logger.error(e.toString());
	    resp.sendError(
		    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
		    "Aggregate suffered an unrecoverable error that likely left the "
			    + "datastore in a corrupted state. Please contact the Aggregate "
			    + "administrator about this issue.");
	}
    }
}
