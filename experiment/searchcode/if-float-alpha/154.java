// Decompiled by Jad v1.5.8e. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.geocities.com/kpdus/jad.html
// Decompiler options: packimports(3) nonlb 

package android.filterpacks.videoproc;

import android.filterfw.core.*;
import android.filterfw.format.ImageFormat;
import android.opengl.GLES20;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

public class BackDropperFilter extends Filter {
    public static interface LearningDoneListener {

        public abstract void onLearningDone(BackDropperFilter backdropperfilter);
    }


    public BackDropperFilter(String s) {
        String s1;
        super(s);
        BACKGROUND_STRETCH = 0;
        BACKGROUND_FIT = 1;
        BACKGROUND_FILL_CROP = 2;
        mBackgroundFitMode = 2;
        mLearningDuration = 40;
        mLearningVerifyDuration = 10;
        mAcceptStddev = 0.85F;
        mHierarchyLrgScale = 0.7F;
        mHierarchyMidScale = 0.6F;
        mHierarchySmlScale = 0.5F;
        mMaskWidthExp = 8;
        mMaskHeightExp = 8;
        mHierarchyLrgExp = 3;
        mHierarchyMidExp = 2;
        mHierarchySmlExp = 0;
        mLumScale = 0.4F;
        mChromaScale = 1.35F;
        mMaskBg = 0.65F;
        mMaskFg = 0.95F;
        mExposureChange = 1.0F;
        mWhiteBalanceRedChange = 0.0F;
        mWhiteBalanceBlueChange = 0.0F;
        mAutoWBToggle = 0;
        mAdaptRateLearning = 0.2F;
        mAdaptRateBg = 0.0F;
        mAdaptRateFg = 0.0F;
        mVerifyRate = 0.25F;
        mLearningDoneListener = null;
        mUseTheForce = false;
        mProvideDebugOutputs = false;
        mMirrorBg = false;
        mOrientation = 0;
        startTime = -1L;
        mLogVerbose = Log.isLoggable("BackDropperFilter", 2);
        s1 = SystemProperties.get("ro.media.effect.bgdropper.adj");
        if(s1.length() <= 0)
            break MISSING_BLOCK_LABEL_269;
        mAcceptStddev = mAcceptStddev + Float.parseFloat(s1);
        if(mLogVerbose)
            Log.v("BackDropperFilter", (new StringBuilder()).append("Adjusting accept threshold by ").append(s1).append(", now ").append(mAcceptStddev).toString());
_L1:
        return;
        NumberFormatException numberformatexception;
        numberformatexception;
        Log.e("BackDropperFilter", (new StringBuilder()).append("Badly formatted property ro.media.effect.bgdropper.adj: ").append(s1).toString());
          goto _L1
    }

