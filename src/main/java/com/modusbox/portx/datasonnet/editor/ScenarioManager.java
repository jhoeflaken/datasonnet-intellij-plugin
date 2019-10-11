package com.modusbox.portx.datasonnet.editor;

import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.Application;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.components.AbstractProjectComponent;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jps.model.java.JavaModuleSourceRootTypes;
import org.jetbrains.jps.model.java.JavaResourceRootType;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class ScenarioManager  extends AbstractProjectComponent implements Disposable {

    public static final String INTEGRATION_TEST_FOLDER_NAME = "mapping-tests";
    public static final String INTEGRATION_TEST_FOLDER_PATH = "src/test/mapping-tests";
    public static final String NO_SCENARIO = "No scenario";
    public static final String DEFAULT_SCENARIO_NAME = "default_scenario";

    private Map<String, Scenario> selectedScenariosByMapping = new HashMap<>();
    private Map<String, VirtualFile> dataSonnetInputsFolders = new HashMap<>();

    protected ScenarioManager(Project project) {
        super(project);
    }

    public static ScenarioManager getInstance(Project myProject) {
        return myProject.getComponent(ScenarioManager.class);
    }

    @Nullable
    public Scenario createScenario(PsiFile psiFile, String scenarioName) {
        VirtualFile testFolder = findOrCreateMappingTestFolder(psiFile);
        try {
            VirtualFile scenarioFolder = WriteAction.compute(() -> testFolder.createChildDirectory(this, scenarioName));
            Scenario scenario = new Scenario(scenarioFolder);
            String mappingFileName = psiFile.getVirtualFile().getCanonicalPath();
            setCurrentScenario(mappingFileName, scenario);
            return scenario;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    public VirtualFile findOrCreateMappingTestFolder(PsiFile psiFile) {
        VirtualFile testFolder = findMappingTestFolder(psiFile);
        if (testFolder == null) {
            testFolder = createMappingTestFolder(psiFile);
        }
        return testFolder;
    }
    @Nullable
    public VirtualFile findMappingTestFolder(PsiFile psiFile) {
        if (psiFile != null) {
            String qualifiedName = psiFile.getName();
            VirtualFile scenariosRootFolder = getScenariosRootFolder(psiFile);
            if (scenariosRootFolder != null && scenariosRootFolder.isValid()) {
                return scenariosRootFolder.findChild(qualifiedName);
            }
        }
        return null;
    }
    @Nullable
    public VirtualFile createMappingTestFolder(PsiFile dataSonnetFile) {
        return WriteAction.compute(() -> {
            try {
                //TODO: handle creation of dataSonnetInputs folder
                VirtualFile dataSonnetInputsFolder = getScenariosRootFolder(dataSonnetFile);
                String qName = dataSonnetFile.getName();
                return dataSonnetInputsFolder.createChildDirectory(this, qName);
            } catch (IOException e) {
                return null;
            }
        });
    }
    @Nullable
    public VirtualFile getScenariosRootFolder(PsiFile dataSonnetFile) {
        final Module module = ModuleUtil.findModuleForFile(dataSonnetFile.getVirtualFile(), dataSonnetFile.getProject());
        if (module != null) {
            return getScenariosRootFolder(module);
        }
        return null;
    }

    @Nullable
    private VirtualFile getScenariosRootFolder(@Nullable Module module) {
        if (module == null) {
            return null;
        }
        String moduleName = module.getName();
        VirtualFile maybeFolder = dataSonnetInputsFolders.get(moduleName);
        if (maybeFolder != null) {
            return maybeFolder;
        }

        ModuleRootManager rootManager = ModuleRootManager.getInstance(module);

        //TODO getSourceRoots - tests only JavaModuleSourceRootTypes.TESTS
        List<VirtualFile> testRoots = rootManager.getSourceRoots(JavaResourceRootType.TEST_RESOURCE);
        if (testRoots == null || testRoots.isEmpty()) {
            return null;
        }

        for (VirtualFile testRoot : testRoots) {
            if (findMappingTestsRoot(testRoot) != null) {
                dataSonnetInputsFolders.put(moduleName, testRoot);
                return testRoot;
            }
        }

        VirtualFile[] sourceRoots = rootManager.getSourceRoots(true);
        for (VirtualFile sourceRoot : sourceRoots) {
            if (sourceRoot.isDirectory() && sourceRoot.getName().endsWith(INTEGRATION_TEST_FOLDER_NAME)) {
                dataSonnetInputsFolders.put(moduleName, sourceRoot);
                return sourceRoot;
            }
        }

        if (dataSonnetInputsFolders.get(moduleName) == null) { //Need to create one
            try {
                //See if "src/test/dataSonnetInputs exists, if not, create it
                VirtualFile moduleRoot = rootManager.getContentRoots()[0];
                VirtualFile testdataSonnetInputs = LocalFileSystem.getInstance().findFileByIoFile(new File(rootManager.getContentRoots()[0].getCanonicalPath(), INTEGRATION_TEST_FOLDER_PATH));
                if (testdataSonnetInputs == null) {
                    VirtualFile srcDir = moduleRoot.findFileByRelativePath("src");
                    if (srcDir == null)
                        srcDir = moduleRoot.createChildDirectory(this, "src");
                    VirtualFile testDir = srcDir.findFileByRelativePath("test");
                    if (testDir == null)
                        testDir = srcDir.createChildDirectory(this, "test");
                    //Create it here
                    testdataSonnetInputs = testDir.createChildDirectory(this, INTEGRATION_TEST_FOLDER_NAME);
                }

                final VirtualFile dataSonnetInputsFile = testdataSonnetInputs;

                final Application app = ApplicationManager.getApplication();
                Runnable action = new Runnable() {
                    @Override
                    public void run() {
                        app.runWriteAction(new Runnable() {
                            @Override
                            public void run() {
                                ModifiableRootModel model = ModuleRootManager.getInstance(module).getModifiableModel();
                                ContentEntry[] entries = model.getContentEntries();
                                for (ContentEntry entry : entries) {
                                    if (entry.getFile() == moduleRoot)
                                        entry.addSourceFolder(dataSonnetInputsFile, true);
                                }
                                model.commit();
                            }
                        });
                    }
                };

                if (app.isDispatchThread()) {
                    action.run();
                }
                else {
                    app.invokeAndWait(action, ModalityState.current());
                }

                dataSonnetInputsFolders.put(moduleName, testdataSonnetInputs);
                return testdataSonnetInputs;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return null;
    }

    public void setCurrentScenario(String dataSonnetMappingName, Scenario scenario) {
        this.selectedScenariosByMapping.put(dataSonnetMappingName, scenario);
    }

    public Scenario getCurrentScenario(String dataSonnetMappingName) {
        return selectedScenariosByMapping.get(dataSonnetMappingName);
    }

    @NotNull
    public List<Scenario> getScenariosFor(PsiFile dataSonnetMappingFile) {
        final List<Scenario> result = new ArrayList<>();
        final Module moduleForFile = ModuleUtil.findModuleForFile(dataSonnetMappingFile.getVirtualFile(), dataSonnetMappingFile.getProject());
        if (moduleForFile != null) {
            List<VirtualFile> scenarios = findScenarios(dataSonnetMappingFile);
            result.addAll(scenarios.stream().map(Scenario::new).collect(Collectors.toList()));
        }
        return result;
    }

    private List<VirtualFile> findScenarios(PsiFile psiFile) {
        VirtualFile mappingTestFolder = findMappingTestFolder(psiFile);
        if (mappingTestFolder != null) {
            return Arrays.asList(mappingTestFolder.getChildren());
        }
        return new ArrayList<>();
    }

    @Nullable
    private VirtualFile findMappingTestsRoot(VirtualFile root) {
        if (root.isDirectory() && root.getName().equals(INTEGRATION_TEST_FOLDER_NAME)) {
            return root;
        } else {
            VirtualFile[] children = root.getChildren();
            for (VirtualFile child : children) {
                VirtualFile mappingTestFolder = findMappingTestsRoot(child);
                if (mappingTestFolder != null) {
                    return mappingTestFolder;
                }
            }
        }
        return null;
    }


    @Override
    public void dispose() {

    }
}