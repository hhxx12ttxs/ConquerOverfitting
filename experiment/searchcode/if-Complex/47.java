
// Klasse zum Speichern von Objekt-Matrizen
class JObjectMatrix {
   // Matrizendaten
   JObjectVector l1;       Complex a , b , c ;
   JObjectVector l2;       Complex d , e , f ;
   JObjectVector l3;       Complex g , h , i ;

   JObjectVector                   r1, r2, r3;

   public JObjectMatrix(double  a, double  b, double  c
                       ,double  d, double  e, double  f
                       ,double  g, double  h, double  i) { this(new Complex(a), new Complex(b), new Complex(c) 
                                                               ,new Complex(d), new Complex(e), new Complex(f) 
                                                               ,new Complex(g), new Complex(h), new Complex(i)); }
   public JObjectMatrix(Complex a, Complex b, Complex c
                       ,Complex d, Complex e, Complex f
                       ,Complex g, Complex h, Complex i) {

      this.a = a.copy();   this.b = b.copy();   this.c = c.copy();
      this.d = d.copy();   this.e = e.copy();   this.f = f.copy();
      this.g = g.copy();   this.h = h.copy();   this.i = i.copy();

      // Die Zeilen- und Spaltenvektoren sollen benutzt werden k�nnen
      l1 = new JObjectVector(0,0,0);   l1.x = a; l1.y = b; l1.z = c;
      l2 = new JObjectVector(0,0,0);   l2.x = d; l2.y = e; l2.z = f;
      l3 = new JObjectVector(0,0,0);   l3.x = g; l3.y = h; l3.z = i;

      r1 = new JObjectVector(0,0,0);   r1.x = a; r1.y = d; r1.z = g;
      r2 = new JObjectVector(0,0,0);   r2.x = b; r2.y = e; r2.z = h;
      r3 = new JObjectVector(0,0,0);   r3.x = c; r3.y = f; r3.z = i;
   }

   public JObjectMatrix copy() {
      // Kopie erstellen
      return new JObjectMatrix( a, b, c
                              , d, e, f
                              , g, h, i);
   }

   // Vektoroperationen (Addition, Subtration, Negation und Skalierung) [Inplace]
   // -----------------------------------------------------------------------------
   public JObjectMatrix add(JObjectMatrix o) { a.add(o.a); b.add(o.b); c.add(o.c);
                                               d.add(o.d); e.add(o.e); f.add(o.f);
                                               g.add(o.g); h.add(o.h); i.add(o.i); return this; }
   public JObjectMatrix sub(JObjectMatrix o) { a.sub(o.a); b.sub(o.b); c.sub(o.c);
                                               d.sub(o.d); e.sub(o.e); f.sub(o.f);
                                               g.sub(o.g); h.sub(o.h); i.sub(o.i); return this; }
   public JObjectMatrix neg()                { a.neg();    b.neg();    c.neg();    
                                               d.neg();    e.neg();    f.neg();   
                                               g.neg();    h.neg();    i.neg();    return this; }

   public JObjectMatrix mul(Complex x)       { a.mul( x ); b.mul( x ); c.mul( x );
                                               d.mul( x ); e.mul( x ); f.mul( x );
                                               g.mul( x ); h.mul( x ); i.mul( x ); return this; }
   public JObjectMatrix div(Complex x)       { a.div( x ); b.div( x ); c.div( x ); 
                                               d.div( x ); e.div( x ); f.div( x ); 
                                               g.div( x ); h.div( x ); i.div( x ); return this; }

   // Matrix-Vektor-Multiplikation
   public JObjectVector mul(JObjectVector v) { return new JObjectVector( v.doScalar(l1)
                                                                       , v.doScalar(l2)
                                                                       , v.doScalar(l3)); }

   // Vektoroperationen (Addition, Subtration, Negation und Skalierung) [Statisch]
   // -----------------------------------------------------------------------------
   public static JObjectMatrix add(JObjectMatrix o
                                  ,JObjectMatrix p) { return new JObjectMatrix(Complex.add(o.a, p.a), Complex.add(o.b, p.b), Complex.add(o.c, p.c)
                                                                              ,Complex.add(o.d, p.d), Complex.add(o.e, p.e), Complex.add(o.f, p.f)
                                                                              ,Complex.add(o.g, p.g), Complex.add(o.h, p.h), Complex.add(o.i, p.i)); }
   public static JObjectMatrix sub(JObjectMatrix o
                                  ,JObjectMatrix p) { return new JObjectMatrix(Complex.sub(o.a, p.a), Complex.sub(o.b, p.b), Complex.sub(o.c, p.c)
                                                                              ,Complex.sub(o.d, p.d), Complex.sub(o.e, p.e), Complex.sub(o.f, p.f)
                                                                              ,Complex.sub(o.g, p.g), Complex.sub(o.h, p.h), Complex.sub(o.i, p.i)); }
   
