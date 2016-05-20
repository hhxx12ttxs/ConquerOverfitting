import java.util.*;

import org.darkstorm.minecraft.darkmod.DarkMod;
import org.darkstorm.minecraft.darkmod.events.*;
import org.darkstorm.minecraft.darkmod.hooks.client.*;
import org.darkstorm.minecraft.darkmod.hooks.client.packets.*;
import org.darkstorm.minecraft.darkmod.mod.Mod;
import org.darkstorm.minecraft.darkmod.mod.commands.*;
import org.darkstorm.minecraft.darkmod.mod.util.Location;
import org.darkstorm.minecraft.darkmod.mod.util.constants.ChatColor;
import org.darkstorm.minecraft.darkmod.tools.*;
import org.darkstorm.tools.events.Event;
import org.darkstorm.tools.settings.*;
import org.lwjgl.input.*;
import org.lwjgl.opengl.GL11;

public class AutoAttackMod extends Mod implements CommandListener {
	private enum AttackMode {
		AUTO,
		NOCHEAT,
		SHORTCUT,
		OFF
	}

	public enum Team {
		ENEMY(ChatColor.RED, "Enemy"),
		ALLY(ChatColor.PINK, "Ally"),
		FACTION(ChatColor.LIME, "Faction"),
		FRIENDLY(null, "Friendly") {
			@Override
			public boolean isOnTeam(String player) {
				return ALLY.isOnTeam(player) || FACTION.isOnTeam(player);
			}
		},
		ALL(null, "?") {
			@Override
			public boolean isOnTeam(String player) {
				return true;
			}
		},
		NONE(null, "X") {
			@Override
			public boolean isOnTeam(String player) {
				return false;
			}
		};

		private final ChatColor color;
		private final String name;

		private Team(ChatColor color, String name) {
			this.color = color;
			this.name = name;
		}

		public ChatColor getColor() {
			return color;
		}

		public String getName() {
			return name;
		}

		public boolean isOnTeam(Humanoid player) {
			return player != null && isOnTeam(player.getName());
		}

		public boolean isOnTeam(String player) {
			return player.startsWith(color.toString());
		}

		public static Team getTeam(String player) {
			if(!player.startsWith("\247"))
				return NONE;
			for(Team team : values())
				if(team.isOnTeam(player))
					return team;
			return NONE;
		}
	}

	private AttackMode attackMode = AttackMode.OFF;
	private Vector<String> friends;
	private Queue<Entity> attackQueue = new ArrayDeque<Entity>();
	private Team team = Team.FRIENDLY;
	private boolean hurtSelf = false, autoSwitch = true;

	private double range = 4.5;

	private int timer = 0;

	@Override
	public void onStart() {
		friends = new Vector<String>();
		loadSettings();
		commandManager.registerListener(new Command("friend", "friend <add|remove|list>", "Modify friends for AutoAttackMod"), this);
		commandManager.registerListener(new Command("attackmode", "attackmode <auto|knohax|shortcut|off|self>", "Controls for AutoAttackMod"), this);
		commandManager.registerListener(new Command("team", "team [blue|b|red|r|all|a]", "Sets attack mode team"), this);
		eventManager.addListener(RenderEvent.class, this);
		eventManager.addListener(TickEvent.class, this);
	}

	@Override
	public void onStop() {
		saveSettings();
		commandManager.unregisterListeners(this);
		eventManager.removeListener(TickEvent.class, this);
		eventManager.removeListener(RenderEvent.class, this);
	}

	@Override
	public ModControl getControlOption() {
		return ModControl.NONE;
	}

	@Override
	public String getName() {
		return "Auto Attack Mod";
	}

	@Override
	public String getShortDescription() {
		return "";
	}

	@Override
	public boolean hasOptions() {
		return false;
	}

	@Override
	public int loop() {
		return 9000;
	}

