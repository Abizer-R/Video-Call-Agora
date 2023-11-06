package com.example.teachjr.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@InstallIn(SingletonComponent::class)
@Module
class FirebaseModule {

    @Provides
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    @Provides
    fun provideDatabase(): FirebaseDatabase {
        return FirebaseDatabase.getInstance()
    }

//    @Provides
//    fun provideAuthRepository(impl: AuthRepositoryImpl): AuthRepository {
//        /**
//         * Since we have constructor injection inside the 'AuthRepositoryImpl'
//         * We can directly get its instance here
//          */
//        return impl
//    }
}