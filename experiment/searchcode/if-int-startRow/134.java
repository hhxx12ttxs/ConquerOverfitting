package com.fjmilens3.fop.opendocument;

import com.fjmilens3.fop.opendocument.states.ODTHandlerStateManager;
import com.fjmilens3.fop.opendocument.utilities.*;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.fop.apps.FOUserAgent;
import org.apache.fop.apps.FormattingResults;
import org.apache.fop.fo.FOEventHandler;
import org.apache.fop.fo.flow.Block;
import org.apache.fop.fo.properties.CommonBorderPaddingBackground;
import org.apache.fop.fo.properties.CommonMarginBlock;
import org.odftoolkit.odfdom.dom.element.style.StyleParagraphPropertiesElement;
import org.odftoolkit.odfdom.dom.element.style.StyleStyleElement;
import org.odftoolkit.odfdom.dom.element.style.StyleTabStopsElement;
import org.odftoolkit.odfdom.dom.element.style.StyleTablePropertiesElement;
import org.odftoolkit.odfdom.dom.element.table.TableTableElement;
import org.odftoolkit.odfdom.dom.element.text.TextPElement;
import org.odftoolkit.odfdom.dom.element.text.TextParagraphElementBase;
import org.odftoolkit.odfdom.dom.element.text.TextSpanElement;
import org.odftoolkit.odfdom.dom.style.props.OdfParagraphProperties;
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle;
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph;
import org.odftoolkit.simple.TextDocument;
import org.odftoolkit.simple.common.field.Fields;
import org.odftoolkit.simple.common.field.PageNumberField;
import org.odftoolkit.simple.style.*;
import org.odftoolkit.simple.table.Cell;
import org.odftoolkit.simple.table.CellRange;
import org.odftoolkit.simple.table.Column;
import org.odftoolkit.simple.table.Table;
import org.odftoolkit.simple.text.Paragraph;
import org.odftoolkit.simple.text.ParagraphStyleHandler;
import org.odftoolkit.simple.text.Span;

import java.io.OutputStream;

/**
 * FOP event handler implemented as part of the integration for ODT output from FOP.
 *
 * @author Frederick John Milens III
 */
public final class ODTHandler extends FOEventHandler {

    /**
     * Constant representing the "xsl-region-before" flow name for static content sections (the header).
     */
    public static final String XSL_REGION_BEFORE = "xsl-region-before";

    /**
     * Constant representing the "xsl-region-after" flow name for static content sections (the footer).
     */
    public static final String XSL_REGION_AFTER = "xsl-region-after";

    /**
     * The Apache Commons logger for this ODTHandler.
     */
    private final Log log = LogFactory.getLog(getClass());

    /**
     * The text document that the ODTHandler is writing content to.
     */
    private TextDocument textDocument;

    /**
     * The output stream that the ODT file will be saved to when the processing has finished.
     */
    private OutputStream outputStream;

    /**
     * The state manager for the ODTHandler.
     */
    private ODTHandlerStateManager stateManager = new ODTHandlerStateManager();

    /**
     * Constructor.
     *
     * @param foUserAgent  The Apache FOP user agent.
     * @param outputStream The output stream that the ODT file will be saved to when the processing has finished.
     * @param textDocument The text document that the ODTHandler is writing content to.
     */
    public ODTHandler(FOUserAgent foUserAgent, OutputStream outputStream, TextDocument textDocument) {
        super(foUserAgent);
        this.outputStream = outputStream;
        this.textDocument = textDocument;
    }

    /**
     * Starts a new document.
     */
    @Override
    public void startDocument() throws org.xml.sax.SAXException {
        stateManager.pushDocumentState(textDocument);
    }

