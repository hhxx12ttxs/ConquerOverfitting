package org.loon.framework.android.game.srpg.ability;

import java.util.ArrayList;
import java.util.Arrays;

import org.loon.framework.android.game.core.LSystem;
import org.loon.framework.android.game.core.graphics.LColor;
import org.loon.framework.android.game.srpg.SRPGType;
import org.loon.framework.android.game.srpg.actor.SRPGActor;
import org.loon.framework.android.game.srpg.actor.SRPGActorFactory;
import org.loon.framework.android.game.srpg.actor.SRPGActors;
import org.loon.framework.android.game.srpg.actor.SRPGStatus;
import org.loon.framework.android.game.srpg.effect.SRPGEffect;
import org.loon.framework.android.game.srpg.effect.SRPGEffectFactory;
import org.loon.framework.android.game.srpg.effect.SRPGExtinctEffect;
import org.loon.framework.android.game.srpg.field.SRPGField;
import org.loon.framework.android.game.srpg.field.SRPGMoveStack;

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
final class SRPGAbilityTemp {

	final static void make() {

		ArrayList<SRPGAbility> lazyAbilityClass = SRPGAbilityFactory.lazyAbilityClass;

		int index = 0;

		SRPGAbility[] abilitys = new SRPGAbility[24];

		// 0-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "??????????????????????????";
				this.minLength = 1;
				this.maxLength = 1;
				this.mp = 0;
				this.baseDamage = 1;
				this.range = 0;
				this.target = 0;
				this.counter = 0;
				this.direct = 0;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_PHYSICS);
				factory.damageValue = factory.status.strength / 10
						+ factory.status.dexterity / 100
						+ factory.status.vitality / 100;
				factory.damageValue = factory.damageValue > 0 ? factory.damageValue
						: baseDamage;

			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_CHOP, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {

				return null;
			}

