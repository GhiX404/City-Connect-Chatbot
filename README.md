# 🌆 City Connect - Smart City Citizen Portal

City Connect is a full-stack web application designed to enhance citizen engagement, streamline civic issue reporting, and improve transparency in Smart City governance. The portal empowers residents to file complaints and incidents, participate in collaborative discussions, find nearby infrastructure, and interact with a multilingual AI-powered assistant.

---

## 📌 Features

- 📝 **Complaint & Incident Reporting**
  - File and track civic complaints and public incidents.
  - Admin can update statuses, which are reflected to users in real time.

- 💬 **AI Chatbot (Hybrid)**
  - Task-oriented flow using rule-based logic for filing & tracking.
  - Integrated with **Gemini LLM** for general and out-of-scope queries.

- 📰 **Live News Feed**
  - Citizens can access the latest local news from the admin panel.

- 📊 **Public Polls & Surveys**
  - Users can create and vote on polls (with options like Yes/No/Maybe).
  - Admin can view polls for decision-making insights.

- 💡 **Collaborative Idea Sharing**
  - Citizens can post ideas and participate in threaded discussions.
  - Admins can comment (shown as "admin") without page reloads.

- 🧭 **Nearby Infrastructure Map**
  - Shows nearest facilities (e.g., hospitals, EV chargers) using Leaflet.js.
  - Based on user-entered or GPS location.

- 🔐 **Admin Dashboard**
  - View & manage all user activity (complaints, incidents, polls, ideas).
  - Post news, comment on ideas, update complaint/incident statuses.

---

## 🛠️ Technologies Used

### 🔧 Backend
- Java Spring Boot
- Spring MVC
- Spring Data JPA (Hibernate)
- REST APIs
- MySQL or H2 (dev)

### 🌐 Frontend
- HTML5, Tailwind CSS
- Thymeleaf templating engine
- JavaScript (minimal, for dynamic tab and comment handling)
- Leaflet.js for interactive maps

### 🤖 Chatbot (Python)
- Flask (REST API backend)
- NLTK (lemmatizer, tokenizer)
- Gemini LLM API integration (via `google.generativeai`)
- Hybrid approach: rule-based + fallback to LLM


## 🧠 How Gemini LLM is Integrated

- For out-of-scope user queries, the message is routed to Gemini:
  ```python
  response = model.generate_content(user_message)
  return {"response": response.text, "is_complete": True}

## 🚦 Project Structure

##File Structure
city-connect/
├── backend/
│   ├── src/main/java/com/cityconnect/app/
│   │   ├── controller/
│   │   │   ├── HomeController.java
│   │   │   └── ChatbotController.java
│   │   ├── model/
│   │   │   ├── Complaint.java
│   │   │   ├── Incident.java
│   │   │   ├── Idea.java
│   │   │   ├── Poll.java
│   │   │   └── etc. (PollVote.java, Comment.java, etc.)
│   │   └── repository/
│   │       └── (JpaRepository interfaces)
│   └── src/main/resources/
│       └── templates/
│           ├── index.html
│           ├── complaints.html
│           ├── incidents.html
│           ├── collaboration.html
│           └── admin.html
│
├── chatbot/
│   └── chatbot.py  # Hybrid chatbot with Gemini LLM fallback
│
├── static/
│   ├── img/
│   └── css/
│
└── data/
└── infrastructure.json  # Used by the Nearby feature (hospitals, schools, etc.)
