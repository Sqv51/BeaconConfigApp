import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.beaconconfigapp.R

class MainActivity : AppCompatActivity() {

    private lateinit var macAddressInput: EditText
    private lateinit var macAddressSpinner: Spinner
    private lateinit var integerInput: EditText
    private lateinit var addMacButton: Button
    private lateinit var getMacButton: Button
    private lateinit var removeMacButton: Button
    private lateinit var updateRssiButton: Button
    private lateinit var httpResponseText: TextView

    private var tempString = "temp"
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        macAddressInput = findViewById(R.id.macAddressInput)
        macAddressSpinner = findViewById(R.id.macAddressSpinner)
        integerInput = findViewById(R.id.integerInput)
        addMacButton = findViewById(R.id.addMacButton)
        getMacButton = findViewById(R.id.getMacButton)
        removeMacButton = findViewById(R.id.removeMacButton)
        updateRssiButton = findViewById(R.id.updateRssiButton)
        httpResponseText = findViewById(R.id.httpResponseText)


        addMacButton.setOnClickListener {
            val macAddress = macAddressInput.text.toString()
            sendRequest(addMac(macAddress))

        }

        getMacButton.setOnClickListener {
            tempString = sendRequest(getMac())
            macAddressSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, tempString.split(","))
        }

        removeMacButton.setOnClickListener {
            val macAddress = macAddressSpinner.selectedItem.toString()
            sendRequest(removeMac(macAddress))
        }

        updateRssiButton.setOnClickListener {
            val rssi = integerInput.text.toString().toInt()
            sendRequest(updateRssi(rssi))
        }
    }
    //create send request function
    private fun sendRequest(request: Request): String {
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                runOnUiThread {
                    httpResponseText.text = body
                }

            }
        })
        return httpResponseText.text.toString()
    }


    private fun addMac(macAddress: String): Request {
        val url = "http://10.34.82.169:80/addMac"
        val json = JSONObject()
        json.put("macAddress", macAddress)
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        return request


    }

    private fun getMac(): Request {
        val url = "http://10.34.82.169:80/getMacs"
        val request = Request.Builder()
            .url(url)
            .build()
        return request


    }

    private fun removeMac(macAddress: String): Request {
        val url = "http://10.34.82.169:80/removeMac"
        val json = JSONObject()
        json.put("macAddress", macAddress)
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        return request

    }

    private fun updateRssi(rssi: Int): Request {
        val url = "http://10.34.82.169:80/updateRssi"
        val json = JSONObject()
        json.put("rssi", rssi)
        println("JSON: $json") // This will print the JSON object
        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        return request
    }
}