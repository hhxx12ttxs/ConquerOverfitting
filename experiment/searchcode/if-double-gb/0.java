int temp1 = gb.getWidth()/2;
int temp2 = gb.getHeight()/2;
double dx = (p.getX()+p.getWidth()/2) - (gb.getX()+temp1);//dx = (play_x_coordinate + (player radius)) - (gravball_x_coordinate + (gravball radius))
double dy = (p.getY()+p.getHeight()/2) - (gb.getY()+temp2);
double distance = Math.sqrt((dx*dx)+(dy*dy));

