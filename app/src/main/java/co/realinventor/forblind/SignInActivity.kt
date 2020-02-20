package co.realinventor.forblind

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class SignInActivity : AppCompatActivity() {
    lateinit var editTextPhone : TextInputEditText
    lateinit var editTextNameRegister : TextInputEditText
    lateinit var buttonContinue : MaterialButton
    val TAG = "SignInActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        editTextPhone = findViewById(R.id.editTextPhoneNumber)
        editTextNameRegister = findViewById(R.id.editTextNameRegister)
        buttonContinue = findViewById(R.id.buttonContinuePhone)

        buttonContinue.setOnClickListener {
            Log.d(TAG, "Continue Button clicked")
            val number = editTextPhone.text.toString()
            val name = editTextNameRegister.text.toString()

            if(TextUtils.isEmpty(name)){
                editTextNameRegister.error = "Please enter a name!"
                return@setOnClickListener
            }


            if(number.length < 10){
//                Toast.makeText(this@SignInActivity, "Enter a valid phone number!", Toast.LENGTH_SHORT).show()
                editTextPhone.error = "Please enter a valid 10 digit phone number!"
                return@setOnClickListener
            }

            var intent = Intent(this@SignInActivity, VerifyActivity::class.java)
            intent.putExtra("phone", number)
            intent.putExtra("name", name)
            startActivity(intent)

        }
    }
}
