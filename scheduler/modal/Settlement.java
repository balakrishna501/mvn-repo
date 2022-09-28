package com.currypoint.scheduler.modal;

import com.currypoint.common.enumeration.SettlementStatus;
import com.currypoint.common.enumeration.UserType;
import com.currypoint.common.global.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "SETTLEMENT")
public class Settlement implements Serializable {

    @Id
    @Column(name = "ID")
    private String id;

    @Column(name = "RAZORPAY_ORDER_ID")
    private String razorpayPaymentId;

    @Column(name = "ORDER_ID")
    private String orderId;

    @Column(name = "USER_TYPE")
    @Enumerated(EnumType.STRING)
    private UserType userType;

    @Column(name = "RESTAURANT_ID")
    private String restaurantId;

    @AttributeOverrides({ @AttributeOverride(name = "amount", column = @Column(name = "ORDER_COST")),
            @AttributeOverride(name = "currencyCode", column = @Column(name = "CURRENCY_CODE", updatable = false, insertable = false)) })
    private Money amount;

    @Column(name = "STATUS")
    private SettlementStatus status;

    @Column(name = "PICKUP_DATE")
    private LocalDateTime pickUpDate;

    @Column(name = "RAZORPAY_PAYOUT_ID")
    private String razorpayPayOutId;

    @Column(name = "RAZORPAY_UTR_ID")
    private String razorpayUtrId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getRestaurantId() {
        return restaurantId;
    }

    public void setRestaurantId(String restaurantId) {
        this.restaurantId = restaurantId;
    }

    public Money getAmount() {
        return amount;
    }

    public void setAmount(Money amount) {
        this.amount = amount;
    }

    public SettlementStatus getStatus() {
        return status;
    }

    public void setStatus(SettlementStatus status) {
        this.status = status;
    }

    public LocalDateTime getPickUpDate() {
        return pickUpDate;
    }

    public void setPickUpDate(LocalDateTime pickUpDate) {
        this.pickUpDate = pickUpDate;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getRazorpayPayOutId() {
        return razorpayPayOutId;
    }

    public void setRazorpayPayOutId(String razorpayPayOutId) {
        this.razorpayPayOutId = razorpayPayOutId;
    }

    public String getRazorpayUtrId() {
        return razorpayUtrId;
    }

    public void setRazorpayUtrId(String razorpayUtrId) {
        this.razorpayUtrId = razorpayUtrId;
    }
}