	@Override
	public void onCommand(String command) {
		String[] parts = command.split(" ");
		if(parts[0].equalsIgnoreCase("friend") && parts.length > 1) {
			synchronized(friends) {
				if(parts[1].equalsIgnoreCase("add") && parts.length == 3) {
					if(!friends.contains(parts[2].toLowerCase())) {
						friends.add(parts[2].toLowerCase());
						displayText(ChatColor.GRAY + "Friend added: " + ChatColor.GOLD + parts[2]);
					} else {
						displayText(ChatColor.GRAY + "Friend already added");
						return;
					}
				} else if(parts[1].equalsIgnoreCase("remove") && parts.length == 3) {
					if(friends.remove(parts[2].toLowerCase()))
						displayText(ChatColor.GRAY + "Friend removed: " + ChatColor.GOLD + parts[2]);
					else {
						displayText(ChatColor.GRAY + "Friend not found");
						return;
					}
				} else if(parts[1].equalsIgnoreCase("list")) {
					String friendsAppended = "";
					if(friends.size() > 0) {
						friendsAppended = friends.get(0);
						for(int i = 1; i < friends.size(); i++)
							friendsAppended += ", " + friends.get(i);
					}
					displayText(ChatColor.GRAY + "Friends: " + ChatColor.GOLD + friendsAppended);
					return;
				} else
					return;
				saveSettings();
			}
		} else if(parts[0].equalsIgnoreCase("attackmode") && parts.length == 2) {
			if(parts[1].equalsIgnoreCase("auto")) {
				attackMode = AttackMode.AUTO;
				displayText(ChatColor.GRAY + "Attack mode set to " + ChatColor.GOLD + "auto");
			} else if(parts[1].equalsIgnoreCase("shortcut")) {
				attackMode = AttackMode.SHORTCUT;
				displayText(ChatColor.GRAY + "Attack mode set to " + ChatColor.GOLD + "shortcut");
			} else if(parts[1].equalsIgnoreCase("nocheat")) {
				attackMode = AttackMode.NOCHEAT;
				displayText(ChatColor.GRAY + "Attack mode set to " + ChatColor.GOLD + "nocheat");
			} else if(parts[1].equalsIgnoreCase("off")) {
				attackMode = AttackMode.OFF;
				displayText(ChatColor.GRAY + "Attack mode set to " + ChatColor.GOLD + "off");
			} else if(parts[1].equalsIgnoreCase("self")) {
				hurtSelf = !hurtSelf;
				displayText(ChatColor.GRAY + "Attacking self is now " + ChatColor.GOLD + (hurtSelf ? "on" : "off"));
			} else
				displayText(ChatColor.GRAY + "Attack mode not recognized");
		} else if(parts[0].equalsIgnoreCase("team")) {
			if(parts.length == 2) {
				for(Team team : Team.values()) {
					if(parts[1].toLowerCase().startsWith(Character.toString(team.getName().toLowerCase().charAt(0)))) {
						this.team = team;
						displayText(ChatColor.GRAY + "Team set to " + (team.getColor() != null ? team.getColor() : "") + team.toString().toLowerCase() + ChatColor.GRAY + ".");
						return;
					}
				}
				displayText(ChatColor.GRAY + "Unknown team!");
			} else {
				team = Team.NONE;
				displayText(ChatColor.GRAY + "Team cleared.");
			}
		}
	}

	@SuppressWarnings("unchecked")
	private Entity getClosest(double maxDistance) {
		Player player = minecraft.getPlayer();
		World world = minecraft.getWorld();
		if(world == null || player == null)
			return null;
		Entity closestEntity = null;
		double closestDistance = 999;
		for(Entity entity : (List<Entity>) world.getEntities()) {
			if(entity.getID() == player.getID() || !(entity instanceof Humanoid))
				continue;
			if(entity instanceof Humanoid) {
				Humanoid otherPlayer = (Humanoid) entity;
				if(isFriend(otherPlayer.getName()) || (team != null && team.isOnTeam(otherPlayer)))
					continue;
			}
			double distance = getDistanceBetween(player, entity);
			if((maxDistance <= 0 || distance <= maxDistance) && distance < closestDistance) {
				closestEntity = entity;
				closestDistance = distance;
			}
		}
		return closestEntity;
	}

	public float getFacingRotationX(Entity entity) {
		Player player = minecraft.getPlayer();
		double d = entity.getX() - player.getX();
		double d1 = entity.getZ() - player.getZ();
		return (float) ((Math.atan2(d1, d) * 180D) / 3.1415927410125732D) - 90F;
	}

