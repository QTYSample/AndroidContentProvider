package com.qty.bookprovider

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView

class BookProviderActivity : AppCompatActivity(), IReportBack {

    private val providerTester = ProviderTester(this, this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        appendMenuItemText(item)
        when (item.itemId) {
            R.id.menu_da_clear -> emptyText()
            R.id.menu_add -> providerTester.addBook()
            R.id.menu_display_table -> providerTester.showBooks()
            R.id.menu_delete -> providerTester.removeBook()
            R.id.menu_update -> providerTester.updateBook()
        }
        return true
    }

    @SuppressLint("WrongViewCast")
    private fun getTextView(): TextView {
        return findViewById(R.id.text1)
    }

    private fun appendMenuItemText(menuItem: MenuItem) {
        val tv = getTextView()
        tv.text = String.format("%s\n%s", tv.text, menuItem.title)
    }

    private fun emptyText() {
        getTextView().text = ""
    }

    private fun appendText(s: String) {
        val tv = getTextView()
        tv.text = String.format("%s\n%s", tv.text, s)
    }

    override fun reportBack(tag: String, message: String) {
        appendText("$tag:$message")
        Log.d(TAG, message)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val st = savedInstanceState.getString("debugViewText") ?: return
        val tv = getTextView()
        tv.text = st
        Log.d(TAG, "Restored state")
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // save state
        outState.putString("debugViewText", getTextView().text.toString())
        Log.d(TAG, "Saved state")
    }

    companion object {
        const val TAG = "BookProvider"
    }
}