System.exit(0); break;
}

inputLine = s.nextLine().trim();
if(inputLine.isEmpty()){
inputLine = s.nextLine().trim();
Cube experimentCube = new Cube(2);
experimentCube.scrambleCube(k);
if (experimentCube.isSolved()) {

