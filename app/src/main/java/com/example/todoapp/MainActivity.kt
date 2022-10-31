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
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*


class MainActivity : AppCompatActivity(), ItemRowListener {

    lateinit var mDatabase: DatabaseReference
    var toDoItemList: MutableList<ToDoItem>? = null
    lateinit var adapter: ToDoItemAdapter
    private var listViewItems: ListView? = null
    lateinit var editText: EditText

    @SuppressLint("WrongViewCast", "CutPasteId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val fab = findViewById<View>(R.id.fab) as FloatingActionButton
        listViewItems = findViewById<View>(R.id.items_list) as ListView
        editText = findViewById<EditText?>(R.id.edittext)

        fab.setOnClickListener {

                val itemEditText = editText.text.toString()
                val todoItem = ToDoItem.create()
                todoItem.itemText = itemEditText
                todoItem.done = false
                val newItem = mDatabase.child(Constants.FIREBASE_ITEM).push()
                todoItem.objectId = newItem.key
                newItem.setValue(todoItem)
                Toast.makeText(this, "Item saved with ID " + todoItem.objectId, Toast.LENGTH_SHORT).show()
            }
        mDatabase = FirebaseDatabase.getInstance().reference
        toDoItemList = mutableListOf<ToDoItem>()
        adapter = ToDoItemAdapter(this, toDoItemList!!)
        listViewItems!!.setAdapter(adapter)

        var itemListener: ValueEventListener = object : ValueEventListener {
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
                val map = currentItem.getValue() as HashMap<String, Any>
                todoItem.objectId = currentItem.key
                todoItem.done = map.get("done") as Boolean?
                todoItem.itemText = map.get("itemText") as String?
                toDoItemList!!.add(todoItem);
            }
        }
        adapter.notifyDataSetChanged()
    }

    override fun modifyItemState(itemObjectId: String, isDone: Boolean) {
        val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
        itemReference.child("done").setValue(isDone);
    }

    override fun onItemDelete(itemObjectId: String) {
        val alert = AlertDialog.Builder(this)
        alert.setTitle("Delete")
        alert.setPositiveButton("Confirm") { dialog, positiveButton ->
            val itemReference = mDatabase.child(Constants.FIREBASE_ITEM).child(itemObjectId)
            itemReference.removeValue()
            dialog.dismiss()
            Toast.makeText(this, "Deleted Successfully", Toast.LENGTH_SHORT).show()
        }
        alert.show()
    }
}