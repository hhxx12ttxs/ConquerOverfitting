package me.osm.gazetter.striper.builders;

public class BBOX {
double minX = Double.NaN;
double minY = Double.NaN;
public void extend(double x, double y) {
if(Double.isNaN(minX)) {
minX = x;
maxX = x;
minY = y;
maxY = y;

