package com.yagubogu.presentation.livetalk

import androidx.lifecycle.ViewModel
import com.yagubogu.domain.repository.StadiumRepository

class LivetalkViewModel(
    private val stadiumRepository: StadiumRepository,
) : ViewModel()
