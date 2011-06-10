package org.opendatakit.aggregate.odktables.command.result;

import java.util.ArrayList;
import java.util.List;

import org.opendatakit.aggregate.odktables.CommandResult;
import org.opendatakit.aggregate.odktables.client.exception.UserDoesNotExistException;
import org.opendatakit.aggregate.odktables.command.GetUser;

/**
 * GetUserUriResult represents the result of the execution of a GetUserUri
 * command.
 * 
 * @author the.dylan.price@gmail.com
 */
public class GetUserResult extends CommandResult<GetUser> {
	private static final List<FailureReason> possibleFailureReasons;
	static {
		possibleFailureReasons = new ArrayList<FailureReason>();
		possibleFailureReasons.add(FailureReason.USER_DOES_NOT_EXIST);
	}

	private final String userId;
	private final String userUri;
	private final String userName;

	/**
	 * For serialization by Gson we need a no-arg constructor
	 */
	private GetUserResult() {
		super(true, null);
		this.userId = null;
		this.userUri = null;
		this.userName = null;
	}

	/**
	 * The success constructor. See {@link #success} for param info.
	 */
	private GetUserResult(String userId, String userUri, String userName) {
		super(true, null);
		if (userId == null || userId.length() == 0)
			throw new IllegalArgumentException("userId '" + userId
					+ "' was null or empty");
		if (userUri == null || userUri.length() == 0)
			throw new IllegalArgumentException("userUri '" + userUri
					+ "' was null or empty");
		if (userName == null || userName.length() == 0)
			throw new IllegalArgumentException("userName '" + userName
					+ "' was null or empty");
		this.userId = userId;
		this.userUri = userUri;
		this.userName = userName;
	}

	/**
	 * The failure constructor. See {@link #failure} for param info.
	 */
	private GetUserResult(String userId, FailureReason reason) {
		super(false, reason);
		if (userId == null || userId.length() == 0)
			throw new IllegalArgumentException("userId '" + userId
					+ "' was null or empty");
		if (!possibleFailureReasons.contains(getReason())) {
			throw new IllegalArgumentException("Not a valid FailureReason: "
					+ getReason());
		}
		this.userId = userId;
		this.userUri = null;
		this.userName = null;
	}

	/**
	 * Retrieves the result of the GetUserUri command.
	 * 
	 * @return the public uri of the user
	 * @throws UserDoesNotExistException
	 *             if the user which the GetUserUri command tried to retrieve
	 *             the uri for does not exist.
	 */
	public String getUserUri() throws UserDoesNotExistException {
		if (successful()) {
			return this.userUri;
		} else {
			switch (getReason()) {
			case USER_DOES_NOT_EXIST:
				throw new UserDoesNotExistException(this.userId);
			default:
				throw new RuntimeException("An unknown error occured.");
			}
		}
	}

	/**
	 * @return the userId associated with the GetUserUri command.
	 */
	public String getUserId() {
		return this.userId;
	}

	/**
	 * @return the human readable name of the user
	 */
	public String getUserName() {
		return this.userName;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("GetUserUriResult [userId=%s, userUri=%s]",
				userId, userUri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((userId == null) ? 0 : userId.hashCode());
		result = prime * result + ((userUri == null) ? 0 : userUri.hashCode());
		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (!(obj instanceof GetUserResult))
			return false;
		GetUserResult other = (GetUserResult) obj;
		if (userId == null) {
			if (other.userId != null)
				return false;
		} else if (!userId.equals(other.userId))
			return false;
		if (userUri == null) {
			if (other.userUri != null)
				return false;
		} else if (!userUri.equals(other.userUri))
			return false;
		return true;
	}

	/**
	 * @param userId
	 *            the unique identifier of the user that was given to the
	 *            GetUserUri command
	 * @param userUri
	 *            the unique, public uri of the user associated with the given
	 *            userId
	 * @param userName
	 *            the human readable name of the user
	 * @return a new GetUserUriResult representing the successful completion of
	 *         a GetUserUri command.
	 */
	public static GetUserResult success(String userId, String userUri,
			String userName) {
		return new GetUserResult(userId, userUri, userName);
	}

	/**
	 * 
	 * @param userId
	 *            the unique identifier of the user that was given to the
	 *            GetUserUri command
	 * @param reason
	 *            the reason the command failed. Currently can only be
	 *            USER_DOES_NOT_EXIST.
	 * @return a new GetUserUriResult representing the failed completion of a
	 *         GetUserUri command.
	 */
	public static GetUserResult failure(String userId, FailureReason reason) {
		return new GetUserResult(userId, reason);
	}
}
