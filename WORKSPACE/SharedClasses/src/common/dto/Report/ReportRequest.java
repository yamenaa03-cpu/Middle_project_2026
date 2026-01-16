package common.dto.Report;

import java.io.Serializable;

import common.enums.ReportOperation;

public class ReportRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private ReportOperation operation;
    private int year;
    private int month;

    private ReportRequest() {}

    public static ReportRequest createTimeReportRequest(int year, int month) {
        ReportRequest req = new ReportRequest();
        req.operation = ReportOperation.GET_TIME_REPORT;
        req.year = year;
        req.month = month;
        return req;
    }

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
