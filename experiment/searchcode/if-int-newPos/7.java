if (newPos.getX() < 0) {
newPos.setX(paper.getWidth() + newPos.getX());
} else if (newPos.getX() >= paper.getWidth()) {
newPos.setX(paper.getWidth() - newPos.getX());
}

// Wraps y coordinate if out of paper

