      rowFFT.backtransform(data,i*rowspan,2); }}
      shuffle(data,i*rowspan,+1); }
    // Now transform half the columns as if they were complex (they are!)
    int nc = ncols/2+1;
    float d0 = data[i0];
    if (sign == 1){
  * and the next 2 values, as well as the data itself, are <b>overwritten<\/b> in 
  * order to store enough of the complex transformation in place.
      shuffle(data,i*rowspan,-1);
      // And backtransform, as if complex, to what would appear to be real values.
  *<P>
  * The physical layout in the transformed (complex) array data, of the
  * mathematical data D[i,j] is as follows:
      data[i0] = d0+data[i0+1];
  * (In fact, it can be done completely in place, but where one has to look for various

