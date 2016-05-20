//---------------------------------------------------------------------------//
// $Id: x26.java 10785 2010-01-31 20:05:17Z hezekiahcarty $
// Multi-lingual version of the first page of example 4.
//---------------------------------------------------------------------------//

//---------------------------------------------------------------------------//
// Copyright (C) 2006  Alan W. Irwin
// Copyright (C) 2006  Andrew Ross
//
// Thanks to the following for providing translated strings for this example:
// Valery Pipin (Russian)
//
// This file is part of PLplot.
//
// PLplot is free software; you can redistribute it and/or modify
// it under the terms of the GNU Library General Public License as published by
// the Free Software Foundation; version 2 of the License.
//
// PLplot is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU Library General Public License for more details.
//
// You should have received a copy of the GNU Library General Public License
// along with PLplot; if not, write to the Free Software
// Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301  USA
//---------------------------------------------------------------------------//

//  This example designed just for devices (e.g., psttfc and the
//  cairo-related devices) that use the pango and fontconfig libraries. The
//  best choice of glyph is selected by fontconfig and automatically rendered
//  by pango in way that is sensitive to complex text layout (CTL) language
//  issues for each unicode character in this example. Of course, you must
//  have the appropriate TrueType fonts installed to have access to all the
//  required glyphs.
//
//  Translation instructions: The strings to be translated are given by
//  x_label, y_label, alty_label, title_label, and line_label below.  The
//  encoding used must be UTF-8.
//
//  The following strings to be translated involve some scientific/mathematical
//  jargon which is now discussed further to help translators.
//
//  (1) dB is a decibel unit, see http://en.wikipedia.org/wiki/Decibel .
//  (2) degrees is an angular measure, see
//        http://en.wikipedia.org/wiki/Degree_(angle) .
//  (3) low-pass filter is one that transmits (passes) low frequencies.
//  (4) pole is in the mathematical sense, see
//      http://en.wikipedia.org/wiki/Pole_(complex_analysis) .  "Single Pole"
//      means a particular mathematical transformation of the filter function has
//      a single pole, see
//      http://ccrma.stanford.edu/~jos/filters/Pole_Zero_Analysis_I.html .
//      Furthermore, a single-pole filter must have an inverse square decline
//      (or -20 db/decade). Since the filter plotted here does have that
//      characteristic, it must by definition be a single-pole filter, see also
//      http://www-k.ext.ti.com/SRVS/Data/ti/KnowledgeBases/analog/document/faqs/1p.htm
//  (5) decade represents a factor of 10, see
//      http://en.wikipedia.org/wiki/Decade_(log_scale) .

//---------------------------------------------------------------------------//
// Implementation of PLplot example 26 in Java.
//---------------------------------------------------------------------------//

package plplot.examples;

import plplot.core.*;

import java.lang.Math;

class x26 {
    PLStream pls = new PLStream();

    static   String[] x_label = {
        "Frequency",
        "???????"
    };

    static   String[] y_label = {
        "Amplitude (dB)",
        "????????? (dB)"
    };

    static   String[] alty_label = {
        "Phase shift (degrees)",
        "??????? ????? (???????)"
    };

    static   String[] title_label = {
        "Single Pole Low-Pass Filter",
        "???????????? ?????-????????? ??????"
    };

    static   String[] line_label = {
        "-20 dB/decade",
        "-20 dB/???????"
    };


    public static void main( String[] args )
    {
        new x26( args );
    }

    public x26( String[] args )
    {
        int nlang, i;
        nlang = x_label.length;
        if ( ( nlang != y_label.length ) || ( nlang != alty_label.length ) ||
             ( nlang != title_label.length ) || ( nlang != line_label.length ) )
        {
            System.out.println( "Internal inconsistency in label dimensions" );
            pls.end();
            System.exit( 1 );
        }

        // Parse and process command line arguments.

        pls.parseopts( args, PLStream.PL_PARSE_FULL | PLStream.PL_PARSE_NOPROGRAM );

        // Initialize plplot.

        pls.init();
        pls.font( 2 );

        // Make log plots using different languages.

        for ( i = 0; i < nlang; i++ )
        {
            plot1( 0, x_label[i], y_label[i], alty_label[i], title_label[i],
                line_label[i] );
        }

        pls.end();
    }

// Log-linear plot.

    void plot1( int type, String x_label, String y_label, String alty_label,
                String title_label, String line_label )
    {
        int i;
        double[] freql = new double[101];
        double[] ampl  = new double[101];
        double[] phase = new double[101];
        double f0, freq;

        pls.adv( 0 );

        // Set up data for log plot.

        f0 = 1.0;
        for ( i = 0; i <= 100; i++ )
        {
            freql[i] = -2.0 + i / 20.0;
            freq     = Math.pow( 10.0, freql[i] );
            // Unbelievably, Java has no log10() that I can find...
            ampl[i]  = 20.0 * Math.log( 1.0 / Math.sqrt( 1.0 + Math.pow( ( freq / f0 ), 2. ) ) ) / Math.log( 10. );
            phase[i] = -( 180.0 / Math.PI ) * Math.atan( freq / f0 );
        }

        pls.vpor( 0.15, 0.85, 0.1, 0.9 );
        pls.wind( -2.0, 3.0, -80.0, 0.0 );

        // Try different axis and labelling styles.

        pls.col0( 1 );
        switch ( type )
        {
        case 0:
            pls.box( "bclnst", 0.0, 0, "bnstv", 0.0, 0 );
            break;
        case 1:
            pls.box( "bcfghlnst", 0.0, 0, "bcghnstv", 0.0, 0 );
            break;
        }

        // Plot ampl vs freq.

        pls.col0( 2 );
        pls.line( freql, ampl );
        pls.col0( 1 );
        pls.ptex( 1.6, -30.0, 1.0, -20.0, 0.5, line_label );

        // Put labels on.

        pls.col0( 1 );
        pls.mtex( "b", 3.2, 0.5, 0.5, x_label );
        pls.mtex( "t", 2.0, 0.5, 0.5, title_label );
        pls.col0( 2 );
        pls.mtex( "l", 5.0, 0.5, 0.5, y_label );

        // For the gridless case, put phase vs freq on same plot.

        if ( type == 0 )
        {
            pls.col0( 1 );
            pls.wind( -2.0, 3.0, -100.0, 0.0 );
            pls.box( "", 0.0, 0, "cmstv", 30.0, 3 );
            pls.col0( 3 );
            pls.line( freql, phase );
            pls.col0( 3 );
            pls.mtex( "r", 5.0, 0.5, 0.5, alty_label );
        }
    }
}

//---------------------------------------------------------------------------//
//                              End of x26.java
//---------------------------------------------------------------------------//

