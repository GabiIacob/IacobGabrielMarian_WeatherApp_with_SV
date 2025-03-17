package com.example.weatherapp

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.network.Forecast
import com.example.weatherapp.network.WeatherResponse
import com.example.weatherapp.ui.theme.BlueJC
import com.example.weatherapp.ui.theme.DarkBlueJC
import com.example.weatherapp.ui.theme.WeatherAppTheme

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferredCity = intent.getStringExtra("preferredCity") ?: ""
        setContent {
            WeatherAppTheme {
                WeatherScreen(preferredCity = preferredCity)
            }
        }
    }
}

@Composable
fun WeatherScreen(preferredCity: String) {
    val viewModel: WeatherViewModel = viewModel()
    val weatherData by viewModel.weatherData.collectAsState()

    var city by remember { mutableStateOf(preferredCity) }
    val context = LocalContext.current
    val locationHelper = remember { LocationHelper(context) }
    val isFetching = remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                isFetching.value = true
                locationHelper.getCurrentCity { cityName ->
                    city = cityName ?: "Locatie indisponibila"
                    isFetching.value = false
                }
            } else {
                city = "Permisiune refuzata"
            }
        }
    )

    fun fetchLocation() {
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                isFetching.value = true
                locationHelper.getCurrentCity { cityName ->
                    city = cityName ?: "Locatie indisponibila"
                    isFetching.value = false
                }
            }
            else -> launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    val dailyForecasts = remember(weatherData) {
        weatherData?.prognoza?.groupBy { it.data.split(" ").firstOrNull() ?: "" }
            ?.values?.toList() ?: emptyList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .paint(
                painterResource(id = R.drawable.image_with_weather_text),
                contentScale = ContentScale.FillBounds
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(180.dp))

            OutlinedTextField(
                leadingIcon = {
                    IconButton(onClick = { city = preferredCity }) {
                        Icon(Icons.Default.Star, "Oras preferat")
                    }
                },
                value = city,
                onValueChange = { city = it },
                label = { Text("Oras") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(30.dp),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    unfocusedIndicatorColor = BlueJC,
                    focusedLabelColor = DarkBlueJC
                ),
                trailingIcon = {
                    IconButton(onClick = { fetchLocation() }) {
                        Icon(Icons.Default.LocationOn, "Locatie curenta")
                    }
                }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { viewModel.fetchWeather(city) },
                colors = ButtonDefaults.buttonColors(containerColor = BlueJC)
            ) {
                Text("Verifica vremea")
            }

            Spacer(Modifier.height(16.dp))

            weatherData?.let { response ->
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        WeatherCard("Oras", response.oras, Icons.Default.Place)
                        WeatherCard(
                            "Temperatura",
                            "${response.prognoza.firstOrNull()?.temp?.toInt() ?: 0}°C",
                            Icons.Default.Star
                        )
                    }

                    Spacer(Modifier.height(16.dp))

                    Text(
                        "Prognoza 5 zile",
                        style = MaterialTheme.typography.titleMedium,
                        color = DarkBlueJC,
                        modifier = Modifier.padding(8.dp)
                    )

                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(dailyForecasts) { daily ->
                            DailyWeatherCard(forecasts = daily)
                        }
                    }
                }
            } ?: run {
                if (isFetching.value) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = BlueJC
                    )
                }
            }
        }
    }
}

@Composable
fun WeatherCard(label: String, value: String, icon: ImageVector) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .size(150.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = DarkBlueJC,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(text = label, fontSize = 14.sp, color = DarkBlueJC)
            }
            Text(
                text = value,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = BlueJC,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun DailyWeatherCard(forecasts: List<Forecast>) {
    val mainForecast = forecasts.firstOrNull() ?: return
    val dailyMax = forecasts.maxOfOrNull { it.temp } ?: 0.0
    val dailyMin = forecasts.minOfOrNull { it.temp } ?: 0.0
    Card(
        modifier = Modifier
            .width(180.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = mainForecast.data.split(" ").firstOrNull() ?: "",
                color = DarkBlueJC,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "▲ ${dailyMax.toInt()}° ▼ ${dailyMin.toInt()}°",
                color = BlueJC,
                fontSize = 14.sp
            )

            Spacer(Modifier.height(4.dp))

            Text(
                text = mainForecast.descriere.replaceFirstChar { it.uppercase() },
                color = DarkBlueJC,
                fontSize = 12.sp,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                WeatherInfoItem(
                    icon = painterResource(R.drawable.ic_humidity),
                    value = "${mainForecast.umiditate}%"
                )
                Spacer(Modifier.width(16.dp))
                WeatherInfoItem(
                    icon = painterResource(R.drawable.ic_wind),
                    value = "${mainForecast.vant.viteza.toInt()} m/s"
                )
            }
        }
    }
}

@Composable
fun WeatherInfoItem(icon: Painter, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = icon,
            contentDescription = null,
            tint = BlueJC,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(value, color = DarkBlueJC, fontSize = 12.sp)
    }
}

@Preview
@Composable
fun PreviewWeatherScreen() {
    WeatherAppTheme {
        WeatherScreen(preferredCity = "Madrid")
    }
}