package com.dou.book.util;

public class PageUtil {
public int getStartRow(int totalRow,int rowCount){
public int getTotalPage(int totalRow,int rowCount){
if(rowCount>totalRow){
rowCount=totalRow;
}
if(totalRow%rowCount==0){

