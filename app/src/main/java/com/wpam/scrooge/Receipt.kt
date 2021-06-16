package com.wpam.scrooge

import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap


class ReceiptEntry(id: Int = 0,name: String="Nazwa", value: Double=0.0, people: MutableList<String> = mutableListOf<String>()) : Serializable {
    var name = name
    var value = value
    var people = people
    var id = id

    fun printAttributes() {
        println("---------")
        println("recipt entry ID $id")
        println("name: $name")
        println("value: $value")
        println("people:")
        for(person in people) {
            println("\t $person")
        }
        println("--------")

    }

    fun toHashMap(): HashMap<String, Any> {
        val hashMap: HashMap<String, Any> = HashMap()
        hashMap.put("name", name)
        hashMap.put("value", value)
        hashMap.put("people", people)
        return hashMap
    }

    fun fromHashMap(map: HashMap<String,Any>) {
        name = map.get("name").toString()
        people = map.get("people") as MutableList<String>
        value = map.get("value").toString().toDouble()
    }
}

class Receipt(uid: String="Undefined", people:MutableList<String> = mutableListOf(),
              name: String="Tytuł", entries: HashMap<Int,
            ReceiptEntry> = hashMapOf(), date: String = getCurrentDateTime().toString("yyyy/MM/dd"), id:String="Undefined") : Serializable {
    var name = name
    var entries = entries
    var uid = uid
    var people = people
    var date = date
    var id = id


    fun printAttributes() {
        println("###### RECEIPT NAME: $name")
        println("###### RECEIPT DATE: $date")
        println("###### RECEIPT ID: $id")
        for(entry in entries.values) {
            entry.printAttributes()
        }


    }

    fun toHashMap(): HashMap<String, Any> {
        val hashMap: HashMap<String,Any> = HashMap()
        hashMap.put("uid", uid)
        hashMap.put("name", name)
        hashMap.put("date", date)
        hashMap.put("people",people)

        val entriesList: MutableList<HashMap<String,Any>> = mutableListOf()
        for(entry in entries.values) {
            entriesList.add(entry.toHashMap())
        }
        hashMap.put("entries", entriesList)

       return hashMap
    }

    fun fromHashMap(map: HashMap<String,Any>) {
        name = map.get("name").toString()
        date = map.get("date").toString()
        people = map.get("people") as MutableList<String>
        val mapEntries = map.get("entries") as MutableList<HashMap<String, Any>>
        var i = 0
        for (mapEntry in mapEntries) {
            val entry = ReceiptEntry()
            entry.fromHashMap(mapEntry)
            entries.put(i, entry)
            i++
        }

    }

    fun getTotalValue() : Double {
        var totalValue = 0.0
        for(entry in entries.values) {
            totalValue += entry.value

        }
        return totalValue
    }

    fun getPeopleDue(): MutableMap<String,Double> {
        val peopleDue = mutableMapOf<String,Double>()

        for(person in people) {
            peopleDue[person] = 0.0
            for (entry in entries.values) {
                if (person in entry.people) {
                    peopleDue[person] = (peopleDue[person] ?: 0.0) + entry.value / entry.people.size
                }
            }
        }

        return peopleDue
    }

}

fun Date.toString(format: String, locale: Locale = Locale.getDefault()): String {
    val formatter = SimpleDateFormat(format, locale)
    return formatter.format(this)
}

fun getCurrentDateTime(): Date {
    return Calendar.getInstance().time
}