	public float getFacingRotationY(Entity entity) {
		Player player = minecraft.getPlayer();
		double dis1 = entity.getY() + 1 - player.getY() + 1;
		double dis2 = Math.sqrt(Math.pow(entity.getX() - player.getX(), 2) + Math.pow(entity.getZ() - player.getZ(), 2));
		return (float) ((Math.atan2(dis2, dis1) * 180D) / 3.1415927410125732D) - 80F - ((float) Math.pow(getDistanceTo(player, entity.getX(), entity.getY(), entity.getZ()) / 4, 2));
	}

	private void loadSettings() {
		synchronized(friends) {
			friends.clear();
			DarkMod darkMod = DarkMod.getInstance();
			SettingsHandler settingsHandler = darkMod.getSettingsHandler();
			SettingVector settings = settingsHandler.getSettings();
			Setting rootSetting = settings.getSetting("AutoAttackMod");
			if(rootSetting != null) {
				SettingVector subSettings = rootSetting.getSubSettings();
				String friends = subSettings.getSettingValue("friends");
				if(friends != null)
					for(String friend : friends.split(","))
						this.friends.add(friend);
			}
		}
	}

	private void saveSettings() {
		synchronized(friends) {
			DarkMod darkMod = DarkMod.getInstance();
			SettingsHandler settingsHandler = darkMod.getSettingsHandler();
			SettingVector settings = settingsHandler.getSettings();
			Setting rootSetting = settings.getSetting("AutoAttackMod");
			if(rootSetting == null) {
				rootSetting = new Setting("AutoAttackMod", "");
				settings.add(rootSetting);
			}
			SettingVector subSettings = rootSetting.getSubSettings();
			Setting friendsSetting = subSettings.getSetting("friends");
			if(friendsSetting == null) {
				friendsSetting = new Setting("friends", "");
				subSettings.add(friendsSetting);
			}
			if(friends.size() > 0) {
				String friendsValue = friends.get(0);
				for(int i = 1; i < friends.size(); i++)
					friendsValue += "," + friends.get(i);
				friendsSetting.setValue(friendsValue);
			}
			settingsHandler.saveSettings();
		}
	}

	public boolean isFriend(String name) {
		synchronized(friends) {
			return friends.contains(ChatColor.removeColors(name).toLowerCase());
		}
	}

	public Team getTeam() {
		return team;
	}

	private void switchSword() {
		Player player = minecraft.getPlayer();
		PlayerController controller = minecraft.getPlayerController();
		MultiplayerWorld world = minecraft.getWorld();
		if(player == null || controller == null || world == null)
			return;
		Inventory inventory = player.getInventory();
		int[] ids = new int[] { 276, 283, 267, 272, 268 };
		loop: for(int i = 0; i < 9; i++) {
			InventoryItem item = inventory.getItemAt(i);
			if(item != null) {
				for(int id : ids) {
					if(item.getID() == id) {
						if(inventory.getSelectedIndex() == i)
							return;
						inventory.setSelectedIndex(i);
						controller.setSelectedItemIndex(i);
						Packet16BlockItemSwitch switchPacket = ReflectionUtil.instantiate(ClassRepository.getClassForInterface(Packet16BlockItemSwitch.class));
						switchPacket.setID(i);
						world.getNetworkHandler().sendPacket(switchPacket);
						break loop;
					}
				}
			}
		}
	}

