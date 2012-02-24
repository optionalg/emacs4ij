package org.jetbrains.emacs4ij.jelisp.elisp;

import org.jetbrains.emacs4ij.jelisp.Environment;
import org.jetbrains.emacs4ij.jelisp.subroutine.BuiltinsCore;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 7/11/11
 * Time: 4:16 PM
 * To change this template use File | Settings | File Templates.
 *
 * elisp string = "anything between double quotation marks"
 */
public class LispString extends LispAtom implements LispSequence {
    private String myData;

    public LispString (String data) {
        myData = data.replaceAll("\\\\\"", "\"");
    }

    public String getData() {
        return myData;
    }

    @Override
    public String toString() {
        return '"' + myData + '"';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LispString that = (LispString) o;

        return !(myData != null ? !myData.equals(that.myData) : that.myData != null);

    }

    @Override
    public int hashCode() {
        return myData != null ? myData.hashCode() : 0;
    }

    @Override
    /**
     * no parameters required
     */
    public LObject evaluate(Environment environment) {
        return this;
    }

    @Override
    public int length() {
        return myData.length();
    }

    @Override
    public List<LObject> toLObjectList() {
        ArrayList<LObject> data = new ArrayList<>();
        for (int i = 0; i < myData.length(); ++i) {
            data.add(new LispInteger(myData.charAt(i)));
        }
        return data;
    }

    @Override
    public List<LObject> mapCar(Environment environment, LispSymbol method) {
        ArrayList<LObject> data = new ArrayList<>();
        for (LObject item: toLObjectList()) {
            data.add(BuiltinsCore.functionCall(environment, method, item));
        }
        return data;
    }

    @Override
    public LObject copy() {
        return new LispString(myData);
    }

    @Override
    public String toCharString() {
        return myData;
    }
    
    private boolean isDelimiter(int c) {
        return c < 48 || (c > 57 && c < 65) || (c > 90 && c < 97) || c > 122;
    }
    
    public LispString capitalize() {
        boolean firstAfterDelimiter = true;
        char[] s = myData.toCharArray();
        for (int c = 0; c < s.length; ++c) {            
            if (isDelimiter(s[c])) {
                firstAfterDelimiter = true;
                continue;
            }
            if (firstAfterDelimiter) {
                s[c] = Character.toUpperCase(s[c]);
                firstAfterDelimiter = false;
                continue;
            }            
            s[c] = Character.toLowerCase(s[c]);
        }        
        return new LispString(new String(s));
    }
    
    public int match (LispString regexpStr, int from, boolean isCaseFoldSearch) {
        String data = myData;
        String regexp = regexpStr.getData();
        if (isCaseFoldSearch) {
            data = data.toLowerCase();
            regexp = regexp.toLowerCase();
        }




        return data.indexOf(regexp, from);
    }
}
