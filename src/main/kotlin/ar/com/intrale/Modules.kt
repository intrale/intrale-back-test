package ar.com.intrale

import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.singleton

val appModule = DI.Module("appModule") {
    bind<Function> (tag="function") {
        singleton {   FunctionImpl() }
    }
    bind<Function> (tag="otherFunction") {
        singleton {   FunctionImpl() }
    }

}