    private void allocateFrames(FrameFormat frameformat, FilterContext filtercontext) {
        if(createMemoryFormat(frameformat)) {
            if(mLogVerbose)
                Log.v("BackDropperFilter", "Allocating BackDropperFilter frames");
            int i = mMaskFormat.getSize();
            byte abyte0[] = new byte[i];
            byte abyte1[] = new byte[i];
            byte abyte2[] = new byte[i];
            for(int j = 0; j < i; j++) {
                abyte0[j] = -128;
                abyte1[j] = 10;
                abyte2[j] = 0;
            }

            for(int k = 0; k < 2; k++) {
                mBgMean[k] = (GLFrame)filtercontext.getFrameManager().newFrame(mMaskFormat);
                mBgMean[k].setData(abyte0, 0, i);
                mBgVariance[k] = (GLFrame)filtercontext.getFrameManager().newFrame(mMaskFormat);
                mBgVariance[k].setData(abyte1, 0, i);
                mMaskVerify[k] = (GLFrame)filtercontext.getFrameManager().newFrame(mMaskFormat);
                mMaskVerify[k].setData(abyte2, 0, i);
            }

            if(mLogVerbose)
                Log.v("BackDropperFilter", "Done allocating texture for Mean and Variance objects!");
            mDistance = (GLFrame)filtercontext.getFrameManager().newFrame(mMaskFormat);
            mMask = (GLFrame)filtercontext.getFrameManager().newFrame(mMaskFormat);
            mAutoWB = (GLFrame)filtercontext.getFrameManager().newFrame(mAverageFormat);
            mVideoInput = (GLFrame)filtercontext.getFrameManager().newFrame(mMemoryFormat);
            mBgInput = (GLFrame)filtercontext.getFrameManager().newFrame(mMemoryFormat);
            mMaskAverage = (GLFrame)filtercontext.getFrameManager().newFrame(mAverageFormat);
            mBgDistProgram = new ShaderProgram(filtercontext, (new StringBuilder()).append(mSharedUtilShader).append("uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float subsample_level;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord, subsample_level);\n  vec4 fg = coeff_yuv * vec4(fg_rgb.rgb, 1.);\n  vec4 mean = texture2D(tex_sampler_1, v_texcoord);\n  vec4 variance = inv_var_scale * texture2D(tex_sampler_2, v_texcoord);\n\n  float dist_y = gauss_dist_y(fg.r, mean.r, variance.r);\n  float dist_uv = gauss_dist_uv(fg.gb, mean.gb, variance.gb);\n  gl_FragColor = vec4(0.5*fg.rg, dist_scale*dist_y, dist_scale*dist_uv);\n}\n").toString());
            mBgDistProgram.setHostValue("subsample_level", Float.valueOf(mSubsampleLevel));
            mBgMaskProgram = new ShaderProgram(filtercontext, (new StringBuilder()).append(mSharedUtilShader).append("uniform sampler2D tex_sampler_0;\nuniform float accept_variance;\nuniform vec2 yuv_weights;\nuniform float scale_lrg;\nuniform float scale_mid;\nuniform float scale_sml;\nuniform float exp_lrg;\nuniform float exp_mid;\nuniform float exp_sml;\nvarying vec2 v_texcoord;\nbool is_fg(vec2 dist_yc, float accept_variance) {\n  return ( dot(yuv_weights, dist_yc) >= accept_variance );\n}\nvoid main() {\n  vec4 dist_lrg_sc = texture2D(tex_sampler_0, v_texcoord, exp_lrg);\n  vec4 dist_mid_sc = texture2D(tex_sampler_0, v_texcoord, exp_mid);\n  vec4 dist_sml_sc = texture2D(tex_sampler_0, v_texcoord, exp_sml);\n  vec2 dist_lrg = inv_dist_scale * dist_lrg_sc.ba;\n  vec2 dist_mid = inv_dist_scale * dist_mid_sc.ba;\n  vec2 dist_sml = inv_dist_scale * dist_sml_sc.ba;\n  vec2 norm_dist = 0.75 * dist_sml / accept_variance;\n  bool is_fg_lrg = is_fg(dist_lrg, accept_variance * scale_lrg);\n  bool is_fg_mid = is_fg_lrg || is_fg(dist_mid, accept_variance * scale_mid);\n  float is_fg_sml =\n      float(is_fg_mid || is_fg(dist_sml, accept_variance * scale_sml));\n  float alpha = 0.5 * is_fg_sml + 0.3 * float(is_fg_mid) + 0.2 * float(is_fg_lrg);\n  gl_FragColor = vec4(alpha, norm_dist, is_fg_sml);\n}\n").toString());
            mBgMaskProgram.setHostValue("accept_variance", Float.valueOf(mAcceptStddev * mAcceptStddev));
            float af[] = new float[2];
            af[0] = mLumScale;
            af[1] = mChromaScale;
            mBgMaskProgram.setHostValue("yuv_weights", af);
            mBgMaskProgram.setHostValue("scale_lrg", Float.valueOf(mHierarchyLrgScale));
            mBgMaskProgram.setHostValue("scale_mid", Float.valueOf(mHierarchyMidScale));
            mBgMaskProgram.setHostValue("scale_sml", Float.valueOf(mHierarchySmlScale));
            mBgMaskProgram.setHostValue("exp_lrg", Float.valueOf(mSubsampleLevel + mHierarchyLrgExp));
            mBgMaskProgram.setHostValue("exp_mid", Float.valueOf(mSubsampleLevel + mHierarchyMidExp));
            mBgMaskProgram.setHostValue("exp_sml", Float.valueOf(mSubsampleLevel + mHierarchySmlExp));
            if(mUseTheForce)
                mBgSubtractProgram = new ShaderProgram(filtercontext, (new StringBuilder()).append(mSharedUtilShader).append("uniform mat3 bg_fit_transform;\nuniform float mask_blend_bg;\nuniform float mask_blend_fg;\nuniform float exposure_change;\nuniform float whitebalancered_change;\nuniform float whitebalanceblue_change;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform sampler2D tex_sampler_3;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 bg_texcoord = (bg_fit_transform * vec3(v_texcoord, 1.)).xy;\n  vec4 bg_rgb = texture2D(tex_sampler_1, bg_texcoord);\n  vec4 wb_auto_scale = texture2D(tex_sampler_3, v_texcoord) * exposure_change / auto_wb_scale;\n  vec4 wb_manual_scale = vec4(1. + whitebalancered_change, 1., 1. + whitebalanceblue_change, 1.);\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord);\n  vec4 fg_adjusted = fg_rgb * wb_manual_scale * wb_auto_scale;\n  vec4 mask = texture2D(tex_sampler_2, v_texcoord, \n                      2.0);\n  float alpha = smoothstep(mask_blend_bg, mask_blend_fg, mask.a);\n  gl_FragColor = mix(bg_rgb, fg_adjusted, alpha);\n").append("  vec4 ghost_rgb = (fg_adjusted * 0.7 + vec4(0.3,0.3,0.4,0.))*0.65 + \n                   0.35*bg_rgb;\n  float glow_start = 0.75 * mask_blend_bg; \n  float glow_max   = mask_blend_bg; \n  gl_FragColor = mask.a < glow_start ? bg_rgb : \n                 mask.a < glow_max ? mix(bg_rgb, vec4(0.9,0.9,1.0,1.0), \n                                     (mask.a - glow_start) / (glow_max - glow_start) ) : \n                 mask.a < mask_blend_fg ? mix(vec4(0.9,0.9,1.0,1.0), ghost_rgb, \n                                    (mask.a - glow_max) / (mask_blend_fg - glow_max) ) : \n                 ghost_rgb;\n}\n").toString());
            else
                mBgSubtractProgram = new ShaderProgram(filtercontext, (new StringBuilder()).append(mSharedUtilShader).append("uniform mat3 bg_fit_transform;\nuniform float mask_blend_bg;\nuniform float mask_blend_fg;\nuniform float exposure_change;\nuniform float whitebalancered_change;\nuniform float whitebalanceblue_change;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform sampler2D tex_sampler_3;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 bg_texcoord = (bg_fit_transform * vec3(v_texcoord, 1.)).xy;\n  vec4 bg_rgb = texture2D(tex_sampler_1, bg_texcoord);\n  vec4 wb_auto_scale = texture2D(tex_sampler_3, v_texcoord) * exposure_change / auto_wb_scale;\n  vec4 wb_manual_scale = vec4(1. + whitebalancered_change, 1., 1. + whitebalanceblue_change, 1.);\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord);\n  vec4 fg_adjusted = fg_rgb * wb_manual_scale * wb_auto_scale;\n  vec4 mask = texture2D(tex_sampler_2, v_texcoord, \n                      2.0);\n  float alpha = smoothstep(mask_blend_bg, mask_blend_fg, mask.a);\n  gl_FragColor = mix(bg_rgb, fg_adjusted, alpha);\n").append("}\n").toString());
            mBgSubtractProgram.setHostValue("bg_fit_transform", DEFAULT_BG_FIT_TRANSFORM);
            mBgSubtractProgram.setHostValue("mask_blend_bg", Float.valueOf(mMaskBg));
            mBgSubtractProgram.setHostValue("mask_blend_fg", Float.valueOf(mMaskFg));
            mBgSubtractProgram.setHostValue("exposure_change", Float.valueOf(mExposureChange));
            mBgSubtractProgram.setHostValue("whitebalanceblue_change", Float.valueOf(mWhiteBalanceBlueChange));
            mBgSubtractProgram.setHostValue("whitebalancered_change", Float.valueOf(mWhiteBalanceRedChange));
            mBgUpdateMeanProgram = new ShaderProgram(filtercontext, (new StringBuilder()).append(mSharedUtilShader).append("uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float subsample_level;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord, subsample_level);\n  vec4 fg = coeff_yuv * vec4(fg_rgb.rgb, 1.);\n  vec4 mean = texture2D(tex_sampler_1, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_2, v_texcoord, \n                      2.0);\n\n  float alpha = local_adapt_rate(mask.a);\n  vec4 new_mean = mix(mean, fg, alpha);\n  gl_FragColor = new_mean;\n}\n").toString());
            mBgUpdateMeanProgram.setHostValue("subsample_level", Float.valueOf(mSubsampleLevel));
            mBgUpdateVarianceProgram = new ShaderProgram(filtercontext, (new StringBuilder()).append(mSharedUtilShader).append("uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform sampler2D tex_sampler_3;\nuniform float subsample_level;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord, subsample_level);\n  vec4 fg = coeff_yuv * vec4(fg_rgb.rgb, 1.);\n  vec4 mean = texture2D(tex_sampler_1, v_texcoord);\n  vec4 variance = inv_var_scale * texture2D(tex_sampler_2, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_3, v_texcoord, \n                      2.0);\n\n  float alpha = local_adapt_rate(mask.a);\n  vec4 cur_variance = (fg-mean)*(fg-mean);\n  vec4 new_variance = mix(variance, cur_variance, alpha);\n  new_variance = max(new_variance, vec4(min_variance));\n  gl_FragColor = var_scale * new_variance;\n}\n").toString());
            mBgUpdateVarianceProgram.setHostValue("subsample_level", Float.valueOf(mSubsampleLevel));
            mCopyOutProgram = ShaderProgram.createIdentity(filtercontext);
            mAutomaticWhiteBalanceProgram = new ShaderProgram(filtercontext, (new StringBuilder()).append(mSharedUtilShader).append("uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float pyramid_depth;\nuniform bool autowb_toggle;\nvarying vec2 v_texcoord;\nvoid main() {\n   vec4 mean_video = texture2D(tex_sampler_0, v_texcoord, pyramid_depth);\n   vec4 mean_bg = texture2D(tex_sampler_1, v_texcoord, pyramid_depth);\n   float green_normalizer = mean_video.g / mean_bg.g;\n   vec4 adjusted_value = vec4(mean_bg.r / mean_video.r * green_normalizer, 1., \n                         mean_bg.b / mean_video.b * green_normalizer, 1.) * auto_wb_scale; \n   gl_FragColor = autowb_toggle ? adjusted_value : vec4(auto_wb_scale);\n}\n").toString());
            mAutomaticWhiteBalanceProgram.setHostValue("pyramid_depth", Float.valueOf(mPyramidDepth));
            mAutomaticWhiteBalanceProgram.setHostValue("autowb_toggle", Integer.valueOf(mAutoWBToggle));
            mMaskVerifyProgram = new ShaderProgram(filtercontext, (new StringBuilder()).append(mSharedUtilShader).append("uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float verify_rate;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 lastmask = texture2D(tex_sampler_0, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_1, v_texcoord);\n  float newmask = mix(lastmask.a, mask.a, verify_rate);\n  gl_FragColor = vec4(0., 0., 0., newmask);\n}\n").toString());
            mMaskVerifyProgram.setHostValue("verify_rate", Float.valueOf(mVerifyRate));
            if(mLogVerbose)
                Log.v("BackDropperFilter", (new StringBuilder()).append("Shader width set to ").append(mMemoryFormat.getWidth()).toString());
            mRelativeAspect = 1.0F;
            mFrameCount = 0;
            mStartLearning = true;
        }
    }

