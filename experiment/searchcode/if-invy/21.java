for (int i = 0; i <= 3 - this.recipeWidth; ++i)
for (int j = 0; j <= 3 - this.recipeHeight; ++j)
if (this.checkMatch(inv, i, j))
ItemStack itemstack1 = inv.getStackInRowAndColumn(invX, invY);

if (itemstack1 != null || itemstack != null)

