package com.infrasight.kodtest.api.client;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Handles API pagination logic based on the "Content-Range" header.
 */
public class PaginationHandler {
    private static final String PAGINATION_HEADER_PATTERN = "items (\\d+)-(\\d+)/(\\d+)";

    /**
     * Determines if the response indicates no more paginated data.
     *
     * @param contentRange The "Content-Range" header value.
     * @return {@code true} if there are no more results to fetch.
     */
    public boolean isLastPage(String contentRange) {
        return contentRange == null || contentRange.startsWith("items */");
    }

    /**
     * Extracts the total number of available items from the "Content-Range" header.
     *
     * @param contentRange The "Content-Range" header value.
     * @return The total number of items available in the API, or {@code Integer.MAX_VALUE} if unavailable.
     */
    public int extractTotalItems(String contentRange) {
        if (contentRange == null) return Integer.MAX_VALUE;

        Matcher matcher = Pattern.compile(PAGINATION_HEADER_PATTERN).matcher(contentRange);
        return matcher.matches() ? Integer.parseInt(matcher.group(3)) : Integer.MAX_VALUE;
    }

    /**
     * Extracts the next skip value based on the last returned item index.
     *
     * @param contentRange The "Content-Range" header value.
     * @return The next "skip" value for pagination, or {@code Integer.MAX_VALUE} if the header is invalid.
     */
    public int extractNextSkip(String contentRange) {
        if (contentRange == null) return Integer.MAX_VALUE;

        Matcher matcher = Pattern.compile(PAGINATION_HEADER_PATTERN).matcher(contentRange);
        return matcher.matches() ? Integer.parseInt(matcher.group(2)) + 1 : Integer.MAX_VALUE;
    }
}
