/*

Polynomial Library
------------------

Copyright (c) 2012, Ebrahim Ashrafizadeh

Permission to use, copy, modify, and/or distribute this software for any purpose with or 
without fee is hereby granted, provided that the above copyright notice and this permission 
notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES WITH REGARD TO THIS 
SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL 
THE AUTHOR BE LIABLE FOR ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES 
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, 
NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE 
OF THIS SOFTWARE.

*/

package com.github.beash.polynomial;

import java.util.*;
import java.lang.Math;

// a term consists of a coefficient and an exponent.
// an exponent must be >= 0.
class Term
{
    private double coeff;
    private int exp;

    public Term(double coeff, int exp)
    {
        if (exp < 0)
            throw new IllegalArgumentException("Exponents must be >= 0.");
        else
        {
            this.coeff = coeff;
            this.exp = exp;
        }
    }

    public Term(double coeff)
    {
        this.coeff = coeff;
        this.exp = 0;
    }

    public double getCoeff()
    {
        return this.coeff;
    }

    public int getExp()
    {
        return this.exp;
    }

    public void setCoeff(double coeff)
    {
        this.coeff = coeff;
    }

    public void setExp(int exp)
    {
        if (exp < 0)
            throw new IllegalArgumentException("Exponents must be >= 0.");
        else
            this.exp = exp;
    }

    // Note that equals(Object o) hasn't been used as Term is a mutable
    // class and overriding equals with mutable fields can cause problems
    // with java collections. 
    public boolean equalContents(Term t)
    {
        if (t == this)
            return true;
        else
        {
            if (t.getCoeff() == this.coeff && t.getExp() == this.exp)
                return true;
            else
                return false;
        }
    }
}

// a Polynomial is a finite set of Term objects
public class Polynomial
{
    // the polynomial (stored from highest exponent term to lowest).
    // note that constants have an exponent of zero.
    protected List<Term> pn;

    private Polynomial()
    {
        this.pn = new ArrayList<Term>();
    }

    public Polynomial(double coeff, int exp)
    {
        this.pn = new ArrayList<Term>();
        this.pn.add(new Term(coeff, exp));
    }

    public Polynomial(double coeff)
    {
        this.pn = new ArrayList<Term>();
        this.pn.add(new Term(coeff));
    }

    public Polynomial(double coeffs[], int exps[])
    {
        if ((coeffs.length != exps.length) || coeffs.length == 0 || exps.length == 0)
        {
            throw new IllegalArgumentException("Coefficient and exponent array inputs must have the same non-zero size!");
        }
        else
        {
            double transposed [][] = new double [exps.length][2];

            for (int i = 0; i < exps.length; i++)
            {
                transposed[i][0] = (double)exps[i];
                transposed[i][1] = coeffs[i];
            }

            java.util.Arrays.sort(transposed, new java.util.Comparator<double[]>() {
                public int compare(double[] a, double[] b) {
                    return (int)(b[0] - a[0]);
                }
            });

            this.pn = new ArrayList<Term>();
            for (double[] element : transposed)
            {
                if (element[1] != 0)
                    this.pn.add(new Term(element[1],(int)element[0]));
            }

            collapse();
        }
    }

    // Important note: If using the 2d array constructor, you must ensure
    // that the input format is pairs of exponents and coefficients.
    // for example x^3 + 2x^2 would be { {3,1} , {2,2} }.
    public Polynomial(double terms[][])
    {
        if (terms.length == 0)
        {
            throw new IllegalArgumentException("2d array input must have a non-zero size!");
        }

        java.util.Arrays.sort(terms, new java.util.Comparator<double[]>() {
            public int compare(double[] a, double[] b) {
                return (int)(b[0] - a[0]);
            }
        });

        this.pn = new ArrayList<Term>();
        for (double[] element : terms)
        {
            if (element[1] != 0)
                this.pn.add(new Term(element[1],(int)element[0]));
        }

        collapse();
    }

    // refactor the polynomial to condense terms (e.g. x^2 + x^2 = 2x^2)
    protected void collapse()
    {
        boolean stop = false;
        int curr_index = 0;

        while(stop == false && getSize() > 1)
        {
            if (this.pn.get(curr_index).getExp() == this.pn.get(curr_index+1).getExp())
            {
                this.pn.get(curr_index).setCoeff(this.pn.get(curr_index).getCoeff() + this.pn.get(curr_index+1).getCoeff());
                this.pn.remove(curr_index+1);
            }
            else
                curr_index++;
            
            if (curr_index == getSize() - 1)
                stop = true;
        }
    }

    public int getSize()
    {
        return this.pn.size();
    }

    // checks to see if two polynomials are equal
    public boolean equalContents(Polynomial p)
    {
        if (this == p)
            return true;
        else
        {
            if (this.getSize() != p.getSize())
                return false;
            else
            {
                if (this.getSize() == 0)
                    return true;
                else
                {
                    for (int i = 0; i < getSize(); i++)
                    {
                        if (!(this.pn.get(i).equalContents(p.pn.get(i))))
                            return false;
                    }
                    return true;
                }
            }
        }
    }

    // returns -this.pn as a seperate polynomial object
    public Polynomial flipSigns()
    {
        if (getSize() == 0)
            return this;
        else
        {
            Polynomial result = new Polynomial();
            for (int i = 0; i < getSize(); i++)
                result.pn.add(new Term(-this.pn.get(i).getCoeff(), this.pn.get(i).getExp()));
            return result;
        }
    }

