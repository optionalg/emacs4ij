package org.jetbrains.emacs4ij.jelisp.interactive;

import com.intellij.openapi.util.text.StringUtil;
import org.apache.commons.lang.NotImplementedException;
import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.JelispBundle;
import org.jetbrains.emacs4ij.jelisp.TestMode;
import org.jetbrains.emacs4ij.jelisp.elisp.LambdaOrSymbolWithFunction;
import org.jetbrains.emacs4ij.jelisp.elisp.LispInteger;
import org.jetbrains.emacs4ij.jelisp.elisp.LispObject;
import org.jetbrains.emacs4ij.jelisp.elisp.LispString;
import org.jetbrains.emacs4ij.jelisp.elisp.LispSymbol;
import org.jetbrains.emacs4ij.jelisp.exception.InternalException;
import org.jetbrains.emacs4ij.jelisp.platformDependent.LispBuffer;
import org.jetbrains.emacs4ij.jelisp.platformDependent.LispMinibuffer;
import org.jetbrains.emacs4ij.jelisp.subroutine.Core;

import java.util.List;

public final class SpecialFormInteractive extends InteractiveReader {
  private String[] myParameters;
  private int myArgsLength = 0;
  private int myIndex;
  private final StandardCompletionContext myCompletionContext = new StandardCompletionContext();

  //for tests
  public SpecialFormInteractive(Environment environment, String interactive) {
    super(environment, null);
    init(interactive);
  }

  public SpecialFormInteractive(Environment environment, LambdaOrSymbolWithFunction function, String interactive) {
    super(environment, function);
    init(interactive);
  }

  private void init (String interactive) {
    myParameters = StringUtil.isEmpty(interactive) ? new String[0] : interactive.split("\\\\n");
    for (String p: myParameters) {
      if ("r".equals(p)) {
        myArgsLength += 2;
      } else {
        myArgsLength++;
      }
    }
    myIndex = 0;
    try {
      myCompletionContext.setInteractiveChar(myParameters[0].charAt(0));
      myPrompt = myParameters[0].substring(1);
    } catch (IndexOutOfBoundsException e) {
      myCompletionContext.setInteractiveChar((char)0);
      myPrompt = ourEmptyMessage;
    }
  }

  @Override
  public boolean isFinished () {
    return myIndex == myArgsLength;
  }

  @Override
  public boolean isNoMatch() {
    return !myCompletionContext.isMatch();
  }

  @Override
  public String getNoMatchMessage() {
    return myCompletionContext.getNoMatchMessage();
  }

  @Override
  public boolean toShowSpecialNoMatchMessage() {
    return myCompletionContext.toShowSpecialNoMatchMessage();
  }

  @Override
  public void readNextArgument() {
    if (isFinished())
      return;
    if (myParameters == null)
      throw new InternalException();
    String command = myParameters[myIndex];
    myInitialInput = null;
    myPrompt = command.substring(1);
    myPromptDefaultValue = ourEmptyMessage;

    myCompletionContext.setInteractiveChar(command.charAt(0));

//        myInteractiveChar = command.charAt(0);
    myParameterDefaultValue = null;

    myCompletionContext.setMatch(true);
//        myCompletionContext.setInteractiveChar(myInteractiveChar);
    prepare();
  }

  private void addArg (LispObject arg) {
    if (isFinished()) {
      throw new InternalException("Entered more arguments than specified in interactive string!");
//            myArguments.set(myArguments.size() - 1, arg);
//            return;
    }
    myArguments.add(arg);
    ++myIndex;
  }

  @Override
  public List<String> getCompletions(String parameter) {
    return myCompletionContext.getCompletions(parameter);
  }

  @Override
  public void setNoMatch (String parameter) {
    myInitialInput = parameter;
    myCompletionContext.setMatch(false);
    putArgument();
  }

  @Override
  public void onReadParameter (String parameter) {
    LispObject arg = myCompletionContext.verify(myEnvironment, parameter, myParameterDefaultValue);
    if (myCompletionContext.isMatch()) {
      addArg(arg == null
          ? parameter.equals("") ? new LispString(myParameterDefaultValue)
          : new LispString(parameter)
          : arg);
      return;
    }
    myInitialInput = parameter;
    myCompletionContext.setMatch(true);
    putArgument();
  }

  private String defaultDirectory()  {
    return ((LispString) myEnvironment.getBufferCurrentForEditing().getVariableValue("default-directory")).getData();
  }

