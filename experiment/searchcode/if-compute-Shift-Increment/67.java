is_moved = horizontal_pressed(real_end,left_increment);
} else if (keyCode == RIGHT) {
is_moved = horizontal_pressed(fake_end,right_increment);
} else if (keyCode == UP) {
panel = rotateRight(panel);
is_moved = horizontal_pressed(fake_end,right_increment);

