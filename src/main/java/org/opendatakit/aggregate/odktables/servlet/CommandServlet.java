package org.opendatakit.aggregate.odktables.servlet;

import java.io.IOException;
import java.io.InputStreamReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.odktables.command.Command;
import org.opendatakit.aggregate.odktables.command.CommandConverter;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.CommandResult;
import org.opendatakit.aggregate.servlet.ServletUtilBase;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.persistence.exception.ODKTaskLockException;
import org.opendatakit.common.web.CallingContext;

import com.google.gson.JsonParseException;

public class CommandServlet extends ServletUtilBase
{
    /**
     * Serial number for serialization.
     */
    private static final long serialVersionUID = -7810505933356321858L;

    // TODO: should I have GET and POST or just POST?
    //    @Override
    //    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
    //            throws ServletException, IOException
    //    {
    //        // TODO: check if query, i.e. something that should be a GET
    //        executeCommandAndReturnResponse(req, resp);
    //    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException
    {
        // TODO: check if modifies datastore, i.e. something that should be a POST
        executeCommandAndReturnResponse(req, resp);
    }

    private void executeCommandAndReturnResponse(HttpServletRequest req,
            HttpServletResponse resp) throws IOException
    {
        CallingContext cc = ContextFactory.getCallingContext(this, req);
        InputStreamReader reader = new InputStreamReader(req.getInputStream());
        try
        {
            String methodName = req.getRequestURI();
            req.getContextPath();

            Class<? extends Command> commandClass = CommandConverter
                    .getInstance().getCommandClass(methodName);

            if (commandClass == null)
            {
                resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                        "No method by the name of: " + methodName);
                return;
            }

            Command command = CommandConverter.getInstance()
                    .deserializeCommand(reader, commandClass);

            CommandLogic<?> commandLogic = CommandLogic.newInstance(command);

            CommandResult<?> result = commandLogic.execute(cc);

            if (result.successful())
            {
                resp.setStatus(HttpServletResponse.SC_OK);
            } else
            {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            resp.getWriter().println(
                    CommandConverter.getInstance().serializeResult(result));
            resp.flushBuffer();
            return;
        } catch (JsonParseException e)
        {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Malformed json syntax: " + e.getMessage());
            return;
        } catch (ODKDatastoreException e)
        {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not complete request. Please try again later.");
            return;
        } catch (ODKTaskLockException e)
        {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Could not complete the request. Please try again later");
        }
    }
}
