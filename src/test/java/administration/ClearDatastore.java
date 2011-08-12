package administration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.tools.remoteapi.RemoteApiInstaller;
import com.google.appengine.tools.remoteapi.RemoteApiOptions;

/**
 * Clears the datastore of all entities in all kinds. Note that due to kindless
 * queries not being supported on the dev server this will not work on the dev
 * server. See com.google.appengine.api.datastore.Query() for details.
 * 
 * As far as I can tell you have to re-deploy the app after you run this.
 * 
 * To make the compile errors go away add a reference to
 * src/main/libs/appengine-remote-api.jar. This could probably be added to the
 * pom.xml eventually, but I didn't want to break everyone.
 * 
 * @author the.dylan.price@gmail.com
 * 
 */
public class ClearDatastore
{
    // set these in the code if you will be running this a lot
    private static String username = null;
    private static String password = null;
    private static String appname = null;

    public static void main(String[] args) throws IOException
    {
        Scanner scanner = new Scanner(System.in);
        if (username == null)
        {
            System.out.print("username: ");
            username = scanner.nextLine();
        }
        if (password == null)
        {
            System.out.print("password: ");
            password = scanner.nextLine();
        }
        if (appname == null)
        {
            System.out.print("appname: ");
            appname = scanner.nextLine();
        }
        scanner.close();

        clearDatastore(username, password, appname);
    }

    public static void clearDatastore(String username, String password,
            String appname) throws IOException
    {
        RemoteApiOptions options = new RemoteApiOptions().server(
                appname + ".appspot.com", 443).credentials(username, password);

        RemoteApiInstaller installer = new RemoteApiInstaller();
        installer.install(options);

        try
        {
            DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
            boolean keepDeleting = true;
            int offset = 0;
            while (keepDeleting)
            {
                Query query = new Query();
                query.setKeysOnly();
                PreparedQuery preparedQuery = ds.prepare(query);
                List<Entity> entities = preparedQuery
                        .asList(FetchOptions.Builder.withLimit(500).offset(
                                offset));

                if (entities.isEmpty())
                {
                    keepDeleting = false;
                } else
                {
                    List<Key> keys = new ArrayList<Key>();
                    for (Entity entity : entities)
                    {
                        String kind = entity.getKind();
                        if (kind.startsWith("__"))
                        {
                            offset++;
                        } else
                        {
                            keys.add(entity.getKey());
                        }
                    }
                    try
                    {
                        if (!keys.isEmpty())
                            ds.delete(keys);
                    } catch (IllegalArgumentException e)
                    {
                        System.out.println(e.getMessage());
                    }
                }
            }
        } finally
        {
            installer.uninstall();
            System.out.println("Datastore cleared!");
        }
    }
}
