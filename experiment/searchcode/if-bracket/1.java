package com.blogspot.dikuro.example.quiz.bracket;

public class Bracket {
private char openBracket;
public boolean isCloseBracket(Bracket bracket) {
boolean result = false;
if(!bracket.isOpen()) {
if(this.closeBracket == bracket.getOriginBracket()) {

