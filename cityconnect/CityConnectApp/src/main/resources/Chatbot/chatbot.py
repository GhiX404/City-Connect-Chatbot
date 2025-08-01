# backend/chatbot.py
# At the top of your chatbot.py file
import os
import google.generativeai as genai

# Load API key from an environment variable
#genai.configure(api_key=os.environ.get("AIzaSyCtwChI10yOifMi5PVQsjjNAlr_ReKAPzo"))

# Or, if you're not using environment variables (not recommended for production):
gemini_api_key = "AIzaSyCtwChI10yOifMi5PVQsjjNAlr_ReKAPzo"
genai.configure(api_key=gemini_api_key)
from flask import Flask, request, jsonify
from flask_cors import CORS
import requests
import re
import nltk
from nltk.stem import WordNetLemmatizer
from nltk.tokenize import word_tokenize
from nltk.corpus import wordnet as wn
from datetime import datetime

# --- 1. FLASK APP INITIALIZATION ---
app = Flask(__name__)
CORS(app)

# --- 2. NLTK AND STATE SETUP ---
try:
    nltk.data.find('tokenizers/punkt')
    nltk.data.find('corpora/wordnet')
except LookupError:
    print("NLTK data not found. Downloading...")
    nltk.download('punkt', quiet=True)
    nltk.download('wordnet', quiet=True)
    print("NLTK data downloaded.")

wn.ensure_loaded()
print("NLTK WordNet corpus loaded.")

lemmatizer = WordNetLemmatizer()

conversation_state = {}
JAVA_API_BASE_URL = "http://localhost:8080/api"

# --- 3. HELPER FUNCTIONS (No changes here) ---
# --- 3.1. NEW: GEMINI HELPER FUNCTION ---
def get_gemini_response(prompt):
    """
    Generates a response using the Gemini LLM.
    """
    try:
        model = genai.GenerativeModel('gemini-1.5-flash')
        response = model.generate_content(prompt)
        return response.text
    except Exception as e:
        print(f"Gemini API Error: {e}")
        return "I'm sorry, I am unable to process your request at the moment."
def preprocess_text(text):
    tokens = word_tokenize(text.lower())
    lemmas = [lemmatizer.lemmatize(token) for token in tokens]
    return lemmas

def get_status(item_id):
    try:
        response = requests.get(f"{JAVA_API_BASE_URL}/complaints/{item_id}")
        if response.status_code == 200:
            data = response.json()
            return f"Complaint #{item_id} status: {data.get('status', 'Unknown')}. Type: {data.get('complaintType', 'N/A')}."
        response = requests.get(f"{JAVA_API_BASE_URL}/incidents/{item_id}")
        if response.status_code == 200:
            data = response.json()
            return f"Incident #{item_id} status: {data.get('status', 'Unknown')}. Type: {data.get('incidentType', 'N/A')}."
        return f"Sorry, I couldn't find any item with the ID '{item_id}'."
    except requests.RequestException:
        return "Sorry, I was unable to connect to the tracking service right now."

def submit_complaint(data):
    try:
        response = requests.post(f"{JAVA_API_BASE_URL}/complaints/json", json=data)
        if response.status_code in [200, 201]:
            return response.json()
        else:
            print(f"API Error (Complaint): Received status code {response.status_code}, Body: {response.text}")
            return None
    except requests.RequestException as e:
        print(f"Connection Error (Complaint): {e}")
        return None

def submit_incident(data):
    try:
        response = requests.post(f"{JAVA_API_BASE_URL}/incidents/json", json=data)
        if response.status_code in [200, 201]:
            return response.json()
        else:
            print(f"API Error (Incident): Received status code {response.status_code}, Body: {response.text}")
            return None
    except requests.RequestException as e:
        print(f"Connection Error (Incident): {e}")
        return None

def get_news():
    try:
        response = requests.get(f"{JAVA_API_BASE_URL}/news")
        if response.status_code == 200:
            news_list = response.json()
            if not news_list: return "No news updates available at the moment."
            news_text = "📰 Latest News & Updates:\n\n"
            for i, news in enumerate(news_list[:3], 1): # Show top 3
                news_text += f"• {news.get('title', 'No Title')}\n"
            return news_text.strip()
        else:
            return "Sorry, I couldn't fetch the latest news right now."
    except requests.RequestException:
        return "Sorry, I was unable to connect to the news service."


