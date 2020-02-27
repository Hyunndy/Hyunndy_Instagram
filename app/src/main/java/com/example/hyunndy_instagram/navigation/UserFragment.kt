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
import com.example.hyunndy_instagram.navigation.model.FollowDTO
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

    companion object{
        var PICK_PROFILE_FROM_ALBUM = 10
    }

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

                fragmentView?.account_btn_follow_signout?.setOnClickListener { view->
                    requestFollow()
                }
            }
        }

        fragmentView?.account_recyclerview?.adapter = UserFragmentRecyclerViewAdapter()
        fragmentView?.account_recyclerview?.layoutManager = GridLayoutManager(activity!!, 3)

        fragmentView?.account_iv_profile?.setOnClickListener { view ->
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBUM)
        }

        getProfileImage()
        return fragmentView
    }

    fun requestFollow(){
        // 나의 계정에는 누구를 팔로우 하는지.
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if(followDTO == null){
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction // 트랜잭션을 닫음.
            }

            //  내가 상대방을 팔로우한 상태인 경우
            if(followDTO.followings.containsKey(uid)){
                // 팔로잉 취소
                followDTO?.followerCount = followDTO!!.followerCount -1
                followDTO?.followers?.remove(uid)
            }else{
                //팔로잉 함
                followDTO!!.followerCount = followDTO!!.followerCount + 1
                followDTO?.followers[uid!!] = true
            }

            // db로 저장
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }

        // 상대방 계정에는 또다른 타인이 팔로워하는 부분

        // 내가 팔로잉할 상대방의 계정에 접근하는 코드
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction{transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if(followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true

                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction
            }

            // it cancel my follower when i follow a third person
            if(followDTO!!.followers.containsKey(currentUserUid)){
                followDTO!!.followerCount = followDTO!!.followerCount -1
                followDTO!!.followers?.remove(currentUserUid!!)
            } else{
                // it add my foloowers when i dont follow a third person
                followDTO!!.followerCount = followDTO!!.followerCount +1
                followDTO!!.followers[currentUserUid!!] = true
            }

            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction

        }
    }

    fun getProfileImage(){
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
            if(documentSnapshot == null) return@addSnapshotListener

            if(documentSnapshot.data != null){
                var url = documentSnapshot?.data!!["image"]
                Glide.with(activity!!).load(url).apply(RequestOptions().circleCrop()).into(fragmentView?.account_iv_profile!!)
            }
        }
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