/*  
 * Copyright (c) 2009  Christian Riekoff <info@texone.org>  
 *  
 *  This file is free software: you may copy, redistribute and/or modify it  
 *  under the terms of the GNU General Public License as published by the  
 *  Free Software Foundation, either version 2 of the License, or (at your  
 *  option) any later version.  
 *  
 *  This file is distributed in the hope that it will be useful, but  
 *  WITHOUT ANY WARRANTY; without even the implied warranty of  
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU  
 *  General Public License for more details.  
 *  
 *  You should have received a copy of the GNU General Public License  
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.  
 *  
 * This file incorporates work covered by the following copyright and  
 * permission notice:  
 */
package cc.creativecomputing.gpu.particles;

import cc.creativecomputing.gpu.particles.util.CCGPUUtil;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.graphics.shader.CCCGShader;
import cc.creativecomputing.graphics.shader.CCShaderTexture;

import com.jogamp.opengl.cg.CGparameter;

/**
 * @author christianriekoff
 * 
 */
public class CCGPUParticleSorter {
	CCShaderTexture[] SortPBuffers;
	int[] SortTextures;
	int SortCurrent;
	int SortDest;

	int SortCurrentPass;
	int SortPassBegin;
	int SortPassEnd;
	int SortPassesPerFrame;
	int SortMaxPasses;

	private CCCGShader SortRecursionFProg;
	private CGparameter SortRecursion_Size_Param;
	private CGparameter SortRecursion_Step_Param;
	private CGparameter SortRecursion_Count_Param;
	private CGparameter _mySortRecursionDataTextureParameter;
	
	private CCCGShader SortEndFProg;
	private CGparameter SortEnd_Size_Param;
	private CGparameter SortEnd_Step_Param;
	private CGparameter _mySortEndDataTextureParameter;

	private CCCGShader DistanceSortInitFProg;
	private CCCGShader DistanceSortComputeFProg;
	private CCCGShader DistanceSortLookupFProg;
	private CGparameter DistanceSortCompute_ViewerPosition_Param;

	private CCGraphics _myGraphics;
	private CCGPUParticles _myParticles;

	public CCGPUParticleSorter(CCGraphics theGraphics, CCGPUParticles theParticles) {
		_myGraphics = theGraphics;
		_myParticles = theParticles;

		SortCurrent = 0;
		SortDest = 1;

		SortCurrentPass = 0;
		SortPassesPerFrame = 5;
		SortPassBegin = 0;
		SortPassEnd = SortPassesPerFrame;
		SortMaxPasses = 0;

		SortPBuffers = new CCShaderTexture[2];
		SortPBuffers[0] = new CCShaderTexture(32, 2, _myParticles.width(), _myParticles.height());
		SortPBuffers[1] = new CCShaderTexture(32, 2, _myParticles.width(), _myParticles.height());

		SortRecursionFProg = new CCCGShader(null, CCGPUUtil.shaderPath("sort/mergeSortRecursion.fp"));
		SortRecursion_Size_Param = SortRecursionFProg.fragmentParameter("_Size");
		SortRecursion_Step_Param = SortRecursionFProg.fragmentParameter("_Step");
		SortRecursion_Count_Param = SortRecursionFProg.fragmentParameter("_Count");
		_mySortRecursionDataTextureParameter = SortRecursionFProg.fragmentParameter("_SortData");
		SortRecursionFProg.load();

		SortEndFProg = new CCCGShader(null, CCGPUUtil.shaderPath("sort/mergeSortEnd.fp"));
		SortEnd_Size_Param = SortEndFProg.fragmentParameter("_Size");
		SortEnd_Step_Param = SortEndFProg.fragmentParameter("_Step");
		_mySortEndDataTextureParameter = SortRecursionFProg.fragmentParameter("_SortData");
		SortEndFProg.load();

		DistanceSortInitFProg = new CCCGShader(null, null, CCGPUUtil.shaderPath("sort/distancesort.fp"), "initSortIndex");
		DistanceSortComputeFProg = new CCCGShader(null, null, CCGPUUtil.shaderPath("sort/distancesort.fp"), "computeDistance");
		DistanceSortCompute_ViewerPosition_Param = DistanceSortComputeFProg.fragmentParameter("_ViewerPosition");
		DistanceSortLookupFProg = new CCCGShader(null, null, CCGPUUtil.shaderPath("sort/distancesort.fp"), "lookupPosition");
	}

//	public void reset() {
//		// srand(157);
//		// srand((unsigned)time(NULL));
//		float[] sortData = new float[_myParticles.size()];
//		for (int i = 0; i < sortData.length; i++)
//			sortData[i] = (float) i / (sortData.length - 1);
//
//		for (int i = 0; i < sortData.length; i++) {
//			int p = (int) (rand() * (__int64) (sortData.size() - 1) / RAND_MAX);
//			float f = sortData[i];
//			sortData[i] = sortData[p];
//			sortData[p] = f;
//		}
//
//		for (int y = 0; y < _myParticles.height(); y++) {
//			for (int x = 0; x < _myParticles.width(); x++) {
//				PositionBuffer[_myParticles.width() * y + x] = float3(sortData[Width * y + x], sortData[Width * y + x], sortData[Width * y + x]);
//			}
//		}
//
//		SortPBuffers[SortCurrent].beginDraw();
//		_myGraphics.clear();
//		DrawImage();
//		SortPBuffers[SortCurrent].endDraw();
//
//		SortCurrentPass = 0;
//		SortMaxPasses = 100000;
//
//		DistanceSortInitFProg.start();
//		SortPBuffers[SortCurrent].draw();
//		DistanceSortInitFProg.end();
//	}
	
