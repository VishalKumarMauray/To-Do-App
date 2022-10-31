package com.example.todoapp

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.todoapp.Adapter.ToDoItemAdapter
import com.example.todoapp.Class.ToDoItem
import com.example.todoapp.Interface.ItemRowListener
import com.example.todoapp.Object.Constants
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*


class MainActivity : AppCompatActivity(), ItemRowListener {

    private lateinit var mDatabase: DatabaseReference
    private var toDoItemList: MutableList<ToDoItem>? = null
    private lateinit var adapter: ToDoItemAdapter
    private var listViewItems: ListView? = null
    private lateinit var editText: EditText

    @SuppressLint("WrongViewCast", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        listViewItems = findViewById<View>(R.id.items_list) as ListView
        editText = findViewById(R.id.edittext)

        fab.setOnClickListener {

            val itemEditText = editText.text.toString()
            val todoItem = ToDoItem.create()
            todoItem.itemText = itemEditText
            todoItem.done = false
            val newItem = mDatabase.child(Constants.FIREBASE_ITEM).push()
            todoItem.objectId = newItem.key
            newItem.setValue(todoItem)
            Toast.makeText(this, "Saved Successfully ", Toast.LENGTH_SHORT).show()
            editText.setText("")
            finish()
            overridePendingTransition(0,0)
            startActivity(intent)
            overridePendingTransition(0,0)
        }
        mDatabase = FirebaseDatabase.getInstance().reference
        toDoItemList = mutableListOf()
        adapter = ToDoItemAdapter(this, toDoItemList!!)
        listViewItems!!.adapter = adapter

        val itemListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                addDataToList(dataSnapshot)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("MainActivity", "loadItem:onCancelled", databaseError.toException())
            }
        }
        mDatabase.orderByKey().addListenerForSingleValueEvent(itemListener)
    }


    private fun addDataToList(dataSnapshot: DataSnapshot) {
        val items = dataSnapshot.children.iterator()
        if (items.hasNext()) {
            val toDoListindex = items.next()
            val itemsIterator = toDoListindex.children.iterator()


            while (itemsIterator.hasNext()) {
                val currentItem = itemsIterator.next()
                val todoItem = ToDoItem.create()
                val map = currentItem.value as HashMap<*, *>
                todoItem.objectId = currentItem.key
                todoItem.done = map["done"] as Boolean?
                todoItem.itemText = map["itemText"] as String?
                toDoItemList!!.add(todoItem)
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun modifyItemState(itemObjectId: String, isDone: Boolean) {
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        itemReference.child("done").setValue(isDone)
    }

    override fun onItemDelete(itemObjectId: String) {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Delete Item")
        alert.setPositiveButton("Confirm") { dialog, _ ->
            val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
            itemReference.removeValue()
            dialog.dismiss()
            Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
        }
        alert.show()
    }

    override fun onItemEdit(itemObjectId: String) {
        val alert = AlertDialog.Builder(this)
        val itemEditText = EditText(this)
        alert.setTitle("update Item")
        alert.setView(itemEditText)
        alert.setPositiveButton("Submit") { dialog, _ ->
            val todoItem = ToDoItem.create()
            todoItem.itemText = itemEditText.text.toString()
            val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
            todoItem.objectId = itemReference.toString()
            itemReference.setValue(todoItem)
            dialog.dismiss()
            Toast.makeText(this, "Update successfully ", Toast.LENGTH_SHORT).show()
        }
        alert.show()
    }
}