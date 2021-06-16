package com.wpam.scrooge.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObjects
import com.wpam.scrooge.R
import com.wpam.scrooge.Receipt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [OverviewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class OverviewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    val receipts: MutableList<Receipt> = mutableListOf()
    lateinit var receiptNoReceiptTextView: TextView
    lateinit var receiptYourReceiptsTextView: TextView
    lateinit var receiptsList: LinearLayout

    val currentReceipt = Receipt()


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
        return inflater.inflate(R.layout.fragment_overview, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment OverviewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            OverviewFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val db = FirebaseFirestore.getInstance()
        val uid = activity!!.intent.getStringExtra("uid")
        val usernameLabel: TextView = view.findViewById(R.id.dashboardTextViewHelloName)
        receiptsList = view.findViewById(R.id.overviewReceiptsList)
        receiptNoReceiptTextView = view.findViewById(R.id.overviewNoReceiptTextView)
        receiptYourReceiptsTextView = view.findViewById(R.id.overviewTextViewYourReceipts)

        var user: MutableMap<String, Any> = HashMap()
        db.collection("Users")
            .whereEqualTo("uid", uid)
            .get()
            .addOnSuccessListener { task ->
                for (document in task.documents) {
                    user = document.data!!.toMutableMap()
                    usernameLabel.text = user["name"].toString()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(context, exception.toString(), Toast.LENGTH_SHORT).show()
            }


        db.collection("Receipts").whereEqualTo("uid",uid).get().addOnSuccessListener {

            val receipts: MutableList<Receipt> = mutableListOf()
            for (document in it.documents) {
                val map = document.data
                try {
                    val receipt = Receipt()
                    receipt.fromHashMap(map as HashMap<String, Any>)
                    receipts.add(receipt)
                    receipt.id = document.id.toString()
                    receipt.printAttributes()
                }
                catch(e: Exception) {
                    println(e.message)

                }
            }

            receiptsList.removeAllViews()
            receipts.sortByDescending { it.date }
            for(receipt in receipts) {
                try {

                    addReceipt(receipt)
                }
                catch(e: Exception) {println(e.message)}
            }
            if(receipts.isEmpty()) {
                receiptNoReceiptTextView.setText("Nie masz żadnych paragonów.")
            }


        }.addOnFailureListener{
            println(it.message)
        }

    }

    fun addReceipt(receipt: Receipt) {

        val receiptRow: View = layoutInflater.inflate(R.layout.list_row_receipt, null, false)
        val receiptNameTextView: TextView = receiptRow.findViewById(R.id.receiptRowReceiptTitle)
        val receiptValueTextView: TextView = receiptRow.findViewById(R.id.receiptRowReceiptValue)
        val receiptDateTextView: TextView = receiptRow.findViewById(R.id.receiptRowReceiptDate)
        val receiptDetailsButton: TextView = receiptRow.findViewById(R.id.receiptRowDetailsButton)

        receiptNameTextView.setText(receipt.name)
        receiptDateTextView.setText(receipt.date)
        receiptValueTextView.setText(String.format("%.2f",receipt.getTotalValue())+ " zł")


            receiptDetailsButton.setOnClickListener {
                try {
                    val fragment = ReceiptDetailsFragment.newInstance(receipt)
                    val transaction = activity!!.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, fragment)
                    transaction.disallowAddToBackStack()
                    transaction.commit()
                }
                catch(e: Exception){
                    println(e.message)
                }
            }

            receiptsList.addView(receiptRow)




    }
}