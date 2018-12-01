package shop.domain;

import java.util.Date;

public class Instruct {
    private Integer id;

    private Integer modbusAddr;

    private Integer status;

    private String pin;

    private Date created;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getModbusAddr() {
        return modbusAddr;
    }

    public void setModbusAddr(Integer modbusAddr) {
        this.modbusAddr = modbusAddr;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin == null ? null : pin.trim();
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}