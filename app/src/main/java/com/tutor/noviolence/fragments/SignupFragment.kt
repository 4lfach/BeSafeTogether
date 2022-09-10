package com.tutor.noviolence.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tutor.noviolence.R
import com.tutor.noviolence.db.RetrofitClient
import com.tutor.noviolence.models.response.DefaultResponseModel
import kotlinx.android.synthetic.main.fragment_signup.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class SignupFragment : Fragment(R.layout.fragment_signup) {
    private val TAG = "Signup Fragment"

    private lateinit var pDialog : ProgressDialog

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Progress dialog
        pDialog = ProgressDialog(requireContext())
        pDialog.setCancelable(false)

        tvLogIn.setOnClickListener{
            findNavController().popBackStack()
        }

        btnSignup.setOnClickListener {
            if(checkFieldsAreFilled()){

                val username = etUsername.text.toString()
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()

                registerUser(username, email, password)
            }
        }

    }

    private fun registerUser(username: String, email: String, password: String) {
        pDialog.setMessage("Registering ...")
        showDialog()

        val call : Call<DefaultResponseModel> = RetrofitClient.getInstance().getApi().createUser(username, email, password)
        call.enqueue(object: Callback<DefaultResponseModel>{
            override fun onResponse(
                call: Call<DefaultResponseModel>,
                response: Response<DefaultResponseModel>
            ) {
                if (response.code() == 201){
                    val dr = response.body()
                    Toast.makeText(requireContext(), dr!!.getMsg(), Toast.LENGTH_LONG).show()
                } else {
                    //TODO handle json response normally
                    val dr = response.errorBody()
                    Toast.makeText(requireContext(), "Email already exists or unexpected error", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<DefaultResponseModel>, t: Throwable) {

            }

        })

        /*call.enqueue(object: Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                var res : String? = null

                try {
                    res = if(response.code() == 201){
                        response.body()!!.string() as String
                    } else{
                        response.errorBody()!!.string() as String
                    }
                } catch (e : IOException){
                    e.printStackTrace()
                }

                if(res != null){
                    try {
                        val jObj : JSONObject = JSONObject(res)
                        var msg = jObj.getString("message")
                        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show()
                    }  catch (e : JSONException){
                        e.printStackTrace()
                    }

                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(requireContext(), t.message, Toast.LENGTH_LONG).show()
            }
        })*/
        hideDialog()
    }


    private fun checkFieldsAreFilled(): Boolean {
        var counter = 0;

        val username = etUsername.text.toString()
        val email = etEmail.text.toString()
        val password = etPassword.text.toString()
        val cPassword = etConfirmPassword.text.toString()

        if (username == ""){
            etUsername.setError("Username field is empty")
            counter++
        }
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
            etEmail.setError("Email is invalid\n")
            counter++
        }
        if (email == ""){
            etEmail.setError("Email field is empty")
            counter++
        }
        if (password == ""){
            etPassword.setError("Password field is empty")

            counter++
        }
        if (cPassword != password){
            etConfirmPassword.setError("Passwords do not match")
            counter++
        }

        if(counter != 0){
            return false
        }
        return true
    }

    private fun showDialog() {
        if (!pDialog.isShowing) pDialog.show()
    }

    private fun hideDialog() {
        if (pDialog.isShowing) pDialog.dismiss()
    }
}