   public static JObjectMatrix neg(JObjectMatrix o) { return new JObjectMatrix(Complex.neg(o.a)     , Complex.neg(o.b)     , Complex.neg(o.c)     
                                                                              ,Complex.neg(o.d)     , Complex.neg(o.e)     , Complex.neg(o.f)     
                                                                              ,Complex.neg(o.g)     , Complex.neg(o.h)     , Complex.neg(o.i)     ); }

   public static JObjectMatrix mul(JObjectMatrix o
                                  ,Complex       c) { return new JObjectMatrix(Complex.mul(o.a,  c ), Complex.mul(o.b,  c ), Complex.mul(o.c,  c )
                                                                              ,Complex.mul(o.d,  c ), Complex.mul(o.e,  c ), Complex.mul(o.f,  c )
                                                                              ,Complex.mul(o.g,  c ), Complex.mul(o.h,  c ), Complex.mul(o.i,  c )); }
   public static JObjectMatrix div(JObjectMatrix o
                                  ,Complex       c) { return new JObjectMatrix(Complex.div(o.a,  c ), Complex.div(o.b,  c ), Complex.div(o.c,  c )
                                                                              ,Complex.div(o.d,  c ), Complex.div(o.e,  c ), Complex.div(o.f,  c )
                                                                              ,Complex.div(o.g,  c ), Complex.div(o.h,  c ), Complex.div(o.i,  c )); }
   // Matrix aus einer Vektormultiplikation erstellen
   public static JObjectMatrix mul(JObjectVector a
                                  ,JObjectVector b) { return new JObjectMatrix(Complex.mul(a.x, b.x), Complex.mul(a.x, b.y), Complex.mul(a.x, b.z)
                                                                              ,Complex.mul(a.y, b.x), Complex.mul(a.y, b.y), Complex.mul(a.y, b.z)
                                                                              ,Complex.mul(a.z, b.x), Complex.mul(a.z, b.y), Complex.mul(a.z, b.z)); }
   // Matrix-Vektor-Multiplikation
   public static JObjectVector mul(JObjectMatrix m
                                  ,JObjectVector v) { return new JObjectVector( v.doScalar(m.l1)
                                                                              , v.doScalar(m.l2)
                                                                              , v.doScalar(m.l3)); }
   // Pseudo-"L�nge" der Matrix berechnen
   // -----------------------------------------------------------------------------
   public Complex len() {
      Complex cSum =   Complex.sqr(a) ;            cSum.add(Complex.sqr(b));        cSum.add(Complex.sqr(c));
              cSum.add(Complex.sqr(d));            cSum.add(Complex.sqr(e));        cSum.add(Complex.sqr(f)); 
              cSum.add(Complex.sqr(g));            cSum.add(Complex.sqr(h));        cSum.add(Complex.sqr(i));
      return  cSum.sqrt();
   }

   public Complex det() {
      Complex cSum =   Complex.mul(Complex.mul(a, e), i) ;
              cSum.add(Complex.mul(Complex.mul(b, f), g));
              cSum.add(Complex.mul(Complex.mul(c, d), h));
              cSum.sub(Complex.mul(Complex.mul(g, e), c));
              cSum.sub(Complex.mul(Complex.mul(h, f), a));
              cSum.sub(Complex.mul(Complex.mul(i, d), b));
      return  cSum;
   }

   // Normalisierung ?!
   // -----------------------------------------------------------------------------
   public void toUnit() {
      Complex  l = len();
      if (    !l.isZero()) this.div(l);
   }

   public String toStringLin() {
      return a.toStringLin() + "    :    " + b.toStringLin() + "    :    " + c.toStringLin() + "\n" +
             d.toStringLin() + "    :    " + e.toStringLin() + "    :    " + f.toStringLin() + "\n" +
             g.toStringLin() + "    :    " + h.toStringLin() + "    :    " + i.toStringLin() + "\n";
             
   }
}


