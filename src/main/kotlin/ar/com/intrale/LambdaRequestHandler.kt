package ar.com.intrale

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import com.google.gson.Gson

import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance
import org.kodein.di.ktor.closestDI
import java.lang.NullPointerException
import kotlin.getValue

class LambdaRequestHandler  : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    // The request limit most be assigned on Api Gateway
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
                    var functionName = requestEvent.headers.get("function")
                    val businessName = requestEvent.headers.get("business")

                    var functionResponse : Response = Response()

                    if (businessName == null) {
                        functionResponse = RequestValidationException("No business defined on headers")
                    } else {
                        val config by di.instance<Config>()
                        if (!config.businesses.contains(businessName)){
                            functionResponse = ExceptionResponse("Business not avaiable with name $businessName")
                        } else {
                            if (functionName == null) {
                                functionResponse = RequestValidationException("No function defined on headers")
                            } else {
                                try {
                                    val function by di.instance<Function>(tag = functionName)
                                    runBlocking {
                                        var requestBody:String = ""
                                        try {
                                            requestBody = requestEvent.body;
                                        } catch (e: NullPointerException){
                                            functionResponse = RequestValidationException("Request body not found")
                                        }
                                        if (requestBody.isNotEmpty()) {
                                            functionResponse = function.execute(requestBody)
                                        }

                                        body = Gson().toJson(functionResponse)
                                        statusCode = functionResponse.statusCode?.value
                                    }
                                } catch (e: DI.NotFoundException) {
                                    functionResponse = ExceptionResponse("No function with name $functionName found")
                                }
                            }
                        }
                    }

                    body = Gson().toJson(functionResponse)
                    statusCode = functionResponse.statusCode?.value

                }

            }
        } catch (e: Exception) {
            e.printStackTrace()
            statusCode = 500
            body = "Internal Server Error"
        }
    }
}
