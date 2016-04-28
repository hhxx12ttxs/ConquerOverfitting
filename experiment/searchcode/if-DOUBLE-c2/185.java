package ht;
import java.util.*;
import java.math.*;

public class Hash_Table
{
	int size = 0;
	Entry[] table;
	
	int put_count = 0;
	int search_count = 0;
	int element_count = 0;
	int collision_count = 0;
	
	//Used for calculating the division factor
	int p;
	ArrayList<Integer> result;
	FindPrimeNumber f = new FindPrimeNumber();
	
	public Hash_Table(int size)
	{
		this.size = size;
		table = new Entry[this.size];
		
		result = f.find(this.size);
		p = result.get(result.size() - 1);
	}
	
	private static class Entry
	{
		long key;
		String element;
		int index;
		
		Entry next;
		
		Entry(int index, long key, String element, Entry next)
		{
			this.key = key;
			this.element = element;
			this.index = index;
			this.next = next;
		}
	}
	
	public String put(String element, String collision_mod, String hashCode_mod, String hashFunction_mod, double c1, double c2)
	{
		long key = hashCode(element, hashCode_mod);
		
		//1. Using chain to resolve collision
		if(collision_mod.equals("Chain"))
		{
			int index = hashFunction(key, hashFunction_mod);
			
			if(table[index] != null)
				collision_count++;
			
			//Make sure the key isn't already in the hash table
			for(Entry next = table[index]; next != null; next = next.next)
			{
				put_count++;
				
				if((next.element).equals(element))
				{
					String temp = next.element;
					next.element = element;
					return temp;
				}
			}
			
			element_count++;
			
			Entry next = table[index];
			table[index] = new Entry(index, key, element, next);
			return null;
		}
		
		//2. Using open address to resolve collision
		//Attention: The size of the table must be larger than the number of elements
		if(collision_mod.equals("OpenAddress"))
		{
			int i = 0;
			
			int index = 0;
			
			while(i < size - 1)
			{
				put_count++;
				
				if(hashFunction_mod.equals("Double"))
					index = (int)(((key % p + 1) + i * (key % (p - 1))) % p);
				
				if(hashFunction_mod.equals("Linear"))
					index = (int)((key % (p - 1) + i) % p);
				
				if(hashFunction_mod.equals("Quadratic"))
					index = (int)((key % (p - 1) + c1 * i + c2 * (i^2)) % p);
				
				if(table[index] != null)
				{
					if(table[index].element.equals(element))
					{
						String temp = table[index].element;
						table[index].element = element;
						return temp;
					}
					
					collision_count++;
					
					i++;
				}
					
				else
				{
					element_count++;
					
					table[index] = new Entry(index, key, element, null);
					
					break;
				}
			}
		}
		
		return null;
	}
	
	public boolean search(String element, String mod, String hashCode_mod, String hashFunction_mod, double c1, double c2)
	{
		long key = hashCode(element, hashCode_mod);
		
		if(mod.equals("Chain"))
		{
			int index = hashFunction(key, hashFunction_mod);
		
			Entry next = table[index];
			
			if(next == null)
				return false;
			
			for(; next != null; next = next.next)
			{
				search_count++;
				
				if(next != null)
				{
					if(key == next.key && element.equals(next.element))
					{
						System.out.println("Find !");
						return true;
					}
				}
			}
		}
		
		if(mod.equals("OpenAddress"))
		{
			int i = 0;
			int index = 0;
			
			for(; i < size; i++)
			{
				search_count++;
				
				if(hashFunction_mod.equals("Double"))
					index = (int)(((key % p + 1) + i * (key % (p - 1))) % p);
				
				if(hashFunction_mod.equals("Linear"))
					index = (int)((key % (p - 1) + i) % p);
				
				if(hashFunction_mod.equals("Quadratic"))
					index = (int)((key % (p - 1) + c1 * i + c2 * (i^2)) % p);
				
				if(table[index] != null)
				{	
					
					if(element.equals(table[index].element))
					{
						System.out.println("Find !");
						return true;
					}
				}
				
				else
					return false;
			}
		}
		
		return false;
	}
	
