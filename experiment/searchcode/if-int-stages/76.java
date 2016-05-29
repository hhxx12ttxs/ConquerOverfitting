package com.spantons.stagesMenu;

import com.spantons.stages.GameStagesManager;

public class SelectWaitMenu implements ISelectAction {
gsm = _gsm;
}

/****************************************************************************************/
@Override
public void select(int _currentChoice) {
if (_currentChoice == 0)
gsm.loadLoadedStage();
}

}

