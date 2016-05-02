package org.rsbuddy.tabs;

import com.rsbuddy.script.methods.Game;
import com.rsbuddy.script.methods.Keyboard;
import com.rsbuddy.script.methods.Widgets;
import com.rsbuddy.script.task.Task;
import com.rsbuddy.script.wrappers.Component;
import com.rsbuddy.script.wrappers.Widget;
import org.rsbuddy.widgets.Lobby;
import org.rsbuddy.wrappers.Channel;
import org.rsbuddy.wrappers.ChannelUser;
import org.rsbuddy.wrappers.Message;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Friends chat related operations.
 *
 * @author Aion
 */
public class FriendsChat {

	public static final int WIDGET = 1109;
	public static final int COMPONENT_JOIN = 27;
	public static final int COMPONENT_LEAVE = 27;

	/**
	 * @deprecated use {@link ChannelUser#chatRank}
	 */
	@Deprecated
	public enum ChatRank {
		RECRUIT(6226), CORPORAL(6225), SERGEANT(6224),
		LIEUTENANT(6232), CAPTAIN(6233), GENERAL(6231),
		ADMIN(6228), DEPUTY_OWNER(6629), OWNER(6227),
		FRIEND(1004), GUEST(-1);

		private final int textureId;

		private ChatRank(int textureId) {
			this.textureId = textureId;
		}

		/**
		 * Gets the texture id of this rank.
		 *
		 * @return the texture id of this rank
		 */
		public int getTextureId() {
			return textureId;
		}
	}

	/**
	 * Gets the channel the local player is on.
	 *
	 * @return an instance of <code>Channel</code>; otherwise <code>null</code> if unavailable
	 */
	public static Channel getChannel() {
		if (Lobby.isValid()) {
			return Lobby.FriendsChat.getChannel();
		}
		Widget w = getWidget();
		if (w != null) {
			return new Room(w);
		}
		return null;
	}

	/**
	 * Gets the last message said in the friends chat.
	 *
	 * @return the last message; otherwise an empty <code>String</code>
	 * @deprecated use {@link #lastMessage()}
	 */
	@Deprecated
	public static String getLastMessage() {
		Message message = lastMessage();
		return message != null ? message.getMessage() : "";
	}

	/**
	 * Gets the widget representing the friends chat interface.
	 *
	 * @return an instance of <code>Widget</code>; otherwise <code>null</code> if invalid
	 */
	public static Widget getWidget() {
		openTab();
		return Widgets.get(WIDGET);
	}

