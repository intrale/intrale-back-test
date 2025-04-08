package ar.com.intrale

import com.typesafe.config.ConfigFactory
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

val appModule = DI.Module("appModule") {

    bind <Config> {
        singleton {
            val configFactory = ConfigFactory.load()
            Config(
                businesses = configFactory.getString("app.avaiableBusinesses").split(",").toSet(),
                region = configFactory.getString("aws.region"),
                awsCognitoClientId = configFactory.getString("aws.cognito.clientId"),
            )
        }
    }

    bind <Logger> {
        singleton { LoggerFactory.getLogger("AppLogger") }
    }

    bind<Function> (tag="signup") {
        singleton {   SignUp(instance()) }
    }
    bind<Function> (tag="signin") {
        singleton {   SignIn() }
    }

}