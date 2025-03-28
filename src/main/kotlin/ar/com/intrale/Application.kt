package ar.com.intrale

//import ar.com.intrale.plugins.configureMonitoring
//import ar.com.intrale.plugins.configureRouting
//import ar.com.intrale.plugins.configureSerialization
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import org.kodein.di.ktor.di

/**
 * Config and initialize
 * for Microservice
 * with KTOR
 */

fun main() {
    embeddedServer(Netty, port = 8080/*, host = "0.0.0.0", module = Application::module*/){
        di {
            /* bindings */
            import(appModule)
        }

        routing {
            post ("/"){
                val function by closestDI().instance<Function>(tag = "function")

                val functionResponse = function.execute(call.receiveText())
                call.respondText(Gson().toJson(functionResponse))
            }
            options {
                call.response.headers.append("Access-Control-Allow-Origin", "*")
                call.response.headers.append("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST")
                call.response.headers.append("Access-Control-Allow-Headers", "Content-Type,Accept,Referer,User-Agent,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,Access-Control-Allow-Origin,Access-Control-Allow-Headers,function,idToken,businessName,filename")
                call.respond(HttpStatusCode.OK)
            }
        }

    }.start(wait = true)
}



