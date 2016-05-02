package builder;

public class If extends DancerStatementWithStatementList
{
  private PrimTest primTest;
  private Else elseStatement;

  If(DancerProgram dancerProgram, IDGenerator idGenerator, Object sync)
  {
    super(dancerProgram, idGenerator, sync);
  }
  
  public void setPrimTest(PrimTest primTest)
  {
    this.primTest = primTest;
    elseStatement = null;
  }
  
  public void setElse(Else elseStatement)
  {
    this.elseStatement = elseStatement;
  }

  @Override
  protected void evalCode(boolean isStep)
  {
    if (primTest.eval())
    {
      for (DancerStatement statement : statements)
        statement.eval(isStep);
    }
    else
    {
      if (elseStatement == null)
        return;
      elseStatement.eval(isStep);
    }
  }
  
  @Override
  public void run()
  {
    if (primTest.eval())
    {
      dancerProgram.helper(true);
      synchronized (sync)
      {
        try
        {
          sync.wait();
        } catch (InterruptedException e) {
        }
      }
      for (DancerStatement statement : statements)
        statement.run();
    }
    else
    {
      dancerProgram.helper(false);
      if (elseStatement == null)
        return;
      elseStatement.run();
    }
  }
}

