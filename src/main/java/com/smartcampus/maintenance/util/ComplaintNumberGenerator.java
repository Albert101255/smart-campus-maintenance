package com.smartcampus.maintenance.util;

import java.time.Year;
import java.util.concurrent.atomic.AtomicLong;

public class ComplaintNumberGenerator {

    private static final AtomicLong sequence = new AtomicLong(1);

    public static String generateComplaintNumber(long nextVal) {
        int currentYear = Year.now().getValue();
        return String.format("CMP-%d-%05d", currentYear, nextVal);
    }
}
