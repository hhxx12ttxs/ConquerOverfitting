package tanks.field;

import tanks.Drowable;
import tanks.Quadrant;

import java.awt.*;
import java.util.List;
for (int v = 1; v <= getDimensionX(); v++) {
FieldObject object = scanQuadrant(v, h);
if (object != null &amp;&amp; object.getClass() == Eagle.class) {