	@Override
	@SuppressWarnings("unchecked")
	public void onEvent(Event event) {
		if(event instanceof TickEvent) {
			if(timer > 0) {
				timer--;
				return;
			}
			try {
				if(minecraft.getWorld() == null || !(minecraft.getWorld() instanceof MultiplayerWorld))
					return;
				if(attackMode == AttackMode.OFF || (attackMode == AttackMode.SHORTCUT && !Mouse.isButtonDown(2) && !Keyboard.isKeyDown(Keyboard.KEY_TAB)))
					return;
				Player player = minecraft.getPlayer();
				int playerID = player.getID();
				Location playerLocation = new Location(player.getX(), player.getY(), player.getZ());
				MultiplayerWorld world = minecraft.getWorld();
				NetworkHandler networkHandler = world.getNetworkHandler();
				if(attackMode == AttackMode.NOCHEAT) {
					Entity entity = getClosest(4.1);
					if(entity == null)
						return;
					if(autoSwitch)
						switchSword();
					Packet18Animation actionPacket = ReflectionUtil.instantiate(ClassRepository.getClassForInterface(Packet18Animation.class));
					actionPacket.setEntityID(playerID);
					actionPacket.setState(1);
					networkHandler.sendPacket(actionPacket);
					Packet7UseEntity attackPacket = ReflectionUtil.instantiate(ClassRepository.getClassForInterface(Packet7UseEntity.class));
					attackPacket.setEntityID(playerID);
					attackPacket.setTargetEntityID(entity.getID());
					attackPacket.setButton(1);
					networkHandler.sendPacket(attackPacket);
					timer += 4;
					return;
				}
				if(attackQueue.isEmpty()) {
					attackQueue.addAll(world.getEntities());
				}
				while(attackQueue.size() > 0) {
					Entity entity = null;
					label: while(team != Team.ALL && attackQueue.peek() != null) {
						entity = attackQueue.poll();
						Location entityLocation = new Location(entity.getX(), entity.getY(), entity.getZ());
						if(entity.equals(player) || !(entity instanceof Animable) || getDistanceBetween(playerLocation, entityLocation) > range) {
							entity = null;
							continue;
						}
						if(entity instanceof Humanoid) {
							Humanoid playerTarget = (Humanoid) entity;
							String playerName = playerTarget.getName();
							synchronized(friends) {
								for(String friend : friends) {
									if(playerName.equalsIgnoreCase(friend)) {
										entity = null;
										continue label;
									}
								}
							}
							if(team != Team.NONE && playerName.startsWith(team.getColor().toString())) {
								entity = null;
								continue;
							}
						}
						break;
					}
					if(hurtSelf) {
						if(autoSwitch)
							switchSword();
						Packet18Animation actionPacket = ReflectionUtil.instantiate(ClassRepository.getClassForInterface(Packet18Animation.class));
						actionPacket.setEntityID(playerID);
						actionPacket.setState(1);
						networkHandler.sendPacket(actionPacket);
						Packet7UseEntity attackPacket = ReflectionUtil.instantiate(ClassRepository.getClassForInterface(Packet7UseEntity.class));
						attackPacket.setEntityID(playerID);
						attackPacket.setTargetEntityID(playerID);
						attackPacket.setButton(1);
						networkHandler.sendPacket(attackPacket);
						return;
					}
					if(entity == null)
						return;

					if(autoSwitch)
						switchSword();
					int id = entity.getID();
					Packet18Animation actionPacket = ReflectionUtil.instantiate(ClassRepository.getClassForInterface(Packet18Animation.class));
					actionPacket.setEntityID(playerID);
					actionPacket.setState(1);
					networkHandler.sendPacket(actionPacket);
					Packet7UseEntity attackPacket = ReflectionUtil.instantiate(ClassRepository.getClassForInterface(Packet7UseEntity.class));
					attackPacket.setEntityID(playerID);
					attackPacket.setTargetEntityID(id);
					attackPacket.setButton(1);
					networkHandler.sendPacket(attackPacket);
				}
			} catch(Exception exception) {
				exception.printStackTrace();
			}
		} else if(event instanceof RenderEvent) {
			final Player player = minecraft.getPlayer();
			World world = minecraft.getWorld();
			if(player == null || world == null || ((RenderEvent) event).getStatus() != RenderEvent.RENDER_ENTITIES_END)
				return;

			double mX = player.getX();
			double mY = player.getY();
			double mZ = player.getZ();
			EntityTarget target = minecraft.getPlayerTarget();
			if(target == null)
				return;
			if(target.getEntity() == null) {
				double X = target.getTargetX();
				double Y = target.getTargetY();
				double Z = target.getTargetZ();
				double dX = (mX - X);
				double dY = (mY - Y);
				double dZ = (mZ - Z);

				GL11.glPushMatrix();
				GL11.glColor4f(1f, 0f, 0f, 1f);
				GL11.glLineWidth(1.7f);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glDisable(GL11.GL_DEPTH_TEST);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glEnable(GL11.GL_LINE_SMOOTH);
				GL11.glBegin(GL11.GL_LINE_LOOP);
				GL11.glVertex3d(-dX, -dY, -dZ);
				GL11.glVertex3d(-dX, -dY + 1.0, -dZ);
				GL11.glVertex3d(-dX + 1.0, -dY + 1.0, -dZ);
				GL11.glVertex3d(-dX + 1.0, -dY + 1.0, -dZ + 1.0);
				GL11.glVertex3d(-dX, -dY + 1.0, -dZ + 1.0);
				GL11.glVertex3d(-dX, -dY + 1.0, -dZ);
				GL11.glVertex3d(-dX, -dY, -dZ);

				GL11.glVertex3d(-dX + 1.0, -dY, -dZ);
				GL11.glVertex3d(-dX + 1.0, -dY + 1.0, -dZ);
				GL11.glVertex3d(-dX + 1.0, -dY, -dZ);

				GL11.glVertex3d(-dX + 1.0, -dY, -dZ + 1.0);
				GL11.glVertex3d(-dX + 1.0, -dY + 1.0, -dZ + 1.0);
				GL11.glVertex3d(-dX + 1.0, -dY, -dZ + 1.0);

				GL11.glVertex3d(-dX, -dY, -dZ + 1.0);
				GL11.glVertex3d(-dX, -dY + 1.0, -dZ + 1.0);
				GL11.glVertex3d(-dX, -dY, -dZ + 1.0);
				GL11.glEnd();

				GL11.glDisable(GL11.GL_LINE_SMOOTH);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glEnable(GL11.GL_DEPTH_TEST);
				GL11.glPopMatrix();
			} else {
				Entity entity = target.getEntity();
				double X = entity.getX();
				double Y = entity.getY();
				double Z = entity.getZ();
				double dX = (mX - X);
				double dY = (mY - Y);
				double dZ = (mZ - Z);
				GL11.glPushMatrix();
				GL11.glColor4f(1f, 0f, 0f, 1f);
				GL11.glLineWidth(1.7f);
				GL11.glDisable(GL11.GL_TEXTURE_2D);
				GL11.glDisable(GL11.GL_LIGHTING);
				GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
				GL11.glEnable(GL11.GL_LINE_SMOOTH);

				GL11.glBegin(GL11.GL_LINE_LOOP);
				if(entity instanceof Animable) {
					GL11.glVertex3d(-dX - 0.5, -dY, -dZ - 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY + 2.0, -dZ - 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY + 2.0, -dZ - 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY + 2.0, -dZ + 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY + 2.0, -dZ + 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY + 2.0, -dZ - 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY, -dZ - 0.5);

					GL11.glVertex3d(-dX + 0.5, -dY, -dZ - 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY + 2.0, -dZ - 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY, -dZ - 0.5);

					GL11.glVertex3d(-dX + 0.5, -dY, -dZ + 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY + 2.0, -dZ + 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY, -dZ + 0.5);

					GL11.glVertex3d(-dX - 0.5, -dY, -dZ + 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY + 2.0, -dZ + 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY, -dZ + 0.5);
				} else if(entity instanceof Chicken) {
					GL11.glVertex3d(-dX - 0.5, -dY, -dZ - 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY + 1.0, -dZ - 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY + 1.0, -dZ - 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY + 1.0, -dZ + 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY + 1.0, -dZ + 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY + 1.0, -dZ - 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY, -dZ - 0.5);

					GL11.glVertex3d(-dX + 0.5, -dY, -dZ - 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY + 1.0, -dZ - 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY, -dZ - 0.5);

					GL11.glVertex3d(-dX + 0.5, -dY, -dZ + 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY + 1.0, -dZ + 0.5);
					GL11.glVertex3d(-dX + 0.5, -dY, -dZ + 0.5);

					GL11.glVertex3d(-dX - 0.5, -dY, -dZ + 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY + 1.0, -dZ + 0.5);
					GL11.glVertex3d(-dX - 0.5, -dY, -dZ + 0.5);
				}
				GL11.glEnd();

				GL11.glDisable(GL11.GL_LINE_SMOOTH);
				GL11.glEnable(GL11.GL_LIGHTING);
				GL11.glEnable(GL11.GL_TEXTURE_2D);
				GL11.glPopMatrix();
			}
		}
	}
}