  private void prepare () {
    LispSymbol currentPrefixArg = myEnvironment.find("current-prefix-arg");

    switch (myCompletionContext.getInteractiveChar()) {
      case 'b': // -- Name of existing buffer.
        myParameterDefaultValue = myEnvironment.getBufferCurrentForEditing().getName();
        myPromptDefaultValue = " (default " + myParameterDefaultValue + "): ";
        break;
      case 'B': // -- Name of buffer, possibly nonexistent.
        myParameterDefaultValue = myEnvironment.getBufferCurrentForEditing().getName();
        myPromptDefaultValue = " (default " + myParameterDefaultValue + "): ";
        break;
      case 'c':
        myEnvironment.getMinibuffer().setCharListener();
        break;
      case 'd': // -- Value of point as number. Does not do I/O.
        addArg(new LispInteger(myEnvironment.getBufferCurrentForEditing().point()));
        notifyMiniBuffer();
        return;
      case 'D': // -- Directory name.
        myInitialInput = defaultDirectory();
        break;
      case 'e': // -- Parametrized event (i.e., one that's a list) that invoked this command.
        // If used more than once, the Nth `e' returns the Nth parametrized event.
        // This skips events that are integers or symbols.
        //if no event: (error "command must be bound to an event with parameters")
        //todo: notifyMiniBuffer(); return;
        throw new NotImplementedException("e character not implemented");
      case 'f': // -- Existing file name.
        myInitialInput = defaultDirectory();
        break;
      case 'F': // -- Possibly nonexistent file name. -- no check
        myInitialInput = defaultDirectory();
        break;
      case 'G': // -- Possibly nonexistent file name, defaulting to just directory name.
        myInitialInput = defaultDirectory();
        break;
      case 'i': // -- Ignored, i.e. always nil. Does not do I/O.
        addArg(LispSymbol.NIL);
        notifyMiniBuffer();
        return;
      case 'k': // -- Key sequence (downcase the last event if needed to get a definition).
        throw new NotImplementedException("k character not implemented");
      case 'K': // -- Key sequence to be redefined (do not downcase the last event).
        throw new NotImplementedException("K character not implemented");
      case 'm': // -- Value of mark as number. Does not do I/O.
        addArg(new LispInteger(getMarkPosition(JelispBundle.message("no.mark"))));
        notifyMiniBuffer();
        return;
      case 'N': // -- Numeric prefix arg, or if none, do like code `n'.
        if (currentPrefixArg == null) {
          myCompletionContext.setInteractiveChar('n');
          break;
        }
        addArg(Core.prefixNumericValue(currentPrefixArg.getValue()));
        notifyMiniBuffer();
        return;
      case 'p': // -- Prefix arg converted to number. Does not do I/O.
        addArg(Core.prefixNumericValue(currentPrefixArg == null ? null : currentPrefixArg.getValue()));
        notifyMiniBuffer();
        return;
      case 'P': // -- Prefix arg in raw form. Does not do I/O.
        addArg(currentPrefixArg.getValue());
        notifyMiniBuffer();
        return;
      case 'r': // -- Region: point and mark as 2 numeric args, smallest first.
        int point = myEnvironment.getBufferCurrentForEditing().point();
        int mark = getMarkPosition(JelispBundle.message("no.mark.no.region"));
        int min = point < mark ? point : mark;
        int max = point < mark ? mark : point;
        addArg(new LispInteger(min));
        addArg(new LispInteger(max));
        notifyMiniBuffer();
        return;
      case 'S': // -- Any symbol.
        //todo: when read a symbol, set <SPACE> to work as <RET>
        break;
      case 'U':
        //todo: get the up-event that was discarded (if any) after ‘k’ or ‘K’ read a down-event. Otherwise nil
        addArg(LispSymbol.NIL);
        notifyMiniBuffer();
        return;
      case 'x': // -- Lisp expression read but not evaluated.
        //todo: try to parse and show [parse error message] while type
        break;
      case 'X': // -- Lisp expression read and evaluated.
        //todo: try to parse and show [parse error message] while type
        break;
    }
    normalizePromptAndDefault();
    putArgument();
  }

  private int getMarkPosition(String errorMsg) {
    LispBuffer currentBuffer = myEnvironment.getBufferCurrentForEditing();
    if (currentBuffer instanceof LispMinibuffer && !TestMode.TEST) {
      throw new IllegalStateException("Current buffer is minibuffer");
    }
    Integer markPosition = currentBuffer.getMark().getPosition();
    if (markPosition == null) {
      Core.error(errorMsg);
      throw new IllegalStateException(); //unreachable statement, because Core.error(...) throws LispException
    }
    return markPosition;
  }
}
