package gov.lbl.superlu;

import java.util.Arrays;

import gov.lbl.superlu.Dlu_pdsp_defs.GlobalLU_t;
import gov.lbl.superlu.Dlu_pdsp_defs.pxgstrf_shared_t;
import gov.lbl.superlu.Dlu_slu_mt_util.Gstat_t;
import gov.lbl.superlu.Dlu_slu_mt_util.desc_eft_t;

import static gov.lbl.superlu.Dlu_sp_ienv.sp_ienv;
import static gov.lbl.superlu.Dlu_slu_mt_util.BADPAN;
import static gov.lbl.superlu.Dlu_slu_mt_util.BADCOL;
import static gov.lbl.superlu.Dlu_slu_mt_util.BADROW;
import static gov.lbl.superlu.Dlu_slu_mt_util.SUPER_REP;
import static gov.lbl.superlu.Dlu_slu_mt_util.TIC;
import static gov.lbl.superlu.Dlu_slu_mt_util.TOC;
import static gov.lbl.superlu.Dlu_slu_mt_util.SUPERLU_MAX;
import static gov.lbl.superlu.Dlu_slu_mt_util.EMPTY;

import static gov.lbl.superlu.Dlu.DEBUGlevel;
import static gov.lbl.superlu.Dlu.printf;
import static gov.lbl.superlu.Dlu.stdout;
import static gov.lbl.superlu.Dlu.fflush;
import static gov.lbl.superlu.Dlu.PREDICT_OPT;
import static gov.lbl.superlu.Dlu.GEMV2;
import static gov.lbl.superlu.Dlu.PROFILE;
import static gov.lbl.superlu.Dlu.DOPRINT;
import static gov.lbl.superlu.Dlu.SCATTER_FOUND;

import static gov.lbl.superlu.Dlu_await.await;

import static gov.lbl.superlu.Dlu_pdgstrf_bmod2D_mv2.pdgstrf_bmod2D_mv2;
import static gov.lbl.superlu.Dlu_pdgstrf_bmod2D.pdgstrf_bmod2D;
import static gov.lbl.superlu.Dlu_pdgstrf_bmod1D_mv2.pdgstrf_bmod1D_mv2;
import static gov.lbl.superlu.Dlu_pdgstrf_bmod1D.pdgstrf_bmod1D;


public class Dlu_pdgstrf_panel_bmod {

	static void PRINT_SPIN_TIME(double t2, int jcol, int pnum, int kcol, int where) {
	  if ( t2 > 0.001 ) {
	      printf("[%d] Panel%6d on P%2d waits s-node%6d for %8.2f msec.\n",
		     where, jcol, pnum, kcol, t2*1e3);
	      fflush(stdout);
	  }
	}

	/* comparison function used by qsort() - in decreasing order */
	static
	int numcomp(desc_eft_t a, desc_eft_t b)
	{
	    if ( a.eft < b.eft )
		return -1;
	    else if ( a.eft > b.eft )
		return 1;
	    else
		return 0;
	}

    static int first = 1, rowblk, colblk;

