package org.opendatakit.aggregate.odktables.command.logic;

import org.opendatakit.aggregate.odktables.CommandLogic;
import org.opendatakit.aggregate.odktables.CommandResult.FailureReason;
import org.opendatakit.aggregate.odktables.command.GetUser;
import org.opendatakit.aggregate.odktables.command.result.GetUserResult;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * GetUserUriLogic encapsulates the logic necessary to validate and execute a
 * GetUserUri Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class GetUserLogic extends CommandLogic<GetUser> {

	private GetUser getUserUri;

	public GetUserLogic(GetUser getUserUri) {
		this.getUserUri = getUserUri;
	}

	@Override
	public GetUserResult execute(CallingContext cc)
			throws ODKDatastoreException {
		Users users = Users.getInstance(cc);
		String userId = getUserUri.getUserId();
		String convertedUserId = convertUserId(userId);
		if (!users.userExists(convertedUserId)) {
			return GetUserResult.failure(userId,
					FailureReason.USER_DOES_NOT_EXIST);
		}
		Entity entity = users.getEntity(convertedUserId);
		return GetUserResult.success(userId, entity.getUri(),
				entity.getField(Users.USER_NAME));
	}
}
