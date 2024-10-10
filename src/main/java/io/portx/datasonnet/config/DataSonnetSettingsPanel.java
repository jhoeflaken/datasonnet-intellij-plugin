package io.portx.datasonnet.config;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.*;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class DataSonnetSettingsPanel {
    private JPanel myWholePanel;
    private JPanel pnlProjectSettings;
    private JBList<String> pathList;
    private JPanel mySearchPathPanel;

    private JPanel pnlTemplate;
    private JPanel pnlAutoSync;
    private JCheckBox chkAutoRefresh;
    private JTextArea txtTemplate;

    private DataSonnetProjectSettingsComponent projectSettings;

    private CollectionListModel<String> pathsModel;

    /**
     * Create the DataSonnet project settings panel.
     *
     * @param theProjectSettings The project settings component which contains the current settings and is used
     *                           to apply and store new settings.
     */
    public JComponent createPanel(@NotNull DataSonnetProjectSettingsComponent theProjectSettings) {
        projectSettings = theProjectSettings;

        pnlProjectSettings.setBorder(IdeBorderFactory.createTitledBorder("Project Settings"));

        pathsModel = new CollectionListModel<>();
        pathList = new JBList<>();
        pathList.setModel(pathsModel);
        pathList.getEmptyText().setText("No additional DataSonnet library paths");
        pathList.setCellRenderer(new ColoredListCellRenderer() {
            @Override
            protected void customizeCellRenderer(@NotNull JList list, Object value, int index, boolean selected, boolean hasFocus) {
                append(value.toString(), SimpleTextAttributes.REGULAR_ATTRIBUTES);
            }
        });

        ToolbarDecorator toolbarDecorator = ToolbarDecorator.createDecorator(pathList);

        toolbarDecorator.setAddAction(new AnActionButtonRunnable() {
            @Override
            public void run(AnActionButton anActionButton) {
                FileChooserDescriptor fileChooserDescriptor = new FileChooserDescriptor(false, true, false, false, false, false);
                final @NotNull VirtualFile dir = FileChooser.chooseFile(fileChooserDescriptor, null, null);
                if (dir != null) {
                    pathsModel.add(dir.getCanonicalPath());
                }
            }
        });

        mySearchPathPanel.add(toolbarDecorator.createPanel(), BorderLayout.CENTER);

        return myWholePanel;
    }

    /**
     * Check if the settings have been modified.
     *
     * @return Whether the settings have been modified.
     */
    public boolean isModified() {
        final DataSonnetProjectSettings state = projectSettings.getState();
        if (state == null) {
            return false;
        }

        return chkAutoRefresh.isSelected() != state.getAutoRefresh() ||
                !Objects.equals(txtTemplate.getText(), state.getDefaultTemplate()) ||
                !Objects.equals(pathsModel.getItems(), state.getDataSonnetLibraryPaths());
    }

    /**
     * Apply the settings to the project.
     */
    public void apply() {
        final DataSonnetProjectSettings state = projectSettings.getState();
        if (state != null) {
            state.setAutoRefresh(chkAutoRefresh.isSelected());
            state.setDefaultTemplate(txtTemplate.getText());

            final java.util.List<String> dataSonnetPaths = new ArrayList<>(pathsModel.getItems());
            state.setDataSonnetLibraryPaths(dataSonnetPaths);
        }
    }

    /**
     * Reset the settings to the last applied settings.
     */
    public void reset() {
        final DataSonnetProjectSettings state = projectSettings.getState();
        if (state != null) {
            chkAutoRefresh.setSelected(state.getAutoRefresh());
            txtTemplate.setText(state.getDefaultTemplate());
            pathsModel.replaceAll(state.getDataSonnetLibraryPaths());
        }
    }

    public Color getDefaultValueColor() {
        return findColorByKey("TextField.inactiveForeground", "nimbusDisabledText");
    }

    @NotNull
    private static Color findColorByKey(String... colorKeys) {
        Color c = null;
        for (String key : colorKeys) {
            c = UIManager.getColor(key);
            if (c != null) {
                break;
            }
        }

        assert c != null : "Can't find color for keys " + Arrays.toString(colorKeys);
        return c;
    }

    public Color getChangedValueColor() {
        return findColorByKey("TextField.foreground");
    }
}
