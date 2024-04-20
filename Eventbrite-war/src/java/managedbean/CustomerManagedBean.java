/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package managedbean;

import java.util.Base64;
import entity.Customer;
import entity.Event;
import entity.EventRegistration;
import error.NoResultException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.inject.Named;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import javax.ejb.EJB;
import javax.enterprise.context.SessionScoped;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.servlet.http.Part;
import session.CustomerSessionLocal;


/**
 *
 * @author timothy
 */
@Named(value = "customerManagedBean")
@SessionScoped
public class CustomerManagedBean implements Serializable {

    @EJB
    private CustomerSessionLocal customerSessionLocal;
    
    private String name;
    private String phone;
    private String email;
    private String password;
    private Part file;
    private byte gender;
    private Date dob;
    private byte[] image;
    private String newPassword;
        
    private Customer selectedCustomer;
    private Customer currCustomer;
    
    private String title;
    private Date eventDate;
    private String location;
    private String description;
    private Date deadline;
    
    private List<Event> ownedEvents;
    private List<Event> events;
    private String searchString;
    
    private Long eventId;
    private Event selectedEvent;
    
    private List<Customer> selectedEventCustomers;
    private List<Customer> attendedEventCustomers;
    private List<Event> registeredEvents;
    
    private Part eventFile;
    private byte[] eventImage;
        
    public CustomerManagedBean() {
    }
    
    public void initAllEvents() {
        
        if (getSearchString() == null || getSearchString().equals("")) {
            this.events = customerSessionLocal.searchEvents(null);
        } else {
            this.events = customerSessionLocal.searchEvents(getSearchString());
        }
    }
    
