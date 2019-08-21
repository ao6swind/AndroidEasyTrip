package com.easy.trip.activities

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.easy.trip.R
import com.easy.trip.databases.viewmodels.BookmarkVM
import com.easy.trip.models.Bookmark
import com.easy.trip.utilities.ImageUtils
import kotlinx.android.synthetic.main.activity_form.*

class FormActivity : AppCompatActivity() {

    private lateinit var bookmarkVM: BookmarkVM

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_form)

        bookmarkVM = ViewModelProviders.of(this).get(BookmarkVM::class.java)

        // 把MapActivity拋過來的資料擷取出來
        val bundle = intent.extras
        val bookmark = bundle!!.getParcelable<Bookmark>("bookmark")
        val byteArray = bundle!!.getByteArray("image")
        val isCreateMode = bundle!!.getBoolean("isCreateMode")

        // 把ByteArray轉成Bitmap
        val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray!!.size)

        // 把值塞到UI上
        txtName.setText(bookmark!!.name.toString())
        txtAddress.setText(bookmark!!.address.toString())
        txtPhone.setText(bookmark!!.phone.toString())
        txtComment.setText(bookmark!!.comment)
        imgPreview.setImageBitmap(bitmap)

        // 當Save按鈕被點擊時
        btnSave.setOnClickListener {
            when(isCreateMode){
                true -> {
                    ImageUtils.saveBitmapToFile(applicationContext, bitmap,"${bookmark.placeId}.png")
                    bookmarkVM.addBookmark(bookmark!!, txtComment.text.toString())
                    Toast.makeText(this, "create success", Toast.LENGTH_LONG).show()
                }
                false -> {
                    bookmarkVM.editBookmark(bookmark!!)
                    Toast.makeText(this, "update success", Toast.LENGTH_LONG).show()
                }
            }
            finish()
        }

        if(!isCreateMode)
            btnDelete.visibility = View.VISIBLE

        // 當Delete按鈕被點擊時
        btnDelete.setOnClickListener {
            val confirm = AlertDialog.Builder(this)
            confirm.setTitle("Confirm")
            confirm.setMessage("This record and photo will be removed after confirm.")
            confirm.setNegativeButton("Cancel", null)
            confirm.setPositiveButton("Confirm", { dialog, whichButton ->
                bookmarkVM.deleteBookmark(bookmark)
                ImageUtils.removeFile(applicationContext, "${bookmark.placeId}.png")
                finish()
            })
            confirm.create().show()
        }
    }
}
