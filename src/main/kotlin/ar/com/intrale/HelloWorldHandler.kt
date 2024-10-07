package ar.com.intrale

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent

class HelloWorldHandler  : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    override fun handleRequest(requestEvent: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent  = APIGatewayProxyResponseEvent().apply {
        body = "Hello from Kotlin Code"
    }


}