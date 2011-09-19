package org.opendatakit.aggregate.server;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.opendatakit.aggregate.ContextFactory;
import org.opendatakit.aggregate.client.preferences.OdkTablesAdmin;
import org.opendatakit.aggregate.client.preferences.OdkTablesAdminService;
import org.opendatakit.aggregate.odktables.client.entity.User;
import org.opendatakit.aggregate.odktables.client.exception.CannotDeleteException;
import org.opendatakit.aggregate.odktables.client.exception.UserAlreadyExistsException;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.commandlogic.common.CreateUserLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.DeleteUserLogic;
import org.opendatakit.aggregate.odktables.commandlogic.common.SetUserManagementPermissionsLogic;
import org.opendatakit.aggregate.odktables.entity.InternalPermission;
import org.opendatakit.aggregate.odktables.entity.InternalUser;
import org.opendatakit.aggregate.odktables.relation.Permissions;
import org.opendatakit.aggregate.odktables.relation.TableEntries;
import org.opendatakit.aggregate.odktables.relation.Users;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.security.client.exception.AccessDeniedException;
import org.opendatakit.common.web.CallingContext;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;

public class OdkTablesAdminServiceImpl extends RemoteServiceServlet implements
        OdkTablesAdminService
{

    /**
   * 
   */
    private static final long serialVersionUID = -2602832816355702415L;

    @Override
    public OdkTablesAdmin[] listAdmin() throws AccessDeniedException
    {
        CallingContext cc = getCC();

        try
        {
            Users users = Users.getInstance(cc);
            Permissions permissions = Permissions.getInstance(cc);

            List<InternalPermission> perms = permissions
                    .query("[odktables]OdkTablesAdminServiceImpl.listAdmin")
                    .equal(Permissions.AGGREGATE_TABLE_IDENTIFIER,
                            users.getAggregateIdentifier())
                    .equal(Permissions.WRITE, true).execute();

            List<OdkTablesAdmin> admins = new ArrayList<OdkTablesAdmin>();
            for (InternalPermission perm : perms)
            {
                InternalUser adminUser = users.getEntity(perm
                        .getAggregateUserIdentifier());
                OdkTablesAdmin admin = new OdkTablesAdmin(
                        adminUser.getAggregateIdentifier(),
                        adminUser.getName(), adminUser.getID());
                admins.add(admin);
            }

            return admins.toArray(new OdkTablesAdmin[] {});
        } catch (ODKDatastoreException e)
        {
            // TODO: display dialog?
        }
        return new OdkTablesAdmin[] {};
    }

    @Override
    public Boolean deleteAdmin(String aggregateUid)
            throws AccessDeniedException
    {
        CallingContext cc = getCC();
        boolean success = false;

        try
        {
            DeleteUserLogic.deleteUser(Users.getInstance(cc),
                    Permissions.getInstance(cc), TableEntries.getInstance(cc),
                    aggregateUid);

            success = true;
        } catch (UserDoesNotExistException e)
        {
            // should not happen since we shouldn't have listed this user in listAdmin()
        } catch (CannotDeleteException e)
        {
            // this is possible since the user may still own some tables
            // TODO: display dialog about this?
        } catch (ODKDatastoreException e)
        {
            // TODO: display dialog?
        }

        return success;
    }

    @Override
    public Boolean addAdmin(OdkTablesAdmin admin) throws AccessDeniedException
    {
        CallingContext cc = getCC();
        boolean createUserSuccess = false;
        boolean setPermsSuccess = false;

        try
        {
            // Create user
            String aggregateUserIdentifier = null;

            try
            {
                User user = CreateUserLogic.createUser(Users.getInstance(cc),
                        admin.getExternalUid(), admin.getName());
                aggregateUserIdentifier = user.getAggregateUserIdentifier();
                if (user.getUserID().equals(admin.getExternalUid()))
                    createUserSuccess = true;
            } catch (UserAlreadyExistsException e)
            {
                // Could happen
                // TODO: should we display a dialog to user?
            }

            // Give user admin permissions
            try
            {
                SetUserManagementPermissionsLogic.setUserManagementPermissions(
                        Users.getInstance(cc), Permissions.getInstance(cc),
                        aggregateUserIdentifier, true);
                setPermsSuccess = true;
            } catch (UserDoesNotExistException e)
            {
                // Should not happen since we just created this user
            }
        } catch (ODKDatastoreException e)
        {
            // TODO: display dialog? 
        }

        return createUserSuccess && setPermsSuccess;
    }

    @Override
    public Boolean updateAdmin(OdkTablesAdmin admin)
            throws AccessDeniedException
    {
        CallingContext cc = getCC();
        boolean success = false;

        try
        {
            InternalUser user = Users.getInstance(cc).getEntity(
                    admin.getAggregateUid());
            user.setID(admin.getExternalUid());
            user.setName(admin.getName());
            user.save();
            success = true;
        } catch (ODKDatastoreException e)
        {

        }
        return success;
    }

    private CallingContext getCC()
    {
        HttpServletRequest req = super.getThreadLocalRequest();
        return ContextFactory.getCallingContext(this, req);
    }

}
