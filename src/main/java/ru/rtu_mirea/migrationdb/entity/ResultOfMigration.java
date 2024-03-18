package ru.rtu_mirea.migrationdb.entity;

public class ResultOfMigration {
    public boolean status;
    public String message;

    public ResultOfMigration() {
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
