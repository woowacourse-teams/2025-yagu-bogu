package com.yagubogu.presentation.livetalk

import androidx.lifecycle.ViewModel
import com.yagubogu.domain.repository.StadiumRepository

class LiveTalkViewModel(
    private val stadiumRepository: StadiumRepository,
) : ViewModel()
