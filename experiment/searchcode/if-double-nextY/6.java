int nextX = current_x + direction_x;
int nextY = current_y + direction_y;

if (recursion_depth > 50) return null;

if (nextX < 0 || !(nextX < map.map_width) || nextY < 0 || !(nextY < map.map_height)) return null;

