package otus.homework.customview.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import otus.homework.customview.data.UiDataFakeRepoImpl
import otus.homework.customview.data.UiDataRepo

@Module
interface AppModule {

    @Binds
    fun bindFakeRepo(repo: UiDataFakeRepoImpl): UiDataRepo

companion object{

    @Provides
    fun providesJson(): Json = Json { ignoreUnknownKeys = true}

}
}