			public int[] getAbilitySkill() {

				return null;
			}

		};
		index++;

		// 1-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "??????????,??????????????";
				this.minLength = 2;
				this.maxLength = 3;
				this.mp = 0;
				this.baseDamage = 1;
				this.range = 0;
				this.target = 0;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_PHYSICS);
				factory.damageValue = factory.status.strength / 10
						+ factory.status.dexterity / 100
						+ factory.status.vitality / 100;
				factory.damageValue = factory.damageValue > 0 ? factory.damageValue
						: baseDamage;

			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_ARROW, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;
		
		// 2-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "??????????????,?????????????";
				this.minLength = 2;
				this.maxLength = 3;
				this.mp = 0;
				this.baseDamage = 1;
				this.range = 1;
				this.target = 0;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_PHYSICS);
				factory.damageValue = (((factory.status.dexterity * 3) / 4 + factory.status.strength / 4) * factory.atk)
						/ 100 - (factory.status1.vitality * factory.def) / 100;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_ARROW, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;


		// 3-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "???????,??????????????";
				this.minLength = 1;
				this.maxLength = 1;
				this.mp = 0;
				this.baseDamage = 10;
				this.range = 0;
				this.target = 0;
				this.counter = 0;
				this.direct = 0;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_PHYSICS);
				factory.damageValue = ((factory.status.strength * factory.atk + factory.status.vitality / 10) / 100 - ((factory.status1.vitality / 8) * factory.def) / 100)
						+ baseDamage;
				factory.hitRate = (factory.hitRate * 7) / 10;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_CHOP, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 4-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "????????????";
				this.minLength = 1;
				this.maxLength = 1;
				this.mp = 10;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 0;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_MPDAMAGE;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_RECOVERY);
				factory.hitRate = (factory.hitRate * 7) / 10;
				d.setStatus(SRPGStatus.STATUS_LOVER);
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_S, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 5-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "???????";
				this.minLength = 0;
				this.maxLength = 1;
				this.mp = 10;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 1;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_HELPER;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				factory.hitRate = 100;
				d.setStatus(SRPGStatus.STATUS_AGILITY);
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_CURE, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 6-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "????????????????????";
				this.minLength = 0;
				this.maxLength = 0;
				this.mp = 0;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 1;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_RECOVERY;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_RECOVERY);
				factory.damageValue = (factory.status.sp + factory.status.mind) / 16;
				factory.hitRate = 100;
				factory.isDamage = false;

			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_CURE, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 7-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "????????????????";
				this.minLength = 0;
				this.maxLength = 1;
				this.mp = 10;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 1;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_RECOVERY;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_RECOVERY);
				factory.damageValue = (factory.status.sp + factory.status.mind) / 6;
				factory.hitRate = 100;
				factory.isDamage = false;

			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_CURE, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {

				return null;
			}

			public int[] getAbilitySkill() {

				return null;
			}

		};
		index++;

		// 8-???
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "???";
				this.abilityAbout = "?????????????,?????????";
				this.minLength = 0;
				this.maxLength = 1;
				this.mp = 30;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 1;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_RECOVERY;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_RECOVERY);
				factory.damageValue = factory.status1.max_hp / 2;
				factory.hitRate = 100;
				for (int i = 9; i < 15; i++) {
					factory.status2.status[i] = 0;
				}
				factory.isDamage = false;
				factory.damageChange = 0;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_CURE, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {

				return null;
			}

			public int[] getAbilitySkill() {

				return null;
			}

		};
		index++;

		// 9-????
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "????";
				this.abilityAbout = "???????????????????????????????????????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 100;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 2;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_HELPER;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_SAINT);
				factory.damageValue = factory.status1.max_hp - 1;
				if (factory.damageValue <= factory.status.hp) {
					factory.damageValue = factory.status.hp - 1;
				}
				for (int i = 9; i < 15; i++) {
					factory.status2.status[i] = 0;
				}
				factory.hitRate = 100;
				factory.isDamage = false;
				factory.damageChange = 0;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_LOOT_1, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				damageData.setDamage(damageaverage.damage);
				return damageData;
			}

			public int[] getAbilitySkill() {

				return null;
			}

		};
		index++;

		// 10-????
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "????";
				this.abilityAbout = "???????????,??????????????????????????????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 100;
				this.baseDamage = 0;
				this.range = 3;
				this.target = 0;
				this.counter = 0;
				this.direct = 0;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ALLRECOVERY;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_DARK);
				factory.damageValue = (factory.status.sp / 100
						+ factory.status.strength / 100
						+ factory.status.vitality / 100
						+ factory.status.dexterity / 100 + factory.status.mp / 1000)
						+ factory.status.mind / 10;
				factory.hitRate = factory.status.dexterity / 50
						+ factory.hitRate / 2 - factory.def / 1000;
				for (int i = 9; i < SRPGStatus.STATUS_MAX; i++) {
					factory.statasFlag[i] = LSystem.random
							.nextInt(factory.hitRate * 2);
					d.setStatus(i);
				}
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_BLOOD_1, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {

				return null;

			}

			public int[] getAbilitySkill() {

				return null;
			}

		};
		index++;

		// 11-????
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "????";
				this.abilityAbout = "?????????????????,??????????????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 10;
				this.baseDamage = 0;
				this.range = 1;
				this.target = 2;
				this.counter = 0;
				this.direct = 0;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_PHYSICS);
				factory.isStatus = true;
				factory.damageValue = ((factory.status.strength + factory.status.agility / 5) * factory.atk)
						/ 100
						- (((factory.status2.vitality * 7) / 16) * factory.def)
						/ 100;
				factory.statasFlag[SRPGStatus.STATUS_WEAK] = factory.hitRate / 8;
				d.setStatus(SRPGStatus.STATUS_WEAK);
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_ARROWS, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				if (damageaverage.number > 0) {
					damageData.setDamage(status.strength / 5);
					return damageData;
				} else {
					return null;
				}
			}

			public int[] getAbilitySkill() {

				return null;
			}

		};
		index++;

		// 12-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "??????";
				this.minLength = 0;
				this.maxLength = 1;
				this.mp = 0;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 1;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_HELPER;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_VOID);
				d.setDirection(defender.getDirection());
				factory.hitRate = 100;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_FADE, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {

				return null;

			}

			public int[] getAbilitySkill() {

				return new int[] { SRPGStatus.SKILL_CARRY };
			}

		};
		index++;

		// 13-???????
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "???????";
				this.abilityAbout = "???????????????????,??????,?????????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 50;
				this.baseDamage = 100;
				this.range = 3;
				this.target = 0;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_ALLDAMAGE;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_THUNDER);
				factory.damageValue = (((((factory.status.strength + factory.status.vitality / 2) * 10) / 13) * factory.atk) / 100 - (factory.status1.sp * factory.def) / 100)
						+ baseDamage;
				factory.hitRate = factory.hitRate * 2;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_T, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 14-????
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "????";
				this.abilityAbout = "??????????,???????,????,????,????,????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 100;
				this.baseDamage = 666;
				this.range = 1;
				this.target = 0;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_DARK);
				factory.damageValue = ((factory.status.strength * factory.atk) / 100 - (factory.status1.vitality * factory.def) / 100) / 8;
				factory.hitRate *= 2;
				factory.damageValue = factory.damageValue + baseDamage;
				int fd = defender.findDirection(attacker.getPosX(), attacker
						.getPosY());
				int x = defender.getPosX();
				int y = defender.getPosY();
				int md = SRPGActor.matchDirection(fd);
				SRPGMoveStack stack = new SRPGMoveStack(x, y);
				stack.setDefault(10, false, false);
				stack.addStack(md);
				if (field.checkArea(stack.getPosX(), stack.getPosY())
						&& actors.checkActor(stack.getPosX(), stack.getPosY()) == -1
						&& field.getMoveCost(
								defender.getActorStatus().movetype | 0x20,
								stack.getPosX(), stack.getPosY()) != -1) {
					d.setMoveStack(stack);
				}
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_FADE, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 15-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "??????,??????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 20;
				this.baseDamage = 5;
				this.range = 0;
				this.target = 0;
				this.counter = 0;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_WATER);
				factory.damageValue = ((factory.status.magic
						+ factory.status.mind + factory.status.vitality) * 3) / 16;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_ICE, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 16-???????
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "???????";
				this.abilityAbout = "????????????,?????????????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 100;
				this.baseDamage = 50;
				this.range = 2;
				this.target = 0;
				this.counter = 0;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_ALLDAMAGE;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_WATER);
				factory.damageValue = ((factory.status.magic
						+ factory.status.mind + factory.status.vitality) * 10) / 16;
				d.setStatus(SRPGStatus.STATUS_STUN);
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_SNOW, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 17-???
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "???";
				this.abilityAbout = "???????,??????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 20;
				this.baseDamage = 5;
				this.range = 0;
				this.target = 0;
				this.counter = 0;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_FIRE);
				factory.damageValue = ((factory.status.magic
						+ factory.status.mind + factory.status.sp) * 3) / 16;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_FIRE, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 18-???
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "???";
				this.abilityAbout = "?????????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 10;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 0;
				this.counter = 0;
				this.direct = 1;
				this.selectNeed = 1;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_DARK);
				factory.damageValue = ((factory.status.magic + factory.status.mind) * 2) / 16;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_S, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 19-?????
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "?????";
				this.abilityAbout = "???????,??????????????,??????,???????????????";
				this.minLength = 1;
				this.maxLength = 2;
				this.mp = 300;
				this.baseDamage = 0;
				this.range = 3;
				this.target = 0;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_PHYSICS);
				d.setHelpers("STOP");
				SRPGStatus status = factory.status2;
				d.setStatus(SRPGStatus.STATUS_WEAK);
				d.setStatus(SRPGStatus.STATUS_SILENCE);
				d.setStatus(SRPGStatus.STATUS_STUN);
				boolean result = false;
				if (status.immunity != null) {
					for (int i = 0; i < status.immunity.length; i++) {
						if (status.immunity[i] == SRPGStatus.ELEMENT_PHYSICS) {
							result = true;
							break;
						}
					}
				}
				if (result) {
					int[] immunity = new int[status.immunity.length - 1];
					if (status.immunity != null) {
						for (int i = 0; i < status.immunity.length; i++) {
							if (status.immunity[i] != SRPGStatus.ELEMENT_PHYSICS) {
								immunity[i] = status.immunity[i];
							}
						}
					}
					status.immunity = immunity;
				}
				factory.hitRate = 100;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_BLAST, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 20-????
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "????";
				this.abilityAbout = "?????????,??????????";
				this.minLength = 1;
				this.maxLength = 1;
				this.mp = 1;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 0;
				this.counter = 1;
				this.direct = 1;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ALLDAMAGE;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_DARK);
				factory.flag = false;
				factory.damageValue = factory.status.mp;
				if (factory.damageValue > factory.status.max_mp
						- factory.status.mp) {
					factory.damageValue = factory.status.max_mp
							- factory.status.mp;
				}
				factory.hitRate = 100;
				factory.damageChange = 0;

			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_LOOT_1, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				damageData.setDamage((damageaverage.damage * 8) / 10);
				damageData.setGenre(SRPGType.GENRE_MPRECOVERY);
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 21-????
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "????";
				this.abilityAbout = "??????????????,???????,?????????,???????????";
				this.minLength = 0;
				this.maxLength = 0;
				this.mp = 999;
				this.baseDamage = 999;
				this.range = 7;
				this.target = 2;
				this.counter = 0;
				this.direct = 0;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ALLDAMAGE;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_DARK);
				factory.damageValue = this.baseDamage;
				factory.hitRate = 100;
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return new SRPGExtinctEffect(x * actor.getTileWidth()
						+ actor.getTileWidth() / 2, y * actor.getTileHeight()
						+ actor.getTileHeight() / 2, LColor.black,
						this.abilityName);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return new int[] { SRPGStatus.SKILL_UNDEAD };
			}

		};
		index++;

		// 22-??
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "??";
				this.abilityAbout = "???????????,????????????????";
				// ????????(???,?????????)
				this.minLength = 2;
				// ????????
				this.maxLength = 3;
				this.mp = 30;
				this.baseDamage = 10;
				this.range = 0;
				this.target = 0;
				this.counter = 0;
				this.direct = 0;
				// ???????0??,1??
				this.selectNeed = 0;
				// ????
				this.genre = SRPGType.GENRE_ATTACK;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_SAINT);
				factory.damageValue = ((factory.status.strength * factory.atk * baseDamage) / 1000 + (factory.status.mind + baseDamage)) / 10;
				int fd = defender.findDirection(attacker.getPosX(), attacker
						.getPosY());
				SRPGMoveStack movestack = new SRPGMoveStack(defender.getPosX(),
						defender.getPosY());
				movestack.setDefault(2, false, false);
				int direction = SRPGActor.matchDirection(fd);
				int count = 0;
				for (;;) {
					if (count >= 3) {
						break;
					}
					movestack.addStack(direction);
					int mx = movestack.getPosX();
					int my = movestack.getPosY();
					if (!field.checkArea(mx, my)
							|| actors.checkActor(mx, my) != -1
							|| field.getMoveCost(
									defender.getActorStatus().movetype
											| SRPGStatus.MOVETYPE_SLOWMOVE, mx,
									my) == -1) {
						movestack.removeStack();
						break;
					}
					count++;
				}
				d.setMoveStack(movestack);
				factory.damageValue = factory.damageValue > 0 ? factory.damageValue
						: baseDamage;

			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_T, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		// 23-???
		abilitys[index] = new SRPGAbility() {

			public void initConfig() {
				this.abilityName = "???";
				this.abilityAbout = "????,????,????,????,????,????";
				this.minLength = 0;
				this.maxLength = 3;
				this.mp = 666;
				this.baseDamage = 0;
				this.range = 0;
				this.target = 0;
				this.counter = 0;
				this.direct = 0;
				this.selectNeed = 0;
				this.genre = SRPGType.GENRE_ALLDAMAGE;
			}

			public void runDamageExpect(SRPGActor attacker, SRPGActor defender,
					SRPGAbilityFactory factory, SRPGField field,
					SRPGDamageData d, SRPGActors actors) {
				d.setElement(SRPGStatus.ELEMENT_VOID);
				SRPGActorFactory.runLevelUp(factory.status2, 1);
				factory.status2.exp = -100;
				factory.status2.hp = factory.status2.max_hp;
				factory.status2.mp = factory.status2.max_mp;
				factory.status2.computer = SRPGType.NOMOVE;
				factory.status2.immunity = null;
				factory.damageValue = 1;
				d.setHelpers("FIRST");
				d.setStatus(SRPGStatus.STATUS_STUN);
			}

			public SRPGEffect runAbilityEffect(int index, SRPGActor actor,
					int x, int y) {
				return SRPGEffectFactory.getAbilityEffect(
						SRPGEffectFactory.EFFECT_TAICHI, actor, x, y);
			}

			public SRPGDamageData dataInput(SRPGDamageAverage damageaverage,
					SRPGDamageData damageData, SRPGStatus status) {
				return null;
			}

			public int[] getAbilitySkill() {
				return null;
			}

		};
		index++;

		lazyAbilityClass.addAll(Arrays.asList(abilitys));

	}
}

