from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import sqlite3
import requests
import logging
import json

app = FastAPI()

# Configurare CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET"],
    allow_headers=["*"],
)

# api
OPENWEATHER_URL = "https://api.openweathermap.org/data/2.5/forecast"
API_KEY = "320f276d9577680125f98a70f46d6a13"

class User(BaseModel):
    email: str
    password: str
    city: str

class LoginRequest(BaseModel):
    email: str
    password: str

def get_db_connection():
    conn = sqlite3.connect('weather_app.db')
    return conn

@app.post("/add_user")
async def add_user(user: User):
    conn = get_db_connection()
    cursor = conn.cursor()
    cursor.execute(
        "INSERT INTO users (email, password, city) VALUES (?, ?, ?)",
        (user.email, user.password, user.city)
    )
    conn.commit()
    conn.close()
    return {"message": "User added successfully", "user": user}

def authenticate_user(email: str, password: str):
    conn = sqlite3.connect('weather_app.db')
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM users WHERE email = ? AND password = ?", (email, password))
    user = cursor.fetchone()
    conn.close()

    if user:
        return {
            "status": "success",
            "message": "User authenticated",
            "email": user[1],
            "preferredCity": user[3]
        }
    else:
        return {"status": "error", "message": "Invalid email or password"}

@app.post("/api/login")
async def login(request: LoginRequest):
    result = authenticate_user(request.email, request.password)
    if result["status"] == "success":
        return result  
    else:
        raise HTTPException(status_code=401, detail=result["message"])

@app.get("/vreme")
async def get_weather(oras: str = "Reghin"):
    try:
        params = {
            "q": oras,
            "appid": API_KEY,
            "units": "metric",
            "lang": "ro",
            "cnt": 40
        }

        response = requests.get(OPENWEATHER_URL, params=params)
        response.raise_for_status()
        data = response.json()

        return {
            "oras": data["city"]["name"],
            "tara": data["city"]["country"],
            "prognoza": [
                {
                    "data": item["dt_txt"],
                    "temp": item["main"]["temp"],
                    "temp_min": item["main"]["temp_min"],
                    "temp_max": item["main"]["temp_max"],
                    "umiditate": item["main"]["humidity"],
                    "descriere": item["weather"][0]["description"].capitalize(),
                    "vant": {
                        "viteza": item["wind"]["speed"],
                        "directie": item["wind"]["deg"]
                    }
                } for item in data["list"]
            ]
        }
    except requests.exceptions.HTTPError as e:
        logging.error(f"Eroare OpenWeather: {str(e)}")
        if e.response.status_code == 404:
            raise HTTPException(status_code=404, detail="Oras negasit în OpenWeather")
        raise HTTPException(status_code=400, detail="Eroare la comunicarea cu OpenWeather API")
    except Exception as e:
        logging.error(f"Eroare generala: {str(e)}")
        raise HTTPException(status_code=500, detail="A aparut o eroare interna pe server")