	public void sort() {
		// Update distances in sort texture.

		DistanceSortComputeFProg.start();

//		glActiveTextureARB(GL_TEXTURE0_ARB);
//		SortPBuffers[SortCurrent]->Bind();
//
//		glActiveTextureARB(GL_TEXTURE1_ARB);
//		PositionPBuffers[PositionCurrent]->Bind();
//
//		System.out.printf("Viewer: %.2f %.2f %.2f\n", ViewerPosition.v[0], ViewerPosition.v[1], ViewerPosition.v[2]);
//		DistanceSortComputeFProg.parameter(DistanceSortCompute_ViewerPosition_Param, ViewerPosition.v);

		SortPBuffers[SortDest].draw();

		DistanceSortComputeFProg.end();

		int temp = SortCurrent;
		SortCurrent = SortDest;
		SortDest = temp;

		// Sort.
		mergeSort();

//		// Lookup positions from sorted texture.
//		PositionPBuffers[PositionDest]->BeginCapture();
//
//		cgGLBindProgram(DistanceSortLookupFProg);
//		cgGLEnableProfile(CG_PROFILE_FP30);
//
//		glActiveTextureARB(GL_TEXTURE0_ARB);
//		SortPBuffers[SortCurrent]->Bind();
//
//		glActiveTextureARB(GL_TEXTURE1_ARB);
//		PositionPBuffers[PositionCurrent]->Bind();
//
//		DrawQuad();
//
//		cgGLDisableProfile(CG_PROFILE_FP30);
//
//		PositionPBuffers[PositionDest]->EndCapture();
//
//		// Don't swap PositionDest and PositionCurrent this time,
//		// because simulation must not use sorted positions!
//
//		positionRender = PositionDest;
	}
	
	
	/**
	 * Return logarithm to base 2. Works only for power of two values.
	 */
	private int logd(int value){
		int l = 0;
//		for (l = 0; value >> l; l++);
		return l - 1;
	}

	public void mergeSort() {
		SortCurrentPass = 0;

		int logdSize = logd(_myParticles.size());
		SortMaxPasses = (logdSize + 1) * logdSize / 2;

		SortPassBegin = SortPassEnd;
		SortPassEnd = (SortPassBegin + SortPassesPerFrame) % SortMaxPasses;

		SortRecursionFProg.parameter(SortRecursion_Size_Param, (float) _myParticles.width(), (float) _myParticles.height());
		SortEndFProg.parameter(SortEnd_Size_Param, (float) _myParticles.width(), (float) _myParticles.height());

		DoMergeSortPass(_myParticles.size());

		/*
		 * SortPBuffers[SortCurrent]->BeginCapture(); glReadPixels(0, 0, Width, Height, GL_RED, GL_FLOAT,
		 * (float*)DebugBuffer); SortPBuffers[SortCurrent]->EndCapture();
		 * 
		 * for (int i = 0; i < GetSize(); i++) //printf("%i, ", (int)(DebugBuffer[i] * 255 + 0.5)); printf("%.2f, ",
		 * DebugBuffer[i]); printf("\n");
		 * 
		 * 
		 * for (int i = 0; i < GetSize() - 1; i++) if (DebugBuffer[i] < DebugBuffer[i + 1])
		 * //printf("Error at %i: %i < %i\n", i, (int)(DebugBuffer[i] * 255 + 0.5), (int)(DebugBuffer[i + 1] * 255 +
		 * 0.5)); printf("Error at %i: %.2f < %.2f\n", i, DebugBuffer[i], DebugBuffer[i + 1]);
		 */
	}

	private void DoMergeSortPass(int count) {
		System.out.printf("mergeSort: count=%i\n", count);

		if (count > 1) {
			DoMergeSortPass(count / 2);
			DoMergePass(count, 1);
		}

		System.out.printf("mergeSort: end\n");
	}

	private void DoMergePass(int count, int step){
		if (count > 2){
			DoMergePass(count / 2, step * 2);

			SortCurrentPass++;

			if (SortPassBegin < SortPassEnd){
				if (SortCurrentPass < SortPassBegin || SortCurrentPass >= SortPassEnd)
					return;
			} else {
				if (SortCurrentPass < SortPassBegin && SortCurrentPass >= SortPassEnd)
					return;
			}

			System.out.printf("%02i: mergeRec: count=%i, step=%i\n", SortCurrentPass, count, step);

			SortRecursionFProg.start();
			SortRecursionFProg.texture(_mySortRecursionDataTextureParameter, SortPBuffers[SortCurrent].id());
			SortRecursionFProg.parameter(SortRecursion_Step_Param, (float)step);
			SortRecursionFProg.parameter(SortRecursion_Count_Param, (float)count);
			SortPBuffers[SortDest].draw();
			SortRecursionFProg.end();

			int temp = SortCurrent;
			SortCurrent = SortDest;
			SortDest = temp;
		} else {
			SortCurrentPass++;

			if (SortPassBegin < SortPassEnd) {
				if (SortCurrentPass < SortPassBegin || SortCurrentPass >= SortPassEnd)
					return;
			} else {
				if (SortCurrentPass < SortPassBegin && SortCurrentPass >= SortPassEnd)
					return;
			}

			System.out.printf("%02i: mergeEnd: count=%i, step=%i\n", SortCurrentPass, count, step);

			SortEndFProg.start();
			SortEndFProg.texture(_mySortEndDataTextureParameter, SortPBuffers[SortCurrent].id());
			SortEndFProg.parameter(SortEnd_Step_Param, (float)step);
			SortPBuffers[SortDest].draw();
			SortEndFProg.end();

			int temp = SortCurrent;
			SortCurrent = SortDest;
			SortDest = temp;
		}
	}
}

