public void setTbUser(TbUser newTbUser) {
if (this.tbUser == null || !this.tbUser.equals(newTbUser))
{
if (this.tbUser != null)
this.tbUser = null;
oldTbUser.removeTbUserLog(this);
}
if (newTbUser != null)

