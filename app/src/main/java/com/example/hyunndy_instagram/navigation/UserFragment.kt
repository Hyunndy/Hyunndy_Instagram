package com.example.hyunndy_instagram.navigation

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.hyunndy_instagram.LoginActivity
import com.example.hyunndy_instagram.MainActivity
import com.example.hyunndy_instagram.R
import com.example.hyunndy_instagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_user.view.*


class UserFragment : Fragment() {

    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null

    // 1. 내 계정에 대한 정보
    // 2. 상대방 계정에 대한 정보
    // 인지 판단해주는 변수
    var currentUserUid : String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentView =
            LayoutInflater.from(activity).inflate(R.layout.fragment_user, container, false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        currentUserUid = auth?.currentUser?.uid
        if(uid == currentUserUid){
            //나의 계정 페이지
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.signout)
            // 로그아웃
            fragmentView?.account_btn_follow_signout?.setOnClickListener { view ->
                activity?.finish()
                startActivity(Intent(activity,LoginActivity::class.java))
                auth?.signOut()
            }
        }else {
            // 다른 사람의 계정 페이지
            fragmentView?.account_btn_follow_signout?.text = getString(R.string.follow)
            fragmentView?.account_btn_follow_signout?.setOnClickListener { view ->
                var mainactivity = (activity as MainActivity)
                mainactivity?.toolbar_username?.text = arguments?.getString("userId")
                mainactivity?.toolbar_btn_back?.setOnClickListener { view ->
                    mainactivity.bottom_navigation.selectedItemId = R.id.action_home
                }
                mainactivity?.toolbar_title_image?.visibility = View.GONE
                mainactivity?.toolbar_username?.visibility = View.VISIBLE
                mainactivity?.toolbar_btn_back?.visibility = View.VISIBLE
            }
        }

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)
        return fragmentView
    }

    inner class UserFragmentRecyclerViewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentsDTOs: ArrayList<ContentDTO> = arrayListOf()

        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)
                ?.addSnapshotListener { querySnapshot, firebaseFirestoreException ->

                    // sometimes, this code return null of querySnapshot when it signout
                    if (querySnapshot == null) return@addSnapshotListener

                    // Get data
                    for (snapshot in querySnapshot.documents) {
                        contentsDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                    }
                    // post값에 배열크기 집어넣는다.
                    fragmentView?.account_tv_post_count?.text = contentsDTOs.size.toString()
                    //Recyclerview 새로고침
                    notifyDataSetChanged()
                }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

            // 화면의 폭
            var width = resources.displayMetrics.widthPixels / 3

            var imageView = ImageView(parent.context)
            imageView.layoutParams = LinearLayout.LayoutParams(width, width)
            return CustomViewHolder(imageView)
        }

        inner class CustomViewHolder(var imageView: ImageView) :
            RecyclerView.ViewHolder(imageView) {

        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageView = (holder as CustomViewHolder).imageView
            Glide.with(holder.itemView.context).load(contentsDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()
            ).into(imageView)
        }

        override fun getItemCount(): Int {
            return contentsDTOs.size
        }


    }
}