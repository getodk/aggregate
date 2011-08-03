package org.opendatakit.aggregate.odktables.command.logic;

import java.util.List;

import org.opendatakit.aggregate.odktables.CommandLogic;
import org.opendatakit.aggregate.odktables.command.DeleteUser;
import org.opendatakit.aggregate.odktables.command.result.DeleteUserResult;
import org.opendatakit.aggregate.odktables.relation.TableIndex;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.Query.FilterOperation;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;

/**
 * DeleteUserLogic encapsulates the logic necessary to validate and execute a
 * DeleteUser Command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class DeleteUserLogic extends CommandLogic<DeleteUser>
{

    private DeleteUser deleteUser;

    public DeleteUserLogic(DeleteUser deleteUser)
    {
        this.deleteUser = deleteUser;
    }

    @Override
    public DeleteUserResult execute(CallingContext cc)
            throws ODKDatastoreException
    {
        TableIndex index = TableIndex.getInstance(cc);
        Users users = Users.getInstance(cc);
        String userId = deleteUser.getUserId();
        String convertedUserId = convertUserId(userId);

        List<Entity> indexes = index.getEntities(TableIndex.USER_ID,
                FilterOperation.EQUAL, convertedUserId, cc);
        for (Entity table_index : indexes)
        {
            index.deleteTable(table_index.getField(TableIndex.USER_ID),
                    table_index.getField(TableIndex.TABLE_ID));
        }
        users.deleteUser(convertedUserId);

        return DeleteUserResult.success(userId);
    }
}