    // add this.pn to a polynomial "p" and return the result as a seperate polynomial object 
    public Polynomial add(Polynomial p)
    {
        if (p.getSize() == 0)
            return this;
        else
        {
            if (this.getSize() == 0)
                return p;
            else
            {
                int this_index = 0;
                int p_index = 0;
                Polynomial result = new Polynomial();

                while (this_index < this.getSize() && p_index < p.getSize())
                {
                    if (this.pn.get(this_index).getExp() == p.pn.get(p_index).getExp())
                    {
                        if (this.pn.get(this_index).getCoeff() + p.pn.get(p_index).getCoeff() != 0)                   
                            result.pn.add(new Term(this.pn.get(this_index).getCoeff() + p.pn.get(p_index).getCoeff(), this.pn.get(this_index).getExp()));

                        this_index++;
                        p_index++;
                    }
                    else
                    {
                        if (this.pn.get(this_index).getExp() > p.pn.get(p_index).getExp())
                        {
                            result.pn.add(new Term(this.pn.get(this_index).getCoeff(), this.pn.get(this_index).getExp()));
                            this_index++;
                        }
                        else
                        {
                            result.pn.add(new Term(p.pn.get(p_index).getCoeff(), p.pn.get(p_index).getExp()));
                            p_index++;
                        }
                    }
                }
                
                if (this_index == this.getSize())
                {
                    while (p_index < p.getSize())
                    {
                        result.pn.add(new Term(p.pn.get(p_index).getCoeff(), p.pn.get(p_index).getExp()));
                        p_index++;
                    }
                }
                else
                {
                    while (this_index < this.getSize())
                    {
                        result.pn.add(new Term(this.pn.get(this_index).getCoeff(), this.pn.get(this_index).getExp()));
                        this_index++;
                    }
                }
                return result;
            }
        }
    }

    // subtract a polynomial "p" from this.pn and return the result as a seperate polynomial object
    public Polynomial subtract(Polynomial p)
    {
        Polynomial flipped = p.flipSigns();
        return this.add(flipped);
    }

    // multiply this.pn by a polynomial "p" and return the result as a seperate polynomial object
    public Polynomial multiply(Polynomial p)
    {
        if (this.getSize() == 0)
            return this;
        else
        {
            if (p.getSize() == 0)
                return p;
            else
            {
                int index = 0;
                double full_result[][] = new double[this.getSize()*p.getSize()][2];
                
                for (int i = 0; i < this.getSize(); i++)
                {
                    for (int j = 0; j < p.getSize(); j++)
                    {
                        full_result[index][0] = (double)this.pn.get(i).getExp() + (double)p.pn.get(j).getExp();
                        full_result[index][1] = this.pn.get(i).getCoeff() * p.pn.get(j).getCoeff();
                        index++;
                    }
                }

                return (new Polynomial(full_result));
            }
        }
    }

    // return the derivative of this.pn as a seperate polynomial
    public Polynomial differentiate()
    {
        if (getSize() == 0)
            return this;
        else
        {
            double term;
            Polynomial result = new Polynomial();
        
            for (int i = 0; i < getSize(); i++)
            {
                if (this.pn.get(i).getExp() != 0)
                {
                    term = (double)this.pn.get(i).getExp() * this.pn.get(i).getCoeff();
                    result.pn.add(new Term(term, this.pn.get(i).getExp() - 1));
                }
            }

            return result;
        }
    }

    // return the indefinite integral of this.pn as a seperate polynomial
    public Polynomial integrate()
    {
        if (getSize() == 0)
            return this;
        else
        {
            double term;
            Polynomial result = new Polynomial();
        
            for (int i = 0; i < getSize(); i++)
            {
                term = this.pn.get(i).getCoeff() / ((double)(this.pn.get(i).getExp() + 1));
                result.pn.add(new Term(term, this.pn.get(i).getExp() + 1));
            }

            return result;
        }
    }

    // evaluate and return this.pn(x)
    public double evaluate(double x)
    {
        if (getSize() == 0)
            throw new NullPointerException("Error: Attempted to evaluate a null polynomial.");
        else
        {
            double result = 0;

            for (int i = 0; i < getSize(); i++)
            {
                result += Math.pow(x, (double)this.pn.get(i).getExp()) * this.pn.get(i).getCoeff(); 
            }

            return result;
        }
    }

    @Override
    public String toString()
    {
        if (getSize() == 0)
            return "0 (Empty Polynomial)";        
        else
        {
            StringBuilder result = new StringBuilder();
            boolean is_neg;

            for (int i = 0; i < getSize(); i++)
            {
                is_neg = (this.pn.get(i).getCoeff() > 0) ? false : true;

                if (i == 0)
                {
                    if (Math.abs(this.pn.get(i).getCoeff()) == 1.0)
                    {
                        if (is_neg)
                            result.append("-");
                    }
                    else
                    {
                        if (is_neg)
                            result.append("-" + Math.abs(this.pn.get(i).getCoeff()));
                        else
                            result.append(this.pn.get(i).getCoeff());
                    }
                }
                else
                {
                    if (Math.abs(this.pn.get(i).getCoeff()) != 1.0)
                    {
                        if (is_neg)
                            result.append("- " + Math.abs(this.pn.get(i).getCoeff()));
                        else
                            result.append("+ " + this.pn.get(i).getCoeff());
                    }
                    else
                    {
                        if (is_neg)
                            result.append("- ");
                        else
                            result.append("+ ");
                    }
                }
                
                if (this.pn.get(i).getExp() > 1)
                    result.append("x^" + this.pn.get(i).getExp() + " ");
                else
                {   
                    if (this.pn.get(i).getExp() == 1)
                        result.append("x ");
                    else
                    {
                        if (Math.abs(this.pn.get(i).getCoeff()) == 1.0)
                            result.append("1.0");
                    }
                }
            }
        
            return result.toString();
        }
    }
}

