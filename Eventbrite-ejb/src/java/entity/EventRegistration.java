/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 *
 * @author timothy
 */
@Entity
public class EventRegistration implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventRegistrationId;
    
    @ManyToOne
    @JoinColumn(name = "event_id")
    private Event event;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;

    private boolean attended = false;

    public Long getEventRegistrationId() {
        return eventRegistrationId;
    }

    public void setEventRegistrationId(Long eventRegistrationId) {
        this.eventRegistrationId = eventRegistrationId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (eventRegistrationId != null ? eventRegistrationId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the eventRegistrationId fields are not set
        if (!(object instanceof EventRegistration)) {
            return false;
        }
        EventRegistration other = (EventRegistration) object;
        if ((this.eventRegistrationId == null && other.eventRegistrationId != null) || (this.eventRegistrationId != null && !this.eventRegistrationId.equals(other.eventRegistrationId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.EventRegistration[ id=" + eventRegistrationId + " ]";
    }

    /**
     * @return the event
     */
    public Event getEvent() {
        return event;
    }

    /**
     * @param event the event to set
     */
    public void setEvent(Event event) {
        this.event = event;
    }

    /**
     * @return the customer
     */
    public Customer getCustomer() {
        return customer;
    }

    /**
     * @param customer the customer to set
     */
    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    /**
     * @return the attended
     */
    public boolean isAttended() {
        return attended;
    }

    /**
     * @param attended the attended to set
     */
    public void setAttended(boolean attended) {
        this.attended = attended;
    }
    
}
