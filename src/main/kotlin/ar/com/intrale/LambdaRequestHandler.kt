package ar.com.intrale

import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.RequestHandler
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import kotlinx.coroutines.runBlocking
import org.kodein.di.DI
import org.kodein.di.instance

class LambdaRequestHandler  : RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {


    override fun handleRequest(requestEvent: APIGatewayProxyRequestEvent?, context: Context?): APIGatewayProxyResponseEvent  = APIGatewayProxyResponseEvent().apply {
        body = "Hello from Kotlin Code"

        val di = DI {
            import(appModule)
        }

        if (requestEvent != null) {
            var httpMehtod = requestEvent.httpMethod
            val function by di.instance<FunctionImpl>()
            runBlocking {

                function.execute(requestEvent.body)
            }
        }



    }


}
