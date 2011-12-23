package org.opendatakit.aggregate.odktables.command;

/**
 * <p>
 * Command is an interface that represents a command that can be run in
 * Aggregate. Implementations of the Command interface are simple, immutable
 * data objects that hold all the necessary information for a certain command.
 * The actual logic of these commands is put in a separate class: a subclass of
 * CommandLogic, and the result of executing a command is put in yet another
 * separate class: a subclass of CommandResult.
 * </p>
 * 
 * <p>
 * The reason for this separation is so that Command objects can easily be
 * shared by Aggregate and its clients without introducing unnecessary
 * dependencies on internal Aggregate classes.
 * </p>
 * 
 * <p>
 * Implementing a new Command is unfortunately fairly involved, so the steps are
 * outlined below:
 * <ol>
 * <li>Come up with a name for your command, figure out what it will do, and
 * figure out all the potential error cases that you will be able to detect.</li>
 * <li>For each error case that you can detect, check if it is in
 * CommandResult.FailureReason, and add it if necessary.</li>
 * <li>Write the Command data object. This is an implementation of this
 * interface and should be a simple, immutable data object with getters for each
 * of it's fields. One special requirement is that in addition to
 * getMethodPath() you should implement a static counterpart called
 * <i>methodPath()</i>. This returns the exact same information as
 * getMethodPath(), it's purpose is to make the method path available statically
 * and to instances of the Command interface. Also, try to implement toString,
 * equals, and hashCode.</li>
 * <li>Write the CommandResult object. This is a subclass of CommandResult,
 * please read the documentation in that class for details on subclassing it.</li>
 * <li>Write the CommandLogic object. This is a subclass of the CommandLogic
 * abstract class, please read documentation in CommandLogic for details on
 * subclassing it.</li>
 * <li>In CommandLogic, add a constant to CommandLogic.CommandType for your
 * command. Then add this constant and your command to the
 * CommandLogic.commandClassMap. Finally, in CommandLogic.newInstance, add a
 * case to the switch statement for your command, follow the pattern of all the
 * other cases.</li>
 * <li>In CommandConverter's constructor, add your Command to the commandMap.</li>
 * <li>You're done! When you run an Aggregate instance, your command will be
 * available at /odktables/&lt;whatever your getMethodPath() returns&gt;. The
 * current web.xml maps all /odktables/* requests to the CommandServlet, then
 * from there it gets routed based on the method paths.</li>
 * </ol>
 * </p>
 * 
 * @author the.dylan.price@gmail.com
 */
public interface Command {

    /**
     * @return the path of this Command relative to the address of an Aggregate
     *         instance. For example, if the full path to a command is
     *         http://aggregate.opendatakit.org/odktables/createTable, then this
     *         method would return '/odktables/createTable'.
     */
    public String getMethodPath();
}
