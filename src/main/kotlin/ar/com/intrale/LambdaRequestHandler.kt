package ar.com.intrale

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.ktor.utils.io.printStack

import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance

class LambdaRequestHandler  : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    override fun handleRequest(requestEvent: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent  = APIGatewayProxyResponseEvent().apply {
        try {
            val di = DI {
                import(appModule)
            }

            if (requestEvent != null) {
                var httpMehtod = requestEvent.httpMethod

                if (httpMehtod == "OPTIONS") {
                    val map = mutableMapOf<String, String>()
                    map["Access-Control-Allow-Origin"] = "*"
                    map["Access-Control-Allow-Methods"] = "GET, OPTIONS, HEAD, PUT, POST"
                    map["Access-Control-Allow-Headers"] =
                        "Content-Type,Accept,Referer,User-Agent,X-Amz-Date,Authorization,X-Api-Key,X-Amz-Security-Token,Access-Control-Allow-Origin,Access-Control-Allow-Headers,function,idToken,businessName,filename"
                    headers = map

                    statusCode = 200
                }

                if (httpMehtod == "POST") {
                    val function by di.instance<FunctionImpl>()
                    runBlocking {
                        function.execute(requestEvent.body)
                        body = "Hello from Kotlin Code"
                        statusCode = 200

                    }
                }


            }
        } catch (e: Exception) {
            e.printStackTrace()
            statusCode = 500
            body = "Internal Server Error"
        }
    }
}