	static
	void
	pdgstrf_panel_bmod(
			   final int  pnum, /* process number */
			   final int  m,    /* number of rows in the matrix */
			   final int  w,    /* current panel width */
			   final int  jcol, /* leading column of the current panel */
			   final int  bcol, /* first column of the farthest busy snode*/
			   int   inv_perm_r[],/* in; inverse of the row pivoting */
			   int   etree[],     /* in */
			   int   nseg[],      /* modified */
			   int   segrep[],    /* modified */
			   int   repfnz[],    /* modified, size n-by-w */
			   int   panel_lsub[],/* modified */
			   int   w_lsub_end[],/* modified */
			   int   spa_marker[],/* modified; size n-by-w */
			   double dense[], /* modified, size n-by-w */
			   double tempv[], /* working array - zeros on input/output */
			   pxgstrf_shared_t pxgstrf_shared /* modified */
			   )
	{
	/*
	 * -- SuperLU MT routine (version 2.0) --
	 * Lawrence Berkeley National Lab, Univ. of California Berkeley,
	 * and Xerox Palo Alto Research Center.
	 * September 10, 2007
	 *
	 * Purpose
	 * =======
	 *
	 *    Performs numeric block updates (sup-panel) in topological order.
	 *    It features combined 1D and 2D blocking of the source updating s-node.
	 *    It consists of two steps:
	 *       (1) accumulates updates from "done" s-nodes.
	 *       (2) accumulates updates from "busy" s-nodes.
	 *
	 *    Before entering this routine, the nonzeros of the original A in
	 *    this panel were already copied into the SPA dense[n,w].
	 *
	 * Updated/Output arguments
	 * ========================
	 *    L[*,j:j+w-1] and U[*,j:j+w-1] are returned collectively in the
	 *    m-by-w vector dense[*,w]. The locations of nonzeros in L[*,j:j+w-1]
	 *    are given by lsub[*] and U[*,j:j+w-1] by (nseg,segrep,repfnz).
	 *
	 */
	    GlobalLU_t Glu = pxgstrf_shared.Glu;  /* modified */
	    Gstat_t Gstat = pxgstrf_shared.Gstat; /* modified */
	    int j, k, ksub;
	    int fsupc, nsupc, nsupr, nrow;
	    int kcol, krep, ksupno, dadsupno;
	    int jj;	      /* index through each column in the panel */
	    int          xsup[], xsup_end[], supno[];
	    int          lsub[], xlsub[], xlsub_end[];
	    int          repfnz_col[]; /* repfnz[] for a column in the panel */
	    double       dense_col[];  /* dense[] for a column in the panel */
	    int          col_marker[]; /* each column of the spa_marker[*,w] */
	    int          col_lsub[];   /* each column of the panel_lsub[*,w] */
	    double[]   t1, t2; /* temporary time */
	    t1 = new double[1];
	    t2 = new double[1];

	    float pmod = 0, max_child_eft = 0, sum_pmod = 0, min_desc_eft = 0;
	    float pmod_eft;
	    int   kid, ndesc = 0;


	if ( DEBUGlevel>=2 ) {
	    int dbg_addr = 0*m;
	}

	    if ( first != 0 ) {
		rowblk   = sp_ienv(4);
		colblk   = sp_ienv(5);
		first = 0;
	    }

	    xsup      = Glu.xsup;
	    xsup_end  = Glu.xsup_end;
	    supno     = Glu.supno;
	    lsub      = Glu.lsub;
	    xlsub     = Glu.xlsub;
	    xlsub_end = Glu.xlsub_end;

	if ( DEBUGlevel>=2 ) {
	    /*if (jcol >= LOCOL && jcol <= HICOL)
	    check_panel_dfs_list(pnum, "begin", jcol, *nseg, segrep);*/
	if (jcol == BADPAN)
	    printf("(%d) Enter pdgstrf_panel_bmod() jcol %d,BADCOL %d,dense_col[%d] %.10f\n",
		   pnum, jcol, BADCOL, BADROW, dense[/*dbg_addr+*/BADROW]);
	}

	    /* --------------------------------------------------------------------
	       For each non-busy supernode segment of U[*,jcol] in topological order,
	       perform sup-panel update.
	       -------------------------------------------------------------------- */
	    k = nseg[0] - 1;
	    for (ksub = 0; ksub < nseg[0]; ++ksub) {
		/*
		 * krep = representative of current k-th supernode
		 * fsupc = first supernodal column
		 * nsupc = no of columns in a supernode
		 * nsupr = no of rows in a supernode
		 */
	        krep = segrep[k--];
		fsupc = xsup[supno[krep]];
		nsupc = krep - fsupc + 1;
		nsupr = xlsub_end[fsupc] - xlsub[fsupc];
		nrow = nsupr - nsupc;

	if (PREDICT_OPT) {
		pmod = Gstat.procstat[pnum].fcops;
	}

		if ( nsupc >= colblk && nrow >= rowblk ) {
		    /* 2-D block update */
	if (GEMV2) {
		    pdgstrf_bmod2D_mv2(pnum, m, w, jcol, fsupc, krep, nsupc, nsupr,
				       nrow, repfnz, panel_lsub, w_lsub_end,
				       spa_marker, dense, tempv, Glu, Gstat);
	} else {
		    pdgstrf_bmod2D(pnum, m, w, jcol, fsupc, krep, nsupc, nsupr, nrow,
				   repfnz, panel_lsub, w_lsub_end, spa_marker,
				   dense, tempv, Glu, Gstat);
	}
		} else {
		    /* 1-D block update */
	if (GEMV2) {
		    pdgstrf_bmod1D_mv2(pnum, m, w, jcol, fsupc, krep, nsupc, nsupr,
				       nrow, repfnz, panel_lsub, w_lsub_end,
				       spa_marker, dense, tempv, Glu, Gstat);
	} else {
		    pdgstrf_bmod1D(pnum, m, w, jcol, fsupc, krep, nsupc, nsupr, nrow,
				   repfnz, panel_lsub, w_lsub_end, spa_marker,
				   dense, tempv, Glu, Gstat);
	}
		}

	if (PREDICT_OPT) {
		pmod = Gstat.procstat[pnum].fcops - pmod;
		kid = (pxgstrf_shared.pan_status[krep].size > 0) ?
		    krep : (krep + pxgstrf_shared.pan_status[krep].size);
		Gstat.desc_eft[ndesc].eft = Gstat.cp_panel[kid].est + Gstat.cp_panel[kid].pdiv;
		Gstat.desc_eft[ndesc++].pmod = pmod;
	}

	if ( DEBUGlevel>=2 ) {
	if (jcol == BADPAN)
	    printf("(%d) non-busy update: krep %d, repfnz %d, dense_col[%d] %.10e\n",
		   pnum, krep, repfnz[/*dbg_addr+*/krep], BADROW, dense[/*dbg_addr+*/BADROW]);
	}

	    } /* for each updating supernode ... */

	if ( DEBUGlevel>=2 ) {
	if (jcol == BADPAN)
	    printf("(%d) After non-busy update: dense_col[%d] %.10e\n",
		   pnum, BADROW, dense[/*dbg_addr+*/BADROW]);
	}

	    /* ---------------------------------------------------------------------
	     * Now wait for the "busy" s-nodes to become "done" -- this amounts to
	     * climbing up the e-tree along the path starting from "bcol".
	     * Several points are worth noting:
	     *
	     *  (1) There are two possible relations between supernodes and panels
	     *      along the path of the e-tree:
	     *      o |s-node| < |panel|
	     *        want to climb up the e-tree one column at a time in order
	     *        to achieve more concurrency
	     *      o |s-node| > |panel|
	     *        want to climb up the e-tree one panel at a time; this
	     *        processor is stalled anyway while waiting for the panel.
	     *
	     *  (2) Need to accommodate new fills, append them in panel_lsub[*,w].
	     *      o use an n-by-w marker array, as part of the SPA (not scalable!)
	     *
	     *  (3) Symbolically, need to find out repfnz[S, w], for each (busy)
	     *      supernode S.
	     *      o use dense[inv_perm_r[kcol]], filter all zeros
	     *      o detect the first nonzero in each segment
	     *        (at this moment, the boundary of the busy supernode/segment
	     *         S has already been identified)
	     *
	     * --------------------------------------------------------------------- */

	    kcol = bcol;
	    while ( kcol < jcol ) {
	        /* Pointers to each column of the w-wide arrays. */
		repfnz_col = repfnz;
		dense_col = dense;
		col_marker = spa_marker;
		col_lsub = panel_lsub;

		/* Wait for the supernode, and collect wait-time statistics. */
		if ( pxgstrf_shared.spin_locks[kcol] != 0 ) {
	if (PROFILE) {
		    TIC(t1);
	}
		    await( pxgstrf_shared.spin_locks[kcol] );

	if (PROFILE) {
		    TOC(t2, t1[0]);
		    Gstat.panstat[jcol].pipewaits++;
		    Gstat.panstat[jcol].spintime += t2[0];
		    Gstat.procstat[pnum].spintime += t2[0];
	if (DOPRINT) {
		    PRINT_SPIN_TIME(t2[0], jcol, pnum, kcol, 1);
	}
	}
		}

	        /* Find leading column "fsupc" in the supernode that
	           contains column "kcol" */
		ksupno = supno[kcol];
		fsupc = kcol;

	if ( DEBUGlevel>=2 ) {
		/*if (jcol >= LOCOL && jcol <= HICOL)    */
	  if ( jcol==BADCOL )
	    printf("(%d) pdgstrf_panel_bmod[1] kcol %d, ksupno %d, fsupc %d\n",
		   pnum, kcol, ksupno, fsupc);
	}

		/* Wait for the whole supernode to become "done" --
		   climb up e-tree one column at a time */
		do {
		    krep = SUPER_REP( xsup_end, ksupno );
		    kcol = etree[kcol];
		    if ( kcol >= jcol ) break;
		    if ( pxgstrf_shared.spin_locks[kcol] != 0 ) {
	if (PROFILE) {
			TIC(t1);
	}
			await ( pxgstrf_shared.spin_locks[kcol] );

	if (PROFILE) {
			TOC(t2, t1[0]);
			Gstat.panstat[jcol].pipewaits++;
			Gstat.panstat[jcol].spintime += t2[0];
			Gstat.procstat[pnum].spintime += t2[0];
	if (DOPRINT) {
			PRINT_SPIN_TIME(t2[0], jcol, pnum, kcol, 2);
	}
	}
		    }

		    dadsupno = supno[kcol];

	if ( DEBUGlevel>=2 ) {
		    /*if (jcol >= LOCOL && jcol <= HICOL)*/
	if ( jcol==BADCOL )
	    printf("(%d) pdgstrf_panel_bmod[2] krep %d, dad=kcol %d, dadsupno %d\n",
		   pnum, krep, kcol, dadsupno);
	}

		} while ( dadsupno == ksupno );

		/* Append the new segment into segrep[*]. After column_bmod(),
		   copy_to_ucol() will use them. */
		segrep[nseg[0]] = krep;
	        ++(nseg[0]);

		/* Determine repfnz[krep, w] for each column in the panel */
		for (jj = jcol; jj < jcol + w; ++jj, dense_col[0] += m,
		       repfnz_col[0] += m, col_marker[0] += m, col_lsub[0] += m) {
		    /*
		     * Note: relaxed supernode may not form a path on the e-tree,
		     *       but its column numbers are contiguous.
		     */
	if (SCATTER_FOUND) {
	 	    for (kcol = fsupc; kcol <= krep; ++kcol) {
			if ( col_marker[inv_perm_r[kcol]] == jj ) {
			    repfnz_col[krep] = kcol;

	 		    /* Append new fills in panel_lsub[*,jj]. */
			    j = w_lsub_end[jj - jcol];
	/*#pragma ivdep*/
			    for (k = xlsub[krep]; k < xlsub_end[krep]; ++k) {
				ksub = lsub[k];
				if ( col_marker[ksub] != jj ) {
				    col_marker[ksub] = jj;
				    col_lsub[j++] = ksub;
				}
			    }
			    w_lsub_end[jj - jcol] = j;

			    break; /* found the leading nonzero in the segment */
			}
		    }

	} else {
		    for (kcol = fsupc; kcol <= krep; ++kcol) {
	                if ( dense_col[inv_perm_r[kcol]] != 0.0 ) {
			    repfnz_col[krep] = kcol;
			    break; /* Found the leading nonzero in the U-segment */
			}
		    }

		    /* In this case, we always treat the L-subscripts of the
		       busy s-node [kcol : krep] as the new fills, even if the
		       corresponding U-segment may be all zero. */

		    /* Append new fills in panel_lsub[*,jj]. */
		    j = w_lsub_end[jj - jcol];
	/*#pragma ivdep*/
		    for (k = xlsub[krep]; k < xlsub_end[krep]; ++k) {
		        ksub = lsub[k];
			if ( col_marker[ksub] != jj ) {
			    col_marker[ksub] = jj;
			    col_lsub[j++] = ksub;
			}
		    }
		    w_lsub_end[jj - jcol] = j;
	}

	if ( DEBUGlevel>=2 ) {
	if (jj == BADCOL) {
	printf("(%d) pdgstrf_panel_bmod[fills]: jj %d, repfnz_col[%d] %d, inv_pr[%d] %d\n",
		   pnum, jj, krep, repfnz_col[krep], fsupc, inv_perm_r[fsupc]);
	printf("(%d) pdgstrf_panel_bmod[fills] xlsub %d, xlsub_end %d, #lsub[%d] %d\n",
	       pnum,xlsub[krep],xlsub_end[krep],krep, xlsub_end[krep]-xlsub[krep]);
	}
	}
		} /* for jj ... */

	if (PREDICT_OPT) {
		pmod = Gstat.procstat[pnum].fcops;
	}

		/* Perform sup-panel updates - use combined 1D + 2D updates. */
		nsupc = krep - fsupc + 1;
		nsupr = xlsub_end[fsupc] - xlsub[fsupc];
		nrow = nsupr - nsupc;
		if ( nsupc >= colblk && nrow >= rowblk ) {
		    /* 2-D block update */
	if (GEMV2) {
		    pdgstrf_bmod2D_mv2(pnum, m, w, jcol, fsupc, krep, nsupc, nsupr,
				       nrow, repfnz, panel_lsub, w_lsub_end,
				       spa_marker, dense, tempv, Glu, Gstat);
	} else {
		    pdgstrf_bmod2D(pnum, m, w, jcol, fsupc, krep, nsupc, nsupr, nrow,
				   repfnz, panel_lsub, w_lsub_end, spa_marker,
				   dense, tempv, Glu, Gstat);
	}
		} else {
		    /* 1-D block update */
	if (GEMV2) {
		    pdgstrf_bmod1D_mv2(pnum, m, w, jcol, fsupc, krep, nsupc, nsupr,
				       nrow, repfnz, panel_lsub, w_lsub_end,
				       spa_marker, dense, tempv, Glu, Gstat);
	} else {
		    pdgstrf_bmod1D(pnum, m, w, jcol, fsupc, krep, nsupc, nsupr, nrow,
				   repfnz, panel_lsub, w_lsub_end, spa_marker,
				   dense, tempv, Glu, Gstat);
	}
		}

	if (PREDICT_OPT) {
		pmod = Gstat.procstat[pnum].fcops - pmod;
		kid = (pxgstrf_shared.pan_status[krep].size > 0) ?
		       krep : (krep + pxgstrf_shared.pan_status[krep].size);
		Gstat.desc_eft[ndesc].eft = Gstat.cp_panel[kid].est + Gstat.cp_panel[kid].pdiv;
		Gstat.desc_eft[ndesc++].pmod = pmod;
	}

	if ( DEBUGlevel>=2 ) {
	if (jcol == BADPAN)
	    printf("(%d) After busy update: dense_col[%d] %.10f\n",
		   pnum, BADROW, dense[/*dbg_addr+*/BADROW]);
	}

		/* Go to the parent of "krep" */
		kcol = etree[krep];

	    } /* while kcol < jcol ... */

	if ( DEBUGlevel>=2 ) {
	    /*if (jcol >= LOCOL && jcol <= HICOL)*/
	if ( jcol==BADCOL )
	    check_panel_dfs_list(pnum, "after-busy", jcol, nseg[0], segrep);
	}

	if (PREDICT_OPT) {
		Arrays.sort(Gstat.desc_eft);
	    pmod_eft = 0;
	    for (j = 0; j < ndesc; ++j) {
		pmod_eft = (float) (SUPERLU_MAX( pmod_eft, Gstat.desc_eft[j].eft ) + Gstat.desc_eft[j].pmod);
	    }

	    if ( ndesc == 0 ) {
		/* No modifications from descendants */
		pmod_eft = 0;
		for (j = Gstat.cp_firstkid[jcol]; j != EMPTY; j = Gstat.cp_nextkid[j]) {
		    kid = (pxgstrf_shared.pan_status[j].size > 0) ?
				j : (j + pxgstrf_shared.pan_status[j].size);
		    pmod_eft = (float) SUPERLU_MAX( pmod_eft,
		    		Gstat.cp_panel[kid].est + Gstat.cp_panel[kid].pdiv );
		}
	    }

	    Gstat.cp_panel[jcol].est = pmod_eft;

	}

	}

	static int
	check_panel_dfs_list(int pnum, String msg, int jcol, int nseg, int segrep[])
	{
	    int k;

	    printf("(%d) pdgstrf_panel_bmod(%s) jcol %d, nseg %d in top. order .\n",
		   pnum, msg, jcol, nseg);
	    for (k = nseg-1; k >= 0; --k)
		printf("(%d) segrep-%d ", pnum, segrep[k]);
	    printf("\n");
	    fflush(stdout);
	    return 0;
	}

}

