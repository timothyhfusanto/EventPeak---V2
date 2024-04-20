/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/SessionLocal.java to edit this template
 */
package session;

import entity.Customer;
import entity.Event;
import entity.EventRegistration;
import error.NoResultException;
import java.util.List;
import javax.ejb.Local;

/**
 *
 * @author timothy
 */
@Local
public interface CustomerSessionLocal {
        
    public Customer getCustomer(Long cId) throws NoResultException;
    
    public void createCustomer(Customer c);
    
    public void deleteCustomer(Long cId) throws NoResultException;
    
    public void updateCustomer(Customer c) throws NoResultException;

    public Customer getCustomerByEmail(String email);
        
    public void createEvent(Long cId, Event e) throws NoResultException;
    
    public List<Event> getCustomerOwnedEvents(Long cId) throws NoResultException;
    
    public List<Event> searchEvents(String title);
    
    public void register(Long cId, Long eId, EventRegistration er) throws NoResultException;
    
    public List<Customer> getEventCustomers(Event e);
    
    public Event getEvent(Long eId) throws NoResultException;
    
    public List<Event> getRegisteredEvents(Customer c);
    
    public void deleteEvent(Long cId, Long eId) throws NoResultException;
    
    public void unregister(Long cId, Long eId, EventRegistration er) throws NoResultException;
    
    public EventRegistration getEventRegistration(Long cId, Long eId);
    
    public List<Customer> getAttendedCustomers(Event e);
    
    public void mark(Long cId, Long eId);
    
    public void unmark(Long cId, Long eId);
    
}
