package com.seattle.msready.transaction.compensating.support.entity;

public enum SequenceEnum {
    S_UID {
        public String value() {
            return "S_UID";
        }
    },
    S_SHORT_UID {
        public String value() {
            return "S_SHORT_UID";
        }
    },
    S_TX_ID {
        public String value() {
            return "S_SHORT_UID";
        }
    },
    S_REV_ID {
        public String value() {
            return "S_SHORT_UID";
        }
    };

    public String value() {
        return "";
    }
}