package org.jetbrains.emacs4ij.jelisp.elisp;

import org.jetbrains.emacs4ij.jelisp.exception.WrongTypeArgumentException;
import org.jetbrains.emacs4ij.jelisp.subroutine.BuiltinPredicates;

/**
 * Created by IntelliJ IDEA.
 * User: Ekaterina.Polishchuk
 * Date: 7/12/11
 * Time: 4:44 PM
 * To change this template use File | Settings | File Templates.
 *
 * elisp integer number = 13, 1355, -7979, etc
 */
public class LispInteger extends LispNumber<Integer> {
    public LispInteger(int data) {
        myData = data;
    }

    @Override
    public String toString() {
        return Integer.toString(myData);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        LispInteger that = (LispInteger) o;

        return myData.equals(that.myData);
    }

    @Override
    public int hashCode() {
        return myData;
    }
    
    public String toCharacterString () {        
        if (!BuiltinPredicates.isCharacter(this))
            throw new WrongTypeArgumentException("characterp", this);
        if (myData < 32) {
//            String s = Integer.toBinaryString(myData);//(Character.toUpperCase(myData + 64));
//            while (s.length() != 6) {
//                s = '0' + s;
//            }
//            s = '1' + s;
//            return Integer.valueOf(s, 2).toString();
            return "^" + (char)Character.toUpperCase(myData + 64);
        }
        if (myData > 127 && myData < 160) {
            return '\\' + Integer.toOctalString(myData);
        }
        return Character.toString((char)(int)myData);
    }

    public void setData (int value) {
        myData = value;
    }

    public int keyToChar () {
        return myData & ((1 << CharUtil.CHARACTERBITS) - 1);
    }
}
