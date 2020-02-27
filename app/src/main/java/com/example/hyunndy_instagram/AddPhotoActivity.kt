package com.example.hyunndy_instagram

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.hyunndy_instagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.UploadTask
import kotlinx.android.synthetic.main.activity_add_photo.*
import java.text.SimpleDateFormat
import java.util.*

class AddPhotoActivity : AppCompatActivity() {

    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null

    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // Initiate storage
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        // Open the album
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        photoPickerIntent.type = "image/*"
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        // add Image upload event
        addphoto_btn_upload.setOnClickListener { view ->
            contentUpload()
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                // This is path to the selected imagee
                photoUri = data?.data
                addphoto_image.setImageURI(photoUri)
            } else {
                // Exit the addPhotoActivity if you leave the album without selecting it
                finish()
            }
        }
    }

    // 파일명 중복을 막기위한 함수
    fun contentUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE" + timestamp + "_.png"

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 이미지 업로드 방식
        // 1. Promise method ( 구글 권장)
        //----------------------------------------------------------------------------------------------------
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        }?.addOnSuccessListener { it ->

            // 이미지 업로드 완료되면 이미지 주소를 받아옴
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

                // INSERT downloadurl of image
                contentDTO.imageUrl = uri.toString()

                // insert uid of user
                contentDTO.uid = auth?.currentUser?.uid

                // insert userid
                contentDTO.userId = auth?.currentUser?.email

                // insert explain of content
                contentDTO.explain = addphoto_edit_explain.text.toString()

                // insert timestamp
                contentDTO.timestamp = System.currentTimeMillis()

                // images collection안에 ContentDTO 데이터를 넣는다.
                firestore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()
            }
        }
        //----------------------------------------------------------------------------------------------------

        // // 2. Callback method
        // //----------------------------------------------------------------------------------------------------
        // // DB에 입력해주는 코드를 작성
        // storageRef?.putFile(photoUri!!)?.addOnSuccessListener { it->

        //     // 이미지 업로드 완료되면 이미지 주소를 받아옴
        //     storageRef.downloadUrl.addOnSuccessListener { uri ->
        //         var contentDTO = ContentDTO()

        //         // INSERT downloadurl of image
        //         contentDTO.imageUrl = uri.toString()

        //         // insert uid of user
        //         contentDTO.uid = auth?.currentUser?.uid

        //         // insert userid
        //         contentDTO.userId = auth?.currentUser?.email

        //         // insert explain of content
        //         contentDTO.explain = addphoto_edit_explain.text.toString()

        //         // insert timestamp
        //         contentDTO.timestamp = System.currentTimeMillis()

        //         // images collection안에 ContentDTO 데이터를 넣는다.
        //         firestore?.collection("images")?.document()?.set(contentDTO)

        //         setResult(Activity.RESULT_OK)

        //         finish()
        //     }
        // }
    }
    //----------------------------------------------------------------------------------------------------
}
