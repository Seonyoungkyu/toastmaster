package com.ykseon.toastmaster.common

import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SharedState @Inject constructor() {

    val testMode = MutableStateFlow(false)
}