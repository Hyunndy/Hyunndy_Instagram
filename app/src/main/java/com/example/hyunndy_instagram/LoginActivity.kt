package com.example.hyunndy_instagram

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    // 로그인 라이브러리를 불러온다.
    var auth : FirebaseAuth? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        email_login_button.setOnClickListener { view->
            signinAndsignup()
        }
    }

    // 회원가입 창을 만든다.
    fun signinAndsignup()
    {
        // 이메일 받는 창
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener {
            task ->

            // 아이디가 생성되었을 때 필요한 코드를 입력해준다.
            if(task.isSuccessful) {
                moveMainPage(task.result?.user)

            }
            //로그인 입력에 오류. 에러메세지 출력
            else if(task.exception?.message.isNullOrEmpty()){
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
            //이미 계정이 있는 경우 로그인 홤녀으로 빠지도록
            else{
                signinEmail()
            }
        }
    }

    // 로그인
    fun signinEmail(){
        auth?.signInWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener {
                task ->

            // 로그인
            if(task.isSuccessful) {
                moveMainPage(task.result?.user)
            }
            // 로그인 실패했을 때
            else {
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    // 로그인 성공하면 다음페이지로 넘어가는 함수
    fun moveMainPage(user:FirebaseUser?) {
        if(user != null){
            startActivity(Intent(this, MainActivity::class.java))
        }
    }
}
