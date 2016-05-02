package org.loon.framework.javase.game.srpg.field;

import java.awt.Image;

import org.loon.framework.javase.game.core.LRelease;
import org.loon.framework.javase.game.utils.CollectionUtils;
import org.loon.framework.javase.game.utils.GraphicsUtils;

/**
 * Copyright 2008 - 2011
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * @project loonframework
 * @author chenpeng
 * @email?ceponline@yahoo.com.cn
 * @version 0.1
 */
public class SRPGFieldElements implements LRelease {

	// ---- ????????(??????) ----//
	private final static String[] ELEMENT_NAMES = { "??", "??", "??", "??",
			"??", "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
			"??", "??", "??", "??", "??", "??", "????" };

	private final static int ELEMENT_TYPES[][] = {
			{ 1, SRPGField.FIELD_NORMAL, 85, 65 },
			{ 2, SRPGField.FIELD_NORMAL, 81, 69 },
			{ 1, SRPGField.FIELD_NORMAL, 70, 60 },
			{ 1, SRPGField.FIELD_NORMAL, 80, 58 },
			{ 3, SRPGField.FIELD_MIRE, 55, 70 },
			{ 2, SRPGField.FIELD_WATER, 55, 35 },
			{ 3, SRPGField.FIELD_WATER, 25, 15 },
			{ 1, SRPGField.FIELD_MIRE, 77, 55 },
			{ 2, SRPGField.FIELD_MIRE, 60, 60 },
			{ 1, SRPGField.FIELD_MIRE, 83, 63 },
			{ 2, SRPGField.FIELD_MIRE, 80, 60 },
			{ -1, SRPGField.FIELD_WALL, 85, 65 },
			{ -1, SRPGField.FIELD_NORMAL, 85, 75 },
			{ 1, SRPGField.FIELD_NORMAL, 100, 80 },
			{ -1, SRPGField.FIELD_WALL, 100, 100 },
			{ 2, SRPGField.FIELD_MIRE, 70, 50 },
			{ -1, SRPGField.FIELD_NORMAL, 85, 65 },
			{ 1, SRPGField.FIELD_NORMAL, 90, 70 },
			{ 1, SRPGField.FIELD_NORMAL, 93, 73 },
			{ 2, SRPGField.FIELD_NORMAL, 65, 65 },
			{ 1, SRPGField.FIELD_KILL, 20, 5 },
			{ -1, SRPGField.FIELD_KILL, 0, 0 } };

	private final SRPGFieldElement[] battleTypes;

	private final int size;

	public final static int[] getDefElement(int index) {
		if (index >= 0 && index <= ELEMENT_NAMES.length) {
			return ELEMENT_TYPES[index];
		}
		return null;
	}

	public final static String getDefElementName(int index) {
		if (index >= 0 && index <= ELEMENT_NAMES.length) {
			return ELEMENT_NAMES[index];
		}
		return null;
	}

	public SRPGFieldElements(int size) {
		this.battleTypes = new SRPGFieldElement[size];
		this.size = size;
	}

	public SRPGFieldElements() {
		// ?????????32????????????BIGMAP????(??????,
		// Android???????,??JavaSE???????)
		this(32);
	}

	public SRPGFieldElements(SRPGFieldElements elements) {
		this.battleTypes = (SRPGFieldElement[]) CollectionUtils
				.copyOf(elements.battleTypes);
		this.size = battleTypes.length;
	}

	public void putBattleElement(int index, int id, String name) {
		int[] res = getDefElement(id);
		putBattleElement(index, getDefElementName(id), "", res[0], res[2],
				res[3], res[1]);
	}

	public void putBattleElement(int index, String name, String depict, int mv,
			int atk, int def, int state) {
		this.isRangeCheck(index);
		this.addBattleElement(index, (Image) null, name, depict, mv, atk, def,
				state);
	}

	public void addBattleElement(int index, int id, String fileName) {
		int[] res = getDefElement(id);
		addBattleElement(index, fileName, getDefElementName(id), "", res[0],
				res[2], res[3], res[1]);
	}

	public void addBattleElement(int index, int id, String fileName, String name) {
		int[] res = getDefElement(id);
		addBattleElement(index, fileName, name, "", res[0], res[2], res[3],
				res[1]);
	}

	public void addBattleElement(int index, int mv, int state, String fileName,
			String name) {
		addBattleElement(index, fileName, name, "", mv, 100, 100, state);
	}

	public void addBattleElement(int index, String fileName, String name,
			String depict, int mv, int atk, int def, int state) {
		addBattleElement(index, GraphicsUtils.loadNotCacheImage(fileName),
				name, depict, mv, atk, def, state);
	}

	public void addBattleElement(int index, int mv, int state, Image img,
			String name) {
		addBattleElement(index, img, name, "", mv, 100, 100, state);
	}

	public void addBattleElement(int index, Image img, String name,
			String depict, int mv, int atk, int def, int state) {
		this.isRangeCheck(index);
		SRPGFieldElement element = new SRPGFieldElement(img, name, depict, mv,
				atk, def, state);
		SRPGFieldElement tmp = battleTypes[index];
		if (tmp != null) {
			if (tmp.image != null) {
				tmp.image.flush();
				tmp.image = null;
			}
			tmp = null;
		}

		battleTypes[index] = element;
	}

	private void isRangeCheck(int index) {
		if (index >= size) {
			throw new IndexOutOfBoundsException("Elemetns Index: " + index
					+ ", Size: " + size);
		}
	}

	public SRPGFieldElement getBattleElement(int index) {
		if (index < 0) {
			return null;
		}
		this.isRangeCheck(index);
		return (SRPGFieldElement) battleTypes[index];
	}

	public void dispose() {
		for (int i = 0; i < size; i++) {
			SRPGFieldElement tmp = battleTypes[i];
			if (tmp != null) {
				if (tmp.image != null) {
					tmp.image.flush();
					tmp.image = null;
				}
				tmp = null;
			}
		}
	}

}

