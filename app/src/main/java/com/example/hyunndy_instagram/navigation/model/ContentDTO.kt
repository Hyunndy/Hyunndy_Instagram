package com.example.hyunndy_instagram.navigation.model

class ContentDTO( var explain : String? = null, //설명
                  var imageUrl : String? = null, // 이미지 주소
                  var uid : String? = null, // 어느유저가 올렸는지
                  var userId : String ? = null, // 올린 유저의 이미지를 관리해주는
                  var timestamp : Long? = null, // 몇 시 몇 분에 컨텐츠를 올렸는지 알려주는
                  var favoriteCount : Int = 0, // 좋아요 몇 개
                  var favorites : Map<String,Boolean> = HashMap()){ // 중복 좋아요를 방지하는 좋아하는 유저를 누른 관리
    data class  Comment( var uid : String? = null, // 유저 아이디
                         var userId : String? = null, // 이메일
                         var comment : String? = null, // 댓글
                         var timestamp: Long? = null) { // 언제 올렸는지
    }

}
