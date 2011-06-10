package org.opendatakit.aggregate.odktables.relation;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.opendatakit.common.ermodel.Entity;
import org.opendatakit.common.persistence.exception.ODKDatastoreException;
import org.opendatakit.common.web.CallingContext;
import org.opendatakit.common.web.TestContextFactory;

public class UsersTest
{

    private Users users;
    private CallingContext cc;
    private String userId;
    private String userName;

    @Before
    public void setUp() throws ODKDatastoreException
    {
        cc = TestContextFactory.getCallingContext();
        users = Users.getInstance(cc);

        userId = "USER1";
        userName = "James Jones";
    }

    @After
    public void tearDown() throws ODKDatastoreException
    {
        users.dropRelation(cc);
    }

    @Test
    public void testCreateUser() throws ODKDatastoreException
    {
        users.createUser(userId, userName);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testCreateUserBadUserId() throws ODKDatastoreException
    {
        users.createUser("baduserId", userName);
    }

    @Test(expected = RuntimeException.class)
    public void testCantCreateSameUserTwice() throws ODKDatastoreException
    {
        users.createUser(userId, userName);
        users.createUser(userId, userName);
    }

    @Test
    public void testCanCreateUserDiffIdSameName() throws ODKDatastoreException
    {
        users.createUser(userId + "1", userName);
        users.createUser(userId + "2", userName);
    }

    @Test
    public void testUserExists() throws ODKDatastoreException
    {
        users.createUser(userId, userName);
        assertTrue(users.userExists(userId));
    }

    @Test
    public void testGetEntity() throws ODKDatastoreException
    {
        users.createUser(userId, userName);
        Entity entity = users.getEntity(userId);
        assertEquals(userId, entity.getField(Users.USER_ID));
        assertEquals(userName, entity.getField(Users.USER_NAME));
    }

    @Test
    public void testDeleteUser() throws ODKDatastoreException
    {
        users.createUser(userId, userName);
        users.deleteUser(userId);
        assertFalse(users.userExists(userId));
    }

    @Test
    public void testCanDeleteUserWhoDoesntExist() throws ODKDatastoreException
    {
        assertFalse(users.userExists(userId));
        users.deleteUser(userId);
        assertFalse(users.userExists(userId));
    }

}
