package enums;

import java.time.Month;

public enum MonthCode {

    A(Month.JANUARY),
    B(Month.FEBRUARY),
    C(Month.MARCH),
    D(Month.APRIL),
    E(Month.MAY),
    H(Month.JUNE),
    L(Month.JULY),
    M(Month.AUGUST),
    P(Month.SEPTEMBER),
    R(Month.OCTOBER),
    S(Month.NOVEMBER),
    T(Month.DECEMBER);

    private final Month month;

    MonthCode(Month month) {
        this.month = month;
    }

    public Month getMonth() {
        return month;
    }

}
