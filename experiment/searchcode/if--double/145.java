package uk.ac.lkl.common.util.config;




import java.util.*;

/***********************************************************
 * This is automatically generated code.
 *
 * (with: 'java FileXmlConfigurationParser migendefaults.xml MiGen ./src ')
 *
 * DO NOT EDIT THIS CLASS.
 *
 ***********************************************************/





public final class MiGenConfiguration implements Configuration {

  private MiGenConfiguration() {
    factoryReset();
  }

  private static MiGenConfiguration INSTANCE = null;

  private Map<String, Boolean> booleanMap = new HashMap<String,Boolean>();

  private Map<String, Double>  doubleMap  = new HashMap<String,Double>();

  private Map<String, Integer> integerMap = new HashMap<String,Integer>();

  private Map<String, String>  stringMap  = new HashMap<String,String>();

  public static MiGenConfiguration getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new MiGenConfiguration();
      INSTANCE.factoryReset();
    }
    return INSTANCE;
  }


  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAcknowledgeGoalAchievement() {
    return getInstance().isAcknowledgeGoalAchievementFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAcknowledgeGoalAchievement(java.lang.Boolean newAcknowledgeGoalAchievement) {
    getInstance().setAcknowledgeGoalAchievementFromInstance(newAcknowledgeGoalAchievement);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAcknowledgeGoalAchievementFromInstance() {
    checkSync();
    return booleanMap.get("acknowledgeGoalAchievement");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAcknowledgeGoalAchievementFromInstance(java.lang.Boolean newAcknowledgeGoalAchievement) {
    booleanMap.put("acknowledgeGoalAchievement", newAcknowledgeGoalAchievement);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getActivityDescriptionFile() {
    return getInstance().getActivityDescriptionFileFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setActivityDescriptionFile(java.lang.String newActivityDescriptionFile) {
    getInstance().setActivityDescriptionFileFromInstance(newActivityDescriptionFile);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getActivityDescriptionFileFromInstance() {
    checkSync();
    return stringMap.get("activityDescriptionFile");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setActivityDescriptionFileFromInstance(java.lang.String newActivityDescriptionFile) {
    stringMap.put("activityDescriptionFile", newActivityDescriptionFile);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Double getActivityDocumentToRestRatio() {
    return getInstance().getActivityDocumentToRestRatioFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setActivityDocumentToRestRatio(java.lang.Double newActivityDocumentToRestRatio) {
    getInstance().setActivityDocumentToRestRatioFromInstance(newActivityDocumentToRestRatio);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Double getActivityDocumentToRestRatioFromInstance() {
    checkSync();
    return doubleMap.get("activityDocumentToRestRatio");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setActivityDocumentToRestRatioFromInstance(java.lang.Double newActivityDocumentToRestRatio) {
    doubleMap.put("activityDocumentToRestRatio", newActivityDocumentToRestRatio);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddActivityDocumentButtons() {
    return getInstance().isAddActivityDocumentButtonsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddActivityDocumentButtons(java.lang.Boolean newAddActivityDocumentButtons) {
    getInstance().setAddActivityDocumentButtonsFromInstance(newAddActivityDocumentButtons);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddActivityDocumentButtonsFromInstance() {
    checkSync();
    return booleanMap.get("addActivityDocumentButtons");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddActivityDocumentButtonsFromInstance(java.lang.Boolean newAddActivityDocumentButtons) {
    booleanMap.put("addActivityDocumentButtons", newAddActivityDocumentButtons);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddAddRemoveTilesRadioButtons() {
    return getInstance().isAddAddRemoveTilesRadioButtonsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddAddRemoveTilesRadioButtons(java.lang.Boolean newAddAddRemoveTilesRadioButtons) {
    getInstance().setAddAddRemoveTilesRadioButtonsFromInstance(newAddAddRemoveTilesRadioButtons);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddAddRemoveTilesRadioButtonsFromInstance() {
    checkSync();
    return booleanMap.get("addAddRemoveTilesRadioButtons");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddAddRemoveTilesRadioButtonsFromInstance(java.lang.Boolean newAddAddRemoveTilesRadioButtons) {
    booleanMap.put("addAddRemoveTilesRadioButtons", newAddAddRemoveTilesRadioButtons);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddBoxMenuToActivityDocumentCanvasItems() {
    return getInstance().isAddBoxMenuToActivityDocumentCanvasItemsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddBoxMenuToActivityDocumentCanvasItems(java.lang.Boolean newAddBoxMenuToActivityDocumentCanvasItems) {
    getInstance().setAddBoxMenuToActivityDocumentCanvasItemsFromInstance(newAddBoxMenuToActivityDocumentCanvasItems);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddBoxMenuToActivityDocumentCanvasItemsFromInstance() {
    checkSync();
    return booleanMap.get("addBoxMenuToActivityDocumentCanvasItems");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddBoxMenuToActivityDocumentCanvasItemsFromInstance(java.lang.Boolean newAddBoxMenuToActivityDocumentCanvasItems) {
    booleanMap.put("addBoxMenuToActivityDocumentCanvasItems", newAddBoxMenuToActivityDocumentCanvasItems);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddConstantsInGroupColourAllocations() {
    return getInstance().isAddConstantsInGroupColourAllocationsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddConstantsInGroupColourAllocations(java.lang.Boolean newAddConstantsInGroupColourAllocations) {
    getInstance().setAddConstantsInGroupColourAllocationsFromInstance(newAddConstantsInGroupColourAllocations);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddConstantsInGroupColourAllocationsFromInstance() {
    checkSync();
    return booleanMap.get("addConstantsInGroupColourAllocations");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddConstantsInGroupColourAllocationsFromInstance(java.lang.Boolean newAddConstantsInGroupColourAllocations) {
    booleanMap.put("addConstantsInGroupColourAllocations", newAddConstantsInGroupColourAllocations);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddEcollaborator() {
    return getInstance().isAddEcollaboratorFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddEcollaborator(java.lang.Boolean newAddEcollaborator) {
    getInstance().setAddEcollaboratorFromInstance(newAddEcollaborator);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddEcollaboratorFromInstance() {
    checkSync();
    return booleanMap.get("addEcollaborator");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddEcollaboratorFromInstance(java.lang.Boolean newAddEcollaborator) {
    booleanMap.put("addEcollaborator", newAddEcollaborator);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddHelpPanel() {
    return getInstance().isAddHelpPanelFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddHelpPanel(java.lang.Boolean newAddHelpPanel) {
    getInstance().setAddHelpPanelFromInstance(newAddHelpPanel);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddHelpPanelFromInstance() {
    checkSync();
    return booleanMap.get("addHelpPanel");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddHelpPanelFromInstance(java.lang.Boolean newAddHelpPanel) {
    booleanMap.put("addHelpPanel", newAddHelpPanel);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddHelpRequestPanel() {
    return getInstance().isAddHelpRequestPanelFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddHelpRequestPanel(java.lang.Boolean newAddHelpRequestPanel) {
    getInstance().setAddHelpRequestPanelFromInstance(newAddHelpRequestPanel);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddHelpRequestPanelFromInstance() {
    checkSync();
    return booleanMap.get("addHelpRequestPanel");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddHelpRequestPanelFromInstance(java.lang.Boolean newAddHelpRequestPanel) {
    booleanMap.put("addHelpRequestPanel", newAddHelpRequestPanel);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddMenuItemToShowAnythingUsingTiedNumber() {
    return getInstance().isAddMenuItemToShowAnythingUsingTiedNumberFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddMenuItemToShowAnythingUsingTiedNumber(java.lang.Boolean newAddMenuItemToShowAnythingUsingTiedNumber) {
    getInstance().setAddMenuItemToShowAnythingUsingTiedNumberFromInstance(newAddMenuItemToShowAnythingUsingTiedNumber);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddMenuItemToShowAnythingUsingTiedNumberFromInstance() {
    checkSync();
    return booleanMap.get("addMenuItemToShowAnythingUsingTiedNumber");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddMenuItemToShowAnythingUsingTiedNumberFromInstance(java.lang.Boolean newAddMenuItemToShowAnythingUsingTiedNumber) {
    booleanMap.put("addMenuItemToShowAnythingUsingTiedNumber", newAddMenuItemToShowAnythingUsingTiedNumber);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddPropertyListWhenWizardFinished() {
    return getInstance().isAddPropertyListWhenWizardFinishedFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddPropertyListWhenWizardFinished(java.lang.Boolean newAddPropertyListWhenWizardFinished) {
    getInstance().setAddPropertyListWhenWizardFinishedFromInstance(newAddPropertyListWhenWizardFinished);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddPropertyListWhenWizardFinishedFromInstance() {
    checkSync();
    return booleanMap.get("addPropertyListWhenWizardFinished");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddPropertyListWhenWizardFinishedFromInstance(java.lang.Boolean newAddPropertyListWhenWizardFinished) {
    booleanMap.put("addPropertyListWhenWizardFinished", newAddPropertyListWhenWizardFinished);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddRulePanelWhenReplacingWithGeneralWorld() {
    return getInstance().isAddRulePanelWhenReplacingWithGeneralWorldFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddRulePanelWhenReplacingWithGeneralWorld(java.lang.Boolean newAddRulePanelWhenReplacingWithGeneralWorld) {
    getInstance().setAddRulePanelWhenReplacingWithGeneralWorldFromInstance(newAddRulePanelWhenReplacingWithGeneralWorld);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddRulePanelWhenReplacingWithGeneralWorldFromInstance() {
    checkSync();
    return booleanMap.get("addRulePanelWhenReplacingWithGeneralWorld");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddRulePanelWhenReplacingWithGeneralWorldFromInstance(java.lang.Boolean newAddRulePanelWhenReplacingWithGeneralWorld) {
    booleanMap.put("addRulePanelWhenReplacingWithGeneralWorld", newAddRulePanelWhenReplacingWithGeneralWorld);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAddSpaceToExpressions() {
    return getInstance().isAddSpaceToExpressionsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAddSpaceToExpressions(java.lang.Boolean newAddSpaceToExpressions) {
    getInstance().setAddSpaceToExpressionsFromInstance(newAddSpaceToExpressions);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAddSpaceToExpressionsFromInstance() {
    checkSync();
    return booleanMap.get("addSpaceToExpressions");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAddSpaceToExpressionsFromInstance(java.lang.Boolean newAddSpaceToExpressions) {
    booleanMap.put("addSpaceToExpressions", newAddSpaceToExpressions);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAllowEditingOfSlaveUnlockedNumbers() {
    return getInstance().isAllowEditingOfSlaveUnlockedNumbersFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAllowEditingOfSlaveUnlockedNumbers(java.lang.Boolean newAllowEditingOfSlaveUnlockedNumbers) {
    getInstance().setAllowEditingOfSlaveUnlockedNumbersFromInstance(newAllowEditingOfSlaveUnlockedNumbers);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAllowEditingOfSlaveUnlockedNumbersFromInstance() {
    checkSync();
    return booleanMap.get("allowEditingOfSlaveUnlockedNumbers");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAllowEditingOfSlaveUnlockedNumbersFromInstance(java.lang.Boolean newAllowEditingOfSlaveUnlockedNumbers) {
    booleanMap.put("allowEditingOfSlaveUnlockedNumbers", newAllowEditingOfSlaveUnlockedNumbers);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getAnimationDuration() {
    return getInstance().getAnimationDurationFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAnimationDuration(java.lang.Integer newAnimationDuration) {
    getInstance().setAnimationDurationFromInstance(newAnimationDuration);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getAnimationDurationFromInstance() {
    checkSync();
    return integerMap.get("animationDuration");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAnimationDurationFromInstance(java.lang.Integer newAnimationDuration) {
    integerMap.put("animationDuration", newAnimationDuration);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getAutoSaveFileName() {
    return getInstance().getAutoSaveFileNameFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAutoSaveFileName(java.lang.String newAutoSaveFileName) {
    getInstance().setAutoSaveFileNameFromInstance(newAutoSaveFileName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getAutoSaveFileNameFromInstance() {
    checkSync();
    return stringMap.get("autoSaveFileName");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAutoSaveFileNameFromInstance(java.lang.String newAutoSaveFileName) {
    stringMap.put("autoSaveFileName", newAutoSaveFileName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAutoSaveToServer() {
    return getInstance().isAutoSaveToServerFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAutoSaveToServer(java.lang.Boolean newAutoSaveToServer) {
    getInstance().setAutoSaveToServerFromInstance(newAutoSaveToServer);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAutoSaveToServerFromInstance() {
    checkSync();
    return booleanMap.get("autoSaveToServer");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAutoSaveToServerFromInstance(java.lang.Boolean newAutoSaveToServer) {
    booleanMap.put("autoSaveToServer", newAutoSaveToServer);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isAutoSaveWhenIndicatorDetected() {
    return getInstance().isAutoSaveWhenIndicatorDetectedFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAutoSaveWhenIndicatorDetected(java.lang.Boolean newAutoSaveWhenIndicatorDetected) {
    getInstance().setAutoSaveWhenIndicatorDetectedFromInstance(newAutoSaveWhenIndicatorDetected);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isAutoSaveWhenIndicatorDetectedFromInstance() {
    checkSync();
    return booleanMap.get("autoSaveWhenIndicatorDetected");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAutoSaveWhenIndicatorDetectedFromInstance(java.lang.Boolean newAutoSaveWhenIndicatorDetected) {
    booleanMap.put("autoSaveWhenIndicatorDetected", newAutoSaveWhenIndicatorDetected);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getAuxStringFile() {
    return getInstance().getAuxStringFileFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAuxStringFile(java.lang.String newAuxStringFile) {
    getInstance().setAuxStringFileFromInstance(newAuxStringFile);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getAuxStringFileFromInstance() {
    checkSync();
    return stringMap.get("auxStringFile");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAuxStringFileFromInstance(java.lang.String newAuxStringFile) {
    stringMap.put("auxStringFile", newAuxStringFile);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getAvailableColours() {
    return getInstance().getAvailableColoursFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setAvailableColours(java.lang.String newAvailableColours) {
    getInstance().setAvailableColoursFromInstance(newAvailableColours);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getAvailableColoursFromInstance() {
    checkSync();
    return stringMap.get("availableColours");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setAvailableColoursFromInstance(java.lang.String newAvailableColours) {
    stringMap.put("availableColours", newAvailableColours);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanel() {
    return getInstance().isCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanelFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanel(java.lang.Boolean newCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanel) {
    getInstance().setCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanelFromInstance(newCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanel);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanelFromInstance() {
    checkSync();
    return booleanMap.get("centralFeedbackAgentDisplayedBelowTiedNumbersControlPanel");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanelFromInstance(java.lang.Boolean newCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanel) {
    booleanMap.put("centralFeedbackAgentDisplayedBelowTiedNumbersControlPanel", newCentralFeedbackAgentDisplayedBelowTiedNumbersControlPanel);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isCheckToBuildAnother() {
    return getInstance().isCheckToBuildAnotherFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setCheckToBuildAnother(java.lang.Boolean newCheckToBuildAnother) {
    getInstance().setCheckToBuildAnotherFromInstance(newCheckToBuildAnother);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isCheckToBuildAnotherFromInstance() {
    checkSync();
    return booleanMap.get("checkToBuildAnother");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setCheckToBuildAnotherFromInstance(java.lang.Boolean newCheckToBuildAnother) {
    booleanMap.put("checkToBuildAnother", newCheckToBuildAnother);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getColorBad() {
    return getInstance().getColorBadFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setColorBad(java.lang.String newColorBad) {
    getInstance().setColorBadFromInstance(newColorBad);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getColorBadFromInstance() {
    checkSync();
    return stringMap.get("colorBad");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setColorBadFromInstance(java.lang.String newColorBad) {
    stringMap.put("colorBad", newColorBad);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getColorFeedback() {
    return getInstance().getColorFeedbackFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setColorFeedback(java.lang.String newColorFeedback) {
    getInstance().setColorFeedbackFromInstance(newColorFeedback);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getColorFeedbackFromInstance() {
    checkSync();
    return stringMap.get("colorFeedback");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setColorFeedbackFromInstance(java.lang.String newColorFeedback) {
    stringMap.put("colorFeedback", newColorFeedback);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getColorGood() {
    return getInstance().getColorGoodFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setColorGood(java.lang.String newColorGood) {
    getInstance().setColorGoodFromInstance(newColorGood);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getColorGoodFromInstance() {
    checkSync();
    return stringMap.get("colorGood");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setColorGoodFromInstance(java.lang.String newColorGood) {
    stringMap.put("colorGood", newColorGood);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getColorNeutral() {
    return getInstance().getColorNeutralFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setColorNeutral(java.lang.String newColorNeutral) {
    getInstance().setColorNeutralFromInstance(newColorNeutral);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getColorNeutralFromInstance() {
    checkSync();
    return stringMap.get("colorNeutral");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setColorNeutralFromInstance(java.lang.String newColorNeutral) {
    stringMap.put("colorNeutral", newColorNeutral);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getColourAllocationPanelCount() {
    return getInstance().getColourAllocationPanelCountFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setColourAllocationPanelCount(java.lang.Integer newColourAllocationPanelCount) {
    getInstance().setColourAllocationPanelCountFromInstance(newColourAllocationPanelCount);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getColourAllocationPanelCountFromInstance() {
    checkSync();
    return integerMap.get("colourAllocationPanelCount");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setColourAllocationPanelCountFromInstance(java.lang.Integer newColourAllocationPanelCount) {
    integerMap.put("colourAllocationPanelCount", newColourAllocationPanelCount);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getComputerModelExpressionFontStyle() {
    return getInstance().getComputerModelExpressionFontStyleFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setComputerModelExpressionFontStyle(java.lang.String newComputerModelExpressionFontStyle) {
    getInstance().setComputerModelExpressionFontStyleFromInstance(newComputerModelExpressionFontStyle);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getComputerModelExpressionFontStyleFromInstance() {
    checkSync();
    return stringMap.get("computerModelExpressionFontStyle");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setComputerModelExpressionFontStyleFromInstance(java.lang.String newComputerModelExpressionFontStyle) {
    stringMap.put("computerModelExpressionFontStyle", newComputerModelExpressionFontStyle);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getComputerModelRuleFontStyle() {
    return getInstance().getComputerModelRuleFontStyleFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setComputerModelRuleFontStyle(java.lang.String newComputerModelRuleFontStyle) {
    getInstance().setComputerModelRuleFontStyleFromInstance(newComputerModelRuleFontStyle);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getComputerModelRuleFontStyleFromInstance() {
    checkSync();
    return stringMap.get("computerModelRuleFontStyle");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setComputerModelRuleFontStyleFromInstance(java.lang.String newComputerModelRuleFontStyle) {
    stringMap.put("computerModelRuleFontStyle", newComputerModelRuleFontStyle);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isConnectedToServer() {
    return getInstance().isConnectedToServerFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setConnectedToServer(java.lang.Boolean newConnectedToServer) {
    getInstance().setConnectedToServerFromInstance(newConnectedToServer);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isConnectedToServerFromInstance() {
    checkSync();
    return booleanMap.get("connectedToServer");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setConnectedToServerFromInstance(java.lang.Boolean newConnectedToServer) {
    booleanMap.put("connectedToServer", newConnectedToServer);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getCountry() {
    return getInstance().getCountryFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setCountry(java.lang.String newCountry) {
    getInstance().setCountryFromInstance(newCountry);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getCountryFromInstance() {
    checkSync();
    return stringMap.get("country");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setCountryFromInstance(java.lang.String newCountry) {
    stringMap.put("country", newCountry);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getDatabaseDefinition() {
    return getInstance().getDatabaseDefinitionFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDatabaseDefinition(java.lang.String newDatabaseDefinition) {
    getInstance().setDatabaseDefinitionFromInstance(newDatabaseDefinition);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getDatabaseDefinitionFromInstance() {
    checkSync();
    return stringMap.get("databaseDefinition");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDatabaseDefinitionFromInstance(java.lang.String newDatabaseDefinition) {
    stringMap.put("databaseDefinition", newDatabaseDefinition);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getDatabaseName() {
    return getInstance().getDatabaseNameFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDatabaseName(java.lang.String newDatabaseName) {
    getInstance().setDatabaseNameFromInstance(newDatabaseName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getDatabaseNameFromInstance() {
    checkSync();
    return stringMap.get("databaseName");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDatabaseNameFromInstance(java.lang.String newDatabaseName) {
    stringMap.put("databaseName", newDatabaseName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getDefaultDeltaX() {
    return getInstance().getDefaultDeltaXFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDefaultDeltaX(java.lang.Integer newDefaultDeltaX) {
    getInstance().setDefaultDeltaXFromInstance(newDefaultDeltaX);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getDefaultDeltaXFromInstance() {
    checkSync();
    return integerMap.get("defaultDeltaX");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDefaultDeltaXFromInstance(java.lang.Integer newDefaultDeltaX) {
    integerMap.put("defaultDeltaX", newDefaultDeltaX);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getDefaultDeltaY() {
    return getInstance().getDefaultDeltaYFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDefaultDeltaY(java.lang.Integer newDefaultDeltaY) {
    getInstance().setDefaultDeltaYFromInstance(newDefaultDeltaY);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getDefaultDeltaYFromInstance() {
    checkSync();
    return integerMap.get("defaultDeltaY");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDefaultDeltaYFromInstance(java.lang.Integer newDefaultDeltaY) {
    integerMap.put("defaultDeltaY", newDefaultDeltaY);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getDefaultRepeatCount() {
    return getInstance().getDefaultRepeatCountFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDefaultRepeatCount(java.lang.Integer newDefaultRepeatCount) {
    getInstance().setDefaultRepeatCountFromInstance(newDefaultRepeatCount);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getDefaultRepeatCountFromInstance() {
    checkSync();
    return integerMap.get("defaultRepeatCount");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDefaultRepeatCountFromInstance(java.lang.Integer newDefaultRepeatCount) {
    integerMap.put("defaultRepeatCount", newDefaultRepeatCount);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDisableExpressionEditors() {
    return getInstance().isDisableExpressionEditorsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDisableExpressionEditors(java.lang.Boolean newDisableExpressionEditors) {
    getInstance().setDisableExpressionEditorsFromInstance(newDisableExpressionEditors);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDisableExpressionEditorsFromInstance() {
    checkSync();
    return booleanMap.get("disableExpressionEditors");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDisableExpressionEditorsFromInstance(java.lang.Boolean newDisableExpressionEditors) {
    booleanMap.put("disableExpressionEditors", newDisableExpressionEditors);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDistinguishTilesFromPatterns() {
    return getInstance().isDistinguishTilesFromPatternsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDistinguishTilesFromPatterns(java.lang.Boolean newDistinguishTilesFromPatterns) {
    getInstance().setDistinguishTilesFromPatternsFromInstance(newDistinguishTilesFromPatterns);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDistinguishTilesFromPatternsFromInstance() {
    checkSync();
    return booleanMap.get("distinguishTilesFromPatterns");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDistinguishTilesFromPatternsFromInstance(java.lang.Boolean newDistinguishTilesFromPatterns) {
    booleanMap.put("distinguishTilesFromPatterns", newDistinguishTilesFromPatterns);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getDividerLocation() {
    return getInstance().getDividerLocationFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDividerLocation(java.lang.Integer newDividerLocation) {
    getInstance().setDividerLocationFromInstance(newDividerLocation);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getDividerLocationFromInstance() {
    checkSync();
    return integerMap.get("dividerLocation");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDividerLocationFromInstance(java.lang.Integer newDividerLocation) {
    integerMap.put("dividerLocation", newDividerLocation);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDivisionEnabled() {
    return getInstance().isDivisionEnabledFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDivisionEnabled(java.lang.Boolean newDivisionEnabled) {
    getInstance().setDivisionEnabledFromInstance(newDivisionEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDivisionEnabledFromInstance() {
    checkSync();
    return booleanMap.get("divisionEnabled");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDivisionEnabledFromInstance(java.lang.Boolean newDivisionEnabled) {
    booleanMap.put("divisionEnabled", newDivisionEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDoNotAddCutCopyPasteTools() {
    return getInstance().isDoNotAddCutCopyPasteToolsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDoNotAddCutCopyPasteTools(java.lang.Boolean newDoNotAddCutCopyPasteTools) {
    getInstance().setDoNotAddCutCopyPasteToolsFromInstance(newDoNotAddCutCopyPasteTools);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDoNotAddCutCopyPasteToolsFromInstance() {
    checkSync();
    return booleanMap.get("doNotAddCutCopyPasteTools");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDoNotAddCutCopyPasteToolsFromInstance(java.lang.Boolean newDoNotAddCutCopyPasteTools) {
    booleanMap.put("doNotAddCutCopyPasteTools", newDoNotAddCutCopyPasteTools);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDoNotAddDeleteTool() {
    return getInstance().isDoNotAddDeleteToolFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDoNotAddDeleteTool(java.lang.Boolean newDoNotAddDeleteTool) {
    getInstance().setDoNotAddDeleteToolFromInstance(newDoNotAddDeleteTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDoNotAddDeleteToolFromInstance() {
    checkSync();
    return booleanMap.get("doNotAddDeleteTool");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDoNotAddDeleteToolFromInstance(java.lang.Boolean newDoNotAddDeleteTool) {
    booleanMap.put("doNotAddDeleteTool", newDoNotAddDeleteTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDoNotAddNegativeColorTileButtons() {
    return getInstance().isDoNotAddNegativeColorTileButtonsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDoNotAddNegativeColorTileButtons(java.lang.Boolean newDoNotAddNegativeColorTileButtons) {
    getInstance().setDoNotAddNegativeColorTileButtonsFromInstance(newDoNotAddNegativeColorTileButtons);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDoNotAddNegativeColorTileButtonsFromInstance() {
    checkSync();
    return booleanMap.get("doNotAddNegativeColorTileButtons");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDoNotAddNegativeColorTileButtonsFromInstance(java.lang.Boolean newDoNotAddNegativeColorTileButtons) {
    booleanMap.put("doNotAddNegativeColorTileButtons", newDoNotAddNegativeColorTileButtons);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDoNotAddZoomTools() {
    return getInstance().isDoNotAddZoomToolsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDoNotAddZoomTools(java.lang.Boolean newDoNotAddZoomTools) {
    getInstance().setDoNotAddZoomToolsFromInstance(newDoNotAddZoomTools);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDoNotAddZoomToolsFromInstance() {
    checkSync();
    return booleanMap.get("doNotAddZoomTools");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDoNotAddZoomToolsFromInstance(java.lang.Boolean newDoNotAddZoomTools) {
    booleanMap.put("doNotAddZoomTools", newDoNotAddZoomTools);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDocumentCanvasShapesDraggableDefault() {
    return getInstance().isDocumentCanvasShapesDraggableDefaultFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDocumentCanvasShapesDraggableDefault(java.lang.Boolean newDocumentCanvasShapesDraggableDefault) {
    getInstance().setDocumentCanvasShapesDraggableDefaultFromInstance(newDocumentCanvasShapesDraggableDefault);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDocumentCanvasShapesDraggableDefaultFromInstance() {
    checkSync();
    return booleanMap.get("documentCanvasShapesDraggableDefault");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDocumentCanvasShapesDraggableDefaultFromInstance(java.lang.Boolean newDocumentCanvasShapesDraggableDefault) {
    booleanMap.put("documentCanvasShapesDraggableDefault", newDocumentCanvasShapesDraggableDefault);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDrawCrossForAnyOverlap() {
    return getInstance().isDrawCrossForAnyOverlapFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDrawCrossForAnyOverlap(java.lang.Boolean newDrawCrossForAnyOverlap) {
    getInstance().setDrawCrossForAnyOverlapFromInstance(newDrawCrossForAnyOverlap);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDrawCrossForAnyOverlapFromInstance() {
    checkSync();
    return booleanMap.get("drawCrossForAnyOverlap");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDrawCrossForAnyOverlapFromInstance(java.lang.Boolean newDrawCrossForAnyOverlap) {
    booleanMap.put("drawCrossForAnyOverlap", newDrawCrossForAnyOverlap);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isDrawCrossForMultipleColors() {
    return getInstance().isDrawCrossForMultipleColorsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDrawCrossForMultipleColors(java.lang.Boolean newDrawCrossForMultipleColors) {
    getInstance().setDrawCrossForMultipleColorsFromInstance(newDrawCrossForMultipleColors);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isDrawCrossForMultipleColorsFromInstance() {
    checkSync();
    return booleanMap.get("drawCrossForMultipleColors");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDrawCrossForMultipleColorsFromInstance(java.lang.Boolean newDrawCrossForMultipleColors) {
    booleanMap.put("drawCrossForMultipleColors", newDrawCrossForMultipleColors);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getDropFeedbackColor() {
    return getInstance().getDropFeedbackColorFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setDropFeedbackColor(java.lang.String newDropFeedbackColor) {
    getInstance().setDropFeedbackColorFromInstance(newDropFeedbackColor);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getDropFeedbackColorFromInstance() {
    checkSync();
    return stringMap.get("dropFeedbackColor");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setDropFeedbackColorFromInstance(java.lang.String newDropFeedbackColor) {
    stringMap.put("dropFeedbackColor", newDropFeedbackColor);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getEgenInactivityThreshold() {
    return getInstance().getEgenInactivityThresholdFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setEgenInactivityThreshold(java.lang.Integer newEgenInactivityThreshold) {
    getInstance().setEgenInactivityThresholdFromInstance(newEgenInactivityThreshold);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getEgenInactivityThresholdFromInstance() {
    checkSync();
    return integerMap.get("egenInactivityThreshold");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setEgenInactivityThresholdFromInstance(java.lang.Integer newEgenInactivityThreshold) {
    integerMap.put("egenInactivityThreshold", newEgenInactivityThreshold);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isEnableAutoVelcro() {
    return getInstance().isEnableAutoVelcroFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setEnableAutoVelcro(java.lang.Boolean newEnableAutoVelcro) {
    getInstance().setEnableAutoVelcroFromInstance(newEnableAutoVelcro);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isEnableAutoVelcroFromInstance() {
    checkSync();
    return booleanMap.get("enableAutoVelcro");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setEnableAutoVelcroFromInstance(java.lang.Boolean newEnableAutoVelcro) {
    booleanMap.put("enableAutoVelcro", newEnableAutoVelcro);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isEnableReplay() {
    return getInstance().isEnableReplayFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setEnableReplay(java.lang.Boolean newEnableReplay) {
    getInstance().setEnableReplayFromInstance(newEnableReplay);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isEnableReplayFromInstance() {
    checkSync();
    return booleanMap.get("enableReplay");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setEnableReplayFromInstance(java.lang.Boolean newEnableReplay) {
    booleanMap.put("enableReplay", newEnableReplay);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isEnableVelcro() {
    return getInstance().isEnableVelcroFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setEnableVelcro(java.lang.Boolean newEnableVelcro) {
    getInstance().setEnableVelcroFromInstance(newEnableVelcro);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isEnableVelcroFromInstance() {
    checkSync();
    return booleanMap.get("enableVelcro");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setEnableVelcroFromInstance(java.lang.Boolean newEnableVelcro) {
    booleanMap.put("enableVelcro", newEnableVelcro);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isEnabledFrozenTime() {
    return getInstance().isEnabledFrozenTimeFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setEnabledFrozenTime(java.lang.Boolean newEnabledFrozenTime) {
    getInstance().setEnabledFrozenTimeFromInstance(newEnabledFrozenTime);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isEnabledFrozenTimeFromInstance() {
    checkSync();
    return booleanMap.get("enabledFrozenTime");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setEnabledFrozenTimeFromInstance(java.lang.Boolean newEnabledFrozenTime) {
    booleanMap.put("enabledFrozenTime", newEnabledFrozenTime);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isEnabledTaskSelection() {
    return getInstance().isEnabledTaskSelectionFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setEnabledTaskSelection(java.lang.Boolean newEnabledTaskSelection) {
    getInstance().setEnabledTaskSelectionFromInstance(newEnabledTaskSelection);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isEnabledTaskSelectionFromInstance() {
    checkSync();
    return booleanMap.get("enabledTaskSelection");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setEnabledTaskSelectionFromInstance(java.lang.Boolean newEnabledTaskSelection) {
    booleanMap.put("enabledTaskSelection", newEnabledTaskSelection);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getEntitiesDefinition() {
    return getInstance().getEntitiesDefinitionFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setEntitiesDefinition(java.lang.String newEntitiesDefinition) {
    getInstance().setEntitiesDefinitionFromInstance(newEntitiesDefinition);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getEntitiesDefinitionFromInstance() {
    checkSync();
    return stringMap.get("entitiesDefinition");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setEntitiesDefinitionFromInstance(java.lang.String newEntitiesDefinition) {
    stringMap.put("entitiesDefinition", newEntitiesDefinition);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getErrorFileName() {
    return getInstance().getErrorFileNameFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setErrorFileName(java.lang.String newErrorFileName) {
    getInstance().setErrorFileNameFromInstance(newErrorFileName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getErrorFileNameFromInstance() {
    checkSync();
    return stringMap.get("errorFileName");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setErrorFileNameFromInstance(java.lang.String newErrorFileName) {
    stringMap.put("errorFileName", newErrorFileName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isExplicitShareStudentModel() {
    return getInstance().isExplicitShareStudentModelFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setExplicitShareStudentModel(java.lang.Boolean newExplicitShareStudentModel) {
    getInstance().setExplicitShareStudentModelFromInstance(newExplicitShareStudentModel);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isExplicitShareStudentModelFromInstance() {
    checkSync();
    return booleanMap.get("explicitShareStudentModel");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setExplicitShareStudentModelFromInstance(java.lang.Boolean newExplicitShareStudentModel) {
    booleanMap.put("explicitShareStudentModel", newExplicitShareStudentModel);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getGeneralModelCanvasGridSize() {
    return getInstance().getGeneralModelCanvasGridSizeFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setGeneralModelCanvasGridSize(java.lang.Integer newGeneralModelCanvasGridSize) {
    getInstance().setGeneralModelCanvasGridSizeFromInstance(newGeneralModelCanvasGridSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getGeneralModelCanvasGridSizeFromInstance() {
    checkSync();
    return integerMap.get("generalModelCanvasGridSize");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setGeneralModelCanvasGridSizeFromInstance(java.lang.Integer newGeneralModelCanvasGridSize) {
    integerMap.put("generalModelCanvasGridSize", newGeneralModelCanvasGridSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getGeneralModelCanvasHeight() {
    return getInstance().getGeneralModelCanvasHeightFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setGeneralModelCanvasHeight(java.lang.Integer newGeneralModelCanvasHeight) {
    getInstance().setGeneralModelCanvasHeightFromInstance(newGeneralModelCanvasHeight);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getGeneralModelCanvasHeightFromInstance() {
    checkSync();
    return integerMap.get("generalModelCanvasHeight");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setGeneralModelCanvasHeightFromInstance(java.lang.Integer newGeneralModelCanvasHeight) {
    integerMap.put("generalModelCanvasHeight", newGeneralModelCanvasHeight);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getGeneralModelCanvasWidth() {
    return getInstance().getGeneralModelCanvasWidthFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setGeneralModelCanvasWidth(java.lang.Integer newGeneralModelCanvasWidth) {
    getInstance().setGeneralModelCanvasWidthFromInstance(newGeneralModelCanvasWidth);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getGeneralModelCanvasWidthFromInstance() {
    checkSync();
    return integerMap.get("generalModelCanvasWidth");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setGeneralModelCanvasWidthFromInstance(java.lang.Integer newGeneralModelCanvasWidth) {
    integerMap.put("generalModelCanvasWidth", newGeneralModelCanvasWidth);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getGoalTrackingCellHeight() {
    return getInstance().getGoalTrackingCellHeightFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setGoalTrackingCellHeight(java.lang.Integer newGoalTrackingCellHeight) {
    getInstance().setGoalTrackingCellHeightFromInstance(newGoalTrackingCellHeight);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getGoalTrackingCellHeightFromInstance() {
    checkSync();
    return integerMap.get("goalTrackingCellHeight");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setGoalTrackingCellHeightFromInstance(java.lang.Integer newGoalTrackingCellHeight) {
    integerMap.put("goalTrackingCellHeight", newGoalTrackingCellHeight);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isGroupsCreatedWithColourAllocationExpressions() {
    return getInstance().isGroupsCreatedWithColourAllocationExpressionsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setGroupsCreatedWithColourAllocationExpressions(java.lang.Boolean newGroupsCreatedWithColourAllocationExpressions) {
    getInstance().setGroupsCreatedWithColourAllocationExpressionsFromInstance(newGroupsCreatedWithColourAllocationExpressions);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isGroupsCreatedWithColourAllocationExpressionsFromInstance() {
    checkSync();
    return booleanMap.get("groupsCreatedWithColourAllocationExpressions");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setGroupsCreatedWithColourAllocationExpressionsFromInstance(java.lang.Boolean newGroupsCreatedWithColourAllocationExpressions) {
    booleanMap.put("groupsCreatedWithColourAllocationExpressions", newGroupsCreatedWithColourAllocationExpressions);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isHideColourAllocationAttributes() {
    return getInstance().isHideColourAllocationAttributesFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setHideColourAllocationAttributes(java.lang.Boolean newHideColourAllocationAttributes) {
    getInstance().setHideColourAllocationAttributesFromInstance(newHideColourAllocationAttributes);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isHideColourAllocationAttributesFromInstance() {
    checkSync();
    return booleanMap.get("hideColourAllocationAttributes");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setHideColourAllocationAttributesFromInstance(java.lang.Boolean newHideColourAllocationAttributes) {
    booleanMap.put("hideColourAllocationAttributes", newHideColourAllocationAttributes);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isHighlightSuggestion() {
    return getInstance().isHighlightSuggestionFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setHighlightSuggestion(java.lang.Boolean newHighlightSuggestion) {
    getInstance().setHighlightSuggestionFromInstance(newHighlightSuggestion);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isHighlightSuggestionFromInstance() {
    checkSync();
    return booleanMap.get("highlightSuggestion");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setHighlightSuggestionFromInstance(java.lang.Boolean newHighlightSuggestion) {
    booleanMap.put("highlightSuggestion", newHighlightSuggestion);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isIgnoringSavedDoneButtonEnabledFlag() {
    return getInstance().isIgnoringSavedDoneButtonEnabledFlagFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setIgnoringSavedDoneButtonEnabledFlag(java.lang.Boolean newIgnoringSavedDoneButtonEnabledFlag) {
    getInstance().setIgnoringSavedDoneButtonEnabledFlagFromInstance(newIgnoringSavedDoneButtonEnabledFlag);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isIgnoringSavedDoneButtonEnabledFlagFromInstance() {
    checkSync();
    return booleanMap.get("ignoringSavedDoneButtonEnabledFlag");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setIgnoringSavedDoneButtonEnabledFlagFromInstance(java.lang.Boolean newIgnoringSavedDoneButtonEnabledFlag) {
    booleanMap.put("ignoringSavedDoneButtonEnabledFlag", newIgnoringSavedDoneButtonEnabledFlag);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isIncludeConstructionPartsInCollaboration() {
    return getInstance().isIncludeConstructionPartsInCollaborationFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setIncludeConstructionPartsInCollaboration(java.lang.Boolean newIncludeConstructionPartsInCollaboration) {
    getInstance().setIncludeConstructionPartsInCollaborationFromInstance(newIncludeConstructionPartsInCollaboration);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isIncludeConstructionPartsInCollaborationFromInstance() {
    checkSync();
    return booleanMap.get("includeConstructionPartsInCollaboration");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setIncludeConstructionPartsInCollaborationFromInstance(java.lang.Boolean newIncludeConstructionPartsInCollaboration) {
    booleanMap.put("includeConstructionPartsInCollaboration", newIncludeConstructionPartsInCollaboration);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getInitialModelFileName() {
    return getInstance().getInitialModelFileNameFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setInitialModelFileName(java.lang.String newInitialModelFileName) {
    getInstance().setInitialModelFileNameFromInstance(newInitialModelFileName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getInitialModelFileNameFromInstance() {
    checkSync();
    return stringMap.get("initialModelFileName");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setInitialModelFileNameFromInstance(java.lang.String newInitialModelFileName) {
    stringMap.put("initialModelFileName", newInitialModelFileName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getKaleidoscopeCommonFormatLogFileName() {
    return getInstance().getKaleidoscopeCommonFormatLogFileNameFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setKaleidoscopeCommonFormatLogFileName(java.lang.String newKaleidoscopeCommonFormatLogFileName) {
    getInstance().setKaleidoscopeCommonFormatLogFileNameFromInstance(newKaleidoscopeCommonFormatLogFileName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getKaleidoscopeCommonFormatLogFileNameFromInstance() {
    checkSync();
    return stringMap.get("kaleidoscopeCommonFormatLogFileName");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setKaleidoscopeCommonFormatLogFileNameFromInstance(java.lang.String newKaleidoscopeCommonFormatLogFileName) {
    stringMap.put("kaleidoscopeCommonFormatLogFileName", newKaleidoscopeCommonFormatLogFileName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getLanguage() {
    return getInstance().getLanguageFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setLanguage(java.lang.String newLanguage) {
    getInstance().setLanguageFromInstance(newLanguage);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getLanguageFromInstance() {
    checkSync();
    return stringMap.get("language");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setLanguageFromInstance(java.lang.String newLanguage) {
    stringMap.put("language", newLanguage);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getLessonDurationInMinutes() {
    return getInstance().getLessonDurationInMinutesFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setLessonDurationInMinutes(java.lang.Integer newLessonDurationInMinutes) {
    getInstance().setLessonDurationInMinutesFromInstance(newLessonDurationInMinutes);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getLessonDurationInMinutesFromInstance() {
    checkSync();
    return integerMap.get("lessonDurationInMinutes");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setLessonDurationInMinutesFromInstance(java.lang.Integer newLessonDurationInMinutes) {
    integerMap.put("lessonDurationInMinutes", newLessonDurationInMinutes);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isLockedTiedNumbersCanHaveNames() {
    return getInstance().isLockedTiedNumbersCanHaveNamesFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setLockedTiedNumbersCanHaveNames(java.lang.Boolean newLockedTiedNumbersCanHaveNames) {
    getInstance().setLockedTiedNumbersCanHaveNamesFromInstance(newLockedTiedNumbersCanHaveNames);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isLockedTiedNumbersCanHaveNamesFromInstance() {
    checkSync();
    return booleanMap.get("lockedTiedNumbersCanHaveNames");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setLockedTiedNumbersCanHaveNamesFromInstance(java.lang.Boolean newLockedTiedNumbersCanHaveNames) {
    booleanMap.put("lockedTiedNumbersCanHaveNames", newLockedTiedNumbersCanHaveNames);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getMainStringFile() {
    return getInstance().getMainStringFileFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setMainStringFile(java.lang.String newMainStringFile) {
    getInstance().setMainStringFileFromInstance(newMainStringFile);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getMainStringFileFromInstance() {
    checkSync();
    return stringMap.get("mainStringFile");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setMainStringFileFromInstance(java.lang.String newMainStringFile) {
    stringMap.put("mainStringFile", newMainStringFile);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Double getMasterSlaveGridRatio() {
    return getInstance().getMasterSlaveGridRatioFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setMasterSlaveGridRatio(java.lang.Double newMasterSlaveGridRatio) {
    getInstance().setMasterSlaveGridRatioFromInstance(newMasterSlaveGridRatio);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Double getMasterSlaveGridRatioFromInstance() {
    checkSync();
    return doubleMap.get("masterSlaveGridRatio");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setMasterSlaveGridRatioFromInstance(java.lang.Double newMasterSlaveGridRatio) {
    doubleMap.put("masterSlaveGridRatio", newMasterSlaveGridRatio);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isMinimiseCollaborationConstructionParts() {
    return getInstance().isMinimiseCollaborationConstructionPartsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setMinimiseCollaborationConstructionParts(java.lang.Boolean newMinimiseCollaborationConstructionParts) {
    getInstance().setMinimiseCollaborationConstructionPartsFromInstance(newMinimiseCollaborationConstructionParts);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isMinimiseCollaborationConstructionPartsFromInstance() {
    checkSync();
    return booleanMap.get("minimiseCollaborationConstructionParts");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setMinimiseCollaborationConstructionPartsFromInstance(java.lang.Boolean newMinimiseCollaborationConstructionParts) {
    booleanMap.put("minimiseCollaborationConstructionParts", newMinimiseCollaborationConstructionParts);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isModelRuleAllColors() {
    return getInstance().isModelRuleAllColorsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setModelRuleAllColors(java.lang.Boolean newModelRuleAllColors) {
    getInstance().setModelRuleAllColorsFromInstance(newModelRuleAllColors);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isModelRuleAllColorsFromInstance() {
    checkSync();
    return booleanMap.get("modelRuleAllColors");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setModelRuleAllColorsFromInstance(java.lang.Boolean newModelRuleAllColors) {
    booleanMap.put("modelRuleAllColors", newModelRuleAllColors);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isModelRulesButtonEnabled() {
    return getInstance().isModelRulesButtonEnabledFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setModelRulesButtonEnabled(java.lang.Boolean newModelRulesButtonEnabled) {
    getInstance().setModelRulesButtonEnabledFromInstance(newModelRulesButtonEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isModelRulesButtonEnabledFromInstance() {
    checkSync();
    return booleanMap.get("modelRulesButtonEnabled");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setModelRulesButtonEnabledFromInstance(java.lang.Boolean newModelRulesButtonEnabled) {
    booleanMap.put("modelRulesButtonEnabled", newModelRulesButtonEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isMultiColorBuildingBlocksAllowed() {
    return getInstance().isMultiColorBuildingBlocksAllowedFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setMultiColorBuildingBlocksAllowed(java.lang.Boolean newMultiColorBuildingBlocksAllowed) {
    getInstance().setMultiColorBuildingBlocksAllowedFromInstance(newMultiColorBuildingBlocksAllowed);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isMultiColorBuildingBlocksAllowedFromInstance() {
    checkSync();
    return booleanMap.get("multiColorBuildingBlocksAllowed");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setMultiColorBuildingBlocksAllowedFromInstance(java.lang.Boolean newMultiColorBuildingBlocksAllowed) {
    booleanMap.put("multiColorBuildingBlocksAllowed", newMultiColorBuildingBlocksAllowed);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isMultiplyDeltaXByWidth() {
    return getInstance().isMultiplyDeltaXByWidthFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setMultiplyDeltaXByWidth(java.lang.Boolean newMultiplyDeltaXByWidth) {
    getInstance().setMultiplyDeltaXByWidthFromInstance(newMultiplyDeltaXByWidth);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isMultiplyDeltaXByWidthFromInstance() {
    checkSync();
    return booleanMap.get("multiplyDeltaXByWidth");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setMultiplyDeltaXByWidthFromInstance(java.lang.Boolean newMultiplyDeltaXByWidth) {
    booleanMap.put("multiplyDeltaXByWidth", newMultiplyDeltaXByWidth);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isMultiplyDeltaYByHeight() {
    return getInstance().isMultiplyDeltaYByHeightFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setMultiplyDeltaYByHeight(java.lang.Boolean newMultiplyDeltaYByHeight) {
    getInstance().setMultiplyDeltaYByHeightFromInstance(newMultiplyDeltaYByHeight);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isMultiplyDeltaYByHeightFromInstance() {
    checkSync();
    return booleanMap.get("multiplyDeltaYByHeight");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setMultiplyDeltaYByHeightFromInstance(java.lang.Boolean newMultiplyDeltaYByHeight) {
    booleanMap.put("multiplyDeltaYByHeight", newMultiplyDeltaYByHeight);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isNamedVariablesAreGeneral() {
    return getInstance().isNamedVariablesAreGeneralFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNamedVariablesAreGeneral(java.lang.Boolean newNamedVariablesAreGeneral) {
    getInstance().setNamedVariablesAreGeneralFromInstance(newNamedVariablesAreGeneral);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isNamedVariablesAreGeneralFromInstance() {
    checkSync();
    return booleanMap.get("namedVariablesAreGeneral");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNamedVariablesAreGeneralFromInstance(java.lang.Boolean newNamedVariablesAreGeneral) {
    booleanMap.put("namedVariablesAreGeneral", newNamedVariablesAreGeneral);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isNamingUnlocksTiedNumbers() {
    return getInstance().isNamingUnlocksTiedNumbersFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNamingUnlocksTiedNumbers(java.lang.Boolean newNamingUnlocksTiedNumbers) {
    getInstance().setNamingUnlocksTiedNumbersFromInstance(newNamingUnlocksTiedNumbers);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isNamingUnlocksTiedNumbersFromInstance() {
    checkSync();
    return booleanMap.get("namingUnlocksTiedNumbers");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNamingUnlocksTiedNumbersFromInstance(java.lang.Boolean newNamingUnlocksTiedNumbers) {
    booleanMap.put("namingUnlocksTiedNumbers", newNamingUnlocksTiedNumbers);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isNoColourAllocation() {
    return getInstance().isNoColourAllocationFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNoColourAllocation(java.lang.Boolean newNoColourAllocation) {
    getInstance().setNoColourAllocationFromInstance(newNoColourAllocation);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isNoColourAllocationFromInstance() {
    checkSync();
    return booleanMap.get("noColourAllocation");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNoColourAllocationFromInstance(java.lang.Boolean newNoColourAllocation) {
    booleanMap.put("noColourAllocation", newNoColourAllocation);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isNonGeneralVariablesCanBeTaskVariables() {
    return getInstance().isNonGeneralVariablesCanBeTaskVariablesFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNonGeneralVariablesCanBeTaskVariables(java.lang.Boolean newNonGeneralVariablesCanBeTaskVariables) {
    getInstance().setNonGeneralVariablesCanBeTaskVariablesFromInstance(newNonGeneralVariablesCanBeTaskVariables);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isNonGeneralVariablesCanBeTaskVariablesFromInstance() {
    checkSync();
    return booleanMap.get("nonGeneralVariablesCanBeTaskVariables");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNonGeneralVariablesCanBeTaskVariablesFromInstance(java.lang.Boolean newNonGeneralVariablesCanBeTaskVariables) {
    booleanMap.put("nonGeneralVariablesCanBeTaskVariables", newNonGeneralVariablesCanBeTaskVariables);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isNumberNameFontBold() {
    return getInstance().isNumberNameFontBoldFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNumberNameFontBold(java.lang.Boolean newNumberNameFontBold) {
    getInstance().setNumberNameFontBoldFromInstance(newNumberNameFontBold);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isNumberNameFontBoldFromInstance() {
    checkSync();
    return booleanMap.get("numberNameFontBold");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNumberNameFontBoldFromInstance(java.lang.Boolean newNumberNameFontBold) {
    booleanMap.put("numberNameFontBold", newNumberNameFontBold);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getNumberNameFontFamily() {
    return getInstance().getNumberNameFontFamilyFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNumberNameFontFamily(java.lang.String newNumberNameFontFamily) {
    getInstance().setNumberNameFontFamilyFromInstance(newNumberNameFontFamily);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getNumberNameFontFamilyFromInstance() {
    checkSync();
    return stringMap.get("numberNameFontFamily");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNumberNameFontFamilyFromInstance(java.lang.String newNumberNameFontFamily) {
    stringMap.put("numberNameFontFamily", newNumberNameFontFamily);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getNumberNameFontSize() {
    return getInstance().getNumberNameFontSizeFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNumberNameFontSize(java.lang.Integer newNumberNameFontSize) {
    getInstance().setNumberNameFontSizeFromInstance(newNumberNameFontSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getNumberNameFontSizeFromInstance() {
    checkSync();
    return integerMap.get("numberNameFontSize");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNumberNameFontSizeFromInstance(java.lang.Integer newNumberNameFontSize) {
    integerMap.put("numberNameFontSize", newNumberNameFontSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isNumberValueFontBold() {
    return getInstance().isNumberValueFontBoldFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNumberValueFontBold(java.lang.Boolean newNumberValueFontBold) {
    getInstance().setNumberValueFontBoldFromInstance(newNumberValueFontBold);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isNumberValueFontBoldFromInstance() {
    checkSync();
    return booleanMap.get("numberValueFontBold");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNumberValueFontBoldFromInstance(java.lang.Boolean newNumberValueFontBold) {
    booleanMap.put("numberValueFontBold", newNumberValueFontBold);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getNumberValueFontFamily() {
    return getInstance().getNumberValueFontFamilyFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNumberValueFontFamily(java.lang.String newNumberValueFontFamily) {
    getInstance().setNumberValueFontFamilyFromInstance(newNumberValueFontFamily);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getNumberValueFontFamilyFromInstance() {
    checkSync();
    return stringMap.get("numberValueFontFamily");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNumberValueFontFamilyFromInstance(java.lang.String newNumberValueFontFamily) {
    stringMap.put("numberValueFontFamily", newNumberValueFontFamily);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getNumberValueFontSize() {
    return getInstance().getNumberValueFontSizeFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setNumberValueFontSize(java.lang.Integer newNumberValueFontSize) {
    getInstance().setNumberValueFontSizeFromInstance(newNumberValueFontSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getNumberValueFontSizeFromInstance() {
    checkSync();
    return integerMap.get("numberValueFontSize");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setNumberValueFontSizeFromInstance(java.lang.Integer newNumberValueFontSize) {
    integerMap.put("numberValueFontSize", newNumberValueFontSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getOpacity() {
    return getInstance().getOpacityFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setOpacity(java.lang.Integer newOpacity) {
    getInstance().setOpacityFromInstance(newOpacity);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getOpacityFromInstance() {
    checkSync();
    return integerMap.get("opacity");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setOpacityFromInstance(java.lang.Integer newOpacity) {
    integerMap.put("opacity", newOpacity);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isPackedSize() {
    return getInstance().isPackedSizeFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setPackedSize(java.lang.Boolean newPackedSize) {
    getInstance().setPackedSizeFromInstance(newPackedSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isPackedSizeFromInstance() {
    checkSync();
    return booleanMap.get("packedSize");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setPackedSizeFromInstance(java.lang.Boolean newPackedSize) {
    booleanMap.put("packedSize", newPackedSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isPatternsCreatedWithColourAllocationExpressions() {
    return getInstance().isPatternsCreatedWithColourAllocationExpressionsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setPatternsCreatedWithColourAllocationExpressions(java.lang.Boolean newPatternsCreatedWithColourAllocationExpressions) {
    getInstance().setPatternsCreatedWithColourAllocationExpressionsFromInstance(newPatternsCreatedWithColourAllocationExpressions);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isPatternsCreatedWithColourAllocationExpressionsFromInstance() {
    checkSync();
    return booleanMap.get("patternsCreatedWithColourAllocationExpressions");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setPatternsCreatedWithColourAllocationExpressionsFromInstance(java.lang.Boolean newPatternsCreatedWithColourAllocationExpressions) {
    booleanMap.put("patternsCreatedWithColourAllocationExpressions", newPatternsCreatedWithColourAllocationExpressions);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isPatternsDrawnOverGrid() {
    return getInstance().isPatternsDrawnOverGridFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setPatternsDrawnOverGrid(java.lang.Boolean newPatternsDrawnOverGrid) {
    getInstance().setPatternsDrawnOverGridFromInstance(newPatternsDrawnOverGrid);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isPatternsDrawnOverGridFromInstance() {
    checkSync();
    return booleanMap.get("patternsDrawnOverGrid");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setPatternsDrawnOverGridFromInstance(java.lang.Boolean newPatternsDrawnOverGrid) {
    booleanMap.put("patternsDrawnOverGrid", newPatternsDrawnOverGrid);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getPixelsToStartDrag() {
    return getInstance().getPixelsToStartDragFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setPixelsToStartDrag(java.lang.Integer newPixelsToStartDrag) {
    getInstance().setPixelsToStartDragFromInstance(newPixelsToStartDrag);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getPixelsToStartDragFromInstance() {
    checkSync();
    return integerMap.get("pixelsToStartDrag");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setPixelsToStartDragFromInstance(java.lang.Integer newPixelsToStartDrag) {
    integerMap.put("pixelsToStartDrag", newPixelsToStartDrag);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isPlayButtonForPatternAnimationOnly() {
    return getInstance().isPlayButtonForPatternAnimationOnlyFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setPlayButtonForPatternAnimationOnly(java.lang.Boolean newPlayButtonForPatternAnimationOnly) {
    getInstance().setPlayButtonForPatternAnimationOnlyFromInstance(newPlayButtonForPatternAnimationOnly);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isPlayButtonForPatternAnimationOnlyFromInstance() {
    checkSync();
    return booleanMap.get("playButtonForPatternAnimationOnly");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setPlayButtonForPatternAnimationOnlyFromInstance(java.lang.Boolean newPlayButtonForPatternAnimationOnly) {
    booleanMap.put("playButtonForPatternAnimationOnly", newPlayButtonForPatternAnimationOnly);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isQueryReplaceTiedNumberEnabled() {
    return getInstance().isQueryReplaceTiedNumberEnabledFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setQueryReplaceTiedNumberEnabled(java.lang.Boolean newQueryReplaceTiedNumberEnabled) {
    getInstance().setQueryReplaceTiedNumberEnabledFromInstance(newQueryReplaceTiedNumberEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isQueryReplaceTiedNumberEnabledFromInstance() {
    checkSync();
    return booleanMap.get("queryReplaceTiedNumberEnabled");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setQueryReplaceTiedNumberEnabledFromInstance(java.lang.Boolean newQueryReplaceTiedNumberEnabled) {
    booleanMap.put("queryReplaceTiedNumberEnabled", newQueryReplaceTiedNumberEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isRequireLocalColourAllocations() {
    return getInstance().isRequireLocalColourAllocationsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setRequireLocalColourAllocations(java.lang.Boolean newRequireLocalColourAllocations) {
    getInstance().setRequireLocalColourAllocationsFromInstance(newRequireLocalColourAllocations);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isRequireLocalColourAllocationsFromInstance() {
    checkSync();
    return booleanMap.get("requireLocalColourAllocations");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setRequireLocalColourAllocationsFromInstance(java.lang.Boolean newRequireLocalColourAllocations) {
    booleanMap.put("requireLocalColourAllocations", newRequireLocalColourAllocations);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isRequireOnlyTotalTileCount() {
    return getInstance().isRequireOnlyTotalTileCountFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setRequireOnlyTotalTileCount(java.lang.Boolean newRequireOnlyTotalTileCount) {
    getInstance().setRequireOnlyTotalTileCountFromInstance(newRequireOnlyTotalTileCount);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isRequireOnlyTotalTileCountFromInstance() {
    checkSync();
    return booleanMap.get("requireOnlyTotalTileCount");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setRequireOnlyTotalTileCountFromInstance(java.lang.Boolean newRequireOnlyTotalTileCount) {
    booleanMap.put("requireOnlyTotalTileCount", newRequireOnlyTotalTileCount);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isRhythmConnector() {
    return getInstance().isRhythmConnectorFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setRhythmConnector(java.lang.Boolean newRhythmConnector) {
    getInstance().setRhythmConnectorFromInstance(newRhythmConnector);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isRhythmConnectorFromInstance() {
    checkSync();
    return booleanMap.get("rhythmConnector");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setRhythmConnectorFromInstance(java.lang.Boolean newRhythmConnector) {
    booleanMap.put("rhythmConnector", newRhythmConnector);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isRunWozServer() {
    return getInstance().isRunWozServerFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setRunWozServer(java.lang.Boolean newRunWozServer) {
    getInstance().setRunWozServerFromInstance(newRunWozServer);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isRunWozServerFromInstance() {
    checkSync();
    return booleanMap.get("runWozServer");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setRunWozServerFromInstance(java.lang.Boolean newRunWozServer) {
    booleanMap.put("runWozServer", newRunWozServer);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getServerName() {
    return getInstance().getServerNameFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setServerName(java.lang.String newServerName) {
    getInstance().setServerNameFromInstance(newServerName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getServerNameFromInstance() {
    checkSync();
    return stringMap.get("serverName");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setServerNameFromInstance(java.lang.String newServerName) {
    stringMap.put("serverName", newServerName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getServerPort() {
    return getInstance().getServerPortFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setServerPort(java.lang.Integer newServerPort) {
    getInstance().setServerPortFromInstance(newServerPort);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getServerPortFromInstance() {
    checkSync();
    return integerMap.get("serverPort");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setServerPortFromInstance(java.lang.Integer newServerPort) {
    integerMap.put("serverPort", newServerPort);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowAuthorOfVariables() {
    return getInstance().isShowAuthorOfVariablesFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowAuthorOfVariables(java.lang.Boolean newShowAuthorOfVariables) {
    getInstance().setShowAuthorOfVariablesFromInstance(newShowAuthorOfVariables);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowAuthorOfVariablesFromInstance() {
    checkSync();
    return booleanMap.get("showAuthorOfVariables");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowAuthorOfVariablesFromInstance(java.lang.Boolean newShowAuthorOfVariables) {
    booleanMap.put("showAuthorOfVariables", newShowAuthorOfVariables);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowElapsedTimeInTitleBar() {
    return getInstance().isShowElapsedTimeInTitleBarFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowElapsedTimeInTitleBar(java.lang.Boolean newShowElapsedTimeInTitleBar) {
    getInstance().setShowElapsedTimeInTitleBarFromInstance(newShowElapsedTimeInTitleBar);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowElapsedTimeInTitleBarFromInstance() {
    checkSync();
    return booleanMap.get("showElapsedTimeInTitleBar");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowElapsedTimeInTitleBarFromInstance(java.lang.Boolean newShowElapsedTimeInTitleBar) {
    booleanMap.put("showElapsedTimeInTitleBar", newShowElapsedTimeInTitleBar);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowSlavePanelGrid() {
    return getInstance().isShowSlavePanelGridFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowSlavePanelGrid(java.lang.Boolean newShowSlavePanelGrid) {
    getInstance().setShowSlavePanelGridFromInstance(newShowSlavePanelGrid);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowSlavePanelGridFromInstance() {
    checkSync();
    return booleanMap.get("showSlavePanelGrid");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowSlavePanelGridFromInstance(java.lang.Boolean newShowSlavePanelGrid) {
    booleanMap.put("showSlavePanelGrid", newShowSlavePanelGrid);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowSlaveWorldWhenTiedNumberCreated() {
    return getInstance().isShowSlaveWorldWhenTiedNumberCreatedFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowSlaveWorldWhenTiedNumberCreated(java.lang.Boolean newShowSlaveWorldWhenTiedNumberCreated) {
    getInstance().setShowSlaveWorldWhenTiedNumberCreatedFromInstance(newShowSlaveWorldWhenTiedNumberCreated);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowSlaveWorldWhenTiedNumberCreatedFromInstance() {
    checkSync();
    return booleanMap.get("showSlaveWorldWhenTiedNumberCreated");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowSlaveWorldWhenTiedNumberCreatedFromInstance(java.lang.Boolean newShowSlaveWorldWhenTiedNumberCreated) {
    booleanMap.put("showSlaveWorldWhenTiedNumberCreated", newShowSlaveWorldWhenTiedNumberCreated);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingClassroomTool() {
    return getInstance().isShowingClassroomToolFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingClassroomTool(java.lang.Boolean newShowingClassroomTool) {
    getInstance().setShowingClassroomToolFromInstance(newShowingClassroomTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingClassroomToolFromInstance() {
    checkSync();
    return booleanMap.get("showingClassroomTool");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingClassroomToolFromInstance(java.lang.Boolean newShowingClassroomTool) {
    booleanMap.put("showingClassroomTool", newShowingClassroomTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingConnectCommandButton() {
    return getInstance().isShowingConnectCommandButtonFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingConnectCommandButton(java.lang.Boolean newShowingConnectCommandButton) {
    getInstance().setShowingConnectCommandButtonFromInstance(newShowingConnectCommandButton);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingConnectCommandButtonFromInstance() {
    checkSync();
    return booleanMap.get("showingConnectCommandButton");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingConnectCommandButtonFromInstance(java.lang.Boolean newShowingConnectCommandButton) {
    booleanMap.put("showingConnectCommandButton", newShowingConnectCommandButton);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingGoalCompletionInClassDynamics() {
    return getInstance().isShowingGoalCompletionInClassDynamicsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingGoalCompletionInClassDynamics(java.lang.Boolean newShowingGoalCompletionInClassDynamics) {
    getInstance().setShowingGoalCompletionInClassDynamicsFromInstance(newShowingGoalCompletionInClassDynamics);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingGoalCompletionInClassDynamicsFromInstance() {
    checkSync();
    return booleanMap.get("showingGoalCompletionInClassDynamics");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingGoalCompletionInClassDynamicsFromInstance(java.lang.Boolean newShowingGoalCompletionInClassDynamics) {
    booleanMap.put("showingGoalCompletionInClassDynamics", newShowingGoalCompletionInClassDynamics);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingGoalCompletionInGrouping() {
    return getInstance().isShowingGoalCompletionInGroupingFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingGoalCompletionInGrouping(java.lang.Boolean newShowingGoalCompletionInGrouping) {
    getInstance().setShowingGoalCompletionInGroupingFromInstance(newShowingGoalCompletionInGrouping);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingGoalCompletionInGroupingFromInstance() {
    checkSync();
    return booleanMap.get("showingGoalCompletionInGrouping");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingGoalCompletionInGroupingFromInstance(java.lang.Boolean newShowingGoalCompletionInGrouping) {
    booleanMap.put("showingGoalCompletionInGrouping", newShowingGoalCompletionInGrouping);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingGoalsTool() {
    return getInstance().isShowingGoalsToolFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingGoalsTool(java.lang.Boolean newShowingGoalsTool) {
    getInstance().setShowingGoalsToolFromInstance(newShowingGoalsTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingGoalsToolFromInstance() {
    checkSync();
    return booleanMap.get("showingGoalsTool");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingGoalsToolFromInstance(java.lang.Boolean newShowingGoalsTool) {
    booleanMap.put("showingGoalsTool", newShowingGoalsTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingGroupingTool() {
    return getInstance().isShowingGroupingToolFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingGroupingTool(java.lang.Boolean newShowingGroupingTool) {
    getInstance().setShowingGroupingToolFromInstance(newShowingGroupingTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingGroupingToolFromInstance() {
    checkSync();
    return booleanMap.get("showingGroupingTool");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingGroupingToolFromInstance(java.lang.Boolean newShowingGroupingTool) {
    booleanMap.put("showingGroupingTool", newShowingGroupingTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingOnlyInitials() {
    return getInstance().isShowingOnlyInitialsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingOnlyInitials(java.lang.Boolean newShowingOnlyInitials) {
    getInstance().setShowingOnlyInitialsFromInstance(newShowingOnlyInitials);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingOnlyInitialsFromInstance() {
    checkSync();
    return booleanMap.get("showingOnlyInitials");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingOnlyInitialsFromInstance(java.lang.Boolean newShowingOnlyInitials) {
    booleanMap.put("showingOnlyInitials", newShowingOnlyInitials);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingServerUi() {
    return getInstance().isShowingServerUiFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingServerUi(java.lang.Boolean newShowingServerUi) {
    getInstance().setShowingServerUiFromInstance(newShowingServerUi);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingServerUiFromInstance() {
    checkSync();
    return booleanMap.get("showingServerUi");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingServerUiFromInstance(java.lang.Boolean newShowingServerUi) {
    booleanMap.put("showingServerUi", newShowingServerUi);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingSimilarityWithNumbers() {
    return getInstance().isShowingSimilarityWithNumbersFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingSimilarityWithNumbers(java.lang.Boolean newShowingSimilarityWithNumbers) {
    getInstance().setShowingSimilarityWithNumbersFromInstance(newShowingSimilarityWithNumbers);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingSimilarityWithNumbersFromInstance() {
    checkSync();
    return booleanMap.get("showingSimilarityWithNumbers");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingSimilarityWithNumbersFromInstance(java.lang.Boolean newShowingSimilarityWithNumbers) {
    booleanMap.put("showingSimilarityWithNumbers", newShowingSimilarityWithNumbers);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isShowingTimelinesTool() {
    return getInstance().isShowingTimelinesToolFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setShowingTimelinesTool(java.lang.Boolean newShowingTimelinesTool) {
    getInstance().setShowingTimelinesToolFromInstance(newShowingTimelinesTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isShowingTimelinesToolFromInstance() {
    checkSync();
    return booleanMap.get("showingTimelinesTool");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setShowingTimelinesToolFromInstance(java.lang.Boolean newShowingTimelinesTool) {
    booleanMap.put("showingTimelinesTool", newShowingTimelinesTool);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isSingleActivityOnly() {
    return getInstance().isSingleActivityOnlyFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setSingleActivityOnly(java.lang.Boolean newSingleActivityOnly) {
    getInstance().setSingleActivityOnlyFromInstance(newSingleActivityOnly);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isSingleActivityOnlyFromInstance() {
    checkSync();
    return booleanMap.get("singleActivityOnly");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setSingleActivityOnlyFromInstance(java.lang.Boolean newSingleActivityOnly) {
    booleanMap.put("singleActivityOnly", newSingleActivityOnly);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getStencilOpacity() {
    return getInstance().getStencilOpacityFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setStencilOpacity(java.lang.Integer newStencilOpacity) {
    getInstance().setStencilOpacityFromInstance(newStencilOpacity);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getStencilOpacityFromInstance() {
    checkSync();
    return integerMap.get("stencilOpacity");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setStencilOpacityFromInstance(java.lang.Integer newStencilOpacity) {
    integerMap.put("stencilOpacity", newStencilOpacity);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getTaskActivityDocument() {
    return getInstance().getTaskActivityDocumentFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTaskActivityDocument(java.lang.String newTaskActivityDocument) {
    getInstance().setTaskActivityDocumentFromInstance(newTaskActivityDocument);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getTaskActivityDocumentFromInstance() {
    checkSync();
    return stringMap.get("taskActivityDocument");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTaskActivityDocumentFromInstance(java.lang.String newTaskActivityDocument) {
    stringMap.put("taskActivityDocument", newTaskActivityDocument);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getTaskDescription() {
    return getInstance().getTaskDescriptionFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTaskDescription(java.lang.String newTaskDescription) {
    getInstance().setTaskDescriptionFromInstance(newTaskDescription);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getTaskDescriptionFromInstance() {
    checkSync();
    return stringMap.get("taskDescription");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTaskDescriptionFromInstance(java.lang.String newTaskDescription) {
    stringMap.put("taskDescription", newTaskDescription);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isTaskInActivityDocument() {
    return getInstance().isTaskInActivityDocumentFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTaskInActivityDocument(java.lang.Boolean newTaskInActivityDocument) {
    getInstance().setTaskInActivityDocumentFromInstance(newTaskInActivityDocument);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isTaskInActivityDocumentFromInstance() {
    checkSync();
    return booleanMap.get("taskInActivityDocument");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTaskInActivityDocumentFromInstance(java.lang.Boolean newTaskInActivityDocument) {
    booleanMap.put("taskInActivityDocument", newTaskInActivityDocument);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getTaskPresentationGridSize() {
    return getInstance().getTaskPresentationGridSizeFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTaskPresentationGridSize(java.lang.Integer newTaskPresentationGridSize) {
    getInstance().setTaskPresentationGridSizeFromInstance(newTaskPresentationGridSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getTaskPresentationGridSizeFromInstance() {
    checkSync();
    return integerMap.get("taskPresentationGridSize");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTaskPresentationGridSizeFromInstance(java.lang.Integer newTaskPresentationGridSize) {
    integerMap.put("taskPresentationGridSize", newTaskPresentationGridSize);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getTeacherToolMaxPollingPeriod() {
    return getInstance().getTeacherToolMaxPollingPeriodFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTeacherToolMaxPollingPeriod(java.lang.Integer newTeacherToolMaxPollingPeriod) {
    getInstance().setTeacherToolMaxPollingPeriodFromInstance(newTeacherToolMaxPollingPeriod);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getTeacherToolMaxPollingPeriodFromInstance() {
    checkSync();
    return integerMap.get("teacherToolMaxPollingPeriod");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTeacherToolMaxPollingPeriodFromInstance(java.lang.Integer newTeacherToolMaxPollingPeriod) {
    integerMap.put("teacherToolMaxPollingPeriod", newTeacherToolMaxPollingPeriod);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getTeacherToolMinPollingPeriod() {
    return getInstance().getTeacherToolMinPollingPeriodFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTeacherToolMinPollingPeriod(java.lang.Integer newTeacherToolMinPollingPeriod) {
    getInstance().setTeacherToolMinPollingPeriodFromInstance(newTeacherToolMinPollingPeriod);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getTeacherToolMinPollingPeriodFromInstance() {
    checkSync();
    return integerMap.get("teacherToolMinPollingPeriod");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTeacherToolMinPollingPeriodFromInstance(java.lang.Integer newTeacherToolMinPollingPeriod) {
    integerMap.put("teacherToolMinPollingPeriod", newTeacherToolMinPollingPeriod);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isTeacherToolPollingFromServer() {
    return getInstance().isTeacherToolPollingFromServerFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTeacherToolPollingFromServer(java.lang.Boolean newTeacherToolPollingFromServer) {
    getInstance().setTeacherToolPollingFromServerFromInstance(newTeacherToolPollingFromServer);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isTeacherToolPollingFromServerFromInstance() {
    checkSync();
    return booleanMap.get("teacherToolPollingFromServer");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTeacherToolPollingFromServerFromInstance(java.lang.Boolean newTeacherToolPollingFromServer) {
    booleanMap.put("teacherToolPollingFromServer", newTeacherToolPollingFromServer);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isTestingExternalInterface() {
    return getInstance().isTestingExternalInterfaceFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTestingExternalInterface(java.lang.Boolean newTestingExternalInterface) {
    getInstance().setTestingExternalInterfaceFromInstance(newTestingExternalInterface);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isTestingExternalInterfaceFromInstance() {
    checkSync();
    return booleanMap.get("testingExternalInterface");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTestingExternalInterfaceFromInstance(java.lang.Boolean newTestingExternalInterface) {
    booleanMap.put("testingExternalInterface", newTestingExternalInterface);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Integer getTiedNumberPlayDelay() {
    return getInstance().getTiedNumberPlayDelayFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTiedNumberPlayDelay(java.lang.Integer newTiedNumberPlayDelay) {
    getInstance().setTiedNumberPlayDelayFromInstance(newTiedNumberPlayDelay);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Integer getTiedNumberPlayDelayFromInstance() {
    checkSync();
    return integerMap.get("tiedNumberPlayDelay");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTiedNumberPlayDelayFromInstance(java.lang.Integer newTiedNumberPlayDelay) {
    integerMap.put("tiedNumberPlayDelay", newTiedNumberPlayDelay);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isTiedNumbersControlPanelEnabled() {
    return getInstance().isTiedNumbersControlPanelEnabledFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTiedNumbersControlPanelEnabled(java.lang.Boolean newTiedNumbersControlPanelEnabled) {
    getInstance().setTiedNumbersControlPanelEnabledFromInstance(newTiedNumbersControlPanelEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isTiedNumbersControlPanelEnabledFromInstance() {
    checkSync();
    return booleanMap.get("tiedNumbersControlPanelEnabled");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTiedNumbersControlPanelEnabledFromInstance(java.lang.Boolean newTiedNumbersControlPanelEnabled) {
    booleanMap.put("tiedNumbersControlPanelEnabled", newTiedNumbersControlPanelEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isTiedNumbersControlPanelOnTop() {
    return getInstance().isTiedNumbersControlPanelOnTopFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTiedNumbersControlPanelOnTop(java.lang.Boolean newTiedNumbersControlPanelOnTop) {
    getInstance().setTiedNumbersControlPanelOnTopFromInstance(newTiedNumbersControlPanelOnTop);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isTiedNumbersControlPanelOnTopFromInstance() {
    checkSync();
    return booleanMap.get("tiedNumbersControlPanelOnTop");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTiedNumbersControlPanelOnTopFromInstance(java.lang.Boolean newTiedNumbersControlPanelOnTop) {
    booleanMap.put("tiedNumbersControlPanelOnTop", newTiedNumbersControlPanelOnTop);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isTiedNumbersNameAndValueDisplayedWhenUnlocked() {
    return getInstance().isTiedNumbersNameAndValueDisplayedWhenUnlockedFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTiedNumbersNameAndValueDisplayedWhenUnlocked(java.lang.Boolean newTiedNumbersNameAndValueDisplayedWhenUnlocked) {
    getInstance().setTiedNumbersNameAndValueDisplayedWhenUnlockedFromInstance(newTiedNumbersNameAndValueDisplayedWhenUnlocked);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isTiedNumbersNameAndValueDisplayedWhenUnlockedFromInstance() {
    checkSync();
    return booleanMap.get("tiedNumbersNameAndValueDisplayedWhenUnlocked");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTiedNumbersNameAndValueDisplayedWhenUnlockedFromInstance(java.lang.Boolean newTiedNumbersNameAndValueDisplayedWhenUnlocked) {
    booleanMap.put("tiedNumbersNameAndValueDisplayedWhenUnlocked", newTiedNumbersNameAndValueDisplayedWhenUnlocked);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isTiedNumbersNameAndValueOptionAlwaysAvailable() {
    return getInstance().isTiedNumbersNameAndValueOptionAlwaysAvailableFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setTiedNumbersNameAndValueOptionAlwaysAvailable(java.lang.Boolean newTiedNumbersNameAndValueOptionAlwaysAvailable) {
    getInstance().setTiedNumbersNameAndValueOptionAlwaysAvailableFromInstance(newTiedNumbersNameAndValueOptionAlwaysAvailable);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isTiedNumbersNameAndValueOptionAlwaysAvailableFromInstance() {
    checkSync();
    return booleanMap.get("tiedNumbersNameAndValueOptionAlwaysAvailable");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setTiedNumbersNameAndValueOptionAlwaysAvailableFromInstance(java.lang.Boolean newTiedNumbersNameAndValueOptionAlwaysAvailable) {
    booleanMap.put("tiedNumbersNameAndValueOptionAlwaysAvailable", newTiedNumbersNameAndValueOptionAlwaysAvailable);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isToolsOnTheSide() {
    return getInstance().isToolsOnTheSideFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setToolsOnTheSide(java.lang.Boolean newToolsOnTheSide) {
    getInstance().setToolsOnTheSideFromInstance(newToolsOnTheSide);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isToolsOnTheSideFromInstance() {
    checkSync();
    return booleanMap.get("toolsOnTheSide");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setToolsOnTheSideFromInstance(java.lang.Boolean newToolsOnTheSide) {
    booleanMap.put("toolsOnTheSide", newToolsOnTheSide);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isUndoEnabled() {
    return getInstance().isUndoEnabledFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setUndoEnabled(java.lang.Boolean newUndoEnabled) {
    getInstance().setUndoEnabledFromInstance(newUndoEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isUndoEnabledFromInstance() {
    checkSync();
    return booleanMap.get("undoEnabled");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setUndoEnabledFromInstance(java.lang.Boolean newUndoEnabled) {
    booleanMap.put("undoEnabled", newUndoEnabled);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getUnlockedTiedNumbersColors() {
    return getInstance().getUnlockedTiedNumbersColorsFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setUnlockedTiedNumbersColors(java.lang.String newUnlockedTiedNumbersColors) {
    getInstance().setUnlockedTiedNumbersColorsFromInstance(newUnlockedTiedNumbersColors);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getUnlockedTiedNumbersColorsFromInstance() {
    checkSync();
    return stringMap.get("unlockedTiedNumbersColors");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setUnlockedTiedNumbersColorsFromInstance(java.lang.String newUnlockedTiedNumbersColors) {
    stringMap.put("unlockedTiedNumbersColors", newUnlockedTiedNumbersColors);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.String getUserName() {
    return getInstance().getUserNameFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setUserName(java.lang.String newUserName) {
    getInstance().setUserNameFromInstance(newUserName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.String getUserNameFromInstance() {
    checkSync();
    return stringMap.get("userName");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setUserNameFromInstance(java.lang.String newUserName) {
    stringMap.put("userName", newUserName);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isUsingHomePageOnly() {
    return getInstance().isUsingHomePageOnlyFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setUsingHomePageOnly(java.lang.Boolean newUsingHomePageOnly) {
    getInstance().setUsingHomePageOnlyFromInstance(newUsingHomePageOnly);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isUsingHomePageOnlyFromInstance() {
    checkSync();
    return booleanMap.get("usingHomePageOnly");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setUsingHomePageOnlyFromInstance(java.lang.Boolean newUsingHomePageOnly) {
    booleanMap.put("usingHomePageOnly", newUsingHomePageOnly);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isUsingSetupFrameOnly() {
    return getInstance().isUsingSetupFrameOnlyFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setUsingSetupFrameOnly(java.lang.Boolean newUsingSetupFrameOnly) {
    getInstance().setUsingSetupFrameOnlyFromInstance(newUsingSetupFrameOnly);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isUsingSetupFrameOnlyFromInstance() {
    checkSync();
    return booleanMap.get("usingSetupFrameOnly");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setUsingSetupFrameOnlyFromInstance(java.lang.Boolean newUsingSetupFrameOnly) {
    booleanMap.put("usingSetupFrameOnly", newUsingSetupFrameOnly);
  }

  /* Auto-generated code. Do not edit this getter. */
  public static java.lang.Boolean isVerticalNumberLayout() {
    return getInstance().isVerticalNumberLayoutFromInstance();
  }

  /* Auto-generated code. Do not edit this setter. */
  public static void setVerticalNumberLayout(java.lang.Boolean newVerticalNumberLayout) {
    getInstance().setVerticalNumberLayoutFromInstance(newVerticalNumberLayout);
  }

  /* Auto-generated code. Do not edit this getter. */
  public java.lang.Boolean isVerticalNumberLayoutFromInstance() {
    checkSync();
    return booleanMap.get("verticalNumberLayout");
  }

  /* Auto-generated code. Do not edit this setter. */
  public void setVerticalNumberLayoutFromInstance(java.lang.Boolean newVerticalNumberLayout) {
    booleanMap.put("verticalNumberLayout", newVerticalNumberLayout);
  }


  /**
   * Returns the configuration to its initial default state.
   * 
   * Note: this is automatically generated code. Do not edit this method. 
   */
  public static final void resetToSystemDefault() {
    getInstance().factoryReset();
  }

  @Override
  public final void factoryReset() {
    booleanMap.put("acknowledgeGoalAchievement",false);
    stringMap.put("activityDescriptionFile","activities.txt");
    doubleMap.put("activityDocumentToRestRatio",0.4);
    booleanMap.put("addActivityDocumentButtons",true);
    booleanMap.put("addAddRemoveTilesRadioButtons",true);
    booleanMap.put("addBoxMenuToActivityDocumentCanvasItems",false);
    booleanMap.put("addConstantsInGroupColourAllocations",true);
    booleanMap.put("addEcollaborator",false);
    booleanMap.put("addHelpPanel",true);
    booleanMap.put("addHelpRequestPanel",true);
    booleanMap.put("addMenuItemToShowAnythingUsingTiedNumber",true);
    booleanMap.put("addPropertyListWhenWizardFinished",true);
    booleanMap.put("addRulePanelWhenReplacingWithGeneralWorld",false);
    booleanMap.put("addSpaceToExpressions",true);
    booleanMap.put("allowEditingOfSlaveUnlockedNumbers",true);
    integerMap.put("animationDuration",3600);
    stringMap.put("autoSaveFileName","saved/autosaved");
    booleanMap.put("autoSaveToServer",true);
    booleanMap.put("autoSaveWhenIndicatorDetected",false);
    stringMap.put("auxStringFile","data/messages/MessagesBundle");
    stringMap.put("availableColours","");
    booleanMap.put("centralFeedbackAgentDisplayedBelowTiedNumbersControlPanel",true);
    booleanMap.put("checkToBuildAnother",true);
    stringMap.put("colorBad","#FF0000");
    stringMap.put("colorFeedback","#FFFF00");
    stringMap.put("colorGood","#00FF00");
    stringMap.put("colorNeutral","#FFC800");
    integerMap.put("colourAllocationPanelCount",1);
    stringMap.put("computerModelExpressionFontStyle","style='font-size: 15px; face: Arial; font-weight: bold;'");
    stringMap.put("computerModelRuleFontStyle","style='font-size: 10px; face: Arial; font-weight: bold;'");
    booleanMap.put("connectedToServer",false);
    stringMap.put("country","UK");
    stringMap.put("databaseDefinition","server/database.xml");
    stringMap.put("databaseName","migendb");
    integerMap.put("defaultDeltaX",0);
    integerMap.put("defaultDeltaY",0);
    integerMap.put("defaultRepeatCount",0);
    booleanMap.put("disableExpressionEditors",false);
    booleanMap.put("distinguishTilesFromPatterns",true);
    integerMap.put("dividerLocation",450);
    booleanMap.put("divisionEnabled",true);
    booleanMap.put("doNotAddCutCopyPasteTools",true);
    booleanMap.put("doNotAddDeleteTool",true);
    booleanMap.put("doNotAddNegativeColorTileButtons",true);
    booleanMap.put("doNotAddZoomTools",true);
    booleanMap.put("documentCanvasShapesDraggableDefault",true);
    booleanMap.put("drawCrossForAnyOverlap",true);
    booleanMap.put("drawCrossForMultipleColors",true);
    stringMap.put("dropFeedbackColor","#d879f7");
    integerMap.put("egenInactivityThreshold",2500);
    booleanMap.put("enableAutoVelcro",true);
    booleanMap.put("enableReplay",true);
    booleanMap.put("enableVelcro",false);
    booleanMap.put("enabledFrozenTime",true);
    booleanMap.put("enabledTaskSelection",false);
    stringMap.put("entitiesDefinition","server/entities.xml");
    stringMap.put("errorFileName","errors");
    booleanMap.put("explicitShareStudentModel",false);
    integerMap.put("generalModelCanvasGridSize",10);
    integerMap.put("generalModelCanvasHeight",300);
    integerMap.put("generalModelCanvasWidth",400);
    integerMap.put("goalTrackingCellHeight",10);
    booleanMap.put("groupsCreatedWithColourAllocationExpressions",false);
    booleanMap.put("hideColourAllocationAttributes",false);
    booleanMap.put("highlightSuggestion",false);
    booleanMap.put("ignoringSavedDoneButtonEnabledFlag",true);
    booleanMap.put("includeConstructionPartsInCollaboration",true);
    stringMap.put("initialModelFileName","");
    stringMap.put("kaleidoscopeCommonFormatLogFileName","log/KalForm-latest-usage-log.xml");
    stringMap.put("language","en");
    integerMap.put("lessonDurationInMinutes",60);
    booleanMap.put("lockedTiedNumbersCanHaveNames",false);
    stringMap.put("mainStringFile","configs/MessagesBundle");
    doubleMap.put("masterSlaveGridRatio",1.5);
    booleanMap.put("minimiseCollaborationConstructionParts",false);
    booleanMap.put("modelRuleAllColors",true);
    booleanMap.put("modelRulesButtonEnabled",false);
    booleanMap.put("multiColorBuildingBlocksAllowed",true);
    booleanMap.put("multiplyDeltaXByWidth",true);
    booleanMap.put("multiplyDeltaYByHeight",true);
    booleanMap.put("namedVariablesAreGeneral",true);
    booleanMap.put("namingUnlocksTiedNumbers",true);
    booleanMap.put("noColourAllocation",false);
    booleanMap.put("nonGeneralVariablesCanBeTaskVariables",true);
    booleanMap.put("numberNameFontBold",true);
    stringMap.put("numberNameFontFamily","Trebuchet MS");
    integerMap.put("numberNameFontSize",14);
    booleanMap.put("numberValueFontBold",true);
    stringMap.put("numberValueFontFamily","Arial");
    integerMap.put("numberValueFontSize",16);
    integerMap.put("opacity",100);
    booleanMap.put("packedSize",false);
    booleanMap.put("patternsCreatedWithColourAllocationExpressions",false);
    booleanMap.put("patternsDrawnOverGrid",false);
    integerMap.put("pixelsToStartDrag",5);
    booleanMap.put("playButtonForPatternAnimationOnly",false);
    booleanMap.put("queryReplaceTiedNumberEnabled",false);
    booleanMap.put("requireLocalColourAllocations",true);
    booleanMap.put("requireOnlyTotalTileCount",false);
    booleanMap.put("rhythmConnector",false);
    booleanMap.put("runWozServer",false);
    stringMap.put("serverName","localhost");
    integerMap.put("serverPort",8182);
    booleanMap.put("showAuthorOfVariables",true);
    booleanMap.put("showElapsedTimeInTitleBar",false);
    booleanMap.put("showSlavePanelGrid",true);
    booleanMap.put("showSlaveWorldWhenTiedNumberCreated",true);
    booleanMap.put("showingClassroomTool",true);
    booleanMap.put("showingConnectCommandButton",true);
    booleanMap.put("showingGoalCompletionInClassDynamics",true);
    booleanMap.put("showingGoalCompletionInGrouping",false);
    booleanMap.put("showingGoalsTool",true);
    booleanMap.put("showingGroupingTool",false);
    booleanMap.put("showingOnlyInitials",false);
    booleanMap.put("showingServerUi",true);
    booleanMap.put("showingSimilarityWithNumbers",false);
    booleanMap.put("showingTimelinesTool",true);
    booleanMap.put("singleActivityOnly",false);
    integerMap.put("stencilOpacity",50);
    stringMap.put("taskActivityDocument","");
    stringMap.put("taskDescription","");
    booleanMap.put("taskInActivityDocument",false);
    integerMap.put("taskPresentationGridSize",10);
    integerMap.put("teacherToolMaxPollingPeriod",3600);
    integerMap.put("teacherToolMinPollingPeriod",30);
    booleanMap.put("teacherToolPollingFromServer",true);
    booleanMap.put("testingExternalInterface",false);
    integerMap.put("tiedNumberPlayDelay",700);
    booleanMap.put("tiedNumbersControlPanelEnabled",true);
    booleanMap.put("tiedNumbersControlPanelOnTop",true);
    booleanMap.put("tiedNumbersNameAndValueDisplayedWhenUnlocked",true);
    booleanMap.put("tiedNumbersNameAndValueOptionAlwaysAvailable",true);
    booleanMap.put("toolsOnTheSide",false);
    booleanMap.put("undoEnabled",true);
    stringMap.put("unlockedTiedNumbersColors","purple:white;pink:black;orange:black;brown:yellow;magenta:white;cyan:navy;black:white;blue:yellow;red:white;green:yellow;yellow:brown");
    stringMap.put("userName","");
    booleanMap.put("usingHomePageOnly",false);
    booleanMap.put("usingSetupFrameOnly",false);
    booleanMap.put("verticalNumberLayout",true);
  }


  /**
   * Returns the current configuration.
   * 
   * Note: this is automatically generated code. Do not edit this method. 
   */
  public static final Set<ConfigurationItem> getConfiguration() {
    return getInstance().getConfigurationFromInstance();
  }

  public final Set<ConfigurationItem> getConfigurationFromInstance() {
    checkSync();

    Set<ConfigurationItem> result = new HashSet<ConfigurationItem>();
    result.add(new ConfigurationItem("Boolean", "acknowledgeGoalAchievement", booleanMap.get("acknowledgeGoalAchievement").toString()));
    result.add(new ConfigurationItem("String", "activityDescriptionFile", stringMap.get("activityDescriptionFile").toString()));
    result.add(new ConfigurationItem("Double", "activityDocumentToRestRatio", doubleMap.get("activityDocumentToRestRatio").toString()));
    result.add(new ConfigurationItem("Boolean", "addActivityDocumentButtons", booleanMap.get("addActivityDocumentButtons").toString()));
    result.add(new ConfigurationItem("Boolean", "addAddRemoveTilesRadioButtons", booleanMap.get("addAddRemoveTilesRadioButtons").toString()));
    result.add(new ConfigurationItem("Boolean", "addBoxMenuToActivityDocumentCanvasItems", booleanMap.get("addBoxMenuToActivityDocumentCanvasItems").toString()));
    result.add(new ConfigurationItem("Boolean", "addConstantsInGroupColourAllocations", booleanMap.get("addConstantsInGroupColourAllocations").toString()));
    result.add(new ConfigurationItem("Boolean", "addEcollaborator", booleanMap.get("addEcollaborator").toString()));
    result.add(new ConfigurationItem("Boolean", "addHelpPanel", booleanMap.get("addHelpPanel").toString()));
    result.add(new ConfigurationItem("Boolean", "addHelpRequestPanel", booleanMap.get("addHelpRequestPanel").toString()));
    result.add(new ConfigurationItem("Boolean", "addMenuItemToShowAnythingUsingTiedNumber", booleanMap.get("addMenuItemToShowAnythingUsingTiedNumber").toString()));
    result.add(new ConfigurationItem("Boolean", "addPropertyListWhenWizardFinished", booleanMap.get("addPropertyListWhenWizardFinished").toString()));
    result.add(new ConfigurationItem("Boolean", "addRulePanelWhenReplacingWithGeneralWorld", booleanMap.get("addRulePanelWhenReplacingWithGeneralWorld").toString()));
    result.add(new ConfigurationItem("Boolean", "addSpaceToExpressions", booleanMap.get("addSpaceToExpressions").toString()));
    result.add(new ConfigurationItem("Boolean", "allowEditingOfSlaveUnlockedNumbers", booleanMap.get("allowEditingOfSlaveUnlockedNumbers").toString()));
    result.add(new ConfigurationItem("Integer", "animationDuration", integerMap.get("animationDuration").toString()));
    result.add(new ConfigurationItem("String", "autoSaveFileName", stringMap.get("autoSaveFileName").toString()));
    result.add(new ConfigurationItem("Boolean", "autoSaveToServer", booleanMap.get("autoSaveToServer").toString()));
    result.add(new ConfigurationItem("Boolean", "autoSaveWhenIndicatorDetected", booleanMap.get("autoSaveWhenIndicatorDetected").toString()));
    result.add(new ConfigurationItem("String", "auxStringFile", stringMap.get("auxStringFile").toString()));
    result.add(new ConfigurationItem("String", "availableColours", stringMap.get("availableColours").toString()));
    result.add(new ConfigurationItem("Boolean", "centralFeedbackAgentDisplayedBelowTiedNumbersControlPanel", booleanMap.get("centralFeedbackAgentDisplayedBelowTiedNumbersControlPanel").toString()));
    result.add(new ConfigurationItem("Boolean", "checkToBuildAnother", booleanMap.get("checkToBuildAnother").toString()));
    result.add(new ConfigurationItem("String", "colorBad", stringMap.get("colorBad").toString()));
    result.add(new ConfigurationItem("String", "colorFeedback", stringMap.get("colorFeedback").toString()));
    result.add(new ConfigurationItem("String", "colorGood", stringMap.get("colorGood").toString()));
    result.add(new ConfigurationItem("String", "colorNeutral", stringMap.get("colorNeutral").toString()));
    result.add(new ConfigurationItem("Integer", "colourAllocationPanelCount", integerMap.get("colourAllocationPanelCount").toString()));
    result.add(new ConfigurationItem("String", "computerModelExpressionFontStyle", stringMap.get("computerModelExpressionFontStyle").toString()));
    result.add(new ConfigurationItem("String", "computerModelRuleFontStyle", stringMap.get("computerModelRuleFontStyle").toString()));
    result.add(new ConfigurationItem("Boolean", "connectedToServer", booleanMap.get("connectedToServer").toString()));
    result.add(new ConfigurationItem("String", "country", stringMap.get("country").toString()));
    result.add(new ConfigurationItem("String", "databaseDefinition", stringMap.get("databaseDefinition").toString()));
    result.add(new ConfigurationItem("String", "databaseName", stringMap.get("databaseName").toString()));
    result.add(new ConfigurationItem("Integer", "defaultDeltaX", integerMap.get("defaultDeltaX").toString()));
    result.add(new ConfigurationItem("Integer", "defaultDeltaY", integerMap.get("defaultDeltaY").toString()));
    result.add(new ConfigurationItem("Integer", "defaultRepeatCount", integerMap.get("defaultRepeatCount").toString()));
    result.add(new ConfigurationItem("Boolean", "disableExpressionEditors", booleanMap.get("disableExpressionEditors").toString()));
    result.add(new ConfigurationItem("Boolean", "distinguishTilesFromPatterns", booleanMap.get("distinguishTilesFromPatterns").toString()));
    result.add(new ConfigurationItem("Integer", "dividerLocation", integerMap.get("dividerLocation").toString()));
    result.add(new ConfigurationItem("Boolean", "divisionEnabled", booleanMap.get("divisionEnabled").toString()));
    result.add(new ConfigurationItem("Boolean", "doNotAddCutCopyPasteTools", booleanMap.get("doNotAddCutCopyPasteTools").toString()));
    result.add(new ConfigurationItem("Boolean", "doNotAddDeleteTool", booleanMap.get("doNotAddDeleteTool").toString()));
    result.add(new ConfigurationItem("Boolean", "doNotAddNegativeColorTileButtons", booleanMap.get("doNotAddNegativeColorTileButtons").toString()));
    result.add(new ConfigurationItem("Boolean", "doNotAddZoomTools", booleanMap.get("doNotAddZoomTools").toString()));
    result.add(new ConfigurationItem("Boolean", "documentCanvasShapesDraggableDefault", booleanMap.get("documentCanvasShapesDraggableDefault").toString()));
    result.add(new ConfigurationItem("Boolean", "drawCrossForAnyOverlap", booleanMap.get("drawCrossForAnyOverlap").toString()));
    result.add(new ConfigurationItem("Boolean", "drawCrossForMultipleColors", booleanMap.get("drawCrossForMultipleColors").toString()));
    result.add(new ConfigurationItem("String", "dropFeedbackColor", stringMap.get("dropFeedbackColor").toString()));
    result.add(new ConfigurationItem("Integer", "egenInactivityThreshold", integerMap.get("egenInactivityThreshold").toString()));
    result.add(new ConfigurationItem("Boolean", "enableAutoVelcro", booleanMap.get("enableAutoVelcro").toString()));
    result.add(new ConfigurationItem("Boolean", "enableReplay", booleanMap.get("enableReplay").toString()));
    result.add(new ConfigurationItem("Boolean", "enableVelcro", booleanMap.get("enableVelcro").toString()));
    result.add(new ConfigurationItem("Boolean", "enabledFrozenTime", booleanMap.get("enabledFrozenTime").toString()));
    result.add(new ConfigurationItem("Boolean", "enabledTaskSelection", booleanMap.get("enabledTaskSelection").toString()));
    result.add(new ConfigurationItem("String", "entitiesDefinition", stringMap.get("entitiesDefinition").toString()));
    result.add(new ConfigurationItem("String", "errorFileName", stringMap.get("errorFileName").toString()));
    result.add(new ConfigurationItem("Boolean", "explicitShareStudentModel", booleanMap.get("explicitShareStudentModel").toString()));
    result.add(new ConfigurationItem("Integer", "generalModelCanvasGridSize", integerMap.get("generalModelCanvasGridSize").toString()));
    result.add(new ConfigurationItem("Integer", "generalModelCanvasHeight", integerMap.get("generalModelCanvasHeight").toString()));
    result.add(new ConfigurationItem("Integer", "generalModelCanvasWidth", integerMap.get("generalModelCanvasWidth").toString()));
    result.add(new ConfigurationItem("Integer", "goalTrackingCellHeight", integerMap.get("goalTrackingCellHeight").toString()));
    result.add(new ConfigurationItem("Boolean", "groupsCreatedWithColourAllocationExpressions", booleanMap.get("groupsCreatedWithColourAllocationExpressions").toString()));
    result.add(new ConfigurationItem("Boolean", "hideColourAllocationAttributes", booleanMap.get("hideColourAllocationAttributes").toString()));
    result.add(new ConfigurationItem("Boolean", "highlightSuggestion", booleanMap.get("highlightSuggestion").toString()));
    result.add(new ConfigurationItem("Boolean", "ignoringSavedDoneButtonEnabledFlag", booleanMap.get("ignoringSavedDoneButtonEnabledFlag").toString()));
    result.add(new ConfigurationItem("Boolean", "includeConstructionPartsInCollaboration", booleanMap.get("includeConstructionPartsInCollaboration").toString()));
    result.add(new ConfigurationItem("String", "initialModelFileName", stringMap.get("initialModelFileName").toString()));
    result.add(new ConfigurationItem("String", "kaleidoscopeCommonFormatLogFileName", stringMap.get("kaleidoscopeCommonFormatLogFileName").toString()));
    result.add(new ConfigurationItem("String", "language", stringMap.get("language").toString()));
    result.add(new ConfigurationItem("Integer", "lessonDurationInMinutes", integerMap.get("lessonDurationInMinutes").toString()));
    result.add(new ConfigurationItem("Boolean", "lockedTiedNumbersCanHaveNames", booleanMap.get("lockedTiedNumbersCanHaveNames").toString()));
    result.add(new ConfigurationItem("String", "mainStringFile", stringMap.get("mainStringFile").toString()));
    result.add(new ConfigurationItem("Double", "masterSlaveGridRatio", doubleMap.get("masterSlaveGridRatio").toString()));
    result.add(new ConfigurationItem("Boolean", "minimiseCollaborationConstructionParts", booleanMap.get("minimiseCollaborationConstructionParts").toString()));
    result.add(new ConfigurationItem("Boolean", "modelRuleAllColors", booleanMap.get("modelRuleAllColors").toString()));
    result.add(new ConfigurationItem("Boolean", "modelRulesButtonEnabled", booleanMap.get("modelRulesButtonEnabled").toString()));
    result.add(new ConfigurationItem("Boolean", "multiColorBuildingBlocksAllowed", booleanMap.get("multiColorBuildingBlocksAllowed").toString()));
    result.add(new ConfigurationItem("Boolean", "multiplyDeltaXByWidth", booleanMap.get("multiplyDeltaXByWidth").toString()));
    result.add(new ConfigurationItem("Boolean", "multiplyDeltaYByHeight", booleanMap.get("multiplyDeltaYByHeight").toString()));
    result.add(new ConfigurationItem("Boolean", "namedVariablesAreGeneral", booleanMap.get("namedVariablesAreGeneral").toString()));
    result.add(new ConfigurationItem("Boolean", "namingUnlocksTiedNumbers", booleanMap.get("namingUnlocksTiedNumbers").toString()));
    result.add(new ConfigurationItem("Boolean", "noColourAllocation", booleanMap.get("noColourAllocation").toString()));
    result.add(new ConfigurationItem("Boolean", "nonGeneralVariablesCanBeTaskVariables", booleanMap.get("nonGeneralVariablesCanBeTaskVariables").toString()));
    result.add(new ConfigurationItem("Boolean", "numberNameFontBold", booleanMap.get("numberNameFontBold").toString()));
    result.add(new ConfigurationItem("String", "numberNameFontFamily", stringMap.get("numberNameFontFamily").toString()));
    result.add(new ConfigurationItem("Integer", "numberNameFontSize", integerMap.get("numberNameFontSize").toString()));
    result.add(new ConfigurationItem("Boolean", "numberValueFontBold", booleanMap.get("numberValueFontBold").toString()));
    result.add(new ConfigurationItem("String", "numberValueFontFamily", stringMap.get("numberValueFontFamily").toString()));
    result.add(new ConfigurationItem("Integer", "numberValueFontSize", integerMap.get("numberValueFontSize").toString()));
    result.add(new ConfigurationItem("Integer", "opacity", integerMap.get("opacity").toString()));
    result.add(new ConfigurationItem("Boolean", "packedSize", booleanMap.get("packedSize").toString()));
    result.add(new ConfigurationItem("Boolean", "patternsCreatedWithColourAllocationExpressions", booleanMap.get("patternsCreatedWithColourAllocationExpressions").toString()));
    result.add(new ConfigurationItem("Boolean", "patternsDrawnOverGrid", booleanMap.get("patternsDrawnOverGrid").toString()));
    result.add(new ConfigurationItem("Integer", "pixelsToStartDrag", integerMap.get("pixelsToStartDrag").toString()));
    result.add(new ConfigurationItem("Boolean", "playButtonForPatternAnimationOnly", booleanMap.get("playButtonForPatternAnimationOnly").toString()));
    result.add(new ConfigurationItem("Boolean", "queryReplaceTiedNumberEnabled", booleanMap.get("queryReplaceTiedNumberEnabled").toString()));
    result.add(new ConfigurationItem("Boolean", "requireLocalColourAllocations", booleanMap.get("requireLocalColourAllocations").toString()));
    result.add(new ConfigurationItem("Boolean", "requireOnlyTotalTileCount", booleanMap.get("requireOnlyTotalTileCount").toString()));
    result.add(new ConfigurationItem("Boolean", "rhythmConnector", booleanMap.get("rhythmConnector").toString()));
    result.add(new ConfigurationItem("Boolean", "runWozServer", booleanMap.get("runWozServer").toString()));
    result.add(new ConfigurationItem("String", "serverName", stringMap.get("serverName").toString()));
    result.add(new ConfigurationItem("Integer", "serverPort", integerMap.get("serverPort").toString()));
    result.add(new ConfigurationItem("Boolean", "showAuthorOfVariables", booleanMap.get("showAuthorOfVariables").toString()));
    result.add(new ConfigurationItem("Boolean", "showElapsedTimeInTitleBar", booleanMap.get("showElapsedTimeInTitleBar").toString()));
    result.add(new ConfigurationItem("Boolean", "showSlavePanelGrid", booleanMap.get("showSlavePanelGrid").toString()));
    result.add(new ConfigurationItem("Boolean", "showSlaveWorldWhenTiedNumberCreated", booleanMap.get("showSlaveWorldWhenTiedNumberCreated").toString()));
    result.add(new ConfigurationItem("Boolean", "showingClassroomTool", booleanMap.get("showingClassroomTool").toString()));
    result.add(new ConfigurationItem("Boolean", "showingConnectCommandButton", booleanMap.get("showingConnectCommandButton").toString()));
    result.add(new ConfigurationItem("Boolean", "showingGoalCompletionInClassDynamics", booleanMap.get("showingGoalCompletionInClassDynamics").toString()));
    result.add(new ConfigurationItem("Boolean", "showingGoalCompletionInGrouping", booleanMap.get("showingGoalCompletionInGrouping").toString()));
    result.add(new ConfigurationItem("Boolean", "showingGoalsTool", booleanMap.get("showingGoalsTool").toString()));
    result.add(new ConfigurationItem("Boolean", "showingGroupingTool", booleanMap.get("showingGroupingTool").toString()));
    result.add(new ConfigurationItem("Boolean", "showingOnlyInitials", booleanMap.get("showingOnlyInitials").toString()));
    result.add(new ConfigurationItem("Boolean", "showingServerUi", booleanMap.get("showingServerUi").toString()));
    result.add(new ConfigurationItem("Boolean", "showingSimilarityWithNumbers", booleanMap.get("showingSimilarityWithNumbers").toString()));
    result.add(new ConfigurationItem("Boolean", "showingTimelinesTool", booleanMap.get("showingTimelinesTool").toString()));
    result.add(new ConfigurationItem("Boolean", "singleActivityOnly", booleanMap.get("singleActivityOnly").toString()));
    result.add(new ConfigurationItem("Integer", "stencilOpacity", integerMap.get("stencilOpacity").toString()));
    result.add(new ConfigurationItem("String", "taskActivityDocument", stringMap.get("taskActivityDocument").toString()));
    result.add(new ConfigurationItem("String", "taskDescription", stringMap.get("taskDescription").toString()));
    result.add(new ConfigurationItem("Boolean", "taskInActivityDocument", booleanMap.get("taskInActivityDocument").toString()));
    result.add(new ConfigurationItem("Integer", "taskPresentationGridSize", integerMap.get("taskPresentationGridSize").toString()));
    result.add(new ConfigurationItem("Integer", "teacherToolMaxPollingPeriod", integerMap.get("teacherToolMaxPollingPeriod").toString()));
    result.add(new ConfigurationItem("Integer", "teacherToolMinPollingPeriod", integerMap.get("teacherToolMinPollingPeriod").toString()));
    result.add(new ConfigurationItem("Boolean", "teacherToolPollingFromServer", booleanMap.get("teacherToolPollingFromServer").toString()));
    result.add(new ConfigurationItem("Boolean", "testingExternalInterface", booleanMap.get("testingExternalInterface").toString()));
    result.add(new ConfigurationItem("Integer", "tiedNumberPlayDelay", integerMap.get("tiedNumberPlayDelay").toString()));
    result.add(new ConfigurationItem("Boolean", "tiedNumbersControlPanelEnabled", booleanMap.get("tiedNumbersControlPanelEnabled").toString()));
    result.add(new ConfigurationItem("Boolean", "tiedNumbersControlPanelOnTop", booleanMap.get("tiedNumbersControlPanelOnTop").toString()));
    result.add(new ConfigurationItem("Boolean", "tiedNumbersNameAndValueDisplayedWhenUnlocked", booleanMap.get("tiedNumbersNameAndValueDisplayedWhenUnlocked").toString()));
    result.add(new ConfigurationItem("Boolean", "tiedNumbersNameAndValueOptionAlwaysAvailable", booleanMap.get("tiedNumbersNameAndValueOptionAlwaysAvailable").toString()));
    result.add(new ConfigurationItem("Boolean", "toolsOnTheSide", booleanMap.get("toolsOnTheSide").toString()));
    result.add(new ConfigurationItem("Boolean", "undoEnabled", booleanMap.get("undoEnabled").toString()));
    result.add(new ConfigurationItem("String", "unlockedTiedNumbersColors", stringMap.get("unlockedTiedNumbersColors").toString()));
    result.add(new ConfigurationItem("String", "userName", stringMap.get("userName").toString()));
    result.add(new ConfigurationItem("Boolean", "usingHomePageOnly", booleanMap.get("usingHomePageOnly").toString()));
    result.add(new ConfigurationItem("Boolean", "usingSetupFrameOnly", booleanMap.get("usingSetupFrameOnly").toString()));
    result.add(new ConfigurationItem("Boolean", "verticalNumberLayout", booleanMap.get("verticalNumberLayout").toString()));
  return result;
  }

  @SuppressWarnings("unused") // Not used since sync-check is done in a JUnit test
  private static final String defaultsFileAutoGen_ = "migendefaults.xml";


  @Override
  public void setGenericProperty(ConfigurationItem ci) {
    String key   = ci.getName();  
    String type  = ci.getShortType();
    Object value = ci.getValueAsObject();
    if ("String".equals(type)) {
      if (!stringMap.containsKey(key))
        throw new IllegalArgumentException("Invalid string property name: " + key);
        
      stringMap.put(key, (String) value);
    } else if ("Boolean".equals(type)) {
      if (!booleanMap.containsKey(key))
        throw new IllegalArgumentException("Invalid boolean property name: " + key);
        
      booleanMap.put(key, (Boolean) value);
    } else if ("Integer".equals(type)) {
      if (!integerMap.containsKey(key))
        throw new IllegalArgumentException("Invalid integer property name: " + key);
      
      integerMap.put(key, (Integer) value);
    } else if ("Double".equals(type)) {
      if (!doubleMap.containsKey(key))
        throw new IllegalArgumentException("Invalid double property name: " + key);
        
      doubleMap.put(key, (Double) value);
    } else {
      if (ConfigurationParser.isValidType(type)) {
        throw new UnsupportedOperationException("Type '" + type + "' not supported yet.");
      } else {
        throw new IllegalArgumentException("Type '" + type + "' is not valid.");
      }
    }
  }

  @SuppressWarnings("unused") // check-sync is now performed externally as a JUnit test
  private static boolean checkSyncAutoGen_ = false;

  public static void checkSync()  {
   getInstance().checkConfigurationSync();
  }

  @Override
  public void checkConfigurationSync()  {
    // Since r10459, check-sync is now performed externally as a JUnit test
   }
}

