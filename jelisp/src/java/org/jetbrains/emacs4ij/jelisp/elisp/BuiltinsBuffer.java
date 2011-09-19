package org.jetbrains.emacs4ij.jelisp.elisp;

import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.exception.WrongTypeArgument;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 8/3/11
 * Time: 5:30 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class BuiltinsBuffer {
    private BuiltinsBuffer() {}

    @Subroutine(value = "current-buffer", max = 0)
    public static LispBuffer getCurrentBuffer(Environment environment, List<LObject> args) {
        return environment.getCurrentBuffer();
    }

    @Subroutine(value = "buffer-size", max = 1)
    public static LispObject bufferSize(Environment environment, List<LObject> args) {
        if (args.size() == 1 && !(args.get(0) instanceof LispBuffer) && !(args.get(0).equals(LispSymbol.ourNil)))
            throw new WrongTypeArgument("LispBuffer", args.get(0).getClass().toString());
        LispBuffer buffer = (args.size() == 0 || args.get(0).equals(LispSymbol.ourNil)) ? environment.getCurrentBuffer() : (LispBuffer) args.get(0);
        if (buffer.equals(LispSymbol.ourNil))
            throw new RuntimeException("no buffer is currently opened");
        return new LispInteger(buffer.getSize());
    }

    @Subroutine(value = "buffer-name", max = 1)
    public static LispObject bufferName (Environment environment, List<LObject> args) {
        if (args.size() == 1 && !(args.get(0) instanceof LispBuffer))
            throw new WrongTypeArgument("LispBuffer", args.get(0).getClass().toString());
        LispBuffer buffer = (args.size() == 0 || args.get(0).equals(LispSymbol.ourNil)) ? environment.getCurrentBuffer() : (LispBuffer) args.get(0);
        return new LispString(buffer.getName());
    }

    /* (get-buffer BUFFER-OR-NAME)
    Return the buffer named BUFFER-OR-NAME.
    BUFFER-OR-NAME must be either a string or a buffer.
    If BUFFER-OR-NAME is a string and there is no buffer with that name, return nil.
    If BUFFER-OR-NAME is a buffer, return it as given.
    */
    @Subroutine(value = "get-buffer", exact = 1)
    public static LObject getBuffer (Environment environment, List<LObject> args) {
        LObject arg = args.get(0);
        if (!(arg instanceof LispString) && !(arg instanceof LispBuffer))
            throw new WrongTypeArgument("LispString or LispBuffer", arg.getClass().toString());

        return (arg instanceof LispString) ? environment.getBuffer(args.get(0).toString()) : arg;
    }

    @Subroutine(value = "other-buffer", max = 1)
    public static LispBuffer otherBuffer (Environment environment, List<LObject> args) {
        if (args.size() == 1 && !(args.get(0) instanceof LispBuffer))
            throw new WrongTypeArgument("LispBuffer", args.get(0).getClass().toString());
        if (args.size() == 0)
            return environment.getOtherBuffer();
        return environment.getOtherBuffer(((LispBuffer)args.get(0)).getName());
    }

    /*
    (set-buffer BUFFER-OR-NAME)
    Make buffer BUFFER-OR-NAME current for editing operations.
    BUFFER-OR-NAME may be a buffer or the name of an existing buffer.
    See also `save-excursion' when you want to make a buffer current temporarily.
    This function does not display the buffer, so its effect ends when the current command terminates.
    Use `switch-to-buffer' or `pop-to-buffer' to switch buffers permanently.
     */
    @Subroutine(value = "set-buffer", exact = 1)
    public static LObject setBuffer (Environment environment, List<LObject> args) {
        return getBuffer(environment, args);
    }

    /*
    switch-to-buffer is an interactive built-in function in `C source code'.

    It is bound to C-x b, <menu-bar> <buffer> <select-named-buffer>.

    (switch-to-buffer BUFFER-OR-NAME &optional NORECORD)

    Make BUFFER-OR-NAME current and display it in selected window.
    BUFFER-OR-NAME may be a buffer, a string (a buffer name), or
    nil.  Return the buffer switched to.

    If BUFFER-OR-NAME is a string and does not identify an existing
    buffer, create a new buffer with that name.  Interactively, if
    `confirm-nonexistent-file-or-buffer' is non-nil, request
    confirmation before creating a new buffer.  If BUFFER-OR-NAME is
    nil, switch to buffer returned by `other-buffer'.

    Optional second arg NORECORD non-nil means do not put this buffer
    at the front of the list of recently selected ones.  This
    function returns the buffer it switched to as a Lisp object.

    If the selected window is the minibuffer window or dedicated to
    its buffer, use `pop-to-buffer' for displaying the buffer.

    WARNING: This is NOT the way to work on another buffer temporarily
    within a Lisp program!  Use `set-buffer' instead.  That avoids
    messing with the window-buffer correspondences.
     */
    //todo: interactive
    @Subroutine(value = "switch-to-buffer", min = 1, max = 2)
    public static LObject switchToBuffer (Environment environment, List<LObject> args) {
        //TODO: make it ok
        /*
        LObject b = args.get(0);
        if (b.equals(LispSymbol.ourNil)) {
            return environment.getOtherBuffer();    
        }
        if (b instanceof LispString) {
            LObject buf = environment.getBuffer(((LispString)b).toString());
            if (buf.equals(LispSymbol.ourNil)) {
                // todo: create a new buffer with that name.  Interactively, if
                   // `confirm-nonexistent-file-or-buffer' is non-nil, request
                   // confirmation before creating a new buffer


            }

            
        }   */


        if (args.size() == 1 && !(args.get(0) instanceof LispBuffer))
            throw new WrongTypeArgument("LispBuffer", args.get(0).getClass().toString());
        return null;
    }

    @Subroutine(value = "point", max = 0)
    public static LObject point (Environment environment, List<LObject> args) {
        return new LispInteger(environment.getCurrentBuffer().point());
    }

    @Subroutine(value = "point-min", max = 0)
    public static LObject pointMin (Environment environment, List<LObject> args) {
        return new LispInteger(environment.getCurrentBuffer().pointMin());
    }

    @Subroutine(value = "point-max", max = 0)
    public static LObject pointMax (Environment environment, List<LObject> args) {
        return new LispInteger(environment.getCurrentBuffer().pointMax());
    }

    @Subroutine(value = "buffer-end", exact = 1)
    public static LObject bufferEnd (Environment environment, List<LObject> args) {
        if (!(args.get(0) instanceof LispNumber))
            throw new WrongTypeArgument("LispNumber (LispInteger, LispFloat)", args.get(0).getClass().toString());
        double p = (args.get(0) instanceof LispInteger) ? (double) ((LispInteger)args.get(0)).getData() : ((LispFloat)args.get(0)).getData();
        return new LispInteger(environment.getCurrentBuffer().bufferEnd(p));
    }

    //todo: interactive, accepts integer OR MARKER
    @Subroutine(value = "goto-char", exact = 1)
    public static LObject gotoChar (Environment environment, List<LObject> args) {
        if (!(args.get(0) instanceof LispInteger))
            throw new WrongTypeArgument("LispInteger", args.get(0).getClass().toString());
        environment.getCurrentBuffer().gotoChar(((LispInteger)args.get(0)).getData());
        return args.get(0);
    }

    //todo: interactive, bound to C-f, <right>
    @Subroutine(value = "forward-char", max = 1)
    public static LObject forwardChar (Environment environment, List<LObject> args) {
        int shift = 1;
        if (!args.isEmpty()) {
            if (!(args.get(0) instanceof LispInteger))
                throw new WrongTypeArgument("LispInteger", args.get(0).getClass().toString());
            shift = ((LispInteger)args.get(0)).getData();
        }
        String message = environment.getCurrentBuffer().forwardChar(shift);
        if (message.equals(""))
            return LispSymbol.ourNil;
        return new LispSymbol(message);
    }

    //todo: interactive, bound to C-b, <left>
    @Subroutine(value = "backward-char", max = 1)
    public static LObject backwardChar (Environment environment, List<LObject> args) {
        int shift = 1;
        if (!args.isEmpty()) {
            if (!(args.get(0) instanceof LispInteger))
                throw new WrongTypeArgument("LispInteger", args.get(0).getClass().toString());
            shift = ((LispInteger)args.get(0)).getData();
        }
        String message = environment.getCurrentBuffer().forwardChar(-shift);
        if (message.equals(""))
            return LispSymbol.ourNil;
        return new LispSymbol(message);
    }

}