    private boolean createMemoryFormat(FrameFormat frameformat) {
        boolean flag = true;
        if(mMemoryFormat != null) {
            flag = false;
        } else {
            if(frameformat.getWidth() == 0 || frameformat.getHeight() == 0)
                throw new RuntimeException("Attempting to process input frame with unknown size");
            mMaskFormat = frameformat.mutableCopy();
            int i = (int)Math.pow(2D, mMaskWidthExp);
            int j = (int)Math.pow(2D, mMaskHeightExp);
            mMaskFormat.setDimensions(i, j);
            mPyramidDepth = Math.max(mMaskWidthExp, mMaskHeightExp);
            mMemoryFormat = mMaskFormat.mutableCopy();
            int k = Math.max(mMaskWidthExp, pyramidLevel(frameformat.getWidth()));
            int l = Math.max(mMaskHeightExp, pyramidLevel(frameformat.getHeight()));
            mPyramidDepth = Math.max(k, l);
            int i1 = Math.max(i, (int)Math.pow(2D, k));
            int j1 = Math.max(j, (int)Math.pow(2D, l));
            mMemoryFormat.setDimensions(i1, j1);
            mSubsampleLevel = mPyramidDepth - Math.max(mMaskWidthExp, mMaskHeightExp);
            if(mLogVerbose) {
                Log.v("BackDropperFilter", (new StringBuilder()).append("Mask frames size ").append(i).append(" x ").append(j).toString());
                Log.v("BackDropperFilter", (new StringBuilder()).append("Pyramid levels ").append(k).append(" x ").append(l).toString());
                Log.v("BackDropperFilter", (new StringBuilder()).append("Memory frames size ").append(i1).append(" x ").append(j1).toString());
            }
            mAverageFormat = frameformat.mutableCopy();
            mAverageFormat.setDimensions(flag, flag);
        }
        return flag;
    }

