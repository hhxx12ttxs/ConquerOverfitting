package cn.hhuc.graph;

public class Edge {
public int x1,x2,y1,y2;//(x1,y1)是下端点
public double dx;
public double xNow;//当前交点
public Edge(int x1,int y1,int x2,int y2){
if(y1>=y2){