	public long hashCode(String element, String mod)
	{
		long key = 0;
		
		if(mod.equals("BKDR"))
		{
			int seed = 131;//magic number: 31, 131, 1313...
			
			for(int i = 0; i < element.length(); i++)
			{
				key = key * seed + element.charAt(i);
			}
			
			return key & 0x7fffffff;
		}
		
		if(mod.equals("SDBM"))
		{
			for(int i = 0; i < element.length(); i++)
			{
				key = element.charAt(i) + 65599 * key;
			}
			
			return key & 0x7fffffff;
		}
		
		if(mod.equals("DJB"))
		{
			key = 5381;
			
			for(int i = 0; i < element.length(); i++)
			{
				key = (key << 5) + key + element.charAt(i);
			}
			 
			return key & 0x7fffffff;
		}
		
		if(mod.equals("RS"))
		{
			int b = 378551;
			int a = 63689;
			
			for(int i = 0; i < element.length(); i++)
			{
				key = key * a + element.charAt(i);
				a *= b;
			}
			
			return key & 0x7fffffff;
		}
		
		if(mod.equals("JS"))
		{
			key = 1315423911;
			
			for(int i = 0; i < element.length(); i++)
			{
				key ^= ((key << 5) + element.charAt(i) + (key >> 2));
			}
			
			return key & 0x7fffffff;
		}
		
		if(mod.equals("ELF"))
		{
			long x = 0;
			
			for(int i = 0; i < element.length(); i++)
			{
				key = (key << 4) + element.charAt(i);
				
				if((x = key & 0xf0000000l) != 0)
				{
					key ^= (x >> 24);
					
					key &= ~x;
				}
			}
			
			return (key & 0x7fffffff);
		}
		
		return 0;
	}
	
	public int hashFunction(long key, String mod)
	{
		//Division: Using Eeatosthese algorithm to find the greatest prime number less than size
		//Use this prime number to be the division parameter
		if(mod.equals("Division"))
		{	
			return (int)(key % p);
		}
		
		//Multiplication: Using Knuth's Suggestion
		if(mod.equals("Multiplication"))
		{
			double a = (Math.sqrt(5) - 1)/2;
			
			return (int)(p * ((key * a) % 1));
		}
		
		return 0;
	}
	
	public int getPutCount()
	{
		int pc = put_count;
		put_count = 0;
		
		return pc;
	}
	
	public int getSearchCount()
	{
		int sc = search_count;
		search_count = 0;
		
		return sc;
	}
	
	public int getElementCount()
	{
		/*
		int ec = element_count;
		element_count = 0;
		*/
		
		return element_count;
	}
	
	public int getCollisionCount()
	{
		/*
		int cc = collision_count;
		collision_count = 0;
		*/
		
		return collision_count;
	}
	
	public double avgChainLength()
	{
		double length = 0.0;
		
		Entry next;
		int hasElement = 0;
		
		for(int i = 0; i < size; i++)
		{
			if(table[i] != null)
			{
				hasElement++;
				
				next = table[i];
			
				while(next != null)
				{
					next = next.next;
					length++;
				}
			}
		}
		
		return length/hasElement;
	}
	
	public double avgProbeLength()
	{
		return ((double)put_count/(double)element_count);
	}
	
	public void chainTraversal()
	{
		Entry next;
		
		for(int i = 0; i < size; i++)
		{
			next = table[i];
			
			while(next != null)
			{
				System.out.print(i + ": " + next.element + " ");
				next = next.next;
			}
			System.out.print("\n");
		}
	}
	
	public void openAddressTraversal()
	{
		for(int i = 0; i < size; i++)
		{
			if(table[i] != null)
				System.out.println(i + ": " + table[i].element);
		}
	}
}

