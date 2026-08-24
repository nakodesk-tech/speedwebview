package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.example.engine2.ui.SunbirdEngineScreen
import com.example.engine2.viewmodel.SunbirdEngineViewModel
import com.example.ui.DikshaSpeedScreen
import com.example.ui.theme.MinimalBlue
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.DikshaSpeedViewModel

class MainActivity : ComponentActivity() {

    private val engine1ViewModel: DikshaSpeedViewModel by viewModels()
    private val engine2ViewModel: SunbirdEngineViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                var selectedEngine by remember { mutableStateOf(1) } // 1 for Engine 1 (Default), 2 for Engine 2

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                ) {
                    // Quick Engine Switcher Bar at the top (Edge-to-Edge aware)
                    Surface(
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .statusBarsPadding()
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ENGINE SELECTOR",
                                color = Color(0xFF94A3B8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp,
                                modifier = Modifier.padding(start = 4.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                EngineSwitchTab(
                                    label = "Engine 1: Live Web",
                                    isSelected = selectedEngine == 1,
                                    onClick = { selectedEngine = 1 },
                                    testTag = "engine_1_tab"
                                )
                                EngineSwitchTab(
                                    label = "Engine 2: Sunbird Lab",
                                    isSelected = selectedEngine == 2,
                                    onClick = { selectedEngine = 2 },
                                    testTag = "engine_2_tab"
                                )
                            }
                        }
                    }

                    // Persistent Dual-Engine Container: Keep both WebViews alive across switches
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        // Engine 1: Live DIKSHA Web Engine
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(if (selectedEngine == 1) 2f else 0f)
                                .alpha(if (selectedEngine == 1) 1f else 0f)
                        ) {
                            DikshaSpeedScreen(
                                viewModel = engine1ViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Engine 2: Sunbird Video Player Web Component Lab
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .zIndex(if (selectedEngine == 2) 2f else 0f)
                                .alpha(if (selectedEngine == 2) 1f else 0f)
                        ) {
                            SunbirdEngineScreen(
                                viewModel = engine2ViewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EngineSwitchTab(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = if (isSelected) MinimalBlue else Color(0xFF1E293B),
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Color.White else Color(0xFF94A3B8),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
