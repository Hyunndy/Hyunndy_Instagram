package com.example.hyunndy_instagram

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    // Firebase Authentication 관리 클래스 ( 1. 이메일 2. 구글 3. 페이스북 )
    var auth : FirebaseAuth? = null

    // GoogleLogion 관리 클래스
    var googlesignInClient: GoogleSignInClient? = null

    // GoogleLogin
    var GOOGLE_LOGIN_CODE = 9001


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Firebase 로그인 통합 관리하는 object를 생성
        auth = FirebaseAuth.getInstance()

        // 구글 로그인 옵션
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        // 구글 로그인 클래스
        googlesignInClient = GoogleSignIn.getClient(this,gso)


        // 이메일 로그인 버튼 세팅
        email_login_button.setOnClickListener { view->  emailLogin() }

        // 구글 로그인 버튼 세팅
        google_sign_in_button.setOnClickListener { view->  googleLogin() }
    }

    // 구글 로그인(1) - googleLogin
    fun googleLogin(){
        var signInIntent = googlesignInClient?.signInIntent
        startActivityForResult(signInIntent,GOOGLE_LOGIN_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode==GOOGLE_LOGIN_CODE && resultCode == Activity.RESULT_OK){
            var result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if(result.isSuccess){
                var account = result.signInAccount
                // 구글 로그인(2) - Firebase 연결
                firebaseAuthWithGoogle(account)
            }
        }
    }

    fun firebaseAuthWithGoogle(account : GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken,null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener {task->
                // 로그인
                if(task.isSuccessful) {
                    moveMainPage(auth?.currentUser)
                }
                // 로그인 실패했을 때
                else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    // 회원가입 창을 만든다.
    fun createAndLoginEmail()
    {
        // Filebase로부터 유저 이메일/패스워드를 생성
        auth?.createUserWithEmailAndPassword(email_edittext.text.toString(), password_edittext.text.toString())?.addOnCompleteListener {
            task ->

            // 아이디가 생성이 성공했을 경우
            if(task.isSuccessful) {
                moveMainPage(task.result?.user)

            }
            //로그인 입력에 오류. 에러메세지 출력
            else if(task.exception?.message.isNullOrEmpty()){
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            }
            //아이디 생성도 안되고 에러도 발생되지 않았을 경우 로그인
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
            finish()
        }
    }

    fun emailLogin()
    {
        if(email_edittext.text.toString().isNullOrEmpty() || password_edittext.text.toString().isNullOrEmpty()){
            Toast.makeText(this, getString(R.string.signout_fail_null), Toast.LENGTH_SHORT).show()
        }
        else {
            createAndLoginEmail()
        }
    }

    override fun onStart() {
        super.onStart()

        // 자동 로그인
        moveMainPage(auth?.currentUser)
    }
}
