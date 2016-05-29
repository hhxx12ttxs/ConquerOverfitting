Graphics2D g2 = (Graphics2D)g;
g2.setColor(Color.WHITE);
Double nextX = 0.0;
Double nextY = 0.0;
for(int i = 0; i < 17; i ++){
if(!queue.isEmpty()){
Line2D nLine = new Line2D.Double();
Double nX = (Math.random()*500);

