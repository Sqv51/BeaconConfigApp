import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import android.os.Bundle
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

    private val client = OkHttpClient()

    private fun sendPostRequest(url: String, json: JSONObject) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                runOnUiThread {
                    httpResponseText.text = "Failed to execute POST request: ${e.message}"
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    val responseData = response.body?.string()
                    runOnUiThread {
                        httpResponseText.text = "Response: $responseData"
                    }
                }
            }
        })
    }

    private fun sendGetRequest(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()
    }

}