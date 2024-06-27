package core.di

import feature.notice.NoticeDetailViewModel
import feature.notice.NoticeViewModel
import feature.university.UniversityViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

actual val viewModelModule: Module = module {
    viewModelOf(::UniversityViewModel)
    viewModelOf(::NoticeViewModel)
    viewModelOf(::NoticeDetailViewModel)
}
