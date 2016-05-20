import java.lang.StringBuilder;
import java.util.ArrayList;
import java.util.Arrays;

/**
FlatTrie: determines, char by char, whether an identifier
	matched or needed to be saved. 
Has key & normal modes. That changes the flag output
*/
public class FlatTrie extends Storage
{
	private Alphabet sigma;
	private final String end;
	private T_Flag state;
	private int[] first_chars; // in an identifier
	private StringBuilder rest_stacked; // other chars with flag
	private ArrayList<Integer> skip_list; // jump indicies for above
	private int next_open;
	private int focus;
	private final int k_spot = 0;

	public FlatTrie( String first_char_alphabet, String end_flags )
	{
		int conspicuous_consumption = 3000; // got to be enough
		sigma = new Alphabet( first_char_alphabet );
		end = end_flags;
		state = T_Flag.initial;
		next_open = 0;
		focus = 0;
		first_chars = new int[ sigma.length() ];
		rest_stacked = new StringBuilder( conspicuous_consumption );
		skip_list = new ArrayList<Integer>( conspicuous_consumption );

		overcome_stl_limitation( conspicuous_consumption - 1 );
	}
	
	// specifically, that I can't update the unallocated indicies otherwise
	private void overcome_stl_limitation( int gluttony )
	{
		for ( int ind = first_chars.length - 1; ind >=0; ind-- )
			first_chars[ ind ] = -1;
		for ( ; gluttony >= 0; gluttony-- )
		{
			skip_list.add( 0 );
			rest_stacked.append( ' ' );
		}
	}

	@Override
	public T_Flag determine_char( char to_validate )
	{
		switch( state )
		{
			case initial :
				return check_initial( to_validate );
			case saving :
				return save_char( to_validate );
			case checking :
				return check_down_the_trie( focus, to_validate ); // will recur
			default :
				return T_Flag.checking;
		}
	}

	// check first_chars for of_new
	private T_Flag check_initial( char of_new )
	{
		int index = sigma.stored_in( of_new );
		if ( valid_symbol( index ) )
		{
			if ( been_seen( index ) )
			{
				state = T_Flag.checking;
				return first_used( index );
			}
			else
			{
				state = T_Flag.saving;
				return save_first( index, of_new );
			}
		}
		else
			return T_Flag.checking; // bad client: bad output
	}

	// alphabet's way of expressing 'x not in sigma'
	private boolean valid_symbol( int been_set )
	{
		return been_set > -1;
	}

	// first_char[0] is valid, so had to init with -1
	private boolean been_seen( int first_index )
	{
		return first_chars[ first_index ] >= 0;
	}

	// so start checking from the second char
	private T_Flag first_used( int where )
	{
		focus = first_chars[ where ];
		return T_Flag.f_seen;
	}

	// save index to switch to
	private T_Flag save_first( int look_here, char is_new )
	{
		first_chars[ look_here ] = next_open;
		return T_Flag.f_unseen;
	}

	private T_Flag save_char( char ready )
	{
		rest_stacked.setCharAt( next_open, ready );
		next_open++;
		return T_Flag.f_unseen;
	}

	// it matches, there's a branch, or I start saving
	private T_Flag check_down_the_trie( int skip_ind, char hmm )
	{
		char presently = rest_stacked.charAt( skip_ind );
		if ( hmm == presently )
			return compare_more_later( skip_ind );
		else if ( more_to_try( skip_ind ) )
			return check_down_the_trie( skip_list.get( skip_ind ), hmm );
		else
		{
			state = T_Flag.saving;
			skip_list.set( skip_ind, next_open );
			return save_char( hmm );
		}
	}

	private T_Flag compare_more_later( int skip_ind )
	{
			focus = skip_ind + 1;
			return T_Flag.f_seen;
	}

	// for lexer: diff flag signifies *, seen signifies @, unseen signifies ?
	@Override
	public T_Flag check_flag( char a_flag )
	{
		T_Flag previous = state;
		state = T_Flag.initial;
		if ( a_flag == end.charAt( k_spot ) )
		{
			save_char( a_flag );
			return T_Flag.f_key;
		}
		else if ( previous == T_Flag.saving )
		{
			save_char( a_flag );
			return T_Flag.f_unseen;
		}
		else // checking
			return recur_for_flag( focus, a_flag );
	}

