    package com.cityconnect.app.controller;

    import com.cityconnect.app.model.Complaint;
    import com.cityconnect.app.model.Incident;
    import com.cityconnect.app.model.Infrastructure;
    import com.cityconnect.app.model.Idea;
    import com.cityconnect.app.model.Poll;
    import com.cityconnect.app.model.Volunteer;
    import com.cityconnect.app.model.Comment;
    import com.cityconnect.app.model.PollVote;

    import com.cityconnect.app.repository.ComplaintRepository;
    import com.cityconnect.app.repository.IncidentRepository;
    import com.cityconnect.app.repository.IdeaRepository;
    import com.cityconnect.app.repository.PollRepository;
    import com.cityconnect.app.repository.VolunteerRepository;
    import com.cityconnect.app.repository.CommentRepository;
    import com.cityconnect.app.repository.PollVoteRepository;

    
    import com.fasterxml.jackson.core.type.TypeReference;
    import com.fasterxml.jackson.databind.ObjectMapper;

    import org.springframework.beans.factory.annotation.Autowired;
    import org.springframework.web.bind.annotation.GetMapping;
    import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
    import org.springframework.http.HttpStatus;
    import org.springframework.http.ResponseEntity;
    import org.springframework.stereotype.Controller;
    import org.springframework.ui.Model;
    import org.springframework.web.bind.annotation.*;
    import org.springframework.web.multipart.MultipartFile;

    import java.io.File;
    import java.io.IOException;
    import java.time.LocalDate;
    import java.util.*;

    @Controller
    public class HomeController {

        // ==== Repositories ====
        @Autowired private ComplaintRepository complaintRepo;
        @Autowired private IncidentRepository incidentRepo;
        @Autowired private IdeaRepository ideaRepo;
        @Autowired private PollRepository pollRepo;
        @Autowired private VolunteerRepository volunteerRepo;
        @Autowired private CommentRepository commentRepo;
        @Autowired private PollVoteRepository pollVoteRepo;


        @GetMapping("/")
        public String loginPage() {
            return "login";
        }

        @GetMapping("/index")
        public String home() {
            return "index";
        }

        @GetMapping("/services")
        public String servicesPage() {
            return "services";    
        }

        // ==== Complaints ====
        @GetMapping("/complaints")
        public String complaintsPage(Model model) {
            model.addAttribute("complaints", complaintRepo.findAll());
            return "complaints";
        }

        // Form-based endpoint for web interface
        @PostMapping("/api/complaints")
        public String submitComplaint(@RequestParam String complaintType,
                                    @RequestParam String description,
                                    @RequestParam String location,
                                    @RequestParam("image") MultipartFile imageFile)
                                    throws IOException {
            Complaint c = new Complaint();
            c.setComplaintType(complaintType);
            c.setDescription(description);
            c.setLocation(location);
            c.setDate(LocalDate.now());

            if (!imageFile.isEmpty()) {
                String dir = "src/main/resources/static/uploads/";
                new File(dir).mkdirs();
                String fn = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                imageFile.transferTo(new File(dir + fn));
                c.setImagePath("/uploads/" + fn);
            }

            complaintRepo.save(c);
            return "redirect:/complaints?success=true";
        }

        // JSON-based endpoint for API clients (e.g., Python chatbot)
        @PostMapping("/api/complaints/json")
        @ResponseBody
        public ResponseEntity<Complaint> submitComplaintJson(@RequestBody Complaint complaint) {
            // Set the date if not provided
            if (complaint.getDate() == null) {
                complaint.setDate(LocalDate.now());
            }
            
            // Set default status if not provided
            if (complaint.getStatus() == null || complaint.getStatus().isEmpty()) {
                complaint.setStatus("Open");
            }
            
            Complaint savedComplaint = complaintRepo.save(complaint);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedComplaint);
        }

        @GetMapping("/api/complaints/{id}")
        @ResponseBody
        public ResponseEntity<Complaint> getComplaintById(@PathVariable Long id) {
            return complaintRepo.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        // ==== Incidents ====
        @GetMapping("/incidents")
        public String incidentsPage(Model model) {
            model.addAttribute("incidents", incidentRepo.findAll());
            return "incidents";
        }

        // Form-based endpoint for web interface
        @PostMapping("/api/incidents")
        public String submitIncident(@RequestParam String incidentType,
                                    @RequestParam String description,
                                    @RequestParam String location,
                                    @RequestParam String urgency,
                                    @RequestParam("image") MultipartFile imageFile)
                                    throws IOException {
            Incident i = new Incident();
            i.setIncidentType(incidentType);
            i.setDescription(description);
            i.setLocation(location);
            i.setUrgency(urgency);
            i.setDate(LocalDate.now());

            if (!imageFile.isEmpty()) {
                String dir = "src/main/resources/static/uploads/";
                new File(dir).mkdirs();
                String fn = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
                imageFile.transferTo(new File(dir + fn));
                i.setImagePath("/uploads/" + fn);
            }

            incidentRepo.save(i);
            return "redirect:/incidents?success=true";
        }

        // JSON-based endpoint for API clients (e.g., Python chatbot)
        @PostMapping("/api/incidents/json")
        @ResponseBody
        public ResponseEntity<Incident> submitIncidentJson(@RequestBody Incident incident) {
            // Set the date if not provided
            if (incident.getDate() == null) {
                incident.setDate(LocalDate.now());
            }
            
            // Set default status if not provided
            if (incident.getStatus() == null || incident.getStatus().isEmpty()) {
                incident.setStatus("Open");
            }
            
            // Set default urgency if not provided
            if (incident.getUrgency() == null || incident.getUrgency().isEmpty()) {
                incident.setUrgency("Medium");
            }
            
            Incident savedIncident = incidentRepo.save(incident);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedIncident);
        }

        @GetMapping("/api/incidents/{id}")
        @ResponseBody
        public ResponseEntity<Incident> getIncidentById(@PathVariable Long id) {
            return incidentRepo.findById(id)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
        }

        // ==== Nearby Infrastructure ====
        @GetMapping("/nearby")
        public String nearbyPage() {
            return "nearby";
        }

        @GetMapping("/api/map/nearby")
        @ResponseBody
        public ResponseEntity<?> getNearbyInfrastructure(
                @RequestParam double lat,
                @RequestParam double lon,
                @RequestParam(required = false) String type,
                @RequestParam(defaultValue = "2") double radiusKm
        ) {
            // Validate coordinates
            if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Invalid coordinates"));
            }
            
            // Validate radius (in kilometers)
            if (radiusKm <= 0 || radiusKm > 100) {
                return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Radius must be between 0.1 and 100 km"));
            }

            try {
                // Load the JSON file from resources
                Resource resource = new ClassPathResource("infrastructure.json");
                if (!resource.exists()) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Infrastructure data not available"));
                }

                ObjectMapper mapper = new ObjectMapper();
                List<Infrastructure> all = mapper.readValue(
                    resource.getInputStream(), 
                    new TypeReference<List<Infrastructure>>() {});

                List<Infrastructure> filtered = new ArrayList<>();
                for (Infrastructure infra : all) {
                    // Skip if coordinates are invalid
                    if (infra.getLatitude() == 0.0 || infra.getLongitude() == 0.0) {
                        continue;
                    }
                    
                    // Check type match (case-insensitive and handle null/empty)
                    boolean matchType = (type == null || type.trim().isEmpty())
                            || (infra.getType() != null && infra.getType().equalsIgnoreCase(type.trim()));
                    
                    // Calculate distance
                    double dist = haversine(lat, lon, infra.getLatitude(), infra.getLongitude());
                    
                    if (matchType && dist <= radiusKm) {
                        filtered.add(infra);
                    }
                }
                
                // Sort by distance (closest first)
                filtered.sort(Comparator.comparingDouble(infra -> 
                    haversine(lat, lon, infra.getLatitude(), infra.getLongitude())));
                
                return ResponseEntity.ok(filtered);
                
            } catch (Exception e) {
                // Log the error for debugging
                System.err.println("Error processing nearby infrastructure request: " + e.getMessage());
                e.printStackTrace();
                
                // Return a user-friendly error message
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "An error occurred while processing your request"));
            }
        }

        // Haversine formula
        private double haversine(double lat1, double lon1,
                                double lat2, double lon2) {
            final int R = 6371; // km
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat/2)*Math.sin(dLat/2)
                    + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2))
                    * Math.sin(dLon/2)*Math.sin(dLon/2);
            return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        }

        // ==== Collaboration ====
        @GetMapping("/collaboration")
        public String showCollaboration(Model model) {
            model.addAttribute("ideas", ideaRepo.findAll());
            model.addAttribute("polls", pollRepo.findAll());
            model.addAttribute("volunteers", volunteerRepo.findAll());
            return "collaboration";
        }


        @PostMapping("/api/collaboration/ideas")
        public String submitIdea(@RequestParam String ideaTitle,
                                 @RequestParam String description,
                                 Model model) {
            Idea idea = new Idea();
            idea.setIdeaTitle(ideaTitle);
            idea.setDescription(description);
            idea.setDate(LocalDate.now());
            ideaRepo.save(idea);

            // Re-populate the model for collaboration.html
            model.addAttribute("ideas", ideaRepo.findAll());
            model.addAttribute("polls", pollRepo.findAll());
            model.addAttribute("volunteers", volunteerRepo.findAll());

            return "collaboration"; // forward to template with updated model
        }
        


        @PostMapping("/api/collaboration/polls")
        public String submitPoll(@RequestParam String question,
                                 @RequestParam String options,
                                 Model model) {
            Poll poll = new Poll();
            poll.setQuestion(question);
            poll.setOptions(options);
            poll.setDate(LocalDate.now());
            pollRepo.save(poll);

            model.addAttribute("ideas", ideaRepo.findAll());
            model.addAttribute("polls", pollRepo.findAll());
            model.addAttribute("volunteers", volunteerRepo.findAll());
            model.addAttribute("success", "poll");

            return "collaboration"; // Forward to template
        }




        