    /**
     * Ends the document, writing the contents of the document to the output stream.
     */
    @Override
    public void endDocument() throws org.xml.sax.SAXException {
        try {
            stateManager.popState();
            textDocument.save(outputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes a page number field as a number to the ODT output.
     *
     * @param pagenum The page number.
     */
    public void startPageNumber(org.apache.fop.fo.flow.PageNumber pagenum) {
        Paragraph paragraph = stateManager.getCurrentState().getParagraph();
        PageNumberField numberField = Fields.createCurrentPageNumberField(paragraph.getOdfElement());
        numberField.setNumberFormat(NumberFormat.HINDU_ARABIC_NUMBER);
        numberField.setDisplayPage(PageNumberField.DisplayType.CURRENT_PAGE);
    }

    /**
     * Starts a new block, which is mapped to ODT by means of creating a new paragraph based on the current state of
     * this handler.  Initial styles for the paragraph are also set from the contents of the block, as well as handling
     * for page breaks before the start of the new paragraph.
     *
     * @param bl The block.
     */
    public void startBlock(org.apache.fop.fo.flow.Block bl) {
        if (!stateManager.isProcessingListLabel()) {

            if (bl.getBreakBefore() == Block.EN_PAGE) {
                textDocument.addPageBreak();
            }

            Paragraph paragraph = stateManager.getCurrentState().newParagraph();
            stateManager.pushBlockState(paragraph);
            OdfStyle style = paragraph.getStyleHandler().getStyleElementForWrite();
            CommonBorderPaddingBackground commonBorderPaddingBackground = bl.getCommonBorderPaddingBackground();

            if (commonBorderPaddingBackground.hasBackground()) {
                style.setProperty(OdfParagraphProperties.BackgroundColor, ODTColorUtilities.calculateColorAsHex(commonBorderPaddingBackground.getBackgroundColor()));
            }
            style.setProperty(OdfParagraphProperties.PaddingTop, ODTMeasurementUtilities.convertMillipointsToPoints(commonBorderPaddingBackground.getPaddingBefore(false, null)) + "pt");
            style.setProperty(OdfParagraphProperties.PaddingBottom, ODTMeasurementUtilities.convertMillipointsToPoints(commonBorderPaddingBackground.getPaddingAfter(false, null)) + "pt");
            style.setProperty(OdfParagraphProperties.PaddingLeft, ODTMeasurementUtilities.convertMillipointsToPoints(commonBorderPaddingBackground.getPaddingStart(false, null)) + "pt");
            style.setProperty(OdfParagraphProperties.PaddingRight, ODTMeasurementUtilities.convertMillipointsToPoints(commonBorderPaddingBackground.getPaddingEnd(false, null)) + "pt");

            CommonMarginBlock commonMarginBlock = bl.getCommonMarginBlock();
            style.setProperty(OdfParagraphProperties.MarginTop, ODTMeasurementUtilities.convertMillipointsToPoints(commonMarginBlock.spaceBefore) + "pt");
            style.setProperty(OdfParagraphProperties.MarginBottom, ODTMeasurementUtilities.convertMillipointsToPoints(commonMarginBlock.spaceAfter) + "pt");
            style.setProperty(OdfParagraphProperties.MarginLeft, ODTMeasurementUtilities.convertMillipointsToPoints(commonMarginBlock.marginLeft) + "pt");
            style.setProperty(OdfParagraphProperties.MarginRight, ODTMeasurementUtilities.convertMillipointsToPoints(commonMarginBlock.marginRight) + "pt");
            style.setProperty(OdfParagraphProperties.LineHeight, ODTMeasurementUtilities.convertMillipointsToPoints(bl.getLineHeight()) + "pt");

            ParagraphStyleHandler styleHandler = paragraph.getStyleHandler();
            paragraph.setHorizontalAlignment(ODTFontUtilities.calculateHorizontalAlignmentType(bl.getTextAlign()));

            TextProperties textProperties = styleHandler.getTextPropertiesForWrite();
            textProperties.setFont(ODTFontUtilities.calculateFont(bl.getCommonFont(), bl.getColor()));
        }
    }

    /**
     * Ends a new block, removing the current block state and its associated paragraph from the list of states.  The
     * method also handles the case where a page break should occur after the block.
     *
     * @param bl The block.
     */
    public void endBlock(org.apache.fop.fo.flow.Block bl) {
        if (!stateManager.isProcessingListLabel()) {
            if (bl.getBreakAfter() == Block.EN_PAGE) {
                textDocument.addPageBreak();
            }
            stateManager.popState();
        }
    }

    /**
     * Starts a new table, creating a new table in the document and initializing it with the appropriate style
     * information.
     *
     * @param tbl The table.
     */
    public void startTable(org.apache.fop.fo.flow.table.Table tbl) {
        if (!stateManager.isProcessingListLabel()) {

            Table table = stateManager.getCurrentState().newTable();
            table.setCellStyleInheritance(false);
            table.setUseRepeat(false);

            TableTableElement element = table.getOdfElement();
            StyleStyleElement styleElement = element.getOrCreateUnqiueAutomaticStyle();
            StyleTablePropertiesElement tablePropertiesElement = styleElement.newStyleTablePropertiesElement();

            CommonBorderPaddingBackground commonBorderPaddingBackground = tbl.getCommonBorderPaddingBackground();
            if (commonBorderPaddingBackground.hasBackground()) {
                tablePropertiesElement.setFoBackgroundColorAttribute(ODTColorUtilities.calculateColorAsHex(commonBorderPaddingBackground.getBackgroundColor()));
            }
            tablePropertiesElement.setTableBorderModelAttribute(ODTBorderUtilities.calculateBorderCollapse(tbl.getBorderCollapse()).toString());
            stateManager.pushTableState(table);            
        }
    }

    /**
     * Ends the current table, applying table border styles in an approximate fashion to the table cells in the ODT
     * content based on the FO styles for the table.
     *
     * @param tbl The table.
     */
    public void endTable(org.apache.fop.fo.flow.table.Table tbl) {
        if (!stateManager.isProcessingListLabel()) {

            // We have to remove the state here so that the dispose logic runs and removes the last row in the table
            // This is yet another workaround for the cell merge issues, and it tends to permeate a lot of table code...
            Table table = stateManager.getCurrentState().getTable();
            stateManager.popState();

            // Fake table borders by setting the borders on all the edge cells
            CommonBorderPaddingBackground common = tbl.getCommonBorderPaddingBackground();
            if (common.hasBorder()) {
                for (Cell cell : ODTTableUtilities.getCellsFromRow(ODTTableUtilities.getTopRow(table))) {
                    cell.getStyleHandler().getTableCellPropertiesForWrite().setTopBorder(ODTBorderUtilities.calculateBorder(common, CommonBorderPaddingBackground.BEFORE));
                }
                for (Cell cell : ODTTableUtilities.getCellsFromRow(ODTTableUtilities.getBottomRow(table))) {
                    cell.getStyleHandler().getTableCellPropertiesForWrite().setBottomBorder(ODTBorderUtilities.calculateBorder(common, CommonBorderPaddingBackground.AFTER));
                }
                for (Cell cell : ODTTableUtilities.getCellsFromColumn(ODTTableUtilities.getLeftColumn(table))) {
                    cell.getStyleHandler().getTableCellPropertiesForWrite().setLeftBorder(ODTBorderUtilities.calculateBorder(common, CommonBorderPaddingBackground.START));
                }
                for (Cell cell : ODTTableUtilities.getCellsFromColumn(ODTTableUtilities.getRightColumn(table))) {
                    cell.getStyleHandler().getTableCellPropertiesForWrite().setRightBorder(ODTBorderUtilities.calculateBorder(common, CommonBorderPaddingBackground.END));
                }
            }
        }
    }

    /**
     * Starts a new table column.
     * <p/>
     * TODO:  Add support for table column styles/sizes.
     *
     * @param tc The table column.
     */
    public void startColumn(org.apache.fop.fo.flow.table.TableColumn tc) {
        if (!stateManager.isProcessingListLabel()) {
            Column column = stateManager.getCurrentState().newTableColumn();
            // column.setWidth(ODTMeasurementUtilities.convertMillipointsToPoints(tc.getColumnWidth()));
            stateManager.pushTableColumnState(column);            
        }
    }

    /**
     * Ends the current table column.
     *
     * @param tc The table column.
     */
    public void endColumn(org.apache.fop.fo.flow.table.TableColumn tc) {
        if (!stateManager.isProcessingListLabel()) {
            stateManager.popState();
        }
    }

    /**
     * Logs an error indicating that table headers are not supported by the ODT backend.
     *
     * @param header The header to process.
     */
    public void startHeader(org.apache.fop.fo.flow.table.TableHeader header) {
        if (log.isErrorEnabled()) {
            log.error("ODTHandler does not support table headers, output may vary for: " + header + ".");
        }
    }

    /**
     * Logs an error indicating that table footers are not supported by the ODT backend.
     *
     * @param footer The footer to process.
     */
    public void startFooter(org.apache.fop.fo.flow.table.TableFooter footer) {
        if (log.isErrorEnabled()) {
            log.error("ODTHandler does not support table footer, output may vary for: " + footer + ".");
        }
    }

    /**
     * Starts a new table row.
     *
     * @param tr The table row.
     */
    public void startRow(org.apache.fop.fo.flow.table.TableRow tr) {
        if (!stateManager.isProcessingListLabel()) {
            stateManager.pushTableRowState(stateManager.getCurrentState().newTableRow());
        }
    }

    /**
     * Ends the current table row.
     *
     * @param tr The table row.
     */
    public void endRow(org.apache.fop.fo.flow.table.TableRow tr) {
        if (!stateManager.isProcessingListLabel()) {
            stateManager.popState();
        }
    }

    /**
     * Starts a new cell in the current table row, setting cell styles as appropriate for ODT output (mostly the border
     * and background style information at the cell level).
     *
     * @param tc The table cell.
     */
    public void startCell(org.apache.fop.fo.flow.table.TableCell tc) {
        if (!stateManager.isProcessingListLabel()) {

            Cell cell = stateManager.getCurrentState().getTableCell(tc.getColumnNumber() - 1);
            CommonBorderPaddingBackground commonBorderPaddingBackground = tc.getCommonBorderPaddingBackground();
            TableCellProperties properties = cell.getStyleHandler().getTableCellPropertiesForWrite();

            if (tc.getNumberColumnsSpanned() > 1 || tc.getNumberRowsSpanned() > 1) {

                Table table = cell.getTable();

                // Calculate the merge region
                int startColumn = cell.getTableColumn().getColumnIndex();
                int startRow = cell.getTableRow().getRowIndex();
                int endColumn = startColumn + tc.getNumberColumnsSpanned() - 1;
                int endRow = startRow + tc.getNumberRowsSpanned() - 1;

                // Make sure we have the necessary rows by getting them (this is a hack...)
                // We always need to have one trailing row in reserve or the merge sometimes breaks things
                for (int index = table.getRowCount(); index <= endRow + 1; index++) {
                    table.getRowByIndex(index);
                }

                // Finally, do the merge
                CellRange range = table.getCellRangeByPosition(startColumn, startRow, endColumn, endRow);
                range.merge();
            }

            properties.setBorder(Border.NONE);
            if (commonBorderPaddingBackground.hasBorder()) {
                properties.setTopBorder(ODTBorderUtilities.calculateBorder(commonBorderPaddingBackground, CommonBorderPaddingBackground.BEFORE));
                properties.setBottomBorder(ODTBorderUtilities.calculateBorder(commonBorderPaddingBackground, CommonBorderPaddingBackground.AFTER));
                properties.setLeftBorder(ODTBorderUtilities.calculateBorder(commonBorderPaddingBackground, CommonBorderPaddingBackground.START));
                properties.setRightBorder(ODTBorderUtilities.calculateBorder(commonBorderPaddingBackground, CommonBorderPaddingBackground.END));
            } else {
                properties.setTopBorder(Border.NONE);
                properties.setBottomBorder(Border.NONE);
                properties.setLeftBorder(Border.NONE);
                properties.setRightBorder(Border.NONE);
            }

            if (commonBorderPaddingBackground.hasBackground()) {
                properties.setBackgroundColor(ODTColorUtilities.calculateColor(commonBorderPaddingBackground.getBackgroundColor()));
            }

            stateManager.pushTableCellState(cell);
        }
    }

    /**
     * Ends the current table cell.
     *
     * @param tc The table cell.
     */
    public void endCell(org.apache.fop.fo.flow.table.TableCell tc) {
        if (!stateManager.isProcessingListLabel()) {
            stateManager.popState();
        }
    }

    /**
     * Starts a new list.
     *
     * @param lb The list block.
     */
    public void startList(org.apache.fop.fo.flow.ListBlock lb) {
        if (!stateManager.isProcessingListLabel()) {
            stateManager.pushListState(stateManager.getCurrentState().newList());
        }
    }

    /**
     * Ends the current list.
     *
     * @param lb The list block.
     */
    public void endList(org.apache.fop.fo.flow.ListBlock lb) {
        if (!stateManager.isProcessingListLabel()) {
            stateManager.popState();
        }
    }

    /**
     * Starts a new list item in the current list.
     *
     * @param li The list item.
     */
    public void startListItem(org.apache.fop.fo.flow.ListItem li) {
        if (!stateManager.isProcessingListLabel()) {
            stateManager.pushListItemState(stateManager.getCurrentState().newListItem());
        }
    }

    /**
     * Ends the current list item.
     *
     * @param li The list item.
     */
    public void endListItem(org.apache.fop.fo.flow.ListItem li) {
        if (!stateManager.isProcessingListLabel()) {
            stateManager.popState();
        }
    }

    /**
     * Starts a new list label.  Note that support for custom list labels is not yet implemented for ODT.  Currently
     * all lists will just contain bullet points as the header (i.e. all lists are bulleted lists).  Output will be
     * suppressed for all child elements when a list label is being processed.
     * <p/>
     * TODO: Investigate options for custom list label support when using ODT as an output format.
     *
     * @param listItemLabel The list item label.
     */
    public void startListLabel(org.apache.fop.fo.flow.ListItemLabel listItemLabel) {
        stateManager.pushListLabelState();
    }

    /**
     * Ends the current list label.
     *
     * @param listItemLabel The list item label.
     */
    public void endListLabel(org.apache.fop.fo.flow.ListItemLabel listItemLabel) {
        stateManager.popState();
    }

    /**
     * Starts a new static content section. In practice this is assumed to be either the header (xsl-region-before) or
     * footer (xsl-region-after), which creates the appropriate state in the chain for that ODT document section. If
     * the flow name does not match either, then the content is skipped and an error message is logged.
     *
     * @param staticContent The static content.
     */
    public void startStatic(org.apache.fop.fo.pagination.StaticContent staticContent) {
        final String flowName = staticContent.getFlowName();
        if (XSL_REGION_BEFORE.equals(flowName)) {
            stateManager.pushHeaderState(textDocument.getHeader());
        } else if (XSL_REGION_AFTER.equals(flowName)) {
            stateManager.pushFooterState(textDocument.getFooter());
        } else {
            log.error("Unsupported static content region, skipping: " + flowName + ".");
        }
    }

    /**
     * Ends the current static content section. If the static content section is not xsl-region-before or
     * xsl-region-after, then no action is taken because all other static content is currently being skipped.
     *
     * @param staticContent The static content.
     */
    public void endStatic(org.apache.fop.fo.pagination.StaticContent staticContent) {
        final String flowName = staticContent.getFlowName();
        if (XSL_REGION_BEFORE.equals(flowName) || XSL_REGION_AFTER.equals(flowName)) {
            stateManager.popState();
        }
    }

    public void startMarkup() { /* compiled code */ }

    public void endMarkup() { /* compiled code */ }

    /**
     * Logs an error indicating that links are not currently supported by the ODT backend.
     *
     * @param basicLink The link.
     */
    public void startLink(org.apache.fop.fo.flow.BasicLink basicLink) {
        if (log.isErrorEnabled()) {
            log.error("ODTHandler does not support links, skipping: " + basicLink + ".");
        }
    }

    /**
     * Logs an error indicating that images are not currently supported by the ODT backend.
     *
     * @param eg The external graphic to process.
     */
    public void image(org.apache.fop.fo.flow.ExternalGraphic eg) {
        if (log.isErrorEnabled()) {
            log.error("ODTHandler does not support images, skipping: " + eg + ".");
        }
    }

    public void pageRef() { /* compiled code */ }

    /**
     * Logs an error indicating that instream foreign objects are not currently supported by the ODT backend.
     *
     * @param ifo The instream foreign object to process.
     */
    @Override
    public void startInstreamForeignObject(org.apache.fop.fo.flow.InstreamForeignObject ifo) {
        if (log.isErrorEnabled()) {
            log.error("ODTHandler does not support InstreamForeignObjects, skipping: " + ifo + ".");
        }
    }

    public void startFootnote(org.apache.fop.fo.flow.Footnote footnote) { /* compiled code */ }

    public void endFootnote(org.apache.fop.fo.flow.Footnote footnote) { /* compiled code */ }

    public void startFootnoteBody(org.apache.fop.fo.flow.FootnoteBody body) { /* compiled code */ }

    public void endFootnoteBody(org.apache.fop.fo.flow.FootnoteBody body) { /* compiled code */ }

    /**
     * Creates a new leader by using a tab stop.  This only has very minimal support for leaders at present, and the
     * actual output will be incorrect except for the most simple of use cases (left and right justification on the
     * same line).
     * <p/>
     * TODO: Add support for leaders of different lengths/spacings (probably not using tab stops if possible?).
     *
     * @param l The FOP leader.
     */
    public void startLeader(org.apache.fop.fo.flow.Leader l) {
        Paragraph paragraph = stateManager.getCurrentState().getParagraph();
        TextParagraphElementBase base = paragraph.getOdfElement();
        StyleStyleElement styleElement = base.getOrCreateUnqiueAutomaticStyle();
        StyleParagraphPropertiesElement paragraphPropertiesElement = styleElement.newStyleParagraphPropertiesElement();

        StyleTabStopsElement tabStopsElement = paragraphPropertiesElement.newStyleTabStopsElement();
        tabStopsElement.newStyleTabStopElement("0.0in", "left");
        tabStopsElement.newStyleTabStopElement("7.0in", "right");

        TextPElement pElement = (TextPElement) base;
        pElement.newTextTabElement();
    }

    public void startWrapper(org.apache.fop.fo.flow.Wrapper wrapper) { /* compiled code */ }

    public void endWrapper(org.apache.fop.fo.flow.Wrapper wrapper) { /* compiled code */ }

    /**
     * Writes a character to the output document.
     *
     * @param c The character.
     */
    public void character(org.apache.fop.fo.flow.Character c) {
        if (!stateManager.isProcessingListLabel()) {
            Paragraph paragraph = stateManager.getCurrentState().getParagraph();
            OdfTextParagraph odfParagraph = (OdfTextParagraph) paragraph.getOdfElement();
            TextSpanElement element = odfParagraph.newTextSpanElement();

            Span span = Span.getInstanceof(element);
            span.appendTextContent(Character.toString(c.getCharacter()));

            DefaultStyleHandler styleHandler = span.getStyleHandler();
            TextProperties textProperties = styleHandler.getTextPropertiesForWrite();
            textProperties.setFont(ODTFontUtilities.calculateFont(c.getCommonFont(), c.getColor()));
            textProperties.setTextLineStyle(ODTFontUtilities.calculateTextLineStyle(c.getTextDecoration()));
        }
    }

    /**
     * Writes a string of text to the output document.
     *
     * @param foText The text.
     */
    public void characters(org.apache.fop.fo.FOText foText) {
        if (!stateManager.isProcessingListLabel()) {

            String content = foText.getCharSequence().toString();

            Paragraph paragraph = stateManager.getCurrentState().getParagraph();
            OdfTextParagraph odfParagraph = (OdfTextParagraph) paragraph.getOdfElement();
            TextSpanElement element = odfParagraph.newTextSpanElement();

            Span span = Span.getInstanceof(element);
            span.appendTextContent(content);

            DefaultStyleHandler styleHandler = span.getStyleHandler();
            TextProperties textProperties = styleHandler.getTextPropertiesForWrite();
            textProperties.setFont(ODTFontUtilities.calculateFont(foText.getCommonFont(), foText.getColor()));
            textProperties.setTextLineStyle(ODTFontUtilities.calculateTextLineStyle(foText.getTextDecoration()));
        }
    }

    /**
     * Logs an error indicating that external documents are not currently supported by the ODT backend.
     *
     * @param document The document to process.
     */
    @Override
    public void startExternalDocument(org.apache.fop.fo.extensions.ExternalDocument document) {
        if (log.isErrorEnabled()) {
            log.error("ODTHandler does not support ExternalDocuments, skipping: " + document + ".");
        }
    }

    /**
     * @see org.apache.fop.fo.FOEventHandler#getResults()
     */
    @Override
    public FormattingResults getResults() {
        return super.getResults();
    }
}

