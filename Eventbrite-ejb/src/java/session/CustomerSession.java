/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/J2EE/EJB30/StatelessEjbClass.java to edit this template
 */
package session;

import entity.Customer;
import entity.Event;
import entity.EventRegistration;
import error.NoResultException;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

/**
 *
 * @author timothy
 */
@Stateless
public class CustomerSession implements CustomerSessionLocal {

    @PersistenceContext
    private EntityManager em;
   
    
    @Override
    public void register(Long cId, Long eId, EventRegistration er) throws NoResultException{
        try {
            Customer cust = getCustomer(cId);
            Event event = getEvent(eId);
            
            er.setCustomer(cust);
            er.setEvent(event);
            em.persist(er);
            
            cust.getEventRegistrations().add(er);
            event.getEventRegistrations().add(er);
        } catch (NoResultException e) {
            throw new NoResultException("Not found");
        }
    }
    
    @Override
    public void unregister(Long cId, Long eId, EventRegistration er) throws NoResultException {
        try {
            Customer cust = getCustomer(cId);
            Event event = getEvent(eId);
            Query q = em.createQuery("SELECT er FROM EventRegistration er WHERE er.customer = :customer AND er.event = :event");
            q.setParameter("customer", cust);
            q.setParameter("event", event);
            
            EventRegistration eventRegis = (EventRegistration) q.getSingleResult();
            
            if (eventRegis != null) {
                cust.getEventRegistrations().remove(eventRegis);
                event.getEventRegistrations().remove(eventRegis);
                em.remove(eventRegis);
            }
         } catch (NoResultException error) {
            throw new NoResultException ("Not found");
        }
    }
    
    @Override 
    public EventRegistration getEventRegistration(Long cId, Long eId) {
        Customer cust = em.find(Customer.class, cId);
        Event event = em.find(Event.class, eId);
        Query q = em.createQuery("SELECT er FROM EventRegistration er WHERE er.customer = :customer AND er.event = :event");
        q.setParameter("customer", cust);
        q.setParameter("event", event);

        List<EventRegistration> results = q.getResultList();
        if (results.isEmpty()) {
            return null; 
        } else {
            return results.get(0); 
        }
    }
    
    @Override 
    public void mark(Long cId, Long eId) {
        EventRegistration er = getEventRegistration(cId, eId);
        
        er.setAttended(true);
    }
    
    @Override 
    public void unmark(Long cId, Long eId) {
        EventRegistration er = getEventRegistration(cId, eId);
        
        er.setAttended(false);
    }
    
    @Override
    public List<Customer> getAttendedCustomers(Event e) {
        Query q = em.createQuery("SELECT er.customer FROM EventRegistration er WHERE er.event = :event AND er.attended = true");
        q.setParameter("event", e);
        List<Customer> customers = q.getResultList();
        if (!customers.isEmpty()) {
            return customers;
        } else {
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Event> getRegisteredEvents(Customer c) {
        Query q = em.createQuery("SELECT er.event FROM EventRegistration er WHERE er.customer = :customer");
        q.setParameter("customer", c);
        List<Event> events = q.getResultList();
        if (!events.isEmpty()) {
            return events;
        } else {
            return new ArrayList<>();
        }
    }
    
    @Override
    public List<Customer> getEventCustomers(Event e) {
        Query q = em.createQuery("SELECT er.customer FROM EventRegistration er WHERE er.event = :event AND er.attended = false");
        q.setParameter("event", e);
        List<Customer> customers = q.getResultList();
        if (!customers.isEmpty()) {
            return customers;
        } else {
            return new ArrayList<>();
        }
    }
    
    @Override
    public Event getEvent(Long eId) throws NoResultException {
        Event event = em.find(Event.class, eId);
        if (event != null) {
            return event;
        } else {
            throw new NoResultException("Not found");
        }
    }
    
    @Override
    public void deleteEvent(Long cId, Long eId) throws NoResultException {
        Customer c = getCustomer(cId);
        Event e = em.find(Event.class, eId);
        
        if (e!=null) {
            c.getOwnedEvents().remove(e);
            em.remove(e);
        } else {
            throw new NoResultException("Not found");
        }
    }
    
    @Override
    public List<Event> searchEvents(String title) {
        Query q;
        if (title != null) {
            q = em.createQuery("SELECT e FROM Event e WHERE "
                    + "LOWER(e.title) LIKE :title");
            q.setParameter("title", "%" + title.toLowerCase() + "%");
        } else {
            q = em.createQuery("SELECT e FROM Event e");
        }
        return q.getResultList();
    }

    @Override
    public Customer getCustomer(Long cId) throws NoResultException {
        Customer cust = em.find(Customer.class, cId);

        if (cust != null) {
            return cust;
        } else {
            throw new NoResultException("Not found");
        }
    }
    
    @Override
    public List<Event> getCustomerOwnedEvents(Long cId) throws NoResultException {
        Customer cust = getCustomer(cId);
        
        Query q = em.createQuery("SELECT e FROM Event e WHERE e.creator = :customer");
        q.setParameter("customer", cust);
        
        List<Event> events = q.getResultList();
        if (!events.isEmpty()) {
            return q.getResultList();
        } else {
            throw new NoResultException ("Not found");
        }
    }
    
    @Override
    public void createEvent(Long cId, Event e) throws NoResultException {
        Customer cust = getCustomer(cId);
        e.setTitle(e.getTitle().trim());
        e.setEventDate(e.getEventDate());
        e.setLocation(e.getLocation());
        e.setDescription(e.getDescription());
        e.setDeadline(e.getDeadline());
        e.setCreator(cust);

        Query q = em.createQuery("SELECT e FROM Event e WHERE LOWER(e.title) = :title AND e.eventDate = :eventDate"
                + " AND LOWER(e.location) = :location AND LOWER(e.description) = :description AND e.deadline = :deadline");
        q.setParameter("title", e.getTitle().toLowerCase());
        q.setParameter("eventDate", e.getEventDate());
        q.setParameter("location", e.getLocation().toLowerCase());
        q.setParameter("description", e.getDescription().toLowerCase());
        q.setParameter("deadline", e.getDeadline());

        try {
            Event found = (Event) q.getSingleResult();
            e = found;
        } catch (Exception err) {
            em.persist(e);
        }

        if (!cust.getOwnedEvents().contains(e)) {
            cust.getOwnedEvents().add(e);
        }
    }
    
    @Override
    public Customer getCustomerByEmail(String email) {
        try {
            Query q = em.createQuery("SELECT c FROM Customer c WHERE c.email = :em");
            q.setParameter("em", email);
            Customer c = (Customer) q.getSingleResult();
            return c;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void createCustomer(Customer c) {
        em.persist(c);
    } 
    
    @Override
    public void deleteCustomer(Long cId) throws NoResultException {
        Customer cust = getCustomer(cId);
        em.remove(cust);
    }
    
    @Override
    public void updateCustomer(Customer c) throws NoResultException {
        Customer oldC = getCustomer(c.getCustomerId());
        
        oldC.setName(c.getName());
        oldC.setEmail(c.getEmail());
        oldC.setPhone(c.getPhone());
        oldC.setPassword(c.getPassword());
        oldC.setImage(c.getImage());
        oldC.setGender(c.getGender());
        oldC.setDob(c.getDob());
    }
}
