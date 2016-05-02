/*
 * ============================================================
 * [ SYSTEM ]		: Web::Java ?? ( HOBOKEN )
 * [ PROJECT ]		: HOBOKEN Project
 * 
 * $Id: ReadSpreadDispatchAction.java 1094 2009-07-29 09:49:06Z mezawa_takuji $
 * ============================================================
 */

package example.hoboken.action.examG;

import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import example.hoboken.Constant;

import prj.hoboken.patrasche.action.BaseMappingDispatchAction;
import prj.hoboken.patrasche.common.LoggingBuffer;
import prj.hoboken.patrasche.config.WebContextUtil;
import prj.hoboken.patrasche.form.MutableForm;
import prj.hoboken.patrasche.service.ServiceResult;
import prj.hoboken.patrasche.service.poi.SpreadAccessService;
import prj.hoboken.patrasche.service.transport.TransportUtil;
import prj.hoboken.patrasche.util.SvoUtil;

/**
 * POI?????????????????????????????????????????
 * 
 * 
 * NOTE:<br>
 * <blockquote></blockquote>
 * 
 * <p>$Revision: 1094 $<br>$Date: 2009-07-29 18:49:06 +0900 (?, 29 7 2009) $</p>
 *
 * @since  J2SDK 1.4 : Servlet2.3/JSP1.2 : Apache Struts 1.2 : SpringFramework 1.2
 * @since  Patrasche 3.0
 * 
 * @author
 *     Mezawa Takuji  ( HOBOKEN Project )<br>
 *     <!-- *???*  ( CompanyName )<br> -->
 */
public final class ReadSpreadDispatchAction extends BaseMappingDispatchAction {
    
    private final Log log = LogFactory.getLog(ReadSpreadDispatchAction.class);
    
    /**
     * ???????????????????????????????????????????
     * 
     * @param mapping ????????????
     * @param form ???????????
     * @param request HTTP???????
     * @param response HTTP???????
     * @return ?????
     * @throws Exception<br>
     * <ul type="disk">
     * <li>IllegalSpreadAccessException
     * <li>IOException
     * </ul>
     */
    public ActionForward readCellProcess(
        ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        String methodName = "readCellProcess(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)";
        LoggingBuffer.markup(log, true, methodName);
        
        
        List checkedLimit = TransportUtil.checkLimitSize(mapping, request.getContentLength());
        if (((Boolean)checkedLimit.get(0)).booleanValue()) {
            MutableForm mute = WebContextUtil.castToMutableForm(form);
            
            int sheetNum = Integer.parseInt(mute.getValue("shnum"));
            String sheetName = mute.getValue("shname");
            int rowpos = Integer.parseInt(mute.getValue("rowpos"));
            int colpos = Integer.parseInt(mute.getValue("colpos"));
            
            
            FormFile upfile = (FormFile)mute.getProperty("upfile");
            InputStream stream = upfile.getInputStream();
            
            SpreadAccessService spread = (SpreadAccessService)super.getService("readCellService");
            
            ServiceResult sr = null;
            
            if (SvoUtil.isUseString(sheetName)) {
                spread.setPosition(rowpos, colpos, sheetName);      // ?????????
                sr = spread.readSpreadData(stream, new String[] {sheetName});       // ?????????????????
            } else {
                spread.setPosition(rowpos, colpos, sheetNum);       // ?????????
                sr = spread.readSpreadData(stream, new int[] {sheetNum});        // ??????????????????
            }
            
            /*
             * ???????????????????????????????????????????????
             */
            
            if (sr.isNormal(Constant.POI_CELL_DATA)) {
                request.setAttribute("cellData", ((List)sr.getExecution(Constant.POI_CELL_DATA)).get(0));
            } else {
                request.setAttribute("cellData", null);
            }
            
            super.setUIMessageRequest(request, sr.getMessageGroup(0), sr.getMessageKey(0), sr.getMessageArgs(0));
            
            // ???
            stream.close();
            upfile.destroy();
            
            mute.setProperty("shnum", String.valueOf(sheetNum));
            mute.setProperty("rowpos", String.valueOf(rowpos));
            mute.setProperty("colpos", String.valueOf(colpos));
            
        } else {
            super.setUIMessageRequest(request, Constant.ERROR, "system.error.522", new Object[] {checkedLimit.get(1), checkedLimit.get(2)});
        }
        
        
        LoggingBuffer.markup(log, false, methodName);
        
        return mapping.findForward(Constant.SUCCESS);
    }
    
    /**
     * ??????????????????????????????????????????????????????
     * 
     * @param mapping ????????????
     * @param form ???????????
     * @param request HTTP???????
     * @param response HTTP???????
     * @return ?????
     * @throws Exception<br>
     * <ul type="disk">
     * <li>IllegalSpreadAccessException
     * <li>IOException
     * </ul>
     */
    public ActionForward offsetCellProcess(
        ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        String methodName = "offsetCellProcess(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)";
        LoggingBuffer.markup(log, true, methodName);
        
        
        MutableForm mute = WebContextUtil.castToMutableForm(form);
        
        int sheetNum = Integer.parseInt(mute.getValue("shnum"));
        String sheetName = mute.getValue("shname");
        
        
        FormFile upfile = (FormFile)mute.getProperty("upfile");
        InputStream stream = upfile.getInputStream();
        
        SpreadAccessService spread = (SpreadAccessService)super.getService("readCellsByOffsetService");
        
        ServiceResult sr = null;
        
        // ??????????????????????
        
        if (SvoUtil.isUseString(sheetName)) {
            sr = spread.readSpreadData(stream, new String[] {sheetName});       // ?????????????????
        } else {
            sr = spread.readSpreadData(stream, new int[] {sheetNum});        // ??????????????????
        }
        
        /*
         * ???????????????????????????????????????????????
         */
        
        if (sr.isNormal(Constant.POI_OFFSET_CELLS)) {
            request.setAttribute("offsetCells", ((List)sr.getExecution(Constant.POI_OFFSET_CELLS)).get(0));
        } else {
            request.setAttribute("offsetCells", null);
        }
        
        super.setUIMessageRequest(request, sr.getMessageGroup(0), sr.getMessageKey(0), sr.getMessageArgs(0));
        
        // ???
        stream.close();
        upfile.destroy();
        
        mute.setProperty("shnum", String.valueOf(sheetNum));
        
        
        LoggingBuffer.markup(log, false, methodName);
        
        return mapping.findForward(Constant.SUCCESS);
    }
    
