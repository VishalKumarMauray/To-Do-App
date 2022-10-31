package com.example.todoapp.Interface

interface ItemRowListener {
    fun modifyItemState(itemObjectId: String, isDone: Boolean)
    fun onItemDelete(itemObjectId: String)
    fun onItemEdit(itemObjectId: String)
}