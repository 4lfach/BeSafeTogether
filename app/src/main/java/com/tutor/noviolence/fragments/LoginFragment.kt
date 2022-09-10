package com.tutor.noviolence.fragments

import android.app.ProgressDialog
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FetchPlaceResponse
import com.google.android.libraries.places.api.net.PlacesClient
import com.tutor.noviolence.R
import com.tutor.noviolence.db.RetrofitClient
import com.tutor.noviolence.models.*
import com.tutor.noviolence.models.adapters.CommentItemAdapter
import com.tutor.noviolence.models.response.CommentResponse
import com.tutor.noviolence.models.response.CommentsResponseModel
import com.tutor.noviolence.models.response.LoginResponseModel
import com.tutor.noviolence.services.SharedPrefManager
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.util.*
import kotlin.jvm.internal.MagicApiIntrinsics


class LoginFragment : Fragment(R.layout.fragment_login) {
    private val TAG = "Login fragment"

    private lateinit var pDialog : ProgressDialog
    lateinit var sharedPrefManager : SharedPrefManager
    lateinit var placesClient: PlacesClient
    lateinit var user : UserModel
    lateinit var comments : ArrayList<CommentModel>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPrefManager = SharedPrefManager.getInstance(requireContext())

        // Progress dialog
        pDialog = ProgressDialog(requireContext())
        pDialog.setCancelable(false)

        tvCreateAccount.setOnClickListener {
            val action = LoginFragmentDirections.actionLoginFragmentToSignupFragment()
            findNavController().navigate(action)
        }

        btnLogin.setOnClickListener {
            if(checkFieldsAreFilled()){
                val email = etEmail.text.toString()
                val password = etPassword.text.toString()
                loginToAccount(email, password)
            }
        }

        btnLogout.setOnClickListener{
            sharedPrefManager.clear()
            rlProfilePage.visibility = View.GONE
            rlLoginPage.visibility = View.VISIBLE
        }

        sharedPrefManager = SharedPrefManager.getInstance(requireContext())

        if(sharedPrefManager.isLoggedIn){
            rlProfilePage.visibility = View.VISIBLE
            rlLoginPage.visibility = View.GONE

            user = sharedPrefManager.user

            prepareProfilePage()

            CoroutineScope(Main).launch {
                delay(2000)
                rvComments.adapter = CommentItemAdapter(requireContext(), comments, this@LoginFragment)
                rvComments.layoutManager = LinearLayoutManager(requireContext())}
        }
    }


    private fun prepareProfilePage() {
        tvUsername.text = "Welcome Back, " + user.username

        CoroutineScope(Main).launch{
            fetchUserCommentsFromDb()
        }

    }

    private fun loginToAccount(email : String, password : String){
        pDialog.setMessage("Logging in...")
        showDialog()

        val call : Call<LoginResponseModel> = RetrofitClient.getInstance().api.loginUser(email, password)
        call.enqueue(object: Callback<LoginResponseModel> {
            override fun onResponse(
                call: Call<LoginResponseModel>,
                response: Response<LoginResponseModel>
            ) {
                val logRes : LoginResponseModel = response.body()!!

                if(!logRes.isError()){
                    user = logRes.getUser()!!
                    sharedPrefManager.saveUser(UserModel(user.id,user.email, user.username))

                    rlProfilePage.visibility = View.VISIBLE
                    rlLoginPage.visibility = View.GONE

                    prepareProfilePage()
                    Toast.makeText(requireContext(), logRes.getMessage(), Toast.LENGTH_LONG).show()

                } else{
                    Toast.makeText(requireContext(), logRes.getMessage(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<LoginResponseModel>, t: Throwable) {

            }

        })
        hideDialog()
    }

    private suspend fun fetchUserCommentsFromDb() {
        comments = ArrayList<CommentModel>()
        var commentResponseList: ArrayList<CommentResponse>

        var userId = user.id

        val call: Call<CommentsResponseModel> =
            RetrofitClient.getInstance().api.getCommentsByUserId(userId)

        call.enqueue(object : Callback<CommentsResponseModel> {
            override fun onResponse(
                call: Call<CommentsResponseModel>,
                response: Response<CommentsResponseModel>
            ) {
                CoroutineScope(IO).launch{
                    commentResponseList = response.body()!!.comments
                    for (cmtRes in commentResponseList) {

                        val id = cmtRes.id
                        val userId = cmtRes.user_id
                        val placeId = cmtRes.place_id
                        val content = cmtRes.content
                        val rating = cmtRes.rating
                        val date = convertStringToInstant(cmtRes.pub_date)

                        findPlaceById(placeId) { place ->
                            val cmt: CommentModel =
                                CommentModel(id, userId, content, place, rating, date)
                            comments.add(cmt)
                        }
                    }
                }
            }

            override fun onFailure(call: Call<CommentsResponseModel>, t: Throwable) {
            }

        })

    }

    private fun convertStringToInstant(date: String): Instant {
        val instant = Instant.parse(date)
        return instant
    }

    private fun findPlaceById(placeId: String, myCallBack: (result: Place) -> Unit){
        Places.initialize(activity?.applicationContext, "AIzaSyBpeIteMoHNdWs3C3vxVC09UtoR4OFP80g")
        //Create a new Place client instance
        placesClient = Places.createClient(requireContext())

        val placeFields = listOf(Place.Field.ID, Place.Field.NAME)
        val request = FetchPlaceRequest.newInstance(placeId, placeFields)

        placesClient.fetchPlace(request)
            .addOnSuccessListener { response: FetchPlaceResponse ->
                val place = response.place
                Log.i(TAG, "Place found: ${place.name}")

                myCallBack.invoke(place)
            }.addOnFailureListener { exception: Exception ->
                if (exception is ApiException) {
                    Log.e(TAG, "Place not found: ${exception.message}")
                    val statusCode = exception.statusCode
                }
            }
    }

    private fun checkFieldsAreFilled(): Boolean {
        var counter = 0

        val email = etEmail.text.toString()
        val password = etPassword.text.toString()

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

        if (counter != 0){
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

    public fun showCommentDetails(comment : CommentModel){
        //TODO navigate to a page with comment details
    }
}