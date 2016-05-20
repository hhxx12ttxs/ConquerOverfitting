import java.awt.Graphics2D;
import java.awt.Point;
//import java.awt.List;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.GraphicAttribute;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Float;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import javax.naming.LinkLoopException;

import org.omg.PortableInterceptor.ACTIVE;


public class PuzzleField extends RenderableEntity implements KeyListener{
	
	int FIELD_WIDTH, FIELD_HEIGHT;

	float SpawnRate = 0.5f;
	float mElapsedTime;
	PuzzleBlock[][] Blocks;
	List<PuzzleBlock> ActiveBlock;
	List<ArrayList<PuzzleBlock>> BlockMap;
	LinkedList<LinkedList<PuzzleBlock>> Map;
	
	BlockField mField;
	int mCurrentRot;
	
	public PuzzleField(int w, int h)
	{
		super(0,new Rectangle());
		FIELD_WIDTH = w;
		FIELD_HEIGHT = h;
		mElapsedTime = 0.0f;
		
		Blocks = new PuzzleBlock[FIELD_WIDTH][FIELD_HEIGHT];
		ActiveBlock = new ArrayList<PuzzleBlock>();
		BlockMap = new ArrayList<ArrayList<PuzzleBlock>>();
		Map = new LinkedList<LinkedList<PuzzleBlock>>();
		
		for(int i = 0; i < FIELD_WIDTH; i++)
		{
			BlockMap.add(new ArrayList<PuzzleBlock>());
		}
		
		AddNewBlock();
		mCurrentRot = 0;
		
		mField = new BlockField(w, h);
	}
	
	public void update(float time)
	{
		HandleBlockSpawn(time);
		for(int i = 0; i < ActiveBlock.size(); i++)
		{
			PuzzleBlock b = ActiveBlock.get(i);
			b.update(time);
		}
		
		for(int i = 0; i < BlockMap.size(); i++)
		{
			for(int j = 0; j < BlockMap.get(i).size(); j++)
			{
				PuzzleBlock block = BlockMap.get(i).get(j);
				
				block.update(time);
				
				if(BlockMap.get(i).get(j).StateChanged() == true)
				{
					System.out.print(" state Changed ");
					HandleBlockClustering(block);
					block.StateIsChanged(false);
				}
				
			}
		}
		
		for(int i = 0; i < BlockMap.size(); i++)
		{
			for(int j = 0; j < BlockMap.get(i).size(); j++)
			{
				PuzzleBlock block = BlockMap.get(i).get(j);
				
				if(!block.Alive)
				{
					
					//if(!block.Alive)
					{
						UpdateCollumn(i, time);
					}
					BlockMap.get(i).remove(j);
				}
			}
		}
	}
	
