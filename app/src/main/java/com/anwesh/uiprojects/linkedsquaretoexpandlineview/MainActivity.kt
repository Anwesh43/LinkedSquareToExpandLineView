package com.anwesh.uiprojects.linkedsquaretoexpandlineview

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.anwesh.uiprojects.squaretoexpandlineview.SquareToExpandLineView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SquareToExpandLineView.create(this)
    }
}
