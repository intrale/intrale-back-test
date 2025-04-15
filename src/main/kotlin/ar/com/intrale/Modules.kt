package ar.com.intrale

import com.typesafe.config.ConfigFactory
import net.datafaker.Faker
import org.kodein.di.DI
import org.kodein.di.bind
import org.kodein.di.instance
import org.kodein.di.singleton
import org.slf4j.Logger
import org.slf4j.LoggerFactory

private const val APP_AVAILABLE_BUSINESSES = "app.availableBusinesses"

private const val AWS_REGION = "aws.region"

private const val AWS_ACCESS_KEY_ID = "aws.accessKeyId"

private const val AWS_SECRET_ACCESS_KEY = "aws.secretAccessKey"

private const val AWS_COGNITO_USER_POOL_ID = "aws.cognito.userPoolId"

private const val AWS_COGNITO_CLIENT_ID = "aws.cognito.clientId"

val appModule = DI.Module("appModule") {

    bind <Config> {
        singleton {
            val configFactory = ConfigFactory.load()

            Config(
                businesses = if (System.getenv(APP_AVAILABLE_BUSINESSES)!=null)
                                    System.getenv(APP_AVAILABLE_BUSINESSES).split(",").toSet()
                                else configFactory.getString(APP_AVAILABLE_BUSINESSES).split(",").toSet(),
                region = System.getenv(AWS_REGION) ?: configFactory.getString(AWS_REGION),
                accessKeyId = System.getenv(AWS_ACCESS_KEY_ID) ?: configFactory.getString(AWS_ACCESS_KEY_ID),
                secretAccessKey = System.getenv(AWS_SECRET_ACCESS_KEY) ?: configFactory.getString(AWS_SECRET_ACCESS_KEY),
                awsCognitoUserPoolId = System.getenv(AWS_COGNITO_USER_POOL_ID) ?: configFactory.getString(AWS_COGNITO_USER_POOL_ID),
                awsCognitoClientId = System.getenv(AWS_COGNITO_CLIENT_ID) ?: configFactory.getString(AWS_COGNITO_CLIENT_ID),
            )
        }
    }

    bind<Faker> {
        singleton  {
            Faker()
        }
    }

    bind <Logger> {
        singleton { LoggerFactory.getLogger("AppLogger") }
    }

    bind<Function> (tag="signup") {
        singleton {   SignUp(instance(), instance(), instance()) }
    }
    bind<Function> (tag="signin") {
        singleton {   SignIn(instance(), instance(), instance()) }
    }

}