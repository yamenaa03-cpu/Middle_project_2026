package common.enums;

import java.io.Serializable;

/**
 * Enumeration of report types available in the reporting system.
 * <p>
 * Each operation type indicates which report the client is requesting
 * from the server. Reports are generated on a monthly basis.
 * </p>
 *
 * @author Yamen Abu Ahmad
 * @version 1.0
 */
public enum ReportOperation implements Serializable {
    /**
     * Request for a time-based report showing reservation timing details.
     * Includes scheduled times, actual arrival times, and session durations.
     */
    GET_TIME_REPORT,

    /**
     * Request for a subscriber-based report showing aggregated statistics.
     * Includes total reservations, completions, cancellations per subscriber.
     */
    GET_SUBSCRIBER_REPORT
}