package com.example.analiza_pogody

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.datepicker.MaterialDatePicker
import io.socket.client.Ack
import io.socket.client.IO
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class SelectionOfParametersForWeatherAnalysisActivity : AppCompatActivity(),
    AdapterView.OnItemSelectedListener {

    private var pickDateButton: Button? = null
    private var showSelectedDateText: TextView? = null
    private var startDate: Long? = null
    private var endDate: Long? = null
    private var lineChart: LineChart? = null
    private var spinnerWeatherAnalysis: Spinner? = null
    private var selectedWeatherAnalysisFromSpinner: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selection_of_parameters_for_weather_analysis)

        val intent = intent
        var city = intent.getStringExtra("KEY_CITY_SELECTION")

        pickDateButton = findViewById(R.id.buttonPickDate)
        showSelectedDateText = findViewById(R.id.showSelectedDate)
        lineChart = findViewById(R.id.chart)
        spinnerWeatherAnalysis = findViewById(R.id.spinnerWeatherAnalysis)

        spinnerWeatherAnalysis?.let {
            it.setOnItemSelectedListener(this)
            val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(this,
                android.R.layout.simple_spinner_item,
                resources.getStringArray(R.array.weather_analysis))
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            it.adapter = arrayAdapter
        }

        val materialDateBuilder: MaterialDatePicker.Builder<Pair<Long, Long>> =
            MaterialDatePicker.Builder.dateRangePicker()
        materialDateBuilder.setTitleText("Wybierz datę: ")
        val materialDatePicker: MaterialDatePicker<Pair<Long, Long>> = materialDateBuilder.build()
        pickDateButton?.setOnClickListener {
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
        }

        val baseUrl = "http://10.0.2.2:42205" // localhost
        //val baseUrl = "https://hurtownie-production.up.railway.app:5612" // railway
        val socket = IO.socket(baseUrl)
        socket.connect()

        materialDatePicker.addOnPositiveButtonClickListener {
            showSelectedDateText?.text = "Wybrana data to: " + materialDatePicker.headerText
            startDate = it.first / 1000
            endDate = it.second / 1000
            city = when (city) {
                "Warszawa" -> {
                    "Warsaw"
                }
                "Kraków" -> {
                    "Krakow"
                }
                else -> {
                    "Tarnów"
                }
            }

            socket.emit("getData", city, startDate, endDate, Ack { args ->
                val responseObject = Response(args[0].toString())
                val dates: MutableList<String> = mutableListOf()
                val tempsEntry: MutableList<Entry> = mutableListOf()
                val pressureEntry: MutableList<Entry> = mutableListOf()
                val humidityEntry: MutableList<Entry> = mutableListOf()
                val windSpeedEntry: MutableList<Entry> = mutableListOf()

                responseObject.data?.let { list ->
                    for ((index, item) in list.withIndex()) {
                        getDateTime(item.date.toString())?.let { date -> dates.add(date) }
                        tempsEntry.add(Entry(index.toFloat(), item.temp.toFloat()))
                        pressureEntry.add(Entry(index.toFloat(), item.pressure.toFloat()))
                        humidityEntry.add(Entry(index.toFloat(), item.humidity.toFloat()))
                        windSpeedEntry.add(Entry(index.toFloat(), item.windSpeed.toFloat()))
                    }
                }

                val description = Description()
                description.text = when (selectedWeatherAnalysisFromSpinner) {
                    "Temperatura" -> {
                        "Temperatura"
                    }
                    "Ciśnienie" -> {
                        "Ciśnienie"
                    }
                    "Wilgotność" -> {
                        "Wilgotność"
                    }
                    else -> {
                        "Prędkość wiatru"
                    }
                }
                description.setPosition(150f, 15f)
                lineChart?.description = description
                lineChart?.axisRight?.setDrawLabels(false)

                val xAxis = lineChart?.xAxis
                xAxis?.position = XAxis.XAxisPosition.BOTTOM
                xAxis?.valueFormatter = IndexAxisValueFormatter(dates)
                xAxis?.labelCount = 4
                xAxis?.granularity = 1f

                val yAxis = lineChart?.axisLeft
                when (selectedWeatherAnalysisFromSpinner) {
                    "Temperatura" -> {
                        yAxis?.axisMinimum = -30f
                        yAxis?.axisMaximum = 40f
                    }
                    "Ciśnienie" -> {
                        yAxis?.axisMinimum = 950f
                        yAxis?.axisMaximum = 1050f
                    }
                    "Wilgotność" -> {
                        yAxis?.axisMinimum = 0f
                        yAxis?.axisMaximum = 100f
                    }
                    else -> {
                        yAxis?.axisMinimum = 0f
                        yAxis?.axisMaximum = 140f
                    }
                }
                yAxis?.axisLineWidth = 2f
                yAxis?.axisLineColor = Color.BLACK
                yAxis?.labelCount = 10

                val dataSet = when (selectedWeatherAnalysisFromSpinner) {
                    "Temperatura" -> {
                        LineDataSet(tempsEntry, "Temperatura")
                    }
                    "Ciśnienie" -> {
                        LineDataSet(pressureEntry, "Ciśnienie")
                    }
                    "Wilgotność" -> {
                        LineDataSet(humidityEntry, "Wilgotność")
                    }
                    else -> {
                        LineDataSet(windSpeedEntry, "Wilgotność")
                    }
                }
                dataSet.color = Color.BLUE

                val lineData = LineData(dataSet)
                lineChart?.data = lineData
                lineChart?.invalidate()
            })
        }
    }

    private fun getDateTime(timestamp: String): String? {
        return try {
            val simpleDateFormat = SimpleDateFormat("MM/dd/yyyy")
            val date = Date(timestamp.toLong() * 1000)
            simpleDateFormat.format(date)
        } catch (exception: Exception) {
            exception.toString()
        }
    }

    override fun onItemSelected(
        adapterView: AdapterView<*>?,
        view: View?,
        position: Int,
        id: Long,
    ) {
        selectedWeatherAnalysisFromSpinner =
            resources.getStringArray(R.array.weather_analysis)[position]
    }

    override fun onNothingSelected(adapterView: AdapterView<*>?) {
        // Do nothing
    }
}

class Response(json: String) : JSONObject(json) {
    val data = this.optJSONArray("data")
        ?.let { 0.until(it.length()).map { i -> it.optJSONObject(i) } }
        ?.map { Foo(it.toString()) }
}

class Foo(json: String) : JSONObject(json) {
    val date = this.optInt("date")
    val timezone = this.optInt("timezone")
    val city: String? = this.optString("city")
    val temp = this.optDouble("temp")
    val feelsLike = this.optDouble("feelsLike")
    val pressure = this.optInt("pressure")
    val humidity = this.optInt("humidity")
    val visibility = this.optInt("visibility")
    val windSpeed = this.optInt("windSpeed")
    val windDeg = this.optInt("windDeg")
}