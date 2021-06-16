package com.wpam.scrooge.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
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
 * Use the [NewFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class NewFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null


    private lateinit var personList: LinearLayout
    private lateinit var entriesList: LinearLayout
    private lateinit var receiptName : EditText
    private lateinit var overViewList: LinearLayout
    private lateinit var receiptEntries: HashMap<Int, ReceiptEntry>
    private lateinit var receiptPeople: HashMap<Int, String>
    private var receiptPeopleIdCounter = 0
    private var receiptEntriesIdCounter = 0
    private lateinit var  receipt: Receipt

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
        return inflater.inflate(R.layout.fragment_new, container, false)
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment NewFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            NewFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        personList = view.findViewById(R.id.newReceiptPeopleList)
        entriesList = view.findViewById(R.id.newReceiptEntry)
        receiptName = view.findViewById(R.id.receiptTitleEditText)
        overViewList = view.findViewById(R.id.newReceiptOverwiew)
        receiptEntries = hashMapOf()
        receiptPeople = hashMapOf()
        receipt = Receipt()
        receiptName.setHint("Tytuł...")
        val addPersonButton: Button = view.findViewById(R.id.newPersonButton)
        val addEntryButton: Button = view.findViewById(R.id.newReceiptEntryAddButton)
        val testButton: Button = view.findViewById(R.id.newReceiptButtonSend)
        addPersonButton.setOnClickListener {
            addPersonRow(it)
        }

        addEntryButton.setOnClickListener {
            addReceiptEntry(it)
        }

        testButton.setOnClickListener {
            sendReceiptToDatabase()
        }

        receiptName.doAfterTextChanged {
            try {
                val newName = receiptName.text.toString()
                if(newName.length > 30) {
                    throw Exception("Too long receipt name")
                    createOverViewLayout()
                }
                else {
                    receipt.name = newName
                    createOverViewLayout()
                    setBackgroundToTransparent(receiptName)
                }
            }
            catch(e: Exception) {
                receiptName.setBackgroundColor(Color.parseColor("#ffcccb"))
                setBackgroundToError(receiptName)
            }
        }
    }



    fun addPersonRow(view: View) {

        val personRow: View = layoutInflater.inflate(R.layout.row_add_person, null, false)
        val personName: EditText = personRow.findViewById(R.id.add_person_name)
        val personDelete: ImageView = personRow.findViewById(R.id.person_remove)
        val person_id = receiptPeopleIdCounter
        receiptPeopleIdCounter += 1
        receiptPeople.put(person_id,"Niepodpisana osoba")

        personDelete.setOnClickListener {

            removePersonRow(personRow, person_id)
            createOverViewLayout()

        }

        personName.doAfterTextChanged {
            try {
                if(personName.text.toString().length > 30) {
                    throw Exception("Too long person name")
                }
                else {
                    refreshPeopleInEntries(person_id, personName.text.toString())
                    refreshPeopleInReceipt(person_id, personName.text.toString())
                    receiptPeople[person_id] = personName.text.toString()
                    setBackgroundToTransparent(personName)
                    createOverViewLayout()
                }
            }
            catch(e: Exception) {
                setBackgroundToError(personName)
            }
        }


        personList.addView(personRow, 1)
        println("ADDED PERSON ID ${person_id}")
    }

    fun removePersonRow(view: View, id: Int) {
        refreshPeopleInEntries(id, "", true)
        refreshPeopleInReceipt(id, "", true)
        receiptPeople.remove(id)
        personList.removeView(view)
        println("REMOVED PERSON ${id}")
        for(entry in receiptEntries.values) {
            entry.printAttributes()
        }
    }

    fun addReceiptEntry(view: View) {

        val receiptEntry: View = layoutInflater.inflate(R.layout.row_add_receipt_entry, null, false)
        val entryName: EditText = receiptEntry.findViewById(R.id.add_receipt_entry_name)
        val entryValue: EditText = receiptEntry.findViewById(R.id.add_receipt_entry_value)
        val entryPeople: TextView = receiptEntry.findViewById(R.id.receiptEntryPeople)
        val entryDelete: ImageView = receiptEntry.findViewById(R.id.person_remove)

        val entry_id = receiptEntriesIdCounter
        receiptEntriesIdCounter += 1

        receiptEntries.put(entry_id, ReceiptEntry(id=entry_id))

        entryDelete.setOnClickListener {
            removeReceiptEntry(receiptEntry, entry_id)

        }

        entryPeople.setOnClickListener {
            showDialog(entryPeople, entry_id)
            Log.d("Receipt entry:", "Tryna show dialog")
        }

        entryValue.doAfterTextChanged {
            try {
                val newValue = entryValue.text.toString().toDouble()
                if (newValue < 0) {
                    updateReceiptEntry(id = entry_id, newValue = 0.0, editValue = true)
                    throw Exception("Number cannot be negative")}
                else {
                    updateReceiptEntry(id = entry_id, newValue = newValue, editValue = true)
                    setBackgroundToTransparent(entryValue)
                    createOverViewLayout()
                }
            }
            catch(e: Exception) {
                setBackgroundToError(entryValue)
            }
        }

        entryName.doAfterTextChanged {
            try {
                val newName = entryName.text.toString()
                if (newName.length > 30) throw Exception("Too long name")
                else {
                    updateReceiptEntry(id = entry_id, newName = newName, editName = true)
                    setBackgroundToTransparent(entryName)
                    createOverViewLayout()

                }
            }
            catch(e: Exception) {
                setBackgroundToError(entryName)
            }
        }

        entriesList.addView(receiptEntry, 1)
        Log.d("Receipt entry:" ,"added receipt entry id $entry_id")
    }




    fun removeReceiptEntry(view: View, id: Int) {
        entriesList.removeView(view)
        receiptEntries.remove(id)
        createOverViewLayout()
        Log.d("Receipt entry:" ,"Removed receipt entry id $id")
    }


    private fun showDialog(view: TextView, entry_id: Int){
        // Late initialize an alert dialog object
        lateinit var dialog:AlertDialog
        Log.d("Dialog:" ,"breakpoint 1")
        // Initialize an array of colors
        val arrayPeople = receiptPeople.values.toTypedArray()

        // Initialize a boolean array of checked items
        val listChecked: MutableList<Boolean> = mutableListOf()

        for(i in arrayPeople) {
            listChecked.add(false)
        }

        val arrayChecked = listChecked.toBooleanArray()
        Log.d("Dialog:" ,"breakpoint 2")

        // Initialize a new instance of alert dialog builder object
        val builder = AlertDialog.Builder(activity!!)

        // Set a title for alert dialog
        builder.setTitle("Wybierz osoby, które się składają")

        Log.d("Dialog:" ,"breakpoint 2")
        // Define multiple choice items for alert dialog
        builder.setMultiChoiceItems(arrayPeople, arrayChecked) { dialog, which, isChecked->
            // Update the clicked item checked status
            arrayChecked[which] = isChecked

            // Get the clicked item
            val person = arrayPeople[which]

            // Display the clicked item text

        }

        Log.d("Dialog:" ,"breakpoint 3")
        // Set the positive/yes button click listener
        builder.setPositiveButton("OK") { _, _ ->
            // Do something when click positive button

            val checkedPeople: MutableList<String> = mutableListOf()
            for (i in 0 until arrayPeople.size) {
                val checked = arrayChecked[i]
                if (checked) {
                    checkedPeople.add(arrayPeople[i])
                }
            }
            if(checkedPeople.size > 0){
                view.text = checkedPeople.joinToString(", ")
                receiptEntries[entry_id]!!.people = checkedPeople
                createOverViewLayout()

        }
            else {
                receiptEntries[entry_id]!!.people = checkedPeople
                createOverViewLayout()
            }

    }
        Log.d("Dialog:" ,"breakpoint 4")

        // Initialize the AlertDialog using builder object
        dialog = builder.create()

        // Finally, display the alert dialog
        dialog.show()

}

    fun refreshPeopleInEntries(id: Int, newValue: String, delete:Boolean=false) {
        for(child in entriesList.children) {
            if(child is LinearLayout) {
                val view : LinearLayout = child
                for(constraintLayout in view.children) {
                    if(constraintLayout is ConstraintLayout) {
                            try {
                                val peopleTextView: TextView =
                                    constraintLayout.findViewById(R.id.receiptEntryPeople)
                                val peopleInText = peopleTextView.text.split(", ").toMutableList()
                                val peopleInText_temp = peopleInText.toMutableList()
                                for(i in 0 until peopleInText_temp.size) {
                                    if(peopleInText_temp[i] == receiptPeople[id]) {
                                        if (delete == false) peopleInText[i] = newValue
                                        if (delete == true) peopleInText.removeAt(i)
                                        break
                                    }

                                }

                                peopleTextView.text = peopleInText.joinToString(", ")
                                println("PEOPLE IN THIS ENTRY: ${peopleInText.joinToString(", ")}")
                            }
                            catch(e: Exception) {
                                println(e.message)
                            }

                    }
                }
            }
        }
    }

    fun refreshPeopleInReceipt(id: Int, newValue:String, delete:Boolean=false) {
        try {
            for (entry in receiptEntries) {

                val people = entry.value.people.toMutableList()
                    for (i in 0 until entry.value.people.size) {

                        if(people[i] == receiptPeople[id]) {
                            if (delete == false) people[i] = newValue
                            if (delete == true) people.removeAt(i)
                            println("PEOPLE IN AFFECTED ENTRY: ${people.joinToString(", ")}")

                            break
                        }
                        entry.value.people = people

                    }
            }
            println("PEOPLE IN RECEIPT OVERALL: ${receiptPeople.values.joinToString(", ")}")

        }
        catch(e: Exception) {println(e.message)}
    }

    fun updateReceiptEntry(id: Int=0, newValue: Double=0.0,
                           newPeople: MutableList<String> = mutableListOf(), newName: String = "",
                           editValue: Boolean = false, editName: Boolean = false,
                           editPeople:Boolean = false) {
        when {
            editValue -> {receiptEntries[id]!!.value = newValue}
            editName -> {receiptEntries[id]!!.name = newName}
            editPeople -> {receiptEntries[id]!!.people = newPeople}
        }

    }

    fun sendReceiptToDatabase() {

        try {
            for(receiptEntry in receiptEntries.values) {
                receipt.entries.put(receiptEntry.id, receiptEntry)
            }
            val uid = activity!!.intent.getStringExtra("uid")
            receipt.uid = uid!!
            receipt.people = receiptPeople.values.toMutableList()
            println(receipt.people)
        }
        catch(e: Exception) {println(e.message)}

        receipt.printAttributes()

        try {
            val db = FirebaseFirestore.getInstance().collection("Receipts")
            db.add(receipt.toHashMap())
                .addOnSuccessListener {
                    val transaction = activity!!.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.fragment_container, OverviewFragment())
                    transaction.disallowAddToBackStack()
                    transaction.commit()
                    Toast.makeText(activity!!, "Dodano paragon do bazy.", Toast.LENGTH_SHORT).show()

                }
                .addOnFailureListener {
                    Toast.makeText(
                        activity!!,
                        "Wystąpił problem podczas dodawania paragonu do bazy: ${it.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
        catch(e: Exception) {println(e.message)}


    }



    fun createOverViewLayout() {
        try {
            overViewList.removeAllViews()
            var total: Double = 0.0
            for (entry in receiptEntries.values) {
                total += entry.value
            }
            val totalText =
                "<big><big><big><b>Suma:</b></big></big><big> <font color=#006600>${Html.escapeHtml(String. format("%.2f", total))} zł</font>"
            val totalTextView = TextView(context)
            totalTextView.setText(Html.fromHtml(totalText, Html.FROM_HTML_MODE_LEGACY))
            overViewList.addView(totalTextView)
            val peopleDues: MutableMap<String, Double> = hashMapOf()
            for (person in receiptPeople.values) {
                peopleDues.put(person, 0.0)
                for (entry in receiptEntries.values) {
                    if (person in entry.people) {
                        peopleDues[person] =
                            (peopleDues[person] ?: 0.0) + entry.value / entry.people.size
                    }
                }
                val personTextView = TextView(context)
                val personText = "${Html.escapeHtml(person)}: <font color=#006600>${Html.escapeHtml(( String. format("%.2f", peopleDues[person])))} zł</font>"
                personTextView.setText(Html.fromHtml(personText, Html.FROM_HTML_MODE_LEGACY))
                overViewList.addView(personTextView)
            }
        }
        catch (e: Exception) {println(e.message)}
    }




    fun setBackgroundToError(e: EditText) {
        e.setBackgroundColor(Color.parseColor("#ffcccb"))
    }
    fun setBackgroundToTransparent(e:EditText) {
        e.setBackgroundColor(Color.TRANSPARENT)
    }

    }


