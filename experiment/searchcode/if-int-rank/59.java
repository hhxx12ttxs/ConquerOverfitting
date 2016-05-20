/*
 * The MIT License (MIT)

Copyright (c) 2014 Chess Position Manager

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */
package com.loloof64.android.chs_pos_mngr.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Picture;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.View;

import com.loloof64.android.chs_pos_mngr.R;
import com.loloof64.android.chs_pos_mngr.core.ChessPicturesManager;
import com.loloof64.android.chs_pos_mngr.core.ChessPosition;

public class ChessPositionView extends View {

	public ChessPositionView(Context context, AttributeSet attrs,
			int defStyleAttr) {

        super(context, attrs, defStyleAttr);
        position = new ChessPosition();

    }

    public ChessPositionView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public ChessPositionView(Context context){
        this(context, null, 0);
    }



    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);

        int minDimension = width < height ? width : height;
        cellsSizePx = minDimension / 8;
    }

    @Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawCells(canvas);
        drawPieces(canvas);
	}

    private void drawPieces(Canvas canvas) {

        for (int rank = 0; rank < 8; rank++){
            for (int file = 0; file < 8; file++){
                char pieceType = position.getPieceAt(file, rank);
                boolean isNotEmptyCell = "pnbrqkPNBRQK".contains(String.valueOf(pieceType));
                if (isNotEmptyCell){
                    Picture picture = ChessPicturesManager.getInstance()
                            .getPieceSVG(pieceType).renderToPicture();
                    RectF rect = new RectF(cellsSizePx * file, cellsSizePx * (7-rank),
                            cellsSizePx * (file+1), cellsSizePx *(8-rank));
                    canvas.drawPicture(picture, rect);
                }
            }
        }

    }

    private void drawCells(Canvas canvas) {
		Paint cellPaint = new Paint();
		for (int rank = 0; rank < 8; rank++){
			for (int file = 0; file < 8; file++){
				Rect cellRect = new Rect(cellsSizePx * file, cellsSizePx * rank,
						cellsSizePx * (file+1), cellsSizePx * (rank+1));
				boolean whiteCell = (rank+file) % 2 == 0;
				if (whiteCell){
					cellPaint.setColor(getResources().getColor(R.color.white_cell));
				}
				else {
					cellPaint.setColor(getResources().getColor(R.color.black_cell));
				}
				
				canvas.drawRect(cellRect, cellPaint);
			}
		}
	}

	private int cellsSizePx;
    private ChessPosition position;

}

