//					mWorld.setBlock(x1, y, z1, 98);
//				}
//			}
//		});
//--------------------------------------------------------------------------

public static void generate(int x0, int z0, int radius, CircleCallback callback)
private static void generateBlocks(int x0, int z0, int x, int z, CircleCallback callback)
{
callback.call(x0 + x, z0 + z);
if (x != 0)

