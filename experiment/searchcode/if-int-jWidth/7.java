*     index   位置
*/
public class ImageLabel extends JLabel
{
private int jwidth = 100;  //设置最大宽度
private int jheight = 100; //设置最大高度
if(per > 1.0)
{
if(imagewidth > jwidth)
{
imagewidth = jwidth;
imageheight = (int)(1.0*imagewidth/per);