	// only called when id ended in checking state, no saving applicable
	private T_Flag recur_for_flag( int looking, char clients )
	{
		char previous = rest_stacked.charAt( looking );
		if ( clients == previous )
			return T_Flag.f_seen;
		else if ( previous == end.charAt( k_spot ) )
			return T_Flag.f_key;
		else if ( ! more_to_try( looking ) )
		{
			rest_stacked.setCharAt( next_open, clients );
			skip_list.set( looking, next_open );
			next_open++;
			return T_Flag.f_unseen;
		}
		else
			return recur_for_flag( skip_list.get( looking ), clients );
	}

	// skip_list is only for indices past 0, else unallocated
	private boolean more_to_try( int skip_ind )
	{
		return skip_list.get( skip_ind ) > 0 ;
	}

	@Override
	public boolean in_alphabet( char thingy ) // for lexer
	{
		return sigma.includes( thingy );
	}

	// print internals according to spec
	public void reveal_thyself()
	{
		System.out.println( "\nTransition List:" );
		String letras = sigma.get_letters();
		emit_alphabet( letras );
		emit_database( ); // saved ids
	}

	// just the alphabet, not sorted, but indicies matching
	private void emit_alphabet( String letras )
	{
		int end_i = letras.length();
		char[] bits = letras.toCharArray();
		Arrays.sort( bits );
		int start = 0;
		int iterations = 5;
		int mid_lim = end_i / iterations;
		int offset = end_i / iterations;
		for ( int times = iterations; times > 0; times-- )
		{
			System.out.print( '\t' );
			for ( int ind = start; ind < mid_lim; ind++ )
			{
				System.out.printf( "%4c ", bits[ ind ] );
			}
			System.out.printf( "%nswitch\t" );
			int nn;
			for ( int ind = start; ind < mid_lim; ind++ )
			{
				nn = sigma.stored_in( bits[ ind ] );
				System.out.printf( "%4d|", first_chars[ nn ] );
				start++;
			}
			mid_lim += offset;
			System.out.printf( "%n%n" );
		}
	}

	// run printing of rest_stacked and skip_list
	private void emit_database( )
	{
		int new_low = 0;
		int upper = rest_stacked.lastIndexOf( "?" ) + 1;
		String storage = rest_stacked.substring( 0, upper );
		upper = storage.length() - 1;
		int offset = 15;
		int times = upper / offset;
		for ( int this_time = times; this_time > 0; this_time-- )
			new_low = print_sans_sigma( new_low, offset, storage );
		capture_last_few( storage, upper, offset );
	}

	// print most of them
	private int print_sans_sigma( int new_low, int offset, String storage )
	{
		int mid_lim = offset + new_low;
		System.out.print( '\t' );
		for ( int step = new_low; step <= mid_lim; step++ )
			System.out.printf( "%4d|", step );
		System.out.printf( "%nrest\t" );
		for ( int step = new_low; step <= mid_lim; step++ )
			System.out.printf( "%4c|", storage.charAt( step ) );
		System.out.printf( "%nskip\t" );
		for ( int step = new_low; step <= mid_lim; step++ )
			sys_print_skip( step );
		System.out.printf( "%n%n" );
		return mid_lim;
	}

	// for skip_list, print empty spot or a skipping index 
	private void sys_print_skip( int ind )
	{
		boolean used = skip_list.get( ind ) > 0;
		String either = used ? skip_list.get( ind ).toString() : "_";
		System.out.printf( "%4s|", either );
	}

	// print a line smaller than the offset
	private void capture_last_few( String storage, int upper, int times )
	{
		int offset = upper / times;
		int ended_at = offset * times;
		int remaining = upper - ended_at;
		if ( remaining == 0 )
			return;
		while ( ended_at + offset < upper )
			ended_at = print_sans_sigma( ended_at, offset, storage );
		// capture last, incomplete char row
		print_sans_sigma( ended_at, upper - ended_at, storage );
	}

	// where alphabet is longer than rest_stacked, take care
	public void show_tiny()
	{
		show_small_alphabet();
		show_few_ids();
	}

