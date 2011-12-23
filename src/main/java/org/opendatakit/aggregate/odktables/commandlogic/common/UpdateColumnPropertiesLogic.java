package org.opendatakit.aggregate.odktables.commandlogic.common;

import org.opendatakit.aggregate.odktables.client.exception.AggregateInternalErrorException;
import org.opendatakit.aggregate.odktables.command.common.UpdateColumnProperties;
import org.opendatakit.aggregate.odktables.commandlogic.CommandLogic;
import org.opendatakit.aggregate.odktables.commandresult.common.UpdateColumnPropertiesResult;
import org.opendatakit.aggregate.odktables.exception.SnafuException;
import org.opendatakit.common.web.CallingContext;

public class UpdateColumnPropertiesLogic extends CommandLogic<UpdateColumnProperties> {

    private final UpdateColumnProperties updateColumnProperties;

    public UpdateColumnPropertiesLogic(
	    UpdateColumnProperties updateColumnProperties) {
	this.updateColumnProperties = updateColumnProperties;
    }

    @Override
    public UpdateColumnPropertiesResult execute(CallingContext cc)
	    throws AggregateInternalErrorException, SnafuException {
	throw new AggregateInternalErrorException("Not implemented.");
    }

}
