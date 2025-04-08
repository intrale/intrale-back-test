package ar.com.intrale

//import ar.com.intrale.plugins.configureMonitoring
//import ar.com.intrale.plugins.configureRouting
//import ar.com.intrale.plugins.configureSerialization
import com.google.gson.Gson
import io.github.flaxoos.ktor.server.plugins.ratelimiter.RateLimiting
import io.github.flaxoos.ktor.server.plugins.ratelimiter.implementations.TokenBucket
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di
import kotlin.time.Duration.Companion.seconds

/**
 * Config and initialize
 * for Microservice
 * with KTOR
 */

fun main() {
    embeddedServer(Netty/*, host = "0.0.0.0", module = Application::module*/) {

        install(RateLimiting) {

            rateLimiter {
                type = TokenBucket::class
                capacity = 100
                rate = 10.seconds
            }
        }

        di {
            /* bindings */
            import(appModule)
        }

        routing {
            post("/") {
                val functionName = call.request.headers["function"]
                val businessName = call.request.headers["business"]

                var functionResponse : Response

                if (businessName == null) {
                    functionResponse = RequestValidationException("No business defined on headers")
                } else {
                    val config by closestDI().instance<Config>()
                    if (!config.businesses.contains(businessName)){
                        functionResponse = ExceptionResponse("Business not avaiable with name $businessName")
                    } else {
                        if (functionName == null) {
                            functionResponse = RequestValidationException("No function defined on headers")
                        } else {
                            try {
                                val function by closestDI().instance<Function>(tag = functionName)
                                functionResponse = function.execute(call.receiveText())
                            } catch (e: DI.NotFoundException) {
                                functionResponse = ExceptionResponse("No function with name $functionName found")
                            }
                        }
                    }
                }

                call.respondText(
                    text = Gson().toJson(functionResponse),
                    contentType = ContentType.Application.Json,
                    status = functionResponse.statusCode
                )

            }
            options {
                call.response.headers.append("Access-Control-Allow-Origin", "*")
                call.response.headers.append("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST")
                call.response.headers.append(
                    "Access-Control-Allow-Headers",
                    "Content-Type,Accept,Referer,User-Agent,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,Access-Control-Allow-Origin,Access-Control-Allow-Headers,function,idToken,businessName,filename"
                )
                call.respond(HttpStatusCode.OK)
            }
        }


    }.start(wait = true)
}