	/**
	 * Checks whether the local player is on a friends chat channel.
	 *
	 * @return <tt>true</tt> if the user is on a channel; otherwise <tt>false</tt>
	 */
	public static boolean isOnChannel() {
		if (Lobby.isValid()) {
			return Lobby.FriendsChat.isOnChannel();
		}
		Component c = getWidget().getComponent(COMPONENT_JOIN);
		for (String action : c.getActions()) {
			// "Leave chat"
			if (action.equals("Leave chat")) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Joins the given channel. If already on a channel,
	 * {@link #leave()} will be called.
	 *
	 * @param channel the channel to join
	 * @return <tt>true</tt> if the channel was joined; otherwise <tt>false</tt>
	 */
	public static boolean join(String channel) {
		if (channel != null && !channel.isEmpty()) {
			if (Lobby.isValid()) {
				return Lobby.FriendsChat.join(channel);
			} else if (isOnChannel()) {
				if (!leave()) {
					return false;
				}
			}
			Component c = getWidget().getComponent(COMPONENT_JOIN);
			if (c != null) {
				c.click();
				Task.sleep(300, 550);
				Keyboard.sendText(channel, true);
				Task.sleep(1550, 2100);
				return isOnChannel();
			}
		}
		return false;
	}

	/**
	 * Gets the last message said in the friends chat.
	 *
	 * @return an instance of <code>Message</code>; otherwise <code>null</code> if none
	 */
	public static Message lastMessage() {
		if (Lobby.isValid()) {
			return Lobby.FriendsChat.lastMessage();
		}
		// 137 - 180-279
		// "<col=0000>[</col><col=00f>Z</col><col=0000>] Montyman: </col>Nvm"
		Widget w = Widgets.get(137);
		if (w != null) {
			Component c;
			for (int i = 279; i >= 180; i--) {
				c = w.getComponent(i);
				String text = c.getText();
				if (text.startsWith("<col=")) {
					String sender = text.substring(text.indexOf(">]") + 3, text.indexOf(":"));
					String message = text.substring(text.indexOf(": </col>") + 8);
					return new Message(sender, message);
				}
			}
		}
		return null;
	}

	/**
	 * Leaves the channel.
	 *
	 * @return <tt>true</tt> if not on a channel; otherwise <tt>false</tt>
	 */
	public static boolean leave() {
		if (isOnChannel()) {
			if (Lobby.isValid()) {
				return Lobby.FriendsChat.leave();
			}
			Component c = getWidget().getComponent(COMPONENT_LEAVE);
			return c != null && c.click();
		}
		return false;
	}

	/**
	 * Opens the friends chat tab if not already opened.
	 */
	public static void openTab() {
		if (Lobby.isValid()) {
			Lobby.openTab(Lobby.LobbyTab.FRIENDS_CHAT);
		} else if (Game.getCurrentTab() != Game.TAB_FRIENDS_CHAT) {
			Game.openTab(Game.TAB_FRIENDS_CHAT);
		}
	}

	/**
	 * Sends the given message. A slash will be added
	 * if necessary. This does not check whether the local
	 * player is on a channel.
	 *
	 * @param message the message to send
	 * @see #sendMessage(String, boolean)
	 */
	public static void sendMessage(String message) {
		sendMessage(message, false);
	}

	/**
	 * Sends the given message. A slash will be added
	 * if necessary. This does not check whether the local
	 * player is on a channel.
	 *
	 * @param message the message to send
	 * @param instant <tt>true</tt> to send the message instantly; otherwise <tt>false</tt>
	 */
	public static void sendMessage(String message, boolean instant) {
		if (message != null && !message.isEmpty()) {
			if (!message.startsWith("/")) {
				message = "/" + message;
			}
			if (instant) {
				Keyboard.sendTextInstant(message, true);
			} else {
				Keyboard.sendText(message, true);
			}
		}
	}

	/**
	 * Channel related operations.
	 *
	 * @author Aion
	 */
	public static class Room implements Channel {

		public static final int COMPONENT_LABEL_ROOM_NAME = 1;
		public static final int COMPONENT_LABEL_ROOM_OWNER = 1;
		public static final int COMPONENT_LIST_USERS = 5;
		public static final int COMPONENT_LIST_RANKS = 6;
		public static final int COMPONENT_LIST_WORLDS = 8;

		private Widget widget;

		private Room(Widget widget) {
			if (widget == null) {
				throw new IllegalArgumentException("widget cannot be null");
			}
			this.widget = widget;
		}

		public String getName() {
			String name = widget.getComponent(COMPONENT_LABEL_ROOM_NAME).getText();
			name = name.substring(name.indexOf(62) + 1);
			return name.substring(0, name.indexOf(60));
		}

		public String getOwner() {
			String name = widget.getComponent(COMPONENT_LABEL_ROOM_OWNER).getText();
			return name.substring(name.lastIndexOf(62) + 1);
		}

		public ChannelUser getUser(String... names) {
			if (names != null && names.length > 0) {
				ChannelUser[] users = getUsers(names);
				if (users != null && users.length > 0) {
					return users[0];
				}
			}
			return null;
		}

		public ChannelUser[] getUsers() {
			List<ChannelUser> users = new LinkedList<ChannelUser>();
			Component list = widget.getComponent(COMPONENT_LIST_USERS);
			for (Component c : list.getComponents()) {
				if (c == null) {
					continue;
				}
				String name = c.getText();
				if (name == null || name.isEmpty()) {
					continue;
				} else if (name.contains(".")) {
					String[] actions = c.getActions();
					if (actions == null) {
						continue;
					}
					for (String action : actions) {
						if (action == null) {
							continue;
						}
						if (action.contains("Add") || action.contains("Remove")) {
							name = action.substring(action.indexOf(32, action.indexOf(32) + 1) + 1);
							break;
						}
					}
				}
				int index = c.getChildIndex();
				Component rank = widget.getComponent(COMPONENT_LIST_RANKS);
				rank = rank.getComponent(index);
				Component world = widget.getComponent(COMPONENT_LIST_WORLDS);
				world = world.getComponent(index * 2 + 1);
				users.add(new ChannelUser(name.trim(), rank, world));
			}
			return users.toArray(new ChannelUser[users.size()]);
		}

		public ChannelUser[] getUsers(String... names) {
			if (names != null && names.length > 0) {
				List<ChannelUser> users = new ArrayList<ChannelUser>();
				for (ChannelUser channelUser : getUsers()) {
					for (String name : names) {
						if (channelUser.getName().equals(name)) {
							users.add(channelUser);
						}
					}
				}
				return users.toArray(new ChannelUser[users.size()]);
			}
			return new ChannelUser[0];
		}

		public void update() {
			update(true);
		}

		public void update(boolean openTab) {
			Widget w = openTab ? getWidget() : Widgets.get(WIDGET);
			if (w != null) {
				widget = w;
			}
		}

	}

	public static final int WIDGET_FRIENDS_CHAT = 1109;
	public static final int WIDGET_FRIENDS_CHAT_BUTTON_JOIN = 27;
	public static final int WIDGET_FRIENDS_CHAT_BUTTON_LEAVE = 27;
	public static final int WIDGET_FRIENDS_CHAT_LABEL_NAME = 1;
	public static final int WIDGET_FRIENDS_CHAT_LABEL_OWNER = 1;
	/**
	 * @deprecated use WIDGET_FRIENDS_CHAT_LIST_NAME
	 */
	@Deprecated
	public static final int WIDGET_FRIENDS_CHAT_LIST_USERS = 5;
	public static final int WIDGET_FRIENDS_CHAT_LIST_NAME = 5;
	public static final int WIDGET_FRIENDS_CHAT_LIST_RANK = 6;
	public static final int WIDGET_FRIENDS_CHAT_LIST_WORLD = 8;

	public static final int WIDGET_FRIENDS_CHAT_LOBBY = 589;
	public static final int WIDGET_FRIENDS_CHAT_LOBBY_BUTTON_JOIN = 41;
	public static final int WIDGET_FRIENDS_CHAT_LOBBY_BUTTON_LEAVE = 41;
	public static final int WIDGET_FRIENDS_CHAT_LOBBY_LABEL_NAME = 19;
	public static final int WIDGET_FRIENDS_CHAT_LOBBY_LABEL_OWNER = 20;
	public static final int WIDGET_FRIENDS_CHAT_LOBBY_LIST_NAME = 55;
	public static final int WIDGET_FRIENDS_CHAT_LOBBY_LIST_RANK = 56;
	public static final int WIDGET_FRIENDS_CHAT_LOBBY_LIST_WORLD = 57;
	public static final int WIDGET_FRIENDS_CHAT_LOBBY_LIST_CHAT = 23;

	public static final int WIDGET_LOBBY_PROMPT = 589;
	public static final int WIDGET_LOBBY_PROMPT_LABEL_WINDOW_TITLE = 149;
	public static final int WIDGET_LOBBY_PROMPT_TEXT_INPUT = 151;
	public static final int WIDGET_LOBBY_PROMPT_BUTTON_OK = 159;
	public static final int WIDGET_LOBBY_PROMPT_BUTTON_CANCEL = 161;


	/**
	 * Gets the name of the channel.
	 *
	 * @return the name of the channel or an empty <code>String</code>
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getName()}
	 */
	@Deprecated
	public static String getChannelName() {
		Channel c = getChannel();
		return c != null ? c.getName() : "";
	}

	/**
	 * Gets the owner of the channel.
	 *
	 * @return the owner of the channel or an empty <code>String</code>
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getOwner()}
	 */
	@Deprecated
	public static String getChannelOwner() {
		Channel c = getChannel();
		return c != null ? c.getOwner() : "";
	}

	/**
	 * Gets the first user matching with any of the provided names.
	 *
	 * @param names the names to look for
	 * @return an instance of <code>ChannelUser</code>; otherwise <code>null</code> if no results
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getUser(String...)}
	 */
	@Deprecated
	public static User getChannelUser(String... names) {
		User[] users = getChannelUsers(names);
		return users != null ? users[0] : null;
	}

	/**
	 * Gets the users on the channel.
	 *
	 * @return an array instance of <code>ChannelUser</code>
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getUsers()}
	 */
	@Deprecated
	public static User[] getChannelUsers() {
		Channel c = getChannel();
		if (c != null) {
			ChannelUser[] channelUsers = c.getUsers();
			List<User> users = new LinkedList<User>();
			for (ChannelUser user : channelUsers) {
				users.add((User) user);
			}
			return users.toArray(new User[users.size()]);
		}
		return null;
	}

	/**
	 * Gets all the users matching with any of the provided names.
	 *
	 * @param names the names to look for
	 * @return an array instance of <code>ChannelUser</code>
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getUsers(String...)}
	 */
	@Deprecated
	public static User[] getChannelUsers(String... names) {
		User[] users = getChannelUsers();
		if (users != null) {
			List<User> list = new ArrayList<User>();
			for (User user : users) {
				for (String s : names) {
					if (user.getName().equals(s)) {
						list.add(user);
					}
				}
			}
			return list.toArray(new User[list.size()]);
		}
		return null;
	}

	/**
	 * Gets the name of the channel.
	 *
	 * @return the name of the channel or an empty <code>String</code>
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getName()}
	 */
	@Deprecated
	public static String getName() {
		return getChannelName();
	}

	/**
	 * Gets the owner of the channel.
	 *
	 * @return the owner of the channel or an empty <code>String</code>
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getOwner()}
	 */
	@Deprecated
	public static String getOwner() {
		return getChannelOwner();
	}

	/**
	 * Gets the first user matching with any of the provided names.
	 *
	 * @param names the names to look for
	 * @return an instance of <code>ChannelUser</code>; otherwise <code>null</code> if no results
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getUser(String...)}
	 */
	@Deprecated
	public static User getUser(String... names) {
		return getChannelUser(names);
	}

	/**
	 * Gets the users on the channel.
	 *
	 * @return an array instance of <code>ChannelUser</code>
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getUsers()}
	 */
	@Deprecated
	public static User[] getUsers() {
		return getChannelUsers();
	}

	/**
	 * Gets all the users matching with any of the provided names.
	 *
	 * @param names the names to look for
	 * @return an array instance of <code>ChannelUser</code>
	 * @deprecated use {@link org.rsbuddy.wrappers.Channel#getUsers(String...)}
	 */
	@Deprecated
	public static User[] getUsers(String... names) {
		return getChannelUsers(names);
	}

	/**
	 * Checks whether a specified user is on the current channel.
	 *
	 * @param user the user to look for
	 * @return <tt>true</tt> if the user is on the current channel; otherwise <tt>false</tt>
	 * @deprecated will be removed
	 */
	@Deprecated
	public static boolean isOnChannel(String user) {
		return isOnChannel() && getUser(user) != null;
	}

	@Deprecated
	public static class User extends ChannelUser {

		public User(String name, Component rank, Component world) {
			super(name, rank, world);
		}

	}

	/**
	 * Friends list related operations. Does not yet handle the lobby.
	 *
	 * @author Aion
	 */
	public static class FriendsList {

		public static final int WIDGET_FRIENDSLIST = 550;
		public static final int WIDGET_FRIENDSLIST_BUTTON_ADD_FRIEND = 23;
		public static final int WIDGET_FRIENDSLIST_BUTTON_REMOVE_FRIEND = 28;
		public static final int WIDGET_FRIENDSLIST_LABEL_FRIENDS_COUNT = 18;
		public static final int WIDGET_FRIENDSLIST_LIST_FRIENDS = 6;

		public static interface User {
			/**
			 * Gets the name of this user.
			 *
			 * @return the name of this user
			 */
			public String getName();

			/**
			 * Gets the world number that this user is on.
			 *
			 * @return the world number or -1 if unavailable
			 */
			public int getWorld();

			/**
			 * Checks whether this user is in lobby.
			 *
			 * @return <tt>true</tt> if in lobby; otherwise <tt>false</tt>
			 */
			public boolean isInLobby();

		}

		/**
		 * Adds a friend.
		 *
		 * @param name the name of the friend to add
		 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
		 */
		public static boolean add(String name) {
			if (name != null && !name.isEmpty()) {
				openTab();
				Component c = Widgets.getComponent(WIDGET_FRIENDSLIST, WIDGET_FRIENDSLIST_BUTTON_ADD_FRIEND);
				if (c != null) {
					c.click();
					Task.sleep(300, 550);
					Keyboard.sendText(name, true);
					Task.sleep(600, 800);
					return getFriend(name) != null;
				}
			}
			return false;
		}

		/**
		 * Adds a friend.
		 *
		 * @param user the instance of <code>ChannelUser</code> to add
		 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
		 */
		public static boolean add(User user) {
			if (user != null) {
				Friend friend = getFriend(user.getName());
				if (friend == null) {
					return add(user.getName());
				}
			}
			return false;
		}

		/**
		 * Gets the count of this end-user's friends.
		 *
		 * @return the count of this end-user's friends
		 */
		public static int getCount() {
			openTab();
			Component c = Widgets.getComponent(WIDGET_FRIENDSLIST, WIDGET_FRIENDSLIST_LABEL_FRIENDS_COUNT);
			if (c != null) {
				String text = c.getText();
				return Integer.parseInt(text.split(" ")[0]);
			}
			return -1;
		}

		/**
		 * Gets the first friend matching with any of the provided names.
		 *
		 * @param names the names to look for
		 * @return an instance of <code>Friend</code> or <code>null</code> if no results
		 */
		public static Friend getFriend(String... names) {
			Friend[] friends = getFriends();
			for (String name : names) {
				for (Friend friend : friends) {
					if (name.equalsIgnoreCase(friend.getName())) {
						return friend;
					}
				}
			}
			return null;
		}

		/**
		 * Gets the end-user's friends from the friends list.
		 *
		 * @return an array instance of <code>Friend</code>
		 */
		public static Friend[] getFriends() {
			openTab();
			Component list = Widgets.getComponent(WIDGET_FRIENDSLIST, WIDGET_FRIENDSLIST_LIST_FRIENDS);
			if (list != null) {
				java.util.LinkedList<Friend> friends = new java.util.LinkedList<Friend>();
				for (Component c : list.getComponents()) {
					if (c == null) {
						continue;
					}
					String name = c.getItemName();
					name = name.substring(name.indexOf(62) + 1);
					Component world = Widgets.getComponent(WIDGET_FRIENDSLIST, 5);
					world = world.getComponent(c.getIndex());
					friends.add(new Friend(name, world));
				}
				return friends.toArray(new Friend[friends.size()]);
			}
			return new Friend[0];
		}

		/**
		 * Gets all the friends matching with any of the provided names.
		 *
		 * @param names the names to look for
		 * @return an array instance of <code>Friend</code>
		 */
		public static Friend[] getFriends(String... names) {
			java.util.LinkedList<Friend> friends = new java.util.LinkedList<Friend>();
			for (String name : names) {
				for (Friend friend : getFriends()) {
					if (name.equalsIgnoreCase(friend.getName())) {
						friends.add(friend);
					}
				}
			}
			return friends.toArray(new Friend[friends.size()]);
		}

		/**
		 * Checks whether the friends list of this end-user is full.
		 *
		 * @return <tt>true</tt> if full; otherwise <tt>false</tt>
		 */
		public static boolean isFull() {
			return getCount() == 200;
		}

		/**
		 * Opens the friends list tab if not already opened.
		 */
		public static void openTab() {
			int tab = Game.TAB_FRIENDS;
			if (Game.getCurrentTab() != tab) {
				Game.openTab(tab);
			}
		}

		/**
		 * Removes a friend.
		 *
		 * @param name the name of the friend to remove
		 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
		 */
		public static boolean remove(String name) {
			if (name != null && getFriend(name) != null) {
				Component c = Widgets.getComponent(WIDGET_FRIENDSLIST, WIDGET_FRIENDSLIST_BUTTON_REMOVE_FRIEND);
				if (c != null) {
					c.click();
					Task.sleep(300, 550);
					Keyboard.sendText(name, true);
					Task.sleep(600, 800);
					return getFriend(name) != null;
				}
			}
			return false;
		}

		/**
		 * Removes a friend.
		 *
		 * @param user the instance of <code>ChannelUser</code> to remove
		 * @return <tt>true</tt> if successful; otherwise <tt>false</tt>
		 */
		public static boolean remove(User user) {
			if (user != null) {
				Friend friend = getFriend(user.getName());
				if (friend != null) {
					return remove(friend.getName());
				}
			}
			return false;
		}

		public static class Friend implements User {

			private String name;
			private int worldNumber;
			private boolean isOffline;
			private boolean isInLobby;

			public Friend(String name, Component world) {
				this.name = name;
				String text = world.getText();
				isOffline = text.contains("Of");
				isInLobby = text.contains("Lo");
				if (!isOffline && !isInLobby && !text.endsWith(".")) {
					worldNumber = Integer.parseInt(text);
				} else {
					worldNumber = -1;
				}
			}

			public String getName() {
				return name;
			}

			public int getWorld() {
				return worldNumber;
			}

			/**
			 * Checks whether this friend is offline.
			 *
			 * @return <tt>true</tt> if offline; otherwise <tt>false</tt>
			 */
			public boolean isOffline() {
				return isOffline;
			}

			public boolean isInLobby() {
				return isInLobby;
			}

			/**
			 * Checks whether this friend is online.
			 *
			 * @return <tt>true</tt> if online; otherwise <tt>false</tt>
			 */
			public boolean isOnline() {
				return !isOffline && !isInLobby;
			}
		}

	}
}

