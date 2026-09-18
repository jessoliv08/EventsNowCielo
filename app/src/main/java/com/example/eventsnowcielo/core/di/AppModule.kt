package com.example.eventsnowcielo.core.di

import com.example.eventsnowcielo.core.network.NetworkModule
import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module(includes = [NetworkModule::class])
@ComponentScan("com.example.eventsnowcielo")
class AppModule