	public void UpdateCollumn(int c, float time)
	{
		for(int i = 0; i < BlockMap.get(c).size(); i++)
		{
			PuzzleBlock b = BlockMap.get(c).get(i);
			if(!b.Alive || b.IsInState(FallingState.FallingStateID))
				continue;
			
			if(NumEmptySpacesBelowBlock(b) > 0)
			{
				//b.Move(0,200*time);
				b.ChangeState(new FallingState(b,NumEmptySpacesBelowBlock(b)));
			}
			
			//b.SetPosition(b.getX(), (int)FieldHeightInPixels() - (i * PuzzleBlock.BLOCK_H));
		}
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
			
			if(IsBlock(newPos.x, newPos.y))
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
	
	public void HandleBlockClustering(PuzzleBlock block)
	{
		int count = 0;
		
		Point dirs[] = { new Point(1,0),new Point(0,1),
				new Point(-1,0), new Point(0,-1) };
		
		LinkedList<PuzzleBlock> found = GetAdjecentBlocksOfSameType(block);
		
		block.num = found.size();
		
		if(found.size() >= 3)
		{
			for(int i = 0; i < found.size(); i++)
			{
				PuzzleBlock bl = found.get(i);
				Point p = PosToIndex(new Point(bl.getX(), bl.getY()));
				bl.Kill();
				block.Kill();
				//RemoveBlock(block);
				//RemoveBlock(bl);
				//UpdateCollumn(p.x);
				//block.StateChanged = false;
			}
		}
		block.Checked = false;
		//block.StateChanged = false;
	}
	
	public void HandleBlockSpawn(float time)
	{
		mElapsedTime += time;
		boolean bottomHit = false;
		for(int i = 0; i < ActiveBlock.size(); i++)
		{
			PuzzleBlock b = ActiveBlock.get(i);
			if(BlockReachedBottom(b))
			{
				b.ChangeState(new IdleState(b));
				bottomHit = true;
			}
		}
		
		if(bottomHit)
		{
			AddNewBlock();
			mElapsedTime = 0.0f;
		}
		
	}
	
	public void AddNewBlock()
	{
		if(ActiveBlock.size() > 0)
			if(ActiveBlock.get(0).IsInState(FallingState.FallingStateID))
				return;
		
		for(int i = 0; i < ActiveBlock.size(); i++)
		{
			PuzzleBlock b = ActiveBlock.get(i);
			AddBlockToBottom(b);
		}
		
		Point2D.Float start = new Point2D.Float(0,0);
		PuzzleBlock block = new PuzzleBlock(start);
		
		start.x += PuzzleBlock.BLOCK_W;
		PuzzleBlock block2 = new PuzzleBlock(start);
		ActiveBlock.clear();
		ActiveBlock.add(block);
		ActiveBlock.add(block2);
	}
	
	public boolean RemoveBlock(PuzzleBlock block)
	{
		for(int i = 0; i < BlockMap.size(); i++)
		{
			for(int j = 0; j < BlockMap.get(i).size(); j++)
			{
				PuzzleBlock b = BlockMap.get(i).get(j);
				if(b.BlockEquals(block))
				{
					BlockMap.get(i).remove(j);
					return true;
				}
			}
		}
		
		return false;
	}
	
	public void AddBlockInMap(PuzzleBlock block)
	{
		int col = CollumnPosition(block);
		BlockMap.get(col).add(block);
	}
	
	public void AddBlock(PuzzleBlock block,int x, int y)
	{
		if(IsInField(x, y))
			Blocks[x][y] = block;
	}
	
	public void AddBlockToBottom(PuzzleBlock block)
	{
		int col = CollumnPosition(block);
		int row = RowPosition(block);
		
		int sz = BlockMap.get(col).size();
		
		int index = sz > 0 ? sz-1 : 0;
		
		if(sz > 0)
		{
			PositionBlockAtCollumnTop(block, col);
		}
		else
			block.SetPosition(block.getX(), FIELD_HEIGHT * PuzzleBlock.BLOCK_H);
		
		AddBlockInMap(block);
		
		HandleBlockClustering(block);
	}
	
	public boolean BlockReachedBottom(PuzzleBlock block)
	{
		int col = CollumnPosition(block);
		int row = RowPosition(block);
		
		int sz = BlockMap.get(col).size();
		int index = sz > 0 ? sz-1 : 0;
		
		if(sz > 0)
		{
			PuzzleBlock top = BlockMap.get(col).get(index);
			if(block.bottom() > top.top() && !top.IsInState(FallingState.FallingStateID))
				return true;
		}
		
		if(block.bottom() > FIELD_HEIGHT * PuzzleBlock.BLOCK_H)
			return true;
		
		return false;
	}
	
	public void PositionBlockAtCollumnTop(PuzzleBlock block, int col)
	{
		int colH = GetCollumnHeightInPixels(col);
		block.SetPosition(block.getX(), colH);
	}
	
	public Point PosToIndex(Point pos)
	{
		Point p = new Point(0,0);
		
		p.x = (pos.x - rect.x) / PuzzleBlock.BLOCK_W;
		p.y = FIELD_HEIGHT - ((pos.y - rect.y) / PuzzleBlock.BLOCK_H);
		
		return p;
	}
	
	public boolean IsInField(int x, int y)
	{
		if(x < 0 || x >= FIELD_WIDTH  ||
				y < 0 || y >= FIELD_HEIGHT )
			return false;
		
		return true;
	}
	
	public boolean IsBlock(int x, int y)
	{
		if(!IsInField(x, y))
			return false;
		
		if(BlockMap.get(x).size() <= y)
			return false;
		
		return true;
	}
	
	public PuzzleBlock GetBlock(int x, int y)
	{
		return BlockMap.get(x).get(y);
	}
	
	
	public void render(Graphics2D g)
	{
		for(int i = 0; i < ActiveBlock.size(); i++)
		{
			PuzzleBlock b = ActiveBlock.get(i);
			b.render(g);
		}
		
		for( int i = 0; i < BlockMap.size(); i++)
		{
			for(int j = 0; j < BlockMap.get(i).size(); j++)
			{
				BlockMap.get(i).get(j).render(g);
			}
		}
	}
	
	public void MoveActiveBlock(float xdist, float ydist)
	{
		for(int i = 0; i < ActiveBlock.size(); i++)
		{
			PuzzleBlock b = ActiveBlock.get(i);			
			b.Move(xdist, ydist);
			if(!CanMoveActiveBlockToCollumn(CollumnPosition(ActiveBlock.get(i))))
			{
				b.Move(-xdist,-ydist);
			}
		}
	}
	
	public void AddNewActiveBlock()
	{
		Point2D.Float start = new Point2D.Float(0, 0);
		PuzzleBlock block = new PuzzleBlock(start);
		
		start.x += PuzzleBlock.BLOCK_W;
		PuzzleBlock block2 = new PuzzleBlock(start);
		
		ActiveBlock.clear();
		ActiveBlock.add(block);
		ActiveBlock.add(block2);
		
	}
	public void DropActiveBlock()
	{
		for(int i = 0; i < ActiveBlock.size(); i++)
		{
			PuzzleBlock b = ActiveBlock.get(i);
			b.ChangeState(new IdleState(b));
			AddBlockToBottom(b);
		}
		
		AddNewActiveBlock();
	}
	
	public void RotateActiveBlock()
	{
		
		Point2D.Float[] dirs = {new Point2D.Float(0,-1),
								new Point2D.Float(1,0),
								new Point2D.Float(0,1),
								new Point2D.Float(-1,0)};
		
		mCurrentRot++;
		
		if(mCurrentRot > 3)
			mCurrentRot = 0;
		
		if(ActiveBlock.size() <= 0)
			return;
		
		
		
		PuzzleBlock parent = ActiveBlock.get(0);
		for(int i = 1; i < ActiveBlock.size(); i++)
		{
			PuzzleBlock b = ActiveBlock.get(i);
			b.SetPosition((int)(parent.getX() + (PuzzleBlock.BLOCK_W*i)*dirs[mCurrentRot].x),
					(int)(parent.getY() + (PuzzleBlock.BLOCK_H*i)*dirs[mCurrentRot].y));
		}
	}
	
	public boolean CanMoveBlockToCollumn(PuzzleBlock block, int col)
	{
		int row = RowPosition(block);
		int colPos = CollumnPosition(block);
		
		if(colPos > FIELD_WIDTH-1 || colPos < 0)
			return false;
		int sz = BlockMap.get(col).size();
		if(sz <= 0)
			return true;
		
		PuzzleBlock b = BlockMap.get(col).get(sz-1);
		
		if(block.bottom() >= b.top())
			return false;
		
		return true;
	}
	
	public boolean CanMoveActiveBlockToCollumn(int col)
	{
		for(int i = 0; i < ActiveBlock.size(); i++)
		{
			if(!CanMoveBlockToCollumn(ActiveBlock.get(i), col))
				return false;
		}
		return true;
	}
	
	public int NumEmptySpacesBelowBlock(PuzzleBlock block)
	{
		int num = 0;
		
		int x = CollumnPosition(block);
		int y = RowPosition(block);
		for(int i = y; i >= 0; i--)
		{
			if(!IsBlock(x, i))
				continue;
			
			if(GetBlock(x, i).Alive == false)
				num++;
		}
		
		return num;
	}
	
	public int GetCollumnHeightInPixels(int col)
	{
		int height =  (int)FieldHeightInPixels() - (BlockMap.get(col).size() * PuzzleBlock.BLOCK_H);
		
		return height;
	}
	
	public int CollumnPosition(PuzzleBlock block)
	{
		return ((block.getX() + PuzzleBlock.BLOCK_W/2)- rect.x) / PuzzleBlock.BLOCK_W;
	}
	
	public int RowPosition(PuzzleBlock block)
	{
		return FIELD_HEIGHT - (((block.top() + PuzzleBlock.BLOCK_H/2) - rect.y) / PuzzleBlock.BLOCK_H);
	}
	
	public int CollumnSize(int col)
	{
		int count = 0; 
		count = BlockMap.get(col).size();
		
		return count;
	}
	
	public int NumberOfAliveBlocksInColumn(int col)
	{
		int c = 0;
		
		for(int i = 0; i < CollumnSize(col); i++)
		{
			if(GetBlock(col, i).Alive)
			{
				c += 1;
			}
		}
		return c;
	}
	
	public float FieldHeightInPixels()
	{
		return FIELD_HEIGHT * PuzzleBlock.BLOCK_H;
	}

	@Override
	public void keyPressed(KeyEvent arg0) {
		// TODO Auto-generated method stub
		if(arg0.getKeyChar() == 'd')
		{
			for(int i = 0; i < ActiveBlock.size(); i++)
			{
				if(CanMoveActiveBlockToCollumn(CollumnPosition(ActiveBlock.get(i))+1))
					MoveActiveBlock(-PuzzleBlock.BLOCK_W, 0);
			}
		}
		
		if(arg0.getKeyChar() == 'a')
		{
			for(int i = 0; i < ActiveBlock.size(); i++)
			{
				if(CanMoveActiveBlockToCollumn(CollumnPosition(ActiveBlock.get(i))-1))
					MoveActiveBlock(-PuzzleBlock.BLOCK_W, 0);
			}
		}
		
		if(arg0.getKeyChar() == 's')
		{
			for(int i = 0; i < ActiveBlock.size(); i++)
			{
				AddBlockToBottom(ActiveBlock.get(i));
			}
		}
	}
	
	public void SetActiveBlockType(int t)
	{
		for(int i = 0; i < ActiveBlock.size(); i++)
			ActiveBlock.get(i).SetType(t);
	}

	@Override
	public void keyReleased(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	

}

