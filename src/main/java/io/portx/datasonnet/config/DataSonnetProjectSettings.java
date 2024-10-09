package io.portx.datasonnet.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The plugins settings for a DataSonnet project.
 */
public class DataSonnetProjectSettings implements Serializable {

    /**
     * The default template for a new DataSonnet file.
     */
    private static final String DEFAULT_TEMPLATE = """
            /** DataSonnet
            version=2.5
            default null
            */
            
            {}
            """;

    private List<String> dataSonnetLibraryPaths;
    private boolean autoRefresh;
    private String defaultTemplate;

    /**
     * Creates a new DataSonnetProjectSettings object with default values.
     */
    public DataSonnetProjectSettings() {
        dataSonnetLibraryPaths = new ArrayList<>();
        autoRefresh = false;
        defaultTemplate = DEFAULT_TEMPLATE;
    }

    /**
     * Gets the paths to the DataSonnet libraries.
     *
     * @return The paths to the DataSonnet libraries.
     */
    public List<String> getDataSonnetLibraryPaths() {
        return dataSonnetLibraryPaths;
    }

    /**
     * Sets the paths to the DataSonnet libraries.
     *
     * @param theDataSonnetLibraryPaths The paths to the DataSonnet libraries.
     */
    public void setDataSonnetLibraryPaths(List<String> theDataSonnetLibraryPaths) {
        dataSonnetLibraryPaths = theDataSonnetLibraryPaths;
    }

    /**
     * Gets whether to automatically sync the DataSonnet mappings. The default is {@code false}.
     *
     * @return Whether to automatically sync the DataSonnet mappings.
     */
    public boolean getAutoRefresh() {
        return autoRefresh;
    }

    /**
     * Sets whether to automatically sync the DataSonnet mappings.
     *
     * @param theAutoSync Whether to automatically sync the DataSonnet mappings.
     */
    public void setAutoRefresh(boolean theAutoSync) {
        autoRefresh = theAutoSync;
    }

    /**
     * Gets the default template for a new DataSonnet file. The default is {@link #DEFAULT_TEMPLATE}
     *
     * @return The default template for a new DataSonnet file.
     */
    public String getDefaultTemplate() {
        return defaultTemplate;
    }

    /**
     * Sets the default template for a new DataSonnet file.
     *
     * @param theDefaultTemplate The default template for a new DataSonnet file.
     */
    public void setDefaultTemplate(String theDefaultTemplate) {
        defaultTemplate = theDefaultTemplate;
    }

}