//        @PostMapping("/api/collaboration/volunteers")
//        public String submitVolunteer(@RequestParam String name,
//                                    @RequestParam String contact,
//                                    @RequestParam String areaOfInterest) {
//            Volunteer v = new Volunteer();
//            v.setName(name);
//            v.setContact(contact);
//            v.setAreaOfInterest(areaOfInterest);
//            v.setDate(LocalDate.now());
//            volunteerRepo.save(v);
//            return "redirect:/collaboration?success=volunteer";
//        }

        // ==== News ====
        @GetMapping("/api/news")
        @ResponseBody
        public ResponseEntity<List<Map<String, String>>> getNews() {
            List<Map<String, String>> newsList = new ArrayList<>();
            
            Map<String, String> news1 = new HashMap<>();
            news1.put("title", "New Bus Routes Announced");
            news1.put("description", "KSRTC introduces new routes starting August 2025. Check schedules in the app.");
            newsList.add(news1);
            
            Map<String, String> news2 = new HashMap<>();
            news2.put("title", "Property Tax Deadline");
            news2.put("description", "Pay your property tax by July 31, 2025, to avoid penalties.");
            newsList.add(news2);
            
            Map<String, String> news3 = new HashMap<>();
            news3.put("title", "EV Charging Stations Added");
            news3.put("description", "New EV charging units installed at 5 locations. Find them in Nearby Services.");
            newsList.add(news3);
            
            return ResponseEntity.ok(newsList);
        }
        @GetMapping("/admin")
        public String adminPage(Model model) {
            model.addAttribute("complaints", complaintRepo.findAll());
            model.addAttribute("incidents", incidentRepo.findAll());
            model.addAttribute("polls", pollRepo.findAll());
            model.addAttribute("ideas", ideaRepo.findAll());
            return "admin";
        }

        @PostMapping("/admin/updateComplaintStatus")
        public String updateComplaintStatus(@RequestParam Long id, @RequestParam String status) {
            Complaint c = complaintRepo.findById(id).orElse(null);
            if (c != null) {
                c.setStatus(status);
                complaintRepo.save(c);
            }
            return "redirect:/admin";
        }

        @PostMapping("/admin/updateIncidentStatus")
        public String updateIncidentStatus(@RequestParam Long id, @RequestParam String status) {
            Incident i = incidentRepo.findById(id).orElse(null);
            if (i != null) {
                i.setStatus(status);
                incidentRepo.save(i);
            }
            return "redirect:/admin";
        }
        @PostMapping("/discussions/comment")
        public String addComment(@RequestParam Long ideaId,
                                 @RequestParam String content,
                                 Model model) {
            Idea idea = ideaRepo.findById(ideaId).orElse(null);
            if (idea != null) {
                Comment comment = new Comment();
                comment.setAuthor("Anonymous");
                comment.setContent(content);
                comment.setDate(LocalDate.now());
                comment.setIdea(idea);
                commentRepo.save(comment);
            }

            // Important: Reload all data into the model for collaboration.html
            model.addAttribute("ideas", ideaRepo.findAll());
            model.addAttribute("polls", pollRepo.findAll());
            model.addAttribute("volunteers", volunteerRepo.findAll());
            model.addAttribute("success", "comment");

            return "collaboration"; // Forward to template with updated model
        }

        @PostMapping("/admin/comment")
        public String addAdminComment(@RequestParam Long ideaId,
                                      @RequestParam String content,
                                      Model model) {
            Idea idea = ideaRepo.findById(ideaId).orElse(null);
            if (idea != null) {
                Comment comment = new Comment();
                comment.setAuthor("Admin"); // set author as Admin
                comment.setContent(content);
                comment.setDate(LocalDate.now());
                comment.setIdea(idea);
                commentRepo.save(comment);
            }

            // Re-load all data to reflect the new comment immediately
            model.addAttribute("complaints", complaintRepo.findAll());
            model.addAttribute("incidents", incidentRepo.findAll());
            model.addAttribute("polls", pollRepo.findAll());
            model.addAttribute("ideas", ideaRepo.findAll());

            return "admin"; // direct render instead of redirect
        }


        @PostMapping("/polls/vote")
        public String votePoll(@RequestParam Long pollId,
                               @RequestParam String choice,
                               Model model) {
            Poll poll = pollRepo.findById(pollId).orElse(null);
            if (poll != null) {
                PollVote vote = new PollVote();
                vote.setChoice(choice);
                vote.setPoll(poll);
                vote.setVoter("Anonymous");
                pollVoteRepo.save(vote);
            }

            // Refresh model to see updated votes
            model.addAttribute("ideas", ideaRepo.findAll());
            model.addAttribute("polls", pollRepo.findAll());
            model.addAttribute("volunteers", volunteerRepo.findAll());
            model.addAttribute("success", "vote");

            return "collaboration";
        }



    }
