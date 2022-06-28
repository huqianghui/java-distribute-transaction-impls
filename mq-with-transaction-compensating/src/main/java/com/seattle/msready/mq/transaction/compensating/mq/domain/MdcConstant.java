package com.seattle.msready.mq.transaction.compensating.mq.domain;

public class MdcConstant {
    public static final String MDC_KEY__USER_NAME = "x-seattle-user-name";
    public static final String MDC_KEY__ORIGINAL_IP_ADDRESS="x-seattle-original-ip-address";
    public static final String MDC_KEY__TRANSACTION_ID = "x-seattle-transaction-id";
    public static final String MDC_KEY__REF_NO ="x-seattle-ref-no";
    public static final String MDC_KEY__ZIPKIN_URL="x-seattle-zipkin-url";
    public static final String MDC_KEY__ZIPKIN_QUEUE_NAME="x-seattle-zipkin-queue";
    public static final String MDC_KEY__REQUEST_URI="x-seattle-request-uri";
    public static final String MDC_KEY__TRACE_ID = "x-seattle-trace-id";
    public static final String MDC_KEY__SLEUTH_TRACE_ID = "X-B3-TraceId";
    public static final String MDC_KEY__TARGET_IP_ADDRESS = "x-seattle-target-ip-address";
    public static final String MDC_KEY_APPLICATION_NAME = "x-seattle-application-name";
}
