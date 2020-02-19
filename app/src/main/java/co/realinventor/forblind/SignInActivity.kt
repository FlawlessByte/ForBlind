package co.realinventor.forblind

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SignInActivity : AppCompatActivity() {
    lateinit var editTextPhone : TextInputEditText
    lateinit var buttonContinue : MaterialButton
    val TAG = "SignInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        editTextPhone = findViewById(R.id.editTextPhoneNumber);
        buttonContinue = findViewById(R.id.buttonContinuePhone);

        buttonContinue.setOnClickListener {
            Log.d(TAG, "Continue Button clicked")
            val number = editTextPhone.text.toString()
            if(number.length < 10){
                Toast.makeText(this@SignInActivity, "Enter a valid phone number!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            var intent = Intent(this@SignInActivity, VerifyActivity::class.java)
            intent.putExtra("phone", number)
            startActivity(intent)

        }
    }
}
