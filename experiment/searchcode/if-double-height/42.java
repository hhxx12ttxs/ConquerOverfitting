package org.usfirst.frc.team1458.robot;

public class Levels {

public double getHeight(MainLevel m, LevelMode l, CarryObject c, LevelMod d) {
if(d==LevelMod.LOAD) {
return m.getHeight()+l.getHeight()+c.getHeight()+d.getHeight();

