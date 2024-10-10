package io.portx.datasonnet.editor;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import com.intellij.openapi.util.IconLoader;
import io.portx.datasonnet.config.DataSonnetProjectSettings;
import io.portx.datasonnet.config.DataSonnetProjectSettingsComponent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

/**
 * Created by eberman on 4/22/17.
 */
public class AutoRefreshAction extends ToggleAction {
    DataSonnetEditor editor;

    final static Icon autosyncIcon = IconLoader.findIcon("/icons/autosync.png", AutoRefreshAction.class);

    public AutoRefreshAction(DataSonnetEditor editor) {
        super("Auto Refresh", "Toggle Auto Refresh Mapping On/Off", autosyncIcon);
        this.editor = editor;

        boolean autoRefresh = false;
        final DataSonnetProjectSettings settings = DataSonnetProjectSettingsComponent.getSettings(editor.getProject());
        if (settings != null) {
            autoRefresh = settings.getAutoRefresh();
        }

        setSelected(null, autoRefresh);
    }

    @Override
    public boolean isSelected(AnActionEvent anActionEvent) {
        return editor.isAutoSync();
    }

    @Override
    public void setSelected(AnActionEvent anActionEvent, boolean b) {
        editor.setAutoSync(b);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
