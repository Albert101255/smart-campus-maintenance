package com.smartcampus.maintenance.util;

import com.smartcampus.maintenance.entity.Complaint;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CsvExportUtil {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public static void writeComplaintsToCsv(PrintWriter writer, List<Complaint> complaints) {
        // Write CSV Header
        writer.println("Complaint ID,Title,Category,Building,Room,Student Name,Student Reg No,Priority,Status,Assigned Staff,Created Date,Resolved Date");

        for (Complaint c : complaints) {
            String staffName = c.getAssignedStaff() != null ? c.getAssignedStaff().getName() : "Unassigned";
            String resolvedDate = c.getResolvedAt() != null ? c.getResolvedAt().format(FORMATTER) : "N/A";
            String createdDate = c.getCreatedAt() != null ? c.getCreatedAt().format(FORMATTER) : "";

            writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                    escapeCsv(c.getComplaintNumber()),
                    escapeCsv(c.getTitle()),
                    escapeCsv(c.getCategory() != null ? c.getCategory().getName() : ""),
                    escapeCsv(c.getBuilding() != null ? c.getBuilding().getName() : ""),
                    escapeCsv(c.getRoomNumber()),
                    escapeCsv(c.getStudent() != null ? c.getStudent().getName() : ""),
                    escapeCsv(c.getStudent() != null ? c.getStudent().getRegisterNumber() : ""),
                    c.getPriority().name(),
                    c.getStatus().name(),
                    escapeCsv(staffName),
                    createdDate,
                    resolvedDate
            );
        }
    }

    private static String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }
}
