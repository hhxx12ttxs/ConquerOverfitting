package jp.ac.oit.is.android_agent_02;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;

public class AnimationImage {
	protected Bitmap _bmp = null;
	protected float _elapsed = 0.0f;
	protected int _crntFrame = 0;
	protected int _blockW = 0;
	protected int _blockH = 0;
	protected float _scale = 1.0f;
	protected float[] _frameTime = null;

	AnimationImage() {
	}

	AnimationImage(Bitmap bmp, int blockW, int blockH, float scale) {
		setUp(bmp, blockW, blockH, scale);
	}

	public void setUp(Bitmap bmp, int blockW, int blockH, float scale) {
		_bmp = bmp;
		_scale = scale;
		_blockW = blockW;
		_blockH = blockH;
		int iw = _bmp.getWidth() / _blockW;
		int ih = _bmp.getHeight() / _blockH;
		_frameTime = new float[iw * ih];
		setFrameTime(1.0f);
	}

	public int getFrameNum() {
		return _frameTime.length;
	}

	public void setFrameTime(float total) {
		float weight = 0;
		if (getFrameNum() > 0) {
			weight = total / getFrameNum();
		}
		for (int i = 0; i < getFrameNum(); ++i) {
			_frameTime[i] = weight;
		}
		_crntFrame = 0;
	}

	public void setFrameTime(float[] times) {
		float total = 0;
		for (int i = 0; i < times.length; ++i) {
			total += times[i];
		}
		setFrameTime(total, times);
	}

	public void setFrameTime(float total, float[] weight) {
		int len = java.lang.Math.min(weight.length, getFrameNum());
		int i = 0;
		float sum = 0;
		for (i = 0; i < len; ++i) {
			sum += weight[i];
		}
		if (sum > 0) {
			for (i = 0; i < len; ++i) {
				_frameTime[i] = total * weight[i] / sum;
			}
			for (; i < _frameTime.length; ++i) {
				_frameTime[i] = 0;
			}
		}
		_crntFrame = 0;
	}

	public void advanceTime(float delta) {
		_elapsed += delta;
		while (_elapsed > _frameTime[_crntFrame]) {
			_elapsed -= _frameTime[_crntFrame];
			_crntFrame = (_crntFrame + 1) % getFrameNum();
		}
	}

	public void drawFrame(Canvas c, int dstX, int dstY, Paint paint) {
		drawFrame(c, _crntFrame, dstX, dstY, paint);
	}

	public void drawFrame(Canvas c, int frameIdx, int dstX, int dstY,
			Paint paint) {
		if (frameIdx >= getFrameNum()) {
			return;
		}
		int iw = _bmp.getWidth() / _blockW;
		int x = frameIdx % iw * _blockW;
		int y = frameIdx / iw * _blockH;
		Rect src = new Rect(x, y, x + _blockW, y + _blockH);
		RectF dst = new RectF(dstX, dstY, dstX + _blockW * _scale, dstY
				+ _blockH * _scale);
		c.drawBitmap(_bmp, src, dst, paint);
	}
}

