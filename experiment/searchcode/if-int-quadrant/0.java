public class Sudoku {

Quadrant[] quadranten;

public Sudoku() {
quadranten = new Quadrant[9];
for (int i = 0; i <= 8; i++) {
public Quadrant getQuadrant(int quadrantNr) {
return quadranten[quadrantNr];
}

public void print() {

