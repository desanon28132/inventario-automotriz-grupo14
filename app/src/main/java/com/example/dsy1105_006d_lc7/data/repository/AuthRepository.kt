package com.example.dsy1105_006d_lc7.data.repository

import com.example.dsy1105_006d_lc7.data.model.Credential

class AuthRepository (
    private val validCredential: Credential = Credential.Admin
){
    fun login(username:String,password:String): Boolean{
        return username== validCredential.username && password ==validCredential.password
    }


}