package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.common.UpdateTableProperties;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.common.UpdateTablePropertiesResult;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.common.web.CallingContext;

public class UpdateTablePropertiesLogic extends
	CommandLogic<UpdateTableProperties> {

    private final UpdateTableProperties updateTableProperties;

    public UpdateTablePropertiesLogic(
	    UpdateTableProperties updateTableProperties) {
	this.updateTableProperties = updateTableProperties;
    }

    @Override
    public UpdateTablePropertiesResult execute(CallingContext cc)
	    throws AggregateInternalErrorException, SnafuException {
	throw new AggregateInternalErrorException("Not implemented.");
    }

}
