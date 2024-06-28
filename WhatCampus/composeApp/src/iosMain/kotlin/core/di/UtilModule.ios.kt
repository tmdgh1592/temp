package core.di

import core.common.util.UrlSharer
import org.koin.core.module.Module
import org.koin.dsl.module

actual val utilModule: Module = module {
    single {
        UrlSharer()
    }
}