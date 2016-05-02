package fridayTea;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Equipment extends Item {
	// TODO add weight to eq.
	public int pos;
	public int def;
	public int matk;
	public int atk;
	public BufferedImage mapImg;
	public BufferedImage mapImgInv;
	public int xoffset = 0;
	public int yoffset = 0;
	public int weight;

	public static int getWeight() {
		int weight = 0;
		weight += PlayerPos.helm != null ? PlayerPos.helm.weight : 0;
		weight += PlayerPos.rhand != null ? PlayerPos.rhand.weight : 0;
		weight += PlayerPos.lhand != null ? PlayerPos.lhand.weight : 0;
		weight += PlayerPos.armor != null ? PlayerPos.armor.weight : 0;
                weight += PlayerPos.shoes != null ? PlayerPos.shoes.weight : 0;
		return weight;
	}

	public static void list(ArrayList<Integer> positions) {
		Main.gui.println("You currently have these items equiped:");
		Main.gui.println("[-1] Helmet: "
				+ (PlayerPos.helm != null ? PlayerPos.helm.name + " ("
						+ PlayerPos.helm.weight + " kg)" : "nothing"));
		Main.gui.println("[-2] Right hand: "
				+ (PlayerPos.rhand != null ? PlayerPos.rhand.name + " ("
						+ PlayerPos.rhand.weight + " kg)" : "nothing"));
		Main.gui.println("[-3] Left hand: "
				+ (PlayerPos.lhand != null ? PlayerPos.lhand.name + " ("
						+ PlayerPos.lhand.weight + " kg)" : "nothing"));
		Main.gui.println("[-4] Armor: "
				+ (PlayerPos.armor != null ? PlayerPos.armor.name + " ("
						+ PlayerPos.armor.weight + " kg)" : "nothing"));
                Main.gui.println("[-5] Shoes: "
				+ (PlayerPos.shoes != null ? PlayerPos.shoes.name + " ("
						+ PlayerPos.shoes.weight + " kg)" : "nothing"));
		Main.gui.println("You currently can equip any of these items:");
		for (int i = 0; i < positions.size(); i++) {
			if (PlayerPos.inventory.get(positions.get(i)).equip) {
				Main.gui.println("["
						+ (i + 1)
						+ "] "
						+ PlayerPos.inventory.get(positions.get(i)).name
						+ " (+"
						+ ((Equipment) PlayerPos.inventory.get(positions.get(i))).atk
						+ " atk) (+"
						+ ((Equipment) PlayerPos.inventory.get(positions.get(i))).def
						+ " def) (+"
						+ ((Equipment) PlayerPos.inventory.get(positions.get(i))).matk
						+ " matk) ("
						+ ((Equipment) PlayerPos.inventory.get(positions.get(i))).weight
						+ "kg)");
			}
		}
		Main.gui.println("[b] Go back");
	}

	public static void eq() {
                boolean firsttime = true;
		GUI.item = new EquipmentGFX();
		Main.gui.jpanel.repaint();
		ArrayList<Integer> positions = new ArrayList<Integer>();
		for (int i = 0; i < PlayerPos.inventory.size(); i++) {
			if (PlayerPos.inventory.get(i).equip) {
				positions.add(i);
			}
		}
		list(positions);
		while (true) {
			positions.clear();
			for (int i = 0; i < PlayerPos.inventory.size(); i++) {
				if (PlayerPos.inventory.get(i).equip) {
					positions.add(i);
				}
			}
                        
                        if (firsttime == false) {
                            Main.gui.println("Press any key to continue:");
                            int anykey = Main.gui.readInt();
                        list(positions);
                        }
                        firsttime = false;
                        Main.gui.jpanel.repaint();
			String choiseS = Main.gui.readString();
                        int choise = 0;
			try {
				choise = Integer.parseInt(choiseS);
			} catch (Exception e) {
			}
			if (choise <= -1 && choise >= -5) {
				Item item = null;
				boolean sucess = false;
				switch (choise) {
				case -1:
					if (PlayerPos.helm != null)
						sucess = PlayerPos.inventory.add(PlayerPos.helm);
					item = PlayerPos.helm;
					break;
				case -2:
					if (PlayerPos.rhand != null)
						sucess = PlayerPos.inventory.add(PlayerPos.rhand);
					item = PlayerPos.rhand;
					break;
				case -3:
					if (PlayerPos.lhand != null)
						sucess = PlayerPos.inventory.add(PlayerPos.lhand);
					item = PlayerPos.lhand;
					break;
				case -4:
					if (PlayerPos.armor != null)
						sucess = PlayerPos.inventory.add(PlayerPos.armor);
					item = PlayerPos.armor;
					break;
                                case -5:
					if (PlayerPos.shoes != null)
						sucess = PlayerPos.inventory.add(PlayerPos.shoes);
					item = PlayerPos.shoes;
					break;
				}
				if (item != null) {
					if (sucess) {
						switch (choise) {
						case -1:
							PlayerPos.helm = null;
							break;
						case -2:
							PlayerPos.rhand = null;
							break;
						case -3:
							PlayerPos.lhand = null;
							break;
						case -4:
							PlayerPos.armor = null;
							break;
                                                case -5:
							PlayerPos.shoes = null;
							break;
						}
						update();
						Main.gui.println("You unequipped a " + item.name);
					} else
						Main.gui.println("You don't have room in your inventory.");
				} else
					Main.gui.println("There's nothing to unequip.");
			}
			if (choise > 0 && choise <= positions.size()) {
				choise--;
				if (PlayerPos.inventory.get(positions.get(choise)) != null) {
					Item temp1 = PlayerPos.inventory.get(positions.get(choise));
					Item temp2 = equip(PlayerPos.inventory.get(positions
							.get(choise)));
					if (temp1 != temp2) {
						Main.gui.println("You have equipped a "
								+ PlayerPos.inventory.get(positions.get(choise)).name
								+ ".");
					} else {
						Main.gui.println("The item you tried to equip was to heavy.");
					}
                                        
					if (temp2 != null) {
						PlayerPos.inventory.set(positions.get(choise), temp1);
                                                
					} else {
						int tempa = positions.get(choise);
						PlayerPos.inventory.remove(tempa);
                                                
					}
                                        

				}
                                
				// Main.gui.println("You have equipped a " +
				// PlayerPos.inventory.get(positions.get(choise)).name +
				// ".");//Main.gui.jpanel.repaint();
                                
			}
			if (choiseS.equalsIgnoreCase("b")) {
				break;
			}
			Main.gui.jpanel.repaint();
		}
	}

	public static Item equip(Item item) {
		if (!item.equip) {
			return item;
		}
		Item retItem = null;
		switch (((Equipment) item).pos) {
		case 1:
			if (PlayerPos.helm != null) {
				if (getWeight() - PlayerPos.helm.weight
						+ ((Equipment) item).weight <= PlayerPos._str) {
					retItem = PlayerPos.helm;
					PlayerPos.helm = (Equipment) item;
				} else {
					retItem = item;
				}
			} else {
				if (getWeight() + ((Equipment) item).weight <= PlayerPos._str) {
					retItem = PlayerPos.helm;
					PlayerPos.helm = (Equipment) item;
				} else {
					retItem = item;
				}
			}
			break;
		case 2:
			Main.gui.println("Choose hand");
			Main.gui.println("[1] Right hand: "
					+ (PlayerPos.rhand != null ? PlayerPos.rhand.name
							: "nothing"));
			Main.gui.println("[2] Left hand: "
					+ (PlayerPos.lhand != null ? PlayerPos.lhand.name
							: "nothing"));
			while (true) {
				int choise2 = Main.gui.readInt();
				if (choise2 == 1) {
					if (PlayerPos.rhand != null) {
						if (getWeight() - PlayerPos.rhand.weight
								+ ((Equipment) item).weight <= PlayerPos._str) {
							retItem = PlayerPos.rhand;
							PlayerPos.rhand = (Equipment) item;
						} else {
							retItem = item;
						}
					} else {
						if (getWeight() + ((Equipment) item).weight <= PlayerPos._str) {
							retItem = PlayerPos.rhand;
							PlayerPos.rhand = (Equipment) item;
						} else {
							retItem = item;
						}
					}
					break;
				}
				if (choise2 == 2) {
					if (PlayerPos.lhand != null) {
						if (getWeight() - PlayerPos.lhand.weight
								+ ((Equipment) item).weight <= PlayerPos._str) {
							retItem = PlayerPos.lhand;
							PlayerPos.lhand = (Equipment) item;
						} else {
							retItem = item;
						}
					} else {
						if (getWeight() + ((Equipment) item).weight <= PlayerPos._str) {
							retItem = PlayerPos.lhand;
							PlayerPos.lhand = (Equipment) item;
						} else {
							retItem = item;
						}
					}
					break;
				}
			}
			break;
		case 3:
			if (PlayerPos.armor != null) {
				if (getWeight() - PlayerPos.armor.weight
						+ ((Equipment) item).weight <= PlayerPos._str) {
					retItem = PlayerPos.armor;
					PlayerPos.armor = (Equipment) item;
				} else {
					retItem = item;
				}
			} else {
				if (getWeight() + ((Equipment) item).weight <= PlayerPos._str) {
					retItem = PlayerPos.armor;
					PlayerPos.armor = (Equipment) item;
				} else {
					retItem = item;
				}
			}
			break;
                case 4:
			if (PlayerPos.shoes != null) {
				if (getWeight() - PlayerPos.shoes.weight
						+ ((Equipment) item).weight <= PlayerPos._str) {
					retItem = PlayerPos.shoes;
					PlayerPos.shoes = (Equipment) item;
				} else {
					retItem = item;
				}
			} else {
				if (getWeight() + ((Equipment) item).weight <= PlayerPos._str) {
					retItem = PlayerPos.shoes;
					PlayerPos.shoes = (Equipment) item;
				} else {
					retItem = item;
				}
			}
			break;
		}
		update();
		return retItem;
	}

	public static void update() {
		PlayerPos.aatk = 0;
		PlayerPos.adef = 0;
		PlayerPos.amatk = 0;
		PlayerPos.weight = 0;

		if (PlayerPos.helm != null) {
			PlayerPos.aatk += PlayerPos.helm.atk;
			PlayerPos.amatk += PlayerPos.helm.matk;
			PlayerPos.adef += PlayerPos.helm.def;
			PlayerPos.weight += PlayerPos.helm.weight;
		}
		if (PlayerPos.rhand != null) {
			PlayerPos.aatk += PlayerPos.rhand.atk;
			PlayerPos.amatk += PlayerPos.rhand.matk;
			PlayerPos.adef += PlayerPos.rhand.def;
			PlayerPos.weight += PlayerPos.rhand.weight;
		}
		if (PlayerPos.lhand != null) {
			PlayerPos.aatk += PlayerPos.lhand.atk;
			PlayerPos.amatk += PlayerPos.lhand.matk;
			PlayerPos.adef += PlayerPos.lhand.def;
			PlayerPos.weight += PlayerPos.lhand.weight;
		}
		if (PlayerPos.armor != null) {
			PlayerPos.aatk += PlayerPos.armor.atk;
			PlayerPos.amatk += PlayerPos.armor.matk;
			PlayerPos.adef += PlayerPos.armor.def;
			PlayerPos.weight += PlayerPos.armor.weight;
		}
                if (PlayerPos.shoes != null) {
                        PlayerPos.aatk += PlayerPos.shoes.atk;
			PlayerPos.amatk += PlayerPos.shoes.matk;
			PlayerPos.adef += PlayerPos.shoes.def;
			PlayerPos.weight += PlayerPos.shoes.weight;
		}
		Main.gui.update();
	}
}

