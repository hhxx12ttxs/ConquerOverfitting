package sdt.java3d;

import sdt.geometry.*;
import sdt.stepb.*;
import java.util.ArrayList;
private int                theCurveType = 0; //0 unkonwn, 1 line, 2 circle
private double scale = 1;

public SDT_3DEdge(stepb_edge_curve ec)
{
stepb_curve        theCurve;

