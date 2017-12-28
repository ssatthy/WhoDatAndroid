package com.likethatalsocan.whodat;

import com.google.firebase.database.Exclude;

/**
 * Created by sathy on 27/12/17.
 */

public class Message {

    @Exclude
    private String id;

    private String fromId;
    private String toId;
    private String message;
    private Double timestamp;

    @Exclude
    private Account account;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromId() {
        return fromId;
    }

    public void setFromId(String fromId) {
        this.fromId = fromId;
    }

    public String getToId() {
        return toId;
    }

    public void setToId(String toId) {
        this.toId = toId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Double getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Double timestamp) {
        this.timestamp = timestamp;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message message = (Message) o;

        return fromId != null ? fromId.equals(message.fromId) : message.fromId == null;
    }

    @Override
    public int hashCode() {
        return fromId != null ? fromId.hashCode() : 0;
    }
}
