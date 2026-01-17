package common.dto.Report;

import java.io.Serializable;

import common.enums.ReportOperation;
/**
 * ReportRequest is a Data Transfer Object (DTO) used to request
 * different types of reports from the server.
 * This class is immutable from outside and can be created
 * only through the provided static factory methods.
 */
public class ReportRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private ReportOperation operation;
    private int year;
    private int month;

    private ReportRequest() {}
    /**
     * Creates a request for a time-based report.
     *
     * @param year  the year of the requested report
     * @param month the month of the requested report
     * @return a ReportRequest configured for a time report
     */
    public static ReportRequest createTimeReportRequest(int year, int month) {
        ReportRequest req = new ReportRequest();
        req.operation = ReportOperation.GET_TIME_REPORT;
        req.year = year;
        req.month = month;
        return req;
    }

    /**
     * Creates a request for a subscriber-based report.
     *
     * @param year  the year of the requested report
     * @param month the month of the requested report
     * @return a ReportRequest configured for a subscriber report
     */
    public static ReportRequest createSubscriberReportRequest(int year, int month) {
        ReportRequest req = new ReportRequest();
        req.operation = ReportOperation.GET_SUBSCRIBER_REPORT;
        req.year = year;
        req.month = month;
        return req;
    }

    public ReportOperation getOperation() { return operation; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
}
