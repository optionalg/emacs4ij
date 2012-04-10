package org.jetbrains.emacs4ij;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.keymap.Keymap;
import com.intellij.openapi.keymap.KeymapManager;
import com.intellij.openapi.keymap.impl.KeymapManagerImpl;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.IdeFrame;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.openapi.wm.impl.IdeFrameImpl;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.GlobalEnvironment;
import org.jetbrains.emacs4ij.jelisp.exception.DoubleBufferException;
import org.jetbrains.emacs4ij.jelisp.exception.LispException;

/**
 * Created by IntelliJ IDEA.
 * User: kate
 * Date: 2/13/12
 * Time: 9:55 AM
 * To change this template use File | Settings | File Templates.
 */
public class EnvironmentInitializer {
    private static boolean isGlobalInitialized = false;

    public static boolean isGlobalInitialized() {
        return isGlobalInitialized;
    }
    
    public static void reset() {
        isGlobalInitialized = false;
    }

    public static boolean silentInitGlobal() {
        EmacsHomeService emacsHomeService = ServiceManager.getService(EmacsHomeService.class);
        EmacsSourceService emacsSourceService = ServiceManager.getService(EmacsSourceService.class);
        if (emacsHomeService.isParameterSet() && emacsSourceService.isParameterSet()) {
            try {
                return init();
            } catch (LispException e) {
                //skip
            }
        }
        return isGlobalInitialized;
    }

    public static boolean initGlobal() {
        try {
            return init();
        } catch (LispException e) {
            GlobalEnvironment.showErrorMessage(e.getMessage());
        }
        return isGlobalInitialized;
    }

    private static boolean init() {
        if (isGlobalInitialized)
            return true;
        Keymap userKeymap = KeymapManager.getInstance().getActiveKeymap();
        try {
            GlobalEnvironment.initialize(new KeymapCreator(), new IdeProvider(),new FrameManagerImpl());
            isGlobalInitialized = true;
        } catch (LispException e) {
            ((KeymapManagerImpl) KeymapManager.getInstance()).setActiveKeymap(userKeymap);
            throw e;
        }
        return isGlobalInitialized;
    }

    public static void initProjectEnv (final Project project, final Environment environment) {
        WindowManager windowManager = WindowManager.getInstance();
        for (IdeFrame frame: windowManager.getAllFrames()) {
            IdeaFrame ideaFrame = new IdeaFrame((IdeFrameImpl) frame);
            GlobalEnvironment.INSTANCE.onFrameOpened(ideaFrame);
            if (((IdeFrameImpl) frame).isFocused())
                GlobalEnvironment.INSTANCE.setSelectedFrame(ideaFrame);
        }

        if (GlobalEnvironment.INSTANCE.getSelectedFrame() == null && windowManager.getAllFrames().length > 0)
            GlobalEnvironment.INSTANCE.setSelectedFrame(new IdeaFrame((IdeFrameImpl) WindowManager.getInstance().getAllFrames()[0]));

        new IdeaMiniBuffer(0, null, environment, null);
        UIUtil.invokeLaterIfNeeded(new Runnable() {
            @Override
            public void run() {
                ApplicationManager.getApplication().runReadAction(new Runnable() {
                    @Override
                    public void run() {
                        final FileEditorManager fileEditorManager = FileEditorManager.getInstance(project);
                        for (final VirtualFile virtualFile : fileEditorManager.getOpenFiles()) {
                            ApplicationManager.getApplication().runReadAction(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        new IdeaBuffer(environment, fileEditorManager, virtualFile);
                                    } catch (DoubleBufferException exc) {
                                        //due to event handling order the buffers were already initialized => skip here
                                    }
                                }
                            });
                        }
                        Editor editor = fileEditorManager.getSelectedTextEditor();
                        if (editor != null) {
                            environment.switchToWindow(fileEditorManager.getSelectedTextEditor());
                            return;
                        }
                        if (fileEditorManager.getOpenFiles().length != 0)
                            throw new Attention();
                    }
                });
            }
        });
    }
}