# --- 4. CORE CHATBOT LOGIC (MODIFIED) ---
def process_message(user_id, user_message):
    state = conversation_state.get(user_id, {})
    lemmas = preprocess_text(user_message)
    response_text = ""

    # Check if we are in the middle of a multi-step task
    if state.get("task"):
        task = state["task"]
        # (The logic for ongoing tasks remains the same)
        if task == "file_complaint":
            step = state["step"]
            if step == "awaiting_title":
                state["data"]["complaintType"] = user_message
                state["step"] = "awaiting_description"
                response_text = "Thank you. Now, please provide a detailed description of the issue."
            elif step == "awaiting_description":
                state["data"]["description"] = user_message
                state["step"] = "awaiting_location"
                response_text = "Got it. Finally, what is the location of this issue?"
            elif step == "awaiting_location":
                state["data"]["location"] = user_message
                state["data"]["date"] = datetime.now().strftime("%Y-%m-%d")
                api_response = submit_complaint(state['data'])
                if api_response and api_response.get("id"):
                    response_text = f"Your complaint has been filed successfully! Your complaint ID is: {api_response.get('id')}."
                else:
                    response_text = "There was an error filing your complaint. Please try again."
                state = {}
        elif task == "file_incident":
            step = state["step"]
            if step == "awaiting_type":
                state["data"]["incidentType"] = user_message
                state["step"] = "awaiting_description"
                response_text = "Thank you. Now, please provide a detailed description of the incident."
            elif step == "awaiting_description":
                state["data"]["description"] = user_message
                state["step"] = "awaiting_location"
                response_text = "Got it. What is the location of this incident?"
            elif step == "awaiting_location":
                state["data"]["location"] = user_message
                state["step"] = "awaiting_urgency"
                response_text = "Finally, what is the urgency level? (Low, Medium, High)"
            elif step == "awaiting_urgency":
                state["data"]["urgency"] = user_message
                state["data"]["date"] = datetime.now().strftime("%Y-%m-%d")
                api_response = submit_incident(state['data'])
                if api_response and api_response.get("id"):
                    response_text = f"Your incident report has been filed successfully! Your incident ID is: {api_response.get('id')}."
                else:
                    response_text = "There was an error filing your incident report. Please try again."
                state = {}
    else:
        # --- MODIFICATION: Changed 'and' to 'or' for more flexible keyword matching ---
        
        # Rule for Complaint Management
        if "complaint" in lemmas or "issue" in lemmas or ("file" in lemmas and "complaint" in lemmas):
            state = {"task": "file_complaint", "step": "awaiting_title", "data": {}}
            response_text = "I can help you file a complaint. What is the title/type of the complaint?"
        
        # Rule for Incident Reporting
        elif "incident" in lemmas or ("report" in lemmas and "incident" in lemmas):
            state = {"task": "file_incident", "step": "awaiting_type", "data": {}}
            response_text = "I can help you file an incident report. What type of incident is this?"
        
        # Rule for Tracking Status
        elif "track" in lemmas or "status" in lemmas:
            match = re.search(r'(\w+-\w+|\d+)', user_message)
            if match:
                response_text = get_status(match.group(0))
            else:
                response_text = "Sure, please provide the complaint or incident ID you wish to track (e.g., 123 or COMP-456)."
        
        # Other rules remain the same
        elif "nearby" in lemmas or "facilities" in lemmas or "services" in lemmas:
            response_text = "To find nearby facilities and services, please use the interactive map feature on our main page. You can search for hospitals, police stations, fire stations, and other civic amenities near your location."
        elif "admin" in lemmas or "contact" in lemmas:
            response_text = "For administrative queries or urgent matters, please contact our admin team at: admin@cityconnect.gov.in"
        elif "news" in lemmas or "updates" in lemmas:
            response_text = get_news()
        elif "certificate" in lemmas:
            response_text = "For certificate services, please visit the official government portal. Are you looking for a Birth, Death, or Marriage certificate?"
        elif "hello" in lemmas or "hi" in lemmas or "help" in lemmas:
            response_text = "Hello! I am the City Connect assistant. I can help you with:\n• File complaints\n• File incident reports\n• Track complaint/incident status\n• Find nearby facilities\n• Contact admin\n• Check news and updates\n\nHow can I assist you today?"
        else:
        # If none of the above rules match, use Gemini as a fallback.
        	response_text = get_gemini_response(user_message)
    # Update the user's state
    conversation_state[user_id] = state
    
    # Return a dictionary with the response and a completion flag
    is_task_complete = not state.get("task")
    return {"response": response_text, "is_complete": is_task_complete}

# --- 5. FLASK API ENDPOINT ---
@app.route("/chat", methods=["POST"])
def chat():
    try:
        user_message = request.json.get("message")
        if not user_message:
            return jsonify({"error": "No message provided"}), 400
        
        user_id = "default_user"
        bot_data = process_message(user_id, user_message)
        return jsonify(bot_data)
    except Exception as e:
        print(f"An error occurred during chat processing: {e}")
        import traceback
        traceback.print_exc()
        return jsonify({"error": "An internal server error occurred"}), 500

# --- 6. RUN THE FLASK APP ---
if __name__ == "__main__":
    app.run(host="0.0.0.0", port=8000)