    private int pyramidLevel(int i) {
        return -1 + (int)Math.floor(Math.log10(i) / Math.log10(2D));
    }

    private void updateBgScaling(Frame frame, Frame frame1, boolean flag) {
        float f = (float)frame.getFormat().getWidth() / (float)frame.getFormat().getHeight() / ((float)frame1.getFormat().getWidth() / (float)frame1.getFormat().getHeight());
        if(f == mRelativeAspect && !flag) goto _L2; else goto _L1
_L1:
        float f1;
        float f2;
        float f3;
        float f4;
        mRelativeAspect = f;
        f1 = 0.0F;
        f2 = 1.0F;
        f3 = 0.0F;
        f4 = 1.0F;
        mBackgroundFitMode;
        JVM INSTR tableswitch 0 2: default 100
    //                   0 100
    //                   1 302
    //                   2 357;
           goto _L3 _L3 _L4 _L5
_L3:
        break; /* Loop/switch isn't completed */
_L5:
        break MISSING_BLOCK_LABEL_357;
_L6:
        if(mMirrorBg) {
            if(mLogVerbose)
                Log.v("BackDropperFilter", "Mirroring the background!");
            float af[];
            if(mOrientation == 0 || mOrientation == 180) {
                f2 = -f2;
                f1 = 1.0F - f1;
            } else {
                f4 = -f4;
                f3 = 1.0F - f3;
            }
        }
        if(mLogVerbose)
            Log.v("BackDropperFilter", (new StringBuilder()).append("bgTransform: xMin, yMin, xWidth, yWidth : ").append(f1).append(", ").append(f3).append(", ").append(f2).append(", ").append(f4).append(", mRelAspRatio = ").append(mRelativeAspect).toString());
        af = new float[9];
        af[0] = f2;
        af[1] = 0.0F;
        af[2] = 0.0F;
        af[3] = 0.0F;
        af[4] = f4;
        af[5] = 0.0F;
        af[6] = f1;
        af[7] = f3;
        af[8] = 1.0F;
        mBgSubtractProgram.setHostValue("bg_fit_transform", af);
_L2:
        return;
_L4:
        if(mRelativeAspect > 1.0F) {
            f1 = 0.5F - 0.5F * mRelativeAspect;
            f2 = 1.0F * mRelativeAspect;
        } else {
            f3 = 0.5F - 0.5F / mRelativeAspect;
            f4 = 1.0F / mRelativeAspect;
        }
          goto _L6
        if(mRelativeAspect > 1.0F) {
            f3 = 0.5F - 0.5F / mRelativeAspect;
            f4 = 1.0F / mRelativeAspect;
        } else {
            f1 = 0.5F - 0.5F * mRelativeAspect;
            f2 = mRelativeAspect;
        }
          goto _L6
    }

    public void close(FilterContext filtercontext) {
        if(mMemoryFormat != null) {
            if(mLogVerbose)
                Log.v("BackDropperFilter", "Filter Closing!");
            for(int i = 0; i < 2; i++) {
                mBgMean[i].release();
                mBgVariance[i].release();
                mMaskVerify[i].release();
            }

            mDistance.release();
            mMask.release();
            mAutoWB.release();
            mVideoInput.release();
            mBgInput.release();
            mMaskAverage.release();
            mMemoryFormat = null;
        }
    }

    public void fieldPortValueUpdated(String s, FilterContext filtercontext) {
        if(!s.equals("backgroundFitMode")) goto _L2; else goto _L1
_L1:
        mBackgroundFitModeChanged = true;
_L4:
        return;
_L2:
        if(s.equals("acceptStddev"))
            mBgMaskProgram.setHostValue("accept_variance", Float.valueOf(mAcceptStddev * mAcceptStddev));
        else
        if(s.equals("hierLrgScale"))
            mBgMaskProgram.setHostValue("scale_lrg", Float.valueOf(mHierarchyLrgScale));
        else
        if(s.equals("hierMidScale"))
            mBgMaskProgram.setHostValue("scale_mid", Float.valueOf(mHierarchyMidScale));
        else
        if(s.equals("hierSmlScale"))
            mBgMaskProgram.setHostValue("scale_sml", Float.valueOf(mHierarchySmlScale));
        else
        if(s.equals("hierLrgExp"))
            mBgMaskProgram.setHostValue("exp_lrg", Float.valueOf(mSubsampleLevel + mHierarchyLrgExp));
        else
        if(s.equals("hierMidExp"))
            mBgMaskProgram.setHostValue("exp_mid", Float.valueOf(mSubsampleLevel + mHierarchyMidExp));
        else
        if(s.equals("hierSmlExp"))
            mBgMaskProgram.setHostValue("exp_sml", Float.valueOf(mSubsampleLevel + mHierarchySmlExp));
        else
        if(s.equals("lumScale") || s.equals("chromaScale")) {
            float af[] = new float[2];
            af[0] = mLumScale;
            af[1] = mChromaScale;
            mBgMaskProgram.setHostValue("yuv_weights", af);
        } else
        if(s.equals("maskBg"))
            mBgSubtractProgram.setHostValue("mask_blend_bg", Float.valueOf(mMaskBg));
        else
        if(s.equals("maskFg"))
            mBgSubtractProgram.setHostValue("mask_blend_fg", Float.valueOf(mMaskFg));
        else
        if(s.equals("exposureChange"))
            mBgSubtractProgram.setHostValue("exposure_change", Float.valueOf(mExposureChange));
        else
        if(s.equals("whitebalanceredChange"))
            mBgSubtractProgram.setHostValue("whitebalancered_change", Float.valueOf(mWhiteBalanceRedChange));
        else
        if(s.equals("whitebalanceblueChange"))
            mBgSubtractProgram.setHostValue("whitebalanceblue_change", Float.valueOf(mWhiteBalanceBlueChange));
        else
        if(s.equals("autowbToggle"))
            mAutomaticWhiteBalanceProgram.setHostValue("autowb_toggle", Integer.valueOf(mAutoWBToggle));
        if(true) goto _L4; else goto _L3
_L3:
    }

