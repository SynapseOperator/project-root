package com.yuelutraffic.accidents;

import com.yuelutraffic.user.AppUser;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "accident_contact_offers")
public class AccidentContactOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "exchange_request_id")
    private ContactExchangeRequest exchangeRequest;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id")
    private AppUser user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 24)
    private ContactType contactType;

    @Column(nullable = false, length = 500)
    private String encryptedContactValue;

    @Column(nullable = false)
    private Instant createdAt;

    protected AccidentContactOffer() {
    }

    public AccidentContactOffer(ContactExchangeRequest exchangeRequest, AppUser user, ContactType contactType, String encryptedContactValue) {
        this.exchangeRequest = exchangeRequest;
        this.user = user;
        this.contactType = contactType;
        this.encryptedContactValue = encryptedContactValue;
    }

    @PrePersist
    void prePersist() {
        createdAt = Instant.now();
    }

    public AppUser getUser() {
        return user;
    }

    public ContactType getContactType() {
        return contactType;
    }

    public String getEncryptedContactValue() {
        return encryptedContactValue;
    }
}
