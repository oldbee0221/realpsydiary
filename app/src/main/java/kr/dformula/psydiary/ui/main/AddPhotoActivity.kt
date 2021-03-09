package kr.dformula.psydiary.ui.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_add_photo.*
import kr.dformula.psydiary.R
import kr.dformula.psydiary.model.ContentDTO
import kr.dformula.psydiary.ui.CommentActivity
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var firestore: FirebaseFirestore? = null

    var category_title :String="category01"
    var category_sub_title : String="category02"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_photo)

        // 초기화
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()


        val toolbar = findViewById(R.id.tb_post) as Toolbar
        setSupportActionBar(toolbar)
        val ab = supportActionBar!!
        ab.setDisplayShowTitleEnabled(false)
        ab.setDisplayHomeAsUpEnabled(true)

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);



        //첫번째 스피너 생성 부분
        val spinnerTitle = findViewById(R.id.spinner_title) as Spinner
        var spinnerTitleData = resources.getStringArray(R.array.spinner_main);
        var adapter =ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,spinnerTitleData)
        spinnerTitle.adapter=adapter
        category_title=spinnerTitleData[0]


        //두번째 스피너 생성 부분
        val spinnerSubTitle=findViewById(R.id.spinner_sub_title) as Spinner
        var spinnerSubTitleDataList = ArrayList<Int>()
        spinnerSubTitleDataList.add(R.array.speech_therapy_story)
        spinnerSubTitleDataList.add(R.array.turtle_mom)
        spinnerSubTitleDataList.add(R.array.rabbitmom)
        spinnerSubTitleDataList.add(R.array.review_ceter)

        var spinnerSubTitleData = ArrayList<String>()
        spinnerSubTitleData.addAll(resources.getStringArray(spinnerSubTitleDataList.get(0)))
        category_sub_title=spinnerSubTitleData.get(0);

        var subTitleAdapter = ArrayAdapter(this,android.R.layout.simple_list_item_1,spinnerSubTitleData)
        spinnerSubTitle.adapter=subTitleAdapter

        //스피너 리스너
        spinnerTitle.onItemSelectedListener = object :AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerSubTitleData.clear()
                spinnerSubTitleData.addAll(resources.getStringArray(spinnerSubTitleDataList.get(position)))
                category_title=spinnerTitleData[position]
                category_sub_title=spinnerSubTitleData.get(0)
                subTitleAdapter.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }



        spinnerSubTitle.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                category_sub_title=spinnerSubTitleData.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {

            }
        }


        add_photo_button.setOnClickListener { contentUpload() }
        // 업로드 버튼에 contentUpload 연결

        post_camera.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)
        }



        add_photo_edit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

                if (add_photo_edit.length() > 0) {
                    add_photo_button.setClickable(true);
                    add_photo_button.setTextColor(Color.BLACK);
                } else {
                    add_photo_button.setClickable(false);
                    add_photo_button.setTextColor(Color.GRAY);
                }
            }


        })


    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val id = item.itemId
        when (id) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                // 이미지를 선택 했을 때
                photoUri = data?.data
                with(add_photo_image) { add_photo_image.setImageURI(photoUri) }
                // 이미지 화면에 선택된 이미지 불러오기
            } else {
                Toast.makeText(this, "사진선택 취소", Toast.LENGTH_LONG ).show();
            }
        }
    }

    fun contentUpload() {
        var timestamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        var imageFileName = "IMAGE_" + timestamp + "_.png"
        // 이미지 이름을 현재시간으로 정해줘서 중복 방지

        var storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 이미지 업로드
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener {
            Toast.makeText(this, getString(R.string.upload_success), Toast.LENGTH_SHORT).show()


            // 이미지 주소 받아오기
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()
                var ContentDTO1 = Intent(this, CommentActivity::class.java)

                //카테고리 넣어주기
                contentDTO.category_title=category_title
                contentDTO.category_sub = category_sub_title

                // 이미지 주소 넣어주기
                contentDTO.imageUrl = uri.toString()

                //유저 uid 넣어주기
                contentDTO.uid = auth?.currentUser?.uid

                // 유저 아이디 넣어주기
                contentDTO.userId = auth?.currentUser?.email

                // 설명 넣어주기
                contentDTO.explain = add_photo_edit.text.toString()
                ContentDTO1.putExtra("explain", add_photo_edit.text.toString())

                contentDTO.explain1 = add_photo_edit1.text.toString()
                ContentDTO1.putExtra("explain1", add_photo_edit1.text.toString())


                // 타임스태프 넣어주기
                contentDTO.timestamp = System.currentTimeMillis()

                // 값 넘겨주기
                firestore?.collection("images")?.document()?.set(contentDTO)


                setResult(Activity.RESULT_OK)
                finish()
            }
        }
    }


    fun contentUpload1() {

        var contentDTO = ContentDTO()

        //유저 uid 넣어주기
        contentDTO.uid = auth?.currentUser?.uid

        // 유저 아이디 넣어주기
        contentDTO.userId = auth?.currentUser?.email

        // 설명 넣어주기
        contentDTO.explain = add_photo_edit.text.toString()

        contentDTO.explain1 = add_photo_edit1.text.toString()

        // 타임스태프 넣어주기
        contentDTO.timestamp = System.currentTimeMillis()

        // 값 넘겨주기
        firestore?.collection("images")?.document()?.set(contentDTO)

        setResult(Activity.RESULT_OK)
        finish()
    }



}




