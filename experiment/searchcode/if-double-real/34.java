public void inc() { setReal( getRealDouble() + 1 ); }
@Override
public void dec() { setReal( getRealDouble() - 1 ); }

@Override
public void set( final T c ){ setReal( c.getRealDouble() ); }