    public FrameFormat getOutputFormat(String s, FrameFormat frameformat) {
        MutableFrameFormat mutableframeformat = frameformat.mutableCopy();
        if(!Arrays.asList(mOutputNames).contains(s))
            mutableframeformat.setDimensions(0, 0);
        return mutableframeformat;
    }

    public void prepare(FilterContext filtercontext) {
        if(mLogVerbose)
            Log.v("BackDropperFilter", "Preparing BackDropperFilter!");
        mBgMean = new GLFrame[2];
        mBgVariance = new GLFrame[2];
        mMaskVerify = new GLFrame[2];
        copyShaderProgram = ShaderProgram.createIdentity(filtercontext);
    }

    public void process(FilterContext filtercontext) {
        Frame frame = pullInput("video");
        Frame frame1 = pullInput("background");
        allocateFrames(frame.getFormat(), filtercontext);
        if(mStartLearning) {
            if(mLogVerbose)
                Log.v("BackDropperFilter", "Starting learning");
            mBgUpdateMeanProgram.setHostValue("bg_adapt_rate", Float.valueOf(mAdaptRateLearning));
            mBgUpdateMeanProgram.setHostValue("fg_adapt_rate", Float.valueOf(mAdaptRateLearning));
            mBgUpdateVarianceProgram.setHostValue("bg_adapt_rate", Float.valueOf(mAdaptRateLearning));
            mBgUpdateVarianceProgram.setHostValue("fg_adapt_rate", Float.valueOf(mAdaptRateLearning));
            mFrameCount = 0;
        }
        int i;
        int j;
        boolean flag;
        Frame aframe[];
        Frame aframe1[];
        if(mPingPong)
            i = 0;
        else
            i = 1;
        if(mPingPong)
            j = 1;
        else
            j = 0;
        if(!mPingPong)
            flag = true;
        else
            flag = false;
        mPingPong = flag;
        updateBgScaling(frame, frame1, mBackgroundFitModeChanged);
        mBackgroundFitModeChanged = false;
        copyShaderProgram.process(frame, mVideoInput);
        copyShaderProgram.process(frame1, mBgInput);
        mVideoInput.generateMipMap();
        mVideoInput.setTextureParameter(10241, 9985);
        mBgInput.generateMipMap();
        mBgInput.setTextureParameter(10241, 9985);
        if(mStartLearning) {
            copyShaderProgram.process(mVideoInput, mBgMean[i]);
            mStartLearning = false;
        }
        aframe = new Frame[3];
        aframe[0] = mVideoInput;
        aframe[1] = mBgMean[i];
        aframe[2] = mBgVariance[i];
        mBgDistProgram.process(aframe, mDistance);
        mDistance.generateMipMap();
        mDistance.setTextureParameter(10241, 9985);
        mBgMaskProgram.process(mDistance, mMask);
        mMask.generateMipMap();
        mMask.setTextureParameter(10241, 9985);
        aframe1 = new Frame[2];
        aframe1[0] = mVideoInput;
        aframe1[1] = mBgInput;
        mAutomaticWhiteBalanceProgram.process(aframe1, mAutoWB);
        if(mFrameCount <= mLearningDuration) {
            pushOutput("video", frame);
            if(mFrameCount == mLearningDuration - mLearningVerifyDuration) {
                copyShaderProgram.process(mMask, mMaskVerify[j]);
                mBgUpdateMeanProgram.setHostValue("bg_adapt_rate", Float.valueOf(mAdaptRateBg));
                mBgUpdateMeanProgram.setHostValue("fg_adapt_rate", Float.valueOf(mAdaptRateFg));
                mBgUpdateVarianceProgram.setHostValue("bg_adapt_rate", Float.valueOf(mAdaptRateBg));
                mBgUpdateVarianceProgram.setHostValue("fg_adapt_rate", Float.valueOf(mAdaptRateFg));
            } else
            if(mFrameCount > mLearningDuration - mLearningVerifyDuration) {
                Frame aframe5[] = new Frame[2];
                aframe5[0] = mMaskVerify[i];
                aframe5[1] = mMask;
                mMaskVerifyProgram.process(aframe5, mMaskVerify[j]);
                mMaskVerify[j].generateMipMap();
                mMaskVerify[j].setTextureParameter(10241, 9985);
            }
            if(mFrameCount == mLearningDuration) {
                copyShaderProgram.process(mMaskVerify[j], mMaskAverage);
                int k = 0xff & mMaskAverage.getData().array()[3];
                if(mLogVerbose) {
                    Object aobj2[] = new Object[2];
                    aobj2[0] = Integer.valueOf(k);
                    aobj2[1] = Integer.valueOf(20);
                    Log.v("BackDropperFilter", String.format("Mask_average is %d, threshold is %d", aobj2));
                }
                Frame aframe3[];
                Frame aframe4[];
                Frame frame3;
                Frame frame4;
                if(k >= 20) {
                    mStartLearning = true;
                } else {
                    if(mLogVerbose)
                        Log.v("BackDropperFilter", "Learning done");
                    if(mLearningDoneListener != null)
                        mLearningDoneListener.onLearningDone(this);
                }
            }
        } else {
            Frame frame2 = filtercontext.getFrameManager().newFrame(frame.getFormat());
            Frame aframe2[] = new Frame[4];
            aframe2[0] = frame;
            aframe2[1] = frame1;
            aframe2[2] = mMask;
            aframe2[3] = mAutoWB;
            mBgSubtractProgram.process(aframe2, frame2);
            pushOutput("video", frame2);
            frame2.release();
        }
        if(mFrameCount < mLearningDuration - mLearningVerifyDuration || (double)mAdaptRateBg > 0.0D || (double)mAdaptRateFg > 0.0D) {
            aframe3 = new Frame[3];
            aframe3[0] = mVideoInput;
            aframe3[1] = mBgMean[i];
            aframe3[2] = mMask;
            mBgUpdateMeanProgram.process(aframe3, mBgMean[j]);
            mBgMean[j].generateMipMap();
            mBgMean[j].setTextureParameter(10241, 9985);
            aframe4 = new Frame[4];
            aframe4[0] = mVideoInput;
            aframe4[1] = mBgMean[i];
            aframe4[2] = mBgVariance[i];
            aframe4[3] = mMask;
            mBgUpdateVarianceProgram.process(aframe4, mBgVariance[j]);
            mBgVariance[j].generateMipMap();
            mBgVariance[j].setTextureParameter(10241, 9985);
        }
        if(mProvideDebugOutputs) {
            frame3 = filtercontext.getFrameManager().newFrame(frame.getFormat());
            mCopyOutProgram.process(frame, frame3);
            pushOutput("debug1", frame3);
            frame3.release();
            frame4 = filtercontext.getFrameManager().newFrame(mMemoryFormat);
            mCopyOutProgram.process(mMask, frame4);
            pushOutput("debug2", frame4);
            frame4.release();
        }
        mFrameCount = 1 + mFrameCount;
        if(mLogVerbose && mFrameCount % 30 == 0)
            if(startTime == -1L) {
                filtercontext.getGLEnvironment().activate();
                GLES20.glFinish();
                startTime = SystemClock.elapsedRealtime();
            } else {
                filtercontext.getGLEnvironment().activate();
                GLES20.glFinish();
                long l = SystemClock.elapsedRealtime();
                StringBuilder stringbuilder = (new StringBuilder()).append("Avg. frame duration: ");
                Object aobj[] = new Object[1];
                aobj[0] = Double.valueOf((double)(l - startTime) / 30D);
                StringBuilder stringbuilder1 = stringbuilder.append(String.format("%.2f", aobj)).append(" ms. Avg. fps: ");
                Object aobj1[] = new Object[1];
                aobj1[0] = Double.valueOf(1000D / ((double)(l - startTime) / 30D));
                Log.v("BackDropperFilter", stringbuilder1.append(String.format("%.2f", aobj1)).toString());
                startTime = l;
            }
    }

