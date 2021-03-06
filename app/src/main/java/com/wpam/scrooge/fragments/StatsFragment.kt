package com.wpam.scrooge.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.wpam.scrooge.R

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [StatsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class StatsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_stats, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment StatsFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            StatsFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val newPasswordEditText: EditText = view.findViewById(R.id.paswordChangeEditTextNewPassword)
        val confirmPasswordEditText: EditText=view.findViewById(R.id.passwordChangeEditTextConfirmPassword)
        val changePasswordButton: Button = view.findViewById(R.id.changePasswordButtonConfirm)

        changePasswordButton.setOnClickListener {
            val newPass = newPasswordEditText.text.toString()
            val confPass = confirmPasswordEditText.text.toString()
            if(newPass.length < 6) {
                Toast.makeText(activity!!, "Has??o musi by?? d??u??sze ni?? 6 znak??w.", Toast.LENGTH_SHORT)
                        .show()
            }
            else if (newPass != confPass) {
                Toast.makeText(activity!!, "Has??a musz?? si?? zgadza??.", Toast.LENGTH_SHORT)
                        .show()
            }
            else if (newPass == confPass) {
                val user: FirebaseUser = FirebaseAuth.getInstance().currentUser!!
                user.updatePassword(newPass).addOnSuccessListener {
                    val transaction = activity!!.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, OverviewFragment())
                    transaction.disallowAddToBackStack()
                    transaction.commit()
                    Toast.makeText(activity!!, "Zmieniono has??o.", Toast.LENGTH_SHORT)
                            .show()
                }.addOnFailureListener {
                    Toast.makeText(activity!!, "Nie uda??o si?? zmieni?? has??a.", Toast.LENGTH_SHORT)
                            .show()
                }

            }
        }

    }
}