private float invX;
private float invY;
private float difX;
private float difY;
private float canvasHeight;
canvas.drawBitmap(bitmap, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), new Rect((int)x - bitmap.getWidth()/resize,(int) y - bitmap.getHeight()/resize,(int) x + bitmap.getWidth()/resize,(int) y + bitmap.getHeight()/resize), null);

if(playPickupAnimation)
{
pickupAnimation(difX, difY);