    /**
     * @deprecated Method relearn is deprecated
     */

    public void relearn() {
        this;
        JVM INSTR monitorenter ;
        mStartLearning = true;
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    public void setupPorts() {
        MutableFrameFormat mutableframeformat = ImageFormat.create(3, 0);
        String as[] = mInputNames;
        int i = as.length;
        for(int j = 0; j < i; j++)
            addMaskedInputPort(as[j], mutableframeformat);

        String as1[] = mOutputNames;
        int k = as1.length;
        for(int l = 0; l < k; l++)
            addOutputBasedOnInput(as1[l], "video");

        if(mProvideDebugOutputs) {
            String as2[] = mDebugOutputNames;
            int i1 = as2.length;
            for(int j1 = 0; j1 < i1; j1++)
                addOutputBasedOnInput(as2[j1], "video");

        }
    }

    private static final float DEFAULT_ACCEPT_STDDEV = 0.85F;
    private static final float DEFAULT_ADAPT_RATE_BG = 0F;
    private static final float DEFAULT_ADAPT_RATE_FG = 0F;
    private static final String DEFAULT_AUTO_WB_SCALE = "0.25";
    private static final float DEFAULT_BG_FIT_TRANSFORM[];
    private static final float DEFAULT_EXPOSURE_CHANGE = 1F;
    private static final int DEFAULT_HIER_LRG_EXPONENT = 3;
    private static final float DEFAULT_HIER_LRG_SCALE = 0.7F;
    private static final int DEFAULT_HIER_MID_EXPONENT = 2;
    private static final float DEFAULT_HIER_MID_SCALE = 0.6F;
    private static final int DEFAULT_HIER_SML_EXPONENT = 0;
    private static final float DEFAULT_HIER_SML_SCALE = 0.5F;
    private static final float DEFAULT_LEARNING_ADAPT_RATE = 0.2F;
    private static final int DEFAULT_LEARNING_DONE_THRESHOLD = 20;
    private static final int DEFAULT_LEARNING_DURATION = 40;
    private static final int DEFAULT_LEARNING_VERIFY_DURATION = 10;
    private static final float DEFAULT_MASK_BLEND_BG = 0.65F;
    private static final float DEFAULT_MASK_BLEND_FG = 0.95F;
    private static final int DEFAULT_MASK_HEIGHT_EXPONENT = 8;
    private static final float DEFAULT_MASK_VERIFY_RATE = 0.25F;
    private static final int DEFAULT_MASK_WIDTH_EXPONENT = 8;
    private static final float DEFAULT_UV_SCALE_FACTOR = 1.35F;
    private static final float DEFAULT_WHITE_BALANCE_BLUE_CHANGE = 0F;
    private static final float DEFAULT_WHITE_BALANCE_RED_CHANGE = 0F;
    private static final int DEFAULT_WHITE_BALANCE_TOGGLE = 0;
    private static final float DEFAULT_Y_SCALE_FACTOR = 0.4F;
    private static final String DISTANCE_STORAGE_SCALE = "0.6";
    private static final String MASK_SMOOTH_EXPONENT = "2.0";
    private static final String MIN_VARIANCE = "3.0";
    private static final String RGB_TO_YUV_MATRIX = "0.299, -0.168736,  0.5,      0.000, 0.587, -0.331264, -0.418688, 0.000, 0.114,  0.5,      -0.081312, 0.000, 0.000,  0.5,       0.5,      1.000 ";
    private static final String TAG = "BackDropperFilter";
    private static final String VARIANCE_STORAGE_SCALE = "5.0";
    private static final String mAutomaticWhiteBalance = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float pyramid_depth;\nuniform bool autowb_toggle;\nvarying vec2 v_texcoord;\nvoid main() {\n   vec4 mean_video = texture2D(tex_sampler_0, v_texcoord, pyramid_depth);\n   vec4 mean_bg = texture2D(tex_sampler_1, v_texcoord, pyramid_depth);\n   float green_normalizer = mean_video.g / mean_bg.g;\n   vec4 adjusted_value = vec4(mean_bg.r / mean_video.r * green_normalizer, 1., \n                         mean_bg.b / mean_video.b * green_normalizer, 1.) * auto_wb_scale; \n   gl_FragColor = autowb_toggle ? adjusted_value : vec4(auto_wb_scale);\n}\n";
    private static final String mBgDistanceShader = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float subsample_level;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord, subsample_level);\n  vec4 fg = coeff_yuv * vec4(fg_rgb.rgb, 1.);\n  vec4 mean = texture2D(tex_sampler_1, v_texcoord);\n  vec4 variance = inv_var_scale * texture2D(tex_sampler_2, v_texcoord);\n\n  float dist_y = gauss_dist_y(fg.r, mean.r, variance.r);\n  float dist_uv = gauss_dist_uv(fg.gb, mean.gb, variance.gb);\n  gl_FragColor = vec4(0.5*fg.rg, dist_scale*dist_y, dist_scale*dist_uv);\n}\n";
    private static final String mBgMaskShader = "uniform sampler2D tex_sampler_0;\nuniform float accept_variance;\nuniform vec2 yuv_weights;\nuniform float scale_lrg;\nuniform float scale_mid;\nuniform float scale_sml;\nuniform float exp_lrg;\nuniform float exp_mid;\nuniform float exp_sml;\nvarying vec2 v_texcoord;\nbool is_fg(vec2 dist_yc, float accept_variance) {\n  return ( dot(yuv_weights, dist_yc) >= accept_variance );\n}\nvoid main() {\n  vec4 dist_lrg_sc = texture2D(tex_sampler_0, v_texcoord, exp_lrg);\n  vec4 dist_mid_sc = texture2D(tex_sampler_0, v_texcoord, exp_mid);\n  vec4 dist_sml_sc = texture2D(tex_sampler_0, v_texcoord, exp_sml);\n  vec2 dist_lrg = inv_dist_scale * dist_lrg_sc.ba;\n  vec2 dist_mid = inv_dist_scale * dist_mid_sc.ba;\n  vec2 dist_sml = inv_dist_scale * dist_sml_sc.ba;\n  vec2 norm_dist = 0.75 * dist_sml / accept_variance;\n  bool is_fg_lrg = is_fg(dist_lrg, accept_variance * scale_lrg);\n  bool is_fg_mid = is_fg_lrg || is_fg(dist_mid, accept_variance * scale_mid);\n  float is_fg_sml =\n      float(is_fg_mid || is_fg(dist_sml, accept_variance * scale_sml));\n  float alpha = 0.5 * is_fg_sml + 0.3 * float(is_fg_mid) + 0.2 * float(is_fg_lrg);\n  gl_FragColor = vec4(alpha, norm_dist, is_fg_sml);\n}\n";
    private static final String mBgSubtractForceShader = "  vec4 ghost_rgb = (fg_adjusted * 0.7 + vec4(0.3,0.3,0.4,0.))*0.65 + \n                   0.35*bg_rgb;\n  float glow_start = 0.75 * mask_blend_bg; \n  float glow_max   = mask_blend_bg; \n  gl_FragColor = mask.a < glow_start ? bg_rgb : \n                 mask.a < glow_max ? mix(bg_rgb, vec4(0.9,0.9,1.0,1.0), \n                                     (mask.a - glow_start) / (glow_max - glow_start) ) : \n                 mask.a < mask_blend_fg ? mix(vec4(0.9,0.9,1.0,1.0), ghost_rgb, \n                                    (mask.a - glow_max) / (mask_blend_fg - glow_max) ) : \n                 ghost_rgb;\n}\n";
    private static final String mBgSubtractShader = "uniform mat3 bg_fit_transform;\nuniform float mask_blend_bg;\nuniform float mask_blend_fg;\nuniform float exposure_change;\nuniform float whitebalancered_change;\nuniform float whitebalanceblue_change;\nuniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform sampler2D tex_sampler_3;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec2 bg_texcoord = (bg_fit_transform * vec3(v_texcoord, 1.)).xy;\n  vec4 bg_rgb = texture2D(tex_sampler_1, bg_texcoord);\n  vec4 wb_auto_scale = texture2D(tex_sampler_3, v_texcoord) * exposure_change / auto_wb_scale;\n  vec4 wb_manual_scale = vec4(1. + whitebalancered_change, 1., 1. + whitebalanceblue_change, 1.);\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord);\n  vec4 fg_adjusted = fg_rgb * wb_manual_scale * wb_auto_scale;\n  vec4 mask = texture2D(tex_sampler_2, v_texcoord, \n                      2.0);\n  float alpha = smoothstep(mask_blend_bg, mask_blend_fg, mask.a);\n  gl_FragColor = mix(bg_rgb, fg_adjusted, alpha);\n";
    private static final String mDebugOutputNames[];
    private static final String mInputNames[];
    private static final String mMaskVerifyShader = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform float verify_rate;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 lastmask = texture2D(tex_sampler_0, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_1, v_texcoord);\n  float newmask = mix(lastmask.a, mask.a, verify_rate);\n  gl_FragColor = vec4(0., 0., 0., newmask);\n}\n";
    private static final String mOutputNames[];
    private static String mSharedUtilShader = "precision mediump float;\nuniform float fg_adapt_rate;\nuniform float bg_adapt_rate;\nconst mat4 coeff_yuv = mat4(0.299, -0.168736,  0.5,      0.000, 0.587, -0.331264, -0.418688, 0.000, 0.114,  0.5,      -0.081312, 0.000, 0.000,  0.5,       0.5,      1.000 );\nconst float dist_scale = 0.6;\nconst float inv_dist_scale = 1. / dist_scale;\nconst float var_scale=5.0;\nconst float inv_var_scale = 1. / var_scale;\nconst float min_variance = inv_var_scale *3.0/ 256.;\nconst float auto_wb_scale = 0.25;\n\nfloat gauss_dist_y(float y, float mean, float variance) {\n  float dist = (y - mean) * (y - mean) / variance;\n  return dist;\n}\nfloat gauss_dist_uv(vec2 uv, vec2 mean, vec2 variance) {\n  vec2 dist = (uv - mean) * (uv - mean) / variance;\n  return dist.r + dist.g;\n}\nfloat local_adapt_rate(float alpha) {\n  return mix(bg_adapt_rate, fg_adapt_rate, alpha);\n}\n\n";
    private static final String mUpdateBgModelMeanShader = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform float subsample_level;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord, subsample_level);\n  vec4 fg = coeff_yuv * vec4(fg_rgb.rgb, 1.);\n  vec4 mean = texture2D(tex_sampler_1, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_2, v_texcoord, \n                      2.0);\n\n  float alpha = local_adapt_rate(mask.a);\n  vec4 new_mean = mix(mean, fg, alpha);\n  gl_FragColor = new_mean;\n}\n";
    private static final String mUpdateBgModelVarianceShader = "uniform sampler2D tex_sampler_0;\nuniform sampler2D tex_sampler_1;\nuniform sampler2D tex_sampler_2;\nuniform sampler2D tex_sampler_3;\nuniform float subsample_level;\nvarying vec2 v_texcoord;\nvoid main() {\n  vec4 fg_rgb = texture2D(tex_sampler_0, v_texcoord, subsample_level);\n  vec4 fg = coeff_yuv * vec4(fg_rgb.rgb, 1.);\n  vec4 mean = texture2D(tex_sampler_1, v_texcoord);\n  vec4 variance = inv_var_scale * texture2D(tex_sampler_2, v_texcoord);\n  vec4 mask = texture2D(tex_sampler_3, v_texcoord, \n                      2.0);\n\n  float alpha = local_adapt_rate(mask.a);\n  vec4 cur_variance = (fg-mean)*(fg-mean);\n  vec4 new_variance = mix(variance, cur_variance, alpha);\n  new_variance = max(new_variance, vec4(min_variance));\n  gl_FragColor = var_scale * new_variance;\n}\n";
    private final int BACKGROUND_FILL_CROP;
    private final int BACKGROUND_FIT;
    private final int BACKGROUND_STRETCH;
    private ShaderProgram copyShaderProgram;
    private boolean isOpen;
    private float mAcceptStddev;
    private float mAdaptRateBg;
    private float mAdaptRateFg;
    private float mAdaptRateLearning;
    private GLFrame mAutoWB;
    private int mAutoWBToggle;
    private ShaderProgram mAutomaticWhiteBalanceProgram;
    private MutableFrameFormat mAverageFormat;
    private int mBackgroundFitMode;
    private boolean mBackgroundFitModeChanged;
    private ShaderProgram mBgDistProgram;
    private GLFrame mBgInput;
    private ShaderProgram mBgMaskProgram;
    private GLFrame mBgMean[];
    private ShaderProgram mBgSubtractProgram;
    private ShaderProgram mBgUpdateMeanProgram;
    private ShaderProgram mBgUpdateVarianceProgram;
    private GLFrame mBgVariance[];
    private float mChromaScale;
    private ShaderProgram mCopyOutProgram;
    private GLFrame mDistance;
    private float mExposureChange;
    private int mFrameCount;
    private int mHierarchyLrgExp;
    private float mHierarchyLrgScale;
    private int mHierarchyMidExp;
    private float mHierarchyMidScale;
    private int mHierarchySmlExp;
    private float mHierarchySmlScale;
    private LearningDoneListener mLearningDoneListener;
    private int mLearningDuration;
    private int mLearningVerifyDuration;
    private final boolean mLogVerbose;
    private float mLumScale;
    private GLFrame mMask;
    private GLFrame mMaskAverage;
    private float mMaskBg;
    private float mMaskFg;
    private MutableFrameFormat mMaskFormat;
    private int mMaskHeightExp;
    private GLFrame mMaskVerify[];
    private ShaderProgram mMaskVerifyProgram;
    private int mMaskWidthExp;
    private MutableFrameFormat mMemoryFormat;
    private boolean mMirrorBg;
    private int mOrientation;
    private FrameFormat mOutputFormat;
    private boolean mPingPong;
    private boolean mProvideDebugOutputs;
    private int mPyramidDepth;
    private float mRelativeAspect;
    private boolean mStartLearning;
    private int mSubsampleLevel;
    private boolean mUseTheForce;
    private float mVerifyRate;
    private GLFrame mVideoInput;
    private float mWhiteBalanceBlueChange;
    private float mWhiteBalanceRedChange;
    private long startTime;

    static  {
        float af[] = new float[9];
        af[0] = 1.0F;
        af[1] = 0.0F;
        af[2] = 0.0F;
        af[3] = 0.0F;
        af[4] = 1.0F;
        af[5] = 0.0F;
        af[6] = 0.0F;
        af[7] = 0.0F;
        af[8] = 1.0F;
        DEFAULT_BG_FIT_TRANSFORM = af;
        String as[] = new String[2];
        as[0] = "video";
        as[1] = "background";
        mInputNames = as;
        String as1[] = new String[1];
        as1[0] = "video";
        mOutputNames = as1;
        String as2[] = new String[2];
        as2[0] = "debug1";
        as2[1] = "debug2";
        mDebugOutputNames = as2;
    }
}

