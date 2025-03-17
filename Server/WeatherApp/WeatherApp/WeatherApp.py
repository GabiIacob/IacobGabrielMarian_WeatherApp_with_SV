from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import requests
import logging

app = FastAPI()

# Configurare CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["GET"],
    allow_headers=["*"],
)

logging.basicConfig(level=logging.INFO)

OPENWEATHER_URL = "https://api.openweathermap.org/data/2.5/forecast"
API_KEY = "320f276d9577680125f98a70f46d6a13"
import json

@app.get("/vreme")
async def get_weather(oras: str = "Reghin"):
    try:
        if not oras.strip():
            raise HTTPException(status_code=400, detail="Numele orasului este obligatoriu")

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

        with open("weather_response.json", "w") as f:
            json.dump(data, f, indent=4)

        if "city" not in data or "list" not in data:
            raise HTTPException(status_code=500, detail="Format raspuns invalid de la OpenWeather")

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
            raise HTTPException(status_code=404, detail="Oras negasit in OpenWeather")
        raise HTTPException(status_code=400, detail="Eroare la comunicarea cu OpenWeather API")
    except Exception as e:
        logging.error(f"Eroare generala: {str(e)}")
        raise HTTPException(status_code=500, detail="A aparut o eroare interna pe server")
