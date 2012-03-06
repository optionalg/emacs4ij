package org.jetbrains.emacs4ij.jelisp.subroutine;

import org.jetbrains.emacs4ij.jelisp.elisp.*;
import org.jetbrains.emacs4ij.jelisp.exception.LispException;
import org.jetbrains.emacs4ij.jelisp.exception.WrongTypeArgumentException;

/**
 * Created by IntelliJ IDEA.
 * User: kate
 * Date: 26/02/12
 * Time: 18:16
 * To change this template use File | Settings | File Templates.
 */
public abstract class BuiltinArithmetic {
    private BuiltinArithmetic() {}

    private static LispNumber numberOrMarkerToNumber (LObject lispObject) {
        if (BuiltinPredicates.numberOrMarkerP(lispObject).equals(LispSymbol.ourNil))
            throw new WrongTypeArgumentException("number-or-marker-p", lispObject.toString());
        LispNumber n;
        if (BuiltinPredicates.markerP(lispObject).equals(LispSymbol.ourT)) {
            LObject pos = ((LispMarker)lispObject).getPosition();
            if (!pos.equals(LispSymbol.ourNil)) {
                n = (LispInteger)pos;
            } else
                throw new LispException("'(error \"Marker does not point anywhere\")");
        } else {
            n = (LispNumber) lispObject;
        }
        return n;
    }

    private static LispNumber fromDouble (boolean isDouble, double d) {
        return isDouble ? new LispFloat(d) : new LispInteger((int) d);
    }

    @Subroutine("+")
    public static LispNumber plus (@Optional LObject... args) {
        double ans = 0.0;
        boolean isDouble = false;
        if (args != null) {
            for (LObject lispObject: args) {
                LispNumber n = numberOrMarkerToNumber(lispObject);
                if (!isDouble && (n.getData() instanceof Double))
                    isDouble = true;
                ans += n.getDoubleData();
            }
        }
        return fromDouble(isDouble, ans);
    }

    @Subroutine("-")
    public static LispNumber minus (@Optional LObject num, @Optional LObject... rest) {
        if (num == null)
            return new LispInteger(0);
        if (rest == null || rest.length == 0) {
            LispNumber n = numberOrMarkerToNumber(num);
            double ans = -n.getDoubleData();
            return fromDouble(n.getData() instanceof Double, ans);
        }
        LispNumber n = numberOrMarkerToNumber(num);
        double ans = n.getDoubleData();
        boolean isDouble = n.getData() instanceof Double;
        for (LObject lispObject: rest) {
            LispNumber k = numberOrMarkerToNumber(lispObject);
            if (!isDouble && (k.getData() instanceof Double))
                isDouble = true;
            ans -= k.getDoubleData();
        }
        return fromDouble(isDouble, ans);
    }

    @Subroutine("*")
    public static LispNumber multiply (@Optional LObject... args) {
        double ans = 1;
        boolean isDouble = false;
        for (LObject lispObject: args) {
            LispNumber n = numberOrMarkerToNumber(lispObject);
            if (!isDouble && (n.getData() instanceof Double))
                isDouble = true;
            ans *= n.getDoubleData();
        }
        return fromDouble(isDouble, ans);
    }

    @Subroutine(">")
    public static LispSymbol more (LObject num1, LObject num2) {
        LispNumber n1 = numberOrMarkerToNumber(num1);
        LispNumber n2 = numberOrMarkerToNumber(num2);
        return LispSymbol.bool(n1.getDoubleData() > n2.getDoubleData());
    }

    @Subroutine("=")
    public static LispSymbol equalNumbersOrMarkers (LObject num1, LObject num2) {
        double n1 = numberOrMarkerToNumber(num1).getDoubleData();
        double n2 = numberOrMarkerToNumber(num2).getDoubleData();
        return LispSymbol.bool(n1 == n2);
    }

    @Subroutine("/=")
    public static LispSymbol notEqualNumbersOrMarkers (LObject num1, LObject num2) {
        double n1 = numberOrMarkerToNumber(num1).getDoubleData();
        double n2 = numberOrMarkerToNumber(num2).getDoubleData();
        return LispSymbol.bool(n1 != n2);
    }

    @Subroutine("<=")
    public static LispSymbol lessOrEqualNumbersOrMarkers (LObject num1, LObject num2) {
        double n1 = numberOrMarkerToNumber(num1).getDoubleData();
        double n2 = numberOrMarkerToNumber(num2).getDoubleData();
        return LispSymbol.bool(n1 <= n2);
    }

    @Subroutine("<")
    public static LispSymbol less (LObject num1, LObject num2) {
        double n1 = numberOrMarkerToNumber(num1).getDoubleData();
        double n2 = numberOrMarkerToNumber(num2).getDoubleData();
        return LispSymbol.bool(n1 < n2);
    }

    @Subroutine(">=")
    public static LispSymbol moreOrEqual (LObject num1, LObject num2) {
        return LispSymbol.bool(!less(num1, num2).toBoolean());
    }

    @Subroutine("1-")
    public static LispNumber minusOne (LObject num) {
        LispNumber n = numberOrMarkerToNumber(num);
        boolean isDouble = n.getData() instanceof Double;
        return fromDouble(isDouble, n.getDoubleData() - 1);
    }

    @Subroutine("1+")
    public static LispNumber plusOne (LObject num) {
        LispNumber n = numberOrMarkerToNumber(num);
        boolean isDouble = n.getData() instanceof Double;
        return fromDouble(isDouble, n.getDoubleData() + 1);
    }
    
    @Subroutine("logand")
    public static LispInteger logAnd (@Optional LObject... args) {
        if (args == null)
            return new LispInteger(-1);
        int result = -1;
        for (LObject arg: args) {
            LispNumber num = numberOrMarkerToNumber(arg);
            if (!(num instanceof LispInteger))
                throw new WrongTypeArgumentException("integer-or-marker-p", num.toString());
            result = result & ((LispInteger)num).getData();
        }
        return new LispInteger(result);
    }
    
    @Subroutine("lognot")
    public static LispInteger logNot (LispInteger object) {
        return new LispInteger(~object.getData());
    }
}