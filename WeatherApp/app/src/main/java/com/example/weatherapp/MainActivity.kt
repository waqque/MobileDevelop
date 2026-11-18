package com.example.weatherapp

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.databinding.ActivityMainBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private var currentWeather: WeatherInfo? = null
    private val apiKey = "YOUR_OPENWEATHERMAP_API_KEY"

    private val cityCoordinates = mapOf(
        "Moscow" to Pair(55.75, 37.62),
        "London" to Pair(51.51, -0.13),
        "Tokyo" to Pair(35.68, 139.69),
        "New York" to Pair(40.71, -74.01),
        "Paris" to Pair(48.85, 2.35)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
    }

    private fun setupListeners() {
        binding.citySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCity = resources.getStringArray(R.array.cities_array)[position]
                loadWeatherData(selectedCity)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        binding.tempRadioGroup.setOnCheckedChangeListener { _, _ ->
            updateUI()
        }

        binding.windDirCheckBox.setOnCheckedChangeListener { _, _ ->
            updateUI()
        }
    }

    private fun loadWeatherData(cityName: String) {
        binding.progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            val weather = withContext(Dispatchers.IO) {
                fetchFromOpenWeatherMap(cityName)
                    ?: fetchFromOpenMeteo(cityName)
                    ?: loadFromLocalJson(cityName)
            }
            binding.progressBar.visibility = View.GONE
            currentWeather = weather
            updateUI()
        }
    }

    private fun fetchFromOpenMeteo(cityName: String): WeatherInfo? {
        val coords = cityCoordinates[cityName] ?: return null
        return try {
            val endpoint = "https://api.open-meteo.com/v1/forecast?latitude=${coords.first}&longitude=${coords.second}&current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,wind_direction_10m&wind_speed_unit=ms"
            val response = URL(endpoint).readText()
            val root = JSONObject(response)
            val current = root.getJSONObject("current")

            val code = current.getInt("weather_code")
            val condition = getWeatherDescription(code)

            WeatherInfo(
                cityName = cityName,
                tempCelsius = current.getDouble("temperature_2m"),
                humidity = current.getInt("relative_humidity_2m"),
                windSpeed = current.getDouble("wind_speed_10m"),
                windDeg = current.getInt("wind_direction_10m"),
                condition = condition
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun getWeatherDescription(code: Int): String {
        return when (code) {
            0 -> "Ясно"
            in 1..3 -> "Облачно"
            in 45..48 -> "Туман"
            in 51..55 -> "Морось"
            in 61..65 -> "Дождь"
            in 71..77 -> "Снег"
            in 80..82 -> "Ливень"
            in 85..86 -> "Снегопад"
            in 95..99 -> "Гроза"
            else -> "Переменная облачность"
        }
    }

    private fun fetchFromOpenWeatherMap(cityName: String): WeatherInfo? {
        if (apiKey == "YOUR_OPENWEATHERMAP_API_KEY") return null

        return try {
            val endpoint = "https://api.openweathermap.org/data/2.5/weather?q=$cityName&appid=$apiKey&units=metric&lang=ru"
            val response = URL(endpoint).readText()
            val root = JSONObject(response)

            val main = root.getJSONObject("main")
            val wind = root.getJSONObject("wind")
            val weatherArray = root.getJSONArray("weather")
            val description = if (weatherArray.length() > 0) weatherArray.getJSONObject(0).getString("description") else ""

            WeatherInfo(
                cityName = root.getString("name"),
                tempCelsius = main.getDouble("temp"),
                humidity = main.getInt("humidity"),
                windSpeed = wind.getDouble("speed"),
                windDeg = wind.optInt("deg", 0),
                condition = description
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun loadFromLocalJson(cityName: String): WeatherInfo? {
        return try {
            val jsonString = assets.open("weather_data.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonString)
            if (root.has(cityName)) {
                val cityObj = root.getJSONObject(cityName)
                WeatherInfo(
                    cityName = cityName,
                    tempCelsius = cityObj.getDouble("temp"),
                    humidity = cityObj.getInt("humidity"),
                    windSpeed = cityObj.getDouble("windSpeed"),
                    windDeg = cityObj.getInt("windDeg"),
                    condition = cityObj.getString("condition")
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun updateUI() {
        val weather = currentWeather ?: return

        binding.tvCityName.text = weather.cityName
        binding.tvCondition.text = weather.condition
        binding.tvHumidity.text = getString(R.string.humidity_format, weather.humidity)
        binding.tvWindSpeed.text = getString(R.string.wind_speed_format, weather.windSpeed)

        if (binding.radioFahrenheit.isChecked) {
            val tempF = weather.tempCelsius * 9 / 5 + 32
            binding.tvTemperature.text = getString(R.string.temp_f_format, tempF)
        } else {
            binding.tvTemperature.text = getString(R.string.temp_c_format, weather.tempCelsius)
        }

        if (binding.windDirCheckBox.isChecked) {
            binding.tvWindDir.visibility = View.VISIBLE
            binding.tvWindDir.text = getString(R.string.wind_dir_format, weather.windDeg)
        } else {
            binding.tvWindDir.visibility = View.GONE
        }
    }
}