	// why am I fighting this so hard? to get the indicies to match
	private void show_small_alphabet()
	{
		String arbitrary_order = sigma.get_letters();
		int len = arbitrary_order.length();
		IterableString letras = new IterableString( arbitrary_order );
		System.out.printf( "%nindex\t" );
		for ( int ind = 0; ind < len; ind++ )
		{
			System.out.printf( "%3d|", ind );
		}
		System.out.printf( "%nalpha\t" );
		for ( Character individ : letras )
		{
			System.out.printf( "%3c|", individ );
		}
		System.out.printf( "%njump\t" );
		// will fill with first_char indicies matching sigma;s actual storage order
		int[] unsorted = new int[ len ];
		int ind = 0;
		for ( Character individ : letras )
		{
			unsorted[ ind++ ] = sigma.stored_in( individ ); // has
		}
		int nn;
		for ( Character gah : letras )
		{
			nn = sigma.stored_in( gah );
			if ( nn >= first_chars.length ) // why is nn 13?
				nn = first_chars.length - 1;
			System.out.printf( "%3d|", first_chars[ nn ] );
			//System.out.printf( "%3d|", first_chars[ ind++] );//unsorted[ind++] ] );
		}
		//for ( int uu = 0; uu < len; uu++ )
		//	System.out.printf( "%3d|", unsorted[ uu ] );
		//System.out.print( "\n\t" );
		/*
alpha	f|  d|  u|  e|  t|  b|  r|  c|  n|  _|  l|  y|  i|
jump	6| 11|  1| 10|  2|  7|  9|  0|  3| 12|  4|  8| 13|
		0| -1| -1|  3|  4| -1| -1|  8| -1| 10| -1| -1| -1|
	  * /
		// I'm saying, get the firstchar cell values of the letters
		// in the order of their arbitrary_order index.
		for ( int outer_ind = 0; outer_ind < len; outer_ind++ )
		{
			for ( int search = 0; search < len; search++ )
			{
				if ( unsorted[search] == outer_ind )
				System.out.printf( "%3d|", first_chars[ unsorted[ search ] ] );
			}
		}/*
		System.out.print( "\n\t" );
		ind = 0;
		for ( Character gah : letras )
		{
			System.out.printf( "%3d|", first_chars[ ind++] );//unsorted[ind++] ] );
		}*/
		System.out.printf( "%n%n" );
	}

	// just print without cutting into pieces
	private void show_few_ids()
	{
		String id_chars = rest_stacked.substring( 0 ,
						rest_stacked.lastIndexOf( "?" ) + 1 );
		int len = id_chars.length();
		IterableString saved = new IterableString( id_chars );
		System.out.printf( "ind\t" );
		for ( int nn = 0; nn < len; nn++ )
			System.out.printf( "%3d|", nn );
		System.out.printf( "%nrest\t" );
		for ( Character nn : saved )
			System.out.printf( "%3c|", nn );
		System.out.printf( "%nskip\t" );
		for ( int nn = 0; nn < len; nn++ )
			System.out.printf( "%3d|", skip_list.get( nn ) );
	}

	public boolean t_matches_state( T_Flag client_thinks )
	{ // for tests
		return state == client_thinks;
	}

	// > did char save in expected spot? 
	public boolean t_initial_index( char a2z, int expected )
	{
		int was = sigma.stored_in( a2z );
		//System.out.println( " is " + Integer.toString( !valid_symbol(was)
		//				? was : first_chars[ was ] ) + ": thought " + Integer.toString( expected ) );
		return ( valid_symbol(was) ? first_chars[ was ] : was ) == expected;
	}

	// > boolean to char/index pair
	public boolean t_char_in_rest_at( char thinking, int spot )
	{
		//System.out.print( " " + rest_stacked.charAt( spot ) + ":" + Integer.toString( spot ) + '\t' );
		return rest_stacked.charAt( spot ) == thinking;
	}

	public boolean t_ind_in_skip_list( int spot, int expected )
	{
		return skip_list.get( spot ) == expected;
	}

	public void t_empty_self()
	{
		int used = rest_stacked.lastIndexOf( "" + end.charAt( k_spot + 1 ) );
		// assumes I put at least one var in.
		for ( int ind = first_chars.length - 1; ind >=0; ind-- )
			first_chars[ ind ] = -1;
		for ( ; used >= 0; used-- )
		{
			skip_list.set( used, 0 );
			rest_stacked.setCharAt( used, ' ' );
		}
		next_open = 0;
		focus = 0;
	}
}




