    /**
     * ?????????????????????????????????????????????
     * 
     * @param mapping ????????????
     * @param form ???????????
     * @param request HTTP???????
     * @param response HTTP???????
     * @return ?????
     * @throws Exception<br>
     * <ul type="disk">
     * <li>IllegalSpreadAccessException
     * <li>IOException
     * </ul>
     */
    public ActionForward readRangeProcess(
        ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        String methodName = "readRangeProcess(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)";
        LoggingBuffer.markup(log, true, methodName);
        
        
        MutableForm mute = WebContextUtil.castToMutableForm(form);
        
        int sheetNum = Integer.parseInt(mute.getValue("shnum"));
        String sheetName = mute.getValue("shname");
        int startrow = Integer.parseInt(mute.getValue("startrow"));
        int endrow = Integer.parseInt(mute.getValue("endrow"));
        int startcol = Integer.parseInt(mute.getValue("startcol"));
        int endcol = Integer.parseInt(mute.getValue("endcol"));
        
        FormFile upfile = (FormFile)mute.getProperty("upfile");
        InputStream stream = upfile.getInputStream();
        
        SpreadAccessService spread = (SpreadAccessService)super.getService("readRangeCellsService");
        
        ServiceResult sr = null;
        
        if (SvoUtil.isUseString(sheetName)) {
            spread.setPositions(new int[] {startrow, startcol, endrow, endcol}, sheetName);     // ?????????
            sr = spread.readSpreadData(stream, new String[] {sheetName});       // ?????????????????
        } else {
            spread.setPositions(new int[] {startrow, startcol, endrow, endcol}, sheetNum);     // ?????????
            sr = spread.readSpreadData(stream, new int[] {sheetNum});        // ??????????????????
        }
        
        /*
         * ???????????????????????????????????????????????
         */
        
        if (sr.isNormal(Constant.POI_CELL_DATA)) {
            request.setAttribute("rangeData", ((List)sr.getExecution(Constant.POI_CELL_DATA)).get(0));
        } else {
            request.setAttribute("rangeData", null);
        }
        
        super.setUIMessageRequest(request, sr.getMessageGroup(0), sr.getMessageKey(0), sr.getMessageArgs(0));
        
        // ???
        stream.close();
        upfile.destroy();
        
        mute.setProperty("shnum", String.valueOf(sheetNum));
        mute.setProperty("startrow", String.valueOf(startrow));
        mute.setProperty("endrow", String.valueOf(endrow));
        mute.setProperty("startcol", String.valueOf(startcol));
        mute.setProperty("endcol", String.valueOf(endcol));
        
        
        LoggingBuffer.markup(log, false, methodName);
        
        return mapping.findForward(Constant.SUCCESS);
    }
    
    /**
     * ????????????????????????????????????????????????????????
     * 
     * @param mapping ????????????
     * @param form ???????????
     * @param request HTTP???????
     * @param response HTTP???????
     * @return ?????
     * @throws Exception<br>
     * <ul type="disk">
     * <li>IllegalSpreadAccessException
     * <li>IOException
     * </ul>
     */
    public ActionForward offsetRangeProcess(
        ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response)
        throws Exception
    {
        String methodName = "offsetRangeProcess(ActionMapping, ActionForm, HttpServletRequest, HttpServletResponse)";
        LoggingBuffer.markup(log, true, methodName);
        
        
        MutableForm mute = WebContextUtil.castToMutableForm(form);
        
        int sheetNum = Integer.parseInt(mute.getValue("shnum"));
        String sheetName = mute.getValue("shname");
        
        FormFile upfile = (FormFile)mute.getProperty("upfile");
        InputStream stream = upfile.getInputStream();
        
        SpreadAccessService spread = (SpreadAccessService)super.getService("readRangeByOffsetService");
        
        ServiceResult sr = null;
        
        // ??????????????????????
        
        if (SvoUtil.isUseString(sheetName)) {
            sr = spread.readSpreadData(stream, new String[] {sheetName});        // ?????????????????
        } else {
            sr = spread.readSpreadData(stream, new int[] {sheetNum});     // ??????????????????
        }
        
        /*
         * ???????????????????????????????????????????????
         */
        
        if (sr.isNormal(Constant.POI_OFFSET_RANGE)) {
            request.setAttribute("offsetRange", ((List)sr.getExecution(Constant.POI_OFFSET_RANGE)).get(0));
        } else {
            request.setAttribute("offsetRange", null);
        }
        
        super.setUIMessageRequest(request, sr.getMessageGroup(0), sr.getMessageKey(0), sr.getMessageArgs(0));
        
        // ???
        stream.close();
        upfile.destroy();
        
        mute.setProperty("shnum", String.valueOf(sheetNum));
        
        
        LoggingBuffer.markup(log, false, methodName);
        
        return mapping.findForward(Constant.SUCCESS);
    }
}


/* Copyright (C) 2005, HOBOKEN Project, All Rights Reserved. */
