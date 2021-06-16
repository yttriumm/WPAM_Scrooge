package com.wpam.scrooge.fragments

import androidx.appcompat.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore
import com.wpam.scrooge.R
import com.wpam.scrooge.Receipt
import com.wpam.scrooge.ReceiptEntry

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ReceiptDetailsFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ReceiptDetailsFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var peopleDueList: LinearLayout
    private lateinit var receiptEntryList: LinearLayout
    private lateinit var deleteButton: Button
    private lateinit var receipt: Receipt

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

            try { arguments?.let {
                receipt = it.getSerializable("receipt") as Receipt
                receipt.printAttributes()
            }
            }
        catch(e:Exception) {println(e.message)}
        }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_receipt_details, container, false)
    }

    companion object {


        @JvmStatic
        fun newInstance(receipt: Receipt) =
                ReceiptDetailsFragment().apply {
                    arguments = Bundle().apply {
                        putSerializable("receipt", receipt)
                    }
                }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        try {
            super.onViewCreated(view, savedInstanceState)
            peopleDueList = view.findViewById(R.id.receiptDetailsPeopleDue)
            receiptEntryList = view.findViewById(R.id.receiptDetailsEntryRow)

            val receiptName: TextView = view.findViewById(R.id.receiptDetailsTextViewReceiptName)
            val receiptTotal: TextView = view.findViewById(R.id.receiptDetailsTextViewTotal)
            val receiptPeople: TextView = view.findViewById(R.id.receiptDetailsTextViewPeople)
            val receiptDate: TextView = view.findViewById(R.id.receiptDetailsReceiptDate)
            deleteButton = view.findViewById(R.id.receiptDetailsDeleteButton)

            receiptName.text = receipt.name
            receiptTotal.text = "${String.format("%.2f", receipt.getTotalValue())} zł"
            receiptPeople.text = receipt.people.joinToString(", ")
            receiptDate.text = receipt.date
            receiptEntryList.removeAllViews()
            for (entry in receipt.entries.values) {
                addEntry(entry)
            }

            peopleDueList.removeAllViews()
            val peopleDue = receipt.getPeopleDue()
            for ((person, amount) in peopleDue) {
                addPersonDue(person, amount)
            }

            deleteButton.setOnClickListener {
                val deleteConfirmDialog: AlertDialog.Builder = AlertDialog.Builder(view.context)
                deleteConfirmDialog.setTitle("Usuń paragon")
                deleteConfirmDialog.setMessage("Czy na pewno chcesz usunąć ten paragon?")
                deleteConfirmDialog.setPositiveButton("Tak") { _, _ ->
                    deleteReceipt()
                }
                deleteConfirmDialog.setNegativeButton("Nie") {_, _ ->
                }
                deleteConfirmDialog.create().show()
            }
        }
            catch(e : Exception) { e.printStackTrace() }
        }

        fun addPersonDue(name: String, value: Double) {
            val personDue: View = layoutInflater.inflate(R.layout.fragment_receipt_details_people_due, null, false)
            val personDueName: TextView = personDue.findViewById(R.id.receiptDetailsPersonDueName)
            val personDueAmount: TextView = personDue.findViewById(R.id.receiptDetailsPersonDueAmount)
            personDueName.setText(name)
            personDueAmount.setText("${String.format("%.2f", value)} zł")
            peopleDueList.addView(personDue)
        }

        fun addEntry(entry: ReceiptEntry) {
            val receiptEntry: View = layoutInflater.inflate(R.layout.fragment_receipt_details_entry_row, null, false)
            val entryName: TextView = receiptEntry.findViewById(R.id.receiptDetailsEntryName)
            val entryValue: TextView = receiptEntry.findViewById(R.id.receiptDetailsEntryValue)
            val entryPeople: TextView = receiptEntry.findViewById(R.id.receiptDetailsEntryPeople)

            entryName.setText(entry.name)
            entryValue.setText("${String.format("%.2f", entry.value)} zł")
            entryPeople.setText(entry.people.joinToString(", "))
            receiptEntryList.addView(receiptEntry)
        }

    fun deleteReceipt() {
        val db = FirebaseFirestore.getInstance()
        db.collection("Receipts").document(receipt.id).delete().addOnSuccessListener {
            Toast.makeText(activity!!, "Usunięto paragon", Toast.LENGTH_SHORT).show()
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, OverviewFragment())
            transaction.disallowAddToBackStack()
            transaction.commit()
        }.addOnFailureListener {
            Toast.makeText(activity!!, "Nie można usunąć paragonu.", Toast.LENGTH_SHORT).show()
        }
    }


}