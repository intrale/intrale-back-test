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
            get("/") {
                val function by closestDI().instance<Function>(tag = "function")

                val functionResponse = FunctionImpl().execute("")
                call.respondText(Gson().toJson(functionResponse))
            }
            options {
                /*responseHeaders.put(FunctionConst.ACCESS_CONTROL_ALLOW_ORIGIN, FunctionConst.ALL);
                responseHeaders.put(FunctionConst.ACCESS_CONTROL_ALLOW_METHODS, FunctionConst.GET_OPTIONS_HEAD_PUT_POST);
                responseHeaders.put(FunctionConst.ACCESS_CONTROL_ALLOW_HEADERS, FunctionConst.ALLOW_HEADERS_AVAIABLES);*/
                call.response.headers.append("Access-Control-Allow-Origin", "*")
                call.response.headers.append("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST")
                call.response.headers.append("Access-Control-Allow-Headers", "Content-Type,Accept,Referer,User-Agent,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,Access-Control-Allow-Origin,Access-Control-Allow-Headers,function,idToken,businessName,filename")
                call.respond(HttpStatusCode.OK)
            }
        }

    }.start(wait = true)
}

fun Application.module() {
    //configureMonitoring()
    //configureSerialization()
    /*val di = DI {
        import(businessFunctionModule)
        import(appModule)
    }*/
    //(environment.config as DIAware).di = di
    configureRouting()
}

/*fun Application.configureMonitoring() {
    install(CallLogging) {
        level = Level.INFO
        filter { call -> call.request.path().startsWith("/") }
    }
}

fun Application.configureSerialization() {
    install(ContentNegotiation) {
        gson {
        }
    }
    routing {
        get("/json/gson") {
            //call.respond(mapOf("hello" to "world"))
            call.respond(Response("Hello World"))
        }
    }
}*/


fun Application.configureRouting() {
    routing {
        post ("/"){
            val di = call.closestDI()

            val function by di.instance<FunctionImpl>()

            val functionResponse = function.execute(call.receiveText())

            call.respondText(Gson().toJson(functionResponse))
        }
        get("/") {
            val di = call.closestDI()
            val function by di.instance<FunctionImpl>()

            val functionResponse = FunctionImpl().execute("")
            call.respondText(Gson().toJson(functionResponse))
        }
        options {
            /*responseHeaders.put(FunctionConst.ACCESS_CONTROL_ALLOW_ORIGIN, FunctionConst.ALL);
            responseHeaders.put(FunctionConst.ACCESS_CONTROL_ALLOW_METHODS, FunctionConst.GET_OPTIONS_HEAD_PUT_POST);
            responseHeaders.put(FunctionConst.ACCESS_CONTROL_ALLOW_HEADERS, FunctionConst.ALLOW_HEADERS_AVAIABLES);*/
            call.response.headers.append("Access-Control-Allow-Origin", "*")
            call.response.headers.append("Access-Control-Allow-Methods", "GET, OPTIONS, HEAD, PUT, POST")
            call.response.headers.append("Access-Control-Allow-Headers", "Content-Type,Accept,Referer,User-Agent,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,Access-Control-Allow-Origin,Access-Control-Allow-Headers,function,idToken,businessName,filename")
            call.respond(HttpStatusCode.OK)
        }
    }
}