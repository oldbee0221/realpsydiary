package kr.dformula.psydiary.ui.fargment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.addCallback
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_pingdatalk.view.*
import kotlinx.android.synthetic.main.item_detail.view.*
import kr.dformula.psydiary.R
import kr.dformula.psydiary.adapter.ViewPagerAdapter
import kr.dformula.psydiary.model.ContentDTO
import kr.dformula.psydiary.ui.CommentActivity
import kr.dformula.psydiary.ui.main.AddPhotoActivity
import android.text.method.ScrollingMovementMethod





class PingDaTalkFragment : Fragment() {

    var firestore: FirebaseFirestore? = null
    var uid: String? = null

/*
    //뷰페이저설정
    val pageMarginPx = resources.getDimensionPixelOffset(R.dimen.pageMargin) // dimen 파일 안에 크기를 정의해두었다.
    val pagerWidth = resources.getDimensionPixelOffset(R.dimen.pageWidth) // dimen 파일이 없으면 생성해야함
    val screenWidth = resources.displayMetrics.widthPixels // 스마트폰의 너비 길이를 가져옴
    val offsetPx = screenWidth - pageMarginPx - pagerWidth*/

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_pingdatalk, container, false).run {


        tb_pingDaTalk.navigationIcon = resources.getDrawable(R.drawable.ic_baseline_dehaze_24, null)
        tb_pingDaTalk.title = "핑다톡"




        tb_pingDaTalk.setNavigationOnClickListener {
            dL_sideBoard.open()
        }


        firestore = FirebaseFirestore.getInstance()
        uid = FirebaseAuth.getInstance().currentUser?.uid
        setBackButton()




        talk_hard_viewPager.adapter = ViewPagerAdapter(getIdolList()) // 어댑터 생성
        talk_hard_viewPager.orientation = ViewPager2.ORIENTATION_HORIZONTAL // 방향을 가로로
        talk_hard_viewPager.offscreenPageLimit = 1

   /*     talk_hard_viewPager.setPageTransformer { page, position ->
            page.translationX = position * -offsetPx
        }*/



        floatingActionButton.bringToFront();
        floatingActionButton2.bringToFront();




        floatingActionButton.setOnClickListener {

            activity?.let {
                val intent = Intent(context, AddPhotoActivity::class.java)
                startActivity(intent)

            }





            requireView().detailviewfragment_recyclerView?.adapter = DetailViewRecyclerViewAdapter()
            requireView().detailviewfragment_recyclerView?.layoutManager = LinearLayoutManager(activity)




        }



        rootView
    }

    private fun setBackButton() {
        requireActivity().onBackPressedDispatcher.addCallback(this@PingDaTalkFragment) {
            if (view?.dL_sideBoard?.isOpen == true)
                view?.dL_sideBoard?.close()
            else
                requireActivity().finish()
        }
    }

    private fun getIdolList(): ArrayList<Int> {
        return arrayListOf<Int>(R.drawable.camera_, R.drawable.crown_g__, R.drawable.crown_p__)
    }



    inner class DetailViewRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        var contentUidList: ArrayList<String> = arrayListOf()

        init {
            firestore?.collection("images")?.orderBy(
                "timestamp",
                Query.Direction.DESCENDING
            )?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                contentDTOs.clear()
                contentUidList.clear()
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot!!.documents) {
                    var item = snapshot.toObject(ContentDTO::class.java)
                    contentDTOs.add(item!!)
                    contentUidList.add(snapshot.id)
                }
                // 새로고침
                notifyDataSetChanged()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_detail, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int     {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var viewholder = (holder as CustomViewHolder).itemView

            //Id
            viewholder.detailviewitem_profile_textView.text = contentDTOs!![position].userId

            //Image (Glide)
            Glide.with(holder.itemView.context).load(contentDTOs!![position].imageUrl)
                .into(viewholder.detailviewitem_content_imageView)


            //Explain
            viewholder.detailviewitem_explain_textView.text = contentDTOs!![position].explain
            viewholder.detailviewitem_explain_textView1.text = contentDTOs!![position].explain1


            //category title
            viewholder.detailviewitem_category1.text= contentDTOs!![position].category_title
            viewholder.detailviewitem_category2.text= contentDTOs!![position].category_sub






            firestore?.collection("profileImages")
                ?.document(contentDTOs[position].uid!!)
                ?.get()
                ?.addOnCompleteListener { task ->
                    if(task.isSuccessful){

                        var url = task.result?.get("image")

                        if (url != null) {
                            Glide.with(holder.itemView.context).load(url)
                                .apply(RequestOptions().circleCrop())
                                .into(viewholder.detailviewitem_profile_image)
                        }

                    }


        }


        // 계정 이미지 눌렀을 때 (프로필로 이동)
        viewholder.detailviewitem_profile_image.setOnClickListener {
            profilemove(position)
        }

        // 계정 이름 눌렀을 때 (프로필로 이동)
        viewholder.detailviewitem_profile_textView.setOnClickListener {
            profilemove(position)
        }


        // 댓글 버튼 눌렀을 때
        viewholder.detailviewitem_comment_imageView.setOnClickListener { view ->
            var intent = Intent(view.context,CommentActivity::class.java)
            intent.putExtra("contentUid",contentUidList[position])
            intent.putExtra("destinationUid",contentDTOs[position].uid)
            intent.putExtra("putexplain",contentDTOs[position].explain)
            intent.putExtra("putexplain1",contentDTOs[position].explain1)
            intent.putExtra("useridtest",contentDTOs[position].userId)
            intent.putExtra("Imagetest",contentDTOs[position].imageUrl)
            intent.putExtra("category_title",contentDTOs[position].category_title)
            intent.putExtra("category_sub_title",contentDTOs[position].category_sub)


























            startActivity(intent)
        }

        /*if(contentDTOs!![position].favorites.containsKey(uid)){*/
        /*    // 좋아요 버튼이 눌려 있을 때*/
        /*    viewholder.detailviewitem_favorite_imageView.setImageResource(R.drawable.ic_favorite)*/
        /*}*/
        /*else{*/
        /*    // 좋아요 버튼이 눌려 있지 않을 때*/
        /*    viewholder.detailviewitem_favorite_imageView.setImageResource(R.drawable.ic_favorite_border)*/
        /*}*/
    }

    fun profilemove(position: Int){
        var fragment = HomeTownFragment()
        var bundle = Bundle()
        bundle.putString("destinationUid",contentDTOs[position].uid)
        bundle.putString("userId",contentDTOs[position].userId)
        fragment.arguments = bundle
        activity?.supportFragmentManager?.beginTransaction()?.replace(R.id.navHostfragment,fragment)?.commit()
    }

    fun favoriteEvent(position : Int){
        var tsDoc = firestore?.collection("images")?.document(contentUidList[position])
        firestore?.runTransaction{ transaction ->

            var contentDTO = transaction.get(tsDoc!!).toObject(ContentDTO::class.java)

            if(contentDTO!!.favorites.containsKey(uid)){
                // 좋아요 버튼이 눌려 있을 때 클릭 (취소)
                contentDTO?.favoriteCount = contentDTO?.favoriteCount - 1
                contentDTO?.favorites.remove(uid)

            }else{
                // 좋아요 버튼이 눌려 있지 않을 때 클릭 (좋아요)
                contentDTO?.favoriteCount = contentDTO?.favoriteCount + 1
                contentDTO?.favorites[uid!!] = true

            }
            transaction.set(tsDoc,contentDTO)
        }
    }







    }



}