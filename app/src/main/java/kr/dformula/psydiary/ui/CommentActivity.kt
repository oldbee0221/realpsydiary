package kr.dformula.psydiary.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_comment.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.activity_login.view.*
import kotlinx.android.synthetic.main.fragment_pingdatalk.*
import kotlinx.android.synthetic.main.fragment_pingdatalk.view.*
import kotlinx.android.synthetic.main.item_comment.view.*
import kotlinx.android.synthetic.main.item_detail.*
import kotlinx.android.synthetic.main.item_detail.view.*
import kr.dformula.psydiary.R
import kr.dformula.psydiary.model.ContentDTO
import org.techtown.stagram.navigation.model.AlarmDTO


class CommentActivity :  AppCompatActivity() {
    var contentUid : String? = null
    var destinationUid : String? = null
    var storage: FirebaseStorage? = null
    var explain : String? = null
    var explain1: String? = null
    var imageUrl : String? = null

    var Imagetest:String?= null

    var category_title : String? = null
    var category_sub_title : String? = null


    // 유저정보 가져오기
     var firestore: FirebaseFirestore? = null
     var auth: FirebaseAuth? = null
     var uid: String? = null






    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_comment)


        // 유저정보 가져오기
        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid

        //여기에서 Imagetest로 파라미터를 넘긴게 아니라 imagetest로 파라미터를 넘겨서 Intent에 담겨있는 Image주소를 얻지 못했었네요.
        // getString으로 받아도 toString 으로 한번 더 정리해줘야 하는 것 같습니다
        Imagetest = intent.getExtras()?.getString("Imagetest").toString();

        val id = intent.getStringExtra("putexplain1")
        txtId.text = intent.getStringExtra("putexplain1").toString()
        txtId.text = id.toString()

        val pw = intent.getStringExtra("putexplain")
        txtPw.text = intent.getStringExtra("putexplain").toString()
        txtPw.text = pw.toString()

        val userid = intent.getStringExtra("useridtest")
        txtid1.text = intent.getStringExtra("useridtest").toString()
        txtid1.text = userid.toString()

        category_title=intent.getStringExtra("category_title")
        category_sub_title = intent.getStringExtra("category_sub_title")

        val tv_category = findViewById(R.id.detailviewitem_category) as TextView
        tv_category.setText(category_title + category_sub_title)

        val img_test: ImageView = findViewById(R.id.img_test)

        val storage = FirebaseStorage.getInstance()
        //FirebaseStorage 전체 주소를 이미 리턴하고 있어서 Reference를 사용할 수 없습니다. 그냥 String으로 넘겼어야 합니다.
        //val storageRef = storage.reference.child(Imagetest)

        //Glide에서 바로 출력하고 placeHolder를 추가해서 예외처리를 하는것이 조금 더 깔끔합니다.
        Glide.with(this).load(Imagetest).into(img_test)

        /**
        storageRef.child(Imagetest.toString()).downloadUrl.addOnSuccessListener { uri -> //이미지 로드 성공시
            Glide.with(this)
                .load(imageUrl)
                .into(
                    img_test
                )
        }.addOnFailureListener { //이미지 로드 실패시
            Toast.makeText(this, "실패", Toast.LENGTH_SHORT).show()
        }
        **/


        contentUid = intent.getStringExtra("contentUid")
        destinationUid = intent.getStringExtra("destinationUid")




        comment_recyclerView.adapter = CommentRecyclerviewAdapter()
        comment_recyclerView.layoutManager = LinearLayoutManager(this)

        add_photo_line.bringToFront();
        comment_editView.bringToFront();
        comment_camera.bringToFront();
        comment_emoticon.bringToFront();
        comment_button.bringToFront()





        // 댓글 입력
        comment_button?.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = comment_editView.text.toString()
            comment.timestamp = System.currentTimeMillis()
            FirebaseFirestore.getInstance().collection("images").document(contentUid!!).collection("comments").document().set(comment)

            commentAlarm(destinationUid!!,comment_editView.text.toString())
            comment_editView.setText("")



        }

    }
    // 댓글 알림
    fun commentAlarm(destinationUid : String, message : String){
        var alarmDTO = AlarmDTO()
        alarmDTO.destinationUid = destinationUid
        alarmDTO.userId = FirebaseAuth.getInstance().currentUser?.email
        alarmDTO.kind = 1
        alarmDTO.uid = FirebaseAuth.getInstance().currentUser?.uid
        alarmDTO.timestamp = System.currentTimeMillis()
        alarmDTO.message = message
        FirebaseFirestore.getInstance().collection("alarms").document().set(alarmDTO)

    }




    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

        var comments : ArrayList<ContentDTO.Comment> = arrayListOf()
        init{
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener{querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if(querySnapshot == null)return@addSnapshotListener

                    for(snapshot in querySnapshot.documents!!){
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)
                    }
                    notifyDataSetChanged()
                }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view = LayoutInflater.from(parent.context).inflate(R.layout.item_comment,parent,false)
            return CustomViewHolder(view)
        }



        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.commentview_comment_textView.text = comments[position].comment
            view.commentview_profile_textView.text = comments[position].userId

            FirebaseFirestore.getInstance()
                ?.collection("profileImages")
                ?.document(comments[position].uid!!)
                ?.get()
                ?.addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        var url = task.result?.get("image")
                        if (url != null) {
                            Glide.with(holder.itemView.context).load(url)
                                .apply(RequestOptions().circleCrop())
                                .into(view.commentview_profile_imageView)
                        }
                    }
                }
        }








    }
}