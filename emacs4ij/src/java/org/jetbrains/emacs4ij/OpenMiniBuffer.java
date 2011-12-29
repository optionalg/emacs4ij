package org.jetbrains.emacs4ij;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.impl.EditorImpl;
import com.intellij.ui.EditorTextField;
import org.jetbrains.emacs4ij.jelisp.CustomEnvironment;
import org.jetbrains.emacs4ij.jelisp.GlobalEnvironment;
import org.jetbrains.emacs4ij.jelisp.elisp.LispBuffer;

import java.awt.event.KeyEvent;

/**
 * Created by IntelliJ IDEA.
 * User: kate
 * Date: 11/3/11
 * Time: 12:14 PM
 * To change this template use File | Settings | File Templates.
 */
public class OpenMiniBuffer extends AnAction {

    public void actionPerformed(AnActionEvent e) {
        Editor editor = PlatformDataKeys.EDITOR.getData(e.getDataContext());
        if (editor == null)
            return;
        if (!Checker.isReady())
            return;

        if (!PlatformDataKeys.PROJECT.getData(e.getDataContext()).getComponent(MyProjectComponent.class).initEnvironment())
            return;

        String myName = GlobalEnvironment.ourMiniBufferName;

        String editorComponentName = editor.getContentComponent().getName();
        if (editorComponentName != null && editorComponentName.equals(myName))
            return;

        CustomEnvironment environment = PlatformDataKeys.PROJECT.getData(e.getDataContext()).getComponent(MyProjectComponent.class).getEnvironment();

        //todo: open new mini buffer over old ones
        IdeaMiniBuffer miniBuffer = (IdeaMiniBuffer) environment.getMiniBuffer();

        EditorTextField input = new EditorTextField();
        editor.setHeaderComponent(input);

        String name;
        try {
            name = ((EditorImpl)editor).getVirtualFile().getName();
        } catch (NullPointerException exc) {
            name = GlobalEnvironment.ourScratchBufferName;
        }

        LispBuffer parent = environment.findBuffer(name);
        parent.setEditor(editor);
        environment.updateBuffer(parent);

        miniBuffer.setEditor(input.getEditor());

        ExecuteCommand command = new ExecuteCommand();
        command.registerCustomShortcutSet(KeyEvent.VK_ENTER, 0, input);
        InterruptMiniBuffer imb = new InterruptMiniBuffer();
        imb.registerCustomShortcutSet(KeyEvent.VK_ESCAPE, 0, input);
        AutoComplete autoComplete = new AutoComplete();
        autoComplete.registerCustomShortcutSet(KeyEvent.VK_TAB, 0, input);

        miniBuffer.setBufferActive();
    }
}
