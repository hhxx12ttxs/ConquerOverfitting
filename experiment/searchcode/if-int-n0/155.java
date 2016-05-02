package org.lispdev.parser.nodes;

import java.util.List;

import org.lispdev.parser.tokens.Token;
import org.lispdev.parser.tokens.TokenKey;
import org.lispdev.parser.tokens.TokenKeyword;
import org.lispdev.parser.tokens.TokenParam;
import org.lispdev.parser.tokens.TokenSymbol;

public class TreeWalker
{
  /**
   * Get index of next not hidden node in list.
   * @param list list of nodes
   * @param c index after which search for nodes (to get first node set c = -1)
   * @return not-hidden node or -1 if no more found
   */
  private static int getNextNotHidden(List<Node> list, int c)
  {
    if( list == null || list.size() <= c - 1 || c < -1 )
    {
      return -1;
    }
    else
    {
      for( int i = c + 1; i < list.size(); ++i )
      {
        if( !(list.get(i) instanceof Hidden ) )
        {
          return i;
        }
      }
    }
    return -1;
  }

  /**
   * Checks if the node is an atom node - i.e. not hidden and contains just
   * a single token.
   * @param n node
   */
  public static boolean isAtom(Node n)
  {
    return !( n instanceof Hidden ) && ( n.firstToken() == n.lastToken() );
  }

  /**
   * Checks if the node is a general symbol: i.e. symbol, keyword, &key, or key
   * @param n node
   */
  public static boolean isGeneralSymbol(Node n)
  {
    if( isAtom(n) )
    {
      Token tok = n.firstToken();

      if(tok instanceof TokenSymbol // symbol
            || tok instanceof TokenKey
            || tok instanceof TokenKeyword
            || tok instanceof TokenParam)
      {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks if the node is a literal - i.e. atom and not symbol.
   * @param n node
   */
  public static boolean isLiteral(Node n)
  {
    return isAtom(n) && !isGeneralSymbol(n);
  }

  /**
   * @param s sexp to examine
   * @return info about sexp, or empty is not valid
   */
  public static SexpInfo getInfo(Sexp s)
  {
    if( s == null ) return null;
    List<Node>els = s.getElements();
    if( els.size() == 0 )
    {
      return new SexpInfo(s);
    }
    Node n0 = els.get(0);
    if( isAtom(n0) )
    {
      String type = n0.text().toLowerCase();
      if( type.length() >= 3 && type.substring(0, 3).equals("def") )
      {
        String name = "bad";
        if( els.size() > 1 )
        {
          Node n1 = els.get(1);
          if( isAtom(n1) && ( n1.firstToken() instanceof TokenSymbol ) )
          {
            name = ((TokenSymbol)(n1.firstToken())).symbol;
          }
        }
        SexpInfo res = new SexpInfo(type, name.toLowerCase(), "",
            s.offset(), s.length(), els);
        return res;
      }
      else
      {
        return new SexpInfo(type,"bad","",s.offset(),s.length(), els);
      }
    }
    else
    {
      return new SexpInfo(s);
    }
  }

  /**
   * @param n
   * @return node of type Sexp which contains node n or root of the tree if
   * no such node exist
   */
  public static Sexp getSexp(Node n)
  {
    if( n instanceof TreeRoot || n == null )
    {
      return null;
    }
    if( n instanceof Sexp )
    {
      return (Sexp)n;
    }
    else
    {
      return getSexp(n.parent());
    }
  }

}