    public void markUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            Map<String, String> params = context.getExternalContext()
                   .getRequestParameterMap();
            String cIdStr = params.get("customerId");
            Long cId = Long.parseLong(cIdStr);
            this.selectedCustomer = customerSessionLocal.getCustomer(cId);
            customerSessionLocal.mark(selectedCustomer.getCustomerId(), this.selectedEvent.getEventId());
        } catch (NoResultException error) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not found", null));
        } 
    }
    
    public void unmarkUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            Map<String, String> params = context.getExternalContext()
                   .getRequestParameterMap();
            String cIdStr = params.get("customerId");
            Long cId = Long.parseLong(cIdStr);
            this.selectedCustomer = customerSessionLocal.getCustomer(cId);
            customerSessionLocal.unmark(selectedCustomer.getCustomerId(), this.selectedEvent.getEventId());
        } catch (NoResultException error) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Not found", null));
        }
    }
    
    private Date getCurrentTime() {
        Date currentTime = new Date(); 
        TimeZone timeZone = TimeZone.getTimeZone("Asia/Singapore");
        Calendar calendar = Calendar.getInstance(timeZone);
        calendar.setTime(currentTime);

        return calendar.getTime();
    }
    
    public void register() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            Map<String, String> params = context.getExternalContext()
                    .getRequestParameterMap();
            String eIdStr = params.get("eventId");
            Long eId = Long.parseLong(eIdStr);
            this.selectedEvent = customerSessionLocal.getEvent(eId);
            
            Date currentTimeSingapore = getCurrentTime();
            if (selectedEvent.getCreator().equals(currCustomer)) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to register", "You are the creator of the event"));
            } else if (currentTimeSingapore.after(selectedEvent.getDeadline())) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to register", "Can't register after event deadline"));
            } else if (!customerSessionLocal.getEventCustomers(selectedEvent).contains(currCustomer) && !selectedEvent.getCreator().equals(currCustomer) && currentTimeSingapore.before(selectedEvent.getDeadline())) {
                try {
                    EventRegistration er = new EventRegistration();
                    customerSessionLocal.register(this.currCustomer.getCustomerId(), this.selectedEvent.getEventId() , er);
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Register successfully", "You are registered to " + selectedEvent.getTitle()));
                } catch (NoResultException err) {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to register", "Event not found"));
                }
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Unable to register", "You have been registered to this event"));
            }
        } catch (NoResultException error) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Event not found", null));
        }
    }
    
    public void handleSearch() {
        initAllEvents();
    }
    
    private byte[] loadEventDefaultImage() {
        try {
            Path imagePath = Paths.get("/Users/timothy/Downloads/Mods/Y2S2_23-24/IS3106/Assignments/Assignment2/Eventbrite/Eventbrite-war/web/resources/images/notavailable.png");
            return Files.readAllBytes(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String addEvent() {
        FacesContext context = FacesContext.getCurrentInstance();
        Date time = getCurrentTime();
        if (deadline.after(eventDate)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Deadline input error", "Deadline should not be later than event date"));
            return "/authenticated/createEvent.xhtml";
        } else if (time.after(eventDate)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Event date input error", "Event date should not before current time"));
            return "/authenticated/createEvent.xhtml";
        }
        Event e = new Event();
        e.setTitle(getTitle());
        e.setEventDate(getEventDate());
        e.setDescription(getDescription());
        e.setLocation(getLocation());
        e.setDeadline(getDeadline());
        e.setCreator(this.getCurrCustomer());
        
        byte[] imageBytes = upload(getEventFile()); 
        if (imageBytes != null) {
            e.setImage(imageBytes); 
        } else {
            e.setImage(loadEventDefaultImage());
        }

        try {
            customerSessionLocal.createEvent(this.getCurrCustomer().getCustomerId(), e);
            this.currCustomer = customerSessionLocal.getCustomer(this.currCustomer.getCustomerId());
            System.out.println(currCustomer.getOwnedEvents());
            this.title = null;
            this.eventDate = null;
            this.location = null;
            this.description = null;
            this.deadline = null;
            this.eventFile = null;
            this.eventImage = null;
            initOwnedEvents();
            initAllEvents();
        } catch (NoResultException ex) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No user found", null));
        }
        return "/authenticated/myevent.xhtml?faces-redirect=true";
    }
    
    public void initRegisteredEvents() {
        this.setRegisteredEvents(customerSessionLocal.getRegisteredEvents(currCustomer));
    }
    
    public void initOwnedEvents() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            ownedEvents = customerSessionLocal.getCustomerOwnedEvents(this.currCustomer.getCustomerId());
        } catch (NoResultException er) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No user found", null));
        }
    }
    
    public boolean isRegistered(Event e) {
        EventRegistration er = customerSessionLocal.getEventRegistration(currCustomer.getCustomerId(), e.getEventId());
        return er != null;

    }
    
    public void unregister() {
        FacesContext context = FacesContext.getCurrentInstance();
        try {
            Map<String, String> params = context.getExternalContext()
                    .getRequestParameterMap();
            String eIdStr = params.get("eventId");
            Long eId = Long.parseLong(eIdStr);
            this.selectedEvent = customerSessionLocal.getEvent(eId);
            
            Date currentTimeSingapore = getCurrentTime();
            if (currentTimeSingapore.before(this.selectedEvent.getEventDate())) {
                EventRegistration er = customerSessionLocal.getEventRegistration(currCustomer.getCustomerId(), selectedEvent.getEventId());
                customerSessionLocal.unregister(currCustomer.getCustomerId(), selectedEvent.getEventId(), er);
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Successfully unregistered", "You have been unregistered from " + this.selectedEvent.getTitle()));
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,"Unable to unregister", "You can't unregister, Event has already started"));
            }
        } catch (NoResultException error) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "No event found",null));
        }
    }
    
    public void initRegisterEvent() {
        selectedEventCustomers = customerSessionLocal.getEventCustomers(selectedEvent);
    }
    
    public void initAttendEvent() {
        attendedEventCustomers = customerSessionLocal.getAttendedCustomers(selectedEvent);
    }
    
    public void deleteEvent() {
        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, String> params = context.getExternalContext()
                    .getRequestParameterMap();
        String eIdStr = params.get("eventId");
        Long eId = Long.parseLong(eIdStr);
        try {
            this.selectedEvent = customerSessionLocal.getEvent(eId);
        
            if (selectedEvent.getCreator().equals(this.currCustomer)) {
                customerSessionLocal.deleteEvent(currCustomer.getCustomerId(), selectedEvent.getEventId());
                initCurrCustomer(); 
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Event " + this.selectedEvent.getTitle() + " deleted successfully",null));
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Only creator can delete this event", null));
            }
        } catch (NoResultException er) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Event not found", null));
        }
    }
    
    public byte[] upload(Part newfile) {
        if (newfile != null) {
            try (InputStream input = newfile.getInputStream()) {
                ByteArrayOutputStream output = new ByteArrayOutputStream();

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = input.read(buffer)) != -1) {
                    output.write(buffer, 0, bytesRead);
                }
                return output.toByteArray();
            } catch (IOException e) {
                System.out.println("Error reading input stream: " + e.getMessage());
            }
        }
        
        return null;
    }
    
    public String addCustomer() {
        if (customerSessionLocal.getCustomerByEmail(getEmail()) != null) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email is already used", "The email you entered already exists"));
            return "signUp.xhtml";
        } else {
            Customer c = new Customer();
            c.setName(getName());
            c.setPhone(getPhone());
            c.setEmail(getEmail());
            c.setPassword(getPassword());
            c.setCreated(new Date());
            c.setGender(getGender());
            c.setDob(getDob());
            
            byte[] imageBytes = upload(getFile()); 
            if (imageBytes != null) {
                c.setImage(imageBytes); 
            } else {
                c.setImage(loadDefaultImage());
            }

            customerSessionLocal.createCustomer(c);
            try {
                this.setCurrCustomer(customerSessionLocal.getCustomer(c.getCustomerId()));
                initOwnedEvents();
                initAllEvents();
            } catch (NoResultException e) {
                System.out.println("not found");
            }
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Sign up successfully!", "Welcome to EventPeak."));
            return "/authenticated/landing.xhtml?faces-redirect=true";
        }
    }
    
    public String updateCustomer() {
        FacesContext context = FacesContext.getCurrentInstance();
        currCustomer.setName(name);
        currCustomer.setDob(dob);
        currCustomer.setGender(gender);
        currCustomer.setPhone(phone);
        byte[] imageBytes = upload(getFile()); 
        if (imageBytes != null) {
            currCustomer.setImage(imageBytes); 
        } 
        try {
            customerSessionLocal.updateCustomer(currCustomer);
        } catch (NoResultException e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update customer"));
        }
        context.addMessage(null, new FacesMessage("Success", "Successfully updated customer"));
        return "/authenticated/myprofile.xhtml?faces-redirect=true";
    }
    
    public String updateCustomerPassword() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (currCustomer.getPassword().equals(getPassword()) && getNewPassword() != null) {
            try {    
                currCustomer.setPassword(getNewPassword());
                customerSessionLocal.updateCustomer(currCustomer);
                context.addMessage(null, new FacesMessage("Success", "Successfully updated customer"));
                return "/authenticated/myprofile.xhtml?faces-redirect=true";
            } catch (NoResultException e) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to update customer"));
                return "/authenticated/myprofile.xhtml"; 
            }
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password is incorrect", "Unable to update customer"));
            return "editPassword.xhtml";
        }
    }
    
    public void loadSelectedEvent() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        try {
            this.selectedEvent = customerSessionLocal.getEvent(getEventId());
            initRegisterEvent();
            initAttendEvent();
        } catch (NoResultException err) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to load event"));
        } 
    }
    
    public void initCurrCustomer() {
        FacesContext context = FacesContext.getCurrentInstance();
        
        try {
            this.currCustomer = customerSessionLocal.getCustomer(currCustomer.getCustomerId());
        } catch (NoResultException err) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to load event"));
        } 
    }
    
    public void loadCurrCustomer() {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            name = this.currCustomer.getName();
            gender = this.currCustomer.getGender();
            dob = this.currCustomer.getDob();
            phone = this.currCustomer.getPhone();
            email = this.currCustomer.getEmail();
            setImage(this.currCustomer.getImage());
           
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Unable to load customer"));
        }
    }
    
    public String getImageContent(byte[] image) {
        String imageString = "";
        if (image != null) {
            imageString = (String) (Base64.getEncoder().encodeToString(image));
        }
        return imageString;
    }
    
    private byte[] loadDefaultImage() {
        try {
            Path imagePath = Paths.get("/Users/timothy/Downloads/Mods/Y2S2_23-24/IS3106/Assignments/Assignment2/Eventbrite/Eventbrite-war/web/resources/images/pro.jpeg");
            return Files.readAllBytes(imagePath);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public String login() {
        try {
            Customer c = customerSessionLocal.getCustomerByEmail(getEmail());
            if (c.getEmail().equals(getEmail()) && c.getPassword().equals(getPassword())) {
                
                this.setCurrCustomer(c);
                initOwnedEvents();
                initAllEvents();
                
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage("Log in successfully!", "Welcome to EventPeak."));
                return "/authenticated/landing.xhtml?faces-redirect=true";
            } else {
                this.setName(null);
                this.setPhone(null);
                this.setEmail(null);
                this.setPassword(null);
                this.setCurrCustomer(null);
                this.setImage(null);
                this.setFile(null);
                this.setDob(null);
                
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Password is incorrect", "The password is incorrect"));
                return "index.xhtml";
            }
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Email not found. Sign up now!", "The email address is not registered"));
            return "index.xhtml";
        }
    }
    
    public String logout() {
        this.setName(null);
        this.setPhone(null);
        this.setEmail(null);
        this.setPassword(null);
        this.setCurrCustomer(null);
        this.setImage(null);
        this.setFile(null);
        this.setDob(null);
        this.setSearchString(null);
                
        return "/index.xhtml?faces-redirect=true";
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the phone
     */
    public String getPhone() {
        return phone;
    }

    /**
     * @param phone the phone to set
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * @return the email
     */
    public String getEmail() {
        return email;
    }

    /**
     * @param email the email to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
 
    /**
     * @return the gender
     */
    public byte getGender() {
        return gender;
    }

    /**
     * @param gender the gender to set
     */
    public void setGender(byte gender) {
        this.gender = gender;
    }

    /**
     * @return the dob
     */
    public Date getDob() {
        return dob;
    }

    /**
     * @param dob the dob to set
     */
    public void setDob(Date dob) {
        this.dob = dob;
    }

    /**
     * @return the file
     */
    public Part getFile() {
        return file;
    }

    /**
     * @param file the file to set
     */
    public void setFile(Part file) {
        this.file = file;
    }
    
    public byte[] getImage() {
        return image;
    }

    /**
     * @return the currCustomer
     */
    public Customer getCurrCustomer() {
        return currCustomer;
    }

    /**
     * @param currCustomer the currCustomer to set
     */
    public void setCurrCustomer(Customer currCustomer) {
        this.currCustomer = currCustomer;
    }

    /**
     * @return the newPassword
     */
    public String getNewPassword() {
        return newPassword;
    }

    /**
     * @param newPassword the newPassword to set
     */
    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /**
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title the title to set
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return the eventDate
     */
    public Date getEventDate() {
        return eventDate;
    }

    /**
     * @param eventDate the eventDate to set
     */
    public void setEventDate(Date eventDate) {
        this.eventDate = eventDate;
    }

    /**
     * @return the location
     */
    public String getLocation() {
        return location;
    }

    /**
     * @param location the location to set
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param description the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the deadline
     */
    public Date getDeadline() {
        return deadline;
    }

    /**
     * @param deadline the deadline to set
     */
    public void setDeadline(Date deadline) {
        this.deadline = deadline;
    }

    /**
     * @return the ownedEvents
     */
    public List<Event> getOwnedEvents() {
        return ownedEvents;
    }

    /**
     * @param ownedEvents the ownedEvents to set
     */
    public void setOwnedEvents(List<Event> ownedEvents) {
        this.ownedEvents = ownedEvents;
    }

    /**
     * @return the events
     */
    public List<Event> getEvents() {
        return events;
    }

    /**
     * @param events the events to set
     */
    public void setEvents(List<Event> events) {
        this.events = events;
    }

    /**
     * @return the searchString
     */
    public String getSearchString() {
        return searchString;
    }

    /**
     * @param searchString the searchString to set
     */
    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    /**
     * @return the selectedEvent
     */
    public Event getSelectedEvent() {
        return selectedEvent;
    }

    /**
     * @param selectedEvent the selectedEvent to set
     */
    public void setSelectedEvent(Event selectedEvent) {
        this.selectedEvent = selectedEvent;
    }

    /**
     * @return the eventId
     */
    public Long getEventId() {
        return eventId;
    }

    /**
     * @param eventId the eventId to set
     */
    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    /**
     * @return the selectedEventCustomers
     */
    public List<Customer> getSelectedEventCustomers() {
        return selectedEventCustomers;
    }

    /**
     * @param selectedEventCustomers the selectedEventCustomers to set
     */
    public void setSelectedEventCustomers(List<Customer> selectedEventCustomers) {
        this.selectedEventCustomers = selectedEventCustomers;
    }

    /**
     * @return the registeredEvents
     */
    public List<Event> getRegisteredEvents() {
        return registeredEvents;
    }

    /**
     * @param registeredEvents the registeredEvents to set
     */
    public void setRegisteredEvents(List<Event> registeredEvents) {
        this.registeredEvents = registeredEvents;
    }

    /**
     * @return the attendedEventCustomers
     */
    public List<Customer> getAttendedEventCustomers() {
        return attendedEventCustomers;
    }

    /**
     * @param attendedEventCustomers the attendedEventCustomers to set
     */
    public void setAttendedEventCustomers(List<Customer> attendedEventCustomers) {
        this.attendedEventCustomers = attendedEventCustomers;
    }

    /**
     * @param image the image to set
     */
    public void setImage(byte[] image) {
        this.image = image;
    }

    /**
     * @return the selectedCustomer
     */
    public Customer getSelectedCustomer() {
        return selectedCustomer;
    }

    /**
     * @param selectedCustomer the selectedCustomer to set
     */
    public void setSelectedCustomer(Customer selectedCustomer) {
        this.selectedCustomer = selectedCustomer;
    }

    /**
     * @return the eventFile
     */
    public Part getEventFile() {
        return eventFile;
    }

    /**
     * @param eventFile the eventFile to set
     */
    public void setEventFile(Part eventFile) {
        this.eventFile = eventFile;
    }

    /**
     * @return the eventImage
     */
    public byte[] getEventImage() {
        return eventImage;
    }

    /**
     * @param eventImage the eventImage to set
     */
    public void setEventImage(byte[] eventImage) {
        this.eventImage = eventImage;
    }
}
