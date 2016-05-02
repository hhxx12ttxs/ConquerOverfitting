import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.LinkedList;


public class BlockField extends RenderableEntity{
	
	LinkedList<LinkedList<PuzzleBlock>> map;
	public int Width, Height;
	
	public BlockField(int w, int h)
	{
		super(0,new Rectangle());
		Width = w;
		Height = h;
		map = new LinkedList<LinkedList<PuzzleBlock>>();
		EmptyMap();
	}
	
	public void EmptyMap()
	{
		for(int i = 0; i < map.size(); i++)
		{
			map.get(i).clear();
			for(int j = 0; j < map.get(i).size(); j++)
			{
				map.get(i).add(new PuzzleBlock(new Point2D.Float(i,j),PuzzleBlock.BLOCK_W,PuzzleBlock.BLOCK_H));
			}
		}
	}
	
	public boolean SetBlock(PuzzleBlock block, int x, int y)
	{
		if(!IsInField(x, y))
			return false;
		
		map.get(x).add(y, block);
		
		return true;
	}
	
	public PuzzleBlock GetBlock(int x, int y)
	{
		if(!IsInField(x, y))
			return null;
		
		PuzzleBlock b  = map.get(x).get(y);
		
		return b;
	}
	
	public boolean IsInField(int x, int y)
	{
		if(x >= Width || x < 0)
			return false;
		
		if(y < 0 || y >= Height)
			return false;
		
		return true;
	}
	
	public Point PosToIndex(Point pos)
	{
		Point p = new Point(0,0);
		
		p.x = (pos.x - rect.x) / PuzzleBlock.BLOCK_W;
		p.y = Height - ((pos.y - rect.y) / PuzzleBlock.BLOCK_H);
		
		return p;
	}
	
	public LinkedList<PuzzleBlock> GetAdjecentBlocksOfSameType(PuzzleBlock block)
	{
		LinkedList<PuzzleBlock> total = new LinkedList<PuzzleBlock>();
		LinkedList<PuzzleBlock> found = new LinkedList<PuzzleBlock>();
		
		Point dirs[] = { new Point(1,0),new Point(0,1),
				new Point(-1,0), new Point(0,-1) };
		
		block.Checked = true;
		
		for(int j = 0; j < dirs.length; j++)
		{
			Point dir = dirs[j];
			Point newPos = PosToIndex(new Point(block.getX(),block.getY()));
			newPos.x += dir.x;
			newPos.y += dir.y;
			
			if(IsInField(newPos.x, newPos.y))
			{
				PuzzleBlock newBlock = GetBlock(newPos.x, newPos.y);
				if(newBlock.BlocksTypeEqual(block))
				{
					if(newBlock.Checked == false)
						found.add(newBlock);	
				}
			}
		}
		
		for(int i = 0; i < found.size(); i++)
		{
			PuzzleBlock newBlock = found.get(i);
			total.add(newBlock);
			LinkedList<PuzzleBlock> news = GetAdjecentBlocksOfSameType(newBlock);
			total.addAll(news);
		}
		
		//total.add(block);
		block.Checked = false;
		
		return total;
	}

}

